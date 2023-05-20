import java.net.URLEncoder;
import org.jsoup.nodes.Document;

public class DBPediaData extends DataHandling {
    
    public DBPediaData() throws Exception
    {
        super("E:/Code/Java/OOP_Project/saveddata/DBPedia/");
        String string = "Bình";
        String encoded = URLEncoder.encode(string, "UTF-8");

        System.out.println(encoded);

        String chr = "\\u00ec";
        int i = Integer.parseInt(chr.replace("\\u", ""), 16);

        char character = '\u00ec';
        //int chr = (int)character;
        char c = (char)i;
        encoded = URLEncoder.encode(character + "", "UTF-8");

        System.out.println(c);

        writeFile("test.json", (getJSONFromURL("https://dbpedia.org/data/Vietnam.json").toString()), false);
        //writeFile("test.txt",unicodeToURIs("https://en.wikipedia.org/wiki/V\\u00F5_Nguy\\u00EAn_Gi\\u00E1p"),false);
    }

    public String unicodeToURIs(String text) throws Exception
    {
        int start = text.indexOf("/wiki/", 0) + 6;
        if (start == 5)
            start = 0;
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
        return ansPath.toString() + URLEncoder.encode(ansBuffer.toString(),"UTF-8");
    }

    @Override
    public void entityAnalys(String url, int depth) throws Exception {
        throw new UnsupportedOperationException("Unimplemented method 'getReferences'");
    }

    @Override
    public boolean checkURL(String url) {
        if (!url.contains("http://dbpedia.org/resource/"))
        {
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
