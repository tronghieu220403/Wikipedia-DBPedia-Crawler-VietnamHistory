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
        for (String bigCategory: bigCategories)
        {
            String path = "data/" + bigCategory;
            HashSet<String> fileList = listAllFiles(path);
            for (String fileName: fileList)
            {
                //if (!fileName.equals("Q1034173.json")) continue;
                String s = readFileAll(path + "/" + fileName);
                int oldSize = s.length();
                s = s.replace("chiến binh của", "thực thể tham chiến của");
                int newSize = s.length();
                if (oldSize != newSize)
                    writeFile(path + "/" + fileName, s, false);
            }
        }

        /*
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
