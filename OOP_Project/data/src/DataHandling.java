import org.json.*;
import org.jsoup.nodes.Document;
import java.net.*;  
import java.io.*;  
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


public class DataHandling implements EntityHandling{
    protected long timeNow = System.currentTimeMillis();
    protected boolean isRelated = false;
    protected HashSet<String> vietnamEntityHashSet = new HashSet<>();
    protected String superpath;
    protected String beginURLsPath;
    protected String craftedURLsPath;
    protected String analysedURLsPath;
    protected String failedURLsPath;
    protected Deque<Pair> deque = new ArrayDeque<>();
    protected HashSet<String> failedURLsHashSet;
    protected HashSet<String> analysedURLsHashSet;
    protected HashMap<String, Integer> craftedURLsHashMap = new HashMap<>();
    protected int totalAnalysed;
    protected int limitAnalysed = 100000;

    public DataHandling(String path)
    {
        superpath = path;
        craftedURLsPath = superpath + "CraftedURLs.txt";
        analysedURLsPath = superpath + "AnalysedURLs.txt";
        failedURLsPath = superpath + "FailedURLs.txt";
        beginURLsPath = superpath + "BeginURLs.txt";
    }

    private int requestRate = 100;
    protected void changeRequestRate(int newRequestRate)
    {
        requestRate = newRequestRate;
    }

    public final StringBuffer getDataFromURL(String urlString) throws Exception {
        
        // sleep for 2 seconds
        // sprint((int)(System.currentTimeMillis() - timeNow));
        try {
            Thread.sleep(Math.max(0,requestRate - (int)(System.currentTimeMillis() - timeNow)));
        } catch (InterruptedException e) {
        }
        timeNow = System.currentTimeMillis();

        URL url = new URL(urlString);

        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        
        int responseCode = connection.getResponseCode();
        StringBuffer response = new StringBuffer("");
        if (responseCode != 200) {
            //throw new Exception("Error response code: " + responseCode);
            return response;
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        
        while ((inputLine = in.readLine()) != null) { 
            response.append(inputLine);
        }
        in.close();
        return response;
    }

    public final JSONObject getJSONFromURL(String urlString) throws Exception {
        StringBuffer response = getDataFromURL(urlString);
        if (response.isEmpty())
        {
            response.append("{}");
        }
        JSONObject myResponse = new JSONObject(response.toString());
        return myResponse;
    }

    

    public final String readFileAll(String fileName) throws IOException
    {
        StringBuffer content = new StringBuffer();
        try(BufferedReader finp = new BufferedReader(new FileReader(fileName)))
        {
            String inputLine;
            while ((inputLine = finp.readLine()) != null)
            {
                content.append(inputLine);
            }
        }
        catch (IOException e)
        {
            throw e;
        }
        return content.toString();
    }

    public final List<String> readFileAllLine(String fileName) throws IOException
    {
        List<String> lines = new ArrayList<>();

        File file = new File(fileName);
        if ((boolean)(file.isFile()) == false){
            return lines;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            System.err.format("IOException: %s%n", e);
        }

        return lines;
    }

    public static boolean fileExist(String fileName)
    {
        File file = new File(fileName);
        if ((boolean)(file.isFile()) == false){
            return false;
        }
        return true;
    }

    public final void writeFile(String fileName, String content, boolean append) throws Exception
    {
        File file = new File(fileName);
        if ((boolean)(file.isFile()) == false){
            try{
                file.createNewFile();
            }
            catch (Exception e)
            {
                throw new Exception("Unable to create file " + fileName);
            }
        }
        try (FileWriter fout = new FileWriter(fileName, append)){
            fout.write(content);
        }
        catch (IOException e)
        {
            throw e;
        }
    }

    public final void print(Object... content)
    {
        for(Object element: content)
        {
            System.out.println(element);
        }
    }

    public final void setAnalyseLimit(int newLimit)
    {
        limitAnalysed = newLimit;
        return;
    }

    public final void getData() throws Exception
    {
        getVietnamRelatedEntity();
        failedURLsHashSet = new HashSet<>(readFileAllLine(failedURLsPath));
        analysedURLsHashSet = new HashSet<>(readFileAllLine(analysedURLsPath));
        totalAnalysed += failedURLsHashSet.size() + analysedURLsHashSet.size();
        if (totalAnalysed > limitAnalysed)
        {
            return;
        }
        List<String> craftedURLsList = readFileAllLine(craftedURLsPath);
        if (craftedURLsList.size()==0)
        {
            String beginURLs = readFileAll(beginURLsPath);
            writeFile(craftedURLsPath, beginURLs + 
            "\n0\n", false);
            deque.addLast(new Pair(beginURLs, 0));
            craftedURLsHashMap.put(beginURLs, 0);
        }
        else
        {
            for (int i = 0; i < craftedURLsList.size(); i+=2)
            {
                String url = craftedURLsList.get(i);
                int depth = Integer.parseInt(craftedURLsList.get(i+1));
                if (checkURL(url) == false) continue;
                if (existInAnalysedURL(url)) continue;
                craftedURLsHashMap.put(url, depth);
                deque.addLast(new Pair(url, depth));
            }
        }

        while(deque.size()!=0)
        {
            int depth = deque.getFirst().second;
            String url = deque.getFirst().first;
            if ( depth <= 3 && totalAnalysed <= 100000)
            {
                entityAnalys(url, depth);
                totalAnalysed++;
            }
            deque.removeFirst();
        }
    }

    public void addRef(String refURL, int depth) throws Exception
    {
        if (craftedURLsHashMap.containsKey(refURL) == false) {
            if (depth < 3)
            {
                deque.add(new Pair(refURL, depth + 1));
                String content = refURL + '\n' + String.valueOf(depth+1)+ '\n';
                writeFile(craftedURLsPath, content, true);
                craftedURLsHashMap.put(refURL, depth + 1);
            }
        }
        return;
    }

    public boolean existInAnalysedURL(String url)
    {
        if (failedURLsHashSet.contains(url)) return true;
        if (analysedURLsHashSet.contains(url)) return true;
        return false;
    }

    public boolean checkURL(String url) throws Exception {
        return false;
    }

    public void getVietnamRelatedEntity() throws Exception {
    }

    @Override
    public Object checkRelated(Document soupHWND) throws Exception {
        throw new UnsupportedOperationException("Unimplemented method 'checkRelated'");
    }

    @Override
    public void entityAnalys(String url, int depth) throws Exception {
        throw new UnsupportedOperationException("Unimplemented method 'entityAnalys'");
    }

}
