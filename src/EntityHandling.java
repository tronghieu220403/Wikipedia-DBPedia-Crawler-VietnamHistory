import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


public class EntityHandling extends DataHandling{
    protected boolean isRelated = false;
    protected HashSet<String> vietnamEntityHashSet = new HashSet<>();
    protected HashSet<String> propertyHashSet = new HashSet<>();
    //root path:
    protected String superpath;
    //files
    protected String beginURLsPath;
    protected String craftedURLsPath;
    protected String analysedURLsPath;
    protected String failedURLsPath;
    //folders:
    protected static String entityJsonPath;
    protected static String entityPropertiesPath;
    protected String htmlPath;

    protected Deque<Pair> deque = new ArrayDeque<>();
    protected HashSet<String> failedURLsHashSet;
    protected HashSet<String> analysedURLsHashSet;
    protected HashMap<String, Integer> craftedURLsHashMap = new HashMap<>();
    protected int totalAnalysed;
    protected int limitAmountAnalysis = 15000;

    /**
     * Set up an environment for saving data.
     * @param path Your path where the crawled data will be stored.
     */
    public EntityHandling(String path)
    {
        superpath = path;
        craftedURLsPath = superpath + "CraftedURLs.txt";
        analysedURLsPath = superpath + "AnalysedURLs.txt";
        failedURLsPath = superpath + "FailedURLs.txt";
        beginURLsPath = superpath + "BeginURLs.txt";
        htmlPath = superpath + "WebHtml";
        entityJsonPath = superpath + "EntityJson";
        entityPropertiesPath = superpath + "EntityProperties";
        createFolder(entityJsonPath);
        createFolder(entityPropertiesPath);
    }

    public EntityHandling()
    {
        throw new IllegalArgumentException("File path must be provided");
    }

    /**
     * This method is used to scrape data.
     * @throws Exception
     */
    public final void getData() throws Exception
    {
        getVietnamRelatedEntity();
        failedURLsHashSet = new HashSet<>(readFileAllLine(failedURLsPath));
        analysedURLsHashSet = new HashSet<>(readFileAllLine(analysedURLsPath));
        totalAnalysed += failedURLsHashSet.size() + analysedURLsHashSet.size();
        if (totalAnalysed > limitAmountAnalysis)
        {
            return;
        }
        List<String> craftedURLsList = readFileAllLine(craftedURLsPath);
        if (craftedURLsList.size()==0)
        {
            String beginURLs = readFileAll(beginURLsPath);
            writeFile(craftedURLsPath, beginURLs + 
            "\n0\n", false);
            deque.addLast(new Pair(beginURLs, 0));
            craftedURLsHashMap.put(beginURLs, 0);
        }
        else
        {
            for (int i = 0; i < craftedURLsList.size(); i+=2)
            {
                String urlString = craftedURLsList.get(i);
                urlString = filterURL(urlString);
                int depth = Integer.parseInt(craftedURLsList.get(i+1));
                if (checkURL(urlString) == false) continue;
                if (existInAnalysedURL(urlString)) continue;
                craftedURLsHashMap.put(urlString, depth);
                deque.addLast(new Pair(urlString, depth));
            }
        }

        while(deque.size()!=0)
        {
            int depth = deque.getFirst().second;
            String urlString = deque.getFirst().first;
            if ( depth <= 3 && totalAnalysed <= limitAmountAnalysis)
            {
                entityAnalys(urlString, depth);
                totalAnalysed++;
            }
            deque.removeFirst();
        }
    }

    /**
     * Set up the limitation of the number of entities to analyze.
     * @param newLimit
     */
    public void setAnalyseLitmit(int newLimit)
    {
        limitAmountAnalysis = newLimit;
    }

    /**
     * This method is a filter for URLs to remove unnecessary things. 
     * @param urlString
     * @return String after applying the filter.
     * @throws Exception
     * @apiNote Default method does not change the URL. Should be overwritten in a subclass.
     */
    public String filterURL(String urlString) throws Exception
    {
        return urlString;
    }

    /**
     Add URL and its depth to crafed URL list.
     */
    protected final void addURLToCrafed(String urlString, int depth) throws Exception
    {
        if (craftedURLsHashMap.containsKey(urlString) == false) {
            if (depth < 3)
            {
                deque.add(new Pair(urlString, depth + 1));
                String content = urlString + '\n' + String.valueOf(depth+1)+ '\n';
                writeFile(craftedURLsPath, content, true);
                craftedURLsHashMap.put(urlString, depth + 1);
            }
        }
        return;
    }

    /**
     * Check if {@code urlString} has been processed before.
     * @return If {@code urlString} has been processed, return {@code true}; otherwise, return {@code false}.
     */
    protected final boolean existInAnalysedURL(String urlString)
    {
        if (failedURLsHashSet.contains(urlString)) return true;
        if (analysedURLsHashSet.contains(urlString)) return true;
        return false;
    }

    /**
     * Check if the URL is valid.
     * @return {@code true} if the URL is valid; otherwise, return {@code false}.
     */
    public boolean checkURL(String urlString) throws Exception {
        throw new UnsupportedOperationException("Unimplemented method 'checkURL' from EntityHandling. Must be overriden in subclasss.");
    }

    /**
     * This method helps to gather information about entities that have a connection to Vietnam.
     */
    protected void getVietnamRelatedEntity() throws Exception {
        throw new UnsupportedOperationException("Unimplemented method 'getVietnamRelatedEntity' from EntityHandling. Must be overriden in subclasss.");
    }

    /**
     * Analize an entity to make sure it is related to Vietnam and write it into logs.
     */
    protected boolean checkRelated(String data) throws Exception {
        throw new UnsupportedOperationException("Unimplemented method 'checkRelated' from EntityHandling. Must be overriden in subclasss.");
    }

    protected void entityAnalys(String url, int depth) throws Exception {
        throw new UnsupportedOperationException("Unimplemented method 'entityAnalys' from EntityHandling. Must be overriden in subclasss.");
    }

    protected void getProperties() throws Exception{
        throw new UnsupportedOperationException("Unimplemented method 'entityAnalys' from EntityHandling. Must be overriden in subclasss.");
    }
}
