package com.speryans.redbus;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.slidingmenu.lib.SlidingMenu;
import com.speryans.redbus.adapters.PointsAdapter;
import com.speryans.redbus.classes.Commons;
import com.speryans.redbus.classes.DatabaseWrapper;
import com.speryans.redbus.classes.En5EstoyActivity;
import com.speryans.redbus.classes.GeoLocator;
import com.speryans.redbus.classes.ProgressHUD;

public class CentersPoints extends En5EstoyActivity {
	
	private DatabaseWrapper database;
	private LinearLayout centersContainer;
	private LinearLayout atentionContainer;
	private Button centersBtn;
	private Button atentionBtn;
	
	private GeoLocator location;
	private TextView titleTxt;
	private EditText searchTxt;
	private ListView listView;
	private Cursor cursor;
	private PointsAdapter adapter;
	private ScrollView atentionScroll;
	private LinearLayout container;
	
	private SlidingMenu menu;
	private TextView iconHeader;
	private LinearLayout recargaButton;
	private LinearLayout atencionButton;
	private LinearLayout searchLayout;
	private TextView shareBtn;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.points);
		
		container = (LinearLayout) this.findViewById(R.id.container);
		
		fontEverywhere(container, roboto);
		
		this.buttonsFont(container, icons);
		
		(( TextView ) this.findViewById( R.id.titleTxt )).setTypeface(roboto); 
		
		iconHeader = (TextView) this.findViewById(R.id.iconHeader);
		shareBtn = (TextView) this.findViewById(R.id.shareBtn);
		
		Commons.createHeader(this);
		
		menu = new SlidingMenu(this);
        menu.setMode(SlidingMenu.RIGHT);
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        menu.setShadowWidthRes(R.dimen.shadow_width);
        menu.setShadowDrawable(R.drawable.shadowright);
        menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        menu.setFadeDegree(0.35f);
        menu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
        menu.setMenu(R.layout.centers_panel);
        
        iconHeader.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				CentersPoints.this.finish();
			}
        	
        });
        
        shareBtn.setText("\uf20b");
        shareBtn.setVisibility(View.VISIBLE);
        shareBtn.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				menu.showMenu();
			}
        	
        });
		
		location = GeoLocator.getInstance(CentersPoints.this);
		
		database = new DatabaseWrapper( this );
		
    	atentionContainer = ( LinearLayout ) this.findViewById(R.id.atentionContainer);
    	
    	listView = (ListView) this.findViewById(R.id.listView);
    	
    	searchLayout = (LinearLayout) this.findViewById(R.id.searchLayout);
    	searchTxt = (EditText) this.findViewById(R.id.searchTxt);
    	
    	try {
    		cursor = database.getAllCenters(location.latitude, location.longitude, "");
    	
	    	adapter = new PointsAdapter(this, cursor, true);
	    	
	    	listView.setAdapter(adapter);
	    	
	    	// Set the FilterQueryProvider, to run queries for choices
	        // that match the specified input.
	        adapter.setFilterQueryProvider(new FilterQueryProvider() {
	            public Cursor runQuery(CharSequence constraint) {
	                // Search for states whose names begin with the specified letters.
	            	Commons.info("Constraint: " + constraint.toString() );
	                Cursor cursor;
					try {
						cursor = database.getAllCenters(location.latitude, location.longitude, constraint.toString());
						return cursor;
					} catch (Exception e) {
						return null;
					}
	            }
	        });
	    	
	    	titleTxt = ( TextView ) this.findViewById( R.id.titleTxt );
	    	
	    	searchTxt.addTextChangedListener(new TextWatcher(){
	
				@Override
				public void afterTextChanged(Editable s) {
					adapter.getFilter().filter(searchTxt.getText());
				}
	
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
	
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {}
	    		
	    	});
	    	
	    	listView.setOnItemClickListener( new OnItemClickListener() {
	
				@Override
				public void onItemClick(AdapterView<?> adap, View view, int position, long id) {
					Cursor c = database.getCenterById(id);
					c.moveToFirst();
					
					String address = c.getString( c.getColumnIndex("address") );
					String barrio = c.getString( c.getColumnIndex("barrio") );
					double lat = c.getDouble( c.getColumnIndex("lat") );
					double lng = c.getDouble( c.getColumnIndex("lng") );
					String tipo = c.getString( c.getColumnIndex("tipo") );
					
					Intent intent = new Intent( CentersPoints.this, com.speryans.redbus.MapDetails.class );
					intent.putExtra("address", address);
					intent.putExtra("barrio", barrio);
					intent.putExtra("lat", lat);
					intent.putExtra("lng", lng);
					intent.putExtra("tipo", tipo);
					
					startActivity( intent );
				}
	    		
	    	});
	    	
	    	((TextView)this.findViewById(R.id.icon_option_text_bus)).setTypeface(icons);
	        ((TextView)this.findViewById(R.id.icon_option_text_taxi)).setTypeface(icons);
	    	
	    	// Panel buttons
	        recargaButton = (LinearLayout) menu.findViewById(R.id.busButton);
	        recargaButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					menu.showContent();
					
					titleTxt.setText("Centros de Recarga");
					searchLayout.setVisibility(View.VISIBLE);
					listView.setVisibility(View.VISIBLE);
					atentionContainer.setVisibility(View.GONE);
				}
	        });
	        
	        atencionButton = (LinearLayout) menu.findViewById(R.id.taxiButton);
	        atencionButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					menu.showContent();
					
					titleTxt.setText("Centros de Atención al Cliente");
					searchLayout.setVisibility(View.GONE);
					listView.setVisibility(View.GONE);
					atentionContainer.setVisibility(View.VISIBLE);
				}
	        });
	    	
	        listView.setOnTouchListener(new ListView.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					int action = event.getAction();
	                switch (action) {
	                case MotionEvent.ACTION_DOWN:
	                    // Disallow ScrollView to intercept touch events.
	                    v.getParent().requestDisallowInterceptTouchEvent(true);
	                    break;
	
	                case MotionEvent.ACTION_UP:
	                    // Allow ScrollView to intercept touch events.
	                    v.getParent().requestDisallowInterceptTouchEvent(false);
	                    break;
	                }
	
	                // Handle ListView touch events.
	                v.onTouchEvent(event);
	                return true;
				}
	        });
	        
	    	//this.loadCenters();
	    	// Remove views from suggested.
			atentionContainer.removeAllViews();
	    	this.loadAttention();    	
			
			ProgressHUD.dismissHUD();
		
    	} catch( Exception ex ) {
    		Toast.makeText(this, R.string.no_db, Toast.LENGTH_LONG).show();
    		this.finish();
    	}
	}
	
	private void loadAttention() {
		(new AsyncTask<JSONObject, View, Boolean>() {

			@Override
			protected Boolean doInBackground(JSONObject... params) {
				try {
					Cursor cursor = database.getAtention(location.latitude, location.longitude, 100000);
					
					if( cursor.getCount() > 0 ) {
						if( cursor.moveToFirst() ) {
							
							do {
								View v = LayoutInflater.from(CentersPoints.this).inflate(R.layout.row, null, false);
								final String tipo = cursor.getString( cursor.getColumnIndex("tipo") );
								
								if( tipo.equalsIgnoreCase("cau") ) {
									((TextView) v.findViewById(R.id.label)).setText("Centro de Atención al Usuario RedBus");
								} else if( tipo.equalsIgnoreCase("ciu") ) {
									((TextView) v.findViewById(R.id.label)).setText("Centro de Abonos de Ciudad de Córdoba");
								} else if( tipo.equalsIgnoreCase("con") ) {
									((TextView) v.findViewById(R.id.label)).setText("Centro de Abonos de Coniferal");
								} else if( tipo.equalsIgnoreCase("tam") ) {
									((TextView) v.findViewById(R.id.label)).setText("Centro de Abonos T.A.M. S.E.");
								}
								
								((TextView) v.findViewById(R.id.sublabel)).setText( cursor.getString( cursor.getColumnIndex("address") ) + "\n" + cursor.getString( cursor.getColumnIndex("barrio") ));
								((LinearLayout) v.findViewById(R.id.rightLayout)).setVisibility(View.GONE);
								
								final String address = cursor.getString( cursor.getColumnIndex("address") );
								final String barrio = cursor.getString( cursor.getColumnIndex("barrio") );
								final double lat = cursor.getDouble( cursor.getColumnIndex("lat") );
								final double lng = cursor.getDouble( cursor.getColumnIndex("lng") );
								
								TextView icon = (TextView) v.findViewById(R.id.iconTxt);
								icon.setTypeface(icons);
								icon.setText("\uf1ff");
								
								v.setOnClickListener( new OnClickListener() {

									@Override
									public void onClick(View v) {
										Intent intent = new Intent( CentersPoints.this, com.speryans.redbus.MapDetails.class );
										intent.putExtra("address", address);
										intent.putExtra("barrio", barrio);
										intent.putExtra("lat", lat);
										intent.putExtra("lng", lng);
										intent.putExtra("tipo", tipo);
										
										startActivity( intent );
									}
									
								});
								
								this.publishProgress(v);
							} while( cursor.moveToNext() );
						}
					}
					
					return Boolean.TRUE;
				} catch( Exception e ) {
					Commons.error("Error: " + e.toString(), e);
					return null;
				}
			}

			@Override
			protected void onProgressUpdate(View... values) {
				if( values != null ) {
					atentionContainer.addView(values[0]);
				}
			}

			@Override
			protected void onPostExecute(Boolean result) {
				super.onPostExecute(result);
			}
			
		}).execute();
	}
	
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}


}
