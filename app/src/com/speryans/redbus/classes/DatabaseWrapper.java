package com.speryans.redbus.classes;

import java.io.File;
import java.io.FileOutputStream;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DatabaseWrapper {
	public static final String DATABASE_NAME = "redbus.db";
	private static final int DATABASE_VERSION = 1;
	
	private DbHelper _dbHelper;

    public DatabaseWrapper(Context c) {
        _dbHelper = DbHelper.getInstance(c);
    }
    
    public void close() {
    	_dbHelper.close();
    }
    
    // Trae los centros de recarga
    public Cursor getCenters( double lat, double lng, int limit ) throws Exception {
    	SQLiteDatabase db = _dbHelper.getReadableDatabase();    	
    	
    	try {
    	// Simple fix.
    	Cursor current_place = db.rawQuery("SELECT *, ((? - lat) * (? - lat) + (? - lng) * (? - lng) ) as distance FROM markers WHERE tipo = 'pdv' order by distance asc LIMIT 1", new String[] {String.valueOf(lat), String.valueOf(lat), String.valueOf(lng), String.valueOf(lng) });
    	current_place.moveToFirst();
    	
    	return db.rawQuery("SELECT *, ((? - lat) * (? - lat) + (? - lng) * (? - lng) ) as distance FROM markers WHERE tipo = 'pdv' and barrio = ? order by distance asc LIMIT ?", new String[] {String.valueOf(lat), String.valueOf(lat), String.valueOf(lng), String.valueOf(lng), current_place.getString( current_place.getColumnIndex("barrio")), String.valueOf(limit) });
    	} catch(Exception e ) {
    		throw e;
    	}
    }
    
    public Cursor getCenterById( long id ) {
    	SQLiteDatabase db = _dbHelper.getReadableDatabase();    	
    	
    	
    	return db.rawQuery("SELECT * FROM markers WHERE _id = ?", new String[] {String.valueOf(id) });
    }
    
    // Trae todos centros de recarga
    public Cursor getAllCenters( double lat, double lng, String filter ) throws Exception {
    	SQLiteDatabase db = _dbHelper.getReadableDatabase();
    	
    	try {
    		return db.rawQuery("SELECT *, ((? - lat) * (? - lat) + (? - lng) * (? - lng) ) as distance FROM markers WHERE tipo = 'pdv' AND address LIKE ? OR barrio LIKE ? order by distance asc", new String[] {String.valueOf(lat), String.valueOf(lat), String.valueOf(lng), String.valueOf(lng), "%" + filter + "%", "%" + filter + "%" });
    	} catch( Exception e ) {
    		throw e;
    	}
    }
    
 // Trae los centros de atenci�n al cliente
    public Cursor getAtention( double lat, double lng, int limit ) {
    	SQLiteDatabase db = _dbHelper.getReadableDatabase();
    	
    	// Simple fix.
    	//Cursor current_place = db.rawQuery("SELECT *, ((? - lat) * (? - lat) + (? - lng) * (? - lng) ) as distance FROM markers WHERE tipo = 'pdv' order by distance asc LIMIT 1", new String[] {String.valueOf(lat), String.valueOf(lat), String.valueOf(lng), String.valueOf(lng) });
    	//current_place.moveToFirst();
    	
    	return db.rawQuery("SELECT *, ((? - lat) * (? - lat) + (? - lng) * (? - lng) ) as distance FROM markers WHERE tipo <> 'pdv' order by distance asc LIMIT ?", new String[] {String.valueOf(lat), String.valueOf(lat), String.valueOf(lng), String.valueOf(lng), String.valueOf(limit) });
    	//return db.rawQuery("SELECT *, ( 3959 * acos( cos( radians('?') ) * cos( radians( lat ) ) * cos( radians( lng ) - radians('?') ) + sin( radians('?') ) * sin( radians( lat ) ) ) ) AS distance FROM markers WHERE tipo <> 'pdv' LIMIT ?", new String[] {String.valueOf(lat), String.valueOf(lng), String.valueOf(lat), "10", String.valueOf(limit)});
    }
    
    // En update el WHERE es nombre_columna = valor solamente.
	
	public static class DbHelper extends SQLiteOpenHelper {
		static DbHelper instance = null;
		private Context context;
		
		public DbHelper(Context context) {
			super(context, context.getFilesDir().getAbsolutePath() + "/" + DATABASE_NAME, null, DATABASE_VERSION);
			
			//this.checkDatabase();
		    
		    Commons.info(context.getFilesDir().getAbsolutePath() + "/" + DATABASE_NAME);
		    
		    this.context = context;
		}
		
		public static DbHelper getInstance( Context cnt ) {
			if( instance == null ) {
				instance = new DbHelper( cnt );
			}
			return instance;
		}
		
		public void checkDatabase() {
			File db_file = new File(context.getFilesDir(), DatabaseWrapper.DATABASE_NAME);
			if( !db_file.exists() ) {
				Commons.info("File not exists: copying the DB");
				try {
					Utils.CopyStream(context.getAssets().open(DatabaseWrapper.DATABASE_NAME), new FileOutputStream(db_file));
					Commons.info("DB copied");
				} catch (Exception e) {
					Commons.error("Error in database copy: " + e.toString(), e);
				}
			}
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			this.checkDatabase();
		}

	    @Override
	    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	        // TODO Auto-generated method stub

	    }

	}
}
