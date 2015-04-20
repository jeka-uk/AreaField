package com.example.areafield.fragment;

import com.example.areafield.Constant;
import com.example.areafield.R;
import com.example.areafield.dbHelper.DatabaseHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

import android.R.bool;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivityFragment extends Fragment {

	private final String LOG_TAG = "myLogs";

	private LocationManager mLocationManager;
	private TextView run_latitudeTextView, run_longitudeTextView,
			run_speedTextView, run_altitudeTextView, run_durationTextView,
			routingTextView;
	private Button run_startButton, run_stopButton;
	private SupportMapFragment mapFragment;
	private GoogleMap mGoogleMap;
	private Location previousLocation = null, myLocation = null;
	private WakeLock wakeLock;
	private boolean gpsFix, firstLocation;

	private long series_mov = 0;

	private Handler customHandler = new Handler();
	private long timeInMilliseconds = 0L, timeSwapBuff = 0L, updatedTime = 0L,
			startTime = 0L;
	private float distanceTraveled = 0;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_main, container, false);

		PowerManager pm = (PowerManager) getActivity().getSystemService(
				Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My wakeloo");

		mapFragment = (SupportMapFragment) (getActivity()
				.getSupportFragmentManager()).findFragmentById(R.id.map);
		mGoogleMap = mapFragment.getMap();

		if (mGoogleMap == null) {

			getActivity().finish();
		}

		starGoogleMap();

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
		run_durationTextView = (TextView) view
				.findViewById(R.id.run_durationTextView);
		run_startButton = (Button) view.findViewById(R.id.run_startButton);
		run_stopButton = (Button) view.findViewById(R.id.run_stopButton);
		run_stopButton.setEnabled(false);
		routingTextView = (TextView) view.findViewById(R.id.routingTextView);

		mLocationManager = (LocationManager) getActivity().getSystemService(
				Context.LOCATION_SERVICE);

		run_startButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				mLocationManager.requestLocationUpdates(
						LocationManager.GPS_PROVIDER, 1, 0, locationListener);

				run_stopButton.setEnabled(true);
				run_startButton.setEnabled(false);
				gpsFix = true;
				firstLocation = true;

				wakeLock.acquire();

			}
		});
		run_stopButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				mLocationManager.removeUpdates(locationListener);
				run_startButton.setEnabled(true);
				run_stopButton.setEnabled(false);

				previousLocation = null;
				distanceTraveled = 0;

				// pause timer
				timeSwapBuff += timeInMilliseconds;
				customHandler.removeCallbacks(updateTimerThread);

				timeInMilliseconds = 0L;
				timeSwapBuff = 0L;
				updatedTime = 0L;
				startTime = 0L;
				
				firstLocation = false;

				wakeLock.release();

				DatabaseHelper dh = DatabaseHelper.getInstance(getActivity());

				Cursor cv = dh.getMyWritableDatabase().query(
						Constant.TABLE_NAME_LOCATION, null, null, null, null,
						null, null);
				cv.moveToFirst();

				while (cv.isAfterLast() == false) {

					Location locationDB = new Location("location from db");
					locationDB.setLatitude(cv.getDouble(cv
							.getColumnIndex(Constant.COLUMN_LOCATION_LATITUDE)));
					locationDB.setLongitude(cv.getDouble(cv
							.getColumnIndex(Constant.COLUMN_LOCATION_LONGITUDE)));

					// drawCalculateRouting(locationDB, null, null);

					cv.moveToNext();
				}

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

		movingCamera(location);

		if (location.getSpeed() >  0 && location.getAccuracy() <= 8) {

			if (gpsFix == true) {

				// start timer
				startTime = SystemClock.uptimeMillis();
				customHandler.postDelayed(updateTimerThread, 0);

				gpsFix = false;
			}

			drawCalculateRouting(location, routingTextView, "draw");

			dh.insertLocation(location, series_mov);
			dh.insertRun(location);
			dh.close();

		} else {

			if (gpsFix == false) {

				// pause timer
				timeSwapBuff += timeInMilliseconds;
				customHandler.removeCallbacks(updateTimerThread);

				gpsFix = true;
			}

		}

	}

	public void addMarkerStartFinish(LatLng mLatLng, String choice) {

		MarkerOptions markerOptions = new MarkerOptions();
		markerOptions.position(mLatLng);

		if (choice == "start") {

			markerOptions.icon(BitmapDescriptorFactory
					.fromResource(R.drawable.st_marker));
		} else {
			markerOptions.icon(BitmapDescriptorFactory
					.fromResource(R.drawable.fi_marker));
		}

		mGoogleMap.addMarker(markerOptions);
	}

	public void starGoogleMap() {

		UiSettings uiSettings = mGoogleMap.getUiSettings();
		uiSettings.setZoomControlsEnabled(true);
		mGoogleMap.setMyLocationEnabled(true);
		CameraPosition cameraPosition = new CameraPosition.Builder()
				.target(new LatLng(48.761043, 30.230563)).zoom(3).build();
		mGoogleMap.moveCamera(CameraUpdateFactory
				.newCameraPosition(cameraPosition));
	}

	public void movingCamera(Location location) {

		CameraPosition cameraPosition = new CameraPosition.Builder()
				.target(new LatLng(location.getLatitude(), location
						.getLongitude())).zoom(13).bearing(90).build();
		mGoogleMap.animateCamera(CameraUpdateFactory
				.newCameraPosition(cameraPosition));
	}

	public void drawCalculateRouting(Location location, TextView textView,
			String choice) {

		// Log.d(LOG_TAG, "Location " + location);
		
		if (firstLocation == true){
			
			LatLng startLocation = new LatLng(location.getLatitude(), location.getLongitude());
			Marker melbourne = mGoogleMap.addMarker(new MarkerOptions().position(startLocation).icon(BitmapDescriptorFactory
					.fromResource(R.drawable.testmarker)));	
			
			firstLocation = false;
			
		}
		
		if (previousLocation != null) {

			double lat1 = location.getLatitude();
			double lon1 = location.getLongitude();
			double lat2 = previousLocation.getLatitude();
			double lon2 = previousLocation.getLongitude();
			double R = 6371; // km
			double dLat = (lat2 - lat1) * Math.PI / 180;
			double dLon = (lon2 - lon1) * Math.PI / 180;
			lat1 = lat1 * Math.PI / 180;
			lat2 = lat2 * Math.PI / 180;

			double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
					+ Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1)
					* Math.cos(lat2);
			double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
			double d = R * c * 1000;

			distanceTraveled += d;

			if (choice == "draw") {

				PolygonOptions polygoneOptions = new PolygonOptions()
						.add((new LatLng(previousLocation.getLatitude(),
								previousLocation.getLongitude())),
								(new LatLng(location.getLatitude(), location
										.getLongitude())))
						.strokeColor(Color.RED).strokeWidth(10);
				mGoogleMap.addPolygon(polygoneOptions);

			} else {

			}

		}

		previousLocation = location;

		textView.setText(String.valueOf(distanceTraveled + " m"));

	}

	private Runnable updateTimerThread = new Runnable() {

		public void run() {

			timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
			updatedTime = timeSwapBuff + timeInMilliseconds;

			int secs = (int) (updatedTime / 1000);
			int mins = secs / 60;
			int hours = secs / 3600;
			secs = secs % 60;

			// int milliseconds = (int) (updatedTime % 1000);

			run_durationTextView.setText("" + hours + ":"
					+ String.format("%02d", mins) + ":"
					+ String.format("%02d", secs));

			customHandler.postDelayed(this, 0);

		}

	};

}
