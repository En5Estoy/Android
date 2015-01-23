package com.speryans.redbus.classes.helpers;


public interface GeocodeListener {
	public void geocode_ready( String address, double lat, double lon );
	public void geocode_error();
}
