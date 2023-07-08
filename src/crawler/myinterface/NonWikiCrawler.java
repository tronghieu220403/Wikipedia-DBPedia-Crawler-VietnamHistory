package crawler.myinterface;

public interface NonWikiCrawler {
    public void getData() throws Exception;
    public void syncData(String wikiPath) throws Exception;
}
