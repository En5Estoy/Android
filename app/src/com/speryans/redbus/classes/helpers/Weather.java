package com.speryans.redbus.classes.helpers;

import java.util.HashMap;
import java.util.Locale;

import android.annotation.SuppressLint;

public class Weather {
	protected static HashMap<String, String> weatherValues = new HashMap<String, String>();
	
	public static int convertFtoC( int F ) {
		return ((F - 32) * 5/9 ); 
	}
	
	public static String parseWeatherState( String condition ) {
		if( weatherValues.size() == 0 ) {
			Weather.fillWeatherMap();
		}
		return weatherValues.get( condition.toUpperCase(Locale.getDefault()) );
	}
	
	@SuppressLint("DefaultLocale")
	protected static void fillWeatherMap() {
		weatherValues.put("tornado".toUpperCase(), "(");
		weatherValues.put("tropical storm".toUpperCase(), "(");
		weatherValues.put("hurricane".toUpperCase(), "F");
		weatherValues.put("severe thunderstorms".toUpperCase(), "O");
		weatherValues.put("thunderstorms".toUpperCase(), "O");
		weatherValues.put("mixed rain and snow".toUpperCase(), "V");
		weatherValues.put("mixed rain and sleet".toUpperCase(), "U");
		weatherValues.put("mixed snow and sleet".toUpperCase(), "U");
		weatherValues.put("freezing drizzle".toUpperCase(), "T");
		weatherValues.put("drizzle".toUpperCase(), "Q");
		weatherValues.put("freezing rain".toUpperCase(), "R");
		weatherValues.put("showers".toUpperCase(), "R");
		weatherValues.put("snow flurries".toUpperCase(), "W");
		weatherValues.put("light snow showers".toUpperCase(), "V");
		weatherValues.put("blowing snow".toUpperCase(), "M");
		weatherValues.put("snow".toUpperCase(), "W");
		weatherValues.put("hail".toUpperCase(), "X");
		weatherValues.put("sleet".toUpperCase(), "U");
		weatherValues.put("dust".toUpperCase(), "M");
		weatherValues.put("foggy".toUpperCase(), "E");
		weatherValues.put("haze".toUpperCase(), "E");
		weatherValues.put("smoky".toUpperCase(), "E");
		weatherValues.put("blustery".toUpperCase(), "F");
		weatherValues.put("windy".toUpperCase(), "F");
		weatherValues.put("cold".toUpperCase(), "G");
		weatherValues.put("cloudy".toUpperCase(), "N");
		weatherValues.put("mostly cloudy (night)".toUpperCase(), "%");
		weatherValues.put("mostly cloudy (day)".toUpperCase(), "Y");
		weatherValues.put("partly cloudy (night)".toUpperCase(), "5");
		weatherValues.put("partly cloudy (day)".toUpperCase(), "N");
		weatherValues.put("partly cloudy".toUpperCase(), "N");
		weatherValues.put("mostly cloudy".toUpperCase(), "Y");
		weatherValues.put("clear (night)".toUpperCase(), "2");
		weatherValues.put("sunny".toUpperCase(), "B");
		weatherValues.put("fair (night)".toUpperCase(), "2");
		weatherValues.put("fair (day)".toUpperCase(), "B");
		weatherValues.put("fair".toUpperCase(), "B");
		weatherValues.put("mixed rain and hail".toUpperCase(), "X");
		weatherValues.put("hot".toUpperCase(), "A");
		weatherValues.put("Light Drizzle".toUpperCase(), "B");
		weatherValues.put("Light Rain".toUpperCase(), "Q");
		weatherValues.put("isolated thunderstorms".toUpperCase(), "O");
		weatherValues.put("scattered thunderstorms".toUpperCase(), "0");
		weatherValues.put("scattered showers".toUpperCase(), "Q");
		weatherValues.put("heavy snow".toUpperCase(), "W");
		weatherValues.put("scattered snow showers".toUpperCase(), "V");
		weatherValues.put("partly cloudy".toUpperCase(), "H");
		weatherValues.put("thundershowers".toUpperCase(), "P");
		weatherValues.put("Thunderstorm".toUpperCase(), "P");
		weatherValues.put("snow showers".toUpperCase(), "U");
		weatherValues.put("isolated thundershowers".toUpperCase(), "T");
		weatherValues.put("not available".toUpperCase(), ")");
	}
}
