package com.speryans.redbus.stadistics;

import java.util.HashMap;

import android.content.Context;
import android.content.pm.PackageInfo;

import com.speryans.redbus.classes.Commons;

public class AppStats {
	
	public static final String ALARM = "geoalarm";
	public static final String BUSES_SEARCH = "buses_search";
	public static final String BUSES_FOUND = "buses_found";
	public static final String BUSES_NOT_FOUND = "buses_not_found";
	public static final String PUSH_DEVICE = "push_device";
	public static final String TAXI_PRICE = "taxi_price";
	public static final String BUS_ACCOUNT = "bus_account";
	public static final String BUS_HISTORY = "bus_history";
	public static final String BUS_LOG = "bus_log";
	public static final String NEWS = "news_click";

	public static void addSectionUse(Context c, String section) {
		PackageInfo info;
		try {
			info = c.getPackageManager().getPackageInfo(c.getPackageName(), 0);
		
			HashMap<String, String> segmentation = new HashMap<String, String>();
			segmentation.put("section", section);
			segmentation.put("app_version", String.valueOf( info.versionCode ));

			AppStats.record("section", segmentation);
		} catch(Exception e) {
			Commons.error("Error adding section", e);
		}
	}
	
	public static void addCategoriesSearch(Context c, String category) {
		try {
			HashMap<String, String> segmentation = new HashMap<String, String>();
			segmentation.put("category", category);

			AppStats.record("categories", segmentation);
		} catch(Exception e) {
			Commons.error("Error adding section", e);
		}
	}
	
	public static void addSearch(Context c, String search) {
		try {
			HashMap<String, String> segmentation = new HashMap<String, String>();
			segmentation.put("search", search);

			AppStats.record("interests", segmentation);
		} catch(Exception e) {
			Commons.error("Error adding section", e);
		}
	}
	
	public static void addSuccesfulCases(Context c, String text) {
		try {
			HashMap<String, String> segmentation = new HashMap<String, String>();
			segmentation.put("contact", text);

			AppStats.record("contact_cases", segmentation);
		} catch(Exception e) {
			Commons.error("Error adding section", e);
		}
	}
	
	public static void addNews(Context c, String search) {
		try {
			HashMap<String, String> segmentation = new HashMap<String, String>();
			segmentation.put("url", search);

			AppStats.record("news", segmentation);
		} catch(Exception e) {
			Commons.error("Error adding section", e);
		}
	}
	
	public static void addAddress(Context c, String search) {
		try {
			HashMap<String, String> segmentation = new HashMap<String, String>();
			segmentation.put("address", search);

			AppStats.record("WrongAddress", segmentation);
		} catch(Exception e) {
			Commons.error("Error adding section", e);
		}
	}
	
	public static void addNotFoundData(Context c, String data) {
		PackageInfo info;
		try {
			info = c.getPackageManager().getPackageInfo(c.getPackageName(), 0);
			
			HashMap<String, String> segmentation = new HashMap<String, String>();
			segmentation.put("data", data);
			segmentation.put("app_version", String.valueOf( info.versionCode ));

			AppStats.record("NotFoundData", segmentation);
		} catch(Exception e) {
			Commons.error("Error adding section", e);
		}
	}
	
	public static void addPhoneChanged(String data) {
		try {
			Commons.info(data);
			HashMap<String, String> segmentation = new HashMap<String, String>();
			segmentation.put("changed", data);

			AppStats.record("PhoneChanged", segmentation);
		} catch(Exception e) {
			Commons.error("Error adding section", e);
		}
	}
	
	public static void addLog(Context c, String section, String message) {
		PackageInfo info;
		try {
			info = c.getPackageManager().getPackageInfo(c.getPackageName(), 0);
		
			HashMap<String, String> segmentation = new HashMap<String, String>();
			segmentation.put("section", section);
			segmentation.put("message", message);
			segmentation.put("app_version", String.valueOf( info.versionCode ));

			AppStats.record("section", segmentation);
		} catch(Exception e) {
			Commons.error("Error adding section", e);
		}
	}
	
	public static void record(String event, HashMap<String, String> segmentation) {
		if( !Commons.DEBUG ) {
			//Countly.sharedInstance().recordEvent(event, segmentation, 1);
		}
	}
}
