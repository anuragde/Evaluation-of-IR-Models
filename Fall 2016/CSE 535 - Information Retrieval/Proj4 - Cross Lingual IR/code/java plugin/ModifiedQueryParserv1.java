package clir;
//imports for connection
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
//imports for twitter data analysis and modification
import org.apache.lucene.*;
import com.memetix.mst.language.*;
import com.memetix.mst.translate.*;
import twitter4j.JSONException;
import twitter4j.JSONObject;
//import for implementing custom plugin
import org.apache.solr.search.ExtendedDismaxQParserPlugin;
import org.apache.solr.search.ExtendedDismaxQParser;
import org.apache.solr.common.params.DisMaxParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QParser;



public class ModifiedQueryParser extends ExtendedDismaxQParserPlugin  {
	

	
		static String ModifiedQuery = null;
/*		static String AzureUname="viveksin";
		static String AzureKey = "9el+H9jfBaMBQmCeziyHw7zJcxH7v6rYMCENP8wA3k0=";*/
		
		
		
		// Modified PF, PF1,Pf2 to boost native languages
		//adding user follower and user listed field to give boost to popular and verified accounts 
		//boosting tweets with more followers and retweet
		static  String[] ModifiedPF = { "text_en^3", "text_es^3", "text_de^3", "text_ru^3", "followers^5","tweet_hashtags", "retweetCount^5"};
		static  String[] ModifiedPF2 ={ "text_en", "text_es", "text_de", "text_ru", "followers^5","tweet_hashtags", "retweetCount^5"};
		static  String[] ModifiedPF3 ={ "text_en^2.5", "text_es^2.5", "text_de^2.5", "text_ru^2.5", "followers^5","tweet_hashtags", "retweetCount^5"};

/*		
	  public static void main(String[] args) throws Exception{
		  	String query="Vivek";
		  	String lang;
		  	ModifiedQueryParser obj = new ModifiedQueryParser();
		   //obj.createParser(query, localParams, params, req)
		    System.out.println("language of query is ");
		    //System.out.println(obj.Translatolang(query));
	  }//end of main
*/	  
      //refer https://lucene.apache.org/solr/4_1_0/solr-core/org/apache/solr/search/ExtendedDismaxQParser.html
	  //constructor details
	  @Override
		public QParser createParser(String qstr,SolrParams localParams,SolrParams params,SolrQueryRequest req){
		  	
		    String AzureUname="viveksin";
			String AzureKey = "9el+H9jfBaMBQmCeziyHw7zJcxH7v6rYMCENP8wA3k0=";
		  	Translate.setClientId(AzureUname);
			Translate.setClientSecret(AzureKey);
			ModifiedQuery=qstr; 
			StringBuilder builder = new StringBuilder();
			String qstr_en	="",qstr_es="",qstr_de="",qstr_ru="";
			builder.append("Start of program input is "+qstr);
			String lang="";
			//usingf bing translator to translate the query
		    ModifiableSolrParams newParameters = new ModifiableSolrParams();
		  	try {
				lang=LangDetect(qstr);
			} catch (IOException | JSONException e) {
				e.printStackTrace();
			}
		  	
		  	builder.append(" language= "+lang);
		  	//if query for a particular hashtag, boost hashtag fields
			if (qstr.contains("#") ){
				ModifiedPF[5]="tweet_hashtags^5";
		  		ModifiedPF2[5]="tweet_hashtags^";
		  		ModifiedPF3[5]="tweet_hashtags^4";
				}
		  	//Boosting native language of the query
		  	if(lang=="en"){
		  		ModifiedPF[0]="text_en^6";
		  		ModifiedPF2[0]="text_en^2";
		  		ModifiedPF3[0]="text_en^4";
		  	}
		  	if(lang=="es"){
		  		ModifiedPF[0]="text_es^6";
		  		ModifiedPF2[0]="text_es^2";
		  		ModifiedPF3[0]="text_es^4";
		  	}
		  	if(lang=="de"){
		  		ModifiedPF[0]="text_de^6";
		  		ModifiedPF2[0]="text_de^2";
		  		ModifiedPF3[0]="text_de^4";
		  	}
		  	if(lang=="ru"){
		  		ModifiedPF[0]="text_ru^6";
		  		ModifiedPF2[0]="text_ru^2";
		  		ModifiedPF3[0]="text_ru^4";
		  	}
		  	newParameters.add(DisMaxParams.QF, ModifiedPF);
		  	newParameters.add(DisMaxParams.PF, ModifiedPF);
		  	newParameters.add(DisMaxParams.PF2, ModifiedPF2);
		  	newParameters.add(DisMaxParams.PF3, ModifiedPF3);
		  	params = SolrParams.wrapAppended(params, newParameters);
		  	
		  	try {
				qstr_en=Translate.execute(qstr,Language.ENGLISH);
			} catch (Exception e) {
				
				e.printStackTrace();
			}
	  		try {
				qstr_de=Translate.execute(qstr, Language.GERMAN);
			} catch (Exception e) {
				
				e.printStackTrace();
			}
	  		try {
				qstr_ru=Translate.execute(qstr, Language.RUSSIAN);
			} catch (Exception e) {
				
				e.printStackTrace();
			}
	  		try {
				qstr_es=Translate.execute(qstr,Language.SPANISH);
			} catch (Exception e) {
				
				e.printStackTrace();
			}
	  		builder.append(" 2. translated querystrings"+qstr_en+" " +qstr_es);
	  		
		  	/*
		  	//translating and modifying query to native language
		  	if(lang=="en"){
		  		builder.append(" query in english");
		  		qstr_en=qstr;
		  		try {
					qstr_es=Translate.execute(qstr,Language.SPANISH);
				} catch (Exception e) {
					
					e.printStackTrace();
				}
		  		try {
					qstr_de=Translate.execute(qstr, Language.GERMAN);
				} catch (Exception e) {
					
					e.printStackTrace();
				}
		  		try {
					qstr_ru=Translate.execute(qstr, Language.RUSSIAN);
				} catch (Exception e) {
					
					e.printStackTrace();
				}
		  		try {
					qstr_es=Translate.execute(qstr, Language.SPANISH);
				} catch (Exception e) {
					
					e.printStackTrace();
				}
		  		
		  		
		  	}
		  	if(lang=="es"){
		  		builder.append(" query in spanish");
		  		qstr_es=qstr;
		  		try {
					qstr_en=Translate.execute(qstr,Language.ENGLISH);
				} catch (Exception e) {
					
					e.printStackTrace();
				}
		  		try {
					qstr_de=Translate.execute(qstr, Language.GERMAN);
				} catch (Exception e) {
					
					e.printStackTrace();
				}
		  		try {
					qstr_ru=Translate.execute(qstr, Language.RUSSIAN);
				} catch (Exception e) {
					
					e.printStackTrace();
				}
		  		
		  	}
		  	if(lang=="de"){
		  		qstr_de=qstr;
			  		try {
						qstr_en=Translate.execute(qstr,Language.ENGLISH);
					} catch (Exception e) {
						
						e.printStackTrace();
					}
			  		try {
						qstr_es=Translate.execute(qstr, Language.SPANISH);
					} catch (Exception e) {
						
						e.printStackTrace();
					}
			  		try {
						qstr_ru=Translate.execute(qstr, Language.RUSSIAN);
					} catch (Exception e) {
						
						e.printStackTrace();
					}
			  		
		  	
		  	}
		  	if(lang=="ru"){
		  		qstr_ru=qstr;
		  		try {
					qstr_en=Translate.execute(qstr,Language.ENGLISH);
				} catch (Exception e) {
					
					e.printStackTrace();
				}
		  		try {
					qstr_es=Translate.execute(qstr, Language.SPANISH);
				} catch (Exception e) {
					
					e.printStackTrace();
				}
		  		try {
					qstr_de=Translate.execute(qstr, Language.GERMAN);
				} catch (Exception e) {
					
					e.printStackTrace();
				}
		  		
		  		
		  		
		  		
		  	}*/
	/*	  	
		  	ModifiedQuery="text_en:\"\"" + qstr_en + "\"\"" + " OR text_de:\"\"" + qstr_de
					+ "\"\"" + " OR text_es:\"\"" + qstr_es + "\"\"" + " OR text_ru:\"\""
					+ qstr_ru + "\"\"";*/
	  		ModifiedQuery="text_en:\"\"" + qstr_en + "\"\"" + " OR text_de:\"\"" + qstr_es;
	  		builder.append(" 3. End of fun modified query= "+ModifiedQuery);
		  	//checking if plugin is working
		  	try {
	            //Whatever the file path is.
	            File statText = new File("/home/ubuntu/solr/statsTest.txt");
	            FileOutputStream is = new FileOutputStream(statText);
	            OutputStreamWriter osw = new OutputStreamWriter(is);    
	            Writer w = new BufferedWriter(osw);
	            w.write(builder.toString());
	            w.flush();
	            w.close();
	        } catch (IOException e) {
	            System.err.println("Problem writing to the file statsTest.txt"); }
		  	
		  	
		  	return new ExtendedDismaxQParser(ModifiedQuery, localParams, params, req);
		  
	  }
/*
	public String Translatolang(String query) throws Exception {
		
		String translated="";
		Translate.setClientId(AzureUname);
		Translate.setClientSecret(AzureKey);
		
		//return Translate.execute(query, Language.ENGLISH, Language.GERMAN);
		return Translate.execute(query, Language.GERMAN, Language.ENGLISH);
	}*/
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
		
		
		 

}//modified query parser class	
	  

