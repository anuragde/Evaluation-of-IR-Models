package clir;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/*import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;*/
import org.apache.lucene.*;
import com.memetix.mst.language.*;
import com.memetix.mst.translate.*;
import twitter4j.JSONException;
import twitter4j.JSONObject;
public class Search {
	static String AzureUname="viveksin";
	static String AzureKey = "9el+H9jfBaMBQmCeziyHw7zJcxH7v6rYMCENP8wA3k0=";
	
  public static void main(String[] args) throws Exception{
	  	String query="I want to eat food";
	  	String lang;
	    Search obj = new Search();
	    lang=obj.LangDetect(query);
	    System.out.println("language of query is "+lang);
	    System.out.println(obj.Translatolang(query));
	
  

}

public String Translatolang(String query) throws Exception {
	
	String translated="";
	Translate.setClientId(AzureUname);
	Translate.setClientSecret(AzureKey);
	
	return Translate.execute(query, Language.ENGLISH, Language.GERMAN);
}
public String LangDetect(String text) throws IOException, JSONException{
	   
	    String charSet = java.nio.charset.StandardCharsets.UTF_8.name();  
	    String url="http://ws.detectlanguage.com/0.2/detect";
	    String querytext=text;
	    String LANG_DETECTION_API_KEY = "7bd4928c9e29cc17138d3c810f39c3a8";
	    String JsonresponseLang="";
	    String language = null;
	  	
	  	String query=String.format("q=%s&key=%s", URLEncoder.encode(querytext, charSet),URLEncoder.encode(LANG_DETECTION_API_KEY, charSet));
	  	JsonresponseLang=fetchHTTPData(url,query);
	  	
	  	if (JsonresponseLang==""){
	  		System.out.println("No response from Language detection server...");
	  	}else language = extractLanguage(JsonresponseLang);
		
			
	  	return language;
   }
  
	private String extractLanguage(String jsonresponseLang) throws JSONException {
		
		String language = "";
		JSONObject jsontext;
		

		jsontext = new JSONObject(jsonresponseLang);
		language=jsontext.getJSONObject("data").getJSONArray("detections").getJSONObject(0).get("language").toString();
		return language;
}

	public String fetchHTTPData(String URL, String query) throws IOException {
		String charSet;
		String response = "";
		int responseCode = 0;
		String USER_AGENT = "Mozilla/5.0";
		HttpURLConnection httpConn = (HttpURLConnection) new URL(URL + "?" + query).openConnection();
		//System.out.println("httpConn : " + httpConn);
		charSet = java.nio.charset.StandardCharsets.UTF_8.name();

		httpConn.setDoOutput(true);
		httpConn.setRequestProperty("Accept-Charset", charSet);
		httpConn.setRequestProperty("User-Agent", USER_AGENT);
		httpConn.setRequestProperty("Content-type", "text/json; charset=utf-8");

		responseCode = httpConn.getResponseCode();

		if (responseCode == 200) {
			BufferedReader in = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
			String inputLine;
			StringBuffer responseBuffer = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				responseBuffer.append(inputLine);
			}

			in.close();
			response = responseBuffer.toString();
		}
        
		return response;
   }
}	
  
