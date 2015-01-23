package com.speryans.redbus.sections;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.speryans.redbus.Home;
import com.speryans.redbus.R;
import com.speryans.redbus.classes.Commons;
import com.speryans.redbus.classes.ProgressHUD;
import com.speryans.redbus.classes.SectionActivity;
import com.speryans.redbus.classes.Urls;
import com.speryans.redbus.classes.connection.Connection;
import com.speryans.redbus.classes.connection.DoneListener;
import com.speryans.redbus.stadistics.AppStats;

public class Notifications extends SectionActivity {
	
	private LinearLayout newsContainerLayout;

	public void onCreated() {
		newsContainerLayout = (LinearLayout) parent.findViewById(R.id.newsContainerLayout);
	}
	
	public void loadE5eNews() {
		ProgressHUD.show(parent, "Cargando noticias...", true, null);
		
		Connection.post(parent, Urls.NOTIFICATIONS)
			.param("udid", Commons.getDeviceID(parent))
			.done(new DoneListener() {

				@Override
				public void ready(String purl, String message) {
					try {
						JSONArray data = (new JSONObject( message )).getJSONArray("data");
						
						newsContainerLayout.removeAllViews();
						
						for( int i = (data.length() - 1) ; i >= 0 ; i-- ) {
							JSONObject obj = data.getJSONObject(i);
							View v = LayoutInflater.from(parent).inflate(R.layout.news_row, null, false);
							
							((TextView) v.findViewById(R.id.cardTitleTxt ) ).setText( obj.getString("title") );
							((TextView) v.findViewById(R.id.cardTitleTxt ) ).setTypeface(parent.roboto);
							((TextView) v.findViewById(R.id.cardText ) ).setText( obj.getString("text") );
							((TextView) v.findViewById(R.id.cardText ) ).setTypeface(parent.roboto);
							
							final String url = obj.getString("url");
							
							String date_string = obj.getString("created");
							String date[] = date_string.split("T");
							String parted_date[] = date[0].split("-");
							
							((TextView) v.findViewById(R.id.dayTxt ) ).setText( parted_date[2] );
							((TextView) v.findViewById(R.id.dayTxt ) ).setTypeface(parent.roboto);
							((TextView) v.findViewById(R.id.monthTxt ) ).setText( parted_date[1] );
							((TextView) v.findViewById(R.id.dayTxt ) ).setTypeface(parent.roboto);
							
							if( url.equalsIgnoreCase("") ) {
								((Button) v.findViewById(R.id.openButton)).setVisibility(View.GONE);
							} else {
							((Button) v.findViewById(R.id.openButton)).setOnClickListener( new OnClickListener() {

								@Override
								public void onClick(View arg0) {
									AppStats.addNews(parent, url);
									
									Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse( url ));
									parent.startActivity(browser);
								}
								
							});
							}
							
							newsContainerLayout.addView(v);
						}
					} catch( Exception ex ) {
						Commons.error("Error notifications " + ex.toString(), ex);
					}
					
					ProgressHUD.dismissHUD();
				}
				
			})
			.error(parent)
			.start();
	}
}
