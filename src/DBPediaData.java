//import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

public class DBPediaData extends EntityHandling {
    
    public DBPediaData() throws Exception
    {
        super("E:/Code/Java/OOP_Project/saveddata/DBPedia/");
        changeRequestRate(100);
    }

    public static void main(String[] args) throws Exception {
        DBPediaData dbpediaData = new DBPediaData();
        //dbpediaData.getData();
        dbpediaData.syncData();
    }

    /**
     * Convert the URL to their true form to be accessed by the Internet.
     */
    @Override
    public String filterURL(String urlString) throws Exception
    {
        int start = 0;
        int id = 0;
        for ( int i = 0; i < 4; i++ )
        {
            id = urlString.indexOf("/",start);
            start = id + 1;
        }
        String rootURL = urlString.substring(0, start);
        String name = unicodeDecode(urlString.replace(rootURL, ""));
        return rootURL + unicodeDecode(name);
    }

    /**
     * Analize an entity to make sure it is related to Vietnam and write it to logs.<p>
     * Get the JSON content of this URL.
     * @apiNote The entity data is in "EntityJson" folder. 
     */
    @Override
    protected void entityAnalys(String url, int depth) throws Exception {
        if (checkURL(url)==false) return;
        url = filterURL(url);
        url = url.replace("http:", "https:");
        if (url.contains("/resource/"))
        {
            url = url.replace("/resource/","/data/");
            url = url + ".json";
        }

        String entityName = url.replace("https://dbpedia.org/data/", "");
        String content;
        if (fileExist(superpath + "EntityJson/" + entityName) == true)
        {
            if (!existInAnalysedURL(url))
            {
                writeFile(analysedURLsPath, url + '\n', true);
            }
        }
        else
        {
            content = getDataFromURL(url).toString();
            // Check related
            if (checkRelated(content) == false)
            {
                writeFile(failedURLsPath, url + '\n', true);
                return;
            }

            writeFile(superpath + "EntityJson/" + entityName, content , false);
            if (!existInAnalysedURL(url))
            {
                writeFile(analysedURLsPath, url + '\n', true);
            }
        }

        int strBegin = 0;
        int strEnd = 0;
        
        content = readFileAll(superpath + "EntityJson/" + entityName);
        while(true)
        {
            strBegin = content.indexOf("http://dbpedia.org/resource/", strEnd);
            if (strBegin == -1) break;
            strEnd = content.indexOf("\"", strBegin);
            if (strEnd == -1 ) break;
            String refURL = content.substring(strBegin, strEnd);
            refURL.replace("http:", "https:");
            if (checkURL(refURL)==false) continue;
            refURL = refURL.replace("http:", "https:");
            if (refURL.contains("/resource/"))
            {
                refURL = refURL.replace("/resource/","/data/");
                refURL = refURL + ".json";
            }
            refURL = filterURL(refURL);
            addURLToCrafed(refURL, depth);
        }
        return;
    }

    private HashSet<Character> bannedChr = new HashSet<>(Arrays.asList( '/', '\\', '?', '*', ':', '>', '<', '|', '\"'));
    
    @Override
    public boolean checkURL(String url) throws Exception {
        url = url.replace("http:", "https:");
        if (!url.contains("https://dbpedia.org/resource/"))
        {
            if (!url.contains("https://dbpedia.org/data/"))
                return false;
        }
        if (url.chars().filter(ch -> ch == ':').count() > 1) {
            return false;
        }
        int index = 0;
        for ( int i = 0; i < 4; i++ )
        {
            index = url.indexOf("/",index) + 1;
        }
        String name = unicodeDecode(url.replace(url.substring(0, index), ""));

        for (char c: bannedChr)
        {
            if (name.contains(Character.toString(c)))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if the entity is related to Vietnam.
     * @param data String content of DBPedia JSON item.
     * @return Return {@code true} if it is related; otherwise return {@code false}.
     */
    @Override
    public boolean checkRelated(String data) throws Exception {
        for (String vietnamEntity: vietnamEntityHashSet)
        {
            if (((String) data).contains(vietnamEntity))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void getVietnamRelatedEntity() throws Exception {
        vietnamEntityHashSet.clear();
        vietnamEntityHashSet.add("http://dbpedia.org/resource/Vietnam");
    }
    
    /**
     * Get all properties of all entities and save it to folder "Properties".
     * @throws Exception
     */
    @Override
    protected void getProperties() throws Exception
    {
        if (fileExist(superpath + "PropertiesList.json"))
        {
            JSONArray myJsonArray = new JSONArray(readFileAll(superpath + "PropertiesList.json"));
            for (int i = 0; i < myJsonArray.length(); i++) { 
                propertyHashSet.add((String)myJsonArray.get(i));
            }
        }
        else
        {
            HashSet<String> entityFileList = listAllFiles(entityJsonPath);
            for (String fileName: entityFileList)
            {
                if (isFileExists(entityJsonPath + "/" + fileName))
                {
                    getPropertiesInJson(entityJsonPath,fileName, entityFileList);
                }
            }
            writeFile(superpath + "PropertiesList.json", (new JSONArray(propertyHashSet)).toString(), false);
        }
    }

    private void getPropertiesInJson(String entityJsonPath, String fileName, HashSet<String> entityFileList) {
        
    }

    protected void syncData() throws Exception
    {
        String[] bigCategories = {"địa điểm du lịch, di tích lịch sử", "lễ hội văn hóa", "nhân vật lịch sử", "sự kiện lịch sử", "triều đại lịch sử"};
        StringBuffer sb = new StringBuffer();
        HashSet<String> qIDHashSet = new HashSet<>();
        for (String bigCategory: bigCategories)
        {
            String path = "export/" + bigCategory;
            HashSet<String> fileList = listAllFiles(path);
            for (String fileName: fileList)
            {
                qIDHashSet.add(fileName.replaceAll(".json",""));
            }
        }
        String dbFolder = superpath + "EntityJson";
        HashSet<String> files = listAllFiles(dbFolder);
        

    }
}
