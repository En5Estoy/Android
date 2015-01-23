package com.speryans.redbus.sections;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.WazaBe.HoloEverywhere.HoloAlertDialogBuilder;
import com.speryans.redbus.R;
import com.speryans.redbus.classes.Commons;
import com.speryans.redbus.classes.DatabaseWrapper;
import com.speryans.redbus.classes.KeySaver;
import com.speryans.redbus.classes.ProgressHUD;
import com.speryans.redbus.classes.SectionActivity;
import com.speryans.redbus.classes.Urls;
import com.speryans.redbus.classes.Utils;
import com.speryans.redbus.classes.connection.Connection;
import com.speryans.redbus.classes.connection.DoneListener;
import com.speryans.redbus.classes.helpers.CardsManagement;
import com.speryans.redbus.classes.helpers.Helpers;
import com.speryans.redbus.stadistics.AppStats;

public class RedBus extends SectionActivity implements DoneListener {
	
	public static final int DATA = 10;
	public static final int SESSION = 50;
	
	private String session = null;
	
	private EditText dniTxt;
	private EditText cardTxt;
	private EditText controlTxt;
	private ImageView captchaView;
	private Button sendBtn;

	private Button historyBtn;
	
	private LinearLayout accountCardContainer;
	
	private LinearLayout suggested;
	
	private LinearLayout registerBtn;
	private Button moreBtn;	

	public void onCreated() {
		// Check version of DB and copy DB if not exists
		File db_file = new File(parent.getFilesDir(), DatabaseWrapper.DATABASE_NAME);
		if (KeySaver.getSaved(parent, KeySaver.DB) == "" || Integer.valueOf(KeySaver.getSaved(parent, KeySaver.DB)).intValue() < 1) {
			try {
				Utils.CopyStream(
						parent.getAssets().open(DatabaseWrapper.DATABASE_NAME),
						new FileOutputStream(db_file));
				Commons.info("DB copied");

				KeySaver.save(parent, KeySaver.DB, "1");
			} catch (Exception e) {
				Commons.error("Error in database copy: " + e.toString(), e);
			}
		}
		
		// Open the Database
		parent.database = new DatabaseWrapper( parent );
				
		suggested = ( LinearLayout ) parent.findViewById(R.id.suggestedContainer);
		
		accountCardContainer = (LinearLayout) parent.panel.menu.findViewById(R.id.accountCardsContainer);
		
		// Connecting DNI Text
        dniTxt = ( EditText ) parent.findViewById(R.id.dniTxt);
        //dniTxt.setText("31134864");
        if( !parent.sharedSettings.getString(KeySaver.DNI_KEY, "").equalsIgnoreCase("") ) {
        	dniTxt.setText(parent.sharedSettings.getString(KeySaver.DNI_KEY, ""));
        }
     		
        // Connecting CARD Text
        cardTxt = (EditText) parent.findViewById(R.id.cardTxt);
        //cardTxt.setText("3304997");
        if( !parent.sharedSettings.getString(KeySaver.CARD_KEY, "").equalsIgnoreCase("") ) {
        	cardTxt.setText(parent.sharedSettings.getString(KeySaver.CARD_KEY, ""));
        }
     		
        // Connecting Captcha Text
        controlTxt = (EditText ) parent.findViewById(R.id.controlTxt);
     		
        captchaView = ( ImageView ) parent.findViewById(R.id.captchaView);
        
        // Send button
        sendBtn = ( Button ) parent.findViewById(R.id.sendBtn);
        
        // Send information
     	sendBtn.setOnClickListener(new OnClickListener() {

     		@Override
     		public void onClick(View v) {
     			if( dniTxt.getText().toString().trim().equalsIgnoreCase("") || 
						cardTxt.getText().toString().trim().equalsIgnoreCase("") || 
						controlTxt.getText().toString().trim().equalsIgnoreCase("") ) {
					Commons.dialog(parent, "Debe ingresar todos los datos para consultar el saldo.");
					
					return;
				}
     			
     			AppStats.addSectionUse(parent, AppStats.BUS_ACCOUNT);
				
				ProgressHUD.show(parent);
				
				InputMethodManager imm = (InputMethodManager) parent.getSystemService( Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(controlTxt.getWindowToken(), 0);
				
				Editor editor = parent.sharedSettings.edit();
				editor.putString( KeySaver.DNI_KEY, dniTxt.getText().toString().trim() );
				editor.putString( KeySaver.CARD_KEY, cardTxt.getText().toString().trim() );
				editor.commit();
				
				CardsManagement.addCard(parent, CardsManagement.createCard(dniTxt.getText().toString(), cardTxt.getText().toString()));
				loadHistoryCards();
				
				Connection.post(parent, Urls.SALDO_SEND_URL)
					.param("dni", dniTxt.getText().toString())
					.param("card", cardTxt.getText().toString())
					.param("captcha", controlTxt.getText().toString())
					.param("cookie", session )
					.done(RedBus.this)
					.error(parent)
					.start();
				
				controlTxt.setText("");
     		}
     	});
     		
        historyBtn = ( Button ) parent.findViewById(R.id.historialBtn);
        historyBtn.setOnClickListener(new OnClickListener() {

     		@Override
     		public void onClick(View v) {     			
     			showHistory("");
     		}
        });
        
        moreBtn = ( Button ) parent.findViewById(R.id.moreBtn);
        moreBtn.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				ProgressHUD.show(parent);
				
				Intent i = new Intent(parent, com.speryans.redbus.CentersPoints.class);
				parent.startActivity(i);
			}
			
		});
        
     // Register card button
     	registerBtn = ( LinearLayout ) parent.findViewById(R.id.registerCardButton);
     		registerBtn.setOnClickListener( new OnClickListener() {
     			@Override
     			public void onClick(View v) {
     				Intent intent = new Intent( parent, com.speryans.redbus.RegisterCard.class );
     				parent.startActivity(intent);
     			}
     		});
        
        loadCaptcha();
	}
	
	private void loadCaptcha() {
		if( session == null || session.equalsIgnoreCase("") ) {
			Connection.get(parent, Urls.SALDO_URL)
				.done(this)
				.error(parent)
				.start();
		} else {
			parent.imgLoader.clearCache();
			parent.imgLoader.displayImage(Urls.SALDO_CAPTCHA_URL + session, parent, captchaView, false, 80);
		}
	}
	
	private void processSession(String message) {
		try {
			JSONObject data = new JSONObject(message);
			
			if( data.getBoolean("result") ) {
				session = data.getString("cookie");
				
				loadCaptcha();
			} else {
				Toast.makeText(parent, "Se produjo un error accediendo al sitio web de Red Bus. Intente nuevamente m�s tarde.", Toast.LENGTH_LONG).show();
			}
		} catch( Exception e ) {
			Commons.info(e.toString());
		}
	}
	
	private void processMessage(String message) {
		try {
			JSONObject data = new JSONObject(message);
			
			if( data.getBoolean("result") ) {
				if( !data.getJSONObject("data").getString("date").equalsIgnoreCase("") ) {
					JSONObject service_data = data.getJSONObject("data");
				
					JSONArray jdata = null;
					File userFile = Helpers.generateFolder(parent, this.dniTxt.getText().toString().trim(), this.cardTxt.getText().toString().trim());
					if( userFile.exists() ) {
						try {
							jdata = new JSONArray( new String(Utils.readFile(userFile)) );
						} catch (Exception e) {
							jdata = new JSONArray();
						}
					} else {
						jdata = new JSONArray();
					}
				
					JSONObject obj = new JSONObject();
					obj.put("date", service_data.getString("date"));
					obj.put("line", service_data.getString("line"));
					obj.put("type", service_data.getString("type"));
					obj.put("ammount", service_data.getString("ammount"));
					obj.put("total", service_data.getString("balance"));
				
					jdata.put(obj);
				
					try {
						Utils.saveFile(userFile, jdata.toString().getBytes());
						Commons.info(data.toString());
					} catch (IOException e) {
						AppStats.addLog(parent, AppStats.BUS_LOG, "Resultado exitoso. Fallo de guardado.");
						this.showError();
						return;
					}
				
					AppStats.addLog(parent, AppStats.BUS_LOG, "Resultado exitoso. Abriendo historial.");
					showHistory( data.getJSONArray("details").toString() );
				
					session = null;
					loadCaptcha();
				} else {
					AppStats.addLog(parent, AppStats.BUS_LOG, "Resultado exitoso. Fallo en datos ( Vacios para: " + this.dniTxt.getText().toString().trim() + " - " + this.cardTxt.getText().toString().trim() + " ).");
					this.showError();
				}
			} else {
				AppStats.addLog(parent, AppStats.BUS_LOG, "Sin resultado.");
				this.showError();
			}
		} catch( Exception e ) {
			AppStats.addLog(parent, AppStats.BUS_LOG, "Excepcion. " + e.toString() );
			
			Commons.error(e.toString(), e);
		}
		
		ProgressHUD.dismissHUD();
	}
	
	public void showError() {
		AlertDialog.Builder builder = new HoloAlertDialogBuilder(parent);
		
		builder.setInverseBackgroundForced(true);
		builder.setCancelable(true);
		builder.setTitle(R.string.application_name);
		TextView view = (TextView)LayoutInflater.from(parent).inflate(R.layout.workarounddialogs, null, false);
		view.setText(R.string.error_popup);
		builder.setView( view );
		builder.setPositiveButton("Registrar tarjeta", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent( parent, com.speryans.redbus.RegisterCard.class );
				parent.startActivity(intent);
			}
		});
		builder.setNegativeButton("Intentar de nuevo", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	private void showHistory( String details ) {
		if( dniTxt.getText().toString().trim().equalsIgnoreCase("") || 
				cardTxt.getText().toString().trim().equalsIgnoreCase("") ) {
			Toast.makeText(parent, "Debe completar D.N.I y su número de Tarjeta para continuar.", Toast.LENGTH_LONG).show();
			
			return;
		}
		
		AppStats.addSectionUse(parent, AppStats.BUS_HISTORY);
			
		Intent intent = new Intent(parent, com.speryans.redbus.History.class);
		intent.putExtra(KeySaver.DNI_KEY, dniTxt.getText().toString().trim());
		intent.putExtra(KeySaver.CARD_KEY, cardTxt.getText().toString().trim());
		intent.putExtra("details", details);
		
		parent.startActivity(intent);
	}
	
	public void loadHistoryCards() {
		this.accountCardContainer.removeAllViews();
		
		JSONArray data = CardsManagement.getCards(parent);
		
		for( int i = 0 ; i < data.length() ; i++ ) {
			final JSONObject obj = data.optJSONObject(i);
			
			View v = LayoutInflater.from(parent).inflate(R.layout.row, null, false);
			
			((TextView) v.findViewById(R.id.label)).setText( "  " + obj.optString("dni") );
			((TextView) v.findViewById(R.id.sublabel)).setText( "  " + obj.optString("card_number") );
			((LinearLayout) v.findViewById(R.id.leftLayout)).setVisibility(View.GONE);
			
			((TextView) v.findViewById(R.id.thirdLabel)).setTypeface(parent.icons);
			((TextView) v.findViewById(R.id.thirdLabel)).setText(" \uf119 ");
			
			((LinearLayout) v.findViewById(R.id.rightLayout)).setOnClickListener( new OnClickListener() {

				@Override
				public void onClick(View v) {
					CardsManagement.removeCard(parent, obj);
					
					loadHistoryCards();
				}
				
			});
			
			((LinearLayout) v.findViewById(R.id.dataLayout)).setOnClickListener( new OnClickListener() {

				@Override
				public void onClick(View v) {
					dniTxt.setText( obj.optString("dni") );
					cardTxt.setText( obj.optString("card_number") );
					
					dniTxt.requestFocus();
				}
				
			});
			
			accountCardContainer.addView(v);
		}
	}
	
	public void positionUpdated(double latitude, double longitude) {
		try {
			Cursor cursor = parent.database.getCenters(latitude, longitude, 3);
			
			try {
				if( cursor.getCount() > 0 ) {
					if( cursor.moveToFirst() ) {
						// Remove views from suggested.
						suggested.removeAllViews();
						
						do {
							View v = LayoutInflater.from(parent).inflate(R.layout.row, null, false);
							
							((TextView) v.findViewById(R.id.label)).setText("" + cursor.getString( cursor.getColumnIndex("address") ));
							((TextView) v.findViewById(R.id.sublabel)).setText("" + cursor.getString( cursor.getColumnIndex("barrio") ));
							((LinearLayout) v.findViewById(R.id.rightLayout)).setVisibility(View.GONE);
							((TextView) v.findViewById(R.id.iconTxt)).setTypeface(parent.icons);
							
							final String address = cursor.getString( cursor.getColumnIndex("address") );
							final String barrio = cursor.getString( cursor.getColumnIndex("barrio") );
							final double lat = cursor.getDouble( cursor.getColumnIndex("lat") );
							final double lng = cursor.getDouble( cursor.getColumnIndex("lng") );
							final String tipo = cursor.getString( cursor.getColumnIndex("tipo") );
							
							v.setOnClickListener( new OnClickListener() {
		
								@Override
								public void onClick(View v) {
									Intent intent = new Intent( parent, com.speryans.redbus.MapDetails.class );
									intent.putExtra("address", address);
									intent.putExtra("barrio", barrio);
									intent.putExtra("lat", lat);
									intent.putExtra("lng", lng);
									intent.putExtra("tipo", tipo);
									
									parent.startActivity( intent );
								}
								
							});
							
							suggested.addView(v);
						} while( cursor.moveToNext() );
					}
				}
			} catch(Exception e) {
				try { cursor.close(); } catch( Exception ex ) {}
			} finally {
				cursor.close();
			}
		} catch( Exception e ) {
			((LinearLayout) parent.findViewById(R.id.rbCentersContainer)).setVisibility(View.GONE);
		}
	}

	@Override
	public void ready(String url, String data) {
		if( url == Urls.SALDO_SEND_URL ) {
			processMessage( data );
		}
		if( url == Urls.SALDO_URL ) {
			this.processSession(data);
		}
	}

}
