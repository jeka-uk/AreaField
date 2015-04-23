package com.example.areafield.fragment;

import com.example.areafield.Constant;
import com.example.areafield.R;
import com.example.areafield.dbHelper.DatabaseHelper;

import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class ListFragment extends Fragment {

	private TextView textView_id, textView_time_stemp;
	private ListView listViewSeries;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.listfragment, container, false);
		
		ListView myListView = (ListView) view.findViewById(R.id.listViewSeries);
		
		viewAllDataSeries(myListView);
		
	

		return view;
	}
	
	
	private void viewAllDataSeries(ListView myListView){
		
		DatabaseHelper dh = DatabaseHelper.getInstance(getActivity());

		Cursor cv = dh.getAllDataSeries();

			String[] fromFielsName = new String[]{Constant.COLUMN_SERIES_ID, Constant.COLUMN_SERIES_TIMESTAMP};
			int[] toViewIds = new int[]{R.id.rank, R.id.country};
			SimpleCursorAdapter mySimpleCursorAdapter;
			mySimpleCursorAdapter = new SimpleCursorAdapter(getActivity().getBaseContext(),R.layout.item_layout_for_listview, cv, fromFielsName, toViewIds, 0);
			myListView.setAdapter(mySimpleCursorAdapter);
		
	}
	
	
		
		
	

}
