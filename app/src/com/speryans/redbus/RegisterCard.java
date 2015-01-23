package com.speryans.redbus;

import java.util.Calendar;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.speryans.redbus.classes.Commons;
import com.speryans.redbus.classes.En5EstoyActivity;
import com.speryans.redbus.classes.ProgressHUD;
import com.speryans.redbus.classes.connection.Connection;
import com.speryans.redbus.classes.connection.DoneListener;
import com.speryans.redbus.classes.connection.ErrorListener;

public class RegisterCard extends En5EstoyActivity implements OnDateSetListener {
	
	private HashMap<String, String> values = new HashMap<String, String>();
	
	private String selectedItem = "";

	private Button lineBtn;

	private EditText dateTxt;

	private DatePickerDialog picker;

	private Button sendBtn;

	private EditText dniTxt;

	private EditText cardTxt;

	private LinearLayout container;
	
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register_card);
		
		container = (LinearLayout) this.findViewById(R.id.container);
		
		fontEverywhere(container, roboto);
		
		this.buttonsFont(container, icons);
		
		Commons.createHeader(this);
		
		(( TextView ) this.findViewById( R.id.titleTxt )).setTypeface(roboto);
		(( TextView ) this.findViewById( R.id.detailsTxt )).setTypeface(roboto);
		
		((TextView) this.findViewById(R.id.iconDetailsTxt)).setTypeface(icons); 
		
		((TextView) this.findViewById(R.id.iconHeader)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
    	this.addValues();
    	
    	dniTxt = ( EditText ) this.findViewById(R.id.dniTxt);
    	cardTxt = ( EditText ) this.findViewById(R.id.cardTxt);
    	
    	lineBtn = ( Button ) this.findViewById(R.id.lineBtn);
    	this.registerForContextMenu(lineBtn);
    	lineBtn.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				Commons.info("Open context");
				RegisterCard.this.openContextMenu(lineBtn);
			}
    		
    	});
    	
    	dateTxt = ( EditText ) this.findViewById( R.id.dateTxt );
    	dateTxt.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				RegisterCard.this.showDialogPicker();
			}
    		
    	});
    	dateTxt.setOnFocusChangeListener( new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if( hasFocus ) {
					RegisterCard.this.showDialogPicker();
				}
			}
    		
    	});
    	
    	sendBtn = (Button) this.findViewById(R.id.sendBtn);
    	sendBtn.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				ProgressHUD.show(RegisterCard.this);
				
				Connection.post(RegisterCard.this, "http://red-bus.com.ar/regcard/app.php")
					.param("txtLinea", selectedItem)
					.param("txtFecha", dateTxt.getText().toString())
					.param("txtDNI", dniTxt.getText().toString())
					.param("txtTarjeta", cardTxt.getText().toString())
					.done(new DoneListener() {

						@Override
						public void ready(String url, String data) {
							ProgressHUD.dismissHUD();
							if( !data.equalsIgnoreCase("") ) {
								Commons.dialog(RegisterCard.this, data);
							} else {
								Toast.makeText(RegisterCard.this, "Todos los campos son obligatorios.", Toast.LENGTH_SHORT).show();
							}
						}
						
					})
					.error(new ErrorListener() {

						@Override
						public void onError(String url, int msg) {
							ProgressHUD.dismissHUD();
						}
						
					})
					.start();
			}
    		
    	});
	}
	
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		
		menu.setHeaderTitle("Seleccione una linea...");
		
		for( String key : values.keySet() ) {
			menu.add(0, -1, 0, key);
		}
	}
	
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		} catch (ClassCastException e) {
			return false;
		}
		
		Commons.info("Item: " + values.get(item.getTitle()) );
		
		lineBtn.setText(item.getTitle());
		selectedItem = values.get(item.getTitle());
		
		return true;
	}
	
	private void addValues() {
		values.put("D2", "902");
		values.put("D4", "904");
		values.put("D5", "905");
		values.put("D1", "901");
		values.put("D3", "903");
		values.put("D6", "906");
		values.put("E1 Bosque", "421");
		values.put("E1 Reserva", "422");
		values.put("E1 IPEF", "423");
		values.put("N11", "327");
		values.put("C6", "518");
		values.put("E7", "417");
		values.put("E3er Cuerpo", "416");
		values.put("T", "126");
		values.put("T1", "127");
		values.put("500", "711");
		values.put("501", "712");
		values.put("Linea A", "1");
		values.put("Linea B", "2");
		values.put("Linea C", "3");
		values.put("N Central", "311");
		values.put("N7", "317");
		values.put("N8", "318");
		values.put("C Central", "511");
		values.put("C5", "512");
		values.put("B2", "550");
		values.put("600", "811");
		values.put("601", "812");
		values.put("N2", "312");
		values.put("N3", "313");
		values.put("N4", "314");
		values.put("N5", "315");
		values.put("N6", "316");
		values.put("N1", "319");
		values.put("C2", "513");
		values.put("C1", "514");
		values.put("C3", "515");
		values.put("C4", "516");
		values.put("C7", "517");
		values.put("A Central", "211");
		values.put("A2", "212");
		values.put("A3", "213");
		values.put("A4", "214");
		values.put("A5", "215");
		values.put("A6", "216");
		values.put("A7", "217");
		values.put("A8", "218");
		values.put("A9", "219");
		values.put("A10", "230");
		values.put("E1", "411");
		values.put("E2", "412");
		values.put("E3", "413");
		values.put("E4", "414");
		values.put("E5", "415");
		values.put("E Central", "419");
		values.put("R1", "112");
		values.put("R2", "113");
		values.put("R Central", "111");
		values.put("R3", "114");
		values.put("R4", "115");
		values.put("R5", "116");
		values.put("R6", "117");
		values.put("R8", "119");
		values.put("R9", "120");
		values.put("R10", "121");
		values.put("R11", "122");
		values.put("R2 B", "123");
		values.put("R12", "125");
		values.put("V H Central", "611");
		values.put("V U Central", "621");
		values.put("V1", "612");
		values.put("V2", "613");
		values.put("S", "190");
		values.put("R12 L", "128");
		values.put("R12 C", "129");
		
		values.put("10", "LINEA 10");
		values.put("11", "LINEA 11");
		values.put("12", "LINEA 12");
		values.put("13", "LINEA 13");
		values.put("14", "LINEA 14");
		values.put("15", "LINEA 15");
		values.put("16", "LINEA 16");
		values.put("17", "LINEA 17");
		values.put("18", "LINEA 18");
		values.put("19", "LINEA 19");
		values.put("810", "Diferencial 10");
		values.put("20", "LINEA 20");
		values.put("21", "LINEA 21");
		values.put("22", "LINEA 22");
		values.put("23", "LINEA 23");
		values.put("24", "LINEA 24");
		values.put("25", "LINEA 25");
		values.put("26", "LINEA 26");
		values.put("27", "LINEA 27");
		values.put("28", "LINEA 28");
		values.put("29", "LINEA 29");
		values.put("820", "Diferencial 20");
		values.put("720", "Barrial 20");
		values.put("30", "LINEA 30");
		values.put("31", "LINEA 31");
		values.put("32", "LINEA 32");
		values.put("33", "LINEA 33");
		values.put("34", "LINEA 34");
		values.put("35", "LINEA 35");
		values.put("36", "LINEA 36");
		values.put("730", "Barrial 30");
		values.put("830", "Diferencial 30");
		values.put("40", "LINEA 40");
		values.put("41", "LINEA 41");
		values.put("42", "LINEA 42");
		values.put("43", "LINEA 43");
		values.put("44", "LINEA 44");
		values.put("740", "Barrial 40");
		values.put("50", "LINEA 50");
		values.put("51", "LINEA 51");
		values.put("52", "LINEA 52");
		values.put("53", "LINEA 53");
		values.put("54", "LINEA 54");
		values.put("55", "LINEA 55");
		values.put("850", "Diferencial 50");
		values.put("60", "LINEA 60");
		values.put("61", "LINEA 61");
		values.put("62", "LINEA 62");
		values.put("63", "LINEA 63");
		values.put("64", "LINEA 64");
		values.put("65", "LINEA 65");
		values.put("66", "LINEA 66");
		values.put("67", "LINEA 67");
		values.put("68", "LINEA 68");
		values.put("760", "Barrial 60");
		values.put("761", "Barrial 61");
		values.put("70", "LINEA 70");
		values.put("71", "LINEA 71");
		values.put("72", "LINEA 72");
		values.put("73", "LINEA 73");
		values.put("74", "LINEA 74");
		values.put("75", "LINEA 75");
		values.put("770", "Barrial 70");
		values.put("80", "LINEA 80");
		values.put("81", "LINEA 81");
		values.put("82", "LINEA 82");
		values.put("83", "LINEA 83");
		values.put("84", "LINEA 84");
		values.put("780", "Barrial 80");
		values.put("880", "Diferencial 80");
		values.put("500", "Anular 500");
		values.put("501", "Anular 501");
		values.put("600", "Anular 600");
		values.put("601", "Anular 601");
	
	}
	
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	private void showDialogPicker() {
		final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        
        if( picker == null ) {
        	picker = new DatePickerDialog( this, this, year, month, day);
        }
		picker.show();
	}

	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear,
			int dayOfMonth) {
		String day = String.valueOf(dayOfMonth);
		String month = String.valueOf( (monthOfYear + 1 ) );
		if( day.length() == 1 ) {
			day = "0" + day;
		}
		if( month.length() == 1 ) {
			month = "0" + month;
		}
		dateTxt.setText( day + "/" + month + "/" + year );
	}
}
