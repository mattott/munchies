package com.ottmatt.munchies;

import roboguice.inject.InjectView;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.widget.Button;
import android.widget.SearchView;

public class MainActivity extends Activity {
	@InjectView(R.id.search)
	SearchView searchView;
	@InjectView(R.id.gps_enabler)
	Button gpsButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (gpsButton == null)
			gpsButton = new Button(this, null);
		if (searchView == null)
			searchView = new SearchView(this);
		final PlacesListFragment places = new PlacesListFragment();
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.add(0, places);
		ft.commit();
	}
}
