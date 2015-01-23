package com.speryans.redbus;

import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.app.TaskStackBuilder;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;
import com.speryans.redbus.classes.Commons;
import com.speryans.redbus.classes.KeySaver;
import com.speryans.redbus.classes.Urls;
import com.speryans.redbus.classes.connection.Connection;
import com.speryans.redbus.classes.connection.DoneListener;
import com.speryans.redbus.classes.helpers.CityManager;
import com.speryans.redbus.stadistics.AppStats;

/**
 * IntentService responsible for handling GCM messages.
 */
public class GCMIntentService extends GCMBaseIntentService {
	public static final String REG_ID = "682852859475";
	
	private static Builder mNotifyBuilder;
	private static NotificationManager mNotificationManager;

	public GCMIntentService() {
		super(GCMIntentService.REG_ID);
	}
	
    @Override
    protected void onRegistered(Context context, String registrationId) {
    	Commons.info("Registered on Server with RID: " + registrationId);
    	
    	GCMIntentService.registerOnServer(context, registrationId);
    	GCMRegistrar.setRegisteredOnServer(context, true);
    }
    
    public static void registerOnServer(final Context context, String rid) {
    	try {
    		Connection
			.post(context, Urls.REGISTER_USER)
			.param("os", "android")
			.param("udid", Commons.getDeviceID(context))
			.param("pid", rid)
			.param("city", CityManager.getCity(context))
			.done(new DoneListener() {

				@Override
				public void ready(String url, String data) {
					try {
						JSONObject obj = new JSONObject(data);
						if( obj.getBoolean("result") ) {
							if( obj.getJSONObject("city") != null ) {
								KeySaver.save(context, KeySaver.CITY_DATA, obj.getJSONObject("city").toString());
							}
							//AppStats.addSectionUse(context, AppStats.PUSH_DEVICE);
						}
					} catch( Exception ex ) {}
				}
				
			}).start();
    	} catch( Exception e ) {
        	Commons.error(e.toString(), e);
        }
    }

    @Override
    protected void onUnregistered(Context context, String registrationId) {
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
    	Commons.printExtras(intent);
        // notifies user
        generateNotification(
        		context, 
        		intent.getStringExtra("title"), 
        		intent.getStringExtra("message"),
        		intent.getStringExtra("url")
        	);
    }

    @Override
    protected void onDeletedMessages(Context context, int total) {
    }

    @Override
    public void onError(Context context, String errorId) {
        
    }

    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        // log message
        return super.onRecoverableError(context, errorId);
    }

    /**
     * Issues a notification to inform the user that server has sent a message.
     */
    private static void generateNotification(Context context, String title, String message, String url) {
    	mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    	
    	mNotifyBuilder = new NotificationCompat.Builder(context)
	    .setContentTitle(title)
	    .setContentText( message )
	    .setAutoCancel(true)
	    .setDefaults(Notification.DEFAULT_ALL)
	    .setPriority(NotificationCompat.PRIORITY_HIGH)
	    .setSmallIcon(R.drawable.ic_stat_notif)
	    .setTicker(title);
		
		// Just for newer versions of Android
    	NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle();
        style.bigText(message);
        
        mNotifyBuilder.setStyle(style);
        // --
	
        Intent resultIntent = null;
        // Create url launch
        if( url == null || url.equalsIgnoreCase("") ) {
        	try {
        		resultIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        	} catch( Exception e ) {
        		resultIntent = new Intent( context, com.speryans.redbus.Home.class );
        	}
        } else {
        	resultIntent = new Intent( context, com.speryans.redbus.Home.class );
        }
        
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent( 0, PendingIntent.FLAG_UPDATE_CURRENT );
        
        mNotifyBuilder.setContentIntent(resultPendingIntent);
        // --
        
		mNotificationManager.notify((int)System.currentTimeMillis(), mNotifyBuilder.build());
    }

}
