package com.speryans.redbus;

import org.codechimp.apprater.AppRater;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.WazaBe.HoloEverywhere.HoloAlertDialogBuilder;
import com.speryans.redbus.classes.Commons;
import com.speryans.redbus.classes.DatabaseWrapper;
import com.speryans.redbus.classes.En5EstoyActivity;
import com.speryans.redbus.classes.GeoLocator;
import com.speryans.redbus.classes.GeoLocator.PositionListener;
import com.speryans.redbus.classes.KeySaver;
import com.speryans.redbus.classes.Typefaces;
import com.speryans.redbus.classes.connection.ErrorListener;
import com.speryans.redbus.classes.helpers.CityManager;
import com.speryans.redbus.sections.Alarm;
import com.speryans.redbus.sections.Commerce;
import com.speryans.redbus.sections.Footer;
import com.speryans.redbus.sections.Notifications;
import com.speryans.redbus.sections.Panel;
import com.speryans.redbus.sections.RedBus;
import com.speryans.redbus.sections.Taxi;
import com.speryans.redbus.sections.Transport;
import com.speryans.redbus.sections.Wizard;

/**
 * TODO
 * 
 * - Actualizar los favoritos en background. Si actualizas un numero modificarlo tambien.
 * 
 * - Actualizar DB
 * 
 * Generales
 * - Reporte de transito
 * - Pedido de taxi.
 * 
 * 
 * keytool -list -alias androiddebugkey -keystore ~/.android/debug.keystore -storepass android -keypass android -v
 * keytool -list -alias "cronos development" -keystore ~/Documents/cronosdevelopment -storepass 123456789 -keypass 123456789 -v
 * 
 * 
 * 
 * @author sarriaroman
 *
 */

public class Home extends En5EstoyActivity implements PositionListener, ErrorListener {
	public static final String ACTUAL_CITY = ", cordoba, argentina";
	
	private String[] autocomplete = new String[] {};
	
	private LinearLayout container;

	public GeoLocator location;
	public SharedPreferences sharedSettings;
	public DatabaseWrapper database;
	// --
	
	public ImageLoader imgLoader;
	
	public TextView titleTxt;
	
	public LinearLayout alarmContainer;
	public LinearLayout busContainer;
	public LinearLayout taxiContainer;
	public LinearLayout interestContainer;
	public LinearLayout newsContainer;
	public LinearLayout accountContainer;
	
	private boolean isLines = false;

	private LinearLayout newAppVersion;

	public TextView iconHeader;

	// All sections are public
	public RedBus red_bus = null;
	public Taxi taxi = null;
	public Notifications notifications = null;
	public Wizard wizard = null;
	public Footer footer = null;
	public Panel panel = null;
	public Transport transport = null;
	public Commerce commerce = null;
	public Alarm alarm = null;

	public TextView shareBtn;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.home);
		
		// Just save a city inside
		if( CityManager.getCity(this) == "" ) {
			Intent wzrd = new Intent(this, com.speryans.redbus.Wizard.class);
			this.startActivity(wzrd);
			
			this.finish();
			
			return;
		}
		
		location = GeoLocator.getInstance(this);
		location.setListener(this);
		
		// General classes
		sharedSettings = this.getSharedPreferences(Commons.TAG, 0);
		
		imgLoader = new ImageLoader( this, false, R.drawable.transparent );
		
		TextView sepTxt = (TextView) this.findViewById(R.id.sepHeaderTxt);
		sepTxt.setTypeface(icons);
		
		iconHeader = (TextView) this.findViewById(R.id.iconHeader);
		iconHeader.setTypeface(icons);
		
		this.createShareButton();
		
		if( android.os.Build.VERSION.SDK_INT >= 19 ) {
			((ImageView) this.findViewById(R.id.topShadow)).setVisibility(View.GONE);
		}
		
		titleTxt = (TextView) this.findViewById(R.id.titleTxt);
		titleTxt.setTypeface(Typefaces.get(this, "fonts/roboto-regular.ttf"));
		
		container = (LinearLayout) this.findViewById(R.id.container);
		
		alarmContainer = (LinearLayout) this.findViewById(R.id.alarmContainer);
		busContainer = (LinearLayout) this.findViewById(R.id.busContainer);
		accountContainer = (LinearLayout) this.findViewById(R.id.accountContainer);
		taxiContainer = (LinearLayout) this.findViewById(R.id.taxiContainer);
		interestContainer = (LinearLayout) this.findViewById(R.id.interestsContainer);
		newsContainer = (LinearLayout) this.findViewById(R.id.newsContainer);
		
		//newAppVersion = (LinearLayout) this.findViewById(R.id.newAppVersion);
		
		busContainer.setVisibility(View.GONE);
		taxiContainer.setVisibility(View.GONE);
		
		fontEverywhere(container, roboto);
		
		this.buttonsFont(container, icons);
		
		panel = new Panel();
		panel.onCreate(savedInstanceState, this);
        
        // Notifications
     	notifications = new Notifications();
     	notifications.onCreate(null, Home.this);
     	
     	// Alarm Section
     	alarm = new Alarm();
		alarm.onCreate(savedInstanceState, this);
		
     	// Check available features
		this.checkFeatures(savedInstanceState);
		
		// Footer handler
		footer = new Footer();
		footer.onCreate(savedInstanceState, this);
		
		//this.loadAutocomplete();
		
		this.checkAppVersion();
		
		//this.checkBlocked();
		
		// Load Wizard functionality
		wizard = new Wizard();
		wizard.onCreate(savedInstanceState, this);
		
		// Rater
		AppRater.app_launched(this);
	}
	
	private void createShareButton() {
		shareBtn = (TextView) this.findViewById(R.id.shareBtn);
		shareBtn.setTypeface(icons);
		shareBtn.setVisibility(View.VISIBLE);
		shareBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent share = new Intent(Intent.ACTION_SEND);
				share.setType("text/plain");
				share.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app));
				startActivity(Intent.createChooser(share, "Compartir"));
			}
			
		});
	}

	private void checkFeatures(final Bundle savedInstanceState) {
		try {
			JSONArray features = CityManager.getFeatures(this);
			Commons.info("Current features: " + features.toString());
			
			boolean transport_av = false;
			
			// Disable everything
			panel.busButton.setVisibility(View.GONE);
			busContainer.setVisibility(View.GONE);
			panel.taxiButton.setVisibility(View.GONE);
			taxiContainer.setVisibility(View.GONE);
			panel.interestButton.setVisibility(View.GONE);
			panel.favoritesLayout.setVisibility(View.GONE);
			panel.accountButton.setVisibility(View.GONE);
			
			// Que hacer cuando la sección Transporte no esta?... Taxi debe estar siempre
			for( int i = 0 ; i < features.length() ; i++ ) {
				String feature = features.getString(i);
				
				if( feature.equalsIgnoreCase("transport") ) {
					// Transport Section
					transport = new Transport();
					transport.onCreate(savedInstanceState, this);
					
					// Enable Panel
					panel.busButton.setVisibility(View.VISIBLE);
					titleTxt.setText(R.string.colectivos);
					busContainer.setVisibility(View.VISIBLE);
					
					transport_av = true;
				}
				
				if( feature.equalsIgnoreCase("taxi") ) {
					// Auto Show Taxi
					if( !transport_av ) {
						titleTxt.setText(R.string.taxis);
						taxiContainer.setVisibility(View.VISIBLE);
					}
					
					// Taxi Section
			        taxi = new Taxi();
			        taxi.onCreate(savedInstanceState, this);
			        
			        // Enable Panel
					panel.taxiButton.setVisibility(View.VISIBLE);
				}
				
				if( feature.equalsIgnoreCase("commerce") ) {
					// Commerce Section
					commerce = new Commerce();
					commerce.onCreate(savedInstanceState, this);
					
					// Enable Panel
					panel.interestButton.setVisibility(View.VISIBLE);
					panel.favoritesLayout.setVisibility(View.VISIBLE);
				}
				
				if( feature.equalsIgnoreCase("redbus") ) {
					// RedBus Section
			        red_bus = new RedBus();
			        red_bus.onCreate(savedInstanceState, this);
					
					// Enable Panel
					panel.accountButton.setVisibility(View.VISIBLE);
				}
			}
		} catch (JSONException e) {
			Commons.error("Problems loading city features");
		}
	}

	protected void checkAppVersion() {
		/*Connection.getInstance(Urls.VERSION, new ConnectionListener() {

			@Override
			public void ready(int msg, String message) {
				try {
					JSONObject data = new JSONObject( message );
					
					PackageInfo info = Home.this.getPackageManager().getPackageInfo( Home.this.getPackageName(), 0);
					
					if( info.versionCode < data.getInt("version") ) {
						Home.this.newAppVersion.setVisibility(View.VISIBLE);
						Home.this.newAppVersion.setOnClickListener( new OnClickListener() {

							@Override
							public void onClick(View v) {
								Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse( "https://play.google.com/store/apps/details?id=com.speryans.redbus" ));
								Home.this.startActivity(browser);
							}
							
						});
					}
				} catch( Exception ex ) {
					Commons.error("Error version " + ex.toString(), ex);
				}
			}

			@Override public void cacheReady(int msg, String message) {}
			@Override public void onNoConnection(int msg) {}
			@Override public void onTimeOut(int msg) {}
		}, 0).start();*/
	}
	
	/*protected void checkBlocked() {
		ArrayList<NameValuePair> values = new ArrayList<NameValuePair>();
		values.add(new BasicNameValuePair("udid", KeySaver.getDeviceID(this)));
		
		Connection.postInstance(Urls.BLOCKED, new ConnectionListener() {

			@Override
			public void ready(int msg, String message) {
				try {
					JSONObject data = new JSONObject( message );
					
					if( data.getBoolean("result") ) {
						Home.this.showBlocked();
					}
				} catch( Exception ex ) {}
			}

			@Override public void cacheReady(int msg, String message) {}
			@Override public void onNoConnection(int msg) {}
			@Override public void onTimeOut(int msg) {}
		}, 0, values).start();
	}*/
	
	

	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		
		if( transport != null ) {
			if( v == transport.linesBtn ) {
				menu.setHeaderTitle("Seleccione una linea...");
		
				for( int i = 0 ; i < transport.lines.length() ; i++ ) {
					menu.add(0, i, 0, transport.lines.optJSONObject(i).optString("name"));
				}
			
				isLines = true;
			}
		} 
		if( commerce != null ) {
			if( v == commerce.categoriesBtn ) {
				menu.setHeaderTitle("Seleccione una categoria...");
		
				menu.add(0, -1, 0, "Todas las categorias");
		
				for( int i = 0 ; i < commerce.categories.length() ; i++ ) {
					JSONObject obj = commerce.categories.optJSONObject(i);
					menu.add(0, i, 0, obj.optString("name"));
				}
			
				isLines = false;
			}
		}
	}
	
	public boolean onContextItemSelected(MenuItem item) {
		try {
			if( isLines ) {
				transport.linesBtn.setText(item.getTitle());
				transport.selectedLine = transport.lines.optJSONObject( item.getItemId() ).optString("_id");
			} else {
				commerce.selectedCategory = (item.getItemId() == -1 ) ? "-1" : commerce.categories.optJSONObject(item.getItemId()).optString("id");
				commerce.categoriesBtn.setText( item.getTitle() );
			}
		} catch( Exception e ) {
			
		}
		
		return true;
	}
	
	private void loadAutocomplete() { 
		String acmpt = KeySaver.getSaved(this, KeySaver.AUTOCOMPLETE);
		if( acmpt != null ) {
			autocomplete = acmpt.split(",");
		}
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line, autocomplete);
		
		// Set adapters
		if( transport != null ) {
			transport.fromTxt.setAdapter(adapter);
			transport.toTxt.setAdapter(adapter);
		}
		if( taxi != null ) {
			taxi.fromTxt.setAdapter(adapter);
			taxi.toTxt.setAdapter(adapter);
		}
		
		// Update autocomplete
		/*Connection.getInstance(Urls.STREETS, new ConnectionListener() {
			@Override
			public void ready(int msg, String message) {
				try {
					autocomplete = message.split(",");
					
					KeySaver.save(Home.this, KeySaver.AUTOCOMPLETE, message);
					
					ArrayAdapter<String> adapter = new ArrayAdapter<String>(Home.this,
							android.R.layout.simple_dropdown_item_1line, autocomplete);
					
					// Set adapters
					if( transport != null ) {
						transport.fromTxt.setAdapter(adapter);
						transport.toTxt.setAdapter(adapter);
					}
					if( taxi != null ) {
						taxi.fromTxt.setAdapter(adapter);
						taxi.toTxt.setAdapter(adapter);
					}
				} catch( Exception e ) {}
			}
			@Override
			public void cacheReady(int msg, String message) {}
			@Override
			public void onNoConnection(int msg) {}
			@Override
			public void onTimeOut(int msg) {}
		}, 1001).start();*/
	}
	
	public void showBlocked() {
		AlertDialog.Builder builder = new HoloAlertDialogBuilder(this);
		
		builder.setInverseBackgroundForced(true);
		builder.setCancelable(false);
		builder.setTitle(R.string.application_name);
		TextView view = (TextView)LayoutInflater.from(this).inflate(R.layout.workarounddialogs, null, false);
		view.setText(R.string.blocked_popup);
		builder.setView( view );
		builder.setPositiveButton("Contacto", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

				/* Fill it with Data */
				emailIntent.setType("plain/text");
				emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"info@en5estoy.com"});
				emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Dispositivo bloqueado: " + Commons.getDeviceID(Home.this));

				/* Send it off to the Activity-Chooser */
				startActivity(Intent.createChooser(emailIntent, "Contacto T?cnico"));
				
				Home.this.finish();
			}
		});
		builder.setNegativeButton("Salir", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Home.this.finish();
			}
		});
		
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_MENU) {
	    	if (panel.menu.isMenuShowing()) {
				panel.menu.showContent();
			} else {
				panel.menu.showMenu();
			}
	    	
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onBackPressed() {
		if (panel.menu.isMenuShowing()) {
			panel.menu.showContent();
		} else {
			super.onBackPressed();
		}
	}
	

	@Override
	public void positionUpdated(double latitude, double longitude) {
		if( red_bus != null ) {
			red_bus.positionUpdated(latitude, longitude);
		}
	}
	
	
	@Override
	public void onError(String url, int msg) {
		Commons.error("Error: " + url + " => " + msg);
	}
}
