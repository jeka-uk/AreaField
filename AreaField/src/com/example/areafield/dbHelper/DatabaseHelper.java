package com.example.areafield.dbHelper;

import com.example.areafield.Adapter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

	//final String LOG_TAG = "myLogs";

	private static final String DB_NAME = "location.db";
	private static final int VERSION = 1;

	private static final String TABLE_NAME = "location";
	private static final String COLUMN_LOCATION_LATITUDE = "latitude";
	private static final String COLUMN_LOCATION_LONGITUDE = "longitude";
	private static final String COLUMN_LOCATION_ALTITUDE = "altitude";
	private static final String COLUMN_LOCATION_SPEED = "speed";
	
	private Adapter mAdapter;

	public DatabaseHelper(Context context) {
		super(context, DB_NAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		db.execSQL("create table location ("
				+ "_id integer primary key autoincrement," + "latitude real,"
				+ "longitude real," + "altitude real," + "speed real" + ");");

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	public long insertLocation(Location location) {
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_LOCATION_LATITUDE, location.getLatitude());
		cv.put(COLUMN_LOCATION_LONGITUDE, location.getLongitude());
		cv.put(COLUMN_LOCATION_ALTITUDE, location.getAltitude());
		cv.put(COLUMN_LOCATION_SPEED, location.getSpeed());
		return getWritableDatabase().insert(TABLE_NAME, null, cv);

	}

	public void cleardata(SQLiteDatabase db) {

		db.execSQL("delete from " + "location");
		db.execSQL("delete from " + "sqlite_sequence");

	}

	public void reaLocation(SQLiteDatabase db) {
		
		mAdapter = new Adapter();

		Cursor cv = db.query(TABLE_NAME, null, null, null, null, null, null);

		if (cv.moveToFirst()) {

		    int latitudeIndex = cv.getColumnIndex(COLUMN_LOCATION_LATITUDE);
			int longitudeIndex = cv.getColumnIndex(COLUMN_LOCATION_LONGITUDE);
			int altitudeIndex = cv.getColumnIndex(COLUMN_LOCATION_ALTITUDE);
			int speedIndex = cv.getColumnIndex(COLUMN_LOCATION_SPEED);
			int idIndex = cv.getColumnIndex("_id");

			/*Log.d(LOG_TAG,
					"_id = " + cv.getInt(idIndex) + ", latitude = "
							+ cv.getDouble(latitudeIndex) + ", longitude = "
							+ cv.getDouble(longitudeIndex) + ", altitude = "
							+ cv.getDouble(altitudeIndex) + ", speed = "
							+ cv.getDouble(speedIndex));*/
		
						
		//mAdapter.setMid(cv.getInt(idIndex));
			mAdapter.setMid(15);
		
		

		}
	}

}