package com.example.areafield.fragment;

import com.example.areafield.Constants;
import com.example.areafield.R;
import com.example.areafield.dbHelper.DatabaseHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.DriveIdResult;
import com.google.android.gms.drive.DriveApi.MetadataBufferResult;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveFolder.DriveFolderResult;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.metadata.SearchableMetadataField;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import android.content.Context;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
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

		if (mGoogleApiClient == null) {
			mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
					.addApi(Drive.API).addScope(Drive.SCOPE_FILE)
					.addConnectionCallbacks(this)
					.addOnConnectionFailedListener(this).build();
		}
		mGoogleApiClient.connect();

	}

	@Override
	public void onResume() {
		super.onResume();

		if (mGoogleApiClient == null) {
		}
		mGoogleApiClient.connect();
	}

	@Override
	public void onStop() {
		super.onStop();
		if (mGoogleApiClient != null) {
			mGoogleApiClient.disconnect();
		}

	}

	private void saveDataSeries() {

		if (inputNameSeries.getText().length() == 0) {

			showMessage(getString(R.string.tost_nameSeries));

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

		showMessage("Connect failed");

		if (connectionResult.hasResolution()) {
			try {
				connectionResult.startResolutionForResult(getActivity(),
						Constants.RESOLVE_CONNECTION_REQUEST_CODE);
			} catch (IntentSender.SendIntentException e) {

			}
		} else {
			GooglePlayServicesUtil.getErrorDialog(
					connectionResult.getErrorCode(), getActivity(), 0).show();
			return;

		}
	}

	@Override
	public void onConnected(Bundle connectionHint) {

		showMessage("Connect to Goodle Drive");

		downUplodingToDrive();
	}

	public void onConnectionSuspended(int cause) {

	}

	public GoogleApiClient getGoogleApiClient() {
		return mGoogleApiClient;
	}

	public void showMessage(String message) {
		Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
	}

	private void downUplodingToDrive() {

		Query query = new Query.Builder().addFilter(
				Filters.eq(SearchableField.TITLE, Constants.NAME_FOLDER))
				.build();

		Drive.DriveApi.query(getGoogleApiClient(), query).setResultCallback(
				metadataCallback);

	}

	final private ResultCallback<MetadataBufferResult> metadataCallback = new ResultCallback<MetadataBufferResult>() {
		@Override
		public void onResult(MetadataBufferResult result) {
			if (!result.getStatus().isSuccess()) {
				showMessage("Problem while retrieving results");
				return;
			}

			if (result.getMetadataBuffer().getCount() > 0) {
				MetadataBuffer mb1 = result.getMetadataBuffer();
				DriveId rootDriveId = mb1.get(0).getDriveId();
				showMessage(String.valueOf(rootDriveId));
				// mb1.close();

			} else {

				// showMessage("no folder");

				MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
						.setTitle(Constants.NAME_FOLDER).build();
				Drive.DriveApi.getRootFolder(getGoogleApiClient())
						.createFolder(getGoogleApiClient(), changeSet)
						.setResultCallback(folderCreatedCallback);
			}
		}
	};

	ResultCallback<DriveFolderResult> folderCreatedCallback = new ResultCallback<DriveFolderResult>() {
		@Override
		public void onResult(DriveFolderResult result) {
			if (!result.getStatus().isSuccess()) {
				showMessage("Error while trying to create the folder");
				return;
			}
			showMessage("Created a folder: "
					+ result.getDriveFolder().getDriveId());
		}
	};

}