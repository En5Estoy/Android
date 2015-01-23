package com.speryans.redbus.classes;

import com.crashlytics.android.Crashlytics;
import com.speryans.redbus.countly.CountlyActivity;

import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class En5EstoyActivity extends CountlyActivity {
	
	public static Typeface icons;
	public static Typeface roboto;
	public static Typeface glyphs;
	public Typeface mapicons;

	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		
		Crashlytics.start(this);
		
		icons = Typefaces.get(this, "fonts/ionicons.ttf");
		roboto = Typefaces.get(this, "fonts/roboto.ttf");
		glyphs = icons; //Typefaces.get(this, "fonts/glyphs.ttf");
		mapicons = Typefaces.get(this, "fonts/map-icons.ttf");
	}
	
	protected void fontEverywhere( ViewGroup vg, Typeface type ) {
		for (int i = 0; i < vg.getChildCount(); i++){
			View child = vg.getChildAt(i);
			
			if (child instanceof Button){ 
				continue;
			}
			
			if (child instanceof TextView){ 
				((TextView)child).setTypeface(type);
			}
			
			if (child instanceof ViewGroup){ 
				fontEverywhere( (ViewGroup)child, type );
			}
		}
	}
	
	protected void buttonsFont( ViewGroup vg, Typeface type ) {
		for (int i = 0; i < vg.getChildCount(); i++){
			View child = vg.getChildAt(i);
			
			if (child instanceof TextView){
				if( ((TextView) child).getTypeface() != roboto ){
					((TextView)child).setTypeface(type);
				}
			}
			
			if (child instanceof Button){ 
				((Button)child).setTypeface(type);
			}
			
			if (child instanceof ViewGroup){ 
				buttonsFont( (ViewGroup)child, type );
			}
		}
	}
	
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	
}
