package crawler.my_interface;

public interface WikiCrawler{
    public void getData() throws Exception;
    public void getBruteForceData() throws Exception;
    public void getSelectiveData() throws Exception;
    public void getTableData() throws Exception;
    public void rewriteReferences() throws Exception;
    public void export() throws Exception;
}