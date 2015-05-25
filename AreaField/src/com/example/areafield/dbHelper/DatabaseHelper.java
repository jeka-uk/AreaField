package com.example.areafield.dbHelper;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.example.areafield.Constants;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;

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

		// Create table series
		db.execSQL("create table series ("
				+ "_id integer primary key autoincrement,"
				+ "timestamp integer," + "nameseries text,"
				+ "routing integer," + "areaplowed integer" + ");");

		// Create table location
		db.execSQL("create table location ("
				+ "_id integer primary key autoincrement," + "latitude real,"
				+ "longitude real," + "altitude real," + "speed real,"
				+ "provider varchar(100)," + "timestamp integer,"
				+ "series_mov integer" + ");");

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	public long insertLocation(Location location, long series_mov) {
		ContentValues cv = new ContentValues();
		cv.put(Constants.COLUMN_LOCATION_LATITUDE, location.getLatitude());
		cv.put(Constants.COLUMN_LOCATION_LONGITUDE, location.getLongitude());
		cv.put(Constants.COLUMN_LOCATION_ALTITUDE, location.getAltitude());
		cv.put(Constants.COLUMN_LOCATION_SPEED, location.getSpeed());
		cv.put(Constants.COLUMN_LOCATION_PROVIDER, location.getProvider());
		cv.put(Constants.COLUMN_LOCATION_TIMESTAMP, location.getTime());
		cv.put(Constants.COLUMN_LOCATION_SERIES_MOV, series_mov);

		return getWritableDatabase().insert(Constants.TABLE_NAME_LOCATION, null,
				cv);
	}

	public int updatedbSeries(long md, float area, float routing,
			String nameSeries) {

		DecimalFormat royting = new DecimalFormat("0.0");
		DecimalFormat areaplow = new DecimalFormat("0.00000");

		ContentValues cv = new ContentValues();
		cv.put(Constants.COLUMN_SERIES_ROUTING,
				String.valueOf(royting.format(routing) + " " + "м"));
		cv.put(Constants.COLUMN_SERIES_AREAPLOWED,
				String.valueOf(areaplow.format(area) + " " + "га"));
		cv.put(Constants.COLUMN_SERIES_NAMESERIES, nameSeries);

		return getWritableDatabase().update(Constants.TABLE_NAME_SERIES, cv,
				Constants.COLUMN_SERIES_ID + " = ?",
				new String[] { String.valueOf(md) });
	}

	public long insertSeries(Location location) {
		ContentValues cv = new ContentValues();
		cv.put(Constants.COLUMN_LOCATION_TIMESTAMP,
				String.valueOf(createDate(location.getTime())));

		return getWritableDatabase().insert(Constants.TABLE_NAME_SERIES, null,
				cv);

	}

	public void cleardata() {

		getMyWritableDatabase().execSQL("delete from " + "location");
		getMyWritableDatabase().execSQL("delete from " + "series");
		getMyWritableDatabase().execSQL("delete from " + "sqlite_sequence");

	}

	public Cursor getAllDataSeries() {

		Cursor cv = getMyWritableDatabase().query(Constants.TABLE_NAME_SERIES,
				null, null, null, null, null, null);

		return cv;

	}

	public static CharSequence createDate(long timestamp) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(timestamp);
		Date d = c.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
		return sdf.format(d);
	}

}