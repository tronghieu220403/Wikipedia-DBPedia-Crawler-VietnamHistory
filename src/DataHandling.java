/**
 * What to do with an Data:
 * 1) Get the content of that JSON file from URL.
 * 2) Check if the entity of that JSON file belong to Vietnam.
 * 3) Get all related entities.
 */

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import org.json.JSONObject;

abstract class DataHandling {   
    protected static long timeNow = System.currentTimeMillis();

    private static final String STR_DATE = (new SimpleDateFormat("yyyy-mm-dd hh:mm:ss")).format(Calendar.getInstance().getTime()).replace(':', '-');

    private static int requestRate = 100;
    /**
     * Set up a request gap time for the data crawler to avoid being blocked by the server.
     * @param newRequestRate Gap between each request in {@code Millisecond(ms)}
     * @apiNote The default value is {@code 100ms} for each request.
     */
    public void changeRequestRate(int newRequestRate)
    {
        requestRate = newRequestRate;
    }

    /**
     * Get content of an URL.
     * @param urlString URL to get content.
     * @return Data of the input URL.
     * @throws Exception
     */
    public final static StringBuffer getDataFromURL(String urlString) throws Exception {
        
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

    
    /**
     * Get JSON content of an URL.
     * @param urlString URL to the JSON content.
     * @return A JSON object of the input URL.
     */
    public final static JSONObject getJSONFromURL(String urlString) throws Exception {
        StringBuffer response = getDataFromURL(urlString);
        if (response.isEmpty())
        {
            response.append("{}");
        }
        JSONObject myResponse = new JSONObject(response.toString());
        return myResponse;
    }

    /**
     * Get JSON content of a file.
     * @param filePath The path to the file to be read.
     * @return The JSON content of the file as a JSONObject.
     */
    public final static JSONObject getJSONFromFile(String filePath) throws Exception {
        String content = readFileAll(filePath);
        return new JSONObject(content);
    }
    
    /**
     * Get content of a file.
     * @param filePath The path to the file to be read.
     * @return The content of the file as a single string.
     */
    public final static String readFileAll(String filePath) throws IOException
    {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException("File not found");
        }
        if (!file.canRead()) {
            throw new SecurityException("File cannot be read");
        }
        StringBuffer content = new StringBuffer();
        try(BufferedReader finp = new BufferedReader(new FileReader(filePath)))
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

    /**
     * Get content of all line of a file.
     * @param filePath The path to the file to be read.
     * @return The content of the file as a list of strings.
     */
    public final static List<String> readFileAllLine(String filePath) throws IOException
    {
        List<String> lines = new ArrayList<>();

        if (fileExist(filePath)){
            return lines;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            System.err.format("IOException: %s%n", e);
        }

        return lines;
    }

    /**
     * Check if a file exists.
     * @param filePath The path to the file to be checked.
     * @return If the file exists, return {@code true}; otherwise, return {@code false}.
     */
    public final static boolean fileExist(String filePath)
    {
        File file = new File(filePath);
        if ((boolean)(file.isFile()) == false){
            return false;
        }
        return true;
    }

    /**
     * Wirte to a file.
     * @param filePath The path to the file is written to.
     * @param content The content you want to write in.
     * @param append Set the value to true if you want to write your content to the end of the file, or false if you want to overwrite it.
     * @apiNote This method will automatically create a file if one does not exist.
     */
    public final static void writeFile(String filePath, String content, boolean append) throws Exception
    {
        filePath = getFullPath(filePath);

        File file = new File(filePath);
        if ((boolean)(file.isFile()) == false){
            try{
                file.createNewFile();
            }
            catch (Exception e)
            {
                throw new Exception("Unable to create file " + filePath);
            }
        }
        else {
            String s = readFileAll(filePath);
            if (s.equals(content) && append == false) return;
        }
        writeToLogs(filePath);
        try (FileWriter fout = new FileWriter(filePath, append)){
            fout.write(content);
        }
        catch (IOException e)
        {
            throw e;
        }
    }

    private final static void writeFile(String filePath, String content) throws Exception
    {
        filePath = getFullPath(filePath);
        
        File file = new File(filePath);
        if ((boolean)(file.isFile()) == false){
            try{
                file.createNewFile();
            }
            catch (Exception e)
            {
                throw new Exception("Unable to create file " + filePath);
            }
        }
        try (FileWriter fout = new FileWriter(filePath, false)){
            fout.write(content);
        }
        catch (IOException e)
        {
            throw e;
        }
    }

    /**
     * Creates the directory named by this abstract pathname.
     */
    public final static void createFolder(String folderPath)
    {
        folderPath = getFullPath(folderPath);
        File folder = new File(folderPath);
        if (!folder.exists()) {  
            // Folder does not exist, create it    
            folder.mkdirs();
        }
    }

    /**
     * Print anything.
     */
    public final static void print(Object... content)
    {
        for(Object element: content)
        {
            System.out.println(element);
        }
    }

    /**
     * Decode URL string
     * @param urlString
     * @return Decode string for that url.
     */
    public final static String urlDecode(String urlString) throws Exception
    {
        return java.net.URLDecoder.decode(urlString, StandardCharsets.UTF_8.name());
    }

    /**
     * Decode Unicode string
     * @param text The string to be decoded.
     * @return Decoded string.
     * @Example String "\u003d" will be convert to "=";
     */
    public final static String unicodeDecode(String text)
    {
        int start = 0;
        int id = 0;
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
        return ansBuffer.toString();
    }

    /**
     * List all files in a folder.
     * @param folderPath Path to folder.
     * @return a {@code HashSet<String>} of file names.
     */
    public final static HashSet<String> listAllFiles(String folderPath)
    {
        HashSet<String> list = new HashSet<String>();
        File folder = new File(folderPath);
        File[] files = folder.listFiles();

        // List all files in a folder.
        for (File file : files) {
            if (file.isFile()) {
                list.add(file.getName());
            }
        }
        return list;
    }

    /**
     * Check if a string contains upper case character.
     * @param str
     * @return return {@code true} if the string contains upper case character; otherwise, return {@code false}.
     */
    public final static boolean containsUpperCase(String str) {
        for (int i = 0; i < str.length(); i++) {
          if (Character.isUpperCase(str.charAt(i))) {
            return true;
          }   
        }
        return false;    
    }

    /**
     * Copy all files in {@code srcFolder} to {@code tarFolder}.
     * @param srcFolder
     * @param tarFolder
     * @throws Exception
     */
    public final static void copyFilesInFolder(String srcFolder, String tarFolder) throws Exception {
        HashSet<String> srcFiles = listAllFiles(srcFolder);
        for (String fileName: srcFiles)
        {
            writeFile(tarFolder + "/" + fileName, readFileAll(srcFolder+"/" + fileName), false);
        }
    }
    
    private final static String writeToLogs(String filePath) throws Exception
    {
        filePath = getFullPath(filePath);
        String s = readFileAll(filePath);
        String fileName = filePath.substring(filePath.lastIndexOf("\\")+1);
        String logsPath = filePath.substring(0,filePath.lastIndexOf("\\")+1) + "logs"+ "\\";
        createFolder(logsPath);
        createFolder(logsPath + STR_DATE);
        //print(logsPath + STR_DATE + "\\" + fileName);
        writeFile(logsPath + STR_DATE + "\\" + fileName, s);
        return logsPath;
    }

    /**
     * 
     */
    private final static String getFullPath(String filePath)
    {
        filePath = filePath.replace("/", "\\");
        if (!filePath.contains(":")){
            filePath = System.getProperty("user.dir").replace('/', '\\') + "\\" + filePath;
        }
        return filePath;
    }

    /**
     * Delete a file.
     * @param filePath
     */
    public final static void deleteFile(String filePath) throws Exception
    {
        filePath = getFullPath(filePath);
        if (!fileExist(filePath))
            return;
        writeToLogs(filePath);
        File myObj = new File(filePath); 
        myObj.delete();
    }

    /**
     * Move a file from one directory to another.<p>
     * Examples: <p>
     * <blockquote><pre>
     * moveFile("/source/Q1.json", "/destination/Q1.json");
     * </pre></blockquote>
     *
     * @param srcFilePath Path of the file to move.
     * @param tarFilePath Path of the file to move it to.
     * @throws Exception
     */
    public final static void moveFile(String srcFilePath, String tarFilePath) throws Exception
    {
        if (fileExist(tarFilePath))
            return;
        Files.move(Paths.get(srcFilePath), Paths.get(tarFilePath));
    }

    /*
     * Get all string keys of a JSONObject object.
     */
    public final static HashSet<String> getAllKeys(JSONObject jsonObject)
    {
        return new HashSet<>(jsonObject.keySet());
    }

    
}
