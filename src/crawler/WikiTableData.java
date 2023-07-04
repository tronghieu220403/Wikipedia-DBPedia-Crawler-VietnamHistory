package crawler;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

public class WikiTableData extends WikiData {

    public static void main(String[] args) throws Exception {
        WikiTableData wikiTableData = new WikiTableData("E:/Code/Java/OOP_Project/saveddata/Wikipedia/");
        wikiTableData.tableDataQueries();
    }

    public void tableDataQueries() throws Exception{
        tableDynastiesQueries();    // done
        tableLocationsQueries();    // done
    }


    public WikiTableData(){
        throw new IllegalArgumentException("File path must be provided.");
    }

    public WikiTableData(String folderPath) throws Exception{
        super(folderPath);
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
                WikiDataHandling.addProperties(claims, "quốc gia", "Việt Nam");
                WikiDataHandling.addProperties(claims, "là một", "triều đại");
                WikiDataHandling.createNewEntity(dynastyJsonObject, 
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

                WikiDataHandling.addProperties(kingClaims, "là một", "người");
                WikiDataHandling.addProperties(kingClaims, "là một", "vua");
                WikiDataHandling.addProperties(kingClaims, "quốc tịch", "Việt Nam");

                for (String prop: kingProp)
                {
                    if (!king.has(prop)) continue;
                    String value = king.getString(prop);
                    if (value.isEmpty()) continue;
                    WikiDataHandling.addProperties(kingClaims, prop.toLowerCase(), value);
                }

                if (kingQID.contains("X"))
                {
                    WikiDataHandling.createNewEntity(kingJsonObject, 
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

                WikiDataHandling.addProperties(kingRefJsonObject, "triều đại", dynastyName, dynastyQID);

                kingJsonObject.put("references", kingRefJsonObject);
                writeFile(ENTITY_FINAL_PATH + kingQID + ".json", kingJsonObject.toString(), false);
            }
            dynastyJsonObject.getJSONObject("references").put("vua", dynastyRefArr);
            writeFile(ENTITY_FINAL_PATH + dynastyQID + ".json", dynastyJsonObject.toString(), false);
        }
        
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
                        json = WikiDataHandling.createNewEntity();
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
                json = WikiDataHandling.createNewEntity();
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
                WikiDataHandling.addProperties(claims, "loại di tích", locationType);
            WikiDataHandling.addProperties(claims, "là một", "di tích");
            WikiDataHandling.addProperties(claims, "quốc gia", "Việt Nam");
            if (locationJSON.has("Vị trí")){
                if (!locationJSON.getString("Vị trí").isEmpty()){
                    WikiDataHandling.addProperties(claims, "vị trí", locationJSON.getString("Vị trí"));
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
                WikiDataHandling.addProperties(claims, "thời gian công nhận di tích", date);
            }
            writeFile(ENTITY_FINAL_PATH + qID + ".json", json.toString(), false);
        }
    }
}
