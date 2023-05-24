/**
 *  The "WikiData" class provides useful methods for analyzing Wikipedia pages and extracting relevant information related to entities in Vietnam.
 */
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
    
    public WikiData()
    {
        super("E:/Code/Java/OOP_Project/saveddata/Wikipedia/");
        createFolder(superpath + "EntityReference");
        createFolder(superpath + "WebHTML");
    }

    public static void main(String[] args) throws Exception {
        WikiData wikiData = new WikiData();
        wikiData.getData();
        wikiData.getAdditionalData();
        wikiData.getProperties();
    }

    @Override
    public void getVietnamRelatedEntity() throws Exception{

        JSONArray myJsonArray = new JSONArray(readFileAll(superpath + "VietnamRelated/FromVietnam.json"));
        vietnamEntityHashSet.clear();
        for (int i = 0; i < myJsonArray.length(); i++) {
            JSONObject tmpJsonObject = myJsonArray.getJSONObject(i);
            String tmpEntity = (String)tmpJsonObject.get("item");
            tmpEntity = tmpEntity.replace("http://www.wikidata.org/entity/", "");
            vietnamEntityHashSet.add(tmpEntity);
        }

        writeFile(superpath + "VietnamRelated/VietnamRelated.json", new JSONArray(vietnamEntityHashSet).toString(), false);

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
        
        urlString = urlString.replace("\n", "");
        if (checkURL(urlString) == false)
        {
            return;
        }
        String data = "";
        try {
            data = getDataFromURL(urlString).toString();
        } catch (Exception e) {
            //System.out.println("Error in " + url);    
            return;
        }

        // Parse the HTML using Jsoup
        Document doc = Jsoup.parse(data);
        String entityID = getEntityID(doc);
        if (!entityID.equals(""))
        {
            if (fileExist(superpath + "WebHtml/" + entityID + ".html"))
            {
                if (fileExist(superpath + "EntityJson/" + entityID + ".json"))
                {
                    writeFile(analysedURLsPath, urlString + '\n', true);
                    return;
                }
                else {
                    writeFile(failedURLsPath, urlString + '\n', true);
                    return;
                }
            }
            // Write the HTML content to "WebHtml" folder. 
            writeFile(superpath + "WebHtml/" + entityID + ".html", data, false);
        }
        else {
            writeFile(failedURLsPath, urlString + '\n', true);
            return;
        }
        
        // Write the entity data to "EntityJson" folder if there's exist an entityID
        if (!checkRelated(entityID))
        {
            writeFile(failedURLsPath, urlString + '\n', true);
            return;
        }
        
        writeFile(analysedURLsPath, urlString + '\n', true);
        
        /*
         * Get related URL for this entity.
         * The related URLs is in "EntityReference" folder. 
         */
        Element divTag = doc.getElementById("mw-content-text"); 
        for (Element aTag : divTag.select("a")) {
            String href = aTag.attr("href");
            String refURL = "https://vi.wikipedia.org" + href; 
            if (!checkURL(refURL)) continue;         
            
            writeFile(superpath + "EntityReference/" + entityID + ".txt", refURL + '\n', true);
            addURLToCrafed(refURL, depth);
        }
        return;
    }

    private String[] filter = {
        "#","T%E1%BA%ADp_tin", 
        "File:", "Wikipedia:",
        "Th%E1%BB%83_lo%E1%BA%A1i:"
    };
    

    @Override
    public boolean checkURL(String urlString) {
        if (urlString == null || urlString.isEmpty()) return false;  
        if (!urlString.contains("/wiki/")) return false;
        
        for (String text : filter) {
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
    private void JSONAnalysis(Object entityJSON)
    {
        if (isRelated == true)
            return;
        if (entityJSON instanceof JSONArray)
        {
            for (int i = 0; i < ((JSONArray) entityJSON).length(); i++) { 
                JSONAnalysis(((JSONArray) entityJSON).get(i));
            }
        }
        else if (entityJSON instanceof JSONObject)
        {
            if (((JSONObject) entityJSON).has("numeric-id"))
            {
                if (vietnamEntityHashSet.contains(((JSONObject) entityJSON).get("id")))
                {
                    isRelated = true;
                    return;
                }
            }
            Iterator<String> keys = ((JSONObject) entityJSON).keys();
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = ((JSONObject) entityJSON).get(key);
                
                if (value instanceof JSONObject) {
                    JSONAnalysis((JSONObject) value);
                } else if (value instanceof JSONArray) {
                    JSONAnalysis((JSONArray) value);
                }
            }
        }
    }

    /**
     * Get the ID of an entity.
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
        String entityID = entityURL.replace("https://www.wikidata.org/wiki/Special:EntityPage/","");
        return entityID;
    }

    /**
     * Check if the entity is related to Vietnam.
     * @param entityID
     * @return Return {@code true} if it is related; otherwise return {@code false}.
     */
    @Override
    public boolean checkRelated(String entityID) throws Exception {
        if (entityID.isEmpty())
        {
            return false;
        }
        if (fileExist(superpath + "EntityJson/" + entityID +".json") == true)
        {
            return true;
        }

        String entityURL = "https://www.wikidata.org/wiki/Special:EntityData/" + entityID + ".json";
        JSONObject jsonData = getJSONFromURL(entityURL);
        
        isRelated = false;
        JSONAnalysis(jsonData);
        if (isRelated == false)
            return false;
        String content = (jsonData).toString();
        writeFile(superpath + "EntityJson/" + entityID +".json", content , false);
        return true;
    }

    protected String entityAdditionalJsonPath = superpath + "EntityJsonAdditional";
    /**
     * Get all entities contain "Vietnam" word.
     * @throws Exception
    */
    protected void getAdditionalData() throws Exception
    {
        createFolder(entityAdditionalJsonPath);
        HashSet<String> webHTML = listAllFiles(htmlPath);
        HashSet<String> entityFileList = listAllFiles(entityJsonPath);
        String vietnamWord[] = {"Vietnam","Việt Nam", "Viet Nam", "việt nam"};
        for (String fileName: webHTML)
        {
            String name = fileName.replace(".html", ".json");
            if (!entityFileList.contains(name))
            {
                String data = getDataFromURL("https://www.wikidata.org/wiki/Special:EntityData/" + name).toString();
                if (fileExist(entityAdditionalJsonPath + "/" + name))
                    continue;
                for (int i = 0; i < 4; i++)
                {
                    if (data.contains(vietnamWord[i]))
                    {
                        writeFile("AnalysedURLsAdditional.txt", name.replace(".json", ""), true);
                        writeFile(entityAdditionalJsonPath + "/" + name, data, false);
                        break;
                    }
                }
            }
        }
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

    void getPropertiesInJson(String root, String fileName, HashSet<String> entityJSONFileList) throws Exception
    {
        JSONObject entityJSON = new JSONObject(readFileAll(entityJsonPath + '/' + fileName));
        JSONObject claims = (JSONObject)(((JSONObject) ((JSONObject) entityJSON.get("entities")).get(fileName.replace(".json",""))).get("claims"));
        jsonPropertiesAnalysis(claims, entityJSONFileList);
    }

    /**
     * Get all properties of all entities and save it to folder "Properties".
     * @throws Exception
     */
    @Override
    protected void getProperties() throws Exception
    {
        if (fileExist(superpath + "PropertiesList.json"))
        {
            JSONArray myJsonArray = new JSONArray(readFileAll(superpath + "PropertiesList.json"));
            for (int i = 0; i < myJsonArray.length(); i++) { 
                propertyHashSet.add((String)myJsonArray.get(i));
            }
        }
        else
        {
            HashSet<String> entityFileList = listAllFiles(entityJsonPath);
            for (String fileName: entityFileList)
            {
                if (isFileExists(entityJsonPath + "/" + fileName))
                {
                    getPropertiesInJson(entityJsonPath,fileName, entityFileList);
                }
            }
            writeFile(superpath + "PropertiesList.json", (new JSONArray(propertyHashSet)).toString(), false);
        }
        HashSet<String> propertyFileList = listAllFiles(entityPropertiesPath);
        for (String property: propertyHashSet)
        {
            if (property.contains("P"))
            {
                if (!propertyFileList.contains(property + ".json"))
                    writeFile(entityPropertiesPath + '/' + property + ".json", getDataFromURL("https://www.wikidata.org/wiki/Special:EntityData/" + property + ".json").toString(),false);
            }
        }
        for (String property: propertyHashSet)
        {
            if (property.contains("Q"))
            {
                if (!propertyFileList.contains(property + ".json"))
                    writeFile(entityPropertiesPath + '/' + property + ".json", getDataFromURL("https://www.wikidata.org/wiki/Special:EntityData/" + property + ".json").toString(),false);
            }
        }
    }
}