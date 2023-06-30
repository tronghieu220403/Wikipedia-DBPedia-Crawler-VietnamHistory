import java.util.*;

import org.json.*;


public class Testing extends DataHandling {

    public static String dataFolderPath = "/data/";
    public static final String[] BIG_CATEGORIES = {"địa điểm du lịch, di tích lịch sử", "lễ hội văn hóa", "nhân vật lịch sử", "sự kiện lịch sử", "triều đại lịch sử"};

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
        findByOverview("nhạc sĩ", 11111, "nhac_si.txt");
        /*
         * lấy tất cả các thực thể có thuộc tính với giá trị xác định
         * Cuối file viết có liệt kê tất cả các thực thể, tiện copy luôn.
         */
        findByProp("quốc gia", "Pháp", "france.txt", false);
        /*
         * lấy tất cả các giá trị của một thuộc tính
         */
        getAllValueOfProp("quốc gia", "quoc_gia.txt");


         // Vùng viết code nằm trên đây.
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

    void findByProp(String propName, String propValue, String fileWrite, boolean printOverview) throws Exception
    {
        StringBuffer s = new StringBuffer();
        StringBuffer gg = new StringBuffer();
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
