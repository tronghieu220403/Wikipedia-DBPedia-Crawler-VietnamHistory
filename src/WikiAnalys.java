import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.json.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class WikiAnalys extends WikiData{
    public static void main(String[] args) throws Exception {
        
        WikiAnalys myWikiAnalys = new WikiAnalys();
        //myWikiAnalys.getInvalidEntities();
        //myWikiAnalys.urlToEntities();
        //myWikiAnalys.entityRefFinal();
        //myWikiAnalys.entityFinal();
        //myWikiAnalys.getFestivals();
        myWikiAnalys.getKings();
        //myWikiAnalys.export();
    }

    public WikiAnalys()
    {
        createFolder(superpath + "/WikiAnalys");
        //createFolder(superpath + );
    }


    private void getFestivals() throws Exception
    {
        allPFile = listAllFiles("E:\\Code\\Java\\OOP_Project\\saveddata\\Wikipedia\\EntityProperties");
        Set<String> bannedFestivalURLs = new HashSet<>(Arrays.asList("https://vi.wikipedia.org/wiki/Lễ_hội_các_dân_tộc_Việt_Nam",
            "https://vi.wikipedia.org/wiki/Lễ_hội_Lào",
            "https://vi.wikipedia.org/wiki/Lễ_hội_Nhật_Bản",
            "https://vi.wikipedia.org/wiki/Lễ_hội_Thái_Lan", "https://vi.wikipedia.org/wiki/Lễ_hội", "https://vi.wikipedia.org/wiki/Lễ_hội_Việt_Nam"));
        String urls[] = {"https://vi.wikipedia.org/wiki/L%E1%BB%85_h%E1%BB%99i_c%C3%A1c_d%C3%A2n_t%E1%BB%99c_Vi%E1%BB%87t_Nam", "https://vi.wikipedia.org/wiki/L%E1%BB%85_h%E1%BB%99i_Vi%E1%BB%87t_Nam"};
        HashSet<String> urlSet = new HashSet<>();
        for (String urlString: urls)
        {
            String data = "";
            try {
                data = getDataFromURL(urlString).toString();
            } catch (Exception e) {
                System.out.println("Error in " + urlString);    
                return;
            }
            Document doc = Jsoup.parse(data);
            Element divTag = doc.getElementById("mw-content-text"); 

            for (Element aTag : divTag.select("a")) {
                String href = aTag.attr("href");
                String craftURL = "https://vi.wikipedia.org" + href; 
                if (!checkURL(craftURL)) continue;
                craftURL = urlDecode(craftURL);
                if ((craftURL.contains("Lễ_hội") || craftURL.contains("Hội")) && !bannedFestivalURLs.contains(craftURL)){
                    if (!urlSet.contains(craftURL))
                    {
                        urlSet.add(craftURL);
                    }
                }
            }
        }
        HashSet<String> qIDSet = new HashSet<>();
        for (String urlString: urlSet)
        {
            String data = "";
            try {
                data = getDataFromURL(urlString).toString();
            } catch (Exception e) {
                //System.out.println("Error in " + url);    
                return;
            }
            Document doc = Jsoup.parse(data);
            String qID = getEntityID(doc);
            if (qID.equals("")) continue;
            writeFile(superpath + "WebHtml/" + qID + ".html", data, false);
            qIDSet.add(qID);
            String entityURL = "https://www.wikidata.org/wiki/Special:EntityData/" + qID + ".json";
            JSONObject jsonData = getJSONFromURL(entityURL);
            writeFile(superpath + "EntityJson/" + qID +".json", jsonData.toString() , false);

            /*
            * Get related URL for this entity.
            * The related URLs is in "EntityReference" folder. 
            */
            if (!fileExist(superpath + "EntityReference/" + qID + ".txt"))
            {
                Element divTag = doc.getElementById("mw-content-text"); 
                for (Element aTag : divTag.select("a")) {
                    String href = aTag.attr("href");
                    String refURL = "https://vi.wikipedia.org" + href; 
                    if (!checkURL(refURL)) continue;         
                    
                    writeFile(superpath + "EntityReference/" + qID + ".txt", urlDecode(refURL) + '\n', true);
                }
            }
            JSONObject json = getVietnameseWikiReadable(qID + ".json");
            //json.getJSONObject("claims").
            JSONObject claims = json.getJSONObject("claims");
            JSONObject addObj = new JSONObject();
            addObj.put("value", "lễ hội");
            addObj.put("type", "string");
            JSONArray jsonArr = new JSONArray();
            jsonArr.put(addObj);
            claims.put("là một", jsonArr);
            if (!claims.has("quốc gia"))
            {
                JSONObject addObj2 = new JSONObject();
                addObj2.put("value", "Việt Nam");
                addObj2.put("type", "wikibase-item");
                addObj2.put("id", "Q881");
                jsonArr = new JSONArray();
                jsonArr.put(addObj2);
                claims.put("quốc gia", jsonArr);
            }
            writeFile("E:\\Code\\Java\\OOP_Project\\saveddata\\Wikipedia\\WikiAnalys\\Category\\export\\lễ hội văn hóa\\" + qID + ".json", json.toString(), false);
            writeFile("E:\\Code\\Java\\OOP_Project\\saveddata\\Wikipedia\\WikiAnalys\\EntityFinal\\" + qID + ".json", json.toString(), false);
        }
    }

    private void getKings() throws Exception
    {
        JSONObject urlMapped = getJSONFromFile(superpath + "/WikiAnalys/URLToEntities.json");
        String[] kingProp = {"Miếu hiệu", "Tôn hiệu hoặc Thụy hiệu", "Niên hiệu", "Thế thứ", "Trị vì"};
        JSONObject allDynastyJsonObject = getJSONFromFile(superpath + "/VVN.json");
        HashMap<String, String> dynastyHashMap = new HashMap<>();
        int cnt = 1;
        for (String fileName: listAllFiles("data/triều đại lịch sử"))
        {
            JSONObject json = getJSONFromFile("data/triều đại lịch sử/" + fileName);
            String dynastyName = json.getString("label");
            dynastyHashMap.put(dynastyName, json.getString("id"));
        }

        for (String dynastyName: getAllKeys(allDynastyJsonObject))
        {
            JSONObject dynastyJsonObject = new JSONObject();
            if (!dynastyHashMap.containsKey(dynastyName))
            {
                JSONObject claims = new JSONObject();
                JSONArray jsonArr = new JSONArray();
                String qID = "Q" + Integer.toString(cnt) + "X";
                cnt++;
                dynastyHashMap.put(dynastyName, qID);
                JSONObject addObj = new JSONObject();
                addObj.put("value", "triều đại");
                addObj.put("type", "string");
                jsonArr.put(addObj);
                claims.put("là một", jsonArr);
                jsonArr = new JSONArray();
                JSONObject addObj2 = new JSONObject();
                addObj2.put("value", "Việt Nam");
                addObj2.put("type", "string");
                jsonArr.put(addObj2);
                claims.put("quốc gia", jsonArr);
                cnt++;
                dynastyJsonObject.put("claims", claims);
                dynastyJsonObject.put("overview",  dynastyName + " là một triều đại phong kiến trong lịch sử Việt Nam.");
                dynastyJsonObject.put("aliases", new JSONArray());
                dynastyJsonObject.put("id", qID);
            }
            else
            {
                dynastyJsonObject = getJSONFromFile("data/nhân vật lịch sử/" + dynastyHashMap.get(dynastyName) + ".json");
            }

            String dynastyQID = dynastyHashMap.get(dynastyName);
            JSONArray refKingArr = new JSONArray();
            JSONArray kingArr = allDynastyJsonObject.getJSONArray(dynastyName);
            for (int i = 0; i < kingArr.length(); i++)
            {
                String qID = "";
                JSONObject king = kingArr.getJSONObject(i);
                String kingURL = urlDecode(king.getString("link"));
                JSONObject kingJsonObject = new JSONObject();
                JSONObject kingClaims = new JSONObject();
                JSONObject kingRef = new JSONObject();
                String name = "";
                if (urlMapped.has(kingURL))
                {
                    qID = urlMapped.getString(kingURL);
                    kingJsonObject = getJSONFromFile("data/nhân vật lịch sử/" + qID + ".json");
                    kingClaims = kingJsonObject.getJSONObject("claims");
                    name = kingJsonObject.getString("label");
                }
                else
                {
                    qID = "Q" + Integer.toString(cnt) + "X";
                    cnt++;
                    name = king.getString("Vua");
                    kingJsonObject.put("label", name);
                    JSONArray jsonArr = new JSONArray();
                    JSONObject addObj = new JSONObject();
                    addObj.put("value", "người");
                    addObj.put("type", "string");
                    jsonArr.put(addObj);
                    kingClaims.put("là một", jsonArr);
                    jsonArr = new JSONArray();
                    JSONObject addObj2 = new JSONObject();
                    addObj2.put("value", "Việt Nam");
                    addObj2.put("type", "string");
                    jsonArr.put(addObj2);
                    kingClaims.put("quốc tịch", jsonArr);
                    cnt++;
                }
                JSONArray kingInstances = kingClaims.getJSONArray("là một");
                JSONObject kingInstanceObj = new JSONObject();
                kingInstanceObj.put("type", "string");
                kingInstanceObj.put("value", "vua");
                kingInstances.put(kingInstanceObj);

                for (String prop: kingProp)
                {
                    JSONArray arr = new JSONArray();
                    JSONObject propObj = new JSONObject();
                    propObj.put("type", "string");
                    propObj.put("value", king.getString(prop));
                    arr.put(propObj);
                    kingClaims.put(prop, arr);
                }
                if (qID.contains("X"))
                {
                    kingJsonObject.put("claims", kingClaims);
                    kingJsonObject.put("aliases", new JSONArray());
                    kingJsonObject.put("overview",  dynastyName + " là một vị vua trong lịch sử Việt Nam.");
                    kingJsonObject.put("id", qID);
                    kingJsonObject.put("references", new JSONObject());
                    urlMapped.put(kingURL, qID);
                }
                JSONObject refJSONObj = new JSONObject();
                refJSONObj.put("type", "wikibase-item");
                refJSONObj.put("value", name);
                refJSONObj.put("id", qID);
                refKingArr.put(refJSONObj);

                if (!kingJsonObject.has("triều đại"))
                {
                    kingRef.put("type", "wikibase-item");
                    kingRef.put("value", dynastyName);
                    kingRef.put("id", dynastyQID);
                    JSONArray kingRefArr = new JSONArray();
                    kingRefArr.put(kingRef);
                    kingJsonObject.put("triều đại", kingRefArr);
                }
            }
            dynastyJsonObject.put("references", (new JSONObject()).put("vua", refKingArr));
            writeFile("gg.json", dynastyJsonObject.toString(), false);
            break;
        }
        
    }

    HashMap<String, String> urlToEntitiesHashMap = new HashMap<>();
    HashSet<String> allQFile = listAllFiles(entityJsonPath);
    HashSet<String> entiHashSet = new HashSet<>();
    HashSet<String> propertyEntityHashSet = new HashSet<>();
    String refFinalPath = superpath + "WikiAnalys/EntityRefFinal";
    String finalEntityPath = "E:/Code/Java/OOP_Project/saveddata/Wikipedia/WikiAnalys/EntityFinal";


    String wikiAnalysPath = superpath + "/WikiAnalys";

    public final void getInvalidEntities() throws Exception
    {
        for (String fileName: allQFile)
        {
            JSONObject content = getJSONFromFile(entityJsonPath + "/" + fileName);
            JSONObject entities  = (JSONObject )content.get("entities");
            String qID = fileName.replace(".json", "");
            JSONObject entitiyContent = (JSONObject )entities.get(qID);
            if (getSitelink(entitiyContent, qID, "viwiki").equals(""))
            {
                moveFile(entityJsonPath + "/" + qID + ".json", entityPropertiesPath + "/" + qID + ".json");
                continue;
            }
            String instance = getInstance(entitiyContent);
            if (instance.equals("Q5"))
            {
                int entityMinYear = getMinYear(entitiyContent);
                if (entityMinYear == 100000)
                {
                    continue;
                }    
                if (entityMinYear > 1962) {
                    moveFile(entityJsonPath + "/" + qID + ".json", entityPropertiesPath + "/" + qID + ".json");
                }
                continue;
            }
        }
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


    private final String getInstance(JSONObject entitiyContent)
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

    public final String getInstance(String qID) throws Exception
    {
        String instance = "";
        JSONObject jsonContent;
        if (fileExist(entityJsonPath + "/" + qID + ".json"))
            jsonContent = getJSONFromFile(entityJsonPath + "/" + qID + ".json");
        else if (fileExist(entityPropertiesPath + "/" + qID + ".json"))
            jsonContent = getJSONFromFile(entityPropertiesPath + "/" + qID + ".json");
        else return instance;
        JSONObject entities  = (JSONObject )jsonContent.get("entities");
        JSONObject entitiyContent = (JSONObject )entities.get(qID);
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
        if (fileExist(wikiAnalysPath  + "/" + "URLToEntities.json"))
        {
            JSONObject jsonContent = getJSONFromFile(wikiAnalysPath + "/" + "URLToEntities.json");
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
            JSONObject entitiyID = (JSONObject )entities.get(qID);
            JSONObject sitelinks = (JSONObject )entitiyID.get("sitelinks");
            String sitelinkVN = "";
            String sitelinkENG = "";
            if (sitelinks.has("viwiki"))
            {
                sitelinkVN = (String )(((JSONObject )(sitelinks.get("viwiki"))).get("url"));
                urlToEntitiesHashMap.put(urlDecode(sitelinkVN), qID);
            }
            if (sitelinks.has("enwiki"))
            {
                sitelinkENG = (String )(((JSONObject )(sitelinks.get("enwiki"))).get("url"));
                urlToEntitiesHashMap.put(sitelinkENG, qID);
            }
        }
        writeFile(wikiAnalysPath + "/" + "URLToEntities.json" , (new JSONObject(urlToEntitiesHashMap)).toString(), false);
    }

    public final void entityRefFinal() throws Exception
    {
        HashSet<String> allQRefFile = listAllFiles(superpath + "EntityReference/");
        HashMap<String, HashSet<String> > refList = new HashMap<String, HashSet<String>>();
        for (String fileName: allQFile)
        {
            String qID = fileName.replace(".json", "");
            HashSet<String> h = new HashSet<>();
            refList.putIfAbsent(qID, h);
        }
        for (String fileName: allQRefFile)
        {
            String qID = fileName.replace(".txt", "");
            List<String> qRef = readFileAllLine(superpath + "EntityReference/" + fileName);
            for (String urlString: qRef)
            {
                urlString = urlDecode(urlString);
                if (urlToEntitiesHashMap.containsKey(urlString))
                {
                    String entityID1 = urlToEntitiesHashMap.get(urlString);
                    if (!refList.containsKey(qID))
                    {
                        HashSet<String> h = new HashSet<>();
                        h.add(entityID1);
                        refList.put(qID, h);
                    }
                    else
                    {
                        refList.get(qID).add(entityID1);
                    }
                    if (!refList.containsKey(entityID1))
                    {
                        HashSet<String> h = new HashSet<>();
                        h.add(qID);
                        refList.put(entityID1, h);
                    }
                    else
                    {
                        refList.get(entityID1).add(qID);
                    }
                }
            }
        }
        refList.forEach((key, value) -> {
            try {
                writeFile(refFinalPath + "/" + key + ".json", (new JSONArray(value)).toString() , false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public String getOverview(String qID)
    {
        String filePath = htmlPath + "/" + qID + ".html";
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
            Element divID = doc.getElementById("mw-content-text"); 
            Element divTag = divID.select("div.mw-parser-output").first(); 
            StringBuffer overviewSB = new StringBuffer();
            for (Element tag: divTag.children())
            {
                if ((tag.tagName()).equals("meta")) break;
                if ((tag.tagName()).equals("h2")) break;
                if ((tag.tagName()).equals("p"))
                {
                    String tagContent = tag.text();
                    if (!tagContent.matches(".*[a-zA-Z].*")) continue;
                    tagContent = tagContent.replaceAll("/[.*?/]","");
                    overviewSB.append(tagContent);
                    break;
                }
            }
            overview = overviewSB.toString();
        }
        return overview;
    }

    private final JSONObject propertyProcess(JSONObject infoObj) throws Exception
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
            if (allQFile.contains(id + ".json")){
                jsonObj.put("value", getViLabel(id));
                jsonObj.put("id", id);
                jsonObj.put("type", "wikibase-item");
            }
            else if (allPFile.contains(id + ".json"))
            {
                String viLabel = getViLabel(id);
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
                unit = getViLabel(qID); 
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

    private JSONObject getVietnameseWikiReadable(String fileName) throws Exception
    {
        JSONObject json = new JSONObject();
        JSONObject content = getJSONFromFile(entityJsonPath + "/" + fileName);
        String qID = fileName.replace(".json", "");
        JSONObject entities = (JSONObject)content.get("entities");
        JSONObject entity = (JSONObject)entities.get(qID);

        /*
            * Get ID of entity
            */
        json.put("id",entity.getString("id"));

        /*
            * Get label of entity
            */
        json.put("label", getViLabel(qID));

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
        json.put("overview", getOverview(qID));

        /*
            * Get aliases of entity
            */
        JSONObject aliases = (JSONObject)entity.get("aliases");
        ArrayList<String> myAliases = new ArrayList<>();
        if (aliases.has("vi"))
        {
            JSONArray viAlias = (JSONArray)aliases.get("vi");
            for (int i = 0 ; i < viAlias.length() ; i++)
            {
                String viAliasValue = (String)(((JSONObject)(viAlias.get(i))).get("value"));
                myAliases.add(viAliasValue);
            }
        }
        json.put("aliases", new JSONArray(myAliases));

        /*
            * Get claims of entity
            */
        JSONObject myClaims = new JSONObject();
        JSONObject claims = (JSONObject)entity.get("claims");
        Iterator<String> properties = ((JSONObject) claims).keys();
        while (properties.hasNext()) {
            String propertyID = properties.next();
            /* Cho that entity if that entity has a name in Vietnamese */
            String propertyName = getViLabel(propertyID);
            if (propertyName.isEmpty())
                continue;
            
            JSONArray propertyInfoArr = new JSONArray();
            propertyInfoArr = (JSONArray)(claims).getJSONArray(propertyID);
            JSONArray jsonArray = new JSONArray();
            for (Object info: propertyInfoArr)
            {
                JSONObject infoObj = (JSONObject) info;
                JSONObject mainsnak = (JSONObject)infoObj.get("mainsnak");
                JSONObject jsonObj = propertyProcess(mainsnak);

                if (jsonObj.length() == 0) continue;
                
                /*
                    * Get qualifiers of a property (a qualifier field will describe a property more clear)
                    */
                if (infoObj.has("qualifiers"))
                {
                    JSONObject qualifiersJsonObj = new JSONObject();
                    JSONObject qualifiers = (JSONObject)infoObj.get("qualifiers");
                    Iterator<String> keys = ((JSONObject) qualifiers).keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        JSONArray myQualifiersPropertyJsonArray = new JSONArray();
                        JSONArray qualifiersPropertyJsonArr = qualifiers.getJSONArray(key);
                        for (Object propertyInfo: qualifiersPropertyJsonArr)
                        {
                            JSONObject propertyInfoJson = (JSONObject)propertyInfo;
                            JSONObject propertyJsonObj = propertyProcess(propertyInfoJson);
                            if (propertyJsonObj.length()>0)
                                myQualifiersPropertyJsonArray.put(propertyJsonObj);
                        }
                        if (myQualifiersPropertyJsonArray.length()>0)
                        {
                            qualifiersJsonObj.put(getViLabel(key), myQualifiersPropertyJsonArray);
                        }
                    }
                    if (qualifiersJsonObj.length()>0)
                        jsonObj.put("qualifiers", qualifiersJsonObj);

                }
                if (jsonObj.length()>0)
                    jsonArray.put(jsonObj);

            }
            if  (jsonArray.length()>0)
                myClaims.put(propertyName, jsonArray);
        }
        json.put("claims", myClaims);

        /*
        * Get references of entity
        */
        JSONObject myRef = new JSONObject();
        if (fileExist(refFinalPath + "/" + fileName))
        {
            HashMap<String, String> idInstance = new HashMap<>();
            String fileContent = readFileAll(refFinalPath + "/" + fileName);
            JSONArray jsonArray = new JSONArray(fileContent);
            for (Object iter: jsonArray)
            {
                String refEntityID = (String)iter;
                String instanceName = "";

                if (!idInstance.containsKey(refEntityID))
                {
                    String instanceID = getInstance(refEntityID);
                    if(instanceID.isEmpty()) continue;
                    instanceName = getViLabel(instanceID);
                    idInstance.put(refEntityID, instanceName);
                }
                else
                    instanceName = idInstance.get(refEntityID);
                if (instanceName.isEmpty()) continue;
                JSONObject refEntityIDObject = new JSONObject();
                refEntityIDObject.put("id", refEntityID);
                refEntityIDObject.put("type","wikibase-item");
                refEntityIDObject.put("value",getViLabel(refEntityID));
                if (!myRef.has(instanceName))
                {
                    JSONArray h = new JSONArray();
                    h.put(refEntityIDObject);
                    myRef.put(instanceName, h);
                }
                else
                {
                    ((JSONArray)myRef.get(instanceName)).put(refEntityIDObject);
                }
            }
        }
        json.put("references",myRef);

        return json;
    }

    public final void entityFinal() throws Exception
    {
        int cnt = 0;
        String entityFinalPath = superpath + "WikiAnalys/EntityFinal";
        allPFile = listAllFiles(entityPropertiesPath);
        for (String fileName: allPFile)
        {
            propertyEntityHashSet.add(fileName.replace(".json",""));
        }
        for (String fileName: allQFile)
        {
            cnt++;
            if (cnt%1000==0)
            {
                print(cnt);
            }
            if (fileExist(entityFinalPath + "/" + fileName))
            {
                continue;
            }
            JSONObject json = getVietnameseWikiReadable(fileName);
            String writePath = entityFinalPath + "/" + fileName;
            String writeContent = json.toString();
            writeFile(writePath, writeContent, false);

        }
    }

    HashSet<String> acceptedCountries = new HashSet<>(Arrays.asList("Việt Nam", "Đại Việt","Nam Việt", "Đại Cồ Việt", "Đại Ngu", "Xích Quỷ", "Văn Lang", "Âu Lạc", "Giao Chỉ", "Lĩnh Nam", "Giao Châu", "An Nam", "Trấn Nam", "Tĩnh Hải quân", "Đại Nam"));

    HashSet<String> bannedProperties = new HashSet<>(Arrays.asList("mã sân bay IATA", "chuyến bay vũ trụ", "Romaja quốc ngữ", "trang Commons Creator", "tập hình Commons", "có trong danh sách chú trọng của dự án Wikimedia", "thể loại ở Commons", "chuyển tự McCune–Reischauer", "thể loại chính của đề tài", "thể loại cho nhóm người", "thể loại có liên quan"));

    public final void export() throws Exception
    {
        String categoryPath = superpath + "WikiAnalys/Category";
        String exportPath = categoryPath + "/export";
        createFolder(exportPath);
        //String exportPath = "E:/Code/Java/OOP_Project/saveddata/Wikipedia/WikiAnalys/Category/New folder/export";
        JSONObject bigCategories = getJSONFromFile(categoryPath + "/Split.json");
        Iterator<String> bigCategory = ((JSONObject) bigCategories).keys();
        while (bigCategory.hasNext()) {
            String bigCate = bigCategory.next();
            createFolder(exportPath + "/" + bigCate);
        }
        
        /*
        HashSet<String> files = listAllFiles(finalEntityPath);
        for (String fileName: files)
        {
            JSONObject json = getJSONFromFile(finalEntityPath + "/" + fileName);
            if(json.has("claims"))
            {
                JSONObject claims = (JSONObject)json.get("claims");
                if (claims.has("là một"))
                {
                    JSONArray isIncstanceOf = (JSONArray)(claims.get("là một"));
                    for(Object instance: isIncstanceOf)
                    {
                        JSONObject instanceObj = (JSONObject)instance;
                        String value = (String)instanceObj.get("value");
                        if (value.equals("người"))
                        {
                            if (claims.has("quốc tịch"))
                            {
                                JSONArray quocTichs = (JSONArray)(claims.get("quốc tịch"));
                                boolean check = false;
                                for(Object quocTich: quocTichs)
                                {
                                    JSONObject quocTichObj = (JSONObject)quocTich;
                                    if (acceptedCountries.contains((String)quocTichObj.get("value")))
                                    {
                                        check = true;
                                    }
                                }
                                if (check==false)
                                {
                                    break;
                                }
                            }
                        }
                        bigCategory = ((JSONObject) bigCategories).keys();
                        while (bigCategory.hasNext()) {
                            String bigCate = bigCategory.next();
                            JSONObject subCategories = (JSONObject)bigCategories.get(bigCate);
                            if(subCategories.has(value))
                            {
                                if (bigCate.equals("triều đại lịch sử"))
                                {
                                    json.remove("references");
                                    //List vua vietnam here
                                }
                                if (bigCate.equals("lễ hội văn hóa"))
                                {
                                    if (!fileExist(exportPath + "/" + bigCate + "/" + fileName))
                                    {
                                        print(fileName);
                                        writeFile(exportPath + "/" + bigCate + "/" + fileName, json.toString(), false);
                                    }
                                }
                                    
                            }
                        }
                    }
                }
            }
        }
        */

        HashSet<String> acceptEntitySet = new HashSet<>();
        bigCategory = ((JSONObject) bigCategories).keys();
        while (bigCategory.hasNext()) {
            String bigCate = bigCategory.next();
            for (String fileName: listAllFiles(exportPath + "/" + bigCate))
            {
                acceptEntitySet.add(fileName.replace(".json", ""));
            }
        }
        bigCategory = ((JSONObject) bigCategories).keys();
        while (bigCategory.hasNext()) {
            String bigCate = bigCategory.next();
            String folderName = exportPath + "/" + bigCate;
            if (!bigCate.equals("lễ hội văn hóa")) continue;
            for (String fileName: listAllFiles(folderName))
            {
                StringBuffer filePath = new StringBuffer(folderName);
                filePath.append("/");
                filePath.append(fileName);
                JSONObject json = getJSONFromFile(filePath.toString());
                if (json.has("claims"))
                {
                    JSONObject claims = (JSONObject)json.get("claims");
                    Iterator<String> claimKeys = claims.keys();
                    List<String> deleteProperties = new ArrayList<String>();
                    while(claimKeys.hasNext())
                    {
                        String key = claimKeys.next();
                        if (bannedProperties.contains(key))
                        {
                            deleteProperties.add(key);
                            continue;
                        }
                        JSONArray jsonArr = claims.getJSONArray(key);
                        for (Object ele: jsonArr)
                        {
                            JSONObject propertyObj = (JSONObject)ele;
                            if ( ((String)propertyObj.get("type")).equals("wikibase-item") )
                            {
                                String qID = (String)propertyObj.get("id");
                                if (!acceptEntitySet.contains(qID))
                                {
                                    propertyObj.remove("id");
                                    propertyObj.put("type", "string");
                                }
                            }
                        }
                    }
                    for (String p: deleteProperties)
                    {
                        claims.remove(p);
                    }
                }
                if (json.has("references"))
                {
                    JSONObject references = (JSONObject)json.get("references");
                    Iterator<String> referenceKeys = references.keys();
                    List<String> delete = new ArrayList<String>();
                    while(referenceKeys.hasNext())
                    {
                        String key = referenceKeys.next();
                        if (bannedProperties.contains(key))
                        {
                            delete.add(key);
                            continue;
                        }
                        JSONArray refArr = references.getJSONArray(key);
                        for (Object ele: refArr)
                        {
                            JSONObject propertyObj = (JSONObject)ele;
                            if ( ((String)propertyObj.get("type")).equals("wikibase-item") )
                            {
                                String qID = (String)propertyObj.get("id");
                                if (!acceptEntitySet.contains(qID))
                                {
                                    propertyObj.remove("id");
                                    propertyObj.put("type", "string");
                                }
                            }
                        }
                        for (String p: delete)
                        {
                            references.remove(p);
                        }
                    }
                }
                writeFile(filePath.toString(), json.toString(), false);
            }
        }
    }
}
