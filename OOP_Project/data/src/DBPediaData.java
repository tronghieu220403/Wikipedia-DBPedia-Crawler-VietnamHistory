import java.net.URLEncoder;
import org.jsoup.nodes.Document;
public class DBPediaData extends DataHandling {
    
    public DBPediaData() throws Exception
    {
        super("E:/Code/Java/OOP_Project/saveddata/DBPedia/");
        changeRequestRate(100);
    }

    public String unicodeToURI(String text) throws Exception
    {
        int start = text.lastIndexOf('/', text.length()) + 1;
        int id = 0;
        StringBuffer ansPath = new StringBuffer(text.substring(0, start));
        StringBuffer ansBuffer = new StringBuffer();
        while(true) {
            id = text.indexOf("\\u", start);
            if(id == -1) {
                ansBuffer.append(text.substring(start));
                break;
            }
            ansBuffer.append(text.substring(start, id));
            int i = Integer.parseInt((text.substring(id, id + 6)).replace("\\u", ""), 16);
            ansBuffer.append((char)i);
            start = id + 6;
        }
        ansPath.append(ansBuffer);
        return ansPath.toString();
    }

    @Override
    public void entityAnalys(String url, int depth) throws Exception {
        if (checkURL(url)==false) return;
        url = unicodeToURI(url);
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
            if (!content.contains("http://dbpedia.org/resource/Vietnam"))
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
            refURL = unicodeToURI(refURL);
            addRef(refURL, depth);
        }
    }

    @Override
    public boolean checkURL(String url) {
        url = url.replace("http:", "https:");
        if (!url.contains("https://dbpedia.org/resource/"))
        {
            if (!url.contains("https://dbpedia.org/data/"))
                return false;
        }
        if (url.chars().filter(ch -> ch == ':').count() > 1) {
            return false;
        }
        return true;
    }

    @Override
    public Object checkRelated(Document soupHWND) throws Exception {
        throw new UnsupportedOperationException("Unimplemented method 'checkRelated'");
    }

    @Override
    public void getVietnamRelatedEntity() throws Exception {
        
    }

    public static void main(String[] args) throws Exception {
        DBPediaData dbpediaData = new DBPediaData();
        dbpediaData.getData();
    }
    
}
