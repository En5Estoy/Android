package com.speryans.redbus.classes.helpers;

import org.json.JSONArray;
import org.json.JSONObject;

import com.speryans.redbus.R;
import com.speryans.redbus.classes.StringUtilities;
import com.speryans.redbus.classes.Typefaces;
import com.speryans.redbus.classes.URLParamEncoder;
import com.speryans.redbus.classes.Urls;
import com.speryans.redbus.classes.connection.Connection;
import com.speryans.redbus.classes.connection.DoneListener;
import com.speryans.redbus.classes.connection.ErrorListener;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GeocodeHelper {
	private Context context;
	private GeocodeListener listener = null;

	public GeocodeHelper(Context c) {
		this.context = c;
	}

	public void resolve(String search, GeocodeListener listener) {
		this.listener = listener;
		search = StringUtilities.removeAccents(search.trim());

		if (!search.contains(",")) {
			search += "," + CityManager.getSearch(context);
		}

		String url = (Urls.GEOCODE + URLParamEncoder.encode(search.trim())).replace(" ", "%20");

		Connection.get(context, url).done(new DoneListener() {

			@Override
			public void ready(String url, String message) {
				try {
					JSONObject result = new JSONObject(message);
					JSONArray results = result.getJSONArray("results");

					if (results.length() > 0) {

						if (results.length() == 1) {
							JSONObject address = results.getJSONObject(0);

							JSONObject location = address.getJSONObject(
									"geometry").getJSONObject("location");

							GeocodeHelper.this.listener.geocode_ready(address.getString("formatted_address"), location.getDouble("lat"), location.getDouble("lng"));
						} else {
							GeocodeHelper.this.placesDialog(results);
						}
					}
				} catch (Exception e) {
					GeocodeHelper.this.listener.geocode_error();
				}
			}

		}).error(new ErrorListener() {

			@Override
			public void onError(String url, int msg) {
				GeocodeHelper.this.listener.geocode_error();
			}
			
		}).start();
	}
	
	private void placesDialog( JSONArray results ) {
		final Dialog dialog = new Dialog(context,R.style.ProgressHUD);
		dialog.setTitle("");
		dialog.setContentView(R.layout.places_hud);
		dialog.setCancelable(true);
		dialog.setOnCancelListener(null);
		dialog.getWindow().getAttributes().gravity=Gravity.CENTER;
		WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();  
		lp.dimAmount=0.2f;
		dialog.getWindow().setAttributes(lp); 
		
		LinearLayout resultsContainer = (LinearLayout) dialog.findViewById(R.id.resultsContainer);
		resultsContainer.removeAllViews();
		
		for( int i = 0 ; i < results.length() ; i++ ) {
			JSONObject rst = results.optJSONObject(i);
			
			View v = LayoutInflater.from(context).inflate(R.layout.row, null, false);
			
			JSONObject location = rst.optJSONObject("geometry").optJSONObject("location");
			final double lat = location.optDouble("lat");
			final double lon = location.optDouble("lng");
			
			final String address = rst.optString("formatted_address").replace("Province", "Provincia");
			
			((TextView) v.findViewById(R.id.label)).setText( address );
			((TextView) v.findViewById(R.id.sublabel)).setText( "(" + lat + ", " + lon + ")"  );
			((LinearLayout) v.findViewById(R.id.rightLayout)).setVisibility(View.GONE);
			
			TextView icon = (TextView) v.findViewById(R.id.iconTxt);
			icon.setTypeface(Typefaces.get(context, "fonts/ionicons.ttf"));
			icon.setText("\uf273");
			
			v.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					listener.geocode_ready(address, lat, lon);
					
					dialog.dismiss();
				}
				
			});
			
			resultsContainer.addView(v);
		}
		dialog.show();
	}
}
