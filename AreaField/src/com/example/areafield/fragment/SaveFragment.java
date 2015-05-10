package com.example.areafield.fragment;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import com.example.areafield.R;
import com.example.areafield.dbHelper.DatabaseHelper;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SaveFragment extends Fragment {

	private float mDistanceTraveled, mAreaPlow;
	private long mSeriesMov;
	private Button saveSeries;
	private EditText inputNameSeries;

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

				saveDataSeries();

			}
		});

		return view;

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
			getActivity().onBackPressed();
			/*ListFragment mySecondFragment = new ListFragment();
			getFragmentManager().beginTransaction()
					.replace(R.id.container, mySecondFragment).commit();*/

		}

	}
}
