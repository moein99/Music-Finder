import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.specification.Artist;
import com.wrapper.spotify.model_objects.specification.TrackSimplified;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import javax.print.attribute.standard.Fidelity;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

import com.neovisionaries.i18n.CountryCode;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.AlbumSimplified;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.requests.data.albums.GetAlbumsTracksRequest;
import com.wrapper.spotify.requests.data.artists.GetArtistsAlbumsRequest;
import javafx.util.Pair;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.http.HttpClient;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

class myException extends Exception
{
    public myException() {}

    public myException(String message)
    {
        super(message);
    }
}
public class Main
{
    public static void main(String args[]) throws Exception
    {
        String name = "Nicole Scherzinger";
        Controller controller = new Controller();
        controller.downloadArtistSongs(name);
        //controller.downloadAChartFromBillboard("hot 100");
        //controller.update();
        /*String baseAddress = "E:\\Music\\Billboard\\" + name;
        File file = new File(baseAddress);
        String[] files = file.list();
        ArrayList<Album> albums = new ArrayList<>();
        for (String file1: files)
        {
            if (file1.contains(".mp3"))
            {
                if (file1.charAt(file1.length() - 5) == ' ')
                {
                    File kop = new File(baseAddress + "\\" + file1);
                    kop.renameTo(new File((baseAddress + "\\" + file1).replace(" .mp3", ".mp3")));
                }
            }
        }
        files = file.list();
        try (BufferedReader br = new BufferedReader(new FileReader("E:\\RL\\Programming\\Java\\Mp3Finder\\data\\" + name + ".txt")))
        {
            String line;
            Album album = null;
            while ((line = br.readLine()) != null)
            {
                if (line.contains("Album"))
                {
                    if (album != null)
                    {
                        albums.add(album);
                    }
                    album = new Album();
                    album.name = line.split(" = ")[1];
                }
                else
                {
                    album.songsNames.add(line);
                }
            }
            albums.add(album);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        for (String f : files)
        {
            if (f.contains(".mp3"))
            {
                File file1 = new File(baseAddress + "\\" + f);
                String albumName = null;
                for (Album album : albums)
                {
                    if (album.songsNames.contains(f.replace(".mp3", "")))
                    {
                        albumName = album.name;
                        break;
                    }
                }
                File temp = new File(baseAddress + "\\" + albumName);
                if (!temp.exists())
                {
                    temp.mkdirs();
                }
                file1.renameTo(new File(baseAddress + "\\" + albumName + "\\" + f));
            }
        }*/
    }
}