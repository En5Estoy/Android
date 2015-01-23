package com.speryans.redbus.classes;

import com.speryans.redbus.Home;
import android.os.Bundle;

public class SectionActivity {
	public Bundle savedInstance;
	public Home parent;
	
	public void onCreate(Bundle savedInstanceState, Home parent) {
		this.savedInstance = savedInstanceState;
		this.parent = parent;
		
		this.onCreated();
	}
	
	public void onCreated() {}
	
}
