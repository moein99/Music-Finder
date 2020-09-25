import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;


public class Downloader
{
    public int download(String url, String fullAddress)
    {
        BufferedInputStream in = null;
        RandomAccessFile raf = null;
        int fileLength = -1;
        int returnValue = 0;
        try
        {
            URL link = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) link.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
            if (conn.getResponseCode() == 302)
            {
                link = new URL(conn.getHeaderField("Location"));
                conn = (HttpURLConnection) link.openConnection();
            }
            conn.connect();
            if (conn.getResponseCode() == 200)
            {
                in = new BufferedInputStream(conn.getInputStream());
                fileLength = conn.getContentLength();
                raf = new RandomAccessFile(fullAddress, "rw");
                int BUFFER_SIZE = 8192;
                byte data[] = new byte[BUFFER_SIZE];
                int numRead;
                while (((numRead = in.read(data, 0, BUFFER_SIZE)) != -1))
                {
                    raf.write(data, 0, numRead);
                }
            }
            else
            {
                System.out.println(conn.getResponseCode());
            }
        }
        catch (java.net.SocketException e)
        {
            System.out.println("Socket Exception!");
            download(url, fullAddress);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (raf != null)
            {
                try
                {
                    long size = raf.length();
                    raf.close();
                    if (size < fileLength - 1000 || size/1000 < 100)
                    {
                        File file = new File(fullAddress);
                        file.delete();
                    }
                    else
                    {
                        returnValue = 1;
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            if (in != null)
            {
                try
                {
                    in.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return returnValue;
    }
}
