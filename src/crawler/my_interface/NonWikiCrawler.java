package crawler.my_interface;

public interface NonWikiCrawler {
    public void getData() throws Exception;
    public void syncData(String wikiPath) throws Exception;
}
