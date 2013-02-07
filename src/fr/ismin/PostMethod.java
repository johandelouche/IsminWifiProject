package fr.ismin;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.SSLHandshakeException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;


/*
 * PostMethod is an AsyncTask because network task has to be run in different thread 
 * in order to not freeze the UI one.
 */
public class PostMethod extends AsyncTask <String, Integer, Long> {

	/*
	 * Objects used during the process
	 */
	private ClientConnectionManager clientConnectionManager;
	private HttpParams params;
	private HttpClient httpClient;
	private HttpPost httpPost;
	private HttpResponse response;
	
	/*
	 * Strings used in the source
	 */
	private String username;
	private String password;
	private static String CMD = "authenticate";
	private static String SUBMIT = "Log+In";
	
	
	/*
	 * The application context (MainActivity)
	 */
	Context context;
	
	public PostMethod(Context context) {
		this.context = context;
	}
	
	@Override
	protected Long doInBackground(String... param) {
		
		/*
		 * The Looper permits to have handler in a thread which is not the main one.
		 */
		Looper.prepare();
		
		/*
		 * The parameters to connect to WiFi portal
		 */
		username = param[0];
		password = param[1];
		
		setParams();
		post();
    
        return null;
	}
	
	
	@Override
    protected void onPostExecute(Long result) {
        
		/*
		 * Called when the doInBackGround is finished.
		 * Response should help to managed errors but most of the time
		 * the server do not send any response and an exception emerge 
		 */
		if (response == null) {
			Log.i(MainActivity.TAG, "echec d'envoie de la requête");
		}
		else {
			Log.i(MainActivity.TAG, "requete envoyee");
			Toast.makeText(context, context.getString(R.string.connection_succeeded), Toast.LENGTH_LONG).show();
		}
		
    }
	
	/*
	 * Set all the parameters needed to post the method.
	 */
	private void setParams() {
		
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		schemeRegistry.register(new Scheme("https", new ISMINSSLSocketFactory(), 443));
		
		params = new BasicHttpParams();
        params.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 1);
        params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, new ConnPerRouteBean(1));		
        params.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);		
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);		
        HttpProtocolParams.setContentCharset(params, "utf8");		
        
        clientConnectionManager = new ThreadSafeClientConnManager(params, schemeRegistry);
        
        httpClient = new DefaultHttpClient(clientConnectionManager, params);

        /*
         * Those one define my post method. The server and the form parameters
         */
        httpPost = new HttpPost("https://securelogin.arubanetworks.com/cgi-bin/login");
        List <NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("user",username));
        nameValuePairs.add(new BasicNameValuePair("password",password));
        nameValuePairs.add(new BasicNameValuePair("cmd",CMD));
        nameValuePairs.add(new BasicNameValuePair("Login",SUBMIT));

        try {
        	httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        } catch (UnsupportedEncodingException e) {
        	e.printStackTrace();	
        }
	}
	


	private void post() {
		
		/*
		 * The post execution
		 */
		 try {
	        	response = httpClient.execute(httpPost);
	        } 
		 	catch (ClientProtocolException e) {
	        	e.printStackTrace();
	        	Toast.makeText(context, context.getString(R.string.problem), Toast.LENGTH_SHORT).show();
	        } 
	        catch (SSLHandshakeException e) {
	        	e.printStackTrace();
	        	Toast.makeText(context, context.getString(R.string.problem), Toast.LENGTH_SHORT).show();	
	        }
	        catch (IOException e) {	
	        	e.printStackTrace();
	        	Toast.makeText(context, context.getString(R.string.problem), Toast.LENGTH_SHORT).show();			
	        } 
	        catch (Exception e) {
	        	e.printStackTrace();
	        	Toast.makeText(context, context.getString(R.string.problem), Toast.LENGTH_SHORT).show();  
	        }	
	}
}
