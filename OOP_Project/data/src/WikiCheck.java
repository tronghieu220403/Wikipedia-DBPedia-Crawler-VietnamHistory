
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.json.*;

public class WikiCheck extends WikiData{
    HashMap<String, Integer> count = new HashMap<>();

    public WikiCheck() throws Exception {
        super();
        countEntityType();
        countTotal();
    }

    public void countTotal() throws Exception
    {
        JSONObject statistics = getJSONFromFile("E:/Code/Java/OOP_Project/saveddata/Wikipedia/statistics.json");
        JSONObject myJsonObject = getJSONFromFile("E:\\Code\\Java\\OOP_Project\\saveddata\\Wikipedia\\WikiAnalys\\Category\\Instance.json");
        Iterator<String> keys = myJsonObject.keys();

        JSONObject _myJsonObject = new JSONObject();

        int total = 0;
        while(keys.hasNext()) {
            String key = keys.next();
            total += (int)statistics.get(key);
            _myJsonObject.put(key, (int)statistics.get(key));
        }
        writeFile("E:\\Code\\Java\\OOP_Project\\saveddata\\Wikipedia\\WikiAnalys\\Category\\Instance.json", _myJsonObject.toString(), false);
        print(total);

    }

    public void countEntityType() throws Exception
    {

        String finalEntityPath = "E:/Code/Java/OOP_Project/saveddata/Wikipedia/WikiAnalys/EntityFinal";
        HashSet<String> allQFile = listAllFiles(finalEntityPath);

        JSONObject myJsonObject = new JSONObject();

        for (String fileName: allQFile)
        {
            JSONObject json = getJSONFromFile(finalEntityPath + "/" + fileName);
            JSONObject claims = (JSONObject)(json.get("claims"));
            if (!claims.has("là một"))
            {
                continue;
            }
            JSONArray isIncstanceOf = (JSONArray)(claims.get("là một"));
            for(Object instance: isIncstanceOf)
            {
                JSONObject instanceObj = (JSONObject)instance;
                String value = (String)instanceObj.get("value");
                
                if (!myJsonObject.has(value))
                {
                    myJsonObject.put(value, 1);
                }
                else
                {
                    int cnt = (int)myJsonObject.get(value) + 1;
                    myJsonObject.put(value, cnt);
                }
                String content = (String)json.get("label") + "\n" + (String)json.get("id") + "\n";
                value = value.replace("/", " hoặc ");
                writeFile("E:/Code/Java/OOP_Project/saveddata/Wikipedia/WikiAnalys/Category/ByInstance/" + value + ".txt", content , true);
            }
        }
        Iterator<String> keys = myJsonObject.keys();

        while(keys.hasNext()) {
            String key = keys.next();
            int cnt = (int)myJsonObject.get(key);
            String content = "\"" + key + "\": " + String.valueOf(cnt) + ",";
            key = key.replace("/", " hoặc ");
            writeFile("E:/Code/Java/OOP_Project/saveddata/Wikipedia/WikiAnalys/Category/ByInstance/" + key + ".txt", content , true);
        }

        writeFile("E:/Code/Java/OOP_Project/saveddata/Wikipedia/statistics.json", myJsonObject.toString() , (boolean)false);

    }

    public static void main(String[] args) throws Exception {
        new WikiCheck();
    }
}
