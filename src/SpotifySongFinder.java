import com.wrapper.spotify.model_objects.specification.AlbumSimplified;
import com.wrapper.spotify.model_objects.specification.Artist;
import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import com.wrapper.spotify.model_objects.specification.TrackSimplified;
import info.debatty.java.stringsimilarity.JaroWinkler;
import javafx.util.Pair;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.util.ArrayList;

public class SpotifySongFinder
{
    private String artistFullName;
    private JaroWinkler jaroWinkler;
    private String songsTxtAddress;
    private Spotify spotify;
    private ArrayList<String> allSongs;


    SpotifySongFinder(String artistFullName)
    {
        spotify = new Spotify();
        allSongs = new ArrayList<>();
        this.artistFullName = capitalize(artistFullName.toLowerCase());
        jaroWinkler = new JaroWinkler();
        songsTxtAddress = new File("").getAbsolutePath() + "\\data";
        File file = new File(songsTxtAddress);
        if (!file.exists()) {
            file.mkdir();
        }
    }

    public void run() {
        String artistID = "";
        Artist[] set = spotify.searchArtist(artistFullName);
        for (Artist artist : set) {
            if (jaroWinkler.similarity(artist.getName().toLowerCase(), artistFullName.toLowerCase()) > 0.9) {
                artistID = artist.getId();
                break;
            }
        }
        if (artistID.equals("")) {
            System.out.println("Couldn't find such a name");
            return;
        }


        AlbumSimplified[] albums = spotify.getArtistAlbums(artistID, "album");
        ArrayList<AlbumSimplified> mainAlbums = new ArrayList<>();
        ArrayList<Pair<String, TrackSimplified>> extentions = new ArrayList<>();

        for (int i = 0; i < albums.length; i++)
        {
            String temp = albums[i].getName();
            AlbumSimplified mainAlbum = checkDuplicate(temp, mainAlbums);
            if (mainAlbum != null)
            {
                TrackSimplified[] albumSongs = spotify.getSongsOfAnAlbum(mainAlbum.getId());
                TrackSimplified[] auxAlbum = spotify.getSongsOfAnAlbum(albums[i].getId());
                for (TrackSimplified track : auxAlbum)
                {
                    String trackName = track.getName().split(" - ")[0];
                    if (!albumContainsSong(albumSongs, trackName) && !extentionsContainsSong(extentions, trackName))
                    {
                        extentions.add(new Pair<>(mainAlbum.getName(), track));
                    }
                }
            }
            else
            {
                mainAlbums.add(albums[i]);
            }
        }
        saveAlbums(mainAlbums, extentions);

        AlbumSimplified[] singles = spotify.getArtistAlbums(artistID, "single");
        ArrayList<String> singleSongsNames = new ArrayList<>();
        ArrayList<String> singlesToBeSaved = new ArrayList<>();
        for (int k = 0; k < singles.length; k++)
        {
            for (TrackSimplified track : spotify.getSongsOfAnAlbum(singles[k].getId()))
            {
                singleSongsNames.add(track.getName());
            }
        }
        singlesToBeSaved = getNewOnes(singleSongsNames, singlesToBeSaved);
        singles = spotify.getArtistAlbums(artistID, "appears_on");
        singleSongsNames = new ArrayList<>();
        for (int k = 0; k < singles.length; k++)
        {
            for (TrackSimplified track : spotify.getSongsOfAnAlbum(singles[k].getId()))
            {
                for (ArtistSimplified artist : track.getArtists())
                {
                    if (artist.getId().equals(artistID))
                    {
                        singleSongsNames.add(track.getName());
                        break;
                    }
                }
            }
        }
        singlesToBeSaved = getNewOnes(singleSongsNames, singlesToBeSaved);
        saveSecondarySongs(singlesToBeSaved);
    }


    private ArrayList<String> getNewOnes(ArrayList<String> totalSongs, ArrayList<String> toBeSaved)
    {
        for (String SongName : totalSongs)
        {
            boolean flag = true;
            if (SongName.contains(" - "))
            {
                SongName = SongName.split(" - ")[0];
            }
            if (SongName.contains("("))
            {
                if (SongName.indexOf("(") != 0)
                    SongName = SongName.substring(0, SongName.indexOf("(") - 1);
            }
            for (String name : allSongs)
            {
                if (SongName.contains(name) || name.contains(SongName))
                {
                    flag = false;
                }
            }
            if (flag)
            {
                allSongs.add(SongName);
                toBeSaved.add(SongName);
            }
        }
        return toBeSaved;
    }


    private AlbumSimplified checkDuplicate(String name, ArrayList<AlbumSimplified> curAlbums)
    {
        for (AlbumSimplified album : curAlbums)
        {
            if (name.contains(album.getName()))
            {
                return album;
            }
        }
        return null;
    }


    private boolean extentionsContainsSong(ArrayList<Pair<String, TrackSimplified>> extentions, String trackName)
    {
        for (int i = 0; i < extentions.size(); i++)
        {
            if (extentions.get(i).getValue().getName().split(" - ")[0].equals(trackName))
            {
                return true;
            }
        }
        return false;
    }


    private boolean albumContainsSong(TrackSimplified[] albumSongs, String songName)
    {
        for (TrackSimplified track : albumSongs)
        {
            if (track.getName().contains(songName) || songName.contains(track.getName()))
            {
                return true;
            }
        }
        return false;
    }


    private void saveAlbums(ArrayList<AlbumSimplified> albums, ArrayList<Pair<String, TrackSimplified>> extentions)
    {

        try(FileWriter fw = new FileWriter(songsTxtAddress + "\\" + artistFullName + ".txt");)
        {
            for (int i = 0; i < albums.size(); i++)
            {
                TrackSimplified[] tracksOfAnAlbum = spotify.getSongsOfAnAlbum(albums.get(i).getId());
                String albumName = albums.get(i).getName();
                if (albumName.contains("(Deluxe)"))
                {
                    albumName = albumName.replace(" (Deluxe)", "");
                }
                fw.append("Album = " + albumName);
                fw.append(String.format("%n"));
                for (int j = 0; j < tracksOfAnAlbum.length; j++)
                {
                    String songName = tracksOfAnAlbum[j].getName();
                    if (songName.contains("-"))
                    {
                        songName = songName.split(" - ")[0];
                    }
                    allSongs.add(songName);
                    fw.append(songName);
                    fw.append(String.format("%n"));
                }
                for (Pair<String, TrackSimplified> ext : extentions)
                {
                    if (ext.getKey().equals(albums.get(i).getName()))
                    {
                        fw.append(ext.getValue().getName().split(" - ")[0]);
                        fw.append(String.format("%n"));
                    }
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    private void saveSecondarySongs(ArrayList<String> singles)
    {
        try(FileWriter fw = new FileWriter(songsTxtAddress + "\\" + artistFullName + ".txt", true);)
        {
            fw.append("Album = Singles");
            fw.append(String.format("%n"));
            for (String name : singles)
            {
                fw.append(name);
                fw.append(String.format("%n"));
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    private String capitalize(String name)
    {
        String finalStr = "";
        while (name.contains("  "))
        {
            name = name.replace("  ", " ");
        }
        for (String part : name.split(" "))
        {
            try {
                if (part.length() == 1) {
                    finalStr += part.toUpperCase() + " ";
                } else {
                    finalStr += part.substring(0, 1).toUpperCase() + part.substring(1) + " ";
                }
            }
            catch (java.lang.StringIndexOutOfBoundsException e)
            {
                System.out.println("inside capitalize");
                System.out.println(name);
            }
        }
        return finalStr.substring(0, finalStr.length() - 1);
    }

    public String getAddress()
    {
        return songsTxtAddress;
    }


    public String getArtistName()
    {
        return artistFullName;
    }
}

