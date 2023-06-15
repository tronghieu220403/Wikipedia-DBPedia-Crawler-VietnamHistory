import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
        //myWikiAnalys.getInvalidEntities();
        //myWikiAnalys.urlToEntities();
        //myWikiAnalys.entityRefFinal();
        //myWikiAnalys.entityFinal();
        myWikiAnalys.export();
    }

    public WikiAnalys()
    {
        createFolder(superpath + "/WikiAnalys");
        //createFolder(superpath + );
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
            String entityID = fileName.replace(".json", "");
            JSONObject entitiyContent = (JSONObject )entities.get(entityID);
            if (getSitelink(entitiyContent, entityID, "viwiki").equals(""))
            {
                moveFile(entityJsonPath + "/" + entityID + ".json", entityPropertiesPath + "/" + entityID + ".json");
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
                    moveFile(entityJsonPath + "/" + entityID + ".json", entityPropertiesPath + "/" + entityID + ".json");
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

    public final String getInstance(String entityID) throws Exception
    {
        String instance = "";
        JSONObject jsonContent;
        if (fileExist(entityJsonPath + "/" + entityID + ".json"))
            jsonContent = getJSONFromFile(entityJsonPath + "/" + entityID + ".json");
        else if (fileExist(entityPropertiesPath + "/" + entityID + ".json"))
            jsonContent = getJSONFromFile(entityPropertiesPath + "/" + entityID + ".json");
        else return instance;
        JSONObject entities  = (JSONObject )jsonContent.get("entities");
        JSONObject entitiyContent = (JSONObject )entities.get(entityID);
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

    private final String getSitelink(JSONObject entitiyContent, String entityID, String wikiLang) throws Exception
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
            String entityID = fileName.replace(".json", "");
            JSONObject entitiyID = (JSONObject )entities.get(entityID);
            JSONObject sitelinks = (JSONObject )entitiyID.get("sitelinks");
            String sitelinkVN = "";
            String sitelinkENG = "";
            if (sitelinks.has("viwiki"))
            {
                sitelinkVN = (String )(((JSONObject )(sitelinks.get("viwiki"))).get("url"));
                urlToEntitiesHashMap.put(urlDecode(sitelinkVN), entityID);
            }
            if (sitelinks.has("enwiki"))
            {
                sitelinkENG = (String )(((JSONObject )(sitelinks.get("enwiki"))).get("url"));
                urlToEntitiesHashMap.put(sitelinkENG, entityID);
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
            String entityID = fileName.replace(".json", "");
            HashSet<String> h = new HashSet<>();
            refList.putIfAbsent(entityID, h);
        }
        for (String fileName: allQRefFile)
        {
            String entityID = fileName.replace(".txt", "");
            List<String> qRef = readFileAllLine(superpath + "EntityReference/" + fileName);
            for (String urlString: qRef)
            {
                urlString = urlDecode(urlString);
                if (urlToEntitiesHashMap.containsKey(urlString))
                {
                    String entityID1 = urlToEntitiesHashMap.get(urlString);
                    if (!refList.containsKey(entityID))
                    {
                        HashSet<String> h = new HashSet<>();
                        h.add(entityID1);
                        refList.put(entityID, h);
                    }
                    else
                    {
                        refList.get(entityID).add(entityID1);
                    }
                    if (!refList.containsKey(entityID1))
                    {
                        HashSet<String> h = new HashSet<>();
                        h.add(entityID);
                        refList.put(entityID1, h);
                    }
                    else
                    {
                        refList.get(entityID1).add(entityID);
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

    public String getOverview(String entityID)
    {
        String filePath = htmlPath + "/" + entityID + ".html";
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
                    tagContent = tagContent.replaceAll("\\[.*?\\]","");
                    overviewSB.append(tagContent);
                    break;
                }
            }
            overview = overviewSB.toString();
        }
        return overview;
    }

    public final JSONObject propertyProcess(JSONObject infoObj) throws Exception
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
                String entityID = unit.replace("http://www.wikidata.org/entity/", "");
                unit = getViLabel(entityID); 
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
            JSONObject json = new JSONObject();
            JSONObject content = getJSONFromFile(entityJsonPath + "/" + fileName);
            String entityID = fileName.replace(".json", "");
            JSONObject entities = (JSONObject)content.get("entities");
            JSONObject entity = (JSONObject)entities.get(entityID);

            /*
             * Get ID of entity
             */
            json.put("id",(String)entity.get("id"));

            /*
             * Get label of entity
             */
            json.put("label", getViLabel(entityID));

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
            json.put("overview", getOverview(entityID));

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
        String exportPath = categoryPath + "/export1";
        createFolder(exportPath);
        JSONObject bigCategories = getJSONFromFile(categoryPath + "/Split.json");
        Iterator<String> bigCategory = ((JSONObject) bigCategories).keys();
        while (bigCategory.hasNext()) {
            String bigCate = bigCategory.next();
            createFolder(exportPath + "/" + bigCate);
        }
        
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
                            if (!claims.has("quốc tịch"))
                            {
                                continue;
                            }
                            else
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
                                //writeFile(exportPath + "/" + bigCate + "/" + fileName, readFileAll(finalEntityPath + "/" + fileName), false);
                            }
                        }
                    }
                }
            }
        }
    }
}