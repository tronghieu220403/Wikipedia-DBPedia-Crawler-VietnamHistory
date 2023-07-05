package crawler;

public class CrawlData {
    public static void main(String[] args) throws Exception {
        String wikiPath = "E:/Code/Java/OOP_Project/saveddata/Wikipedia/";
        String dbpediaPath = "E:/Code/Java/OOP_Project/saveddata/DBPedia/";
        
        WikiBruteForceData wikiBruteForceData = new WikiBruteForceData(wikiPath);
        wikiBruteForceData.setBruteForceAnalyseLitmit(0);
        wikiBruteForceData.getBruteForceData();
        DataHandling.print("Done brute force");

        WikiSelectiveData wikiSelectiveData = new WikiSelectiveData(wikiPath);
        wikiSelectiveData.selectiveDataQueries();
        wikiSelectiveData.analyzeSelectiveData();
        DataHandling.print("Done selective");
        
        WikiTableData wikiTableData = new WikiTableData(wikiPath);
        wikiTableData.tableDataQueries();
        DataHandling.print("Done table");

        wikiBruteForceData.entityRefFinal();
        wikiBruteForceData.resetEntityRef();
        DataHandling.print("Done rewrite ref");

        WikiDataExport wikiDataExport = new WikiDataExport(wikiPath);
        wikiDataExport.export();
        DataHandling.print("Done export");
        
        DBPediaData dbpediaData = new DBPediaData(dbpediaPath);
        dbpediaData.getBruteForceData();
        dbpediaData.syncData();
        DataHandling.print("Done dbpediaData");

        Merge mergeData = new Merge();
        mergeData.merge("data", wikiPath, dbpediaPath, Merge.createSource("Wikipedia"), Merge.createSource("DBPedia"));

        DataHandling.print("Done merge");

        ModifyData md = new ModifyData();
        md.removeEntity();
        DataHandling.print("Finish");
    }

}
