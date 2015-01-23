package com.speryans.redbus;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener;
import org.osmdroid.views.overlay.OverlayItem;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.speryans.redbus.classes.Commons;
import com.speryans.redbus.classes.En5EstoyActivity;
import com.speryans.redbus.classes.GeoLocator;
import com.speryans.redbus.classes.connection.Connection;
import com.speryans.redbus.classes.connection.DoneListener;
import com.speryans.redbus.classes.connection.ErrorListener;

public class MapDetails extends En5EstoyActivity implements DoneListener, ErrorListener, OnItemGestureListener<OverlayItem> {
	
	private ScrollView scroller;
	
	private GeoLocator location;
	private TextView detailsTxt;
	private TextView titleTxt;
	private LinearLayout stepsContainer;
	
	private String address;
	private String barrio;
	private double pointLat;
	private double pointLng;
	private String tipo;
	private TextView detailsTimeTxt;
	private LinearLayout container;
	private Button navigationButton;
	
	private MapView map;
	private IMapController mapController;
	private DefaultResourceProxyImpl mResourceProxy;
	private Drawable markerIcon;
	
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);
		
		container = (LinearLayout) this.findViewById(R.id.container);
		scroller = (ScrollView) this.findViewById(R.id.scroller);
		
		fontEverywhere(container, roboto);
		this.buttonsFont(container, icons);
		
		Commons.createHeader(this);
		
		address = this.getIntent().getStringExtra("address");
		barrio = this.getIntent().getStringExtra("barrio");
		pointLat = this.getIntent().getDoubleExtra("lat", 0);
		pointLng = this.getIntent().getDoubleExtra("lng", 0);
		tipo = this.getIntent().getStringExtra("tipo");
		
		location = GeoLocator.getInstance(MapDetails.this);
		
		map = (MapView) this.findViewById(R.id.map);
		map.setMultiTouchControls(true);
		mapController = map.getController();
		
		map.setTileSource(TileSourceFactory.MAPQUESTOSM);
		
		mResourceProxy = new DefaultResourceProxyImpl(getApplicationContext());
		markerIcon = getResources().getDrawable(R.drawable.point);
        
        map.setOnTouchListener(new ListView.OnTouchListener() {
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
                return false;
			}
        });
		
		titleTxt = ( TextView ) this.findViewById( R.id.titleTxt );
		detailsTxt = ( TextView ) this.findViewById( R.id.detailsTxt );
		detailsTimeTxt = ( TextView ) this.findViewById( R.id.detailsTimeTxt );
		detailsTxt.setText("Dirección: " + address + "\nBarrio: " + barrio + "\n");
		
		((TextView) this.findViewById(R.id.iconDetailsTxt)).setTypeface(icons);
		((TextView) this.findViewById(R.id.iconDetailsTimeTxt)).setTypeface(icons);
		
		((TextView) this.findViewById(R.id.iconHeader)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		if( tipo.equalsIgnoreCase("pdv") ) {
			titleTxt.setText("Punto de Recarga y Venta");
		} else if( tipo.equalsIgnoreCase("cau") ) {
			titleTxt.setText("Centro de Atención al Usuario RedBus");
			detailsTxt.setText( detailsTxt.getText() + "Teléfono: 4276750\n" );
		} else if( tipo.equalsIgnoreCase("ciu") ) {
			titleTxt.setText("Centro de Abonos de Ciudad de Córdoba");
			detailsTxt.setText( detailsTxt.getText() + "Teléfono: 4211149\n" );
		} else if( tipo.equalsIgnoreCase("con") ) {
			titleTxt.setText("Centro de Abonos de Coniferal");
			detailsTxt.setText( detailsTxt.getText() + "Teléfono: 4260093\n" );
		} else if( tipo.equalsIgnoreCase("tam") ) {
			titleTxt.setText("Centro de Abonos T.A.M. S.E.");
			detailsTxt.setText( detailsTxt.getText() + "Teléfono: 4342373\n" );
		}
		
		navigationButton = (Button) this.findViewById(R.id.navigationButton);
		navigationButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/?daddr=" + pointLat + "," + pointLng)); 
				startActivity(myIntent);
			}
			
		});
		
    	stepsContainer = ( LinearLayout ) this.findViewById(R.id.stepsContainer);
    	
    	this.addPoint(location.latitude, location.longitude);
    	this.addPoint(pointLat, pointLng);
    	
    	Connection
    		.get(this, "http://maps.googleapis.com/maps/api/directions/json?origin=" + location.latitude + "," + location.longitude + "&destination=" + pointLat + "," + pointLng + "&sensor=false&mode=walking&language=es")
    		.done(this)
    		.error(this)
    		.start();
	}
	
	private void addPoint( double lat, double lng ) {
		OverlayItem marker = new OverlayItem(titleTxt.getText().toString(), "", new GeoPoint(lat, lng));
		marker.setMarker(markerIcon);
		
		ArrayList<OverlayItem> mItems = new ArrayList<OverlayItem>();
		mItems.add(marker);
		
		ItemizedIconOverlay<OverlayItem> items = new ItemizedIconOverlay<OverlayItem>(mItems, markerIcon, this, mResourceProxy);
		map.getOverlays().add(items);
		
		mapController.setCenter(new GeoPoint( lat, lng ));
		mapController.setZoom(15);
    }
	
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void ready(String url, String message) {
		try {
			JSONObject data = new JSONObject(message);
			
			if( data.getString("status").equalsIgnoreCase("OK") ) {
				JSONObject route = data.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0);
				
				detailsTimeTxt.setText( "Estas a " + route.getJSONObject("distance").getString("text") + " y deberias llegar en " + route.getJSONObject("duration").getString("text") );
				
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
	}

	@Override
	public void onError(String url, int msg) {
		Toast.makeText(this, R.string.connection_error, Toast.LENGTH_LONG).show();
	}

	@Override
	public boolean onItemLongPress(int arg0, OverlayItem arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onItemSingleTapUp(int arg0, OverlayItem arg1) {
		// TODO Auto-generated method stub
		return false;
	}


}
