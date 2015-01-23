package com.speryans.redbus.sections;

import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;

import com.WazaBe.HoloEverywhere.HoloAlertDialogBuilder;
import com.slidinglayer.SlidingLayer;
import com.speryans.redbus.R;
import com.speryans.redbus.classes.Commons;
import com.speryans.redbus.classes.SectionActivity;
import com.speryans.redbus.classes.Typefaces;
import com.speryans.redbus.classes.Urls;
import com.speryans.redbus.classes.connection.Connection;
import com.speryans.redbus.classes.connection.DoneListener;
import com.speryans.redbus.classes.helpers.Weather;
import com.speryans.redbus.stadistics.AppStats;

public class Footer extends SectionActivity {
	
	public TextSwitcher newsTicker;
	private TextView weatherTemp;
	public TextView weatherIcon;
	public SlidingLayer bottomLayout;
	private LinearLayout newsList;

	public void onCreated() {
		bottomLayout = (SlidingLayer) parent.findViewById(R.id.bottomLayout);
		
		weatherIcon = (TextView) parent.findViewById(R.id.weatherIcon);
		weatherIcon.setTypeface(Typefaces.get(parent, "fonts/meteocons.ttf"));
		
		newsList = (LinearLayout) parent.findViewById(R.id.news_list);
		
		newsTicker = (TextSwitcher) parent.findViewById(R.id.newsTicker);
		newsTicker.setInAnimation(parent, android.R.anim.fade_in);
		newsTicker.setOutAnimation(parent, android.R.anim.fade_out);
		
		newsTicker.setText("Cargando noticias...");
		
		weatherTemp = (TextView) parent.findViewById(R.id.weatherTemp);
		
		loadWeather();
		loadNews();
		
		Timer newsUpdater = new Timer();
		newsUpdater.scheduleAtFixedRate(newsUpdate, 2000, 10000);
	}
	
	private void loadWeather() {
		Connection.post(parent, Urls.WEATHER)
			.param("udid", Commons.getDeviceID(parent))
			.done(new DoneListener() {

				@Override
				public void ready(String url, String data) {
					try {
						JSONObject condition = (new JSONObject(data)).getJSONObject("data");
						
						String icon = Weather.parseWeatherState( condition.getString("text") );
						if( icon == null ) icon = ")";
						
						weatherTemp.setText(condition.getInt("temp") + "º" );
						weatherIcon.setText( icon );
					} catch( Exception ex ) {
						Commons.error("Error weather " + ex.toString(), ex);
					}
				}
				
			})
			.error(parent)
			.start();
	}
	
	private JSONArray rssItems;
	private int newsIndex = 0;
	
	private TimerTask newsUpdate = new TimerTask() {

		@Override
		public void run() {
			parent.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if( rssItems == null ) return;
					
					try {
						JSONObject item = rssItems.getJSONObject(newsIndex);
						final String title = item.getString("title");
						final String url = item.getString("url");
						
						newsTicker.setText( title );
						newsTicker.setOnClickListener( new OnClickListener() {

							@Override
							public void onClick(View v) {
								Footer.this.showBrowser(title, url);
							}
							
						});
						((TextView)newsTicker.getCurrentView()).setTypeface(parent.roboto);
					
						newsIndex++;
						if( newsIndex > rssItems.length() ) {
							loadNews();
						}
					} catch(Exception e ) {}
				}
				
			});
		}
		
	};
	
	public void showBrowser(String title, final String url) {
		AlertDialog.Builder builder = new HoloAlertDialogBuilder(parent);
		builder.setInverseBackgroundForced(true);
		builder.setCancelable(true);
		builder.setTitle(R.string.application_name);
		TextView view = (TextView)LayoutInflater.from(parent).inflate(R.layout.workarounddialogs, null, false);
		view.setText("La siguiente noticia será abierta en su navegador:\n\n" + title);
		builder.setView( view );
		builder.setPositiveButton("Continuar", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				try {
					AppStats.addNews(parent, url);
				
					Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse( url ));
					parent.startActivity(browser);
				} catch( Exception e ) {
					Toast.makeText(parent, R.string.news_error, Toast.LENGTH_LONG).show();
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
	
	private void loadNews() {
		Commons.info("Loading news...");
		Connection
		.post(parent, Urls.NEWS)
		.param("udid", Commons.getDeviceID(parent))
		.done(new DoneListener() {
			@Override public void ready(String url, String data) {
				// Load data into rssItems
				Commons.info("Received: " + data);
				try {
					newsIndex = 0;
					rssItems = (new JSONObject(data)).getJSONArray("data");
					
					newsList.removeAllViews();
					// Load items on news list
					for( int i = 0 ; i < rssItems.length() ; i ++ ) {
						JSONObject item = rssItems.getJSONObject(i);
						
						TextView v = (TextView) LayoutInflater.from( parent ).inflate(R.layout.new_row, null, false);
						
						final String i_tl = item.getString("title");
						final String i_url = item.getString("url");
						
						v.setText( i_tl );
						v.setTypeface(parent.roboto);
						
						v.setOnClickListener( new OnClickListener() {

							@Override
							public void onClick(View v) {
								Footer.this.showBrowser(i_tl, i_url);
							}
							
						});
						
						newsList.addView(v);
					}
				} catch (JSONException e) {
					Toast.makeText(parent, "Se produjo un error al cargar las noticias.", Toast.LENGTH_SHORT).show();
				}
			}
		})
		.error(parent)
		.start();
	}
}
