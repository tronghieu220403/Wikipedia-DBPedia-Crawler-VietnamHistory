import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.json.JSONObject;


public class Testing extends DataHandling {
    public static void main(String[] args) throws Exception {
        HashMap<String, String> p = new HashMap<>();
        String filePath = "E:/Code/Java/OOP_Project/saveddata/Wikipedia/WikiAnalys/Category/export/";
        String[] bigCategories = {"địa điểm du lịch, di tích lịch sử", "lễ hội văn hóa", "nhân vật lịch sử", "sự kiện lịch sử", "triều đại lịch sử"};
        for (String bigCategory: bigCategories)
        {
            HashSet<String> fileList = listAllFiles(filePath + bigCategory);
            for (String fileName: fileList)
            {
                JSONObject json = getJSONFromFile(filePath + bigCategory + "/" + fileName);
                if (json.has("claims"))
                {                
                    JSONObject claims = (JSONObject)json.get("claims");
                    Iterator<String> keys = claims.keys();

                    while(keys.hasNext()) {
                        String key = keys.next();

                        if (!p.containsKey(key))
                        {
                        }
                        p.put(key, fileName);
                    }
                }
            }
        }
        writeFile("gg.json", (new JSONObject(p)).toString(), false);
    }

    
}
