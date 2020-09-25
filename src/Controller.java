import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Controller
{
    public void downloadAChartFromBillboard(String chartName)
    {
        BillBoardChartScrapper billBoardChartScrapper = new BillBoardChartScrapper(chartName);
        billBoardChartScrapper.run();

        String address = billBoardChartScrapper.getChartsAddress();
        String name = billBoardChartScrapper.getChartName();

        SongsLinkFinder songsLinkFinder = new SongsLinkFinder(address, name, false);
        songsLinkFinder.run();
    }


    public void downloadArtistSongs(String artistName)
    {
        SpotifySongFinder spotifySongFinder = new SpotifySongFinder(artistName);
        spotifySongFinder.run();

        String address = spotifySongFinder.getAddress();
        String name = spotifySongFinder.getArtistName();

        SongsLinkFinder songsLinkFinder = new SongsLinkFinder(address, name, true);
        songsLinkFinder.run();
    }


    public void update()
    {
        String dataAddress = new File("").getAbsolutePath() + "\\data";
        String[] names = new File(Config.baseAddress).list();
        for (String name : names)
        {
            if (isChart(name, dataAddress))
            {
                downloadAChartFromBillboard(name);
            }
            else
            {
                downloadArtistSongs(name);
            }
        }
    }
    private boolean isChart(String name, String baseAddress)
    {
        try (BufferedReader br = new BufferedReader(new FileReader(baseAddress + "\\" + name + ".txt")))
        {
            String line = br.readLine();
            if (line.contains(","))
            {
                if (line.split(",")[1].split(" = ")[1].equals("Chart"))
                {
                    return true;
                }
            }
            return false;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
    }
}
