package com.ottmatt.munchies;

import java.util.List;

import roboguice.inject.InjectResource;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ottmatt.munchies.loaders.PlaceDetailsLoader;
import com.ottmatt.munchies.loaders.PlaceSearchLoader;
import com.ottmatt.munchies.parsers.PlaceSearchParser.Message;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class PlacesListFragment extends ListFragment implements
		LoaderManager.LoaderCallbacks<Object>, LocationListener {
	@InjectResource(R.string.no_results)
	String no_results;
	PlacesAdapter mAdapter;
	LocationManager mLocationManager;
	String mReference = null;
	String mUrl = "";

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setEmptyText(no_results);
		mAdapter = new PlacesAdapter(getActivity());
		setListAdapter(mAdapter);
		setListShown(false);
		mLocationManager = (LocationManager) getActivity().getSystemService(
				Context.LOCATION_SERVICE);
		mLocationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 1000, 0, this);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		mReference = null;
		Message item = (Message) getListView().getItemAtPosition(position);
		mReference = item.getReference();
		getLoaderManager().initLoader(1, null, this);
	}

	@Override
	public Loader onCreateLoader(int id, Bundle args) {
		if (id == 0) {
			PlaceSearchLoader sLoader = new PlaceSearchLoader(getActivity());
			sLoader.setLocation(mReference);
			return sLoader;
		} else {
			PlaceDetailsLoader dLoader = new PlaceDetailsLoader(getActivity());
			dLoader.setParams(mReference);
			return dLoader;
		}
	}

	@Override
	public void onLoadFinished(Loader loader, Object data) {
		if (loader.getId() == 1) {
			mUrl = (String) data;
			Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
					Uri.parse(mUrl));
			startActivity(intent);
			getLoaderManager().destroyLoader(1);
		} else {
			mAdapter.setData((List) data);
			if (isResumed())
				setListShown(true);
			else
				setListShownNoAnimation(true);
			mLocationManager.removeUpdates(this);
		}
	}

	@Override
	public void onLoaderReset(Loader loader) {
		if (loader.getId() == 0)
			mAdapter.setData(null);
	}

	public static class PlacesAdapter extends ArrayAdapter {
		private final LayoutInflater mInflater;

		public PlacesAdapter(Context context) {
			super(context, R.layout.places_list_view);
			mInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public void setData(List data) {
			clear();
			if (data != null)
				addAll(data);
		}

		// Populate new items in the list
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			if (convertView == null)
				view = mInflater.inflate(R.layout.places_list_view, parent,
						false);
			else
				view = convertView;
			Message item = (Message) getItem(position);
			((TextView) view.findViewById(R.id.rating)).setText(Double
					.toString(item.getRating()));
			((TextView) view.findViewById(R.id.places_name)).setText(item
					.getStore());
			((TextView) view.findViewById(R.id.vicinity)).setText(item
					.getVicinity());

			return view;
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		mReference = location.getLatitude() + "," + location.getLongitude();
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

}
