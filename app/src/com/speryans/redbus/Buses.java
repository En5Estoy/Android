package com.speryans.redbus;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.speryans.redbus.classes.Commons;
import com.speryans.redbus.classes.En5EstoyActivity;
import com.speryans.redbus.classes.KeySaver;
import com.speryans.redbus.classes.StringUtilities;
import com.speryans.redbus.classes.Typefaces;
import com.speryans.redbus.stadistics.AppStats;

public class Buses extends En5EstoyActivity {
	
	private TextView detailsTxt;
	private LinearLayout cardsContainer;
	
	private String json_data;
	private LinearLayout container;
	
	private TextView detailsTimeTxt;
	
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.buses);
		
		AppStats.addSectionUse(Buses.this, AppStats.BUSES_FOUND);
		
		container = (LinearLayout) this.findViewById(R.id.container);
		
		fontEverywhere(container, roboto);
		
		Commons.createHeader(this);
		
		json_data = this.getIntent().getStringExtra("data");
		
		detailsTxt = ( TextView ) this.findViewById( R.id.detailsTxt );
		detailsTxt.setText( this.getIntent().getStringExtra("from").replace("Province", "Provincia") );
		detailsTimeTxt = ( TextView ) this.findViewById( R.id.detailsTimeTxt );
		detailsTimeTxt.setText( this.getIntent().getStringExtra("to").replace("Province", "Provincia") );
		
    	cardsContainer = ( LinearLayout ) this.findViewById(R.id.cardsContainer);
    	
    	
    	((TextView) this.findViewById(R.id.iconDetailsTxt)).setTypeface(icons);
		((TextView) this.findViewById(R.id.iconDetailsTimeTxt)).setTypeface(icons);
		((TextView) this.findViewById(R.id.iconDetailsPriceTxt)).setTypeface(icons);
		
		((TextView) this.findViewById(R.id.iconHeader)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				finish();
			}
		});
    	
    	try {
			JSONArray data = new JSONArray(json_data);
				
			for( int i = 0 ; i < data.length() ; i++ ) {
				try {
					final JSONObject stop = data.getJSONObject(i);
					
					View v = null;
					
					if( stop.has("steps") ) {
						// This is a combination... so... it's not the same!
						
						v = LayoutInflater.from(this).inflate(R.layout.steps_bus_row, null, false);
						
						// Set typefaces
						((TextView) v.findViewById(R.id.detailsUp)).setTypeface(roboto);
						((TextView) v.findViewById(R.id.detailsChange)).setTypeface(roboto);
						((TextView) v.findViewById(R.id.detailsDown)).setTypeface(roboto);
						
						((TextView) v.findViewById(R.id.cardTitleTxt)).setTypeface(roboto);
						
						((TextView) v.findViewById(R.id.iconDown)).setTypeface(icons);
						((TextView) v.findViewById(R.id.iconChange)).setTypeface(icons);
						((TextView) v.findViewById(R.id.iconUp)).setTypeface(icons);
						((TextView) v.findViewById(R.id.busIcon)).setTypeface(icons);
						((TextView) v.findViewById(R.id.bus2Icon)).setTypeface(icons);
						
						JSONObject firstStep = stop.getJSONArray("steps").getJSONObject(0);
						JSONObject secondStep = stop.getJSONArray("steps").getJSONObject(1);
						
						String title = "Combinación mediante línea " + firstStep.getJSONObject("line").getString("name") + " y " + secondStep.getJSONObject("line").getString("name");
						
						((TextView) v.findViewById(R.id.cardTitleTxt)).setText( title );					
						
						String upDescription = 
								"Debes ascender en " + StringUtilities.capitalizeString( firstStep.optJSONObject("from").optJSONObject("street").getString("name") ) +
								" a " + firstStep.getJSONObject("from").getString("distance_string") + " mts." +
								"\nLínea " + firstStep.getJSONObject("line").getString("name") + " por "  + firstStep.optJSONObject("line").optJSONObject("transport").getString("name");
						
						((TextView) v.findViewById(R.id.detailsUp)).setText( upDescription );
						
						String changeDescription = "Debes descender en " + StringUtilities.capitalizeString( firstStep.optJSONObject("to").optJSONObject("street").getString("name") ) + " y caminar " + firstStep.optJSONObject("to").getString("distance_string") + " mts hasta " + secondStep.optJSONObject("from").optJSONObject("street").getString("name") + " y ascender a la línea " +  secondStep.getJSONObject("line").getString("name") + " por " + secondStep.optJSONObject("line").optJSONObject("transport").getString("name") ;
						
						((TextView) v.findViewById(R.id.detailsChange)).setText( changeDescription );
						
						String downDescription = "Debes descender en " + StringUtilities.capitalizeString( secondStep.optJSONObject("to").optJSONObject("street").getString("name") ) + "\nDestino a " + secondStep.optJSONObject("to").getString("distance_string") + " mts";
						
						((TextView) v.findViewById(R.id.detailsDown)).setText( downDescription );
						
						((TextView) v.findViewById(R.id.busIcon)).setVisibility(View.VISIBLE);
						
						final JSONObject fromStop = firstStep.getJSONObject("from").getJSONObject("location");
						
						final String endAddress = fromStop.getString("lon") + "," + fromStop.getString("lat");
						
						// Ir hasta la parada por ahora
						Button navigationButton = (Button) v.findViewById(R.id.navigationButton);
						navigationButton.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								try {
								Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/?daddr=" + endAddress)); 
								startActivity(myIntent);
								} catch(Exception e) {}
							}
						});
						
						this.createAlarmButton(secondStep.optJSONObject("to"), v);
					} else {
						v = LayoutInflater.from(this).inflate(R.layout.bus_row, null, false);
							
						String title = "Línea " + stop.optJSONObject("line").getString("name") + " a " + stop.optJSONObject("from").getString("distance_string") + " mts";
						
						String description = "Debes ascender en " + StringUtilities.capitalizeString( stop.optJSONObject("from").optJSONObject("street").getString("name") ) +
								"\nPor: " + stop.optJSONObject("line").optJSONObject("transport").getString("name");
						
						String downDescription = "Debes descender en " + StringUtilities.capitalizeString( stop.optJSONObject("to").optJSONObject("street").getString("name") ) + "\nDestino a " + stop.optJSONObject("to").getString("distance_string") + " mts";
						
						((TextView) v.findViewById(R.id.cardTitleTxt)).setText( title );
						((TextView) v.findViewById(R.id.cardTitleTxt)).setTypeface(roboto);
						((TextView) v.findViewById(R.id.detailsUp)).setText( description );
						((TextView) v.findViewById(R.id.detailsUp)).setTypeface(roboto);
						
						((TextView) v.findViewById(R.id.detailsDown)).setText( downDescription );
						((TextView) v.findViewById(R.id.detailsDown)).setTypeface(roboto);
						
						((TextView) v.findViewById(R.id.iconDown)).setTypeface(Typefaces.get(this, "fonts/ionicons.ttf"));
						((TextView) v.findViewById(R.id.iconUp)).setTypeface(Typefaces.get(this, "fonts/ionicons.ttf"));
						((TextView) v.findViewById(R.id.busIcon)).setTypeface(Typefaces.get(this, "fonts/ionicons.ttf"));
						((TextView) v.findViewById(R.id.busIcon)).setVisibility(View.VISIBLE);
						
						final JSONObject fromStop = stop.getJSONObject("from").getJSONObject("location");
						
						final String endAddress = fromStop.getString("lon") + "," + fromStop.getString("lat");
						
						// Ir hasta la parada por ahora
						Button navigationButton = (Button) v.findViewById(R.id.navigationButton);
						navigationButton.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								try {
								Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/?daddr=" + endAddress)); 
								startActivity(myIntent);
								} catch(Exception e) {}
							}
						});
						
						this.createAlarmButton(stop.optJSONObject("to"), v);
					}
						
					cardsContainer.addView(v);
				} catch( Exception ex ) {
					Commons.error("Loading bus row", ex);
				}
			}
		} catch( Exception e ) {
			Commons.error(e.toString(), e);
		}
	}
	
	private void createAlarmButton( final JSONObject to, View v ) {
		Button alarmBtn = (Button) v.findViewById(R.id.favoriteBtn);
		alarmBtn.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				KeySaver.save(Buses.this, KeySaver.ALARM, StringUtilities.capitalizeString( to.optJSONObject("street").optString("name") ) );
				KeySaver.save(Buses.this, KeySaver.ALARM_LAT, to.optJSONObject("location").optString("lon"));
				KeySaver.save(Buses.this, KeySaver.ALARM_LON, to.optJSONObject("location").optString("lat"));
				
				AppStats.addSectionUse(Buses.this, AppStats.ALARM);
				
				Toast.makeText(Buses.this, R.string.alarm_created, Toast.LENGTH_LONG).show();
				
				Intent service = new Intent( Buses.this, com.speryans.redbus.services.AlarmTracker.class );
				Buses.this.startService(service);
			}
			
		});
	}
	
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
}
