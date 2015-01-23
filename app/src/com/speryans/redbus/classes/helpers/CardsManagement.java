package com.speryans.redbus.classes.helpers;

import java.io.File;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;

import com.speryans.redbus.classes.Commons;
import com.speryans.redbus.classes.Utils;


import android.content.Context;

public class CardsManagement {
	
	private static final String FILENAME = "cards";
	
	public static JSONObject createCard( String dni, String cnumber ) {
		try {
			JSONObject obj = new JSONObject();
		
			obj.put("dni", dni);
			obj.put("card_number", cnumber);
		
			return obj;
		} catch( Exception e ) {
			return null;
		}
	}

	public static JSONArray getCards(Context c) {
		JSONArray data = new JSONArray();
		File userFile = CardsManagement.generateFolder(c, CardsManagement.FILENAME);
		if (userFile.exists()) {
			try {
				String dataUser = new String(Utils.readFile(userFile));
				data = new JSONArray(dataUser);
			} catch (Exception e) {
			}
		}

		return data;
	}

	public static boolean isCard(Context c, JSONObject obj) {
		JSONArray data = CardsManagement.getCards(c);
		if (data.length() == 0) {
			return false;
		} else {
			for (int i = 0; i < data.length(); i++) {
				if (obj.optInt("dni") == data.optJSONObject(i).optInt("dni") && obj.optInt("card_number") == data.optJSONObject(i).optInt("card_number") ) {
					return true;
				}
			}
		}

		return false;
	}

	public static void addCard(Context c, JSONObject obj) {
		JSONArray data = CardsManagement.getCards(c);
		Commons.info(data.toString());
		if (!CardsManagement.isCard(c, obj)) {
			data.put(obj);
		}

		try {
			Utils.saveFile(CardsManagement.generateFolder(c, CardsManagement.FILENAME), data.toString().getBytes());
		} catch (IOException e) {
		}
	}

	public static void removeCard(Context c, JSONObject obj) {
		JSONArray data = CardsManagement.getCards(c);
		JSONArray newData = new JSONArray();
		if (CardsManagement.isCard(c, obj)) {
			for (int i = 0; i < data.length(); i++) {
				if (obj.optInt("id") != data.optJSONObject(i).optInt("id")) {
					newData.put(data.optJSONObject(i));
				}
			}
		}

		try {
			Utils.saveFile(CardsManagement.generateFolder(c, CardsManagement.FILENAME), newData.toString().getBytes());
		} catch (IOException e) {
		}
	}

	public static File generateFolder(Context ctx, String filename) {
		return new File(ctx.getFilesDir(), filename + ".json");
	}
}
