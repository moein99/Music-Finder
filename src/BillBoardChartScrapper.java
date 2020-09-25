import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.*;
import java.util.ArrayList;

public class BillBoardChartScrapper
{
    private String songsClassNameInBillboard = Config.songsClassNameInBillboardHot100;
    private String chartsAddress;
    private String chartsLink = Config.billboardChartsLink;
    private String chartName;


    BillBoardChartScrapper(String chartName)
    {
        this.chartName = chartName.toLowerCase();
        chartsAddress = new File("").getAbsolutePath() + "\\data";
        File file = new File(chartsAddress);
        if (!file.exists())
        {
            file.mkdir();
        }
    }


    public void run()
    {
        String link = Config.billboardBaseAddress + getChartLink();
        if (link == null)
        {
            System.out.println("This chart doesn't exist!");
            return;
        }
        Document document = Config.getDocument(link);
        Elements songs = document.getElementsByClass(songsClassNameInBillboard);
        String songName = "";
        String songArtist = "";
        String songFullName = "";
        ArrayList<String> songsDetail = new ArrayList<>();
        for (Element song : songs)
        {
            songName = song.getElementsByTag("h2").text();
            songArtist = song.getElementsByTag("span").text();
            if (songArtist.equals(""))
            {
                songArtist = song.getElementsByTag("a").text();
            }
            songFullName = songName + "%" + songArtist;
            songsDetail.add(songFullName);
        }
        saveSongsNames(songsDetail);
    }


    private String getChartLink()
    {
        Document document = Config.getDocument(chartsLink);
        Elements charts = document.getElementsByClass("chart-row__chart-link");
        for (Element chart : charts)
        {
            if (chart.text().toLowerCase().equals(chartName))
            {
                return chart.attr("href");
            }
        }
        return null;
    }


    private void saveSongsNames(ArrayList<String> songsDetail)
    {
        try(FileWriter fw = new FileWriter(chartsAddress + "\\" + chartName + ".txt");)
        {
            fw.append("Album = " + chartName + ",Type = Chart");
            fw.append(String.format("%n"));
            for (String song : songsDetail)
            {
                fw.append(song);
                fw.append(String.format("%n"));
            }
            System.out.println("Songs Has Been Saved!");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    public String getChartsAddress()
    {
        return chartsAddress;
    }


    public String getChartName()
    {
        return chartName;
    }
}
