package com.speryans.redbus.classes;

import android.content.Context;
import android.content.SharedPreferences;

public class KeySaver {
	private static final String AWKEY = "En5Estoy";
	private static final String AWPREFIX = "e5e_";
	
	// APP Keys
	public static final String DB = "DB_VERSION";
	public static final String ALARM = "ALARM_POS";
	public static final String ALARM_LAT = "ALARM_LAT";
	public static final String ALARM_LON = "ALARM_LON";
	public static final String AUTOCOMPLETE = "AUTOCOMPLETE";
	
	public static final String NEW_NEWS = "NEW_SECTION_NEWS";
	public static final String NEW_RB = "NEW_SECTION_RB";
	
	public static final String WIZARD = "WIZARD";
	
	public static final String NO_CHANGE_CITY = "NO_CHANGE_CITY";
	
	public static final String CITY_DATA = "CITY_DATA";
	
	// Internal keys
	public static final String DNI_KEY = "lastDNI";
	public static final String CARD_KEY = "lastCARD";
	
	public static void save(Context a, String key, String uid ) {
		SharedPreferences settings = a.getSharedPreferences(AWKEY, 0);
	    SharedPreferences.Editor editor = settings.edit();
	    editor.putString( AWPREFIX + key, uid);

	    editor.commit();
	}
	
	public static String getSaved(Context a, String key) {
		SharedPreferences settings = a.getSharedPreferences(AWKEY, 0);
		return settings.getString( AWPREFIX + key, "");
	}

}
