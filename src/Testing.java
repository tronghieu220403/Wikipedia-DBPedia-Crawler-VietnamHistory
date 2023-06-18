import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;


public class Testing extends DataHandling {
    public static void main(String[] args) throws Exception {
        //String[] bigCategories = {"địa điểm du lịch, di tích lịch sử", "lễ hội văn hóa", "nhân vật lịch sử", "sự kiện lịch sử", "triều đại lịch sử"};
        String[] bigCategories = {"địa điểm du lịch, di tích lịch sử", "lễ hội văn hóa", "nhân vật lịch sử", "sự kiện lịch sử", "triều đại lịch sử"};
        StringBuffer sb = new StringBuffer();
        HashSet<String> filePaths = new HashSet<>();
        for (String bigCategory: bigCategories)
        {
            String path = "data/" + bigCategory;
            HashSet<String> fileList = listAllFiles(path);
            for (String fileName: fileList)
            {
                filePaths.add(path + "/" + fileName);
            }
        }
        for (String bigCategory: bigCategories)
        {
            String path = "E:\\Code\\Github\\VietNamHistory\\src\\data\\" + bigCategory;
            HashSet<String> fileList = listAllFiles(path);
            for (String fileName: fileList)
            {
                filePaths.add(path + "\\" + fileName);
            }
        }
        for (String bigCategory: bigCategories)
        {
            String path = "E:/Code/Java/OOP_Project/saveddata/Wikipedia/data/" + bigCategory;
            HashSet<String> fileList = listAllFiles(path);
            for (String fileName: fileList)
            {
                filePaths.add(path + "/" + fileName);
            }
        }
        String path = "E:/Code/Java/OOP_Project/saveddata/Wikipedia/WikiAnalys/EntityFinal";
        for (String fileName: listAllFiles(path))
        {
            filePaths.add(path + "/" + fileName);
        }
        path = "E:\\Code\\Java\\OOP_Project\\saveddata\\DBPedia\\data";
        for (String fileName: listAllFiles(path))
        {
            filePaths.add(path + "\\" + fileName);
        }
        
        String str = "địa điểm";

        int cnt = 0;
        String erase = "nằm trong phạm vi của khu vực hành chính";
        String replace = "địa phương";

        /*
        for (String filePath: filePaths)
        {
            String s = readFileAll(filePath);
            int oldSize = s.length();
            s = s.replace(erase, replace);
            if (s.length()!=oldSize)
            {
                cnt++;
                writeFile(filePath, s, false);
            }
        }
        print(cnt);
        */
        for (String filePath: filePaths)
        {
            //if (filePath.length()>0) continue;
            JSONObject json;
            try{
                json = getJSONFromFile(filePath);
            }
            catch (Exception e)
            {
                print(filePath);
                return;
            }
            boolean check = false;
            if (json.has("claims"))
            {
                JSONObject claims = json.getJSONObject("claims");
                if (claims.has(erase))
                {
                    //claims.put(replace, claims.get(erase));
                    claims.remove(erase);
                    check = true;
                }
            }
            if (json.has("references"))
            {   
                JSONObject ref = json.getJSONObject("references");
                if (ref.has(erase))
                {
                    //ref.put(replace, ref.get(erase));
                    ref.remove(erase);
                    check = true;
                }
            }   
            if (check == true)
            {
                cnt++;
                //print(filePath);
                //writeFile(filePath, json.toString(), false);
            }
        }
        print(cnt);

        /*

        for (String bigCategory: bigCategories)
        {
            String path = "E:/Code/Java/OOP_Project/saveddata/Wikipedia/data/" + bigCategory;
            HashSet<String> fileList = listAllFiles(path);
            for (String fileName: fileList)
            {
                String s = readFileAll(path + "/" + fileName);
                int oldSize = s.length();
                s = s.replace("Bight (địa lý)", "");
                if (s.length()!=oldSize)
                {
                    writeFile(path + "/" + fileName, s, false);
                }
            }
        }

        String path = "E:/Code/Java/OOP_Project/saveddata/Wikipedia/WikiAnalys/EntityFinal";
        HashSet<String> fileList = listAllFiles(path);
        for (String fileName: fileList)
        {
            String s = readFileAll(path + "/" + fileName);
            int oldSize = s.length();
            s = s.replace("Bight (địa lý)", "");
            if (s.length()!=oldSize)
            {
                writeFile(path + "/" + fileName, s, false);
            }
        }



        JSONObject json = getJSONFromFile("E:/Code/Java/OOP_Project/saveddata/Wikipedia/WikiAnalys/Category/export1/nhân vật lịch sử/Q36014.json");
        JSONObject claims = (JSONObject)json.get("claims");
        Iterator<String> properties = ((JSONObject) claims).keys();
        StringBuilder sb = new StringBuilder();
        while (properties.hasNext()) {
            String propertyName = properties.next();
            sb.append(propertyName + ": ");
            JSONArray propertyArr = (JSONArray)claims.get(propertyName);
            int count = 0;
            for (Object propertyDetail: propertyArr)
            {
                if (count > 0)
                {
                    sb.append(", ");
                }
                JSONObject obj = (JSONObject)propertyDetail;
                String value = (String)obj.get("value");
                sb.append(value +" ");
                if (obj.has("qualifiers"))
                {
                    sb.append("(");
                    JSONObject qualifiersObj = (JSONObject)obj.get("qualifiers");
                    Iterator<String> qualifierKeys = qualifiersObj.keys();
                    int subCount = 0;
                    while(qualifierKeys.hasNext())
                    {
                        if (subCount != 0) sb.append(", ");
                        String qualifierPropertyName = qualifierKeys.next();
                        sb.append(qualifierPropertyName + ": ");
                        JSONArray qualifierPropertyArr = (JSONArray)qualifiersObj.get(qualifierPropertyName);
                        int subSubCount = 0;
                        for (Object ele: qualifierPropertyArr)
                        {
                            if (subSubCount != 0) sb.append(", ");
                            JSONObject subQualifierProperty = (JSONObject)ele;
                            sb.append(subQualifierProperty.get("value"));
                            subSubCount++;
                        }
                        subCount++;
                    }
                    sb.append(")");
                }
                count++;
            }
            sb.append("\n");
        }
        writeFile("hehe.txt",sb.toString(), false);
        */
    }

    
}
