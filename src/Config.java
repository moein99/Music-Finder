import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;

public class Config
{
    public static String baseAddress = "E:\\Music\\Billboard\\";
    public static String musicPleerBaseAddress = "https://musicpleer.to";
    public static String songListWebSite = "http://www.song-list.net/%*%/songs";
    public static String myFreeMp3Api = "https://my-free-mp3.net/api/search.php?callback=jQuery21302593753314606533_1524602101615";
    public static String billboardChartsLink = "https://www.billboard.com/charts";
    public static String songsClassNameInBillboardHot100 = "chart-row__title";
    public static String billboardBaseAddress = "https://www.billboard.com";
    public static String clientId = "4a3287cfe90f4379966e3bf4e1283c53";
    public static String clientSecret = "fb58a575438844da842050e15f184f26";


    public static Document getDocument(String url)
    {
        Document document;
        try
        {
            document = Jsoup.connect(url).get();
            return document;
        }
        catch (org.jsoup.HttpStatusException e)
        {
            System.out.println(e.getStatusCode() + " on " + e.getUrl());
            return null;
        }
        catch (java.net.SocketTimeoutException | java.net.UnknownHostException e)
        {
            System.out.println("Time out exception ...! \nRunning again ...!");
            return getDocument(url);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
