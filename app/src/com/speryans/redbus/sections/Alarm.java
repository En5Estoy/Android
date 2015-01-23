package com.speryans.redbus.sections;

import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.speryans.redbus.R;
import com.speryans.redbus.classes.Commons;
import com.speryans.redbus.classes.KeySaver;
import com.speryans.redbus.classes.SectionActivity;

public class Alarm extends SectionActivity {

	private LinearLayout alarmLayout;
	private TextView noAlarmTxt;
	private TextView detailsAlarmTxt;
	private Timer timer;
	private Button addAlarmBtn;
	private Button cancelAlarmBtn;
	
	public void onCreated() {
		// Alarm
				alarmLayout = ( LinearLayout ) parent.findViewById( R.id.alarmLayout );
				noAlarmTxt = ( TextView ) parent.findViewById(R.id.noAlarmTxt);
				detailsAlarmTxt = ( TextView ) parent.findViewById(R.id.detailsAlarmTxt);
				
				addAlarmBtn = ( Button ) parent.findViewById(R.id.addAlarmBtn);
				cancelAlarmBtn = ( Button ) parent.findViewById( R.id.cancelAlarmBtn );
				
				(( TextView ) parent.findViewById(R.id.iconAlarmTxt)).setTypeface(parent.mapicons);
				
				addAlarmBtn.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Commons.info("Oppening alarm");
						Intent intent = new Intent(parent, com.speryans.redbus.Alarm.class);
						parent.startActivity(intent);
					}
					
				});
				
				cancelAlarmBtn.setEnabled(false);
				cancelAlarmBtn.setOnClickListener( new OnClickListener() {

					@Override
					public void onClick(View v) {
						// Si se vacia la alarma cerrar el proceso
						if( KeySaver.getSaved(parent, KeySaver.ALARM_LAT) == "" &&
								KeySaver.getSaved(parent, KeySaver.ALARM_LON) == "" ) {
							Toast.makeText(parent, "No hay alarmas activas.", Toast.LENGTH_SHORT).show();
						} else {
							// Restore keys
							KeySaver.save(parent, KeySaver.ALARM, "");
							KeySaver.save(parent, KeySaver.ALARM_LAT, "");
							KeySaver.save(parent, KeySaver.ALARM_LON, "");
							
							Toast.makeText(parent, "Alarma desactivada. Es probable que demore algunos segundos en reflejarse en la interfaz.", Toast.LENGTH_SHORT).show();
						}
					}
					
				});
				
				this.createTimer();
	}
	
	private void createTimer() {
		alarmHandler.sendEmptyMessage(0);
		
		WaitTask task = new WaitTask();
		timer = new Timer();
		timer.scheduleAtFixedRate(task, 0, 3 * 1000);
	}
	
	@SuppressLint("HandlerLeak")
	private Handler alarmHandler = new Handler() {
		@Override
	    public void handleMessage(Message msg) {
			if(msg.what == 0) {
				alarmLayout.setVisibility(View.GONE);
				noAlarmTxt.setVisibility(View.VISIBLE);
				
				cancelAlarmBtn.setEnabled(false);
			} else {
				alarmLayout.setVisibility(View.VISIBLE);
				noAlarmTxt.setVisibility(View.GONE);
				
				cancelAlarmBtn.setEnabled(true);
				
				detailsAlarmTxt.setText( KeySaver.getSaved(parent, KeySaver.ALARM) );
			}
		}
	};

	class WaitTask extends TimerTask {

		@Override
		public void run() {
			try {
				// Si se vacia la alarma cerrar el proceso
				if( KeySaver.getSaved(parent, KeySaver.ALARM_LAT) == "" &&
						KeySaver.getSaved(parent, KeySaver.ALARM_LON) == "" ) {
					alarmHandler.sendEmptyMessage(0);
				} else {
					alarmHandler.sendEmptyMessage(1);
				}
				
				//createTimer();
			} catch( Exception ex ) {
				createTimer();
			}
		}
		
	}
}
