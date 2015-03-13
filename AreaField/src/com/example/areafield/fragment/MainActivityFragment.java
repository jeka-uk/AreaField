package com.example.areafield.fragment;

import com.example.areafield.Constant;
import com.example.areafield.R;
import com.example.areafield.dbHelper.DatabaseHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Telephony.TextBasedSmsColumns;
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
			run_speedTextView, run_altitudeTextView, textView1;
	private Button run_startButton, run_stopButton;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_main, container, false);

		DatabaseHelper dh = DatabaseHelper.getInstance(getActivity());
		dh.cleardata();
		dh.close();

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

		textView1 = (TextView) view.findViewById(R.id.textView1);

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

			private int latitudeIndex, longitudeIndex;

			@Override
			public void onClick(View v) {

				mLocationManager.removeUpdates(locationListener);
				run_startButton.setEnabled(true);
				run_stopButton.setEnabled(false);

				DatabaseHelper dh = DatabaseHelper.getInstance(getActivity());
				
				int endID;
				
				endID = dh.getEndId(Constant._ID);
						
										
				for (int i = 1; i <= endID; i++){
				
				Log.d(LOG_TAG,
						"latitude - "
								+ Double.toString(dh.getLocation(
										Constant.LATITUDE,
										Constant.COLUMN_LOCATION_LATITUDE, i)));
				

				Log.d(LOG_TAG,
						"longitude - "
								+ Double.toString(dh.getLocation(
										Constant.LONGITUDE,
										Constant.COLUMN_LOCATION_LONGITUDE, i)));
			

				
				
				dh.close();
				}
				
				textView1.setText(Double.toString(distance(50.487994, 30.227913, 46.652156, 32.864631, "K")));

		

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

		DatabaseHelper dh = DatabaseHelper.getInstance(getActivity());

		run_latitudeTextView.setText(Double.toString(location.getLatitude()));
		run_longitudeTextView.setText(Double.toString(location.getLongitude()));
		run_speedTextView.setText(Double.toString((location.getSpeed() * 3.6)));
		run_altitudeTextView.setText(Double.toString(location.getAltitude()));

		dh.insertLocation(location);
		dh.close();

	}

	private double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}

	private double rad2deg(double rad) {
		return (rad * 180 / Math.PI);
	}

	private double distance(double startLat1, double startLon1,
			double finishLat2, double finishLon2, String unit) {
		double theta = startLon1 - finishLon2;
		double dist = Math.sin(deg2rad(startLat1))
				* Math.sin(deg2rad(finishLat2)) + Math.cos(deg2rad(startLat1))
				* Math.cos(deg2rad(finishLat2)) * Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
		if (unit == "K") {
			dist = dist * 1.609344;
		} else if (unit == "N") {
			dist = dist * 0.8684;
		}
		return (dist);
	}

	
}