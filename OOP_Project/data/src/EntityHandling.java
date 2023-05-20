/**
 * What to do with an Enity:
 * 1) Get the content of that JSON file from URL.
 * 2) Check if the entity of that JSON file belong to Vietnam.
 * 3) Get all related entities.
 */


import org.json.JSONObject;
import org.jsoup.nodes.Document;

public interface EntityHandling {    
    JSONObject getJSONFromURL(String urlString) throws Exception;
    Object checkRelated(Document soupHWND) throws Exception;
    void entityAnalys(String url, int depth) throws Exception;
}
