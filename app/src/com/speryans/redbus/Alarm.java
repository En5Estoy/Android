package com.speryans.redbus;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.speryans.redbus.classes.Commons;
import com.speryans.redbus.classes.En5EstoyActivity;
import com.speryans.redbus.classes.KeySaver;
import com.speryans.redbus.classes.StringUtilities;
import com.speryans.redbus.classes.Typefaces;
import com.speryans.redbus.classes.URLParamEncoder;
import com.speryans.redbus.classes.Urls;
import com.speryans.redbus.classes.connection.Connection;
import com.speryans.redbus.classes.connection.DoneListener;
import com.speryans.redbus.classes.helpers.CityManager;
import com.speryans.redbus.stadistics.AppStats;

/**
 * Eliminar esta clase y pasar todo a la sección con el GeoCoder
 * 
 * @author sarriaroman
 *
 */
public class Alarm extends En5EstoyActivity implements DoneListener {
	
	protected static final int DATA = 10;
	
	private LinearLayout container;
	private Button searchButton;
	
	private AutoCompleteTextView addressTxt;
	private LinearLayout resultsContainer;

	private String[] autocomplete = new String[]{};
	
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm);
		
		// Url para obtener detalles de la ciudad o la concatenación para buscar en GMaps
		
		container = (LinearLayout) this.findViewById(R.id.container);
		
		fontEverywhere(container, roboto);
		
		this.buttonsFont(container, icons);
		
		Commons.createHeader(this);
		
		addressTxt = ( AutoCompleteTextView ) this.findViewById(R.id.addressTxt);
		addressTxt.setThreshold(0);
		
		this.loadAutocomplete();
		
		((TextView) this.findViewById(R.id.iconHeader)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		searchButton = (Button) this.findViewById(R.id.searchButton);
		searchButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				final String fieldContent = StringUtilities.removeAccents(addressTxt.getText().toString());
				if( !fieldContent.contains(",") ) {
					Connection
					.get(Alarm.this, Urls.GEOCODE + URLParamEncoder.encode( fieldContent + ", " + CityManager.getSearch(Alarm.this) ))
					.done(Alarm.this)
					.start();
				} else {
					Connection
					.get(Alarm.this, Urls.GEOCODE + URLParamEncoder.encode(fieldContent))
					.done(Alarm.this)
					.start();
				}
			}
			
		});
		
    	resultsContainer = ( LinearLayout ) this.findViewById(R.id.resultsContainer);
	}
	
	private void loadAutocomplete() { 
		String acmpt = KeySaver.getSaved(this, KeySaver.AUTOCOMPLETE);
		if( acmpt != null ) {
			autocomplete = acmpt.split(",");
		}
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line, autocomplete);
		
		// Set adapters
		addressTxt.setAdapter(adapter);
	}
	
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void ready(String url, String message) {
		try {
			JSONObject data = new JSONObject(message);
			
			if( data.getString("status").equalsIgnoreCase("OK") ) {
				
				resultsContainer.removeAllViews();
				
				JSONArray results = data.getJSONArray("results");
				for( int i = 0 ; i < results.length() ; i++ ) {
					JSONObject result = results.getJSONObject(i);
					
					View v = LayoutInflater.from(this).inflate(R.layout.row, null, false);
					
					JSONObject location = result.getJSONObject("geometry").getJSONObject("location");
					final double lat = location.getDouble("lat");
					final double lon = location.getDouble("lng");
					
					final String address = result.getString("formatted_address").replace("Province", "Provincia");
					
					((TextView) v.findViewById(R.id.label)).setText( address );
					((TextView) v.findViewById(R.id.sublabel)).setText( "(" + lat + ", " + lon + ")"  );
					((LinearLayout) v.findViewById(R.id.rightLayout)).setVisibility(View.GONE);
					
					TextView icon = (TextView) v.findViewById(R.id.iconTxt);
					icon.setTypeface(mapicons);
					icon.setText("\ue04c");
					
					v.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							KeySaver.save(Alarm.this, KeySaver.ALARM, address);
							KeySaver.save(Alarm.this, KeySaver.ALARM_LAT, "" + lat);
							KeySaver.save(Alarm.this, KeySaver.ALARM_LON, "" + lon);
							
							AppStats.addSectionUse(Alarm.this, AppStats.ALARM);
							
							Intent service = new Intent( Alarm.this, com.speryans.redbus.services.AlarmTracker.class );
							Alarm.this.startService(service);
							
							Toast.makeText(Alarm.this, R.string.alarm_set, Toast.LENGTH_LONG).show();
							
							Alarm.this.finish();
						}
						
					});
					
					resultsContainer.addView(v);
				}
			}
		} catch( Exception e ) {
			Commons.error(e.toString(), e);
		}
	}


}
