package com.speryans.redbus.sections;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.speryans.redbus.R;
import com.speryans.redbus.classes.Commons;
import com.speryans.redbus.classes.FavoritesManagement;
import com.speryans.redbus.classes.ProgressHUD;
import com.speryans.redbus.classes.SectionActivity;
import com.speryans.redbus.classes.Urls;
import com.speryans.redbus.classes.connection.Connection;
import com.speryans.redbus.classes.connection.DoneListener;
import com.speryans.redbus.classes.helpers.GeocodeHelper;
import com.speryans.redbus.classes.helpers.GeocodeListener;
import com.speryans.redbus.stadistics.AppStats;

public class Transport extends SectionActivity {

	protected static final int BUS_FROM = 60;
	protected static final int BUS_TO = 61;
	protected static final int BUS = 65;
	
	protected static final int LINES = 66;
	
	private String busFrom = "";
	private String busTo = "";
	private double busFromLat = 0;
	private double busFromLon = 0;
	private double busToLat = 0;
	private double busToLon = 0;
	
	public AutoCompleteTextView toTxt;
	public AutoCompleteTextView fromTxt;
	
	private Button searchBusBtn;
	public Button linesBtn;
	private Button searchLinesBtn;
	
	public JSONArray lines = null;
	public String selectedLine = "";
	private Button changePointsBtn;
	
	public void onCreated() {
		fromTxt = ( AutoCompleteTextView ) parent.findViewById(R.id.fromBusTxt);
		fromTxt.setThreshold(0);
		toTxt = ( AutoCompleteTextView ) parent.findViewById(R.id.toBusTxt);
		fromTxt.setThreshold(0);
		
		changePointsBtn = ( Button ) parent.findViewById(R.id.changePointsBtn);
		changePointsBtn.setTypeface(parent.icons);
		
		changePointsBtn.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				String tfrom = fromTxt.getText().toString();
				fromTxt.setText( toTxt.getText().toString() );
				toTxt.setText(tfrom);
			}
			
		});
		
		searchBusBtn = ( Button ) parent.findViewById(R.id.searchBusBtn);
		searchBusBtn.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				busFromLat = 0;
				busFromLon = 0;
				busToLat = 0;
				busToLon = 0;
				
				if( fromTxt.getText().toString().trim().equalsIgnoreCase("") &&
						toTxt.getText().toString().trim().equalsIgnoreCase("")) {
					Toast.makeText( parent, "Debe completar al menos un campo para poder realizar el cálculo.", Toast.LENGTH_LONG).show();
					return;
				}
				
				AppStats.addSectionUse(parent, AppStats.BUSES_SEARCH);
				
				ProgressHUD.show(parent, "Obteniendo su ruta. Aguarde...", true, null);
				
				if( fromTxt.getText().toString().trim().equalsIgnoreCase("") ) {
					busFrom = "Tu ubicación actual";
					
					busFromLat = parent.location.latitude;
					busFromLon = parent.location.longitude;
				} else {
					String fieldContent = fromTxt.getText().toString().trim();
					
					(new GeocodeHelper(parent)).resolve(fieldContent, new GeocodeListener() {

						@Override
						public void geocode_ready(String address, double lat, double lon) {
							ProgressHUD.dismissHUD();
							
							Commons.info(address);
							busFrom = address;
						
							busFromLat = lat;
							busFromLon = lon;
						
							makeBusMaths();
						}

						@Override public void geocode_error() {}
					});
					
					if( toTxt.getText().toString().trim().equalsIgnoreCase("") ) {
						busTo = parent.getString(R.string.your_location);
						
						busToLat = parent.location.latitude;
						busToLon = parent.location.longitude;
					}
					
					return;
				}
				
				if( toTxt.getText().toString().trim().equalsIgnoreCase("") ) {
					busTo = parent.getString(R.string.your_location);
					
					busToLat = parent.location.latitude;
					busToLon = parent.location.longitude;
				} else {
					String fieldContent = toTxt.getText().toString().trim();
					
					(new GeocodeHelper(parent)).resolve(fieldContent, new GeocodeListener() {

						@Override
						public void geocode_ready(String address, double lat, double lon) {
							ProgressHUD.dismissHUD();
							
							busTo = address;
						
							busToLat = lat;
							busToLon = lon;
						
							if( busFromLat != 0 && busFromLon != 0 ) {
								makeBusMaths();
							}
						}

						@Override public void geocode_error() {}
					});
				}
			}
			
		});
		
		// -------- LINES ---------
		linesBtn = ( Button ) parent.findViewById(R.id.linesBtn);
		parent.registerForContextMenu(linesBtn);
		linesBtn.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				if( lines == null ) {
					ProgressHUD.show(parent, "Actualizando lineas...", true, null);
					
					Connection.post(parent, Urls.LINES)
						.param("udid", Commons.getDeviceID(parent))
						.done(new DoneListener() {

							@Override
							public void ready(String url, String message) {
								ProgressHUD.dismissHUD();
								
								try {
									lines = (new JSONObject(message)).getJSONArray("data");
									
									parent.openContextMenu(linesBtn);
								} catch (Exception e) {
									Toast.makeText(parent, "Se produjo un error cargando las lineas.", Toast.LENGTH_SHORT).show();
								}
							}
							
						})
						.error(parent)
						.start();
				} else {
					// open menu
					parent.openContextMenu(linesBtn);
				}
			}
			
		});
		
		searchLinesBtn = (Button) parent.findViewById(R.id.searchLinesBtn);
		searchLinesBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if( linesBtn.getText() == parent.getString(R.string.lines_btn) ) {
					Toast.makeText(parent, R.string.select_a_line, Toast.LENGTH_SHORT).show();
					
					return;
				}
				
				try {
					Intent intent = new Intent(parent, com.speryans.redbus.Lines.class);
					intent.putExtra("line", linesBtn.getText().toString() );
					intent.putExtra("_id", selectedLine );
						
					parent.startActivity(intent);
				} catch( Exception e ) {
					
				}
			}
			
		});
		// ------------------------

	}
	
	/*
	public void ready(int msg, String message) {
		if( msg == Transport.BUS_FROM ) {
			try {
				JSONObject result = new JSONObject(message);
				JSONArray results = result.getJSONArray("results");
				
				if( results.length() > 0 ) {
					ProgressHUD.dismissHUD();
					
					if( results.length() == 1 ) {
						JSONObject address = results.getJSONObject(0);
						busFrom = address.getString("formatted_address");
					
						JSONObject location = address.getJSONObject("geometry").getJSONObject("location");
					
						this.busFromLat = location.getDouble("lat");
						this.busFromLon = location.getDouble("lng");
					
						this.makeBusMaths();
					} else {
						this.placesDialog(results, msg);
					}
				} else {					
					AppStats.addAddress(parent, fromTxt.getText().toString());
					
					// Si llega aca cruzar con comercios y obtener lat y lng para llevar directo.
					ArrayList<NameValuePair> values = new ArrayList<NameValuePair>();
					values.add(new BasicNameValuePair("lat", String.valueOf(parent.location.latitude)));
					values.add(new BasicNameValuePair("lon", String.valueOf(parent.location.longitude)));
					values.add(new BasicNameValuePair("category", "-1"));
					values.add(new BasicNameValuePair("sort", "0"));
					values.add(new BasicNameValuePair("search", fromTxt.getText().toString() ));
					
					Connection.postInstance(Urls.POINTS_SEARCH, new ConnectionListener() {
						@Override
						public void ready(int msg, String message) {
							try {
								ProgressHUD.dismissHUD();
								
								JSONObject data = new JSONObject(message);
								
								if( data.getJSONArray("venues").length() > 0 ) {
									JSONArray venues = data.getJSONArray("venues");
									for( int i = 0 ; i < venues.length() ; i++ ) {
										JSONObject obj = venues.getJSONObject(i);
										
										if( obj.getJSONObject("location").has("address") ) {
											JSONObject address = obj.getJSONObject("location");
											busFrom = address.getString("address");
										
											busFromLat = address.getDouble("lat");
											busFromLon = address.getDouble("lng");
										
											makeBusMaths();
											
											return;
										}
									}
								} else {
									Toast.makeText(parent, "No se ha podido obtener informaci�n sobre la direcci�n ingresada. Por favor revise.", Toast.LENGTH_SHORT).show();
								}
							} catch( Exception e ) {}
						}
						@Override public void cacheReady(int msg, String message) {}
						@Override public void onNoConnection(int msg) {}
						@Override public void onTimeOut(int msg) {}
					}, 0, values).start();
					// ------------
				}
			} catch( Exception ex ) {
				Toast.makeText(parent, "No se ha podido obtener información sobre la dirección ingresada. Por favor revise.", Toast.LENGTH_SHORT).show();
				AppStats.addAddress(parent, this.fromTxt.getText().toString());
			}
		}
		if( msg == Transport.BUS_TO ) {
			try {
				JSONObject result = new JSONObject(message);
				JSONArray results = result.getJSONArray("results");
				
				if( results.length() > 0 ) {
					ProgressHUD.dismissHUD();
					
					if( results.length() == 1 ) {
						JSONObject address = results.getJSONObject(0);
						busTo = address.getString("formatted_address");
						if( busTo.equalsIgnoreCase("Córdoba, Córdoba Province, Argentina") ) {
							searchBusInPoints();
							
							return;
						}
						// "",
						
						JSONObject location = address.getJSONObject("geometry").getJSONObject("location");
					
						this.busToLat = location.getDouble("lat");
						this.busToLon = location.getDouble("lng");
					
						if( this.busFromLat != 0 && this.busFromLon != 0 ) {
							this.makeBusMaths();
						}
					} else {
						this.placesDialog(results, msg);
					}
				} else {
					AppStats.addAddress(parent, this.toTxt.getText().toString());
					
					searchBusInPoints();
				}
			} catch( Exception ex ) {
				Toast.makeText(parent, "No se ha podido obtener información sobre la dirección ingresada. Por favor revise.", Toast.LENGTH_SHORT).show();
				
				AppStats.addAddress(parent, this.toTxt.getText().toString());
			}
		}
		if( msg == BUS ) {
					}
	}*/
	
	/**
	 * Volver a agregar esto con las Venues pero no sólo la primera sinó un listado.
	 * 
	 * ** Agregar a GeocodeHelper reutilizando el selector de direcciones
	 */
	private void searchBusInPoints() {
		// Si llega aca mostrarle la tarjeta antes de llevarlo.
		ArrayList<NameValuePair> values = new ArrayList<NameValuePair>();
		values.add(new BasicNameValuePair("lat", String.valueOf(parent.location.latitude)));
		values.add(new BasicNameValuePair("lon", String.valueOf(parent.location.longitude)));
		values.add(new BasicNameValuePair("category", "-1"));
		values.add(new BasicNameValuePair("sort", "0"));
		values.add(new BasicNameValuePair("search", toTxt.getText().toString() ));
		
		/*Connection.postInstance(Urls.POINTS_SEARCH, new ConnectionListener() {
			@Override
			public void ready(int msg, String message) {
				try {
					ProgressHUD.dismissHUD();
					
					JSONObject data = new JSONObject(message);
					
					if( data.getJSONArray("venues").length() > 0 ) {
						JSONArray venues = data.getJSONArray("venues");
						for( int i = 0 ; i < venues.length() ; i++ ) {
							JSONObject obj = venues.getJSONObject(i);
							
							if( obj.getJSONObject("location").has("address") ) {
								placeDialog(data.getJSONArray("venues").getJSONObject(i));
								
								return;
							}
						}
					} else {
						Toast.makeText(parent, "No se ha podido obtener informaci�n sobre la direcci�n ingresada. Por favor revise.", Toast.LENGTH_SHORT).show();
					}
				} catch( Exception e ) {}
			}
			@Override public void cacheReady(int msg, String message) {}
			@Override public void onNoConnection(int msg) {}
			@Override public void onTimeOut(int msg) {}
		}, 0, values).start();*/
		// ------------
	}
	
	private void placeDialog( final JSONObject obj ) {
		final Dialog dialog = new Dialog(parent,R.style.ProgressHUD);
		dialog.setTitle("");
		dialog.setContentView(R.layout.place_hud);
		dialog.setCancelable(true);
		dialog.setOnCancelListener(null);
		dialog.getWindow().getAttributes().gravity=Gravity.CENTER;
		WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();  
		lp.dimAmount=0.2f;
		dialog.getWindow().setAttributes(lp); 
		
		try {
			final JSONObject location = obj.getJSONObject("location");
			final JSONObject contact = obj.getJSONObject("contact");
			final JSONObject category = obj.getJSONArray("categories").getJSONObject(0);
			
			final String title = obj.getString("name");
		
			String description = location.getString("address");
			description += "\nEstas a " + location.getString("distance") + " mts";
		
			description += "\n\n" + category.getString("shortName");
		
			((TextView) dialog.findViewById(R.id.cardTitleTxt)).setText( title );
			((TextView) dialog.findViewById(R.id.cardTitleTxt)).setTypeface(parent.roboto);
			((TextView) dialog.findViewById(R.id.cardDetailsTxt)).setText( description );
			((TextView) dialog.findViewById(R.id.cardDetailsTxt)).setTypeface(parent.roboto);
			
			((TextView) dialog.findViewById(R.id.providerIcon)).setTypeface(parent.icons);
		
			final String phoneNumber = ( contact.has("phone") ? contact.getString("phone").replace("+54", "0") : "" );
		
			final Button callButton = (Button) dialog.findViewById(R.id.callButton);
			if( contact.has("phone") ) {
				callButton.setText("Llamar " + phoneNumber);
			} else {
				callButton.setText("Sin número");
				callButton.setEnabled(false);
			}
			callButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						AppStats.addSuccesfulCases(parent, title);
					
						Intent callIntent = new Intent(Intent.ACTION_CALL);
						callIntent.setData(Uri.parse("tel:" + phoneNumber));
						parent.startActivity(callIntent);
					} catch (ActivityNotFoundException e) {
						Commons.error("Error al hacer la llamada.");
					}
				}
			});
		
			// Ir hasta la parada por ahora
			Button navigationButton = (Button) dialog.findViewById(R.id.goToPointBtn);
			navigationButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					busTo = location.optString("address");
					
					busToLat = location.optDouble("lat");
					busToLon = location.optDouble("lng");
				
					makeBusMaths();
					
					dialog.dismiss();
				}
			});
		
			final Button favoriteBtn = (Button) dialog.findViewById(R.id.favoriteBtn);
			if( FavoritesManagement.isFavorite(parent, obj) ) {
				favoriteBtn.setText("Eliminar de mis\nfavoritos");
			}
			favoriteBtn.setOnClickListener( new OnClickListener() {

				@Override
				public void onClick(View v) {
					if( FavoritesManagement.isFavorite(parent, obj) ) {
						favoriteBtn.setText("Agregar a mis\nfavoritos");
						FavoritesManagement.removeFavorite(parent, obj);
					} else {
						favoriteBtn.setText("Eliminar de mis\nfavoritos");
						FavoritesManagement.addFavorite(parent, obj);
					}
				}
			
			});
		} catch( Exception ex ) {}
		
		dialog.show();
	}
	
	private void makeBusMaths() {
		if( this.busToLat == 0 && this.busToLon == 0 ) {
			String fieldContent = toTxt.getText().toString();
			
			ProgressHUD.show(parent, "Obteniendo su ruta. Aguarde...", true, null);
			
			(new GeocodeHelper(parent)).resolve(fieldContent, new GeocodeListener() {

				@Override
				public void geocode_ready(String address, double lat, double lon) {
					ProgressHUD.dismissHUD();
					
					Commons.info(address);
					
					busTo = address;
				
					busToLat = lat;
					busToLon = lon;
				
					if( busFromLat != 0 && busFromLon != 0 ) {
						makeBusMaths();
					}
				}

				@Override public void geocode_error() {}
			});
			
			return;
		}
		
		ProgressHUD.show(parent, "Obteniendo su ruta. Aguarde...", true, null);
		
		Connection.post(parent, Urls.TRANSPORT)
			.param("udid", Commons.getDeviceID(parent))
			.param("lat", String.valueOf(this.busFromLat) )
			.param("lon", String.valueOf(this.busFromLon) )
			.param("to_lat", String.valueOf(this.busToLat) )
			.param("to_lon", String.valueOf(this.busToLon) )
			.done(new DoneListener() {

				@Override
				public void ready(String url, String message) {
					try {
						ProgressHUD.dismissHUD();
						
						if( (new JSONArray(message)).length() > 0 ) {
							Intent intent = new Intent(parent, com.speryans.redbus.Buses.class);
							intent.putExtra("from", busFrom);
							intent.putExtra("to", busTo);
							intent.putExtra("data", message);
							
							parent.startActivity(intent);
						} else {
							AppStats.addSectionUse(parent, AppStats.BUSES_NOT_FOUND);
							AppStats.addNotFoundData(parent, "From: " + fromTxt.getText().toString() + "( " + busFromLat + ", " + busFromLon + " ) ==> to: " +
									toTxt.getText().toString() + "( " + busToLat + ", " + busToLon + " )" );
							Toast.makeText(parent, R.string.transport_no_match, Toast.LENGTH_LONG).show();
						}
					} catch( Exception e ) {
						AppStats.addSectionUse(parent, AppStats.BUSES_NOT_FOUND);
						AppStats.addNotFoundData(parent, "[ERROR] From: " + fromTxt.getText().toString() + "( " + busFromLat + ", " + busFromLon + " ) ==> to: " +
								toTxt.getText().toString() + "( " + busToLat + ", " + busToLon + " )" );
						
						Toast.makeText(parent, R.string.transport_no_match, Toast.LENGTH_LONG).show();
					}
					
					busFrom = "";
					busTo = "";
				}
				
			})
			.error(parent)
			.start();
	}
}
