package com.speryans.redbus.services;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;

import com.speryans.redbus.R;
import com.speryans.redbus.classes.GeoLocator;
import com.speryans.redbus.classes.KeySaver;

public class AlarmTracker extends Service {

	private int notification_id = 1001;
	
	int dot = 200;      // Length of a Morse Code "dot" in milliseconds
	int dash = 500;     // Length of a Morse Code "dash" in milliseconds
	int short_gap = 200;    // Length of Gap Between dots/dashes
	int medium_gap = 500;   // Length of Gap Between Letters
	int long_gap = 1000;    // Length of Gap Between Words
	long[] pattern = {
	    0,  // Start immediately
	    dot, short_gap, dot, short_gap, dot,    // s
	    medium_gap,
	    dash, short_gap, dash, short_gap, dash, // o
	    medium_gap,
	    dot, short_gap, dot, short_gap, dot,    // s
	    long_gap,
	    dot, short_gap, dot, short_gap, dot,    // s
	    medium_gap,
	    dash, short_gap, dash, short_gap, dash, // o
	    medium_gap,
	    dot, short_gap, dot, short_gap, dot,    // s
	    long_gap,
	    dot, short_gap, dot, short_gap, dot,    // s
	    medium_gap,
	    dash, short_gap, dash, short_gap, dash, // o
	    medium_gap,
	    dot, short_gap, dot, short_gap, dot,    // s
	    long_gap,
	    dot, short_gap, dot, short_gap, dot,    // s
	    medium_gap,
	    dash, short_gap, dash, short_gap, dash, // o
	    medium_gap,
	    dot, short_gap, dot, short_gap, dot,    // s
	    long_gap,
	    dot, short_gap, dot, short_gap, dot,    // s
	    medium_gap,
	    dash, short_gap, dash, short_gap, dash, // o
	    medium_gap,
	    dot, short_gap, dot, short_gap, dot,    // s
	    long_gap,
	    dot, short_gap, dot, short_gap, dot,    // s
	    medium_gap,
	    dash, short_gap, dash, short_gap, dash, // o
	    medium_gap,
	    dot, short_gap, dot, short_gap, dot,    // s
	    long_gap,
	    dot, short_gap, dot, short_gap, dot,    // s
	    medium_gap,
	    dash, short_gap, dash, short_gap, dash, // o
	    medium_gap,
	    dot, short_gap, dot, short_gap, dot,    // s
	    long_gap,
	    dot, short_gap, dot, short_gap, dot,    // s
	    medium_gap,
	    dash, short_gap, dash, short_gap, dash, // o
	    medium_gap,
	    dot, short_gap, dot, short_gap, dot,    // s
	    long_gap,
	    dot, short_gap, dot, short_gap, dot,    // s
	    medium_gap,
	    dash, short_gap, dash, short_gap, dash, // o
	    medium_gap,
	    dot, short_gap, dot, short_gap, dot,    // s
	    long_gap
	};
	
	private Timer timer;
	
	private int waitTime = 3;

	private NotificationManager mNotificationManager;

	private Builder mNotifyBuilder;

	private GeoLocator geoLocation;

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		
		geoLocation = GeoLocator.getInstance(this);
		
		this.createTimer();

		return START_STICKY;
	}
	
	private void notification( String text, boolean notify ) {
		mNotifyBuilder = new NotificationCompat.Builder(this)
		    .setContentTitle("En 5' Estoy Alarma!")
		    .setContentText( text )
		    .setAutoCancel(false)
		    .setDefaults(Notification.DEFAULT_ALL)
		    .setVibrate(pattern)
		    .setPriority(NotificationCompat.PRIORITY_HIGH)
		    .setSmallIcon(R.drawable.ic_stat_notif);
		   
		if( notify ) {
			mNotifyBuilder.setTicker(text);
		}
		
		mNotificationManager.notify(this.notification_id, mNotifyBuilder.build());
	}
	
	private void createTimer() {
		timer = new Timer();
		timer.schedule(new WaitTask(), waitTime * 1000); 
	}

	class WaitTask extends TimerTask {

		@Override
		public void run() {
			try {
				// Si se vacia la alarma cerrar el proceso
				if( KeySaver.getSaved(AlarmTracker.this, KeySaver.ALARM_LAT) == "" &&
						KeySaver.getSaved(AlarmTracker.this, KeySaver.ALARM_LON) == "" ) {
					AlarmTracker.this.stopSelf();
				}
				
				Location current = new Location("manager");
				current.setLatitude( geoLocation.latitude );
				current.setLongitude( geoLocation.longitude );
				
				Location destiny = new Location("destination");
				destiny.setLatitude( Double.parseDouble( KeySaver.getSaved(AlarmTracker.this, KeySaver.ALARM_LAT) ) );
				destiny.setLongitude( Double.parseDouble( KeySaver.getSaved(AlarmTracker.this, KeySaver.ALARM_LON) ) );
				
				float distance = current.distanceTo( destiny );
				if( distance < 400 ) {
					notification("Estas a " + distance + " mts de tu destino.", true);
					
					// Restore keys
					KeySaver.save(AlarmTracker.this, KeySaver.ALARM, "");
					KeySaver.save(AlarmTracker.this, KeySaver.ALARM_LAT, "");
					KeySaver.save(AlarmTracker.this, KeySaver.ALARM_LON, "");
					
					// Cancel service
					AlarmTracker.this.stopSelf();
				} else {
					createTimer();
				}
			} catch( Exception ex ) {
				createTimer();
			}
		}
		
	}
}
