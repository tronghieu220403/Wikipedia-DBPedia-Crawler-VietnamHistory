import crawler.DataManage.DataHandling;

import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;

public class Statistical {

    //public static String dataFolderPath = "data/";
    public static final String[] BIG_CATEGORIES = {"triều đại lịch sử","địa điểm du lịch, di tích lịch sử", "lễ hội văn hóa", "sự kiện lịch sử", "nhân vật lịch sử"};

    public static void main(String[] args) throws Exception {
        new Statistical("data/");
    }

    HashSet<String> entityHashSet = new HashSet<>();

    Statistical(String dataFolderPath) throws Exception{
        JSONObject stat = new JSONObject();
        stat.put("Number of entity", new JSONObject());
        JSONObject e = new JSONObject();
        int associated = 0;
        JSONObject hm = new JSONObject();
        for (String cat: BIG_CATEGORIES){
            String folderPath = dataFolderPath + cat + "/";
            for (String fileName: DataHandling.listAllFiles(folderPath)){
                JSONObject json = DataHandling.getJSONFromFile(folderPath + fileName);
                HashSet<String> qIDHashSet = new HashSet<>();
                JSONObject claims = json.getJSONObject("claims");
                JSONArray instance = claims.getJSONArray("là một");
                for (int i = 0; i < instance.length(); i++){
                    String s = instance.getJSONObject(i).getString("value");
                    if (!e.has(s)){
                        e.put(s, 1);
                    }
                    else{
                        e.put(s, e.getInt(s)+1);
                    }
                }
                for (String key: DataHandling.getAllKeys(claims)){
                    JSONArray arr = claims.getJSONArray(key);
                    for (int i = 0; i < arr.length(); i++)
                    {
                        JSONObject obj = arr.getJSONObject(i);
                        if (obj.has("id")){
                            qIDHashSet.add(obj.getString("id"));
                        }
                    }
                }
                JSONObject ref = json.getJSONObject("references");
                for (String key: DataHandling.getAllKeys(ref)){
                    JSONArray arr = ref.getJSONArray(key);
                    for (int i = 0; i < arr.length(); i++)
                    {
                        JSONObject obj = arr.getJSONObject(i);
                        if (obj.has("id")){
                            qIDHashSet.add(obj.getString("id"));
                        }
                    }
                }
                associated += qIDHashSet.size();
                hm.put(fileName.replace(".json", ""), qIDHashSet.size());
            }
            stat.getJSONObject("Number of entity").put(cat, DataHandling.listAllFiles(folderPath).size());
        }
        stat.put("Number of entity for each type", e);
        stat.put("Number of relation", associated);
        stat.put("Number of relation of each entity", hm);
        //DataHandling.print(hm);
        
        DataHandling.writeFile("statistic.json", stat.toString(), false);
    }
}
