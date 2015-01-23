package com.speryans.redbus.classes;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.speryans.redbus.R;

public class ProgressHUD extends Dialog {
	public static ProgressHUD instance = null;
	
	public ProgressHUD(Context context) {
		super(context);
	}

	public ProgressHUD(Context context, int theme) {
		super(context, theme);
	}


	public void onWindowFocusChanged(boolean hasFocus){
		/*ImageView imageView = (ImageView) findViewById(R.id.spinner);
        AnimationDrawable spinner = (AnimationDrawable) imageView.getBackground();
        spinner.start();*/
    }
	
	public void setMessage(CharSequence message) {
		if(message != null && message.length() > 0) {
			findViewById(R.id.message).setVisibility(View.VISIBLE);			
			TextView txt = (TextView)findViewById(R.id.message);
			txt.setText(message);
			txt.invalidate();
		}
	}
	
	public static void dismissHUD() {
		try {
			instance.dismiss();
		} catch( Exception e ){}
	}
	
	public static void show( Context context ) {
		ProgressHUD.show(context, "", false, null);
	}
	
	public static void show( Context context, CharSequence message ) {
		ProgressHUD.show(context, message, false, null);
	}
	
	public static void show(Context context, CharSequence message, boolean cancelable, OnCancelListener cancelListener) {
		ProgressHUD dialog = new ProgressHUD(context,R.style.ProgressHUD);
		dialog.setTitle("");
		dialog.setContentView(R.layout.progress_hud);
		if(message == null || message.length() == 0) {
			dialog.findViewById(R.id.message).setVisibility(View.GONE);			
		} else {
			TextView txt = (TextView)dialog.findViewById(R.id.message);
			txt.setText(message);
		}
		dialog.setCancelable(cancelable);
		dialog.setOnCancelListener(cancelListener);
		dialog.getWindow().getAttributes().gravity=Gravity.CENTER;
		WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();  
		lp.dimAmount=0.2f;
		dialog.getWindow().setAttributes(lp); 
		//dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
		dialog.show();
		//return dialog;
		instance = dialog;
	}	
}