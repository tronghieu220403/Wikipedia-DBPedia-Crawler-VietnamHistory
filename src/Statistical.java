import crawler.datamanage.DataHandling;

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
        stat.put("Number of entities", new JSONObject());
        stat.put("Number of properties", new JSONObject());
        JSONObject dbProp = new JSONObject();
        JSONObject wikiProp = new JSONObject();
        JSONObject dbUniqueProp = new JSONObject();
        JSONObject wikiUniqueProp = new JSONObject();
        JSONObject dbwUniqueProp = new JSONObject();
        JSONObject dbpedia = new JSONObject();
        JSONObject wikipedia = new JSONObject();
        int associated = 0;
        JSONObject hm = new JSONObject();
        for (String cat: BIG_CATEGORIES){
            String folderPath = dataFolderPath + cat + "/";
            int dbCount = 0;
            int dbPropCount = 0;
            int wikiPropCount = 0;
            HashSet<String> propWikiName = new HashSet<>();
            HashSet<String> propDbName = new HashSet<>();
            for (String fileName: DataHandling.listAllFiles(folderPath)){
                boolean checkDB = false;
                JSONObject json = DataHandling.getJSONFromFile(folderPath + fileName);
                HashSet<String> qIDHashSet = new HashSet<>();
                JSONObject claims = json.getJSONObject("claims");
                /*
                JSONArray instance = claims.getJSONArray("là một");
                for (int i = 0; i < instance.length(); i++){
                }
                */
                for (String key: DataHandling.getAllKeys(claims)){
                    JSONArray arr = claims.getJSONArray(key);
                    for (int i = 0; i < arr.length(); i++)
                    {
                        JSONObject obj = arr.getJSONObject(i);
                        JSONArray srcArr = obj.getJSONArray("source");
                        for (int j = 0; j < srcArr.length(); j++){
                            if (srcArr.getString(j).equals("DBPedia")){
                                checkDB = true;
                                dbPropCount++;
                                propDbName.add(key);
                            }
                            else{
                                propWikiName.add(key);
                                wikiPropCount++;
                            }
                        }
                        if (obj.has("id")){
                            associated++;
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
                            //qIDHashSet.add(obj.getString("id"));
                            associated++;
                        }
                        else{
                            wikiPropCount++;
                        }
                    }
                }
                //associated += qIDHashSet.size();
                if (checkDB){
                    dbCount++;
                }
                hm.put(fileName.replace(".json", ""), qIDHashSet.size());
            }
            dbUniqueProp.put(cat, propDbName.size());
            wikiUniqueProp.put(cat, propWikiName.size());
            propWikiName.addAll(propDbName);
            dbwUniqueProp.put(cat, propWikiName.size());

            dbProp.put(cat, dbPropCount);
            wikiProp.put(cat, wikiPropCount);
            dbpedia.put(cat, dbCount);
            wikipedia.put(cat, DataHandling.listAllFiles(folderPath).size());
        }
        stat.put("Number of relation", associated);
        
        stat.getJSONObject("Number of entities").put("Wikipedia", wikipedia);
        stat.getJSONObject("Number of entities").put("DBPedia", dbpedia);
        
        stat.getJSONObject("Number of properties").put("Wikipedia", new JSONObject());
        stat.getJSONObject("Number of properties").put("DBPedia", new JSONObject());
        stat.getJSONObject("Number of properties").put("Wikipedia + DBPedia", new JSONObject());
        stat.getJSONObject("Number of properties").getJSONObject("Wikipedia").put("Number of unique property names", wikiUniqueProp);
        stat.getJSONObject("Number of properties").getJSONObject("DBPedia").put("Number of unique property names", dbUniqueProp);
        stat.getJSONObject("Number of properties").getJSONObject("Wikipedia").put("Number of property value", wikiProp);
        stat.getJSONObject("Number of properties").getJSONObject("DBPedia").put("Number of property value", dbProp);
        stat.getJSONObject("Number of properties").getJSONObject("Wikipedia + DBPedia").put("Number of unique property names", dbwUniqueProp);


        DataHandling.writeFile("statistic.json", stat.toString(), false);
    }
}
