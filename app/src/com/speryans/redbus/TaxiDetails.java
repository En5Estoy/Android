package com.speryans.redbus;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.speryans.redbus.classes.Commons;
import com.speryans.redbus.classes.En5EstoyActivity;
import com.speryans.redbus.classes.Typefaces;

public class TaxiDetails extends En5EstoyActivity {
	
	private TextView detailsTxt;
	private LinearLayout stepsContainer;
	
	private String json_data;
	private LinearLayout container;
	private Button navigationButton;
	
	private String endAddress;
	private LinearLayout pricesLayout;
	
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.taxi);
		
		container = (LinearLayout) this.findViewById(R.id.container);
		
		fontEverywhere(container, roboto);
		
		this.buttonsFont(container, icons);
		
		Commons.createHeader(this);
		
		json_data = this.getIntent().getStringExtra("data");
		
		detailsTxt = ( TextView ) this.findViewById( R.id.detailsTxt );
    	stepsContainer = ( LinearLayout ) this.findViewById(R.id.stepsContainer);
    	
    	pricesLayout = (LinearLayout) this.findViewById(R.id.pricesLayout);
    	
    	try {
			JSONObject data = new JSONObject(json_data);
			
			if( data.getBoolean("result") ) {
				JSONObject route = data.getJSONObject("data");
				
				String details = "Desde: " + route.getString("start_address");
				details += "\nHasta: " + route.getString("end_address");
				
				detailsTxt.setText(details);
				
				// Get prices and show them
				JSONArray prices = route.getJSONArray("prices");
				
				for( int e = 0 ; e < prices.length() ; e++ ) {
					JSONObject price = prices.getJSONObject(e);
					
					View v = LayoutInflater.from(this).inflate(R.layout.price_row, null, false);
					
					((TextView) v.findViewById(R.id.iconDetailsTimeTxt)).setTypeface(icons);
				
					TextView detailsTimeTxt = ( TextView ) v.findViewById( R.id.detailsTimeTxt );
					
					details = "Costo de tu viaje: $ " + price.getString("price");
					details += "\nTarifa: " + price.getString("name");
					details += "\nDescripción: " + price.getString("description");
					
					detailsTimeTxt.setText(details);
					detailsTimeTxt.setTypeface(roboto);
					
					pricesLayout.addView(v);
				}
				
				endAddress = route.getString("end_address");
				
				JSONArray steps = route.getJSONArray("steps");
				for( int i = 0 ; i < steps.length() ; i++ ) {
					JSONObject step = steps.getJSONObject(i);
					
					View v = LayoutInflater.from(this).inflate(R.layout.row, null, false);
					
					((TextView) v.findViewById(R.id.label)).setText(Html.fromHtml( step.getString("html_instructions")));
					((TextView) v.findViewById(R.id.sublabel)).setText("" + step.getJSONObject("distance").getString("text") + " - " + step.getJSONObject("duration").getString("text") );
					((LinearLayout) v.findViewById(R.id.rightLayout)).setVisibility(View.GONE);
					
					TextView icon = (TextView) v.findViewById(R.id.iconTxt);
					icon.setTypeface(icons);
					
					if( step.getString("html_instructions").contains("derecha") ) {
						icon.setText("\uf13a");
					} else if( step.getString("html_instructions").contains("izquierda") ) {
						icon.setText("\uf21e");
					} else {
						icon.setText("\uf273");
					}
					
					stepsContainer.addView(v);
				}
			}
		} catch( Exception e ) {
			Commons.error(e.toString(), e);
		}
    	
    	((TextView) this.findViewById(R.id.iconDetailsTxt)).setTypeface(icons);
		
		((TextView) this.findViewById(R.id.iconHeader)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				finish();
			}
		});
    	
    	navigationButton = (Button) this.findViewById(R.id.navigationButton);
		navigationButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("google.navigation:geo:0,0?q=" + endAddress )); //" + location.longitude + "," + location.latitude + "
				//startActivity(intent);
				
				Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/?daddr=" + endAddress)); 
				startActivity(myIntent);
			}
			
		});
	}
	
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}


}
