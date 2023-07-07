package crawler;

import crawler.dbpedia_data_crawler.DBPediaData;
import crawler.data_manage.DataHandling;
import crawler.data_manage.Merge;
import crawler.data_manage.ModifyData;
import crawler.wiki_data_crawler.*;

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
