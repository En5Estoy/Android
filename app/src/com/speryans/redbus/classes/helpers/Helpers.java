package com.speryans.redbus.classes.helpers;

import java.io.File;

import android.content.Context;

public class Helpers {

	public static File generateFolder( Context ctx, String dni, String card ) {
		return new File( ctx.getFilesDir() , dni + "-" + card + ".json" );
	}
	
}
