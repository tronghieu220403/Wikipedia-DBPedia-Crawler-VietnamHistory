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
            String path = "E:/Code/Java/OOP_Project/saveddata/Wikipedia/data/" + bigCategory;
            HashSet<String> fileList = listAllFiles(path);
            for (String fileName: fileList)
            {
                filePaths.add(path + "/" + fileName);
            }
        }
        String path = "E:/Code/Java/OOP_Project/saveddata/Wikipedia/WikiAnalys/EntityFinal";
        HashSet<String> fileList = listAllFiles(path);
        for (String fileName: fileList)
        {
            filePaths.add(path + "/" + fileName);
        }
        for (String filePath: filePaths)
        {
            JSONObject json = getJSONFromFile(filePath);
            JSONObject claims = json.getJSONObject("claims");
            for (String propName: getAllKeys(claims))
            {
                JSONArray jsonArr = claims.getJSONArray(propName);
                for (int i = 0; i < jsonArr.length(); i++)
                {
                    JSONObject obj = jsonArr.getJSONObject(i);   
                    if (obj.has("qualifiers"))
                    {
                        if (obj.getJSONObject("qualifiers").has("lý do lỗi thời"))
                        {
                            obj.getJSONObject("qualifiers").remove("lý do lỗi thời");
                            if (obj.getJSONObject("qualifiers").length() == 0)
                            {
                                obj.remove("qualifiers");
                            }
                        }
                    }
                }
            }
            writeFile(filePath, json.toString(), false);
        }

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
