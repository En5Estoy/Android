package com.speryans.redbus.classes;

import java.util.HashMap;
import java.util.Map;

public class StringUtilities {

	private static Map<Character, Character> MAP_NORM;
	static { // Greek characters normalization
	    MAP_NORM = new HashMap<Character, Character>();
	    MAP_NORM.put('á', 'a');
	    MAP_NORM.put('é', 'e');
	    MAP_NORM.put('í', 'i');
	    MAP_NORM.put('ó', 'o');
	    MAP_NORM.put('ú', 'u');
	    MAP_NORM.put('ñ', 'n');
	}

	public static String removeAccents(String s) {
	    if (s == null) {
	        return null;
	    }
	    StringBuilder sb = new StringBuilder(s);

	    for(int i = 0; i < s.length(); i++) {
	        Character c = MAP_NORM.get(sb.charAt(i));
	        if(c != null) {
	            sb.setCharAt(i, c.charValue());
	        }
	    }

	    return sb.toString();
	}
	
	public static String capitalizeString(String string) {
		  char[] chars = string.toLowerCase().toCharArray();
		  boolean found = false;
		  for (int i = 0; i < chars.length; i++) {
		    if (!found && Character.isLetter(chars[i])) {
		      chars[i] = Character.toUpperCase(chars[i]);
		      found = true;
		    } else if (Character.isWhitespace(chars[i]) || chars[i]=='.' || chars[i]=='\'') { // You can add other chars here
		      found = false;
		    }
		  }
		  return String.valueOf(chars);
		}
}
