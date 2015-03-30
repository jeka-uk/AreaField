package com.example.areafield.fragment;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.example.areafield.Constant;
import com.example.areafield.R;
import com.example.areafield.dbHelper.DatabaseHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
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
import android.location.Criteria;
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
			run_speedTextView, run_altitudeTextView, run_durationTextView,
			textView1;
	private Button run_startButton, run_stopButton;
	private SupportMapFragment mapFragment;
	private GoogleMap mGoogleMap;
	private double routingTwo, routing;
	private double startLati, startlongi, startLatiDB, startlongiBD;

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
		run_durationTextView = (TextView) view
				.findViewById(R.id.run_durationTextView);
		run_startButton = (Button) view.findViewById(R.id.run_startButton);
		run_stopButton = (Button) view.findViewById(R.id.run_stopButton);
		run_stopButton.setEnabled(false);
		textView1 = (TextView) view.findViewById(R.id.textView1);

		mLocationManager = (LocationManager) getActivity().getSystemService(
				Context.LOCATION_SERVICE);

		run_startButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mLocationManager
						.requestLocationUpdates(LocationManager.GPS_PROVIDER,
								1000, 0, locationListener);
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

					if (startLatiDB == 0 && startlongiBD == 0) {
						startLatiDB = latLng.latitude;
						startlongiBD = latLng.longitude;
					}

					LatLng prev = new LatLng(startLatiDB, startlongiBD);
					LatLng my = new LatLng(latLng.latitude, latLng.longitude);

					Polyline line = mGoogleMap
							.addPolyline(new PolylineOptions().add(prev, my)
									.width(8).color(Color.GREEN));

					Location mylocation = new Location(" ");
					Location dest_location = new Location(" ");
					dest_location.setLatitude(prev.latitude);
					dest_location.setLongitude(prev.longitude);
					mylocation.setLatitude(my.latitude);
					mylocation.setLongitude(my.longitude);
					double distanceNew = mylocation.distanceTo(dest_location);

					startLatiDB = latLng.latitude;
					startlongiBD = latLng.longitude;

					routing = distanceNew + routing;
					run_altitudeTextView.setText(String.valueOf(routing));

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
		// run_altitudeTextView.setText(Double.toString(location.getAltitude()));
		run_durationTextView.setText(Double.toString(location.getAccuracy()));

		movingCamera(location);

		if (location.getSpeed() > 0 && location.getAccuracy() <= 8) {

			drawmap(location.getLatitude(), location.getLongitude());

			dh.insertLocation(location);
			dh.close();
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

	public void polyline(LatLng mLatLngStart, LatLng mLatLngFinish) {

		Polyline line = mGoogleMap.addPolyline(new PolylineOptions()
				.add(mLatLngStart, mLatLngFinish).width(10).color(Color.RED));
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
						.getLongitude())).zoom(13).bearing(90).tilt(30).build();
		mGoogleMap.animateCamera(CameraUpdateFactory
				.newCameraPosition(cameraPosition));
	}

	public void drawmap(double latid, double longid) {

		if (startLati == 0 && startlongi == 0) {
			startLati = latid;
			startlongi = longid;
		}

		LatLng prev = new LatLng(startLati, startlongi);
		LatLng my = new LatLng(latid, longid);

		Polyline line = mGoogleMap.addPolyline(new PolylineOptions()
				.add(prev, my).width(12).color(Color.BLUE));

		Location mylocation = new Location(" ");
		Location dest_location = new Location(" ");
		dest_location.setLatitude(prev.latitude);
		dest_location.setLongitude(prev.longitude);
		mylocation.setLatitude(my.latitude);
		mylocation.setLongitude(my.longitude);
		double distanceNew = mylocation.distanceTo(dest_location);

		startLati = latid;
		startlongi = longid;

		routingTwo = distanceNew + routingTwo;
		textView1.setText(String.valueOf(routingTwo));

	}

}