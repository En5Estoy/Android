package com.speryans.redbus.classes;

import java.util.Iterator;

import com.speryans.redbus.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.webkit.CookieManager;
import android.widget.TextView;

public class Commons {
	// Tag to recognice the app in LogCat
	public static final String TAG = "com.speryans.redbus";
	public static final String cacheDir = "com.speryans.redbus";
	
	//public static String UDID = null;
	
	public static final boolean DEBUG = false;
	
	public static void dialog( Activity context, String msg ) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setInverseBackgroundForced(true);
		builder.setCancelable(true);
		//builder.setMessage(msg);
		TextView view = (TextView)LayoutInflater.from(context).inflate(R.layout.workarounddialogs, null, false);
		view.setText(msg);
		builder.setView( view );
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
		});
		
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	public static void createHeader( Activity c ) {
		Typeface icons = Typefaces.get(c, "fonts/ionicons.ttf");
		
		TextView iconHeader = (TextView) c.findViewById(R.id.iconHeader);
		iconHeader.setTypeface(icons);
		
		TextView shareBtn = (TextView) c.findViewById(R.id.shareBtn);
		shareBtn.setTypeface(icons);
	}
	
	public static void info( String msg ) {
		if( DEBUG ) {
			Log.i(Commons.TAG, msg);
		}
	}
	
	public static void error( String msg ) {
		if( DEBUG ) {
			Log.e(Commons.TAG, msg);
		}
	}
	
	public static void error( String msg, Throwable t ) {
		if( DEBUG ) {
			Log.e(Commons.TAG, msg, t);
		}
	}
	
	public static void debug( String msg ) {
		if( DEBUG ) {
			Log.d(Commons.TAG, msg);
		}
	}
	
	public static String getCookie(String url ) {
    	Commons.info("URL: " + url);
		
		String mCookie = CookieManager.getInstance().getCookie(url);
		
		Commons.info("Cookie Result: " + mCookie);
		
		return mCookie;
    }
	
	/**
	 * Solo para depuraci�n.
	 * 
	 * Permite imprimir en el log todos los parametros que vienen cargados en un Intent,
	 * mediante su bundle.
	 * 
	 * @param a Intent que contiene el Bundle cargado de datos.
	 */
	public static void printExtras( Bundle bundle ) {
		if( DEBUG ) {
			/*
	    	 * Only for test
	    	 * 
	    	 * Print all extras
	    	 * 
	    	 */
	    	try {
	    		Iterator<String> keys = bundle.keySet().iterator();
	    		
	    		for( ; keys.hasNext() ; ) {
	    			String key = keys.next();
	    			Log.i( Commons.TAG, " -> " + key + ": " + bundle.get(key) );
	    		}
	    	} catch( Exception e ) {
	    		Log.e(Commons.TAG, "Error reading tags: " + e.toString() );
	    	}
		}
	}
	
	/**
	 * Solo para depuraci�n.
	 * 
	 * Permite imprimir en el log todos los parametros que vienen cargados en un Intent,
	 * mediante su bundle.
	 * 
	 * @param a Activity con el Intent que contiene el Bundle cargado de datos.
	 */
	public static void printExtras( Activity a ) {
		/*
    	 * Only for test
    	 * 
    	 * Print all extras
    	 * 
    	 */
    	try {
    		Bundle bundle = a.getIntent().getExtras();
    		Commons.printExtras(bundle);
    	} catch( Exception e ) {
    		Log.e(Commons.TAG, "Error reading tags: " + e.toString() );
    	}
	}
	
	/**
	 * Solo para depuraci�n.
	 * 
	 * Permite imprimir en el log todos los parametros que vienen cargados en un Intent,
	 * mediante su bundle.
	 * 
	 * @param a Activity con el Intent que contiene el Bundle cargado de datos.
	 */
	public static void printExtras( Intent a ) {
		/*
    	 * Only for test
    	 * 
    	 * Print all extras
    	 * 
    	 */
    	try {
    		Bundle bundle = a.getExtras();
    		Commons.printExtras(bundle);
    	} catch( Exception e ) {
    		Log.e(Commons.TAG, "Error reading tags: " + e.toString() );
    	}
	}
	
	
	/**
	 * Permite eliminar toda la informaci�n del un Bundle.
	 * Util cuando necesitamos saber si la informaci�n se pisa.
	 * 
	 * @param a
	 */
	public static void removeExtras( Activity a ) {
		/*
    	 * Only for test
    	 * 
    	 * Print all extras
    	 * 
    	 */
    	try {
    		Log.i(Commons.TAG, "Removing all extras of " + a.getLocalClassName() );
    		Bundle bundle = a.getIntent().getExtras();
    		Iterator<String> keys = bundle.keySet().iterator();
    		
    		for( ; keys.hasNext() ; ) {
    			String key = keys.next();
    			a.getIntent().removeExtra( key );
    			Log.i( Commons.TAG, " -> " + key + ": REMOVED");
    		}
    	} catch( Exception e ) {
    		Log.e(Commons.TAG, "Error removing tags: " + e.toString() );
    	}
	}
	
	/**
	 * Permite eliminar toda la informaci�n del un Bundle.
	 * Util cuando necesitamos saber si la informaci�n se pisa.
	 * 
	 * @param a
	 */
	public static void removeExtras( Intent a ) {
		/*
    	 * Only for test
    	 * 
    	 * Print all extras
    	 * 
    	 */
    	try {
    		Log.i(Commons.TAG, "Removing all extras" );
    		Bundle bundle = a.getExtras();
    		Iterator<String> keys = bundle.keySet().iterator();
    		
    		for( ; keys.hasNext() ; ) {
    			String key = keys.next();
    			a.removeExtra( key );
    			Log.i( Commons.TAG, " -> " + key + ": REMOVED");
    		}
    	} catch( Exception e ) {
    		Log.e(Commons.TAG, "Error removing tags: " + e.toString() );
    	}
	}
	
	public static String getIMEI( Activity a ) {
		TelephonyManager telephonyManager = (TelephonyManager) a.getSystemService(Context.TELEPHONY_SERVICE);
		return telephonyManager.getDeviceId();
	}
	
	public static String getDeviceID( Context a ) {
		return Secure.getString( a.getContentResolver(), Secure.ANDROID_ID);
	}
}
