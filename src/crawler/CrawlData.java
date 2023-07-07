package crawler;

import crawler.DBPediaDataCrawler.DBPediaData;
import crawler.DataManage.DataHandling;
import crawler.DataManage.Merge;
import crawler.DataManage.ModifyData;
import crawler.WikiDataCrawler.*;

public class CrawlData {
    public static void main(String[] args) throws Exception {
        String wikiPath = "raw/Wikipedia/";
        String dbpediaPath = "raw/DBPedia/";
        
        WikiData wikiData = new WikiData(wikiPath);
        wikiData.setBruteForceLimit(0);
        wikiData.getData();
        
        DBPediaData dbpediaData = new DBPediaData(dbpediaPath);
        dbpediaData.getBruteForceData();
        dbpediaData.syncData();
        DataHandling.print("Done dbpediaData");

        Merge mergeData = new Merge();
        mergeData.merge("data/", wikiPath, dbpediaPath, Merge.createSource("Wikipedia"), Merge.createSource("DBPedia"));
        DataHandling.print("Done merge");

        ModifyData md = new ModifyData("data/","src/text-modify/");
        md.removeEntity();
        DataHandling.print("Finish");
    }

}
