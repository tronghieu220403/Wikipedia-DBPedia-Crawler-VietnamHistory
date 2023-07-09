import crawler.datamanage.DataHandling;

import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;

public class Statistical {

    //public static String dataFolderPath = "data/";
    public static final String[] BIG_CATEGORIES = {"triều đại lịch sử","địa điểm du lịch, di tích lịch sử", "lễ hội văn hóa", "sự kiện lịch sử", "nhân vật lịch sử"};

    public static void main(String[] args) throws Exception {
        new Statistical("E:/Code/Github/VietNamHistory/src/data/");
    }

    HashSet<String> entityHashSet = new HashSet<>();

    Statistical(String dataFolderPath) throws Exception{
        JSONObject stat = new JSONObject();
        stat.put("Number of entities", new JSONObject());
        stat.put("Number of properties", new JSONObject());
        JSONObject dbProp = new JSONObject();
        JSONObject wikiProp = new JSONObject();
        JSONObject nksProp = new JSONObject();

        JSONObject dbUniqueProp = new JSONObject();
        JSONObject wikiUniqueProp = new JSONObject();
        JSONObject nksUniqueProp = new JSONObject();

        JSONObject dbpedia = new JSONObject();
        JSONObject wikipedia = new JSONObject();
        JSONObject nks = new JSONObject();

        int associated = 0;
        JSONObject hm = new JSONObject();
        for (String cat: BIG_CATEGORIES){
            String folderPath = dataFolderPath + cat + "/";
            int dbCount = 0;
            int wikiCount = 0;
            int nksCount = 0;

            int dbPropCount = 0;
            int nksPropCount = 0;
            int wikiPropCount = 0;

            HashSet<String> propWikiName = new HashSet<>();
            HashSet<String> propDbName = new HashSet<>();
            HashSet<String> propNksName = new HashSet<>();
            for (String fileName: DataHandling.listAllFiles(folderPath)){
                boolean checkDB = false;
                boolean checkNKS = false;
                boolean checkWiki = false;
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
                            else if (srcArr.getString(j).equals("Wikipedia")){
                                checkWiki = true;
                                propWikiName.add(key);
                                wikiPropCount++;
                            }
                            else if (srcArr.getString(j).equals("Người kể sử")){
                                propNksName.add(key);
                                checkNKS = true;
                                nksPropCount++;
                            }
                        }
                        if (obj.has("id")){
                            associated++;
                        }
                    }
                }
                if (json.has("references")){
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
                }
                //associated += qIDHashSet.size();
                if (checkDB){
                    dbCount++;
                }
                if (checkWiki){
                    wikiCount++;
                }
                if (checkNKS){
                    nksCount++;
                }
                hm.put(fileName.replace(".json", ""), qIDHashSet.size());
            }
            dbUniqueProp.put(cat, propDbName.size());
            wikiUniqueProp.put(cat, propWikiName.size());

            propWikiName.addAll(propDbName);
            dbUniqueProp.put(cat, propDbName.size());

            propNksName.addAll(propNksName);
            nksUniqueProp.put(cat, propNksName.size());

            dbProp.put(cat, dbPropCount);
            wikiProp.put(cat, wikiPropCount);
            nksProp.put(cat, nksPropCount);

            dbpedia.put(cat, dbCount);
            wikipedia.put(cat, wikiCount);
            nks.put(cat, nksCount);
        }
        stat.put("Number of relation", associated);
        
        stat.getJSONObject("Number of entities").put("Wikipedia", wikipedia);
        stat.getJSONObject("Number of entities").put("DBPedia", dbpedia);
        stat.getJSONObject("Number of entities").put("Người kể sử", nks);

        stat.getJSONObject("Number of properties").put("Wikipedia", new JSONObject());
        stat.getJSONObject("Number of properties").put("DBPedia", new JSONObject());
        stat.getJSONObject("Number of properties").put("Người kể sử", new JSONObject());

        stat.getJSONObject("Number of properties").getJSONObject("Wikipedia").put("Number of unique property names", wikiUniqueProp);
        stat.getJSONObject("Number of properties").getJSONObject("DBPedia").put("Number of unique property names", dbUniqueProp);
        stat.getJSONObject("Number of properties").getJSONObject("Người kể sử").put("Number of unique property names", nksUniqueProp);

        stat.getJSONObject("Number of properties").getJSONObject("Wikipedia").put("Number of property value", wikiProp);
        stat.getJSONObject("Number of properties").getJSONObject("DBPedia").put("Number of property value", dbProp);
        stat.getJSONObject("Number of properties").getJSONObject("Người kể sử").put("Number of property value", nksProp);
        //stat.getJSONObject("Number of properties").getJSONObject("Wikipedia + DBPedia").put("Number of unique property names", dbpUniqueProp);

        DataHandling.writeFile("E:\\Code\\Github\\VietNamHistory\\src\\statistic.json", stat.toString(), false);

    }
}
