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
        JSONObject dbpedia = new JSONObject();
        JSONObject wikipedia = new JSONObject();
        int associated = 0;
        JSONObject hm = new JSONObject();
        for (String cat: BIG_CATEGORIES){
            String folderPath = dataFolderPath + cat + "/";
            int dbCount = 0;
            for (String fileName: DataHandling.listAllFiles(folderPath)){
                boolean checkDB = false;
                JSONObject json = DataHandling.getJSONFromFile(folderPath + fileName);
                HashSet<String> qIDHashSet = new HashSet<>();
                JSONObject claims = json.getJSONObject("claims");
                JSONArray instance = claims.getJSONArray("là một");
                for (int i = 0; i < instance.length(); i++){
                    /*
                    if (!instance.getJSONObject(i).has("source"))
                    {
                        DataHandling.print(i);

                    }
                    JSONArray arr = instance.getJSONObject(i).getJSONArray("source");
                    for (int j = 0; j < arr.length(); j++){
                        if (arr.getString(j).equals("DBPedia")){
                            checkDB = true;
                            break;
                        }
                    }*/
                    if (instance.getJSONObject(i).getString("value").equals("thời kỳ lịch sử")){
                        DataHandling.print(fileName.replaceAll(".json", ""));
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
                if (checkDB){
                    dbCount++;
                }
                hm.put(fileName.replace(".json", ""), qIDHashSet.size());
            }
            dbpedia.put(cat, dbCount);
            wikipedia.put(cat, DataHandling.listAllFiles(folderPath).size());
        }
        stat.put("Number of relation", associated);
        //DataHandling.print(hm);
        stat.getJSONObject("Number of entity").put("Wikipedia", wikipedia);
        stat.getJSONObject("Number of entity").put("DBPedia", dbpedia);
        DataHandling.writeFile("statistic.json", stat.toString(), false);
    }
}
