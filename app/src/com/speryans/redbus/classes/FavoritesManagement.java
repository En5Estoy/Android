package com.speryans.redbus.classes;

import java.io.File;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

public class FavoritesManagement {
	
	public static JSONArray getFavorites(Context c) {
		JSONArray data = new JSONArray();
		File userFile = FavoritesManagement.generateFolder(c, "favorites");
		if( userFile.exists() ) {
			try {
				String dataUser = new String(Utils.readFile(userFile));
				data = new JSONArray( dataUser );
			} catch (Exception e) {}
		}
		
		return data;
	}
	
	public static boolean isFavorite(Context c, JSONObject obj) {
		JSONArray data = FavoritesManagement.getFavorites(c);
		if( data.length() == 0 ) {
			return false;
		} else {
			for( int i = 0 ; i < data.length() ; i++ ) {
				if( obj.optString("id").equalsIgnoreCase( data.optJSONObject(i).optString("id") ) ) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	public static void addFavorite(Context c, JSONObject obj ) {
		JSONArray data = FavoritesManagement.getFavorites(c);
		if( !FavoritesManagement.isFavorite(c, obj) ) {
			data.put(obj);
		}
		
		try {
			Utils.saveFile(FavoritesManagement.generateFolder(c, "favorites"), data.toString().getBytes());
		} catch (IOException e) {}
	}
	
	public static void removeFavorite(Context c, JSONObject obj ) {
		JSONArray data = FavoritesManagement.getFavorites(c);
		JSONArray newData = new JSONArray();
		if( FavoritesManagement.isFavorite(c, obj) ) {
			for( int i = 0 ; i < data.length() ; i++ ) {
				if( obj.optInt("id") != data.optJSONObject(i).optInt("id") ) {
					newData.put(data.optJSONObject(i));
				}
			}
		}
		
		try {
			Utils.saveFile(FavoritesManagement.generateFolder(c, "favorites"), newData.toString().getBytes());
		} catch (IOException e) {}
	}
	
	public static File generateFolder( Context ctx, String filename ) {
		return new File( ctx.getFilesDir() , filename + ".json" );
	}
}
