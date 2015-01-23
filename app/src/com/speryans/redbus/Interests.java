package com.speryans.redbus;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
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

import com.WazaBe.HoloEverywhere.HoloAlertDialogBuilder;
import com.speryans.redbus.classes.Commons;
import com.speryans.redbus.classes.En5EstoyActivity;
import com.speryans.redbus.classes.FavoritesManagement;
import com.speryans.redbus.classes.Typefaces;
import com.speryans.redbus.stadistics.AppStats;

public class Interests extends En5EstoyActivity {
	
	private TextView detailsTxt;
	private LinearLayout cardsContainer;
	
	private String json_data;
	private LinearLayout container;
	
	private TextView titleTxt;
	
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.interest_points);
		
		container = (LinearLayout) this.findViewById(R.id.container);
		
		fontEverywhere(container, roboto);
		
		this.buttonsFont(container, icons);
		
		Commons.createHeader(this);
		
		json_data = this.getIntent().getStringExtra("data");
		
		titleTxt = ( TextView ) this.findViewById( R.id.titleTxt );
		titleTxt.setText( this.getIntent().getStringExtra("category") );
		
		detailsTxt = ( TextView ) this.findViewById( R.id.detailsTxt );
		
		if(titleTxt.getText().toString().equalsIgnoreCase("Favorito")) {
			detailsTxt.setText( R.string.interest_favorite );
		} else {
			detailsTxt.setText( R.string.interest_points );
		}
		
    	cardsContainer = ( LinearLayout ) this.findViewById(R.id.cardsContainer);
    	
    	((TextView) this.findViewById(R.id.iconDetailsTxt)).setTypeface(icons);
		
		((TextView) this.findViewById(R.id.iconHeader)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				finish();
			}
		});
    	
    	try {
			JSONArray data = new JSONArray(json_data);
				
			for( int i = 0 ; i < data.length() ; i++ ) {
				final JSONObject obj = data.getJSONObject(i);
				
				try {
					final JSONObject location = obj.getJSONObject("location");
					final JSONObject contact = obj.getJSONObject("contact");
					final JSONObject category = obj.getJSONArray("categories").getJSONObject(0);
					
					View v = LayoutInflater.from(this).inflate(R.layout.point_row, null, false);
					
					final String id = obj.getString("id");
					
					final String title = obj.getString("name");
				
					String description = location.getString("address");
					description += "\nEstas a " + location.getString("distance") + " mts";
				
					description += "\n\n" + category.getString("shortName");
				
					((TextView) v.findViewById(R.id.cardTitleTxt)).setText( title );
					((TextView) v.findViewById(R.id.cardTitleTxt)).setTypeface(roboto);
					((TextView) v.findViewById(R.id.cardDetailsTxt)).setText( description );
					((TextView) v.findViewById(R.id.cardDetailsTxt)).setTypeface(roboto);
					
					((TextView) v.findViewById(R.id.providerIcon)).setTypeface(Typefaces.get(Interests.this, "fonts/glyphs.ttf"));
					// foursquare.com/v/{id}
					((TextView) v.findViewById(R.id.providerIcon)).setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							showBrowser(title, "https://foursquare.com/v/" + id);
						}
						
					});
				
					final String endAddress = location.getString("lat") + "," + location.getString("lng");
				
					final String phoneNumber = ( contact.has("phone") ? contact.getString("phone").replace("+54", "0") : "" );
				
					final Button callButton = (Button) v.findViewById(R.id.callButton);
					if( contact.has("phone") ) {
						callButton.setText("Llamar " + phoneNumber);
					} else {
						callButton.setText(R.string.interest_nonumber);
						callButton.setEnabled(false);
					}
					callButton.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							try {
								AppStats.addSuccesfulCases(Interests.this, title);
							
								Intent callIntent = new Intent(Intent.ACTION_CALL);
								callIntent.setData(Uri.parse("tel:" + phoneNumber));
								startActivity(callIntent);
							} catch (ActivityNotFoundException e) {
								Commons.error("Error al hacer la llamada.");
							}
						}
					});
					
					Button editBtn = (Button) v.findViewById(R.id.editBtn); 
					editBtn.setVisibility(View.GONE);
					editBtn.setTypeface(icons);
					
					/*editBtn.setOnClickListener( new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							AlertDialog.Builder builder = new HoloAlertDialogBuilder(Interests.this);
							builder.setInverseBackgroundForced(true);
							builder.setCancelable(true);
							builder.setTitle(R.string.application_name);
							View v = LayoutInflater.from(Interests.this).inflate(R.layout.inputdialog, null, false);
							TextView dtxt = (TextView) v.findViewById(R.id.descriptionTxt);
							dtxt.setText(R.string.change_number_string);
							
							final EditText inField = ( EditText ) v.findViewById(R.id.editField);
							inField.setInputType(InputType.TYPE_CLASS_PHONE);
							inField.setHint( phoneNumber );
							inField.setText( phoneNumber );
							
							builder.setView( v );
							builder.setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									if(inField.getText().toString().trim().equalsIgnoreCase("") ) {
										Toast.makeText(Interests.this, "El campo de n�mero no debe quedar vac�o.", Toast.LENGTH_SHORT).show();
										
										return;
									}
									if( inField.getText().toString().trim().length() < 7 ) {
										Toast.makeText(Interests.this, "El n�mero debe tener al menos 7 d�gitos.", Toast.LENGTH_SHORT).show();
										
										return;
									}
									
									ArrayList<NameValuePair> values = new ArrayList<NameValuePair>();
									values.add(new BasicNameValuePair("vid", id));
									values.add(new BasicNameValuePair("number", inField.getText().toString().trim()));
									
									AppStats.addPhoneChanged( title + " ( " + id + " ) " + phoneNumber + " ==> " + inField.getText().toString().trim() + " > " + location.optString("address") + " { " + KeySaver.getDeviceID(Interests.this) + " }" );
									
									Connection.postInstance(Urls.POINTS_EDIT, new ConnectionListener() {

										@Override
										public void ready(int msg, String message) {
											try {
												JSONObject obj = new JSONObject(message);
												if( obj.getBoolean("result") ) {
													callButton.setText("Llamar " + inField.getText().toString().trim());
													
													Toast.makeText(Interests.this, "El n�mero fue modificado con �xito. Agradecemos su colaboraci�n.", Toast.LENGTH_SHORT).show();
												}
											} catch(Exception e) {
												Toast.makeText(Interests.this, "Se produjo un error al guardar el n�mero. Intente nuevamente m�s tarde.", Toast.LENGTH_SHORT).show();
											}
										}

										@Override public void cacheReady(int msg, String message) {}
										@Override public void onNoConnection(int msg) {}
										@Override public void onTimeOut(int msg) {}
									}, 0, values).start();
								}
							});
							builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
								@Override public void onClick(DialogInterface dialog, int which) { }
							});
							
							AlertDialog dialog = builder.create();
							dialog.show();
						}
						
					});*/
				
					// Ir hasta la parada por ahora
					Button navigationButton = (Button) v.findViewById(R.id.navigationButton);
					navigationButton.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/?daddr=" + endAddress)); 
							startActivity(myIntent);
						}
					});
				
					final Button favoriteBtn = (Button) v.findViewById(R.id.favoriteBtn);
					if( FavoritesManagement.isFavorite(this, obj) ) {
						favoriteBtn.setText("Eliminar de mis\nfavoritos");
					}
					favoriteBtn.setOnClickListener( new OnClickListener() {

						@Override
						public void onClick(View v) {
							if( FavoritesManagement.isFavorite(Interests.this, obj) ) {
								favoriteBtn.setText("Agregar a mis\nfavoritos");
								FavoritesManagement.removeFavorite(Interests.this, obj);
							} else {
								favoriteBtn.setText("Eliminar de mis\nfavoritos");
								FavoritesManagement.addFavorite(Interests.this, obj);
							}
						}
					
					});
					
					cardsContainer.addView(v);
				} catch( Exception ex ) {
				}
			}
		} catch( Exception e ) {
			Commons.error(e.toString(), e);
		}
	}
	
	public void showBrowser(String title, final String url) {
		AlertDialog.Builder builder = new HoloAlertDialogBuilder(this);
		builder.setInverseBackgroundForced(true);
		builder.setCancelable(true);
		builder.setTitle(R.string.application_name);
		TextView view = (TextView)LayoutInflater.from(this).inflate(R.layout.workarounddialogs, null, false);
		view.setText(String.format(getString(R.string.interests_open), title));
		builder.setView( view );
		builder.setPositiveButton("Continuar", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				try {
					Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse( url ));
					Interests.this.startActivity(browser);
				} catch( Exception e ) {
					Toast.makeText(Interests.this, R.string.interests_error, Toast.LENGTH_LONG).show();
				}
			}
		});
		builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	public static String UppercaseFirstLetters(String str) {
	    boolean prevWasWhiteSp = true;
	    char[] chars = str.toCharArray();
	    for (int i = 0; i < chars.length; i++) {
	        if (Character.isLetter(chars[i])) {
	            if (prevWasWhiteSp) {
	                chars[i] = Character.toUpperCase(chars[i]);    
	            }
	            prevWasWhiteSp = false;
	        } else {
	            prevWasWhiteSp = Character.isWhitespace(chars[i]);
	        }
	    }
	    return new String(chars);
	}
	
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}


}
