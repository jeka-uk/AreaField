package com.example.areafield.dbHelper;

import com.example.areafield.Constant;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

	// final String LOG_TAG = "myLogs";

	private static DatabaseHelper mInstance;
	private static SQLiteDatabase myWritableDb;

	private static final String DB_NAME = "location.db";
	private static final int VERSION = 1;

	public DatabaseHelper(Context context) {
		super(context, DB_NAME, null, VERSION);
	}

	public static DatabaseHelper getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new DatabaseHelper(context);
		}
		return mInstance;
	}

	public SQLiteDatabase getMyWritableDatabase() {
		if ((myWritableDb == null) || (!myWritableDb.isOpen())) {
			myWritableDb = this.getWritableDatabase();
		}

		return myWritableDb;
	}

	@Override
	public void close() {
		super.close();
		if (myWritableDb != null) {
			myWritableDb.close();
			myWritableDb = null;
		}
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
		cv.put(Constant.COLUMN_LOCATION_LATITUDE, location.getLatitude());
		cv.put(Constant.COLUMN_LOCATION_LONGITUDE, location.getLongitude());
		cv.put(Constant.COLUMN_LOCATION_ALTITUDE, location.getAltitude());
		cv.put(Constant.COLUMN_LOCATION_SPEED, location.getSpeed());
		return getWritableDatabase().insert(Constant.TABLE_NAME, null, cv);

	}

	public void cleardata() {

		getMyWritableDatabase().execSQL("delete from " + "location");
		getMyWritableDatabase().execSQL("delete from " + "sqlite_sequence");

	}

	public Double getLocation(int columIndex, String columName) {

		Cursor cv = getMyWritableDatabase().query(Constant.TABLE_NAME, null,
				null, null, null, null, null);

		columIndex = cv.getColumnIndex(columName);

		cv.moveToFirst();
		
		while (cv.isAfterLast() == false) {
			
			// действие пока не достигнет последней записи
			cv.moveToNext();
		}
		
		return cv.getDouble(columIndex);
	}

}