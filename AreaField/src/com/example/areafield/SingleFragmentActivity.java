package com.example.areafield;

import com.example.areafield.fragment.MainActivityFragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

public abstract class SingleFragmentActivity extends FragmentActivity {
	protected abstract Fragment createFragment();

	@Override
	public void onBackPressed() {
		FragmentManager fm = getSupportFragmentManager();
		OnBackPressedListener backPressedListener = null;
		for (Fragment fragment : fm.getFragments()) {
			if (fragment instanceof OnBackPressedListener) {
				backPressedListener = (OnBackPressedListener) fragment;
				break;
			}
		}

		if (backPressedListener != null) {
			backPressedListener.onBackPressed();
		} else {
			super.onBackPressed();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		FragmentManager fm = getSupportFragmentManager();
		Fragment fragment = fm.findFragmentById(R.id.container);

		if (fragment == null) {
			fragment = createFragment();
			fm.beginTransaction().add(R.id.container, fragment).commit();
		}

	}

}
