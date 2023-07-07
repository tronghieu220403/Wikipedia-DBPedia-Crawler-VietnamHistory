package crawler.my_interface;

public interface WikiBruteForceCrawler {
    public void getVietnamRelatedEntity() throws Exception;

    public void getBruteForceData() throws Exception;
    public void entityAnalys(String url, int depth, boolean forceRelated) throws Exception;

    public void analyzeBruteForceData() throws Exception;
    public void urlToEntities() throws Exception;
    public void getWikiProperties() throws Exception;
    public void entityRefFinal() throws Exception;
    public void entityFinal() throws Exception;
    public void resetEntityRef() throws Exception;
}
