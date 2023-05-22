/**
 *  The "WikiData" class provides useful methods for analyzing Wikipedia pages and extracting relevant information related to entities in Vietnam.
 */
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
        createFolder(superpath + "VietnamRelated");
    }

    public static void main(String[] args) throws Exception {
        WikiData wikiData = new WikiData();
        wikiData.getData();
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
     * @param EntityJSON the JSON of an entity.
     * @return Member variable isRelated is {@code true} if that entity has any properties that are related to Vietnam, else {@code false}.
     */
    protected void JSONAnalysis(Object EntityJSON)
    {
        if (isRelated == true)
            return;
        if (EntityJSON instanceof JSONArray)
        {
            for (int i = 0; i < ((JSONArray) EntityJSON).length(); i++) { 
                JSONAnalysis(((JSONArray) EntityJSON).get(i));
            }
        }
        else if (EntityJSON instanceof JSONObject)
        {
            if (((JSONObject) EntityJSON).has("numeric-id"))
            {
                if (vietnamEntityHashSet.contains(((JSONObject) EntityJSON).get("id")))
                {
                    isRelated = true;
                    return;
                }
            }
            Iterator<String> keys = ((JSONObject) EntityJSON).keys();
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = ((JSONObject) EntityJSON).get(key);
                
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
}