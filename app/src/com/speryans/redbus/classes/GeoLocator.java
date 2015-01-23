package com.speryans.redbus.classes;


import com.speryans.redbus.R;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

public class GeoLocator {
	private static GeoLocator instance = null;
	
	public double init_latitude = -31.420126;
	public double init_longitude = -64.188840;
	
	public double latitude = init_latitude;
	public double longitude = init_longitude;
	
	public PositionListener listener;
	
	private static Context context;
	
	public GeoLocator( Context a ) {
		context = a;
		
	    LocationManager mlocManager = (LocationManager) context.getSystemService( Activity.LOCATION_SERVICE );
	    
	    LocationListener mlocListener = new MyLocationListener();
	    // Some cheap devices not support network provider.
	    try {
		    mlocManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 5000, 0, mlocListener);
		} catch( Exception ex ) {}
	    try {
	    	mlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, mlocListener);
	    } catch( Exception ex ) {}
	    try {
	    	mlocManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 5000, 0, mlocListener);
	    } catch( Exception ex ) {}
	}
	
	public void setListener( PositionListener listener ) {
		this.listener = listener;
	}
	
	public static GeoLocator getInstance( Context a ) {
		if( instance == null ) {
			instance = new GeoLocator( a );
		}
		return instance;
	}
	
	/* Class My Location Listener */
    public class MyLocationListener implements LocationListener {

    	@Override
    	public void onLocationChanged(Location loc) {
    		latitude = loc.getLatitude();
    		longitude = loc.getLongitude();
    		
    		//Commons.info("X: " + latitude + " <> Y: " + longitude);
    		
    		if( listener != null ) {
    			listener.positionUpdated(latitude, longitude);
    		}
    	}

    	@Override
    	public void onStatusChanged(String provider, int status, Bundle extras) {
    		Commons.info("Provider: " + provider);
    	}

    	@Override
    	public void onProviderDisabled(String provider) {
    		//Toast.makeText(context, String.format(context.getString(R.string.geolocation_precision), provider.toUpperCase()), Toast.LENGTH_LONG).show();
    		Commons.info("Provider disabled: " + provider);
    	}

    	@Override
    	public void onProviderEnabled(String provider) {Commons.info("Provider enabled: " + provider);}
    }
    
    public interface PositionListener {
    	public void positionUpdated(double latitude, double longitude);
    }
}
