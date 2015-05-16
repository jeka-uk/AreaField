package com.example.areafield.fragment;

import java.text.DecimalFormat;

import com.example.areafield.Constant;
import com.example.areafield.Calculation;
import com.example.areafield.OnBackPressedListener;
import com.example.areafield.R;
import com.example.areafield.dbHelper.DatabaseHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.sip.SipRegistrationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivityFragment extends Fragment implements OnBackPressedListener {

	// private final String LOG_TAG = "myLogs";

	private LocationManager mLocationManager;
	private TextView run_latitudeTextView, run_longitudeTextView,
			run_speedTextView, run_durationTextView, routingTextView,
			areaplowed;

	private EditText widthPlow;
	private Button run_startButton, run_stopButton;
	private SupportMapFragment mapFragment;
	private GoogleMap mGoogleMap;
	private Location previousLocation = null, secondLocation = null;
	private WakeLock wakeLock;
	private boolean gpsFix, firstLocation, writedata;

	private int widthplow = 0;

	private Handler customHandler = new Handler();
	private long timeInMilliseconds = 0L, timeSwapBuff = 0L, updatedTime = 0L,
			startTime = 0L, series_mov = 0;
	private float distanceTraveled = 0, areaplow = 0;

	private DecimalFormat dec = new DecimalFormat("0.0");
	private DecimalFormat decSecond = new DecimalFormat("0.00000");
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_main, container, false);
			
		starGoogleMap();
	
		PowerManager pm = (PowerManager) getActivity().getSystemService(
				Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My wakeloo");

		/*
		 * DatabaseHelper.getInstance(getActivity()).cleardata();
		 * DatabaseHelper.getInstance(getActivity()).close();
		 */

		run_latitudeTextView = (TextView) view
				.findViewById(R.id.run_latitudeTextView);
		run_longitudeTextView = (TextView) view
				.findViewById(R.id.run_longitudeTextView);
		run_speedTextView = (TextView) view
				.findViewById(R.id.run_speedTextView);
		areaplowed = (TextView) view.findViewById(R.id.areaplowed);
		widthPlow = (EditText) view.findViewById(R.id.widthPlow);
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
				
				
			/*	InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);*/

				if (widthPlow.getText().length() == 0) {

					Toast toast = Toast.makeText(getActivity()
							.getApplicationContext(),
							getString(R.string.tost_width_plow),
							Toast.LENGTH_SHORT);
					toast.show();

				} else {

					mLocationManager.requestLocationUpdates(
							LocationManager.GPS_PROVIDER, 1, 0,
							locationListener);

					run_stopButton.setEnabled(true);
					run_startButton.setEnabled(false);
					gpsFix = true;
					firstLocation = true;
					widthPlow.setEnabled(false);

					widthplow = Integer
							.parseInt(widthPlow.getText().toString());

					wakeLock.acquire();

				}

			}
		});
		run_stopButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				mLocationManager.removeUpdates(locationListener);
				run_startButton.setEnabled(true);
				run_stopButton.setEnabled(false);
				widthPlow.setEnabled(true);
				writedata = false;

				previousLocation = null;
				// distanceTraveled = 0;

				// pause timer
				timeSwapBuff += timeInMilliseconds;
				customHandler.removeCallbacks(updateTimerThread);

				timeInMilliseconds = 0L;
				timeSwapBuff = 0L;
				updatedTime = 0L;
				startTime = 0L;

				firstLocation = false;

				wakeLock.release();

				SaveFragment mySecondFragment = new SaveFragment(series_mov,
						distanceTraveled, areaplow);
				getFragmentManager().beginTransaction()
						.replace(R.id.container, mySecondFragment)
						.addToBackStack("myBackStack").commit();

				Cursor cv = DatabaseHelper
						.getInstance(getActivity())
						.getMyWritableDatabase()
						.query(Constant.TABLE_NAME_LOCATION, null, null, null,
								null, null, null);
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

		run_latitudeTextView.setText(Double.toString(location.getLatitude()));
		run_longitudeTextView.setText(Double.toString(location.getLongitude()));
		run_speedTextView.setText(dec.format(location.getSpeed() * 3.6) + " "
				+ getString(R.string.size_spedd));

		movingCamera(location);

		if (location.getSpeed() > 0 && location.getAccuracy() <= 8) {

			if (gpsFix == true) {
				// start timer
				startTime = SystemClock.uptimeMillis();
				customHandler.postDelayed(updateTimerThread, 0);
				gpsFix = false;
			}

			drawCalculateRouting(location, routingTextView, areaplowed, "draw");
			writeLocationToDB(location);

		} else {

			if (gpsFix == false) {
				// pause timer
				timeSwapBuff += timeInMilliseconds;
				customHandler.removeCallbacks(updateTimerThread);
				gpsFix = true;
			}
		}
	}

	public void starGoogleMap() {

		mapFragment = new SupportMapFragment() {

			@Override
			public void onActivityCreated(Bundle savedInstanceState) {
				super.onActivityCreated(savedInstanceState);
				mGoogleMap = mapFragment.getMap();
				if (mGoogleMap != null) {

					UiSettings uiSettings = mGoogleMap.getUiSettings();
					uiSettings.setZoomControlsEnabled(true);
					mGoogleMap.setMyLocationEnabled(true);
					CameraPosition cameraPosition = new CameraPosition.Builder()
							.target(new LatLng(48.761043, 30.230563)).zoom(3)
							.build();
					mGoogleMap.moveCamera(CameraUpdateFactory
							.newCameraPosition(cameraPosition));
				}
			}
		};
		
				
		FragmentTransaction transaction = getChildFragmentManager()
				.beginTransaction();
		transaction.replace(R.id.map, mapFragment).addToBackStack(null).commit();
	}

	public void movingCamera(Location location) {

		CameraPosition position = CameraPosition
				.builder()
				.bearing(location.getBearing())
				.target(new LatLng(location.getLatitude(), location
						.getLongitude()))
				.zoom(mGoogleMap.getCameraPosition().zoom)
				.tilt(mGoogleMap.getCameraPosition().tilt).build();
		mGoogleMap.animateCamera(CameraUpdateFactory
				.newCameraPosition(position));
	}

	public void drawCalculateRouting(Location location,
			TextView distanceTextView, TextView areaplowedTextView,
			String choice) {

		// Log.d(LOG_TAG, "Location " + location);

		if (firstLocation == true) {

			LatLng startLocation = new LatLng(location.getLatitude(),
					location.getLongitude());
			Marker melbourne = mGoogleMap.addMarker(new MarkerOptions()
					.position(startLocation).icon(
							BitmapDescriptorFactory
									.fromResource(R.drawable.testmarker)));
			firstLocation = false;
		}

		if (previousLocation != null) {

			Calculation mCoordinates = new Calculation();

			distanceTraveled += mCoordinates
					.routing(location, previousLocation);

			areaplow += (mCoordinates.routing(location, previousLocation) * widthplow) / 10000;

			if (choice == "draw") {

				PolygonOptions polygoneOptions = new PolygonOptions()
						.add((new LatLng(previousLocation.getLatitude(),
								previousLocation.getLongitude())),
								(new LatLng(location.getLatitude(), location
										.getLongitude())))
						.strokeColor(Color.RED).strokeWidth(10);
				mGoogleMap.addPolygon(polygoneOptions);

				drawSecondLine(location, widthplow);

			} else {

			}

		}

		previousLocation = location;

		distanceTextView.setText(dec.format(distanceTraveled) + " "
				+ getString(R.string.size_m));

		areaplowedTextView.setText(decSecond.format(areaplow) + " "
				+ getString(R.string.size_g));

	}

	private Runnable updateTimerThread = new Runnable() {

		public void run() {

			timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
			updatedTime = timeSwapBuff + timeInMilliseconds;

			int secs = (int) (updatedTime / 1000);
			int mins = secs / 60;
			int hours = secs / 3600;
			secs = secs % 60;

			run_durationTextView.setText("" + hours + ":"
					+ String.format("%02d", mins) + ":"
					+ String.format("%02d", secs));

			customHandler.postDelayed(this, 0);

		}

	};

	private void writeLocationToDB(Location location) {

		if (writedata == false) {

			DatabaseHelper.getInstance(getActivity()).insertSeries(location);
			DatabaseHelper.getInstance(getActivity()).close();

			Cursor cv = DatabaseHelper
					.getInstance(getActivity())
					.getMyWritableDatabase()
					.query(Constant.TABLE_NAME_SERIES, null, null, null, null,
							null, null);
			cv.moveToLast();

			series_mov = cv.getLong(cv
					.getColumnIndex(Constant.COLUMN_SERIES_ID));

			writedata = true;

		}

		DatabaseHelper.getInstance(getActivity()).insertLocation(location,
				series_mov);
		DatabaseHelper.getInstance(getActivity()).close();

	}

	private void drawSecondLine(Location location, double radius) {

		if (secondLocation != null) {

			Calculation mCoordinates = new Calculation();
			PolygonOptions polygoneOptions = new PolygonOptions()
					.add((mCoordinates.coordinatesSecondLine(location, radius)),
							(mCoordinates.coordinatesSecondLine(secondLocation,
									radius))).strokeColor(Color.BLUE)
					.strokeWidth(5);
			mGoogleMap.addPolygon(polygoneOptions);

		}

		secondLocation = location;

	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		
		//getActivity().finish();
		mLocationManager.removeUpdates(locationListener);
		
		ListFragment mySecondFragment = new ListFragment();
		getFragmentManager().beginTransaction()
				.replace(R.id.container, mySecondFragment).commit();
		
	}
	
	 
	
}
