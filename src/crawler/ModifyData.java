package crawler;


import java.util.HashSet;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class ModifyData extends DataHandling{
    
    public static void main(String[] args) throws Exception {
        ModifyData md = new ModifyData();
        md.removeEntity();
    }

    public static final String[] BIG_CATEGORIES = {"triều đại lịch sử","địa điểm du lịch, di tích lịch sử", "lễ hội văn hóa", "sự kiện lịch sử", "nhân vật lịch sử"};

    public static boolean cmpPropValue(JSONObject obj1, JSONObject obj2)
    {
        if (!obj1.has("value") || !obj2.has("value") || !obj1.has("type") || !obj1.has("type")){
            return false;
        }
        if(obj1.getString("value").equals(obj2.getString("value"))){
            return false;
        }
        if(obj1.getString("type").equals(obj2.getString("type"))){
            return false;
        }
        if(obj1.has("id") && obj2.has("id") && !obj1.getString("id").equals(obj2.getString("id"))){
            return false;
        }
        if (obj1.has("qualifiers") != obj2.has("qualifiers")){
            return false;
        }
        if (!obj1.has("qualifiers")){
            return true;
        }
        JSONArray arr1 = obj1.getJSONArray("qualifiers");
        JSONArray arr2 = obj2.getJSONArray("qualifiers");
        for (int i = 0; i < arr1.length(); i++){
            boolean check = false;
            for (int j = 0; j < arr2.length(); j++){
                if (cmpPropValue(arr1.getJSONObject(i), arr2.getJSONObject(j))){
                    check = true;
                    break;
                }
            }
            if (check == false){
                return false;
            }
        }
        return true;
    }

    private JSONObject addProperties(JSONObject myJsonClaims, String propName, JSONObject addObj)
    {
        if (!myJsonClaims.has(propName)){
            JSONArray jsonArr = new JSONArray();
            jsonArr.put(addObj);
            myJsonClaims.put(propName, jsonArr);
        }
        else{
            JSONArray jsonArr = myJsonClaims.getJSONArray(propName);
            boolean check = false;
            for (int i = 0; i < jsonArr.length(); i++){
                JSONObject obj = jsonArr.getJSONObject(i);
                if (cmpPropValue(obj, addObj)){
                    check = true;
                    break;
                }
            }
            if (!check){
                jsonArr.put(addObj);
            }
        }
        return myJsonClaims;
    }

    private void modifyEntity(JSONObject json, HashSet<String> rmHashSet, JSONObject changeName)
    {
        JSONObject claims = json.getJSONObject("claims");
        JSONObject ref = json.getJSONObject("references");
        removeDeletedEntities(claims, rmHashSet);
        removeDeletedEntities(ref, rmHashSet);
        changePropNameInEntity(claims, changeName);
        changePropNameInEntity(ref, changeName);
    }

    private void removeEntity() throws Exception{
        List<String> list = readFileAllLine("delete.txt");
        JSONObject changeName = getJSONFromFile("change_name.json");
        HashSet<String> rmHashSet = new HashSet<>();
        for (String pID: list)
        {
            rmHashSet.add(pID);
        }
        for (String bigCategory: BIG_CATEGORIES)
        {
            for (String fileName: listAllFiles("data/" + bigCategory))
            {
                String qID = fileName.replace(".json","");
                if (rmHashSet.contains(qID)){
                    deleteFile("data/" + bigCategory + "/" + fileName);
                    continue;
                }
                JSONObject json = getJSONFromFile("data/" + bigCategory + "/" + fileName);
                modifyEntity(json, rmHashSet, changeName);
                writeFile("data/" + bigCategory + "/" + fileName, json.toString(), false);
            }

        }
    }

    private void removeDeletedEntities(JSONObject json,HashSet<String> rmHashSet){
        for (String key: getAllKeys(json)){
            JSONArray prop = json.getJSONArray(key);
            for (int i = 0; i < prop.length(); i++)
            {
                JSONObject obj = prop.getJSONObject(i);
                if (obj.has("id")){
                    String qID = obj.getString("id");
                    if (rmHashSet.contains(qID)){
                        obj.put("type", "string");
                        obj.remove("id");
                    }
                }
            }
        }
    }

    private void changePropNameInEntity(JSONObject json, JSONObject changeName){
        for (String key: getAllKeys(json)){
            if (changeName.has(key)){
                String changed = changeName.getString(key);
                if (changed.isEmpty()){
                    json.remove(key);
                }
                else{
                    JSONArray arr = json.getJSONArray(key);
                    for (int i = 0; i < arr.length(); i++)
                    {
                        addProperties(json, changed, arr.getJSONObject(i));
                    }
                    json.remove(key);
                }
            }
        }
    }
}
