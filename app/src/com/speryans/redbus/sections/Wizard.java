package com.speryans.redbus.sections;

import java.util.List;
import java.util.Locale;
import java.util.Vector;

import org.json.JSONArray;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Geocoder;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.WazaBe.HoloEverywhere.HoloAlertDialogBuilder;
import com.espian.showcaseview.OnShowcaseEventListener;
import com.espian.showcaseview.ShowcaseView;
import com.espian.showcaseview.targets.ViewTarget;
import com.google.android.gcm.GCMRegistrar;
import com.slidingmenu.lib.SlidingMenu;
import com.speryans.redbus.GCMIntentService;
import com.speryans.redbus.Home;
import com.speryans.redbus.R;
import com.speryans.redbus.classes.Commons;
import com.speryans.redbus.classes.KeySaver;
import com.speryans.redbus.classes.SectionActivity;
import com.speryans.redbus.classes.helpers.CityManager;

/**
 * This class starts to be very important on the new version
 * because check changes and control the "wizard" showed to the user
 * on the new cities.
 * 
 * Functions
 * - Check new cities.
 * - Check notifications system.
 * - Show the start or new functions wizard.
 * 
 */
public class Wizard extends SectionActivity {
	
	private TextView currentCityTxt;
	
	
	
	private boolean isWizard = false;
	
	public void onCreated() {
		currentCityTxt = (TextView) parent.findViewById(R.id.currentCityTxt);
		
		//Actualizar en la actualización de Ciudad
		GCMRegistrar.checkDevice(parent);
		GCMRegistrar.checkManifest(parent);
		final String regId = GCMRegistrar.getRegistrationId(parent);
		if (regId.equals("")) {
		  GCMRegistrar.register(parent, GCMIntentService.REG_ID);
		} else {
			GCMIntentService.registerOnServer(parent, regId);
		}
		
		// Show init wizard
		// This is just show if the user is not registered. (No City Stored)
		
		// Show the new options in the app and the design.
		// Ask if the user wants the tour
		
		if( KeySaver.getSaved(parent, KeySaver.WIZARD) == "" || 
				Integer.valueOf(KeySaver.getSaved(parent, KeySaver.WIZARD)).intValue() < 1 ) {
			this.isWizard = true;
			this.showWizard();
		}
		
		currentCityTxt.setText( CityManager.getCity(parent) );
		
		if( KeySaver.getSaved(parent, KeySaver.NO_CHANGE_CITY).equalsIgnoreCase("") ) {
			this.loadCurrentCity();
		}
	}
	
	/**
	 * Check if the current city was changed with the KeyWords
	 */
	public void loadCurrentCity() {
		// Boton para que no se vuelva a mostrar. Borrar valor cuando se ejecuta de nuevo el wizard.
		// PRECIOS!!!
		try {
			Geocoder gcd = new Geocoder(parent, Locale.getDefault());
			List<Address> addresses;
		
			addresses = gcd.getFromLocation(parent.location.latitude, parent.location.longitude, 1);
			if (addresses.size() > 0) {
				JSONArray kws = CityManager.getKeywords(parent);
				
				for( int i = 0 ; i < kws.length() ; i++ ) {
					if( addresses.get(0).getLocality().equalsIgnoreCase( kws.getString(i) ) ) {
						return;
					}
				}
				
				showChangeCity(R.string.different_city, true);
			}
		} catch (Exception e) {
			Commons.error("Current City: " + e.toString(), e);
		}
	}
	
	public void showChangeCity(int text, boolean no_ask) {
		AlertDialog.Builder builder = new HoloAlertDialogBuilder(parent);
		
		builder.setInverseBackgroundForced(true);
		builder.setCancelable(false);
		builder.setTitle(R.string.application_name);
		TextView view = (TextView)LayoutInflater.from(parent).inflate(R.layout.workarounddialogs, null, false);
		view.setText(text);
		builder.setView( view );
		if( no_ask ) {
			builder.setNeutralButton(R.string.change_city_no_ask, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					KeySaver.save(parent, KeySaver.NO_CHANGE_CITY, "1");
					
					dialog.dismiss();
				}
			});
		}
		builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(parent, com.speryans.redbus.Wizard.class);
				parent.startActivity(intent);
				
				parent.finish();
			}
		});
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	@SuppressLint("NewApi")
	private void showWizard() {
		ViewTarget mvt = new ViewTarget(parent.iconHeader);
		ShowcaseView
		.insertShowcaseView(mvt, parent, "Menú", "Toca este botón para ingresar a todas las secciones de la aplicación.")
		.setOnShowcaseEventListener(new OnShowcaseEventListener() {

			@Override public void onShowcaseViewHide(ShowcaseView showcaseView) {}
			@Override public void onShowcaseViewShow(ShowcaseView showcaseView) {}
			
			@Override
			public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
				Commons.info("Menú dismissed");
				
				parent.panel.menu.showMenu();
				ViewTarget mvt = new ViewTarget(currentCityTxt);
				ShowcaseView
				.insertShowcaseView(mvt, parent, "Ciudad", "Aquí aparecerá tu ciudad actual.")
				.setOnShowcaseEventListener(new OnShowcaseEventListener() {

					@Override public void onShowcaseViewHide(ShowcaseView showcaseView) {}
					@Override public void onShowcaseViewShow(ShowcaseView showcaseView) {}
					
					@Override
					public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
						parent.panel.menu.showContent();
						
						ViewTarget mvt = new ViewTarget(parent.footer.newsTicker);
						ShowcaseView
						.insertShowcaseView(mvt, parent, "Noticias y Clima", "Aquí podras ver el estado del clima de tu ciudad y las últimas noticias. Para ver más detalles expandí la solapa hacia arriba.")
						.setOnShowcaseEventListener(new OnShowcaseEventListener() {

							@Override public void onShowcaseViewHide(ShowcaseView showcaseView) {}
							@Override public void onShowcaseViewShow(ShowcaseView showcaseView) {}
							
							@Override
							public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
								ViewTarget mvt = new ViewTarget(parent.shareBtn);
								ShowcaseView
								.insertShowcaseView(mvt, parent, "Compartí", "Compartí esta aplicación con tus amigos y ayudanos a mejorar.")
								.setOnShowcaseEventListener(new OnShowcaseEventListener() {

									@Override public void onShowcaseViewHide(ShowcaseView showcaseView) {}
									@Override public void onShowcaseViewShow(ShowcaseView showcaseView) {}
									
									@Override
									public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
										KeySaver.save(parent, KeySaver.WIZARD, "1");
									}
									
								});
							}
							
						});
					}
					
				});
			}
			
		});
	}
	
}
