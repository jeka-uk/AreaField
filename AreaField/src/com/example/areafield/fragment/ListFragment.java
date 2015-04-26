package com.example.areafield.fragment;

import com.example.areafield.Constant;
import com.example.areafield.R;
import com.example.areafield.dbHelper.DatabaseHelper;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

public class ListFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_list, container, false);

		ListView myListView = (ListView) view.findViewById(R.id.listViewSeries);
		Button startlist = (Button) view.findViewById(R.id.startlistfragment);
	 // Button finishlist = (Button) view.findViewById(R.id.finishlistfragment);

		
		  /*DatabaseHelper dh = DatabaseHelper.getInstance(getActivity());
		  dh.cleardata(); dh.close();*/
		 

		viewAllDataSeries(myListView);

		startlist.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				MainActivityFragment mySecondFragment = new MainActivityFragment();
				getFragmentManager().beginTransaction()
						.replace(R.id.container, mySecondFragment)
						.addToBackStack("myBackStack").commit();

			}
		});

		return view;
	}

	private void viewAllDataSeries(ListView myListView) {

		DatabaseHelper dh = DatabaseHelper.getInstance(getActivity());

		Cursor cv = dh.getAllDataSeries();

		String[] fromFielsName = new String[] { Constant.COLUMN_SERIES_TIMESTAMP, Constant.COLUMN_SERIES_NAMESERIES, Constant.COLUMN_SERIES_ROUTING, Constant.COLUMN_SERIES_AREAPLOWED, Constant.COLUMN_SERIES_ID};
		int[] toViewIds = new int[] { R.id.timestemp, R.id.title, R.id.routing, R.id.area };
		SimpleCursorAdapter mySimpleCursorAdapter;
		mySimpleCursorAdapter = new SimpleCursorAdapter(getActivity()
				.getBaseContext(), R.layout.item_layout, cv,
				fromFielsName, toViewIds, 0);
		myListView.setAdapter(mySimpleCursorAdapter);

	}

}
