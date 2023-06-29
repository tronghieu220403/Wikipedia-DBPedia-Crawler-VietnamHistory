/**
 *  The "WikiData" class provides useful methods for analyzing Wikipedia pages and extracting relevant information related to entities in Vietnam.
 */
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
        wikiData.setAnalyseLitmit(0);
        wikiData.getData();
        wikiData.getDataCallBack();
    }

    private HashMap<String, String> urlToEntityHashMap = new HashMap<>();

    private final String ENTITY_PROPERTIES_PATH = LOGS_PATH + "EntityProperties/";
    private final String HTML_PATH = LOGS_PATH + "WebHtml/";
    private final String SCARLARLY_PATH = LOGS_PATH + "Scarlarly/";

    private final String ENTITY_REFERENCE_PATH = LOGS_PATH + "EntityReference/";;
    private final String EVENT_PATH = DATA_PATH + "sự kiện lịch sử";
    private final String PLACE_PATH = DATA_PATH + "địa điểm du lịch, di tích lịch sử/";
    private final String HUMAN_PATH = DATA_PATH + "nhân vật lịch sử/";
    private final String DYNASTY_PATH = DATA_PATH + "triều đại lịch sử/";
    private final String FESTIVAL_PATH = DATA_PATH + "lễ hội văn hóa/";

    private final String ENTITY_FINAL_PATH = LOGS_PATH + "EntityFinal/";
    private final String ENTITY_REF_FINAL_PATH = LOGS_PATH + "EntityRefFinal/";

    private HashSet<String> allQFile = listAllFiles(ENTITY_JSON_PATH);
    private HashSet<String> allPFile = listAllFiles(ENTITY_PROPERTIES_PATH);

    private HashSet<String> festivalHashSet = new HashSet<>();
    private HashSet<String> locationHashSet = new HashSet<>();
    private HashSet<String> humanHashSet = new HashSet<>();

    public WikiData()
    {
        throw new IllegalArgumentException("File path must be provided.");
    }

    public WikiData(String folderPath) throws Exception
    {
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
        selectiveDataQueries();
        analyzeBruteForceData();
        analyzeSelectiveData();
        tableDataQueries();
        //export();
        return;
    }

    private void selectiveDataQueries() throws Exception{
        selectiveFestivalsQueries(); // done;
        selectiveHumansQueries();  // done;
        selectiveLocationsQueries(); // done;
    }

    private void analyzeBruteForceData() throws Exception{
        urlToEntities();
        getProperties();
        entityRefFinal();
        entityFinal();
    }

    private void analyzeSelectiveData()throws Exception{
        analyzeSelectiveFestivalData();
        analyzeSelectiveHumanData();
        analyzeSelectiveLocationData();
    }

    private void tableDataQueries() throws Exception{
        tableDynastiesQueries();
        //tableFestivalsQueries();
        tableLocationsQueries();
    }

    private void getAllURL(String catString, int floor, boolean getCat, HashSet<String> urlSet) throws Exception
    {
        if (floor >= 4) return;
        String wikiPageData = "";
        while(true)
        {
            wikiPageData = getDataFromURL(catString).toString();
            for (String craftURL: getAllHref(wikiPageData,"mw-pages",false))
            {
                urlSet.add(craftURL);
            }
            Element divTag = getWikiHtmlElement(wikiPageData, "mw-pages");
            String nextPageUrl = "";
            if (divTag!=null)
            {                
                for (Element aTag : divTag.select("a")) {
                    if (!aTag.text().equals("Trang sau")) continue;
                    //print(aTag.attr("href"));
                    nextPageUrl = ("https://vi.wikipedia.org" + aTag.attr("href"));
                    break;
                }
            }
            if (nextPageUrl.isEmpty())
                break;
            else catString = nextPageUrl;
        }
        if (getCat == true)
        {
            for (String craftURL: getAllHref(wikiPageData, "mw-subcategories", true)){
                getAllURL(craftURL, floor + 1, getCat, urlSet);
            }
        }
    }

    private void analyzeScarlarlyURLs(HashSet<String> urlSet, HashSet<String> categoryHashSet) throws Exception
    {
        List<String> removeURLs = new ArrayList<>();
        for (String urlString: urlSet)
        {
            String qID = urlToEntityHashMap.get(urlString);
            if (qID!=null)
            {
                //print(qID);
                categoryHashSet.add(qID);
                continue;
            }
            entityAnalys(urlString, 3, true);
            qID = urlToEntityHashMap.get(urlString);
            if (qID!=null)
            {
                //print(qID);
                categoryHashSet.add(qID);
            }
            else{
                removeURLs.add(urlString);
            }
        }
        for (String urlString: removeURLs){
            urlSet.remove(urlString);
        }
    }

    private void selectiveFestivalsQueries() throws Exception
    {
        HashSet<String> urlSet = new HashSet<>();
        if (!fileExist(SCARLARLY_PATH + "festivals.json"))
        {    
            Set<String> bannedFestivalURLs = new HashSet<>(Arrays.asList("https://vi.wikipedia.org/wiki/Lễ_hội_các_dân_tộc_Việt_Nam",
                "https://vi.wikipedia.org/wiki/Lễ_hội_Lào",
                "https://vi.wikipedia.org/wiki/Lễ_hội_Nhật_Bản",
                "https://vi.wikipedia.org/wiki/Lễ_hội_Thái_Lan", "https://vi.wikipedia.org/wiki/Lễ_hội", "https://vi.wikipedia.org/wiki/Lễ_hội_Việt_Nam"));
            String urls[] = {"https://vi.wikipedia.org/wiki/L%E1%BB%85_h%E1%BB%99i_c%C3%A1c_d%C3%A2n_t%E1%BB%99c_Vi%E1%BB%87t_Nam", "https://vi.wikipedia.org/wiki/L%E1%BB%85_h%E1%BB%99i_Vi%E1%BB%87t_Nam"};
            urlSet.add("https://vi.wikipedia.org/wiki/Giỗ_Tổ_Hùng_Vương");

            urlToEntityHashMap.forEach((key, value) -> {
                String urlString = urlDecode(key);
                if ((urlString.contains("/Lễ"))  && !bannedFestivalURLs.contains(urlString)){
                    festivalHashSet.add(value);
                    urlSet.add(urlString);
                }
            });
            for (String urlString: urls)
            {
                String wikiPageData = getDataFromURL(urlString).toString();

                for (String craftURL: getAllHref(wikiPageData)){
                    if ((craftURL.contains("Lễ_hội") || craftURL.contains("Hội")) && !bannedFestivalURLs.contains(craftURL)){
                        if (!urlSet.contains(craftURL)) {
                            urlSet.add(craftURL);
                        }
                    }
                }
            }

        }
        else{
            for (Object key: new JSONArray(readFileAll(SCARLARLY_PATH + "festivals.json")))
            {
                urlSet.add((String)key);
            }
        }

        analyzeScarlarlyURLs(urlSet, festivalHashSet);

        writeFile(SCARLARLY_PATH + "festivals.json", (new JSONArray(urlSet)).toString(), false);
        writeFile(LOGS_PATH +  "URLToEntities.json" , (new JSONObject(urlToEntityHashMap)).toString(), false);
    }


    private void selectiveHumansQueries() throws Exception {
        HashSet<String> urlSet = new HashSet<>();
        if (!fileExist(SCARLARLY_PATH + "humans.json"))
        {
            getAllURL("https://vi.wikipedia.org/wiki/Th%E1%BB%83_lo%E1%BA%A1i:Nh%C3%A0_c%C3%A1ch_m%E1%BA%A1ng_Vi%E1%BB%87t_Nam", 0 , false, urlSet);
            getAllURL("https://vi.wikipedia.org/wiki/Th%E1%BB%83_lo%E1%BA%A1i:Nh%C3%A2n_v%E1%BA%ADt_l%E1%BB%8Bch_s%E1%BB%AD_Vi%E1%BB%87t_Nam", 0 , true, urlSet);
        }
        else{
            for (Object key: new JSONArray(readFileAll(SCARLARLY_PATH + "humans.json")))
            {
                urlSet.add((String)key);
            }
        }

        analyzeScarlarlyURLs(urlSet, humanHashSet);

        writeFile(SCARLARLY_PATH + "humans.json", (new JSONArray(urlSet)).toString(), false);
        writeFile(LOGS_PATH +  "URLToEntities.json" , (new JSONObject(urlToEntityHashMap)).toString(), false);
    }


    private void selectiveLocationsQueries() throws Exception
    {
        HashSet<String> urlSet = new HashSet<>();
        if (!fileExist(SCARLARLY_PATH + "locations.json"))
        {
            String urlCat[] = {"https://vi.wikipedia.org/wiki/Thể_loại:Khu_bảo_tồn_Việt_Nam", "https://vi.wikipedia.org/wiki/Thể_loại:Di_tích_tại_Hà_Nội", "https://vi.wikipedia.org/wiki/Thể_loại:Di_tích_quốc_gia_đặc_biệt", "https://vi.wikipedia.org/wiki/Thể_loại:Di_tích_tại_Hà_Nội","https://vi.wikipedia.org/wiki/Thể_loại:Chùa_Việt_Nam_theo_tỉnh_thành","https://vi.wikipedia.org/wiki/Thể_loại:Di_tích_quốc_gia_Việt_Nam"};
            for (String catString: urlCat){
                getAllURL(catString, 0, true, urlSet);
            }
        }
        else{
            for (Object key: new JSONArray(readFileAll(SCARLARLY_PATH + "locations.json")))
            {
                urlSet.add((String)key);
            }
        }
        analyzeScarlarlyURLs(urlSet, locationHashSet);

        JSONArray allLocationsArr = new JSONArray(readFileAll(INITIALIZE_PATH + "HistoricalSite.json"));
        for (int i = 0; i < allLocationsArr.length(); i++)
        {
            JSONObject locationJSON = allLocationsArr.getJSONObject(i);
            String urlString = urlDecode(locationJSON.getString("link"));
            String qID = "";
            if (checkURL(urlString)){
                if (!urlToEntityHashMap.containsKey(urlString)){
                    entityAnalys(urlString, 3, true);
                    qID = urlToEntityHashMap.get(urlString);
                    if (qID != null){
                        urlSet.add(urlString);
                        locationHashSet.add(qID);
                    }
                }
            }
        }

        writeFile(SCARLARLY_PATH + "locations.json", (new JSONArray(urlSet)).toString(), false);
        writeFile(LOGS_PATH +  "URLToEntities.json" , (new JSONObject(urlToEntityHashMap)).toString(), false);
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
            JSONObject content = getJSONFromFile(ENTITY_JSON_PATH + fileName);
            JSONObject entities  = content.getJSONObject("entities");
            String qID = fileName.replace(".json", "");
            JSONObject entitiyID = entities.getJSONObject(qID);
            JSONObject sitelinks = entitiyID.getJSONObject("sitelinks");
            String sitelinkVN = "";
            String sitelinkENG = "";
            if (sitelinks.has("viwiki"))
            {
                sitelinkVN = sitelinks.getJSONObject("viwiki").getString("url");
                urlToEntityHashMap.put(urlDecode(sitelinkVN), qID);
            }
            if (sitelinks.has("enwiki"))
            {
                sitelinkENG = sitelinks.getJSONObject("enwiki").getString("url");
                urlToEntityHashMap.put(sitelinkENG, qID);
            }
        }
        writeFile(LOGS_PATH +  "URLToEntities.json" , (new JSONObject(urlToEntityHashMap)).toString(), false);
    }

    private Element getWikiHtmlElement(String wikiPageData, String subID)
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

    private HashSet<String> getAllHref(String wikiPageData, String subID, boolean getCategory) throws Exception{
        
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

    private HashSet<String> getAllHref(String wikiPageData) throws Exception
    {
        return getAllHref(wikiPageData, "", false);
    }


    private JSONObject addProperties(JSONObject myJsonClaims, String propName, String value)
    {
        return addProperties(myJsonClaims, propName, value, "");
    }

    private JSONObject addProperties(JSONObject myJsonClaims, String propName, String value, String qID)
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


    private void analyzeSelectiveFestivalData() throws Exception
    {
        for (String qID: festivalHashSet)
        {
            JSONObject json = getVietnameseWikiReadable(qID);
            JSONObject claims = json.getJSONObject("claims");
            addProperties(claims, "là một", "lễ hội");
            addProperties(claims, "quốc gia", "Việt Nam");
            writeFile(ENTITY_FINAL_PATH + qID + ".json", json.toString(), false);
        }
    }

    private void analyzeSelectiveHumanData() throws Exception {
        for (String qID: humanHashSet)
        {
            JSONObject json = getVietnameseWikiReadable(qID);
            JSONObject claims = json.getJSONObject("claims");
            addProperties(claims, "là một", "người");
            addProperties(claims, "quốc tịch", "Việt Nam");
            writeFile(ENTITY_FINAL_PATH + qID + ".json", json.toString(), false);
        }
    }

    private void analyzeSelectiveLocationData() throws Exception {
        for (String qID: locationHashSet)
        {
            JSONObject json = getVietnameseWikiReadable(qID);
            JSONObject claims = json.getJSONObject("claims");
            addProperties(claims, "là một", "địa điểm");
            addProperties(claims, "quốc gia", "Việt Nam");
            writeFile(ENTITY_FINAL_PATH + qID + ".json", json.toString(), false);
        }
    }


    private void tableDynastiesQueries() throws Exception
    {
        JSONObject allDynastyJsonObject = getJSONFromFile(INITIALIZE_PATH + "VVN.json");
        HashMap<String, String> dynastyHashMap = new HashMap<>();
        for (String fileName: listAllFiles(ENTITY_FINAL_PATH))
        {
            JSONObject json = getJSONFromFile(ENTITY_FINAL_PATH + fileName);
            if (json.getJSONObject("claims").has("là một")){
                JSONArray jsonArr = json.getJSONObject("claims").getJSONArray("là một");
                for (int i = 0; i < jsonArr.length(); i++)
                {
                    if (jsonArr.getJSONObject(i).getString("value").equals("triều đại"))
                    {
                        String dynastyName = json.getString("label");
                        dynastyHashMap.put(dynastyName, json.getString("id"));
                        break;
                    }
                }
            }
        }

        String[] kingProp = {"Miếu hiệu", "Tôn hiệu hoặc Thụy hiệu", "Tôn hiệu", "Niên hiệu", "Thế thứ", "Trị vì"};
        for (String dynastyName: getAllKeys(allDynastyJsonObject))
        {
            JSONObject dynastyJsonObject = new JSONObject();
            if (!dynastyHashMap.containsKey(dynastyName))
            {
                JSONObject claims = new JSONObject();
                String qID = "Q" + Integer.toString(dynastyName.hashCode()).replace("-", "") + "X";
                addProperties(claims, "quốc gia", "Việt Nam");
                addProperties(claims, "là một", "triều đại");
                createNewObject(dynastyJsonObject, 
                    qID, 
                    dynastyName, 
                    dynastyName + " là một triều đại phong kiến trong lịch sử Việt Nam.", 
                    "", 
                    new JSONArray(), 
                    claims, 
                    new JSONObject()
                );
                dynastyHashMap.put(dynastyName, qID);
            }
            else
            {
                try{
                    dynastyJsonObject = getJSONFromFile(ENTITY_FINAL_PATH + dynastyHashMap.get(dynastyName) + ".json");
                }
                catch (Exception e) {
                    System.out.println("[ERROR] Can't find file: data/triều đại lịch sử/" + dynastyHashMap.get(dynastyName));
                }
            }

            String dynastyQID = dynastyHashMap.get(dynastyName);
            JSONArray dynastyRefArr = new JSONArray();
            JSONArray kingArr = allDynastyJsonObject.getJSONArray(dynastyName);
            for (int i = 0; i < kingArr.length(); i++)
            {
                String kingQID = "";
                JSONObject king = kingArr.getJSONObject(i);
                String kingURL = urlDecode(king.getString("link"));
                
                JSONObject kingJsonObject = new JSONObject();
                JSONObject kingClaims = new JSONObject();

                String kingName = "";
                if (urlToEntityHashMap.containsKey(kingURL))
                {
                    kingQID = urlToEntityHashMap.get(kingURL);
                    kingJsonObject = getJSONFromFile(ENTITY_FINAL_PATH + kingQID + ".json");
                    kingClaims = kingJsonObject.getJSONObject("claims");
                    kingName = kingJsonObject.getString("label");
                }
                else
                {
                    String[] kingType = {"Vua", "Tước hiệu", "Thủ lĩnh", "Tiết độ sứ"};
                    for (int j = 0; j < kingType.length; j++) {
                        if (king.has(kingType[j])) {
                            kingName = king.getString(kingType[j]);
                            break;
                        }
                    }
                    kingQID = "Q" + Integer.toString(kingName.hashCode()).replace("-", "") + "X";
                }

                addProperties(kingClaims, "là một", "người");
                addProperties(kingClaims, "là một", "vua");
                addProperties(kingClaims, "quốc tịch", "Việt Nam");

                for (String prop: kingProp)
                {
                    if (!king.has(prop)) continue;
                    String value = king.getString(prop);
                    if (value.isEmpty()) continue;
                    addProperties(kingClaims, prop.toLowerCase(), value);
                }

                if (kingQID.contains("X"))
                {
                    createNewObject(kingJsonObject, 
                        kingQID, 
                        kingName, 
                        kingName + " là một vị vua trong lịch sử Việt Nam.", 
                        "", 
                        new JSONArray(), 
                        kingClaims,
                        new JSONObject()
                    );
                    urlToEntityHashMap.put(kingURL, kingQID);
                }
                JSONObject refJSONObj = new JSONObject();
                refJSONObj.put("type", "wikibase-item");
                refJSONObj.put("value", kingName);
                refJSONObj.put("id", kingQID);
                dynastyRefArr.put(refJSONObj);

                JSONObject kingRefJsonObject = new JSONObject();
                if (kingJsonObject.has("references"))
                    kingRefJsonObject = kingJsonObject.getJSONObject("references");

                addProperties(kingRefJsonObject, "triều đại", dynastyName, dynastyQID);

                kingJsonObject.put("references", kingRefJsonObject);
                writeFile(ENTITY_FINAL_PATH + kingQID + ".json", kingJsonObject.toString(), false);
            }
            dynastyJsonObject.getJSONObject("references").put("vua", dynastyRefArr);
            writeFile(ENTITY_FINAL_PATH + dynastyQID + ".json", dynastyJsonObject.toString(), false);
        }
        
    }

    private JSONObject createNewObject(JSONObject myJsonObject)
    {
        return createNewObject(myJsonObject, "", "", "", "", new JSONArray(), new JSONObject(), new JSONObject());
    }

    private JSONObject createNewObject(JSONObject myJsonObject, String qID, String label,  String overview, String description, JSONArray aliases, JSONObject claims, JSONObject references)
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

    private void tableLocationsQueries() throws Exception {
        JSONArray allLocationsArr = new JSONArray(readFileAll(INITIALIZE_PATH + "HistoricalSite.json"));
        for (int i = 0; i < allLocationsArr.length(); i++)
        {
            JSONObject locationJSON = allLocationsArr.getJSONObject(i);
            String urlString = urlDecode(locationJSON.getString("link"));
            JSONObject json = new JSONObject();
            String qID = "";
            if (checkURL(urlString)){
                if (!urlToEntityHashMap.containsKey(urlString)){
                    entityAnalys(urlString, 3, true);
                    qID = urlToEntityHashMap.get(urlString);
                    if (qID != null){
                        json = getVietnameseWikiReadable(qID);
                    }
                    else{
                        createNewObject(json);
                    }
                }
                else{
                    qID = urlToEntityHashMap.get(urlString);
                    if (fileExist(ENTITY_FINAL_PATH + qID + ".json"))
                    {
                        json = getJSONFromFile(ENTITY_FINAL_PATH + qID + ".json");
                    }
                    else{
                        json = getVietnameseWikiReadable(qID);
                    }
                }
            }
            else{
                createNewObject(json);
            }
            String locationName = locationJSON.getString("Di tích");

            if (locationName.isEmpty())
                continue;
            if (json.getString("label").isEmpty()){
                json.put("label", locationName);
            }
            String locationType = "";
            if (locationJSON.has("Loại di tích")){
                locationType = locationJSON.getString("Loại di tích").toLowerCase();
            }

            if (json.getString("overview").isEmpty()){
                String txt = "";
                if (!locationType.isEmpty()) txt = locationType + " ";
                json.put("overview", locationName + " là một di tích " + txt + "tại Việt Nam.");
            }
            if (json.getString("id").isEmpty()){
                qID = "Q" + Integer.toString(locationName.hashCode()).replace("-", "") + "X";
                json.put("id", qID);
            }
            JSONObject claims = json.getJSONObject("claims");
            if (!locationType.isEmpty())
                addProperties(claims, "loại di tích", locationType);
            addProperties(claims, "là một", "di tích");
            addProperties(claims, "quốc gia", "Việt Nam");
            if (locationJSON.has("Vị trí")){
                if (!locationJSON.getString("Vị trí").isEmpty()){
                    addProperties(claims, "vị trí", locationJSON.getString("Vị trí"));
                }
            }  
            if (locationJSON.has("Năm CN") && !locationJSON.getString("Năm CN").isEmpty()){
                String date = locationJSON.getString("Năm CN");
                if (date.contains("/"))
                {
                    date = date.replaceFirst("/", " tháng ");
                    date = date.replaceFirst("/", " năm ");
                    date = "ngày " + date;
                }
                else date = "năm " + date;
                addProperties(claims, "thời gian công nhận di tích", date);
            }
            writeFile(ENTITY_FINAL_PATH + qID + ".json", json.toString(), false);
        }
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



    public final void entityFinal() throws Exception
    {
        //HashSet<String> allPFile = listAllFiles(ENTITY_PROPERTIES_PATH);
        allQFile = listAllFiles(ENTITY_JSON_PATH);
        allPFile = listAllFiles(ENTITY_PROPERTIES_PATH);

        /*
        HashSet<String> propertyEntityHashSet = new HashSet<>();
        for (String fileName: allPFile)
        {
            propertyEntityHashSet.add(fileName.replace(".json",""));
        }
        */
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


    private JSONObject getVietnameseWikiReadable(String qID) throws Exception
    {
        String fileName = qID + ".json";
        JSONObject json = new JSONObject();

        JSONObject content = getJSONFromFile(ENTITY_JSON_PATH + fileName);
        JSONObject entities = content.getJSONObject("entities");
        JSONObject entity = entities.getJSONObject(qID);

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
        JSONObject descriptions = entity.getJSONObject("descriptions");
        if (descriptions.has("vi"))
        {
            JSONObject viDescriptions = descriptions.getJSONObject("vi");
            viDescriptionValue = viDescriptions.getString("value");
            json.put("description", viDescriptionValue);
        }

        /*
        * Get overview of entity
        */
        json.put("overview", getOverview(qID));

        /*
        * Get aliases of entity
        */
        JSONObject aliases = entity.getJSONObject("aliases");
        ArrayList<String> myAliases = new ArrayList<>();
        if (aliases.has("vi"))
        {
            JSONArray viAlias = aliases.getJSONArray("vi");
            for (int i = 0 ; i < viAlias.length() ; i++)
            {
                String viAliasValue = viAlias.getJSONObject(i).getString("value");
                myAliases.add(viAliasValue);
            }
        }
        json.put("aliases", new JSONArray(myAliases));

        /*
        * Get claims of entity
        */
        JSONObject myClaims = new JSONObject();
        JSONObject claims = entity.getJSONObject("claims");
        for (String propertyID: getAllKeys(claims))
        {
            /* Choose that entity if that entity has a name in Vietnamese */
            String propertyName = getViLabel(propertyID);
            if (propertyName.isEmpty())
                continue;
            
            JSONArray propertyInfoArr = claims.getJSONArray(propertyID);
            JSONArray jsonArray = new JSONArray();
            for (Object info: propertyInfoArr)
            {
                JSONObject infoObj = (JSONObject) info;
                JSONObject mainsnak = infoObj.getJSONObject("mainsnak");
                JSONObject jsonObj = propertyProcess(mainsnak);

                if (jsonObj.length() == 0) continue;
                
                /*
                * Get qualifiers of a property (a qualifier field will describe a property more clear)
                */
                if (infoObj.has("qualifiers"))
                {
                    JSONObject qualifiersJsonObj = new JSONObject();
                    JSONObject qualifiers = infoObj.getJSONObject("qualifiers");
                    for (String key: getAllKeys(qualifiers))
                    {
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
        if (fileExist(ENTITY_REF_FINAL_PATH + fileName))
        {
            HashMap<String, String> idInstance = new HashMap<>();
            String fileContent = readFileAll(ENTITY_REF_FINAL_PATH + fileName);
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

    public final String getInstance(String qID) throws Exception
    {
        String instance = "";
        JSONObject json;
        if (fileExist(ENTITY_JSON_PATH + qID + ".json"))
            json = getJSONFromFile(ENTITY_JSON_PATH + qID + ".json");
        else if (fileExist(ENTITY_PROPERTIES_PATH + qID + ".json"))
            json = getJSONFromFile(ENTITY_PROPERTIES_PATH + qID + ".json");
        else return instance;
        JSONObject claims = json.getJSONObject("entities").getJSONObject(qID).getJSONObject("claims");
        if (!claims.has("P31")){
            return instance;
        }
        JSONArray p31Arr = (JSONArray)(claims.get("P31"));
        for (int i = 0 ; i < p31Arr.length() ; i++)
        {
            JSONObject mainsnak = p31Arr.getJSONObject(i).getJSONObject("mainsnak");
            if (mainsnak.has("datavalue"))
            {
                JSONObject datavalue = mainsnak.getJSONObject("datavalue");
                JSONObject value = datavalue.getJSONObject("value");
                instance = value.getString("id");
                if (!instance.equals("")) 
                    break;
            }
        }
        return instance;
    }


    public String getOverview(String qID)
    {
        String filePath = HTML_PATH + qID + ".html";
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
                    String regex = "\\s*\\[[^\\]]*\\]\\s*";
                    if (tagContent.matches(regex)){
                        tagContent = tagContent.replaceAll(regex, "");
                    }
                    overviewSB.append(tagContent);
                    break;
                }
            }
            if (overviewSB.length() > 0 && overviewSB.charAt(overviewSB.length()-1) == (char)(':'))
            {
                int dot = overviewSB.lastIndexOf(".", overviewSB.length()-1);
                overview = overviewSB.substring(0, dot + 1);
            }
            else{
                overview = overviewSB.toString();
            }
        }
        return overview;
    }


    @Override
    public void getVietnamRelatedEntity() throws Exception{

        if (!fileExist(INITIALIZE_PATH + "FromVietnam.json")){
            throw new FileNotFoundException("Please create file FromVietnam.json that contains entities related to Vietnam in this folder: " + INITIALIZE_PATH);
        }
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
     * @apiNote The HTML content is in "logs/WebHtml" folder. 
     * @apiNote The entity data is in "logs/EntityJson" folder. 
     * @apiNote The related URLs is in "logs/EntityReference" folder. 
     */
    @Override
    protected void entityAnalys(String urlString, int depth, boolean forceRelated) throws Exception {
        
        // Check if urlString is a valid Wikipedia URL .
        urlString = urlString.replace("\n", "");
        if (checkURL(urlString) == false){
            return;
        }
        urlString = urlDecode(urlString);
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
            qID = getEntityID(doc);
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
            for (String craftURL: getAllHref(wikiPageData)){
                s.append(craftURL + '\n');
                addToCrafedURL(craftURL, depth);
            }
            writeFile(ENTITY_REFERENCE_PATH + qID + ".txt", s.toString(), true);
            addToAnalysedURL(urlString);
        }
        return;
    }

    private final String[] FILTER = {
        "#","T%E1%BA%ADp_tin", "B%E1%BA%A3n_m%E1%BA%ABu"
    };
    
    @Override
    public boolean checkURL(String urlString) throws Exception {        
        return checkURL(urlString, false);  
    }

    public boolean checkURL(String urlString, boolean getCategory) throws Exception
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

    /**
     * Check if the JSON of an entity has any properties that are related to Vietnam.
     * @param entityJSON the JSON of an entity.
     * @return Member variable isRelated is {@code true} if that entity has any properties that are related to Vietnam, else {@code false}.
     */
    private boolean jsonAnalysis(Object entityJSON)
    {
        if (entityJSON instanceof JSONArray)
        {
            for (int i = 0; i < ((JSONArray) entityJSON).length(); i++) { 
                if (jsonAnalysis(((JSONArray) entityJSON).get(i)) == true) {
                    return true;
                }
            }
        }
        else if (entityJSON instanceof JSONObject)
        {
            JSONObject qJSON = (JSONObject) entityJSON;
            if (qJSON.has("numeric-id"))
            {
                if (vietnamEntityHashSet.contains(qJSON.get("id"))) {
                    return true;
                }
            }
            for(String key: getAllKeys(qJSON)){
                Object value = qJSON.get(key);
                if (value instanceof JSONObject) {
                    if (jsonAnalysis((JSONObject) value) == true){
                        return true;
                    }
                } else if (value instanceof JSONArray) {
                    if (jsonAnalysis((JSONArray) value) == true){
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

    public boolean checkRelated(String qID, String wikiPageData, boolean forceRelated) throws Exception {
        
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
        if (forceRelated == false){
            if (content.isEmpty())
                return false;
            JSONObject json = new JSONObject(content);
        
            if (jsonAnalysis(json) == false)
                return false;
            if (getViLabel(json, qID).isEmpty())
                return false;
            
            boolean check = false;
            for (String vnWord: VIETNAM_WORD) {
                if (content.contains(vnWord)){
                    check = true;
                    break;
                }
            }
            if (check == false) return false;

            JSONObject entities  = json.getJSONObject("entities");
            JSONObject entitiyJson = entities.getJSONObject(qID);
            // If an entity has no sitelinks to Wikipedia then that entity is virtual. We will put it into the ENTITY_PROPERTIES_PATH
            if (getSitelink(entitiyJson, qID, "viwiki").equals("")) {
                writeFile(ENTITY_PROPERTIES_PATH + qID +".json", content , false);
                return false;
            }
            
            // If an entity is a human (Q5) and there exist at least one year, it must be less than 1962.
            if (getInstance(entitiyJson).equals("Q5")){
                int entityMinYear = getMinYear(entitiyJson);
                if (entityMinYear > 1962 && entityMinYear != 100000) {
                    writeFile(ENTITY_PROPERTIES_PATH + qID +".json", content , false);
                }
            }
        }

        writeFile(ENTITY_JSON_PATH + qID +".json", content , false);
        return true;
    }

    private int getMinYear(Object entityJSON)
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


    private final String getSitelink(JSONObject entitiyContent, String qID, String wikiLang) throws Exception
    {
        JSONObject sitelinks = (JSONObject )entitiyContent.get("sitelinks");
        String sitelink = "";
        if (sitelinks.has(wikiLang)) {
            sitelink = sitelinks.getJSONObject(wikiLang).getString("url");
        }
        return sitelink;
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

    private static HashMap<String, String> viLabelHashMap = new HashMap<>();
    /**
     * Get the label of entity
     */
    private final String getViLabel(String qID) throws Exception
    {
        if (viLabelHashMap.containsKey(qID)) {
            return viLabelHashMap.get(qID);
        }
        return getViLabel(qID, ENTITY_JSON_PATH, ENTITY_PROPERTIES_PATH);
    }

    /**
     * Get the label of entity
     */
    public static String getViLabel(String qID, String jsonPath1, String jsonPath2) throws Exception
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
        return getViLabel(jsonContent, qID);
    }

    /**
     * Get the label of entity
     */
    public static String getViLabel(JSONObject jsonContent, String qID) throws Exception
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
    private void jsonPropertiesAnalysis(Object entityJSON, HashSet<String> entityJSONFileList)
    {
        if (entityJSON instanceof JSONArray) {
            for (int i = 0; i < ((JSONArray) entityJSON).length(); i++) { 
                jsonPropertiesAnalysis(((JSONArray) entityJSON).get(i), entityJSONFileList);
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
        jsonPropertiesAnalysis(claims, entityJSONFileList);
    }

    /**
     * Get all properties of all entities and save it to folder "Properties".
     * @throws Exception
     */
    @Override
    protected void getProperties() throws Exception
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
                    if (!getViLabel(new JSONObject(data), pID).isEmpty()) {
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

    private final JSONObject propertyProcess(JSONObject infoObj) throws Exception
    {
        JSONObject jsonObj = new JSONObject();
        String datatype = (String)infoObj.get("datatype");
        if (!infoObj.has("datavalue"))
        {
            return jsonObj;
        }
        JSONObject datavalue = infoObj.getJSONObject("datavalue");
        if (datatype.equals("wikibase-item") || datatype.equals("wikibase-property"))
        {
            JSONObject value = datavalue.getJSONObject("value");
            String id = value.getString("id");
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
            JSONObject value = datavalue.getJSONObject("value");
            String amount = value.getString("amount").replace("+", "");
            String unit = value.getString("unit");
            if (unit.equals("1")) {
                unit = " ";
            }
            else {
                String qID = unit.replace("http://www.wikidata.org/entity/", "");
                unit = getViLabel(qID); 
            }
            jsonObj.put("value", amount + " " + unit);
            jsonObj.put("type", "string");

        }
        else if (datatype.equals("string"))
        {
            String value = datavalue.getString("value");
            jsonObj.put("value", value);
            jsonObj.put("type", "string");
        }
        else if (datatype.equals("monolingualtext"))
        {
            JSONObject value = datavalue.getJSONObject("value");
            String lang = value.getString("language");
            if (lang.equals("vi"))
            {
                jsonObj.put("value", value.getString("text"));
                jsonObj.put("type", "string");
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
            jsonObj.put("value", formatDMY);
            jsonObj.put("type", "string");
        }
        return jsonObj;
    }

}
