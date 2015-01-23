package com.speryans.redbus.sections;

import java.util.HashMap;

import org.json.JSONObject;

import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.speryans.redbus.R;
import com.speryans.redbus.classes.Commons;
import com.speryans.redbus.classes.ProgressHUD;
import com.speryans.redbus.classes.SectionActivity;
import com.speryans.redbus.classes.StringUtilities;
import com.speryans.redbus.classes.Urls;
import com.speryans.redbus.classes.connection.Connection;
import com.speryans.redbus.classes.connection.DoneListener;
import com.speryans.redbus.stadistics.AppStats;

public class Taxi extends SectionActivity {
	
	protected static final int TAXI = 55;
	
	public AutoCompleteTextView fromTxt;
	public AutoCompleteTextView toTxt;
	
	private Button calculateBtn;

	private Button changePointsBtn;

	public void onCreated() {
		// EditTexts Taxi
		fromTxt = (AutoCompleteTextView) parent.findViewById(R.id.fromTxt);
		fromTxt.setThreshold(0);
		toTxt = (AutoCompleteTextView) parent.findViewById(R.id.toTxt);
		toTxt.setThreshold(0);
		
		changePointsBtn = ( Button ) parent.findViewById(R.id.changeTaxiPointsBtn);
		changePointsBtn.setTypeface(parent.icons);
		
		changePointsBtn.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				String tfrom = fromTxt.getText().toString();
				fromTxt.setText( toTxt.getText().toString() );
				toTxt.setText(tfrom);
			}
			
		});

		calculateBtn = (Button) parent.findViewById(R.id.calculateBtn);
		calculateBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (fromTxt.getText().toString().trim().equalsIgnoreCase("")
						&& toTxt.getText().toString().trim()
								.equalsIgnoreCase("")) {
					Toast.makeText(parent,R.string.uncomplete_fields,Toast.LENGTH_LONG).show();
				} else {
					AppStats.addSectionUse(parent, AppStats.TAXI_PRICE);

					ProgressHUD.show(parent, "Calculando...");

					HashMap<String, String> values = new HashMap<String, String>();
					values.put("udid", Commons.getDeviceID(parent));
					
					/**
					 * Remover caracteres extraños
					 */
					if (fromTxt.getText().toString().trim()
							.equalsIgnoreCase("")) {
						values.put("gps_from", parent.location.latitude + "," + parent.location.longitude);
					} else {
						values.put("address_from", StringUtilities.removeAccents(fromTxt.getText().toString().trim()));
					}
					if (toTxt.getText().toString().trim().equalsIgnoreCase("")) {
						values.put("gps_to", parent.location.latitude + "," + parent.location.longitude);
					} else {
						values.put("address_to", StringUtilities.removeAccents(toTxt.getText().toString().trim()));
					}
					
					
					Connection
					.post(parent, Urls.TAXI)
					.values(values)
					.done( new DoneListener() {

						@Override
						public void ready(String url, String data) {
							try {
								ProgressHUD.dismissHUD();
								
								JSONObject obj = new JSONObject( data );
								
								if( obj.getBoolean("result") ) {
									Intent intent = new Intent( parent, com.speryans.redbus.TaxiDetails.class );
									intent.putExtra("data", data);
									parent.startActivity(intent);
								} else {
									Toast.makeText(parent, R.string.taxi_no_match,  Toast.LENGTH_LONG).show();
								}
							} catch( Exception e ) {
								Toast.makeText(parent, R.string.taxi_no_match,  Toast.LENGTH_LONG).show();
							}
						}
						
					})
					.error(parent).start();
				}

			}
		});
	}
}
