package crawler;

public class CrawlData {
    public static void main(String[] args) throws Exception {
        String wikiPath = "E:/Code/Java/OOP_Project/saveddata/Wikipedia/";
        String dbpediaPath = "E:/Code/Java/OOP_Project/saveddata/DBPedia/";
        WikiBruteForceData wikiBruteForceData = new WikiBruteForceData(wikiPath);
        wikiBruteForceData.setBruteForceAnalyseLitmit(0);
        wikiBruteForceData.getBruteForceData();

        WikiSelectiveData wikiSelectiveData = new WikiSelectiveData(wikiPath);
        wikiSelectiveData.selectiveDataQueries();
        wikiSelectiveData.analyzeSelectiveData();
        
        WikiTableData wikiTableData = new WikiTableData(wikiPath);
        wikiTableData.tableDataQueries();

        wikiBruteForceData.entityRefFinal();
        wikiBruteForceData.resetEntityRef();
        
        DBPediaData dbpediaData = new DBPediaData(dbpediaPath);
        dbpediaData.getBruteForceData();
        dbpediaData.syncData();

        Merge mergeData = new Merge();
        mergeData.merge("data", dbpediaPath, Merge.createSource("Wikipedia"), Merge.createSource("DBPedia"));

        ModifyData md = new ModifyData();
        md.removeEntity();
    }

}
