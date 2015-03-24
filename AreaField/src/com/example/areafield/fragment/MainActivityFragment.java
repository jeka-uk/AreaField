package com.example.areafield.fragment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import com.example.areafield.Constant;
import com.example.areafield.R;
import com.example.areafield.dbHelper.DatabaseHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.api.Api.c;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.internal.mf;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Telephony.TextBasedSmsColumns;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
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

	private SupportMapFragment mapFragment;
	private GoogleMap mGoogleMap;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_main, container, false);

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

			@Override
			public void onClick(View v) {

				mLocationManager.removeUpdates(locationListener);
				run_startButton.setEnabled(true);
				run_stopButton.setEnabled(false);

				DatabaseHelper dh = DatabaseHelper.getInstance(getActivity());

				double routing = 0;

				ArrayList<LatLng> latitLngit = new ArrayList<LatLng>();

				Cursor cv = dh.getMyWritableDatabase()
						.query(Constant.TABLE_NAME, null, null, null, null,
								null, null);

				cv.moveToFirst();

				while (cv.isAfterLast() == false) {

					LatLng latLng = new LatLng(
							(cv.getDouble(cv
									.getColumnIndex(Constant.COLUMN_LOCATION_LATITUDE))),
							(cv.getDouble(cv
									.getColumnIndex(Constant.COLUMN_LOCATION_LONGITUDE))));

					latitLngit.add(latLng);

					cv.moveToNext();
				}

				for (int index = 0; index < (latitLngit.size()) - 1; index++) {
					routing = (distance((latitLngit.get(index).latitude),
							(latitLngit.get(index).longitude),
							(latitLngit.get(index + 1).latitude),
							(latitLngit.get(index + 1).longitude)))
							+ routing;

					textView1.setText(String.valueOf(routing));

					addMarkerStartFinish(latitLngit.get(0));

					addMarkerStartFinish(latitLngit.get((latitLngit.size()) - 1));

					polyline(latitLngit.get(index), latitLngit.get(index + 1));					
					
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
			double finishLat2, double finishLon2) {
		double theta = startLon1 - finishLon2;
		double dist = Math.sin(deg2rad(startLat1))
				* Math.sin(deg2rad(finishLat2)) + Math.cos(deg2rad(startLat1))
				* Math.cos(deg2rad(finishLat2)) * Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
		dist = (dist * 1.609344) * 1000; // metr
		return (dist);
	}

	public void addMarkerStartFinish(LatLng mLatLng) {

		MarkerOptions markerOptions = new MarkerOptions();
		markerOptions.position(mLatLng);
		markerOptions.icon(BitmapDescriptorFactory
				.fromResource(R.drawable.st_marker));
		mGoogleMap.addMarker(markerOptions);

	}

	public void polyline(LatLng mLatLngStart, LatLng mLatLngFinish) {

		Polyline line = mGoogleMap.addPolyline(new PolylineOptions()
				.add(mLatLngStart, mLatLngFinish).width(7).color(Color.BLUE));

	}

	public void starGoogleMap() {

		UiSettings uiSettings = mGoogleMap.getUiSettings();
		uiSettings.setZoomControlsEnabled(true);
		mGoogleMap.setMyLocationEnabled(true);
		
		CameraPosition cameraPosition = new CameraPosition.Builder()
	    .target(new LatLng(48.761043, 30.230563))
	    .zoom(3).build();            
	   
		mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
		
	}
	
	public void movingCamera(Location location){
		
		CameraPosition cameraPosition = new CameraPosition.Builder()
	    .target(new LatLng(location.getLatitude(),location.getLongitude()))
	    .zoom(17)                   // Sets the zoom
	    .bearing(90)                // Sets the orientation of the camera to east
	    .tilt(30)                   // Sets the tilt of the camera to 30 degrees
	    .build();                   // Creates a CameraPosition from the builder
		mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
		
	}

}