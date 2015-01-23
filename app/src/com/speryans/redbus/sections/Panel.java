package com.speryans.redbus.sections;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.SlidingMenu.OnOpenListener;
import com.speryans.redbus.R;
import com.speryans.redbus.classes.FavoritesManagement;
import com.speryans.redbus.classes.SectionActivity;

public class Panel extends SectionActivity {
	
	public SlidingMenu menu;
	
	public LinearLayout busButton;
	public LinearLayout taxiButton;
	public LinearLayout interestButton;
	public LinearLayout newsButton;
	public LinearLayout accountButton;
	
	public LinearLayout favoritesContainer;
	public LinearLayout favoritesLayout;

	private LinearLayout alarmButton;

	private LinearLayout wizardButton;

	public void onCreated() {
		menu = new SlidingMenu(parent);
        menu.setMode(SlidingMenu.LEFT);
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        menu.setShadowWidthRes(R.dimen.shadow_width);
        menu.setShadowDrawable(R.drawable.shadowleft);
        menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        menu.setFadeDegree(0.35f);
        menu.attachToActivity(parent, SlidingMenu.SLIDING_WINDOW);
        menu.setMenu(R.layout.panel);
        
        parent.iconHeader.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				menu.showMenu();
			}
        	
        });
        
        ((TextView)parent.findViewById(R.id.icon_option_text_news)).setTypeface(parent.icons);
        ((TextView)parent.findViewById(R.id.icon_option_text_bus)).setTypeface(parent.mapicons);
        ((TextView)parent.findViewById(R.id.icon_option_text_alarm)).setTypeface(parent.icons);
        ((TextView)parent.findViewById(R.id.icon_option_text_account)).setTypeface(parent.icons);
        ((TextView)parent.findViewById(R.id.icon_option_text_taxi)).setTypeface(parent.mapicons);
        ((TextView)parent.findViewById(R.id.icon_option_text_points)).setTypeface(parent.mapicons);
        ((TextView)parent.findViewById(R.id.icon_option_text_settings)).setTypeface(parent.mapicons);
        ((TextView)parent.findViewById(R.id.icon_option_text_wizard)).setTypeface(parent.icons);
        ((TextView) parent.findViewById(R.id.iconDetailsTxt)).setTypeface(parent.icons);
        ((TextView) parent.findViewById(R.id.registerIcon)).setTypeface(parent.icons);
        ((TextView) parent.findViewById(R.id.iconAlarmTxt)).setTypeface(parent.icons);
        ((TextView) parent.findViewById(R.id.iconDetailsNewsTxt)).setTypeface(parent.icons);
        //((TextView) parent.findViewById(R.id.iconNewVersion)).setTypeface(parent.icons);
        
        if( android.os.Build.VERSION.SDK_INT >= 19 ) {
			((ImageView) parent.findViewById(R.id.topPanelShadow)).setVisibility(View.GONE);
		}
        
        this.buttonListeners();
	}

	private void buttonListeners() {
		// Panel buttons
		
		alarmButton = (LinearLayout) menu.findViewById(R.id.alarmButton);
		alarmButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				menu.showContent();
				if( parent.titleTxt.getText().equals( parent.getString(R.string.alarm) ) ) return;
				
				parent.titleTxt.setText(R.string.alarm);
				
				Panel.this.hideActualTab();
				
				parent.alarmContainer.setVisibility(View.VISIBLE);
				parent.alarmContainer.startAnimation(AnimationUtils.loadAnimation(parent, android.R.anim.fade_in) );
			}
        });
		
        busButton = (LinearLayout) menu.findViewById(R.id.busButton);
        busButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				menu.showContent();
				if( parent.titleTxt.getText().equals( parent.getString(R.string.colectivos) ) ) return;
				
				parent.titleTxt.setText(R.string.colectivos);
				
				Panel.this.hideActualTab();
				
				parent.busContainer.setVisibility(View.VISIBLE);
				parent.busContainer.startAnimation(AnimationUtils.loadAnimation(parent, android.R.anim.fade_in) );
			}
        });
        
        accountButton = (LinearLayout) menu.findViewById(R.id.accountButton);
        accountButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				menu.showContent();
				if( parent.titleTxt.getText().equals( parent.getString(R.string.account) ) ) return;
				parent.titleTxt.setText(R.string.account);
				
				Panel.this.hideActualTab();
				
				parent.accountContainer.setVisibility(View.VISIBLE);
				parent.accountContainer.startAnimation(AnimationUtils.loadAnimation(parent, android.R.anim.fade_in) );
				
				parent.red_bus.loadHistoryCards();
			}
        });
        
        taxiButton = (LinearLayout) menu.findViewById(R.id.taxiButton);
        taxiButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				menu.showContent();
				if( parent.titleTxt.getText().equals( parent.getString(R.string.taxis) ) ) return;
				parent.titleTxt.setText(R.string.taxis);
				
				Panel.this.hideActualTab();
				
				parent.taxiContainer.setVisibility(View.VISIBLE);
				parent.taxiContainer.startAnimation(AnimationUtils.loadAnimation(parent, android.R.anim.fade_in) );				
			}
        });
        
        interestButton = (LinearLayout) menu.findViewById(R.id.interestButton);
        interestButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				menu.showContent();
				if( parent.titleTxt.getText().equals( parent.getString(R.string.points) ) ) return;
				parent.titleTxt.setText(R.string.points);
				
				Panel.this.hideActualTab();
				
				parent.interestContainer.setVisibility(View.VISIBLE);
				parent.interestContainer.startAnimation(AnimationUtils.loadAnimation(parent, android.R.anim.fade_in) );
				
			}
        });
        
        newsButton = (LinearLayout) menu.findViewById(R.id.newsButton);
        newsButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				menu.showContent();
				if( parent.titleTxt.getText().equals( parent.getString(R.string.news) ) ) return;
				parent.titleTxt.setText(R.string.news);
				
				Panel.this.hideActualTab();
				
				parent.newsContainer.setVisibility(View.VISIBLE);
				parent.newsContainer.startAnimation(AnimationUtils.loadAnimation(parent, android.R.anim.fade_in) );
				
				parent.notifications.loadE5eNews();
			}
        });
        
        wizardButton = (LinearLayout) menu.findViewById(R.id.wizardButton);
        wizardButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				menu.showContent();
				
				parent.wizard.showChangeCity(R.string.change_city, false);
			}
        });
        
        favoritesContainer = (LinearLayout) menu.findViewById(R.id.favoritesContainer);
        favoritesLayout = (LinearLayout) menu.findViewById(R.id.favoritesLayout);
        
        ((TextView) menu.findViewById(R.id.favoritesTxt)).setTypeface(parent.roboto);
        
        menu.setOnOpenListener(new OnOpenListener() {

			@Override
			public void onOpen() {
				favoritesContainer.removeAllViews();
				
				JSONArray favorites = FavoritesManagement.getFavorites(parent);
				
				if( favorites.length() == 0 ) {
					favoritesLayout.setVisibility(View.GONE);
				} else {
					favoritesLayout.setVisibility(View.VISIBLE);
				}
				
				for( int i = 0 ; i < favorites.length() ; i++ ) {
					final JSONObject obj = favorites.optJSONObject(i);
					if( !obj.has("contact") ) { // Remove old saved
						FavoritesManagement.removeFavorite(parent, obj);
						
						continue;
					}
					
					View v = LayoutInflater.from(parent).inflate(R.layout.interest_item, null, false);
					
					TextView nameText = (TextView) v.findViewById(R.id.text);
					nameText.setText( obj.optString("name") );
					
					((TextView) v.findViewById(R.id.icon_option_text)).setTypeface(parent.icons);
					
					
					((LinearLayout) v.findViewById(R.id.interestPointButton)).setOnClickListener( new OnClickListener() {

						@Override
						public void onClick(View v) {
							Intent intent = new Intent(parent, com.speryans.redbus.Interests.class);
							intent.putExtra("category", "Favorito");
							intent.putExtra("data", ((new JSONArray()).put(obj)).toString());
							
							parent.startActivity(intent);
							
							menu.showContent();
						}
						
					});
					
					favoritesContainer.addView(v);
				}
			}
        	
        });
	}
	
	private void hideActualTab() {
		parent.alarmContainer.startAnimation(AnimationUtils.loadAnimation(parent, android.R.anim.fade_out) );
		parent.alarmContainer.setVisibility(View.GONE);
		parent.taxiContainer.startAnimation(AnimationUtils.loadAnimation(parent, android.R.anim.fade_out) );
		parent.taxiContainer.setVisibility(View.GONE);
		parent.busContainer.startAnimation(AnimationUtils.loadAnimation(parent, android.R.anim.fade_out) );
		parent.busContainer.setVisibility(View.GONE);
		parent.accountContainer.startAnimation(AnimationUtils.loadAnimation(parent, android.R.anim.fade_out) );
		parent.accountContainer.setVisibility(View.GONE);
		parent.interestContainer.startAnimation(AnimationUtils.loadAnimation(parent, android.R.anim.fade_out) );
		parent.interestContainer.setVisibility(View.GONE);
		parent.newsContainer.startAnimation(AnimationUtils.loadAnimation(parent, android.R.anim.fade_out) );
		parent.newsContainer.setVisibility(View.GONE);
	}
}
