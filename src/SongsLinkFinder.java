import info.debatty.java.stringsimilarity.JaroWinkler;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openqa.selenium.*;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;


public class SongsLinkFinder
{
    private String musicPleerBaseAddress = Config.musicPleerBaseAddress;
    private String songsTxtAddress;
    private String songsDlLinksTxtAddress;
    private JSONParser parser;
    private JSONObject json;
    private String artistName;
    private String chartName;
    private Downloader downloader;
    private boolean isArtist;
    private String fileExt = ".mp3";


    SongsLinkFinder(String address, String artistName, boolean isArtist)
    {
        this.artistName = artistName;
        if (isArtist)
        {
            this.artistName = artistName;
        }
        else
        {
            this.chartName = artistName;
        }
        this.songsDlLinksTxtAddress = address + "\\" + this.artistName + "_DlLinks.txt";
        this.songsTxtAddress = address + "\\" + this.artistName + ".txt";
        parser = new JSONParser();
        downloader = new Downloader();
        this.isArtist = isArtist;
    }


    public void run()
    {
        ArrayList<Album> content = getToBeFounded();
        ArrayList<String> saved = getAlreadySaved();
        String downloadLink = "";
        int response;
        String songName;
        for (Album album : content)
        {
            for (String trackName : album.songsNames)
            {
                if (!isArtist)
                {
                    String[] details = trackName.split("%");
                    songName = details[0];
                    artistName = details[1];
                    trackName = songName + " " + artistName;
                }
                if (!saved.contains(trackName))
                {
                    response = 0;
                    downloadLink = myFreeMp3GetDownloadLink(artistName, trackName.replace(" " + artistName, ""));
                    String songFullAddress;
                    String fixedAlbumName = fixName(album.name);
                    String fixedTrackName = fixName(trackName);
                    if (isArtist)
                    {
                        songFullAddress = Config.baseAddress + artistName + "\\" + fixedAlbumName + "\\" + fixedTrackName + fileExt;
                    }
                    else
                    {
                        songFullAddress = Config.baseAddress + chartName + "\\" + fixedTrackName + fileExt;
                    }
                    File file = new File(songFullAddress.replace(fixedTrackName + fileExt, ""));
                    if (!file.exists())
                    {
                        file.mkdirs();
                    }
                    if (downloadLink != null && !downloadLink.equals(""))
                    {
                        String temp;
                        if (isArtist)
                        {
                            temp = "Album : ";
                        }
                        else
                        {
                            temp = "Chart : ";
                        }
                        System.out.println("Downloading of [Song Name : " + trackName.replace(" " + artistName, "") + " - Artist : " + artistName
                        + " - " + temp + album.name + "] Started!");
                        response = downloader.download(downloadLink, songFullAddress);
                        if (response == 0)
                        {
                            System.out.println("Couldn't download  " + trackName.replace(" " + artistName, "") + " Artist : " + artistName
                                    + "Album : " + album.name);
                        }
                        else
                        {
                            System.out.println("Downloading of " + trackName.replace(" " + artistName, "") + " is finished!");
                        }
                    }
                    if (response == 1 && downloadLink != null)
                    {
                        saveLink(trackName, downloadLink);
                    }
                }
            }
        }
    }


    private ArrayList<Album> getToBeFounded()
    {
        ArrayList<Album> albums = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(songsTxtAddress)))
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
                    if (!isArtist)
                    {
                        album.name = line.split(",")[0].split(" = ")[1];
                    }
                    else
                    {
                        album.name = line.split(" = ")[1];
                    }
                }
                else
                {
                    album.songsNames.add(line);
                }
            }
            albums.add(album);
            return albums;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }


    private ArrayList<String> getAlreadySaved()
    {
        ArrayList<String> saved = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(songsDlLinksTxtAddress)))
        {
            String line;
            while ((line = br.readLine()) != null)
            {
                saved.add(line.split(" Download Link:")[0]);
            }
        }
        catch (FileNotFoundException e)
        {
            System.out.println("running for first time!");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return saved;
    }


    private String musicPleerGetDownloadLink(String trackName)
    {
        JaroWinkler jaroWinkler = new JaroWinkler();
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setJavascriptEnabled(true);
        caps.setCapability("takesScreenshot", false);
        caps.setCapability(
                PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
                "E:\\RL\\Programming\\Java\\Mp3Finder" + "\\phantomjs.exe"
        );
        WebDriver driver = new  PhantomJSDriver(caps);
        driver.get(musicPleerBaseAddress + "/#!" + trackName.replace(" ", "+"));
        try
        {
            Thread.sleep(500);
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        ArrayList<WebElement> results = new ArrayList<WebElement>(driver.findElements(By.cssSelector(".ui-li-has-thumb.ui-li-has-count")));
        for (WebElement song : results)
        {
            double songsSimilarity = jaroWinkler.similarity(song.findElement(By.tagName("h3")).getText(), trackName);
            if (songsSimilarity > 0.8)
            {
                driver.get(musicPleerBaseAddress + song.findElement(By.tagName("a")).getAttribute("href"));
                WebDriverWait wait = new WebDriverWait(driver, 15);
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("download-btn")));
                return driver.findElement(By.id("download-btn")).getAttribute("href");
            }
        }
        return null;
    }


    private String myFreeMp3GetDownloadLink(String artistName, String songName)
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
            String urlParameters = "q=" + artistName + " " + songName;
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
                json = (JSONObject) parser.parse(html);
                JSONArray jsonArray = (JSONArray) json.get("response");
                JSONObject songDetails;
                JaroWinkler jaroWinkler = new JaroWinkler();
                if (jsonArray != null && jsonArray.size() > 1)
                {
                    JSONObject result = null;
                    for (int i = 1; i < jsonArray.size(); i++)
                    {
                        if (i == 1)
                        {
                            result = (JSONObject) jsonArray.get(i);
                            if (jaroWinkler.similarity(result.get("artist").toString().toLowerCase(), artistName.toLowerCase()) > 0.85)
                            {
                                if (jaroWinkler.similarity(result.get("artist").toString().toLowerCase(), artistName.toLowerCase()) > 0.85)
                                {
                                    break;
                                }
                            }
                        }
                        else
                        {
                            songDetails = (JSONObject) jsonArray.get(i);
                            if (jaroWinkler.similarity(songDetails.get("artist").toString().toLowerCase(), artistName.toLowerCase()) > 0.85)
                            {
                                if (jaroWinkler.similarity(songName, songDetails.get("title").toString()) > 0.85)
                                {
                                    result = songDetails;
                                    break;
                                }
                            }
                        }
                    }
                    id = Integer.parseInt(result.get("id").toString());
                    owner_id = Integer.parseInt(result.get("owner_id").toString());
                    return "https://newtabs.stream/" + myFreeMp3Encode(owner_id) + ":" + myFreeMp3Encode(id);
                }
                else
                {
                    return null;
                }
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
            System.out.println(songName);
            return null;
        }
    }


    private String myFreeMp3Encode(int input)
    {
        char chars[] = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'j', 'k', 'm', 'n', 'p', 'q', 'r', 's', 't', 'u', 'v', 'x', 'y', 'z', '1', '2', '3'};
        int length = chars.length;
        String encoded = "";
        int temp;
        if (input == 0)
        {
            encoded += chars[0];
            return encoded;
        }
        if (input < 0)
        {
            input *= -1;
            encoded += "-";
        }
        while (input > 0)
        {
            temp = input % length;
            input = input / length;
            encoded += chars[temp];
        }
        return encoded;
    }


    private void saveLink(String songName, String downloadLink)
    {
        String toBeSaved = songName + " Download Link:" + downloadLink;
        try(FileWriter fw = new FileWriter(songsDlLinksTxtAddress, true);)
        {
            fw.append(toBeSaved);
            fw.append(String.format("%n"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    public String fixName(String fileName)
    {
        String[] notAllowedWords = {"CON", "PRN", "AUX", "NUL", "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"};
        if (fileName.length() > 200)
        {
            fileName = fileName.substring(0,200);
        }
        fileName = fileName.replaceAll("[<>:\"/|?*]", "").replace("\\", "");
        for (String s : notAllowedWords)
        {
            if (fileName.contains(s))
            {
                fileName = fileName.replace(s, "");
            }
        }
        return fileName;
    }
}