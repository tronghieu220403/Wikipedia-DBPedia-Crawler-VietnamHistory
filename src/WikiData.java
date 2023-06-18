/**
 *  The "WikiData" class provides useful methods for analyzing Wikipedia pages and extracting relevant information related to entities in Vietnam.
 */
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import org.json.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

class Pair
{
    public String first;
    public int second;
    public Pair(String a, int b)
    {
        first = a;
        second = b;
    }
}

public class WikiData extends EntityHandling{
    
    public static void main(String[] args) throws Exception {
        WikiData wikiData = new WikiData("E:/Code/Java/OOP_Project/saveddata/Wikipedia/");
        wikiData.getData();
        wikiData.getProperties();
    }

    private final String ENTITY_PROPERTIES_PATH = LOGS_PATH + "/EntityProperties/";
    private final String HTML_PATH = LOGS_PATH + "/WebHtml/";
    protected final String ENTITY_REFERENCE_PATH = LOGS_PATH + "EntityReference/";;

    public WikiData()
    {
        throw new IllegalArgumentException("File path must be provided.");
    }

    public WikiData(String folderPath) throws FileNotFoundException
    {
        super(folderPath);
        if (!fileExist(folderPath))
        {
            throw new FileNotFoundException("Folder doesn't exist: " + folderPath);
        }
        createFolder(ENTITY_PROPERTIES_PATH);
        createFolder(ENTITY_REFERENCE_PATH);

    }


    @Override
    public void getVietnamRelatedEntity() throws Exception{

        JSONArray myJsonArray = new JSONArray(readFileAll(INITIALIZE_PATH + "/FromVietnam.json"));
        vietnamEntityHashSet.clear();

        for (int i = 0; i < myJsonArray.length(); i++) {
            JSONObject tmpJsonObject = myJsonArray.getJSONObject(i);
            String tmpEntity = tmpJsonObject.getString("item");
            tmpEntity = tmpEntity.replace("http://www.wikidata.org/entity/", "");
            vietnamEntityHashSet.add(tmpEntity);
        }

        writeFile(INITIALIZE_PATH + "/VietnamRelated.json", new JSONArray(vietnamEntityHashSet).toString(), false);

        return;
    }

    /**
     * Analize an entity to make sure it is related to Vietnam and write it to logs.<p>
     * Get the HTML content of this URL, also get the entity data and related URLs for this.
     * @apiNote The HTML content is in "WebHtml" folder. 
     * @apiNote The entity data is in "EntityJson" folder. 
     * @apiNote The related URLs is in "EntityReference" folder. 
     */
    @Override
    protected void entityAnalys(String urlString, int depth) throws Exception {
        
        // Check if urlString is a valid Wikipedia URL .
        urlString = urlString.replace("\n", "");
        if (checkURL(urlString) == false)
        {
            return;
        }
        String wikiPageData = "";
        try {
            wikiPageData = getDataFromURL(urlString).toString();
        } catch (Exception e) {
            return;
        }

        // Parse the HTML using Jsoup
        
        // Write the entity data to "EntityJson" folder if there's exist an qID
        if (existInAnalysedURL(urlString))
        {
            return;
        }

        if (checkRelated(wikiPageData) == false)
        {
            addToFailedURL(urlString);
            return;
        }
        else{
            addToAnalysedURL(urlString);
        }
                
        /*
         * Get related URL for this entity.
         * The related URLs is in "EntityReference" folder. 
         */
        Document doc = Jsoup.parse(wikiPageData);
        String qID = getEntityID(doc);

        Element divTag = doc.getElementById("mw-content-text"); 
        for (Element aTag : divTag.select("a")) {
            String href = aTag.attr("href");
            String refURL = "https://vi.wikipedia.org" + href; 
            if (!checkURL(refURL)) continue;         
            
            writeFile(ENTITY_REFERENCE_PATH + qID + ".txt", refURL + '\n', true);
            addToCrafedURL(refURL, depth);
        }
        return;
    }

    private final String[] FILTER = {
        "#","T%E1%BA%ADp_tin"
    };
    

    @Override
    public boolean checkURL(String urlString) {
        if (!urlString.contains("http")) return false;  

        if (urlString == null || urlString.isEmpty()) return false;  
        if (!urlString.contains("/wiki/")) return false;
        
        for (String text : FILTER) {
            if (urlString.contains(text)) return false;
        }
        
        if (urlString.chars().filter(ch -> ch == ':').count() > 1) {
            return false;  
        }
        
        return true;  
    }

    /**
     * Check if the JSON of an entity has any properties that are related to Vietnam.
     * @param entityJSON the JSON of an entity.
     * @return Member variable isRelated is {@code true} if that entity has any properties that are related to Vietnam, else {@code false}.
     */
    private boolean JSONAnalysis(Object entityJSON)
    {
        if (entityJSON instanceof JSONArray)
        {
            for (int i = 0; i < ((JSONArray) entityJSON).length(); i++) { 
                if (JSONAnalysis(((JSONArray) entityJSON).get(i)) == true)
                {
                    return true;
                }
            }
        }
        else if (entityJSON instanceof JSONObject)
        {
            if (((JSONObject) entityJSON).has("numeric-id"))
            {
                if (vietnamEntityHashSet.contains(((JSONObject) entityJSON).get("id")))
                {
                    return true;
                }
            }
            for(String key: getAllKeys((JSONObject) entityJSON)){
                Object value = ((JSONObject) entityJSON).get(key);
                
                if (value instanceof JSONObject) {
                    if (JSONAnalysis((JSONObject) value) == true){
                        return true;
                    }
                } else if (value instanceof JSONArray) {
                    if (JSONAnalysis((JSONArray) value) == true){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Get the ID of an entity from a Wikipedia page.
     * @param soupHWND HTML content parsed by Jsoup library.
     * @return That entity's ID of that soupHWND
     */
    public String getEntityID(Document soupHWND) 
    {
        String entityURL = "";
        Element liTag = soupHWND.getElementById("t-wikibase");
        if (liTag == null)
            return "";
        for (Element aTag : liTag.select("a")) {
            entityURL = aTag.attr("href");
            break;
        }
        if (entityURL.equals(""))
            return "";
        String qID = entityURL.replace("https://www.wikidata.org/wiki/Special:EntityPage/","");
        return qID;
    }


    private final String VIETNAM_WORD[] = {"Vi\\u1ec7t Nam", "Vietnam","Việt Nam", "Viet Nam", "việt nam", };

    /**
     * Check if the entity is related to Vietnam.
     * @param qID
     * @return Return {@code true} if it is related; otherwise return {@code false}.
     */

    @Override
    public boolean checkRelated(String wikiPageData) throws Exception {
        Document doc = Jsoup.parse(wikiPageData);
        String qID = getEntityID(doc);
        if (!qID.isEmpty())
        {
            // Write the HTML content to "WebHtml" folder. 
            writeFile(HTML_PATH + qID + ".html", wikiPageData, false);
        }
        else {
            return false;
        }

        String entityURL = "https://www.wikidata.org/wiki/Special:EntityData/" + qID + ".json";
        String content = getDataFromURL(entityURL).toString();
        JSONObject json;
        if (!content.isEmpty())
            return false;
        json = new JSONObject(content);
        
    
        if (JSONAnalysis(json) == false)
            return false;
        if (getViLabel(json, qID).isEmpty())
            return false;
        
        boolean check = false;
        for (String vnWord: VIETNAM_WORD)
        {
            if (content.contains(vnWord)){
                check = true;
                break;
            }
        }
        if (check == false) return false;

        writeFile(ENTITY_JSON_PATH + qID +".json", content , false);
        return true;
    }

    /**
     * Get the label of entity
     */
    private String getViLabel(String qID) throws Exception
    {
        if (viLabelHashMap.containsKey(qID))
        {
            return viLabelHashMap.get(qID);
        }
        return getViLabel(qID, ENTITY_JSON_PATH, ENTITY_PROPERTIES_PATH);
    }

    /**
     * Get the label of entity
     */
    public static String getViLabel(String qID, String jsonPath1, String jsonPath2) throws Exception
    {
        if (viLabelHashMap.containsKey(qID))
        {
            return viLabelHashMap.get(qID);
        }
        String viLabelValue = "";
        JSONObject jsonContent;
        if (fileExist(jsonPath1 + "/" + qID + ".json"))
            jsonContent = getJSONFromFile(jsonPath1 + "/" + qID + ".json");
        else if (fileExist(jsonPath2 + "/" + qID + ".json"))
            jsonContent = getJSONFromFile(jsonPath2 + "/" + qID + ".json");
        else return viLabelValue;
        return getViLabel(jsonContent, qID);
    }

    /**
     * Get the label of entity
     */
    private static HashMap<String, String> viLabelHashMap = new HashMap<>();
    public static String getViLabel(JSONObject jsonContent, String qID) throws Exception
    {
        String viLabelValue = "";
        if (!jsonContent.has("entities")) return viLabelValue;
        JSONObject entities = jsonContent.getJSONObject("entities");
        
        if (!entities.has(qID)) return viLabelValue;
        JSONObject entity = entities.getJSONObject(qID);
       
        if (!entity.has("labels")) return viLabelValue;
        JSONObject labels = entity.getJSONObject("labels");

        if (labels.has("vi"))
        {
            JSONObject viLabel = labels.getJSONObject("vi");
            if (viLabel.has("value"))
            {
                viLabelValue = viLabel.getString("value");
                viLabelHashMap.put(qID, viLabelValue);
            }
        }
        else if (entity.has("sitelinks"))
        {
            JSONObject sitelinks =  entity.getJSONObject("sitelinks");;
            if (sitelinks.has("viwiki"))
            {
                JSONObject viwiki = sitelinks.getJSONObject("viwiki");
                if (viwiki.has("title")){
                    viLabelValue = viwiki.getString("title");
                    viLabelHashMap.put(qID, viLabelValue);
                }
            }
        }
        return viLabelValue;
    }

    /**
     * Analyze a JSON Object and add all properties into propertyHashSet
     * @param entityJSON A Wikidata JSON Object.
     * @param entityJSONFileList A list of files in "EntityJSON" folder.
     */
    private void jsonPropertiesAnalysis(Object entityJSON, HashSet<String> entityJSONFileList)
    {
        if (entityJSON instanceof JSONArray)
        {
            for (int i = 0; i < ((JSONArray) entityJSON).length(); i++) { 
                jsonPropertiesAnalysis(((JSONArray) entityJSON).get(i), entityJSONFileList);
            }
        }
        else if (entityJSON instanceof JSONObject)
        {
            if (((JSONObject) entityJSON).has("datavalue") &&
                ((JSONObject) entityJSON).has("property") &&
                ((JSONObject) entityJSON).has("datatype"))
            {
                propertyHashSet.add((String)((JSONObject) entityJSON).get("property"));
                String datatype = (String)((JSONObject) entityJSON).get("datatype");
                if (!datatype.equals("wikibase-item") && !datatype.equals("wikibase-property"))
                {
                    return;
                }
                JSONObject datavalue = (JSONObject)((JSONObject) entityJSON).get("datavalue");
                JSONObject value = (JSONObject)((JSONObject) datavalue).get("value");
                String id = (String)((JSONObject) value).get("id");
                if (!entityJSONFileList.contains(id + ".json"))
                {
                    propertyHashSet.add(id);
                }
                return;
            }
            Iterator<String> keys = ((JSONObject) entityJSON).keys();
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = ((JSONObject) entityJSON).get(key);
                
                if (value instanceof JSONObject) {
                    jsonPropertiesAnalysis((JSONObject) value, entityJSONFileList);
                } else if (value instanceof JSONArray) {
                    jsonPropertiesAnalysis((JSONArray) value, entityJSONFileList);
                }
            }
        }
    }

    protected void getPropertiesInJson(String root, String fileName, HashSet<String> entityJSONFileList) throws Exception
    {
        String content = readFileAll(ENTITY_JSON_PATH + fileName);
        int last = 0;
        while(true)
        {
            int start = content.indexOf("http://www.wikidata.org/entity/", last);
            if (start == -1) break;
            int end = content.indexOf("\"", start);
            String qID = (content.substring(start, end)).replace("http://www.wikidata.org/entity/", "");
            propertyHashSet.add(qID);
            last = end;
        }
        JSONObject entityJSON = new JSONObject(readFileAll(ENTITY_JSON_PATH + fileName));
        JSONObject claims = entityJSON.getJSONObject("entities").getJSONObject(fileName.replace(".json","")).getJSONObject("claims");
        jsonPropertiesAnalysis(claims, entityJSONFileList);
    }

    /**
     * Get all properties of all entities and save it to folder "Properties".
     * @throws Exception
     */
    @Override
    protected void getProperties() throws Exception
    {
        if (fileExist(LOGS_PATH + "PropertiesList.json"))
        {
            JSONArray myJsonArray = new JSONArray(readFileAll(LOGS_PATH + "PropertiesList.json"));
            for (int i = 0; i < myJsonArray.length(); i++) { 
                propertyHashSet.add(myJsonArray.getString(i));
            }
        }
        else
        {
            HashSet<String> entityFileList = listAllFiles(ENTITY_JSON_PATH);
            for (String fileName: entityFileList)
            {
                if (fileExist(ENTITY_JSON_PATH + fileName))
                {
                    getPropertiesInJson(ENTITY_JSON_PATH, fileName, entityFileList);
                }
            }
            writeFile(LOGS_PATH + "PropertiesList.json", (new JSONArray(propertyHashSet)).toString(), false);
        }
        HashSet<String> propertyFileList = listAllFiles(ENTITY_PROPERTIES_PATH);
        for (String pID: propertyHashSet)
        {
            if (!propertyFileList.contains(pID + ".json")){
                writeFile(ENTITY_PROPERTIES_PATH + pID + ".json", getDataFromURL("https://www.wikidata.org/wiki/Special:EntityData/" + pID + ".json").toString(),false);
            }
        }

        HashSet<String> allPFile = listAllFiles(ENTITY_PROPERTIES_PATH);
        for (String fileName: allPFile)
        {
            if (getViLabel(fileName.replace(".json", "")).isEmpty())
            {
                deleteFile(ENTITY_PROPERTIES_PATH + fileName);
            }
        }
    }
}