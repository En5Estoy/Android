package com.speryans.redbus.classes.connection;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.apache.http.conn.HttpHostConnectException;

import com.speryans.redbus.classes.Commons;
import com.speryans.redbus.classes.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;

public class Connection extends AsyncTask<Object, Void, Object> {
	private static final int MAX_TIMEOUT = (30 * 1000);
	
	private Context context;
	private String url;
	private DoneListener done;
	private CacheListener cache_listener;
	private ErrorListener error;
	
	public static final int FAILED = 499;
	public static final int NO_CONNECTION = 500;
	public static final int TIMEOUT = 501;
	
	public static final String POST = "POST";
	public static final String GET = "GET";
	
	private String type = GET;
	
	private Map<String, String> postValues = new HashMap<String, String>();
	private boolean cache = false;
	
	public Connection(Context context, String url, String type ) {
		this.context = context;
		
		this.url = url;
		this.type = type;
	}
	
	public static Connection get( Context context, String url ) {
		return new Connection(context, url, Connection.GET);
	}
	
	public static Connection post( Context context, String url ) {
		return new Connection(context, url, Connection.POST);
	}
	
	private static String convertStreamToString(InputStream is) {
		BufferedReader reader = 
			new BufferedReader(new InputStreamReader(is), 8192);
		
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return sb.toString();
	}
	
	public Connection param( String key, String value ) {
		postValues.put(key, value);
		
		return this;
	}
	
	public Connection values( HashMap<String, String> values ) {
		postValues = values;
		
		return this;
	}

	@Override
	protected Object doInBackground(Object... params) {
		if( this.type == Connection.GET ) {
			try {
            	URL url = new URL( this.url );
            	HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            	urlConnection.setConnectTimeout(MAX_TIMEOUT);
            	
            	InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            	
            	return Connection.convertStreamToString(in);
            } catch( Exception e ) {
            	return e;
            }
		} else {
			try {
				Commons.info(postValues.toString());
				String parameters = this.toPostContent(postValues);
				
            	URL url = new URL( this.url );
            	HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            	urlConnection.setConnectTimeout(MAX_TIMEOUT);
            	
            	urlConnection.setDoOutput(true);
            	urlConnection.setDoInput(true);
            	
            	urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
            	urlConnection.setRequestProperty("charset", "utf-8");
            	urlConnection.setRequestProperty("Content-Length", "" + Integer.toString(parameters.getBytes().length));
            	
            	DataOutputStream out = new DataOutputStream(urlConnection.getOutputStream());
            	out.write( parameters.getBytes() );
            	out.flush();
            	out.close();

            	Commons.info("Response: "+ urlConnection.getResponseCode() ); 
            	
            	InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            	
            	return Connection.convertStreamToString(in);
            } catch( Exception e ) {
            	return e;
            }
		}
	}
	
	protected void onPostExecute(Object obj) {
		try {
			if( obj == null ) {
				this.error.onError(this.url, Connection.FAILED);
			} else if( obj instanceof HttpHostConnectException ) {
				this.error.onError(this.url, Connection.NO_CONNECTION);
			} else if( obj instanceof TimeoutException || obj instanceof SocketTimeoutException ) {
				this.error.onError(this.url, Connection.MAX_TIMEOUT);
			} else {
				Commons.info("URL: " + this.url);
				Commons.info("Response: " + obj.toString());
				
				if( cache ) this.writeCache((String)obj);

				this.done.ready(this.url, (String)obj);
			}
		} catch( Exception e ) {
			Commons.error("Error on posting", e);
		}
	}
	
	private String toPostContent( Map<String, String> map ) {
		String ret = "";
		int c = 0;
		for( String key : map.keySet() ) {
			ret += key + "=" + map.get(key) + (( c <= (map.size() - 1 ) ) ? "&" : "");
			c++;
		}
		
		return ret;
	}
	
	public Connection cache( CacheListener cl ) {
		cache = true;
		executeCache();
		
		cache_listener = cl;
		
		return this;
	}
	
	private File getCacheDir( ) {
		File cacheDir;
		
		if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            cacheDir=new File(android.os.Environment.getExternalStorageDirectory(), Commons.cacheDir);
        else
            cacheDir=context.getCacheDir();
        if(!cacheDir.exists())
            cacheDir.mkdirs();
        // ------------------------
        
        return cacheDir;
	}
	
	private void writeCache( String obj ) {
		File cacheDir = getCacheDir();
        
        File cache = new File( cacheDir, String.valueOf( this.url.hashCode() ) );
        
        try {
        	Utils.saveFile(cache,  obj.getBytes() );
        } catch( Exception e ) {
        	Commons.error("Cache error for: " + url + ". Write Error: " + e.toString());
        }
	}
	
	private void executeCache() {
		// Verify the cache directory
		File cacheDir = getCacheDir();
        
        File cache = new File( cacheDir, String.valueOf( this.url.hashCode() ) );
        if( cache.exists() && cache_listener != null ) {
        	try {
				String content = new String( Utils.readFile( cache ) );
				
				cache_listener.cacheReady(this.url, content);
			} catch (Exception e) {
				Commons.error("Cache error for: " + url + ". Read Error: " + e.toString());
			}
        }
	}
	
	// New Thread Pool
	@SuppressLint("NewApi")
    public void start() {
		Commons.info("Starting connection for url => " + this.url);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            this.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            this.execute();
        }
	}
	
	// Listener
	public Connection done( DoneListener listener ) {
		this.done = listener;
		
		return this;
	}
	
	public Connection error( ErrorListener listener ) {
		this.error = listener;
		
		return this;
	}
}
