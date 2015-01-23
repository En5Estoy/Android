package com.speryans.redbus;

import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.speryans.redbus.classes.Commons;
import com.speryans.redbus.classes.En5EstoyActivity;
import com.speryans.redbus.classes.KeySaver;
import com.speryans.redbus.classes.ProgressHUD;
import com.speryans.redbus.classes.Urls;
import com.speryans.redbus.classes.connection.Connection;
import com.speryans.redbus.classes.connection.DoneListener;
import com.speryans.redbus.classes.connection.ErrorListener;
import com.viewpagerindicator.LinePageIndicator;

public class Wizard extends En5EstoyActivity {
	
	public Vector<WizardPage> pages = new Vector<WizardPage>();
	private Vector<String> cities = new Vector<String>();

	private ViewPager wizardPager;

	private LinePageIndicator mIndicator;
	
	@TargetApi(19)
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if( android.os.Build.VERSION.SDK_INT >= 19 ) {
			Window window = this.getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		}
		
		setContentView(R.layout.wizard);
		
		((TextView) this.findViewById(R.id.slideTxt)).setTypeface(roboto);
		
		wizardPager = (ViewPager) this.findViewById(R.id.wizardPager);
		
		pages.add(new WizardPage("Bienvenido!", "En 5' Estoy te permite conocer todos los detalles de los servicios de tu ciudad. La aplicación te acompañará a todos lados y se personalizará de acuerdo a tu ciudad actual.", R.drawable.wizard_e5e) );
		pages.add(new WizardPage("Transporte público", "Mediante la opción de transporte podrás conocer que medio de transporte usar para moverte en tu ciudad.", R.drawable.wizard_transport) );
		pages.add(new WizardPage("Transporte privado", "Viaje en taxi tranquilo de conocer el costo y su ruta con En 5' Estoy.", R.drawable.wizard_travel) );
		pages.add(new WizardPage("Tarjetas de Transporte", "Con En 5' Estoy podrá conocer el saldo de su tarjeta de Transporte desde su smartphone. ( Sólo Red Bus ).", R.drawable.wizard_cards) );
		pages.add(new WizardPage("Lugares", "Encuentre comercios y lugares cercanos a su ubicación y de la forma más simple.", R.drawable.wizard_places) );
		pages.add(new WizardPage("Comencemos!", "Elija su ciudad actual o pruebe alguna de las ciudades actualmente disponibles.", R.drawable.wizard_select) );
		
		WizardPagerAdapter adapter = new WizardPagerAdapter();
	    wizardPager.setAdapter(adapter);
	    
	    mIndicator = (LinePageIndicator)findViewById(R.id.indicator);
        mIndicator.setViewPager(wizardPager);
        mIndicator.setSelectedColor(0xFFe67e22);
        
        this.loadCities();
	}
	
	private void loadCities() {
		if( cities.size()  == 0 ) {
			Connection
        	.get(this, Urls.CITIES)
        	.done(new DoneListener() {

				@Override
				public void ready(String url, String data) {
					try {
						JSONArray arr = (new JSONObject(data)).getJSONArray("data");
						
						for( int i = 0 ; i < arr.length() ; i++ ) {
							cities.add( arr.getJSONObject(i).getString("name") );
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
        	
        	}).start();
		}
	}
	
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	
	private class WizardPage {
		public String title;
		public String text;
		public int image;
		
		public WizardPage(String title, String text, int image) {
			this.title = title;
			this.text = text;
			this.image = image;
		}
	}
	
	private class WizardPagerAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return pages.size();
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == ((LinearLayout) object);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			WizardPage page = pages.elementAt(position);
			
			LinearLayout view = (LinearLayout) LayoutInflater.from(Wizard.this).inflate(R.layout.wizard_row, null, false);
			
			((TextView) view.findViewById(R.id.titleTxt)).setText( page.title );
			((TextView) view.findViewById(R.id.textTxt)).setText( page.text );
			
			((ImageView) view.findViewById(R.id.image)).setImageResource( page.image );

			final Spinner spinner = (Spinner) view.findViewById(R.id.wspinner);
			Button ready = (Button) view.findViewById(R.id.ready);
			
			ready.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					ProgressHUD.show(Wizard.this, "Registrando...");
					
					Connection
						.post(Wizard.this, Urls.REGISTER_USER)
						.param("os", "android")
						.param("udid", Commons.getDeviceID(Wizard.this))
						.param("pid", "")
						.param("city", (String)spinner.getSelectedItem())
						.done(new DoneListener() {

							@Override
							public void ready(String url, String data) {
								try {
									ProgressHUD.dismissHUD();
									
									JSONObject obj = new JSONObject(data);
									
									if( obj.getBoolean("result") ) {
										KeySaver.save(Wizard.this, KeySaver.CITY_DATA, obj.getJSONObject("city").toString());
										KeySaver.save(Wizard.this, KeySaver.NO_CHANGE_CITY, "");
										
										Intent i = new Intent(Wizard.this, com.speryans.redbus.Home.class);
										Wizard.this.startActivity(i);
										
										Wizard.this.finish();
									}
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}
							
						})
						.error(new ErrorListener() {

							@Override
							public void onError(String url, int msg) {
								Commons.info("Error!");
							}
							
						})
						.start();
				}
				
			});
	        
			if(page.image == R.drawable.wizard_select) {
				ArrayAdapter<String> aa = new ArrayAdapter<String>(Wizard.this, android.R.layout.simple_spinner_item, cities);
				aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				spinner.setAdapter(aa);
			} else {
				spinner.setVisibility(View.GONE);
				ready.setVisibility(View.GONE);
			}
			
			((ViewPager) container).addView(view, 0);

			return view;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			((ViewPager) container).removeView((LinearLayout) object);
		}

		@Override
		public Parcelable saveState() {
			return null;
		}
	}
}
