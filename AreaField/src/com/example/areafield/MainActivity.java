package com.example.areafield;

import com.example.areafield.fragment.MainActivityFragment;

import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends SingleFragmentActivity {
	protected Fragment createFragment() {
		return new MainActivityFragment();
	}
}
