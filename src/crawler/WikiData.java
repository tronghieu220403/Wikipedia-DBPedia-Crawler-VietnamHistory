package crawler;

/**
 *  The "WikiData" class provides useful methods for analyzing Wikipedia pages and extracting relevant information related to entities in Vietnam.
 */
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.json.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class WikiData extends EntityHandling{
    
    public static void main(String[] args) throws Exception {
        WikiData wikiData = new WikiData("E:/Code/Java/OOP_Project/saveddata/Wikipedia/");
        wikiData.setBruteForceAnalyseLitmit(0);
        wikiData.getBruteForceData();
        wikiData.getDataCallBack();
    }

    protected final HashMap<String, String> urlToEntityHashMap = new HashMap<>();

    protected final String ENTITY_PROPERTIES_PATH = LOGS_PATH + "EntityProperties/";
    protected final String HTML_PATH = LOGS_PATH + "WebHtml/";
    protected final String SCARLARLY_PATH = LOGS_PATH + "Scarlarly/";

    protected final String ENTITY_REFERENCE_PATH = LOGS_PATH + "EntityReference/";;
    protected final String EVENT_PATH = DATA_PATH + "sự kiện lịch sử";
    protected final String PLACE_PATH = DATA_PATH + "địa điểm du lịch, di tích lịch sử/";
    protected final String HUMAN_PATH = DATA_PATH + "nhân vật lịch sử/";
    protected final String DYNASTY_PATH = DATA_PATH + "triều đại lịch sử/";
    protected final String FESTIVAL_PATH = DATA_PATH + "lễ hội văn hóa/";

    protected final String ENTITY_FINAL_PATH = LOGS_PATH + "EntityFinal/";
    protected final String ENTITY_REF_FINAL_PATH = LOGS_PATH + "EntityRefFinal/";

    private HashSet<String> allQFile = listAllFiles(ENTITY_JSON_PATH);
    private HashSet<String> allPFile = listAllFiles(ENTITY_PROPERTIES_PATH);

    public WikiData(){
        throw new IllegalArgumentException("File path must be provided.");
    }

    public WikiData(String folderPath) throws Exception{
        super(folderPath);
        if (!folderExist(folderPath)) {
            throw new FileNotFoundException("Folder doesn't exist: " + folderPath);
        }
        createFolder(ENTITY_PROPERTIES_PATH);
        createFolder(ENTITY_REFERENCE_PATH);
        createFolder(SCARLARLY_PATH);

        createFolder(EVENT_PATH);
        createFolder(HUMAN_PATH);
        createFolder(PLACE_PATH);
        createFolder(DYNASTY_PATH);
        createFolder(FESTIVAL_PATH);

        createFolder(ENTITY_FINAL_PATH);
        createFolder(ENTITY_REF_FINAL_PATH);

        if(fileExist(LOGS_PATH + "URLToEntities.json"))
        {
            JSONObject jsonContent = getJSONFromFile(LOGS_PATH + "URLToEntities.json");
            for (String key: getAllKeys(jsonContent)){
                urlToEntityHashMap.put(key,(jsonContent).getString(key));
            }
        }
        return;
    }

    @Override
    public void getDataCallBack() throws Exception
    {
        analyzeBruteForceData();
        return;
    }

    private void analyzeBruteForceData() throws Exception{
        urlToEntities();
        getWikiProperties();
        entityRefFinal();
        entityFinal();
    }

    public final void urlToEntities() throws Exception
    {
        if (fileExist(LOGS_PATH + "URLToEntities.json"))
        {
            JSONObject jsonContent = getJSONFromFile(LOGS_PATH + "URLToEntities.json");
            for (String key: getAllKeys(jsonContent)){
                urlToEntityHashMap.put(key,jsonContent.getString(key));
            }
            return;
        }
        for (String fileName: listAllFiles(ENTITY_JSON_PATH))
        {
            String qID = fileName.replace(".json", "");
            JSONObject entity = getJSONFromFile(ENTITY_JSON_PATH + fileName).getJSONObject("entities").getJSONObject(qID);
            JSONObject sitelinks = entity.getJSONObject(qID).getJSONObject("sitelinks");
            if (sitelinks.has("viwiki")){
                urlToEntityHashMap.put(urlDecode(sitelinks.getJSONObject("viwiki").getString("url")), qID);
            }
            if (sitelinks.has("enwiki")){
                urlToEntityHashMap.put(sitelinks.getJSONObject("enwiki").getString("url"), qID);
            }
        }
        writeFile(LOGS_PATH +  "URLToEntities.json" , (new JSONObject(urlToEntityHashMap)).toString(), false);
    }

    private void entityRefFinal() throws Exception
    {
        HashSet<String> allQRefFile = listAllFiles(ENTITY_REFERENCE_PATH);
        HashMap<String, HashSet<String> > refList = new HashMap<String, HashSet<String>>();
        for (String fileName: allQRefFile)
        {
            String qID = fileName.replace(".txt", "");
            List<String> qRef = readFileAllLine(ENTITY_REFERENCE_PATH + fileName);
            for (String urlString: qRef)
            {
                urlString = urlDecode(urlString);
                if (urlString.isEmpty()) continue;
                if (urlToEntityHashMap.containsKey(urlString))
                {
                    String qID1 = urlToEntityHashMap.get(urlString);
                    if (!refList.containsKey(qID))
                    {
                        HashSet<String> h = new HashSet<>();
                        h.add(qID1);
                        refList.put(qID, h);
                    }
                    else
                    {
                        refList.get(qID).add(qID1);
                    }
                    if (!refList.containsKey(qID1))
                    {
                        HashSet<String> h = new HashSet<>();
                        h.add(qID);
                        refList.put(qID1, h);
                    }
                    else
                    {
                        refList.get(qID1).add(qID);
                    }
                }
            }
        }
        refList.forEach((key, value) -> {
            try {
                writeFile(ENTITY_REF_FINAL_PATH + key + ".json", (new JSONArray(value)).toString() , false);
            } catch (Exception e) {
                print("Can not write to file: " + ENTITY_REF_FINAL_PATH + key + ".json", "content: ", (new JSONArray(value)).toString());
            }
        });
    }

    public final JSONObject getVietnameseWikiReadable(String qID) throws Exception{
        return WikiDataHandling.getVietnameseWikiReadable(qID, allQFile, allPFile, ENTITY_JSON_PATH, ENTITY_PROPERTIES_PATH, ENTITY_REF_FINAL_PATH, HTML_PATH);
    }

    public final void entityFinal() throws Exception
    {
        //HashSet<String> allPFile = listAllFiles(ENTITY_PROPERTIES_PATH);
        allQFile = listAllFiles(ENTITY_JSON_PATH);
        allPFile = listAllFiles(ENTITY_PROPERTIES_PATH);

        for (String fileName: allQFile)
        {
            if (fileExist(ENTITY_FINAL_PATH + fileName)) {
                //continue;
            }
            String qID = fileName.replace(".json", "");
            JSONObject json = getVietnameseWikiReadable(qID);
            String writePath = ENTITY_FINAL_PATH + fileName;
            String writeContent = json.toString();
            writeFile(writePath, writeContent, false);
        }
    }

    public final String getRawEntityFirstInstance(String qID) throws Exception
    {
        return WikiDataHandling.getRawEntityFirstInstance(qID, ENTITY_JSON_PATH, ENTITY_PROPERTIES_PATH);
    }

    public String getOverview(String qID) 
    {
        return WikiDataHandling.getOverview(qID, HTML_PATH);
    }


    /**
     * Analize an entity to make sure it is related to Vietnam and write it to logs.<p>
     * Get the HTML content of this URL, also get the entity data and related URLs for this.
     * @apiNote The HTML content is in "logs/WebHtml" folder. 
     * @apiNote The entity data is in "logs/EntityJson" folder. 
     * @apiNote The related URLs is in "logs/EntityReference" folder. 
     */
    @Override
    protected void entityAnalys(String urlString, int depth, boolean forceRelated) throws Exception {
        // Check if urlString is a valid Wikipedia URL .
        urlString = urlDecode(urlString.replace("\n", ""));
        if (checkURL(urlString) == false){
            return;
        }
        if (forceRelated == false && existInAnalysedURL(urlString)){
            return;
        }

        if (!urlToEntityHashMap.containsKey(urlString))
        {
            // Get page data from Wiki API
            String wikiPageData = getDataFromURL(urlString).toString();
            String qID = "";
            if (wikiPageData.isEmpty()) return;
            Document doc = Jsoup.parse(wikiPageData);
            qID = WikiDataHandling.getEntityIdFromHtml(doc);
            if (!qID.isEmpty()){
                urlToEntityHashMap.put(urlString, qID);
            }
            // Write the entity data to "EntityJson" and "WebHtml" folder if there's exist a qID
            if (!checkRelated(qID, wikiPageData, forceRelated)){
                if (forceRelated == false)
                    addToFailedURL(urlString);
                return;
            }

            /*
            * Get related URL for this entity.
            * The related URLs is in "EntityReference" folder. 
            */
            // Parse the HTML using Jsoup
            StringBuffer s = new StringBuffer("");
            for (String craftURL: WikiDataHandling.getAllWikiHref(wikiPageData)){
                s.append(craftURL + '\n');
                addToCrafedURL(craftURL, depth);
            }
            writeFile(ENTITY_REFERENCE_PATH + qID + ".txt", s.toString(), true);
            addToAnalysedURL(urlString);
        }
        return;
    }

    @Override
    public boolean checkURL(String urlString) throws Exception {        
        return WikiDataHandling.checkURL(urlString, false);  
    }

    private boolean jsonAnalysis(Object json){
        return WikiDataHandling.jsonAnalysis(json, vietnamEntityHashSet);
    }

    /**
     * Check if the entity is related to Vietnam.
     * @param qID
     * @return Return {@code true} if it is related; otherwise return {@code false}.
     */
    public boolean checkRelated(String qID, String wikiPageData, boolean forceRelated) throws Exception {
        if (!qID.isEmpty()){
            writeFile(HTML_PATH + qID + ".html", wikiPageData, false);
        }
        else {
            return false;
        }

        String entityURL = "https://www.wikidata.org/wiki/Special:EntityData/" + qID + ".json";
        String content = getDataFromURL(entityURL).toString();
        if (forceRelated == false){
            if (content.isEmpty())
                return false;
            JSONObject json = new JSONObject(content);
        
            if (jsonAnalysis(json) == false)
                return false;
            if (WikiDataHandling.getWikiEntityViLabel(json, qID).isEmpty())
                return false;
            
            boolean check = false;
            for (String vnWord: WikiDataHandling.VIETNAM_WORD) {
                if (content.contains(vnWord)){
                    check = true;
                    break;
                }
            }
            if (check == false) return false;

            JSONObject entitiyJson = json.getJSONObject("entities").getJSONObject(qID);
            // If an entity has no sitelinks to Wikipedia then that entity is virtual. We will put it into the ENTITY_PROPERTIES_PATH
            if (WikiDataHandling.getWikiSitelink(entitiyJson, qID, "viwiki").isEmpty()) {
                writeFile(ENTITY_PROPERTIES_PATH + qID +".json", content , false);
                return false;
            }
            
            // If an entity is a human (Q5) and there exist at least one year, it must be less than 1962.
            if (WikiDataHandling.getWikiEntityInstance(entitiyJson).equals("Q5")){
                int entityMinYear = WikiDataHandling.getMinYear(entitiyJson);
                if (entityMinYear > 1962 && entityMinYear != 100000) {
                    writeFile(ENTITY_PROPERTIES_PATH + qID +".json", content , false);
                }
            }
        }
        writeFile(ENTITY_JSON_PATH + qID +".json", content , false);
        return true;
    }

    /**
     * Analyze a JSON Object and add all properties into propertyHashSet
     * @param entityJSON A Wikidata JSON Object.
     * @param entityJSONFileList A list of files in "EntityJSON" folder.
     */
    private void jsonGetPropertiesFromEntity(Object entityJSON, HashSet<String> entityJSONFileList)
    {
        WikiDataHandling.jsonGetPropertiesFromEntity(entityJSON, entityJSONFileList, propertyHashSet);
    }

    protected void getPropertiesInJson(String root, String fileName, HashSet<String> entityJSONFileList) throws Exception
    {
        String content = readFileAll(ENTITY_JSON_PATH + fileName);
        int last = 0;
        while(true) {
            int start = content.indexOf("http://www.wikidata.org/entity/", last);
            if (start == -1) break;
            int end = content.indexOf("\"", start);
            String qID = (content.substring(start, end)).replace("http://www.wikidata.org/entity/", "");
            propertyHashSet.add(qID);
            last = end;
        }
        JSONObject entityJSON = new JSONObject(readFileAll(ENTITY_JSON_PATH + fileName));
        JSONObject claims = entityJSON.getJSONObject("entities").getJSONObject(fileName.replace(".json","")).getJSONObject("claims");
        jsonGetPropertiesFromEntity(claims, entityJSONFileList);
    }

    /**
     * Get all properties of all entities and save it to folder "Properties".
     * @throws Exception
     */
    protected void getWikiProperties() throws Exception
    {
        if (fileExist(LOGS_PATH + "PropertiesList.json")) {
            JSONArray myJsonArray = new JSONArray(readFileAll(LOGS_PATH + "PropertiesList.json"));
            for (int i = 0; i < myJsonArray.length(); i++) {
                String pID = myJsonArray.getString(i);
                propertyHashSet.add(pID);
            }
        }
        else{
            HashSet<String> entityFileList = listAllFiles(ENTITY_JSON_PATH);
            for (String fileName: entityFileList){
                if (fileExist(ENTITY_JSON_PATH + fileName)) {
                    getPropertiesInJson(ENTITY_JSON_PATH, fileName, entityFileList);
                }
            }
            HashSet<String> propertyFileList = listAllFiles(ENTITY_PROPERTIES_PATH);
            List<String> removePID = new ArrayList<>();
            for (String pID: propertyHashSet) {
                if (!propertyFileList.contains(pID + ".json")) {
                    String data = getDataFromURL("https://www.wikidata.org/wiki/Special:EntityData/" + pID + ".json").toString();
                    if (!WikiDataHandling.getWikiEntityViLabel(new JSONObject(data), pID).isEmpty()) {
                        writeFile(ENTITY_PROPERTIES_PATH + pID + ".json", data, false);
                    }
                    else{
                        removePID.add(pID);
                    }
                }
            }
            for (String pID: removePID){
                propertyHashSet.remove(pID);
            }
            writeFile(LOGS_PATH + "PropertiesList.json", (new JSONArray(propertyHashSet)).toString(), false);
        }
    }
}
