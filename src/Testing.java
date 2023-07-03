import java.util.*;

import org.json.*;


public class Testing extends DataHandling {

    public static String dataFolderPath = "/data/";
    public static final String[] BIG_CATEGORIES = {"triều đại lịch sử","địa điểm du lịch, di tích lịch sử", "lễ hội văn hóa", "sự kiện lịch sử", "nhân vật lịch sử"};

    public static void main(String[] args) throws Exception {
        new Testing();
    }

    HashSet<String> entityHashSet = new HashSet<>();

    Testing() throws Exception{

        // Chỉnh đường dẫn đến thư mục chứa data.
        dataFolderPath = "data/";
        
        
        for (String cat: BIG_CATEGORIES)
        {
            for (String fileName: listAllFiles(dataFolderPath + cat))
            {
                entityHashSet.add(fileName.replace(".json", ""));
            }
        }

        // Vùng viết code nằm dưới đây.


        /*
         * Tìm text trong overview và description
         * 
         * biến selection: đặt value ở vị trí nào bằng 0 thì không chọn vị trí đó trong BIG_CATEGORIES, có thể hiểu:
         * 11011: không chọn nhân vật lịch sử
         * 11011: không chọn triều đại lịch sử
         * Cuối file viết có liệt kê tất cả các thực thể, tiện copy luôn.
         */
        //findByOverview("biên tập viên", 11111, "filter/dien_vien.txt");
        /*
         * lấy tất cả các thực thể có thuộc tính với giá trị xác định
         * Cuối file viết có liệt kê tất cả các thực thể, tiện copy luôn.
         */
        //findByProp("quốc gia", "Pháp", "france.txt", false);
        //findByProp("quốc tịch", "", 1, "filter/quốc tịch.txt", true);
        /*
         * lấy tất cả các giá trị của một thuộc tính
         */
        //getAllValueOfProp("quốc gia", "filter/quoc_gia.txt");
        //getAllValueOfProp("quốc gia xuất xứ", "filter/quoc_gia_xuat_xu.txt");
        //getAllValueOfProp("là một", "filter/instance.txt");
        //getAllValueOfProp("vị trí trong đội", "filter/trash.txt");
        /*
         * Lấy tất cả thuộc tính:
         */
        //getAllProp("prop.txt");
         // Vùng viết code nằm trên đây.

    }

    private void getAllProp(String fileWrite) throws Exception{
        StringBuffer s = new StringBuffer();
        HashSet<String> prop = new HashSet<>();
        
        for (String cat: BIG_CATEGORIES)
        {
            for (String fileName: listAllFiles(dataFolderPath + cat)){
                if (fileName.contains("X")) continue;
                JSONObject json = getJSONFromFile(dataFolderPath+cat+"/"+fileName);
                JSONObject claims = json.getJSONObject("claims");
                for (String propName: getAllKeys(claims))
                    if (!prop.contains(propName))
                    {
                        JSONArray arr = claims.getJSONArray(propName);
                        JSONObject obj = arr.getJSONObject(0);
                        s.append("\"" + propName + "\": \"\",").append("\n");
                        s.append(obj.getString("value")).append("\n");
                        s.append("https://www.wikidata.org/wiki/" + fileName.replace(".json", "")).append("\n");
                        s.append("\n\n");
                        prop.add(propName);
                    }
            }
        }
        writeFile(fileWrite, s.toString(), false);

    }

    void getAllValueOfProp(String propName, String fileWrite) throws Exception{
        StringBuffer s = new StringBuffer();
        HashSet<String> gg = new HashSet<>();
        for (String cat: BIG_CATEGORIES)
        {
            for (String fileName: listAllFiles(dataFolderPath + cat)){
                if (fileName.contains("X")) continue;
                JSONObject json = getJSONFromFile(dataFolderPath+cat+"/"+fileName);
                JSONObject claims = json.getJSONObject("claims");
                if (claims.has(propName))
                {
                    JSONArray arr = claims.getJSONArray(propName);
                    for (int i = 0; i < arr.length(); i++)
                    {
                        JSONObject obj = arr.getJSONObject(i);
                        gg.add(obj.getString("value"));
                    }
                }
            }
        }
        for (String f: gg)
        {
            s.append(f).append("\n");
        }
        writeFile(fileWrite, s.toString(), false);

    }

    void findByProp(String propName, String propValue, int selection, String fileWrite, boolean printOverview) throws Exception
    {
        StringBuffer s = new StringBuffer();
        StringBuffer gg = new StringBuffer();
        int index = 4;
        while(selection > 0)
        {
            if (selection % 10 == 1)
            {
                for (String fileName: listAllFiles(dataFolderPath + BIG_CATEGORIES[index]))
                {
                    if (fileName.contains("X")) continue;
                    JSONObject json = getJSONFromFile(dataFolderPath+BIG_CATEGORIES[index]+"/"+fileName);
                    JSONObject claims = json.getJSONObject("claims");
                    if (propValue.isEmpty() && !claims.has(propName))
                    {
                        s.append(json.getString("label")).append("\n");
                        if (printOverview)
                        {
                            s.append(json.getString("overview") + '\n');
                        }
                        s.append(json.getString("id") + "\n");
                        s.append("https://www.wikidata.org/wiki/" + fileName.replace(".json", "") + "\n\n");
                        gg.append(fileName.replace(".json", "") + '\n');
                        continue;
                    }
                    
                    if (claims.has(propName))
                    {
                        JSONArray arr = claims.getJSONArray(propName);
                        for (int i = 0; i < arr.length(); i++)
                        {
                            JSONObject obj = arr.getJSONObject(i);
                            if (obj.getString("value").equals(propValue))
                            {
                                if (printOverview)
                                {
                                    s.append(json.getString("overview") + '\n');
                                }
                                s.append(json.getString("id") + "\n");
                                s.append("https://www.wikidata.org/wiki/" + fileName.replace(".json", "") + "\n\n");
                                gg.append(fileName.replace(".json", "") + '\n');
                                break;
                            }
                        }
                    }
                }
            }
            selection = selection / 10;
            index--;
        }
        writeFile(fileWrite, s.toString() + "\n\n\n" + gg.toString(), false);
    }

    void findByOverview(String txt, int selection, String fileWrite) throws Exception
    {
        int index = 4;
        StringBuffer s = new StringBuffer();
        StringBuffer gg = new StringBuffer();
        while(selection > 0)
        {
            if (selection % 10 == 1)
            {
                for (String fileName: listAllFiles(dataFolderPath + BIG_CATEGORIES[index])){
                    if (fileName.contains("X")) continue;
                    JSONObject json = getJSONFromFile(dataFolderPath + BIG_CATEGORIES[index] + "/" + fileName);
                    String overview = json.getString("overview");
                    if (json.has("description"))
                    {
                        String des = json.getString("description");
                        if (des.contains(txt))
                        {
                            s.append(overview + '\n');
                            s.append("https://www.wikidata.org/wiki/" + fileName.replace(".json", "") + "\n\n");
                            gg.append(fileName.replace(".json", "") + '\n');
                            continue;
                        }
                    }
                    if (overview.contains(txt))
                    {
                        s.append(overview + '\n');
                        s.append("https://www.wikidata.org/wiki/" + fileName.replace(".json", "") + "\n\n");
                        gg.append(fileName.replace(".json", "") + '\n');
                        continue;
                    }
                }
            }
            selection = selection / 10;
            index--;
        }
        writeFile(fileWrite, s.toString() + "\n\n\n" + gg.toString(), false);
    }
}
