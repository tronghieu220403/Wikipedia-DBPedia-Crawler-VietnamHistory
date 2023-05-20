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
    public long timeNow = System.currentTimeMillis();
    public boolean isRelated = false;
    public HashSet<String> vietnamEntityHashSet = new HashSet<>();
    
    public String superpath;

    public String beginURLsPath;
    public String craftedURLsPath;
    public String analysedURLsPath;
    public String failedURLsPath;
    public Deque<Pair> deque = new ArrayDeque<>();
    public HashSet<String> failedURLsHashSet;
    public HashSet<String> analysedURLsHashSet;
    public HashMap<String, Integer> craftedURLsHashMap = new HashMap<>();

    public DataHandling(String path)
    {
        superpath = path;
        craftedURLsPath = superpath + "CraftedURLs.txt";
        analysedURLsPath = superpath + "AnalysedURLs.txt";
        failedURLsPath = superpath + "FailedURLs.txt";    
    }

    public final StringBuffer getDataFromURL(String urlString) throws Exception {
        
        // sleep for 2 seconds
        // sprint((int)(System.currentTimeMillis() - timeNow));
        try {
            Thread.sleep(Math.max(0,200 - (int)(System.currentTimeMillis() - timeNow)));
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
            if (!file.createNewFile()){
                throw new Exception("Unable to create file");
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

    public final void getData() throws Exception
    {
        getVietnamRelatedEntity();
        failedURLsHashSet = new HashSet<>(readFileAllLine(failedURLsPath));
        analysedURLsHashSet = new HashSet<>(readFileAllLine(analysedURLsPath));
        List<String> craftedURLsList = readFileAllLine(craftedURLsPath);
        if (craftedURLsList.size()==0)
        {
            String beginURLs = readFileAll(beginURLsPath);
            writeFile(craftedURLsPath, beginURLs + 
            "\n0\n", false);
            deque.addLast(new Pair(beginURLs, 0));
        }
        else
        {
            for (int i = 0; i < craftedURLsList.size(); i+=2)
            {
                String url = craftedURLsList.get(i);
                int depth = Integer.parseInt(craftedURLsList.get(i+1));
                if (checkURL(url) == false) continue;
                craftedURLsHashMap.put(url, depth);
                if (failedURLsHashSet.contains(url)) continue;
                if (analysedURLsHashSet.contains(url)) continue;
                deque.addLast(new Pair(url, depth));
            }
        }

        while(deque.size()!=0)
        {
            int depth = deque.getFirst().second;
            String url = deque.getFirst().first;
            if ( depth <= 3 )
            {
                entityAnalys(url, depth);
            }
            deque.removeFirst();
        }
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
