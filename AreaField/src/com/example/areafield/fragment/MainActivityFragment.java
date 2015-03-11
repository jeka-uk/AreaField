package com.example.areafield.fragment;


import com.example.areafield.Adapter;
import com.example.areafield.R;
import com.example.areafield.dbHelper.DatabaseHelper;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class MainActivityFragment extends Fragment {
	
	final String LOG_TAG = "myLogs";

	private LocationManager mLocationManager;

	private TextView run_latitudeTextView, run_longitudeTextView,
			run_speedTextView, run_altitudeTextView;
	private Button run_startButton, run_stopButton;
	
	private DatabaseHelper mDatabaseHelper;
	private SQLiteDatabase db;
	private Adapter mAdapter = new Adapter();
	
	
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_main, container, false);
		
		mDatabaseHelper = new DatabaseHelper(getActivity());
		db = mDatabaseHelper.getWritableDatabase();
		mDatabaseHelper.cleardata(db);
		
		

		run_latitudeTextView = (TextView) view
				.findViewById(R.id.run_latitudeTextView);
		run_longitudeTextView = (TextView) view
				.findViewById(R.id.run_longitudeTextView);
		run_speedTextView = (TextView) view
				.findViewById(R.id.run_speedTextView);
		run_altitudeTextView = (TextView) view
				.findViewById(R.id.run_altitudeTextView);
		run_startButton = (Button) view.findViewById(R.id.run_startButton);
		run_stopButton = (Button) view.findViewById(R.id.run_stopButton);
		run_stopButton.setEnabled(false);
		
		

		mLocationManager = (LocationManager) getActivity().getSystemService(
				Context.LOCATION_SERVICE);

		run_startButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				mLocationManager.requestLocationUpdates(
						LocationManager.GPS_PROVIDER, 0, 0, locationListener);
				
				run_stopButton.setEnabled(true);
				run_startButton.setEnabled(false);

			}
		});

		run_stopButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				mLocationManager.removeUpdates(locationListener);
				run_startButton.setEnabled(true);
				run_stopButton.setEnabled(false);
				
				mDatabaseHelper.reaLocation(db);
				
				Log.d(LOG_TAG,
						"Main ID = " + String.valueOf(mAdapter.getMid()));
				
				
			}
		});

		return view;

	}

	private LocationListener locationListener = new LocationListener() {

		@Override
		public void onLocationChanged(Location location) {

			showLocation(location);

		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {

		}

		@Override
		public void onProviderEnabled(String provider) {

		}

		@Override
		public void onProviderDisabled(String provider) {

		}
	};

	private void showLocation(Location location) {

		if (location == null)
			return;

		run_latitudeTextView.setText(Double.toString(location.getLatitude()));
		run_longitudeTextView.setText(Double.toString(location.getLongitude()));
		run_speedTextView.setText(Double.toString((location.getSpeed()*3.6)));
		run_altitudeTextView.setText(Double.toString(location.getAltitude()));
		
		mDatabaseHelper.insertLocation(location);
			
		
	}

}