
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

public class WikiData extends DataHandling{
        
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

    
    @Override
    public void entityAnalys(String url, int depth) throws Exception {
        
        url = url.replace("\n", "");
        if (checkURL(url) == false)
        {
            return;
        }
        String data = "";
        try {
            data = getDataFromURL(url).toString();
        } catch (Exception e) {
            //System.out.println("Error in " + url);    
            return;
        }

        // Parse the HTML using Jsoup
        Document doc = Jsoup.parse(data);
        
        String entityTmp = getEntityID(doc);
        if (!entityTmp.equals(""))
        {
            if (fileExist(superpath + "WebHtml/" + entityTmp + ".html"))
            {
                if (fileExist(superpath + "EntityJson/" + entityTmp + ".json"))
                {
                    writeFile(analysedURLsPath, url + '\n', true);
                }
                else writeFile(failedURLsPath, url + '\n', true);
                return;
            }
            writeFile(superpath + "WebHtml/" + entityTmp + ".html", data, false);

        }
        else {
            writeFile(failedURLsPath, url + '\n', true);
            return;
        }
        Object entityID = checkRelated(doc);
        if (entityID instanceof Boolean)
        {
            writeFile(failedURLsPath, url + '\n', true);
            return;
        }
        
        writeFile(analysedURLsPath, url + '\n', true);
        
        Element divTag = doc.getElementById("mw-content-text"); 
        for (Element aTag : divTag.select("a")) {
            String href = aTag.attr("href");
            String refURL = "https://vi.wikipedia.org" + href; 
            if (!checkURL(refURL)) continue;         
            
            writeFile(superpath + "EntityReference/" + entityID + ".txt", refURL + '\n', true);
            addRef(refURL, depth);
        }
        return;
    }

    private String[] filter = {
        "#","T%E1%BA%ADp_tin", 
        "File:", "Wikipedia:",
        "Th%E1%BB%83_lo%E1%BA%A1i:"
    };
    

    @Override
    public boolean checkURL(String url) {
        if (url == null || url.isEmpty()) return false;  
        if (!url.contains("/wiki/")) return false;
        
        for (String text : filter) {
            if (url.contains(text)) return false;
        }
        
        if (url.chars().filter(ch -> ch == ':').count() > 1) {
            return false;  
        }
        
        return true;  
    }

    public void JSONAnalysis(Object myJSON)
    {
        if (isRelated == true)
            return;
        if (myJSON instanceof JSONArray)
        {
            for (int i = 0; i < ((JSONArray) myJSON).length(); i++) { 
                JSONAnalysis(((JSONArray) myJSON).get(i));
            }
        }
        else if (myJSON instanceof JSONObject)
        {
            if (((JSONObject) myJSON).has("numeric-id"))
            {
                if (vietnamEntityHashSet.contains(((JSONObject) myJSON).get("id")))
                {
                    isRelated = true;
                    return ;
                }
            }
            Iterator<String> keys = ((JSONObject) myJSON).keys();
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = ((JSONObject) myJSON).get(key);
                
                if (value instanceof JSONObject) {
                    JSONAnalysis((JSONObject) value);
                } else if (value instanceof JSONArray) {
                    JSONAnalysis((JSONArray) value);
                }
            }
        }
    }

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

    @Override
    public Object checkRelated(Document soupHWND) throws Exception {
        String entityURL = "";
        Element liTag = soupHWND.getElementById("t-wikibase");
        if (liTag == null)
            return (Boolean)false;

        for (Element aTag : liTag.select("a")) {
            entityURL = aTag.attr("href");
            break;
        }
        if (entityURL == "")
            return (Boolean)false;
        entityURL= entityURL.replace("Page", "Data");
        String entityID = entityURL.replace("https://www.wikidata.org/wiki/Special:EntityData/","");
        if (fileExist(superpath + "EntityJson/" +entityID +".json") == true)
        {
            return entityID;
        }
        entityURL = entityURL + ".json";
        JSONObject jsonData = getJSONFromURL(entityURL);
        isRelated = false;
        JSONAnalysis(jsonData);
        if (isRelated == false)
            return (Boolean)false;
        String content = (jsonData).toString();
        writeFile(superpath + "EntityJson/" +entityID +".json", content , false);
        return entityID;
    }

    public WikiData()
    {
        super("E:/Code/Java/OOP_Project/saveddata/Wikipedia/");
    }

    public static void main(String[] args) throws Exception {
        WikiData wikiData = new WikiData();
        wikiData.getData();
    }

}