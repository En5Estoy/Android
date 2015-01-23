package com.speryans.redbus.classes;

import java.util.Hashtable;


import android.content.Context;
import android.graphics.Typeface;

public class Typefaces {
	private static final Hashtable<String, Typeface> cache = new Hashtable<String, Typeface>();

	public static Typeface get(Context c, String assetPath) {
		synchronized (cache) {
			if (!cache.containsKey(assetPath)) {
				try {
					Typeface t = Typeface.createFromAsset(c.getAssets(),
							assetPath);
					cache.put(assetPath, t);
					
					Commons.info("Font: " + assetPath + " loaded");
					
					return t;
				} catch (Exception e) {
					Commons.error("Could not get typeface '" + assetPath + "' because " + e.getMessage());
					return null;
				}
			}
			
			Commons.info("Font: " + assetPath + " from cache");
			return cache.get(assetPath);
		}
	}
}
