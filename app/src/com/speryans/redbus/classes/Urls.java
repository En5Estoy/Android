package com.speryans.redbus.classes;

public class Urls {
	// API Base URL
	public static final String server = "http://54.243.218.97:3000/api/v1";
	//public static final String server = "http://192.168.0.2:3000/api/v1";
	
	// OLD API's
	public static final String GEOCODE = "http://maps.googleapis.com/maps/api/geocode/json?sensor=false&address=";
	
	public static final String STREETS = "http://en5estoy.com/api/v1/streets";
	
	public static final String VERSION = "http://en5estoy.com/api/v1/app/version/Android";
	
	// V1 API's	
	public static final String SALDO_URL = server + "/redbus/session";
	public static final String SALDO_SEND_URL = server + "/redbus/send";
	public static final String SALDO_CAPTCHA_URL = server + "/redbus/captcha/";
	
	public static final String REGISTER_USER = server + "/user/register";
	
	public static final String CITIES = server + "/cities";
	
	public static final String WEATHER = server + "/weather";
	public static final String NEWS = server + "/news";
	
	public static final String NOTIFICATIONS = server + "/notifications";
	
	public static final String POINTS_CATEGORY = server + "/commerce/categories";
	public static final String POINTS_SEARCH = server + "/commerce/search";
	
	public static final String TAXI = server + "/taxi";
	
	public static final String CITY_ADDRESS = server + "/cities/address";
	
	public static final String LINES = server + "/transport/lines";
	public static final String STOPS = server + "/transport/stops";
	public static final String TRANSPORT = server + "/transport/calculate";
	
	//public static final String POINTS_EDIT = "http://en5estoy.com/api/v1/venues/edit";
	//public static final String BLOCKED = "http://en5estoy.com/api/v1/blocked";
}
