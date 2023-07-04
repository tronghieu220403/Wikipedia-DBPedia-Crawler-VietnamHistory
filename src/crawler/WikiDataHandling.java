package crawler;

import java.util.HashMap;
import java.util.HashSet;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WikiDataHandling extends DataHandling {

    public static final String VIETNAM_WORD[] = {"Vi\\u1ec7t Nam", "Vietnam","Việt Nam", "Viet Nam", "việt nam", };

    private static HashMap<String, String> viLabelHashMap = new HashMap<>();

    private final static String[] FILTER = {
        "#","T%E1%BA%ADp_tin", "B%E1%BA%A3n_m%E1%BA%ABu"
    };

    /*
     * Create a chunk of prop
     */
    public static JSONObject createPropValue(String value, String qID, String source, JSONObject qualifiers){
        JSONObject obj = new JSONObject();
        obj.put("value", value);
        if (qID != null){
            obj.put("type", "wikibase-item");
            obj.put("id", qID);
        }
        else{
            obj.put("type", "string");
        }
        if (source != null){
            obj.put("source", source);
        }
        if (qualifiers != null){
            obj.put("qualifiers", qualifiers);
        }
        return obj;
    }

    public static Element getWikiHtmlElement(String wikiPageData, String subID)
    {
        Document doc = Jsoup.parse(wikiPageData);
        
        Elements elements = doc.select("#catlinks");
        elements.remove();

        Element divTag = doc.getElementById("mw-content-text"); 
        if (divTag == null) return null;
        
        Elements tables = divTag.select("table[align=right]");
        for (Element table : tables) {
            table.remove();
        }

        Element xemThemTag = divTag.selectFirst("h2:has(span#Xem_th\\.C3\\.AAm)"); // Get the Xem thêm tag
        if (xemThemTag != null) {
            Element nextElement = xemThemTag.nextElementSibling(); // Get the next element after Xem thêm tag
            while (nextElement != null) {
                Element toRemove = nextElement; // Store the current element to remove
                nextElement = nextElement.nextElementSibling(); // Get the next element
                toRemove.remove(); // Remove the current element from the DOM
            }
        }

        Elements navboxElements = divTag.select("div.navbox"); // Get all elements with class navbox
        for (Element navboxElement : navboxElements) {
            navboxElement.remove(); // Remove each navbox element from the DOM
        }

        if (!subID.isEmpty())
        {
            divTag = divTag.getElementById(subID);
        }
        return divTag;
    }

    public static HashSet<String> getAllWikiHref(String wikiPageData, String subID, boolean getCategory) throws Exception{
        
        HashSet<String> hrefList = new HashSet<>();
        Element divTag = getWikiHtmlElement(wikiPageData, subID);

        if (divTag!=null)
        {
            for (Element aTag : divTag.select("a")) {
                String href = aTag.attr("href");
                String fullURL = "https://vi.wikipedia.org" + href;
                if (!checkURL(fullURL, getCategory)) continue;
                fullURL = urlDecode(fullURL);
                hrefList.add(fullURL);
            }
        }
        return hrefList;
    }

    public static HashSet<String> getAllWikiHref(String wikiPageData) throws Exception
    {
        return getAllWikiHref(wikiPageData, "", false);
    }

    public static JSONObject addProperties(JSONObject myJsonClaims, String propName, String value)
    {
        return addProperties(myJsonClaims, propName, value, "");
    }

    public static JSONObject addProperties(JSONObject myJsonClaims, String propName, String value, String qID)
    {
        JSONObject addObj = new JSONObject();
        addObj.put("value", value);
        if (!qID.isEmpty())
        {
            addObj.put("type", "wikibase-item");
            addObj.put("id", qID);
        }
        else{
            addObj.put("type", "string");
        }
        if (!myJsonClaims.has(propName)){
            JSONArray jsonArr = new JSONArray();
            jsonArr.put(addObj);
            myJsonClaims.put(propName, jsonArr);
        }
        else{
            JSONArray jsonArr = myJsonClaims.getJSONArray(propName);
            boolean check = false;
            for (int i = 0; i < jsonArr.length(); i++)
            {
                JSONObject obj = jsonArr.getJSONObject(i);
                if ((obj).getString("value").equals(value)){
                    check = true;
                    break;
                }
            }
            if (check == false)
            {
                jsonArr.put(addObj);
            }
        }
        return myJsonClaims;
    }

    public static boolean checkURL(String urlString, boolean getCategory) throws Exception
    {
        if (urlString == null || urlString.isEmpty()) return false;  
        if (!urlString.contains("http")) return false;  
        if (!urlString.contains("/wiki/")) return false;
        if (getCategory == true)
        {
            if (urlDecode(urlString).contains("wiki/Thể_loại:")){
                return true;
            }
        }
        for (String text : FILTER) {
            if (urlString.contains(text)) return false;
        }
        if (urlString.chars().filter(ch -> ch == ':').count() > 1) {
            return false;  
        }
        return true;  
    }

    public static JSONObject createNewEntity(JSONObject myJsonObject, String qID, String label,  String overview, String description, JSONArray aliases, JSONObject claims, JSONObject references)
    {
        myJsonObject.put("id", qID);
        myJsonObject.put("label", label);
        myJsonObject.put("overview",  overview);
        myJsonObject.put("description",  description);
        myJsonObject.put("aliases", aliases);
        myJsonObject.put("claims", claims);
        myJsonObject.put("references", references);
        return myJsonObject;
    }

    /**
     * 
     * @return
     */
    public static JSONObject createNewEntity()
    {
        return createNewEntity(new JSONObject(), "", "", "", "", new JSONArray(), new JSONObject(), new JSONObject());
    }

    /**
     * Get the label of entity
     */
    public static String getWikiEntityViLabel(String qID, String jsonPath1, String jsonPath2) throws Exception
    {
        if (viLabelHashMap.containsKey(qID)) {
            return viLabelHashMap.get(qID);
        }
        String viLabelValue = "";
        JSONObject jsonContent;
        if (fileExist(jsonPath1 + "/" + qID + ".json"))
            jsonContent = getJSONFromFile(jsonPath1 + "/" + qID + ".json");
        else if (fileExist(jsonPath2 + "/" + qID + ".json"))
            jsonContent = getJSONFromFile(jsonPath2 + "/" + qID + ".json");
        else return viLabelValue;
        return getWikiEntityViLabel(jsonContent, qID);
    }

    /**
     * Get the label of entity
     */
    public static String getWikiEntityViLabel(JSONObject jsonContent, String qID) throws Exception
    {
        if (viLabelHashMap.containsKey(qID)) {
            return viLabelHashMap.get(qID);
        }
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
    public static void jsonGetPropertiesFromEntity(Object entityJSON, HashSet<String> entityJSONFileList, HashSet<String> propertyHashSet)
    {
        if (entityJSON instanceof JSONArray) {
            for (int i = 0; i < ((JSONArray) entityJSON).length(); i++) { 
                jsonGetPropertiesFromEntity(((JSONArray) entityJSON).get(i), entityJSONFileList, propertyHashSet);
            }
        }
        else if (entityJSON instanceof JSONObject) {
            JSONObject qJSON = (JSONObject) entityJSON;
            if (qJSON.has("datavalue") && qJSON.has("property") && qJSON.has("datatype")) {
                propertyHashSet.add(qJSON.getString("property"));
                String datatype = (String)qJSON.getString("datatype");
                if (!datatype.equals("wikibase-item") && !datatype.equals("wikibase-property")) {
                    return;
                }
                JSONObject datavalue = qJSON.getJSONObject("datavalue");
                JSONObject value = datavalue.getJSONObject("value");
                String id = value.getString("id");
                if (!entityJSONFileList.contains(id + ".json")) {
                    propertyHashSet.add(id);
                }
                return;
            }
            for (String key: getAllKeys(qJSON)) {
                Object value = qJSON.get(key);
                
                if (value instanceof JSONObject) {
                    jsonGetPropertiesFromEntity((JSONObject) value, entityJSONFileList, propertyHashSet);
                } else if (value instanceof JSONArray) {
                    jsonGetPropertiesFromEntity((JSONArray) value, entityJSONFileList, propertyHashSet);
                }
            }
        }
    }

    public static final JSONObject propertyCompression(JSONObject infoObj, HashSet<String> allQFile, HashSet<String> allPFile, String jsonPath1, String jsonPath2) throws Exception
    {
        JSONObject jsonObj = new JSONObject();
        String datatype = infoObj.getString("datatype");
        if (!infoObj.has("datavalue")){
            return jsonObj;
        }
        JSONObject datavalue = infoObj.getJSONObject("datavalue");
        if (datatype.equals("wikibase-item") || datatype.equals("wikibase-property")){
            String qID = datavalue.getJSONObject("value").getString("id");
            if (allQFile.contains(qID + ".json")){
                jsonObj = WikiDataHandling.createPropValue(getWikiEntityViLabel(qID, jsonPath1, jsonPath2), qID, null, null);
            }
            else if (allPFile.contains(qID + ".json")){
                String viLabel = getWikiEntityViLabel(qID, jsonPath1, jsonPath2);
                if (!viLabel.isEmpty()){
                    jsonObj = WikiDataHandling.createPropValue(getWikiEntityViLabel(qID, jsonPath1, jsonPath2), null, null, null);
                }
            }
        }
        else if (datatype.equals("quantity")){
            JSONObject value = datavalue.getJSONObject("value");
            String amount = value.getString("amount").replace("+", "");
            String unit = value.getString("unit");
            unit = unit.equals("1") ? " " : getWikiEntityViLabel(unit.replace("http://www.wikidata.org/entity/", ""), jsonPath1, jsonPath2);
            jsonObj = WikiDataHandling.createPropValue(amount + " " + unit, null, null, null);
        }
        else if (datatype.equals("string")){
            jsonObj = WikiDataHandling.createPropValue(datavalue.getString("value"), null, null, null);
        }
        else if (datatype.equals("monolingualtext"))
        {
            JSONObject value = datavalue.getJSONObject("value");
            if (value.getString("language").equals("vi")){
                jsonObj = WikiDataHandling.createPropValue(value.getString("text"), null, null, null);
            }
        }
        else if (datatype.equals("time"))
        {
            JSONObject value = datavalue.getJSONObject("value");
            String time = value.getString("time");
            String year = time.substring(0, 5);
            String formatDMY = "";
            if (!year.contains("0000")) {
                String month = time.substring(6, 8);
                if (!month.contains("00") && ! month.equals("01")) {
                    String day = time.substring(9, 11);
                    if (!day.equals("00") && ! day.equals("01")) {
                        formatDMY = "ngày " + day + " ";
                    }
                    formatDMY += "tháng " + month + " ";
                }
                formatDMY += "năm " + year.substring(1, 5);
                if (year.contains("-")) {
                    formatDMY += " trước công nguyên";
                }
            }
            jsonObj = WikiDataHandling.createPropValue(formatDMY, null, null, null);
        }
        return jsonObj;
    }

    public static final String getWikiEntityInstance(JSONObject entitiyContent)
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

    public static final String getWikiSitelink(JSONObject entitiyContent, String qID, String wikiLang) throws Exception
    {
        JSONObject sitelinks = (JSONObject )entitiyContent.get("sitelinks");
        String sitelink = "";
        if (sitelinks.has(wikiLang)) {
            sitelink = sitelinks.getJSONObject(wikiLang).getString("url");
        }
        return sitelink;
    }


    public static final int getMinYear(Object entityJSON)
    {
        int minYear = 100000;
        if (entityJSON instanceof JSONArray) {
            for (int i = 0; i < ((JSONArray) entityJSON).length(); i++) { 
                minYear = Math.min(getMinYear(((JSONArray)entityJSON).get(i)), minYear);
                if (minYear < 1962) {
                    return minYear;
                }
            }
        }
        else if (entityJSON instanceof JSONObject) {
            JSONObject json = (JSONObject) entityJSON;
            if (json.has("datatype")) {
                if ((json.getString("datatype")).equals("time")) {
                    if (!json.has("datavalue"))
                        return minYear;
                    JSONObject datavalue = json.getJSONObject("datavalue");
                    if (datavalue.has("value")) {
                        String time = datavalue.getJSONObject("value").getString("time");
                        String sign = time.substring(0,1);
                        if (sign.equals("-")) {
                            minYear = 0; 
                        }
                        else minYear = Integer.parseInt(time.substring(1,5));
                    }
                }
                return minYear;
            }
            for (String key: getAllKeys(json)){
                if (key.equals("references")){
                    continue;
                }
                Object value = json.get(key);
                if (value instanceof JSONObject) {
                    minYear = Math.min(getMinYear((JSONObject) value), minYear);
                } else if (value instanceof JSONArray) {
                    minYear = Math.min(getMinYear((JSONArray) value), minYear);
                }
                if (minYear < 1962) {
                    return minYear;
                }
            }
        }
        return minYear;
    }

}
