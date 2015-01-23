package com.speryans.redbus.sections;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.speryans.redbus.R;
import com.speryans.redbus.classes.ProgressHUD;
import com.speryans.redbus.classes.SectionActivity;
import com.speryans.redbus.classes.StringUtilities;
import com.speryans.redbus.classes.Urls;
import com.speryans.redbus.classes.connection.Connection;
import com.speryans.redbus.classes.connection.DoneListener;
import com.speryans.redbus.stadistics.AppStats;

public class Commerce extends SectionActivity implements DoneListener {
	
	protected static final int SEARCH = 56;
	
	public Button categoriesBtn;
	public String selectedCategory = "-1";
	private Button searchPlacesBtn;
	private EditText searchTxt;
	
	public JSONArray categories = null;

	public void onCreated() {
		/*
		
		 */
		
		searchTxt = ( EditText ) parent.findViewById(R.id.searchTxt);
		
		searchPlacesBtn = ( Button ) parent.findViewById(R.id.searchPlacesBtn);
		searchPlacesBtn.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				ProgressHUD.show(parent, "Buscando...", true, null);
				
				AppStats.addCategoriesSearch(parent, categoriesBtn.getText().toString());
				AppStats.addSearch(parent, searchTxt.getText().toString());
				
				Connection.post(parent, Urls.POINTS_SEARCH)
					.param("lat", String.valueOf(parent.location.latitude))
					.param("lon", String.valueOf(parent.location.longitude))
					.param("category", selectedCategory)
					.param("search", StringUtilities.removeAccents(searchTxt.getText().toString()) )
					.done(Commerce.this)
					.error(parent)
					.start();
				
				//Connection.postInstance(Urls.POINTS_SEARCH, Commerce.this, SEARCH, values).start();
			}
			
		});
		
		categoriesBtn = ( Button ) parent.findViewById(R.id.categoryBtn);
		parent.registerForContextMenu(categoriesBtn);
		categoriesBtn.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				if( categories == null ) {
					ProgressHUD.show(parent, "Actualizando categorias...", true, null);
					
					Connection.get(parent, Urls.POINTS_CATEGORY)
						.done(new DoneListener() {

							@Override
							public void ready(String url, String data) {
								ProgressHUD.dismissHUD();
								
								try {
									categories = (new JSONObject(data)).getJSONArray("categories");
									
									parent.openContextMenu(categoriesBtn);
								} catch (Exception e) {
									Toast.makeText(parent, "Se produjo un error cargando las categorias.", Toast.LENGTH_SHORT).show();
								}
							}
							
						})
						.error(parent)
						.start();
				} else {
					// open menu
					parent.openContextMenu(categoriesBtn);
				}
			}
			
		});
		
	}

	@Override
	public void ready(String url, String message) {
		try {
			ProgressHUD.dismissHUD();
			JSONObject data = new JSONObject(message);
			
			if( data.getJSONArray("venues").length() > 0 ) {
				Intent intent = new Intent(parent, com.speryans.redbus.Interests.class);
				intent.putExtra("category", categoriesBtn.getText().toString());
				intent.putExtra("data", data.getJSONArray("venues").toString());
				
				parent.startActivity(intent);
			} else {
				Toast.makeText(parent, R.string.commerce_no_result, Toast.LENGTH_LONG).show();
			}
		} catch( Exception e ) {
			Toast.makeText(parent, R.string.commerce_no_result, Toast.LENGTH_LONG).show();
		}
	}
}
