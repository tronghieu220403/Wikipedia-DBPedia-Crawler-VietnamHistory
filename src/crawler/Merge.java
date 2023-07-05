package crawler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class Merge {
    public static void main(String[] args) throws Exception {
        //Merge mergeData = new Merge();
        //mergeData.merge("data","E:/Code/Java/OOP_Project/saveddata/Wikipedia/", "E:/Code/Java/OOP_Project/saveddata/DBPedia/data/", createSource("Wikipedia"), createSource("DBPedia"));
    }

    public static String[] bigCategories = {"địa điểm du lịch, di tích lịch sử", "lễ hội văn hóa", "nhân vật lịch sử", "sự kiện lịch sử", "triều đại lịch sử"};

    public void merge(String exportPath, String path1, String path2, JSONArray src1, JSONArray src2) throws Exception
    {
        path1 += "data";
        path2 += "data";
        DataHandling.createFolder(exportPath);
        for (String bigCategory: bigCategories)
        {
            String path = path1 + "/" + bigCategory;
            String exportDataSubFolder = exportPath + "/" + bigCategory;
            DataHandling.createFolder(exportDataSubFolder);
            HashSet<String> fileList = DataHandling.listAllFiles(path);
            for (String fileName: fileList)
            {
                JSONObject wikiJSON = DataHandling.getJSONFromFile(path + "/" + fileName);
                if (DataHandling.fileExist(path2 + fileName))
                {
                    JSONObject dbpediaJSON = DataHandling.getJSONFromFile(path2 + fileName);
                    JSONObject dbpediaClaims = dbpediaJSON.getJSONObject("claims");
                    JSONObject wikiClaims = wikiJSON.getJSONObject("claims");
                    JSONObject exportClaims = new JSONObject();
                    for (String propertyName: DataHandling.getAllKeys(wikiClaims))
                    {                        
                        JSONArray wikiPropertyArr = wikiClaims.getJSONArray(propertyName);
                        if (dbpediaClaims.has(propertyName))
                        {
                            JSONArray dbpediaPropertyArr = dbpediaClaims.getJSONArray(propertyName);
                            JSONArray fullJoinArr = fullJoin(wikiPropertyArr, dbpediaPropertyArr, src1, src2);
                            exportClaims.put(propertyName, fullJoinArr);
                        }
                        else
                        {
                            for (int i = 0; i < wikiPropertyArr.length(); i++)
                            {
                                wikiPropertyArr.getJSONObject(i).put("source", src1);
                            }
                            exportClaims.put(propertyName, wikiPropertyArr);
                        }
                    }
                    for (String propertyName: DataHandling.getAllKeys(dbpediaClaims))
                    {
                        if (!wikiClaims.has(propertyName))
                        {
                            JSONArray dbpediaPropertyArr = dbpediaClaims.getJSONArray(propertyName);
                            for (int i = 0; i < dbpediaPropertyArr.length(); i++)
                            {
                                dbpediaPropertyArr.getJSONObject(i).put("source", src2);
                            }
                            exportClaims.put(propertyName, dbpediaClaims.getJSONArray(propertyName));
                        }
                    }
                    wikiJSON.put("claims", exportClaims);
                }
                DataHandling.writeFile(exportDataSubFolder + "/" + fileName, wikiJSON.toString(), false);
            }
        }
    }

    public static JSONArray createSource(String... sources){
        JSONArray srcArray = new JSONArray();
        List<String> arr = new ArrayList<>();
        for (String source: sources){
            arr.add(source);
        }
        Collections.sort(arr); 
        for (String str: arr){
            srcArray.put(str);
        }
        return srcArray;
    }

    public static boolean cmpPropObj(JSONObject obj1, JSONObject obj2){
        JSONArray objSrc1 = new JSONArray();
        if (obj1.has("source")){
            objSrc1 = obj1.getJSONArray("source");
            obj1.remove("source");
        }
        JSONArray objSrc2 = new JSONArray();
        if (obj2.has("source")){
            objSrc2 = obj2.getJSONArray("source");
            obj2.remove("source");
        }
        boolean check = false;
        if (obj1.toString().equals(obj2.toString())){
            check = true;
        }
        if (objSrc1.length() > 0) obj1.put("source", objSrc1);
        if (objSrc2.length() > 0) obj2.put("source", objSrc2);
        return check;
    }

    private JSONArray fullJoin(JSONArray arr1, JSONArray arr2, JSONArray src1, JSONArray src2){
        JSONArray ansArr = new JSONArray();
        JSONArray srcMergeJsonArray = new JSONArray();
        HashSet<String> srcHashSet = new HashSet<>();
        for (Object source: src1){
            srcHashSet.add((String )source);
        }
        for (Object source: src2){
            srcHashSet.add((String )source);
        }
        List<String> srcArr = new ArrayList<>();
        for (String source: srcHashSet){
            srcArr.add(source);
        }
        for (String str: srcArr){
            srcMergeJsonArray.put(str);
        }
        for (int i = 0; i < arr1.length(); i++)
        {
            JSONObject obj1 = arr1.getJSONObject(i);
            boolean isUnique = true;
            for (int j = 0; j < arr2.length(); j++)
            {
                JSONObject obj2 = arr2.getJSONObject(j);
                if (cmpPropObj(obj1, obj2)) {
                    obj1.put("source", srcMergeJsonArray);
                    ansArr.put(obj1);
                    isUnique = false;
                    break;
                }
            }
            if (isUnique == true)
            {
                obj1.put("source", src1);
                ansArr.put(obj1);
            }
        }
        for (int i = 0; i < arr2.length(); i++)
        {
            JSONObject obj2 = arr2.getJSONObject(i);
            boolean isUnique = true;
            for (int j = 0; j < arr1.length(); j++)
            {
                JSONObject obj1 = arr1.getJSONObject(j);
                if (cmpPropObj(obj1, obj2)){
                    isUnique = false;
                    break;
                }
            }
            if (isUnique == true){
                obj2.put("source", src2);
                ansArr.put(obj2);
            }
        }
        return ansArr;
    }
}
