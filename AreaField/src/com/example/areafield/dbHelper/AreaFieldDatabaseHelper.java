package com.example.areafield.dbHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;

public class AreaFieldDatabaseHelper extends SQLiteOpenHelper {	
	 
	private static final String DB_NAME = "location.sqlite";
	private static final int VERSION = 1;
	
	private static final String TABLE_LOCATION = "location";
    private static final String COLUMN_LOCATION_LATITUDE = "latitude";
    private static final String COLUMN_LOCATION_LONGITUDE = "longitude";
    private static final String COLUMN_LOCATION_ALTITUDE = "altitude";
    private static final String COLUMN_LOCATION_SPEED = "speed";    
    private static final String COLUMN_LOCATION_LOCATION_ID = "_id";
	
	public AreaFieldDatabaseHelper(Context context) {
		super(context, DB_NAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		
		// create the "location" table
	    db.execSQL("create table location (_id integer primary key autoincrement, latitude real, longitude real, altitude real, speed real");
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
		
	}
	
	 public long insertLocation( Location location) {
	        ContentValues cv = new ContentValues();
	        cv.put(COLUMN_LOCATION_LATITUDE, location.getLatitude());
	        cv.put(COLUMN_LOCATION_LONGITUDE, location.getLongitude());
	        cv.put(COLUMN_LOCATION_ALTITUDE, location.getAltitude());
	        cv.put(COLUMN_LOCATION_SPEED, location.getSpeed()*3.6);	        
	        //cv.put(COLUMN_LOCATION_LOCATION_ID, locId);
	        return getWritableDatabase().insert(TABLE_LOCATION, null, cv);
	    }

}