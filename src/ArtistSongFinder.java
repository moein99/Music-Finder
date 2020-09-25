import info.debatty.java.stringsimilarity.JaroWinkler;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;

public class ArtistSongFinder
{
    private String artistFullName;
    private int count;
    JaroWinkler jaroWinkler;
    JSONParser parser;
    String songsTxtAddress;

    ArtistSongFinder(String artistFullName)
    {
        this.artistFullName = artistFullName.toLowerCase();
        this.count = count;
        jaroWinkler = new JaroWinkler();
        parser = new JSONParser();
        songsTxtAddress = new File("").getAbsolutePath() + "\\data";
        File file = new File(songsTxtAddress);
        if (!file.exists())
        {
            file.mkdir();
        }
    }


    public void run()
    {
        ArrayList<String> songsList = new ArrayList<>();
        songsList = getSongsFromMyFreeMp3(songsList);
        songsList = getSongsFromSongList(songsList);
        saveSongs(songsList);
    }


    private void saveSongs(ArrayList<String> songs)
    {
        try(FileWriter fw = new FileWriter(songsTxtAddress + "\\" + artistFullName + ".txt");)
        {
            for (String song : songs)
            {
                fw.append(song + "%" + artistFullName);
                fw.append(String.format("%n"));
            }
            System.out.println("Songs Has Been Saved!");
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


    private JSONObject getJsonObjFromMyFreeMp3()
    {
        try
        {
            int id = Integer.MIN_VALUE;
            int owner_id = Integer.MIN_VALUE;
            String url = Config.myFreeMp3Api;
            URL obj = new URL(url);
            HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
            con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
            con.setRequestMethod("POST");
            String urlParameters = "q=" + artistFullName + "&" + "sort=2" + "&" + "count=300" + "&" + "performer_only=1" ;
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();
            int responseCode = con.getResponseCode();
            if (responseCode == 200)
            {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
                String inputLine;
                StringBuffer response = new StringBuffer();
                String total = "";
                while ((inputLine = in.readLine()) != null)
                {
                    response.append(inputLine);
                }
                in.close();
                response.deleteCharAt(0);
                response.deleteCharAt(response.length() - 1);
                response.deleteCharAt(response.length() - 1);
                String html = response.toString();
                html = html.replace("Query21302593753314606533_1524602101615(", "");
                return (JSONObject) parser.parse(html);
            }
            else
            {
                System.out.println("response code is : " + responseCode);
                return null;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }


    private ArrayList<String> getSongsFromMyFreeMp3(ArrayList<String> songsList)
    {
        JSONObject data = getJsonObjFromMyFreeMp3();
        String songName;
        String artist;
        JSONObject songDetails;
        boolean flag;
        if (data != null)
        {
            JSONArray jsonArray = (JSONArray) data.get("response");
            count = jsonArray.size();
            for (int i = 1; i < count; i++)
            {
                songDetails = (JSONObject) jsonArray.get(i);
                artist = songDetails.get("artist").toString();
                if (getSimilarity(artistFullName, artist) > 0.6)
                {
                    songName = capitalize(songDetails.get("title").toString().toLowerCase());
                    flag = false;
                    for (String name : songsList)
                    {
                        if (getSimilarity(name, songName) > 0.8)
                        {
                            flag = true;
                            break;
                        }
                    }
                    if (!flag)
                    {
                        songsList.add(songName);
                    }
                }
            }
        }
        return songsList;
    }


    private ArrayList<String> getSongsFromSongList(ArrayList<String> songsList)
    {
        String songName;
        StringBuilder sb;
        boolean flag;
        Document doc = Config.getDocument(Config.songListWebSite.replace("%*%", artistFullName.replace(" ", "")));
        if (doc != null)
        {
            Elements songs = doc.getElementsByClass("songitem-even");
            for (Element el : doc.getElementsByClass("songitem-odd")) {
                songs.add(el);
            }
            for (Element song : songs) {
                songName = song.getElementsByTag("a").text();
                flag = false;
                for (String name : songsList) {
                    if (getSimilarity(name, songName) > 0.8) {
                        flag = true;
                        break;
                    }
                }
                if (!flag) {
                    sb = new StringBuilder(songName);
                    sb.deleteCharAt(sb.length() - 1);
                    songsList.add(sb.toString());
                }
            }
        }
        return songsList;
    }


    public String getAddress()
    {
        return songsTxtAddress;
    }


    public String getArtistName() {
        return artistFullName;
    }


    private double getSimilarity(String s1, String s2)
    {
        if (s1.contains(s2) || s2.contains(s1))
        {
            return 1;
        }
        return jaroWinkler.similarity(s1.toLowerCase(), s2.toLowerCase());
    }
}