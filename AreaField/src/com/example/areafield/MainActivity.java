package com.example.areafield;

import com.example.areafield.fragment.ListFragment;


import android.support.v4.app.Fragment;


public class MainActivity extends SingleFragmentActivity {
	
		
	protected Fragment createFragment() {
		return new ListFragment();
	}
}
