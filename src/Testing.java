import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class Testing extends DataHandling {

    private final static String[] FILTER = {
        "#","T%E1%BA%ADp_tin"
    };

    public static boolean checkURL(String urlString) {
        if (!urlString.contains("http")) return false;  

        if (urlString == null || urlString.isEmpty()) return false;  
        if (!urlString.contains("/wiki/")) return false;
        
        for (String text : FILTER) {
            if (urlString.contains(text)) return false;
        }
        
        if (urlString.chars().filter(ch -> ch == ':').count() > 1) {
            return false;  
        }
        
        return true;  
    }


    private final static HashSet<String> getAllHref(String htmlData) throws Exception
    {

        HashSet<String> hrefList = new HashSet<>();

        Document doc = Jsoup.parse(htmlData);
        Element divTag = doc.getElementById("mw-content-text"); 
        
        Elements tables = divTag.select("table[align=right]");
        for (Element table : tables) {
            table.remove();
        }

        Element xemThemTag = divTag.selectFirst("h2:has(span#Xem_th\\.C3\\.AAm)"); // Get the Xem thêm tag
        if (xemThemTag != null) {
            Element nextElement = xemThemTag.nextElementSibling(); // Get the next element after Xem thêm tag
            while (nextElement != null) {
                Element toRemove = nextElement; // Store the current element to remove
                nextElement = nextElement.nextElementSibling(); // Get the next element
                toRemove.remove(); // Remove the current element from the DOM
            }
        }

        Elements navboxElements = divTag.select("div.navbox"); // Get all elements with class navbox
        for (Element navboxElement : navboxElements) {
            navboxElement.remove(); // Remove each navbox element from the DOM
        }
        
        for (Element aTag : divTag.select("a")) {
            String href = aTag.attr("href");
            String fullURL = "https://vi.wikipedia.org" + href;
            if (!checkURL(fullURL)) continue;
            fullURL = urlDecode(fullURL);
            hrefList.add(fullURL);
        }
        return hrefList;
    }


    public static void main(String[] args) throws Exception {
        print("1[2][4]3".replaceAll("\\s*\\[[^\\]]*\\]\\s*", ""));
        String a = "E:\\Code\\Java\\OOP_Project\\saveddata\\Wikipedia\\logs\\EntityFinal\\";
        for (String fileName: listAllFiles(a))
        {
            JSONObject o = getJSONFromFile(a + fileName);
            String overv = o.getString("overview");
            String gg = overv.replaceAll("\\s*\\[[^\\]]*\\]\\s*", "");
            if (!gg.equals(overv))
            {
                o.put("overview", gg);
                writeFile(a + fileName, o.toString(), false);
                //print(fileName);
            }
        }
        /*
        //String[] bigCategories = {"địa điểm du lịch, di tích lịch sử", "lễ hội văn hóa", "nhân vật lịch sử", "sự kiện lịch sử", "triều đại lịch sử"};
        String a = "E:\\Code\\Java\\OOP_Project\\saveddata\\Wikipedia\\logs\\EntityReference\\";
        for (String fileName: listAllFiles(a))
        {
            String qID = fileName.replace(".txt", "");
            for (String craftURL: getAllHref(readFileAll("E:\\Code\\Java\\OOP_Project\\saveddata\\Wikipedia\\logs\\WebHtml\\" + qID +  ".html"))){
                writeFile(a + fileName, craftURL + '\n', true);
            }
            //String path = a + fileName;
            //writeFile(path, "", false);
        }
        /*
        for (String craftURL: getAllHref(readFileAll("E:\\Code\\Java\\OOP_Project\\saveddata\\Wikipedia\\logs\\WebHtml\\Q1010530.html"))){
            writeFile("gg.txt", craftURL + '\n', true);
        }
        */
    }

    public static void trash() throws Exception
    {
        print(System.getProperty("user.dir"));
        writeFile("human.txt", "aa", true);
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
            String path = "E:/Code/Github/VietNamHistory/src/data/" + bigCategory;
            HashSet<String> fileList = listAllFiles(path);
            for (String fileName: fileList)
            {
                //filePaths.add(path + "/" + fileName);
            }
        }
        for (String bigCategory: bigCategories)
        {
            String path = "E:/Code/Java/OOP_Project/saveddata/Wikipedia/data/" + bigCategory;
            HashSet<String> fileList = listAllFiles(path);
            for (String fileName: fileList)
            {
                //filePaths.add(path + "/" + fileName);
            }
        }
        int cnt = 0;
        String path = "E:/Code/Java/OOP_Project/saveddata/Wikipedia/logs/WikiAnalys/EntityFinal/";
        for (String fileName: listAllFiles(path))
        {
            //filePaths.add(path + "/" + fileName);
            //cnt++;
        }
        //print(cnt);
        path = "E:/Code/Java/OOP_Project/saveddata/DBPedia/data";
        for (String fileName: listAllFiles(path))
        {
            //filePaths.add(path + "/" + fileName);
        }
        
        String str = "địa điểm";

        JSONObject freq = new JSONObject();
        JSONObject appear = new JSONObject();
        JSONObject where = new JSONObject();
        /*
        for (String filePath: filePaths)
        {
            if (filePath.contains("nhân vật lịch sử"))
            {
                JSONObject json;
                try{
                    json = getJSONFromFile(filePath);
                }
                catch (Exception e)
                {
                    print(filePath);
                    return;
                }
                if (json.has("claims"))
                {
                    JSONObject claims = json.getJSONObject("claims");
                    for (String key: getAllKeys(claims))
                    {
                        if (!freq.has(key))
                        {
                            String value = claims.getJSONArray(key).getJSONObject(0).getString("value");
                            freq.put(key, 1);
                            appear.put(key, value);
                            where.put(key, filePath);
                        }
                        else{
                            freq.put(key, freq.getInt(key) + 1);
                        }
                    }
                }
            }
        }
        for (String key: getAllKeys(freq))
        {
            writeFile("human-prop.txt", where.getString(key) + '\n', true);
            writeFile("human-prop.txt", "Name: " + key + "\n    Appear: " + Integer.valueOf(freq.getInt(key)) + '\n', true);
            writeFile("human-prop.txt", "    " + key + ": " + appear.getString(key) + '\n', true);
            writeFile("human-prop.txt", "\n", true);
        }
        */
    
        for (String filePath: filePaths)
        {
            if (!filePath.contains("nhân vật lịch sử"))
            {
                //writeFile("non-human.txt", getJSONFromFile(filePath).getString("overview") + "\n" + filePath + "\n\n", true);
            }
        }
        
        String erase = "bài danh sách Wikimedia";
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
                for (String key: getAllKeys(claims))
                {
                    if (key.contains("wiki")||key.contains("Wiki"))
                    {
                        //print(key);
                        claims.remove(key);
                        check = true;
                    }
                }
                if (claims.has(erase))
                {
                    //claims.put(replace, claims.get(erase));
                    //claims.remove(erase);
                    //check = true;
                }
            }
            if (json.has("references"))
            {   
                JSONObject ref = json.getJSONObject("references");
                for (String key: getAllKeys(ref))
                {
                    if (key.contains("wiki")||key.contains("Wiki"))
                    {
                        //print(key);
                        ref.remove(key);
                        check = true;
                    }
                }
                if (ref.has(erase))
                {
                    //ref.put(replace, ref.get(erase));
                    //ref.remove(erase);
                    //check = true;
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
