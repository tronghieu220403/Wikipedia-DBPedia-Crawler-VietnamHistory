/*
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import org.json.*;
*/

public class WikiCheck extends WikiData{
/*
    HashMap<String, Integer> count = new HashMap<>();

    public WikiCheck() throws Exception {
        super();
        //countEntityType();
        localCheck(readFileAll(analysedURLsPath));
    }


    public boolean localCheckAll() throws Exception 
    {
        getVietnamRelatedEntity();
        String pathName1 = "E:/OneDrive - Hanoi University of Science and Technology/StudyMaterial/OOP/OOP Project/test/json";
        //String pathName2 = "E:/OneDrive - Hanoi University of Science and Technology/StudyMaterial/OOP/OOP Project/test/References";
        File directoryPath = new File(pathName1);
        File[] fileList = directoryPath.listFiles();
        int _count = 0;
        for(File file : fileList) {
            if (localCheck(readFileAll(pathName1+"/"+file.getName()))==false)
            {
                print(file.getName());
                _count += 1;
            }
        }
        if (_count == 0)
        {
            print("Not thing wrong with data.");
            return true;
        }
        else
        {
            print(_count + " files do not belong to Vietnam.");
            return false;
        }
        
    }

    public boolean localCheck(String content) throws Exception {
        JSONObject jsonData = new JSONObject(content);
        //print(jsonData);
        isRelated = false;
        JSONAnalysis(jsonData);
        return isRelated;
    }

    public final void getInstances(Object myJSON)
    {
        if (myJSON instanceof JSONArray)
        {
            for (int i = 0; i < ((JSONArray) myJSON).length(); i++) { 
                getInstances(((JSONArray) myJSON).get(i));
            }
        }
        else if (myJSON instanceof JSONObject)
        {
            if (((JSONObject) myJSON).has("property")==true)
            {
                if (((String)((((JSONObject) myJSON).get("property")))).equals("P31"))
                {
                    JSONObject datavalue = (JSONObject)((JSONObject) myJSON).get("datavalue");
                    //print(datavalue);
                    JSONObject value = (JSONObject)((JSONObject) datavalue).get("value");
                    String entityID = (String)((JSONObject) value).get("id");
                    if (count.containsKey(entityID)) {
                        int prevCount = count.get(entityID);
                        count.put(entityID, prevCount + 1); 
                    } else {
                        count.put(entityID, 1);  
                    }
                }
            }
            Iterator<String> keys = ((JSONObject) myJSON).keys();
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = ((JSONObject) myJSON).get(key);
                
                if (value instanceof JSONObject) {
                    getInstances((JSONObject) value);
                } else if (value instanceof JSONArray) {
                    getInstances((JSONArray) value);
                }
            }
        }
    }

    public void countEntityType() throws Exception
    {
        String pathName1 = "E:/OneDrive - Hanoi University of Science and Technology/StudyMaterial/OOP/OOP Project/test/json";
        //String pathName2 = "E:/OneDrive - Hanoi University of Science and Technology/StudyMaterial/OOP/OOP Project/test/References";
        File directoryPath = new File(pathName1);
        File[] fileList = directoryPath.listFiles();
        for(File file : fileList) {
            String content = readFileAll(pathName1+"/"+file.getName());
            JSONObject jsonData = new JSONObject(content);
            getInstances(jsonData);
        }
        // print(count.get("Q5")); // number of human
        // print(count.get("Q178561")); // number of battle
        //print(count);
        ArrayList<Integer> list = new ArrayList<>();
 
        for (Map.Entry<String, Integer> entry : count.entrySet()) {
            
            list.add((entry.getValue()).intValue());
        }
        Collections.sort(list, new Comparator<Integer>() {
            public int compare(Integer str, Integer str1) {
                return (str).compareTo(str1);
            }
        });
        //print(list);
        StringBuffer content = new StringBuffer("{");
        int last = 0;
        for (Integer freq : list) {
            if (freq.intValue() == last) continue;
            for (Entry<String, Integer> entry : count.entrySet()) {
                if (entry.getValue().intValue() == freq.intValue()) {
                    //sortedMap.put(entry.getKey(), str);
                    content.append("\""+entry.getKey()+"\":");
                    content.append(freq);
                    content.append(",");
                }
            }
            last = freq.intValue();
        }
        content.setLength(content.length() - 1);  
        content.append("}");
        //String content = (String)(new JSONObject(sortedMap).toString());
        //print(content.length());
        writeFile("src/Wikipedia/statistics.json", content.toString() , (boolean)false);
        
    }

    public static void main(String[] args) throws Exception {
        new WikiCheck();
    }
*/
}
