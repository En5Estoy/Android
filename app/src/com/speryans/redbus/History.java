package com.speryans.redbus;

import java.io.File;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.speryans.redbus.classes.Commons;
import com.speryans.redbus.classes.En5EstoyActivity;
import com.speryans.redbus.classes.KeySaver;
import com.speryans.redbus.classes.ProgressHUD;
import com.speryans.redbus.classes.Typefaces;
import com.speryans.redbus.classes.Utils;
import com.speryans.redbus.classes.helpers.Helpers;

public class History extends En5EstoyActivity {
	
	//private SharedPreferences sharedSettings;
	private LinearLayout container;
	private LinearLayout detailsLayout;
	private LinearLayout detailsContainer;
	
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.history);
		
		ProgressHUD.show(this);
		
		detailsLayout = (LinearLayout) this.findViewById(R.id.detailsLayout);
		detailsContainer = (LinearLayout) this.findViewById(R.id.detailsContainer);
		
		fontEverywhere(((LinearLayout)this.findViewById(R.id.container)), roboto);
		
		Commons.createHeader(this);
		
		(( TextView ) this.findViewById( R.id.titleTxt )).setTypeface(roboto);
		(( TextView ) this.findViewById( R.id.detailsTxt )).setTypeface(roboto);
		
		((TextView) this.findViewById(R.id.iconDetailsTxt)).setTypeface(icons); 
		
		((TextView) this.findViewById(R.id.iconHeader)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		JSONArray data = new JSONArray();
		File userFile = Helpers.generateFolder(this, this.getIntent().getStringExtra(KeySaver.DNI_KEY), this.getIntent().getStringExtra(KeySaver.CARD_KEY));
		if( userFile.exists() ) {
			try {
				String dataUser = new String(Utils.readFile(userFile));
				Commons.info(dataUser);
				data = new JSONArray( dataUser );
			} catch (Exception e) {}
		}
		
		if( data.length() == 0 ) {
			ProgressHUD.dismissHUD();
			Toast.makeText(this, "No hay datos para mostrar.", Toast.LENGTH_LONG).show();
			
			this.finish();
		} else {
			container = ( LinearLayout ) this.findViewById(R.id.dataContainer);
			
			try {
				for( int i = (data.length() - 1) ; i >= 0 ; i-- ) {
					JSONObject temp = data.getJSONObject(i);
					Commons.info(temp.toString());
				
					this.createRow(container, temp);
				
					if( i < (data.length() - 6) ) break;
				}
			} catch( Exception e ) {
				Commons.error(e.toString(), e);
			}
			
			// Load details if required
			String details = this.getIntent().getStringExtra("details");
			if( !details.equalsIgnoreCase("") ) {
				try {
					JSONArray arr = new JSONArray(details);
					
					for( int i = 0 ; i < arr.length() ; i++ ) {
						JSONObject temp = arr.getJSONObject(i);
						Commons.info(temp.toString());
					
						this.createRow(detailsContainer, temp);
					}
				} catch (JSONException e) {
					Commons.error(e.toString(), e);
				}
				
			} else {
				detailsLayout.setVisibility(View.GONE);
			}
			
			ProgressHUD.dismissHUD();
		}
	}
	
	private void createRow( LinearLayout cont, JSONObject temp ) {
		try {
			View v = LayoutInflater.from(this).inflate(R.layout.row, null, false);
			
			String detail = ( temp.getString("line").trim().equalsIgnoreCase("") ? "Recarga" : "Linea " + temp.getString("line") );
			
			if( temp.has("type") ) {
				if( temp.getString("type").trim().equalsIgnoreCase("Uso") && temp.getString("line").trim().equalsIgnoreCase("") ) {
					detail = "Transbordo";
				}
			}
			 
			((TextView) v.findViewById(R.id.label)).setText( detail );
			
			String dt = temp.getString("date");
			
			if( temp.has("type") ) {
				if( temp.getString("type").trim().equalsIgnoreCase("Uso") ) {
					dt = "Monto: $ " + temp.getString("ammount").trim() + "\n" + dt; 
				}
			}
			
			((TextView) v.findViewById(R.id.sublabel)).setText( dt );
			((TextView) v.findViewById(R.id.thirdLabel)).setText("$ " + temp.getString("total"));
			
			TextView icon = (TextView) v.findViewById(R.id.iconTxt);
			icon.setTypeface(icons);
			
			if( detail.equalsIgnoreCase("Transbordo") ) {
				icon.setText("\uf26e");
			} else if( detail.equalsIgnoreCase("Recarga") ) {
				icon.setText("\uf119");
			} else {
				icon.setText("\uf26e");
			}
		
			cont.addView(v);
		} catch( Exception e ) {
			
		}
	}
	
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

}
