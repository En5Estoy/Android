package com.speryans.redbus.classes.helpers;

import java.io.File;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;

import com.speryans.redbus.classes.Commons;
import com.speryans.redbus.classes.KeySaver;
import com.speryans.redbus.classes.Utils;


import android.content.Context;

public class AlarmManagement {
	
	private static final String FILENAME = "alarms";
	
	public static JSONObject create( String name, long lat, long lon ) {
		try {
			JSONObject obj = new JSONObject();
		
			obj.put(KeySaver.ALARM, name);
			obj.put(KeySaver.ALARM_LAT, lat);
			obj.put(KeySaver.ALARM_LON, lon);
		
			return obj;
		} catch( Exception e ) {
			return null;
		}
	}

	public static JSONArray getAll(Context c) {
		JSONArray data = new JSONArray();
		File userFile = AlarmManagement.generateFolder(c, AlarmManagement.FILENAME);
		if (userFile.exists()) {
			try {
				String dataUser = new String(Utils.readFile(userFile));
				data = new JSONArray(dataUser);
			} catch (Exception e) {
			}
		}

		return data;
	}

	public static boolean isEquals(Context c, JSONObject obj) {
		JSONArray data = AlarmManagement.getAll(c);
		if (data.length() == 0) {
			return false;
		} else {
			for (int i = 0; i < data.length(); i++) {
				if (obj.optString(KeySaver.ALARM).equalsIgnoreCase(data.optJSONObject(i).optString(KeySaver.ALARM)) 
						&& obj.optLong(KeySaver.ALARM_LAT) == data.optJSONObject(i).optLong(KeySaver.ALARM_LAT)
						&& obj.optLong(KeySaver.ALARM_LON) == data.optJSONObject(i).optLong(KeySaver.ALARM_LON) ) {
					return true;
				}
			}
		}

		return false;
	}

	public static void add(Context c, JSONObject obj) {
		JSONArray data = AlarmManagement.getAll(c);
		Commons.info(data.toString());
		if (!AlarmManagement.isEquals(c, obj)) {
			data.put(obj);
		}

		try {
			Utils.saveFile(AlarmManagement.generateFolder(c, AlarmManagement.FILENAME), data.toString().getBytes());
		} catch (IOException e) {
		}
	}

	public static void remove(Context c, JSONObject obj) {
		JSONArray data = AlarmManagement.getAll(c);
		JSONArray newData = new JSONArray();
		if (AlarmManagement.isEquals(c, obj)) {
			for (int i = 0; i < data.length(); i++) {
				if (!obj.optString(KeySaver.ALARM).equalsIgnoreCase(data.optJSONObject(i).optString(KeySaver.ALARM)) 
						&& obj.optLong(KeySaver.ALARM_LAT) != data.optJSONObject(i).optLong(KeySaver.ALARM_LAT)
						&& obj.optLong(KeySaver.ALARM_LON) != data.optJSONObject(i).optLong(KeySaver.ALARM_LON)) {
					newData.put(data.optJSONObject(i));
				}
			}
		}

		try {
			Utils.saveFile(AlarmManagement.generateFolder(c, AlarmManagement.FILENAME), newData.toString().getBytes());
		} catch (IOException e) {
		}
	}

	public static File generateFolder(Context ctx, String filename) {
		return new File(ctx.getFilesDir(), filename + ".json");
	}
}
