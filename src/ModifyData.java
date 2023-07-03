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

    private JSONObject addProperties(JSONObject myJsonClaims, String propName, String value, String qID, String source)
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
        if (!source.isEmpty())
        {
            addObj.put("source", source);
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

    private void removeEntityInEntity(JSONObject json, HashSet<String> rmHashSet, JSONObject changeName)
    {
        JSONObject claims = json.getJSONObject("claims");
        JSONObject ref = json.getJSONObject("references");
        for (String key: getAllKeys(claims)){
            JSONArray prop = claims.getJSONArray(key);
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
        for (String key: getAllKeys(ref)){
            JSONArray prop = ref.getJSONArray(key);
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
        for (String key: getAllKeys(claims)){
            if (changeName.has(key)){
                String changed = changeName.getString(key);
                if (changed.isEmpty()){
                    claims.remove(key);
                }
                else{
                    JSONArray arr = claims.getJSONArray(key);
                    for (int i = 0; i < arr.length(); i++)
                    {
                        JSONObject obj = arr.getJSONObject(i);
                        
                        String source = "";
                        if (obj.has("source")){
                            source = obj.getString("source");
                        }
                        if (!obj.has("id")){
                            addProperties(claims, changed, obj.getString("value"), "", source);
                        }
                        else{
                            addProperties(claims, changed, obj.getString("value"),obj.getString("id"), source);
                        }
                    }
                    claims.remove(key);
                }
            }
        }
        for (String key: getAllKeys(ref)){
            if (changeName.has(key)){
                String changed = changeName.getString(key);
                if (changed.isEmpty()){
                    ref.remove(key);
                }
                else{
                    JSONArray arr = ref.getJSONArray(key);
                    for (int i = 0; i < arr.length(); i++)
                    {
                        JSONObject obj = arr.getJSONObject(i);
                        String source = "";
                        if (obj.has("source")){
                            source = obj.getString("source");
                        }
                        if (!obj.has("id")){
                            addProperties(ref, changed, obj.getString("value"),"", source);
                        }
                        else{
                            addProperties(ref, changed, obj.getString("value"),obj.getString("id"), source);
                        }
                    }
                }
            }
        }
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
                removeEntityInEntity(json, rmHashSet, changeName);
                writeFile("data/" + bigCategory + "/" + fileName, json.toString(), false);
                continue;
            }

        }
    }

}
