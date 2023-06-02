import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.json.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class WikiAnalys extends WikiData{
    public static void main(String[] args) throws Exception {
        WikiAnalys myWikiAnalys = new WikiAnalys();
        //myWikiAnalys.getPropertyLabels();
        myWikiAnalys.invalidEntities();
        myWikiAnalys.urlToEntities();
        //myWikiAnalys.entityRefFinal();
        myWikiAnalys.entityFinal();
        
    }

    HashMap<String, String> urlToEntitiesHashMap = new HashMap<>();
    HashSet<String> allQFile = listAllFiles(entityJsonPath);
    HashSet<String> invalidEntities = new HashSet<>();
    HashSet<String> validEntities = new HashSet<>();
    HashSet<String> allPFile = listAllFiles(entityPropertiesPath);
    HashSet<String> propertyEntityHashSet = new HashSet<>();

    String wikiAnalysPath = superpath + "/WikiAnalys/";

    public final void getPropertyLabels() throws Exception
    {
        HashMap<String, HashMap<String, String> > propertyLabelsHashMap = new HashMap<>();
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
            for (String fileName: allQFile)
            {
                String qID = fileName.replace(".json", "");
                if (!invalidEntities.contains(qID))
                {
                    validEntities.add(qID);
                }
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

    public String getOverview(String filePath)
    {
        String overview = "";
        if (fileExist(filePath))
        {
            String data = "";
            try {
                data = readFileAll(filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Document doc = Jsoup.parse(data);
            Element divTag = doc.getElementById("mw-content-text"); 
            StringBuffer overviewSB = new StringBuffer();
            for (Element tag: divTag.getAllElements())
            {
                if ((tag.tagName()).equals("meta")) break;
                if ((tag.tagName()).equals("h2")) break;
                if ((tag.tagName()).equals("p"))
                {
                    overviewSB.append(tag.text() + '\n');
                }
            }
            overview = overviewSB.toString();
        }
        return overview;
    }

    public String getViLabel(String filePath, String qID) throws Exception
    {
        String viLabelValue = " ";
        JSONObject content = getJSONFromFile(filePath);
        if (!validEntities.contains(qID)) return viLabelValue;
        JSONObject entities = (JSONObject)content.get("entities");
        JSONObject entity = (JSONObject)entities.get(qID);
        JSONObject labels = (JSONObject)entity.get("labels");
        if (labels.has("vi"))
        {
            JSONObject viLabel = (JSONObject)labels.get("vi");
            viLabelValue = (String)viLabel.get("value");
        }
        else
        {
            JSONObject sitelinks = (JSONObject)entity.get("sitelinks");
            if (sitelinks.has("viwiki"))
            {
                JSONObject viwiki = (JSONObject)sitelinks.get("viwiki");
                viLabelValue = (String)viwiki.get("title");
            }
        }         
        return viLabelValue;
    }

    public final JSONObject propertyProcess(JSONObject infoObj, String uID) throws Exception
    {
        JSONObject jsonObj = new JSONObject();
        String datatype = (String)infoObj.get("datatype");
        JSONObject datavalue = new JSONObject();
        if (!infoObj.has("datavalue"))
        {
            return jsonObj;
        }
        datavalue = (JSONObject)infoObj.get("datavalue");
        if (datatype.equals("wikibase-item") || datatype.equals("wikibase-property"))
        {
            JSONObject value = (JSONObject)datavalue.get("value");
            String id = (String)value.get("id");
            if (validEntities.contains(id)){
                jsonObj.put("value", id);
                jsonObj.put("type", "wikibase-item");
            }
            else if (propertyEntityHashSet.contains(id))
            {
                String viLabel = getViLabel(entityPropertiesPath + "/" + id + ".json", id);
                if (!viLabel.isEmpty()){
                    jsonObj.put("value", viLabel);
                    jsonObj.put("type", "string");
                }

            }
        }
        else if (datatype.equals("quantity"))
        {
            JSONObject value = (JSONObject)datavalue.get("value");
            String amount = ((String)value.get("amount")).replace("+", "");
            String unit = (String)value.get("unit");
            if (unit.equals("1"))
            {
                unit = " ";
            }
            else
            {
                String qID = unit.replace("http://www.wikidata.org/entity/", "");
                unit = getViLabel(entityPropertiesPath + "/" + qID + ".json", qID); 
            }
            jsonObj.put("value", amount + " " + unit);
            jsonObj.put("type", "string");

        }
        else if (datatype.equals("string"))
        {
            String value = (String)datavalue.get("value");
            jsonObj.put("value", value);
            jsonObj.put("type", "string");
        }
        else if (datatype.equals("monolingualtext"))
        {
            JSONObject value = (JSONObject)datavalue.get("value");
            String lang = (String)value.get("language");
            if (lang.equals("vi"))
            {
                jsonObj.put("value", (String)value.get("text"));
                jsonObj.put("type", "string");
            }
        }
        else if (datatype.equals("time"))
        {
            JSONObject value = (JSONObject)datavalue.get("value");
            String time = (String)value.get("time");
            String year = time.substring(0, 5);
            String formatDMY = "";
            if (!year.contains("0000"))
            {
                String month = time.substring(6, 8);
                if (!month.contains("00"))
                {
                    String day = time.substring(9, 11);
                    if (!day.contains("00"))
                    {
                        formatDMY = "Ngày " + day + " ";
                    }
                    formatDMY += "Tháng " + month + " ";
                }
                formatDMY += "Năm " + year.substring(1, 5);
                if (year.contains("-"))
                    formatDMY += " trước công nguyên";
            }
            jsonObj.put("value", formatDMY);
            jsonObj.put("type", "string");
        }
        return jsonObj;
    }

    public final void entityFinal() throws Exception
    {
        print("Here");
        String entityFinalPath = superpath + "WikiAnalys/EntityFinal/";
        for (String fileName: propertyEntityHashSet)
        {
            propertyEntityHashSet.add(fileName.replace(".json",""));
        }
        for (String fileName: allQFile)
        {
            JSONObject json = new JSONObject();
            JSONObject content = getJSONFromFile(entityJsonPath + "/" + fileName);
            String qID = fileName.replace(".json", "");
            if (!validEntities.contains(qID)) continue;
            JSONObject entities = (JSONObject)content.get("entities");
            JSONObject entity = (JSONObject)entities.get(qID);

            /*
             * Get ID of entity
             */
            json.put("id",(String)entity.get("id"));

            /*
             * Get label of entity
             */
            json.put("label", getViLabel(entityJsonPath + "/" + fileName, qID));

            /*
             * Get description of entity
             */
            String viDescriptionValue = "";
            JSONObject descriptions = (JSONObject)entity.get("descriptions");
            if (descriptions.has("vi"))
            {
                JSONObject viDescriptions = (JSONObject)descriptions.get("vi");
                viDescriptionValue = (String)viDescriptions.get("value");
                json.put("description", viDescriptionValue);
            }

            /*
             * Get overview of entity
             */
            json.put("overview", getOverview(htmlPath + "/" + qID + ".html"));

            /*
             * Get aliases of entity
             */
            JSONObject aliases = (JSONObject)entity.get("aliases");
            ArrayList<String> arrayList = new ArrayList<>();
            if (aliases.has("vi"))
            {
                JSONArray viAlias = (JSONArray)aliases.get("vi");
                for (int i = 0 ; i < viAlias.length() ; i++)
                {
                    String viAliasValue = (String)(((JSONObject)(viAlias.get(i))).get("value"));
                    arrayList.add(viAliasValue);
                }
            }
            json.put("aliases", new JSONArray(arrayList));

            /*
             * Get claims of entity
             */
            JSONObject myClaims = new JSONObject();
            JSONObject claims = (JSONObject)entity.get("claims");
            Iterator<String> properties = ((JSONObject) claims).keys();
            while (properties.hasNext()) {
                String property = properties.next();
                JSONArray propertyInfoArr = new JSONArray();
                propertyInfoArr = (JSONArray)(claims).getJSONArray(property);
                JSONArray jsonArray = new JSONArray();
                for (Object info: propertyInfoArr)
                {
                    JSONObject infoObj = (JSONObject) info;
                    JSONObject mainsnak = (JSONObject)infoObj.get("mainsnak");
                    JSONObject jsonObj = propertyProcess(mainsnak, qID);
                    if (jsonObj.length() == 0) continue;
                    jsonArray.put(jsonObj);
                }
                if (jsonArray.length()>0)
                    myClaims.put(property, jsonArray);
            }
            json.put("claims", myClaims);
        }
    }

}