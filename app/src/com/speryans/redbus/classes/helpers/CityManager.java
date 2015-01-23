package com.speryans.redbus.classes.helpers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.speryans.redbus.classes.KeySaver;

import android.content.Context;

public class CityManager {
	
	public static JSONObject getData( Context c ) {
		JSONObject obj = null;
		try {
			obj = new JSONObject(KeySaver.getSaved(c, KeySaver.CITY_DATA));
		} catch (JSONException e) {}
		
		return obj;
	}
	
	public static String getCity( Context c ) {
		JSONObject obj = CityManager.getData(c);
		
		return (obj == null ) ? "" : obj.optString("name");
	}
	
	public static String getSearch( Context c ) {
		JSONObject obj = CityManager.getData(c);
		
		return (obj == null ) ? "" : obj.optString("search");
	}
	
	public static JSONArray getFeatures( Context c ) {
		JSONObject obj = CityManager.getData(c);
		
		return obj.optJSONArray("features");
	}
	
	public static JSONArray getKeywords( Context c ) {
		JSONObject obj = CityManager.getData(c);
		
		return obj.optJSONArray("keywords");
	}
}
