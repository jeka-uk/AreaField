package com.example.areafield.fragment;

import com.example.areafield.Constant;
import com.example.areafield.R;
import com.example.areafield.dbHelper.DatabaseHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;

import android.content.Context;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SaveFragment extends Fragment implements ConnectionCallbacks,
		OnConnectionFailedListener {

	protected static final String TAG = null;
	private float mDistanceTraveled, mAreaPlow;
	private long mSeriesMov;
	private Button saveSeries;
	private EditText inputNameSeries;

	private GoogleApiClient mGoogleApiClient;

	public SaveFragment(long mSeriesMov, float mDistanceTraveled,
			float mAreaPlow) {
		super();
		this.mDistanceTraveled = mDistanceTraveled;
		this.mAreaPlow = mAreaPlow;
		this.mSeriesMov = mSeriesMov;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_save_series, container,
				false);

		saveSeries = (Button) view.findViewById(R.id.saveSeries);
		inputNameSeries = (EditText) view.findViewById(R.id.inputNameSeries);

		mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
				.addApi(Drive.API).addScope(Drive.SCOPE_FILE)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this).build();

		saveSeries.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				InputMethodManager inputMethodManager = (InputMethodManager) getActivity()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				inputMethodManager.hideSoftInputFromWindow(getActivity()
						.getCurrentFocus().getWindowToken(),
						InputMethodManager.HIDE_NOT_ALWAYS);

				saveDataSeries();

			}
		});

		return view;

	}

	@Override
	public void onStart() {
		super.onStart();

		mGoogleApiClient.connect();
	}

	@Override
	public void onStop() {
		super.onStop();

		mGoogleApiClient.disconnect();
	}

	private void saveDataSeries() {

		if (inputNameSeries.getText().length() == 0) {

			Toast toast = Toast.makeText(getActivity().getApplicationContext(),
					getString(R.string.tost_nameSeries), Toast.LENGTH_SHORT);
			toast.show();

		} else {

			DatabaseHelper.getInstance(getActivity()).updatedbSeries(
					mSeriesMov, mAreaPlow, mDistanceTraveled,
					inputNameSeries.getText().toString());

			FragmentManager manager = getFragmentManager();
			if (manager.getBackStackEntryCount() > 0) {
				FragmentManager.BackStackEntry first = manager
						.getBackStackEntryAt(0);
				manager.popBackStack(first.getId(),
						FragmentManager.POP_BACK_STACK_INCLUSIVE);
			}

		}

	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {

		if (connectionResult.hasResolution()) {
			try {
				connectionResult.startResolutionForResult(getActivity(),
						Constant.RESOLVE_CONNECTION_REQUEST_CODE);
			} catch (IntentSender.SendIntentException e) {

			}
		} else {
			GooglePlayServicesUtil.getErrorDialog(
					connectionResult.getErrorCode(), getActivity(), 0).show();
		}

	}

	@Override
	public void onConnected(Bundle connectionHint) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnectionSuspended(int cause) {
		// TODO Auto-generated method stub

	}

}
