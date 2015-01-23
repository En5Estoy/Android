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

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.speryans.redbus.classes.Commons;
import com.speryans.redbus.classes.En5EstoyActivity;
import com.speryans.redbus.classes.GeoLocator;
import com.speryans.redbus.classes.KeySaver;
import com.speryans.redbus.classes.ProgressHUD;
import com.speryans.redbus.classes.Urls;
import com.speryans.redbus.classes.connection.Connection;
import com.speryans.redbus.classes.connection.DoneListener;
import com.speryans.redbus.stadistics.AppStats;

public class Lines extends En5EstoyActivity implements OnItemGestureListener<OverlayItem>, OnClickListener {
	private LinearLayout goCardsContainer;

	private boolean isMap = false;

	private ScrollView scroller;
	private LinearLayout closeBtn;
	private ImageView mapShadow;
	private TextView goBtn;
	private TextView returnBtn;
	private JSONArray goingPoints;
	private JSONArray returnPoints;
	private LinearLayout header;
	
	//private GoogleMap googleMap;

	private String line;

	private MapView map;

	private ArrayList<OverlayItem> goItemsArray = null;
	private ArrayList<OverlayItem> returnItemsArray = null;
	
	private IMapController mapController;

	private DefaultResourceProxyImpl mResourceProxy;

	private Drawable markerIcon;

	private TextView lineTitleTxt;

	private LinearLayout openMap;

	private boolean reloadPoints;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lines);

		// AppStats.addSectionUse(Lines.this, AppStats.BUSES_FOUND);

		scroller = (ScrollView) this.findViewById(R.id.scroller);

		((TextView) this.findViewById(R.id.iconHeader)).setTypeface(icons);
		((TextView) this.findViewById(R.id.iconHeader))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						finish();
					}
				});

		
		lineTitleTxt = (TextView) this.findViewById(R.id.lineTitleTxt);
		goCardsContainer = (LinearLayout) this.findViewById(R.id.goCardsContainer);
		
		mapShadow = (ImageView) this.findViewById(R.id.mapShadow);
		
		map = (MapView) this.findViewById(R.id.map);
		map.setMultiTouchControls(true);
		mapController = map.getController();
		
		map.setTileSource(TileSourceFactory.MAPQUESTOSM);
		
		openMap = (LinearLayout) this.findViewById(R.id.openMap);
		openMap.setOnClickListener(this);
        
		GeoLocator location = GeoLocator.getInstance(this);
		
		mapController.setZoom(13);
		mapController.animateTo(new GeoPoint( location.latitude, location.longitude ));
		
		mResourceProxy = new DefaultResourceProxyImpl(getApplicationContext());
		markerIcon = Lines.this.getResources().getDrawable(R.drawable.point);

		closeBtn = (LinearLayout) this.findViewById(R.id.closeBtn);
		closeBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isMap) {
					hideMap();
				}
			}

		});

		
		goBtn = (TextView) this.findViewById(R.id.goBtn);
		goBtn.setTypeface(icons);
		goBtn.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				lineTitleTxt.setText( "Linea " + line + "\nIda" );
				loadPoints(goingPoints);
			}
			
		});
		returnBtn = (TextView) this.findViewById(R.id.returnBtn);
		returnBtn.setTypeface(icons);
		returnBtn.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				lineTitleTxt.setText( "Linea " + line + "\nRetorno" );
				loadPoints(returnPoints);
			}
			
		});

		ProgressHUD.show(this, "Cargando línea...", true, null);
		
		line = this.getIntent().getStringExtra("line");
		
		lineTitleTxt.setTypeface(roboto);
		lineTitleTxt.setText( "Linea " + line + "\nIda" );
		
		header = (LinearLayout) this.findViewById(R.id.header);
		header.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!isMap) {
					scroller.startAnimation(AnimationUtils.loadAnimation(Lines.this, R.anim.to_bottom) );
					scroller.setVisibility(View.GONE);
					mapShadow.setVisibility(View.GONE);
					
					header.startAnimation(AnimationUtils.loadAnimation(Lines.this, android.R.anim.fade_out) );
					header.setVisibility(View.GONE);

					isMap = true;
				}
			}

		});

		// Crear los dos layers de puntos de una y solo cambiar uno por otro en el mapa en vez de crearlos de nuevo.
		Connection.post(this, Urls.STOPS)
			.param("line", this.getIntent().getStringExtra("_id"))
			.done(new DoneListener() {

				@Override
				public void ready(String url, String message) {
					try {
						JSONObject data = new JSONObject(message);
						
						ProgressHUD.dismissHUD();

						goingPoints = data.getJSONArray("going");
						returnPoints = data.getJSONArray("return");

						if( goingPoints.length() > 0 && returnPoints.length() > 0 ) {
							goBtn.setVisibility(View.VISIBLE);
							returnBtn.setVisibility(View.VISIBLE);
						}
						
						if( goingPoints.length() != 0 ){
							loadPoints(goingPoints);
							//loadPointsOnMap(goingPoints);
						} else {
							loadPoints(returnPoints);
							//loadPointsOnMap(returnPoints);
						}
					} catch (Exception e) {
						Commons.error(e.toString(), e);
					}
				}
				
			}).start();
	}

	protected void hideMap() {
		scroller.setVisibility(View.VISIBLE);
		scroller.startAnimation(AnimationUtils.loadAnimation(Lines.this, R.anim.from_bottom) );
		mapShadow.setVisibility(View.VISIBLE);
		mapShadow.startAnimation(AnimationUtils.loadAnimation(Lines.this, android.R.anim.fade_in) );
		header.setVisibility(View.VISIBLE);
		header.startAnimation(AnimationUtils.loadAnimation(Lines.this, android.R.anim.fade_in) );

		openMap.setVisibility(View.VISIBLE);
		
		isMap = false;
	}

	private void addPoint(ArrayList<OverlayItem> mItems, double lat, double lng, String title, String description) {
		OverlayItem marker = new OverlayItem(title, description, new GeoPoint(lat, lng));
		marker.setMarker(markerIcon);
		mItems.add(marker);
	}

	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	private void loadPoints( final JSONArray points) {
		ProgressHUD.show(Lines.this, "Cargando paradas...", true, null);
		goCardsContainer.removeAllViews();
		
		reloadPoints = false;
		if( points == goingPoints ) {
			if( goItemsArray == null ) {
				goItemsArray =new ArrayList<OverlayItem>();
				
				reloadPoints = true;
			}
		} else {
			if( returnItemsArray == null ) {
				returnItemsArray =new ArrayList<OverlayItem>();
				
				reloadPoints = true;
			}
		}
		
		(new AsyncTask<Object, View, Boolean>() {

			@Override
			protected Boolean doInBackground(Object... params) {
				try {
					for (int i = 0; i < points.length(); i++) {
						final JSONObject stop = points.getJSONObject(i);

						View v = LayoutInflater.from(Lines.this).inflate(R.layout.line_stop_row, null, false);

						String title = stop.getJSONObject("street").getString("name");

						((TextView) v.findViewById(R.id.cardTitleTxt)).setText(title);
						((TextView) v.findViewById(R.id.cardTitleTxt)).setTypeface(roboto);

						final JSONArray fromStop = stop.getJSONArray("location");

						final String endAddress = fromStop.getString(0) + "," + fromStop.getString(1);

						// Ir hasta la parada por ahora
						Button navigationButton = (Button) v.findViewById(R.id.navigationButton);
						navigationButton.setTypeface(icons);
						navigationButton.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/?daddr=" + endAddress));
								startActivity(myIntent);
							}
						});

						Button alarmBtn = (Button) v.findViewById(R.id.favoriteBtn);
						alarmBtn.setTypeface(icons);
						alarmBtn.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								KeySaver.save( Lines.this, KeySaver.ALARM, stop.optString("road") + " " + ((stop.optInt("number") == 0) ? "S/N" : stop.optString("number")));
								KeySaver.save(Lines.this, KeySaver.ALARM_LAT, fromStop.optString(0));
								KeySaver.save(Lines.this, KeySaver.ALARM_LON, fromStop.optString(1));

								AppStats.addSectionUse(Lines.this, AppStats.ALARM);

								Intent service = new Intent(Lines.this, com.speryans.redbus.services.AlarmTracker.class);
								Lines.this.startService(service);
								
								Toast.makeText(Lines.this, R.string.alarm_created, Toast.LENGTH_LONG).show();
							}

						});
						
						if( points == goingPoints && reloadPoints ) {
							addPoint(goItemsArray, fromStop.optDouble(1), fromStop.optDouble(0), title, "");
						} else if( reloadPoints ) {
							addPoint(returnItemsArray, fromStop.optDouble(1), fromStop.optDouble(0), title, "");
						}

						this.publishProgress(v);
					}
				} catch (Exception e) {
					Commons.error(e.toString(), e);
				}
				
				return Boolean.TRUE;
			}

			@Override
			protected void onProgressUpdate(View... values) {
				if( values != null ) {
					goCardsContainer.addView(values[0]);
				}
			}

			@Override
			protected void onPostExecute(Boolean result) {
				super.onPostExecute(result);
				
				map.getOverlays().clear();
				ItemizedIconOverlay<OverlayItem> items = null;
				if( points == goingPoints ) {
					items = new ItemizedIconOverlay<OverlayItem>(goItemsArray, markerIcon, Lines.this, mResourceProxy);
				} else {
					items = new ItemizedIconOverlay<OverlayItem>(returnItemsArray, markerIcon, Lines.this, mResourceProxy);
				}
				
				map.getOverlays().add(items);
				
				map.invalidate();
				
				ProgressHUD.dismissHUD();
			}
			
		}).execute();
	}
	
	@Override
	public void onBackPressed() {
		if (isMap) {
			hideMap();
		} else {
			super.onBackPressed();
		}
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

	@Override
	public void onClick(View v) {
		if( v == openMap ) {
			if (!isMap) {
				scroller.startAnimation(AnimationUtils.loadAnimation(Lines.this, R.anim.to_bottom) );
				scroller.setVisibility(View.GONE);
				mapShadow.setVisibility(View.GONE);
				
				openMap.setVisibility(View.GONE);
				
				header.startAnimation(AnimationUtils.loadAnimation(Lines.this, android.R.anim.fade_out) );
				header.setVisibility(View.GONE);

				//mapView.setOnClickListener(null);
				
				isMap = true;
			}
		}
	}

}
