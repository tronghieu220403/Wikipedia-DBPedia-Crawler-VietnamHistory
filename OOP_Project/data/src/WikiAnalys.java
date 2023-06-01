import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.json.*;

public class WikiAnalys extends WikiData{
    public static void main(String[] args) throws Exception {
        WikiAnalys myWikiAnalys = new WikiAnalys();
        //myWikiAnalys.getPropertyLabels();
        myWikiAnalys.invalidEntities();
        myWikiAnalys.urlToEntities();
        //myWikiAnalys.entityRefFinal();
        
    }

    HashMap<String, String> urlToEntitiesHashMap = new HashMap<>();
    HashSet<String> allQFile = listAllFiles(entityJsonPath);
    HashSet<String> invalidEntities = new HashSet<>();

    String wikiAnalysPath = superpath + "/WikiAnalys/";

    public final void getPropertyLabels() throws Exception
    {
        HashMap<String, HashMap<String, String> > propertyLabelsHashMap = new HashMap<>();
        HashSet<String> allPFile = listAllFiles(entityPropertiesPath);
        for (String fileName: allPFile)
        {
            if (fileName.contains("P"))
            {
                JSONObject content = getJSONFromFile(entityPropertiesPath + "/" + fileName);
                JSONObject entities  = (JSONObject )content.get("entities");
                String pID = fileName.replace(".json", "");
                JSONObject entitiyID = (JSONObject )entities.get(pID);
                JSONObject labels = (JSONObject )entitiyID.get("labels");
                String labelVN = "";
                String labelENG = "";
                if (labels.has("vi"))
                {
                    labelVN = (String )(((JSONObject )(labels.get("vi"))).get("value"));
                }
                if (labels.has("en"))
                {
                    labelENG = (String )(((JSONObject )(labels.get("en"))).get("value"));
                }
                HashMap<String, String> lang = new HashMap<>();
                if (!labelVN.isEmpty()) {
                    lang.put("vi", labelVN);
                }
                if (!labelENG.isEmpty()) {

                    lang.put("en", labelENG);
                }
                if (lang.size() > 1)
                    if (labelENG.contains("Viet") || !containsUpperCase(labelENG))
                        propertyLabelsHashMap.put(pID, lang);
            }
        }
        writeFile(wikiAnalysPath + "PLabels.json" , (new JSONObject(propertyLabelsHashMap)).toString(), false);
    }

    public final void invalidEntities() throws Exception
    {
        if (fileExist(wikiAnalysPath + "InvalidEntities.json"))
        {
            JSONObject content = getJSONFromFile(wikiAnalysPath + "InvalidEntities.json");
            JSONArray data = (JSONArray)content.get("InvalidEntities");
            for (int i = 0; i < data.length(); i++) {
                String qID = data.getString(i); 
                invalidEntities.add(qID); 
            }
            return;
        }
        for (String fileName: allQFile)
        {
            JSONObject content = getJSONFromFile(entityJsonPath + "/" + fileName);
            JSONObject entities  = (JSONObject )content.get("entities");
            String qID = fileName.replace(".json", "");
            JSONObject entitiyContent = (JSONObject )entities.get(qID);
            if (getSitelink(entitiyContent, qID, "viwiki").equals(""))
            {
                invalidEntities.add(qID);
                continue;
            }
            String instance = getInstance(entitiyContent, qID);
            if (instance.equals("Q5"))
            {
                int entityMinYear = getMinYear(entitiyContent);
                if (entityMinYear == 100000)
                {
                    continue;
                }    
                if (entityMinYear > 1962) {
                    invalidEntities.add(qID);
                }
                continue;
            }
        }
        JSONObject json = new JSONObject();
        ArrayList<String> invalidEntitiesArr = new ArrayList<>(invalidEntities);
        json.put("InvalidEntities", invalidEntitiesArr);
        writeFile(wikiAnalysPath + "InvalidEntities.json" , json.toString(), false);
    }

    private int getMinYear(Object entityJSON)
    {
        int minYear = 100000;
        if (entityJSON instanceof JSONArray)
        {
            for (int i = 0; i < ((JSONArray) entityJSON).length(); i++) { 
                minYear = Math.min(getMinYear(((JSONArray)entityJSON).get(i)),minYear);
                if (minYear < 1962)
                {
                    return minYear;
                }
            }
        }
        else if (entityJSON instanceof JSONObject)
        {
            if (((JSONObject) entityJSON).has("datatype"))
            {
                if (((String)((JSONObject) entityJSON).get("datatype")).equals("time"))
                {
                    if (!((JSONObject) entityJSON).has("datavalue"))
                        return minYear;
                    JSONObject datavalue = (JSONObject) ((JSONObject) entityJSON).get("datavalue");
                    if (datavalue.has("value"))
                    {
                        JSONObject value = (JSONObject)(datavalue.get("value"));
                        String time = (String)(value.get("time"));
                        String sign = time.substring(0,1);
                        if (sign.equals("-"))
                        {
                            minYear = 0; 
                        }
                        else minYear = Integer.parseInt(time.substring(1,5));
                    }
                }
                return minYear;
            }
            Iterator<String> keys = ((JSONObject) entityJSON).keys();
            while (keys.hasNext()) {
                String key = keys.next();
                if (key.equals("references")){
                    continue;
                }
                Object value = ((JSONObject) entityJSON).get(key);
                
                if (value instanceof JSONObject) {
                    minYear = Math.min(getMinYear((JSONObject) value), minYear);
                } else if (value instanceof JSONArray) {
                    minYear = Math.min(getMinYear((JSONArray) value), minYear);
                }
                if (minYear < 1962)
                {
                    return minYear;
                }
            }
        }
        return minYear;
    }


    private final String getInstance(JSONObject entitiyContent, String qID)
    {
        String instance = "";
        JSONObject claims = (JSONObject)entitiyContent.get("claims");
        if (!claims.has("P31")){
            return instance;
        }
        JSONArray p31Arr = (JSONArray)(claims.get("P31"));
        for (int i = 0 ; i < p31Arr.length() ; i++)
        {
            JSONObject mainsnak = ((JSONObject)(p31Arr.getJSONObject(i).get("mainsnak")));
            if (mainsnak.has("datavalue"))
            {
                JSONObject datavalue = (JSONObject)(mainsnak.get("datavalue"));
                JSONObject value = (JSONObject)datavalue.get("value");
                instance = (String )value.get("id");
                if (!instance.equals("")) 
                    break;
            }
        }
        return instance;
    }

    private final String getSitelink(JSONObject entitiyContent, String qID, String wikiLang) throws Exception
    {
        JSONObject sitelinks = (JSONObject )entitiyContent.get("sitelinks");
        String sitelinkVN = "";
        String sitelinkENG = "";
        if (wikiLang.equals("viwiki"))
        {
            if (sitelinks.has("viwiki"))
            {
                sitelinkVN = (String )(((JSONObject )(sitelinks.get("viwiki"))).get("url"));
            }
            return sitelinkVN;
        }
        if (wikiLang.equals("enwiki"))
        {
            if (sitelinks.has("enwiki"))
            {
                sitelinkENG = (String )(((JSONObject )(sitelinks.get("enwiki"))).get("url"));
            }
            return sitelinkENG;
        }
        return sitelinkVN;
    }

    public final void urlToEntities() throws Exception
    {
        if (fileExist(wikiAnalysPath + "" + "URLToEntities.json"))
        {
            JSONObject jsonContent = getJSONFromFile(wikiAnalysPath + "" + "URLToEntities.json");
            Iterator<String> keys = ((JSONObject) jsonContent).keys();
            while (keys.hasNext()) {
                String key = keys.next();
                urlToEntitiesHashMap.put(key,(String)((jsonContent).get(key)));
            }
            return;
        }
        for (String fileName: allQFile)
        {
            JSONObject content = getJSONFromFile(entityJsonPath + "/" + fileName);
            JSONObject entities  = (JSONObject )content.get("entities");
            String qID = fileName.replace(".json", "");
            if (invalidEntities.contains(qID))
            {
                continue;
            }
            JSONObject entitiyID = (JSONObject )entities.get(qID);
            JSONObject sitelinks = (JSONObject )entitiyID.get("sitelinks");
            String sitelinkVN = "";
            String sitelinkENG = "";
            if (sitelinks.has("viwiki"))
            {
                sitelinkVN = (String )(((JSONObject )(sitelinks.get("viwiki"))).get("url"));

                //urlToEntitiesHashMap.put(sitelinkVN, qID);
                urlToEntitiesHashMap.put(urlDecode(sitelinkVN), qID);
            }
            if (sitelinks.has("enwiki"))
            {
                sitelinkENG = (String )(((JSONObject )(sitelinks.get("enwiki"))).get("url"));
                urlToEntitiesHashMap.put(sitelinkENG, qID);
            }
        }
        writeFile(wikiAnalysPath + "" + "URLToEntities.json" , (new JSONObject(urlToEntitiesHashMap)).toString(), false);
    }

    public final void entityRefFinal() throws Exception
    {
        String refFinalPath = superpath + "WikiAnalys/EntityRefFinal/";
        HashSet<String> allQRefFile = listAllFiles(superpath + "EntityReference/");
        HashMap<String, HashSet<String> > refList = new HashMap<String, HashSet<String>>();
        for (String fileName: allQFile)
        {
            String qID = fileName.replace(".json", "");
            if (invalidEntities.contains(qID)) continue;
            HashSet<String> h = new HashSet<>();
            refList.putIfAbsent(qID, h);
        }
        for (String fileName: allQRefFile)
        {
            String qID = fileName.replace(".txt", "");
            if (invalidEntities.contains(qID)) continue;
            List<String> qRef = readFileAllLine(superpath + "EntityReference/" + fileName);
            for (String urlString: qRef)
            {
                urlString = urlDecode(urlString);
                if (urlToEntitiesHashMap.containsKey(urlString))
                {
                    String qID1 = urlToEntitiesHashMap.get(urlString);
                    if (invalidEntities.contains(qID1)) continue;
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
                    //refList.get(qID).add(qID1);
                    //refList.get(qID1).add(qID);
                }
            }
        }
        print("DONE");
        refList.forEach((key, value) -> {
            try {
                writeFile(refFinalPath + key + ".json", (new JSONArray(value)).toString() , false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}