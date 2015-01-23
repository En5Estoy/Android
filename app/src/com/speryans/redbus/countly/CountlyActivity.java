package com.speryans.redbus.countly;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class CountlyActivity extends FragmentActivity {
	
    /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Countly.sharedInstance().init(this, "https://cloud.count.ly", "cb5f077affacb41594f16f9053a93876abc048e8");
    }
    
    @Override
    public void onStart()
    {
    	super.onStart();
        //Countly.sharedInstance().onStart();
    }

    @Override
    public void onStop()
    {
        //Countly.sharedInstance().onStop();
    	super.onStop();
    }
}
