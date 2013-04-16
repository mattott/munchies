package com.ottmatt.munchies.loaders;

import java.io.IOException;

import com.ottmatt.munchies.parsers.PlaceDetailsParser;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;

public class PlaceDetailsLoader extends AsyncTaskLoader<String> {
	String mUrl;
	String mResource;

	final InterestingConfigChanges mLastConfig = new InterestingConfigChanges();

	public PlaceDetailsLoader(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public void setParams(String input) {
		mResource = input;
	}

	/**
	 * This is where the bulk of our work is done. This function is called in a
	 * background thread and should generate a new set of data to be published
	 * by the loader.
	 */
	@Override
	public String loadInBackground() {
		PlaceDetailsParser dParser = new PlaceDetailsParser();
		try {
			String url = dParser.retrieveStream(mResource);
			return url;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Called when there is new data to deliver to the client. The super class
	 * will take care of delivering it; the implementation here just adds a
	 * little more logic.
	 */
	@Override
	public void deliverResult(String url) {
		if (isReset()) {
			// An async query came in while the loader is stopped. We
			// don't need the result.
			if (url != null) {
				onReleaseResources(url);
			}
		}
		String oldUrl = url;
		mUrl = url;

		if (isStarted()) {
			// If the Loader is currently started, we can immediately
			// deliver its results.
			super.deliverResult(url);
		}

		// At this point we can release the resources associated with
		// 'oldApps' if needed; now that the new result is delivered we
		// know that it is no longer in use.
		if (oldUrl != null) {
			onReleaseResources(oldUrl);
		}
	}

	/**
	 * Handles a request to start the Loader.
	 */
	@Override
	protected void onStartLoading() {
		if (mUrl != null) {
			// If we currently have a result available, deliver it
			// immediately.
			deliverResult(mUrl);
		}

		// Has something interesting in the configuration changed since we
		// last built the app list?
		boolean configChange = mLastConfig.applyNewConfig(getContext()
				.getResources());

		if (takeContentChanged() || mUrl == null || configChange) {
			// If the data has changed since the last time it was loaded
			// or is not currently available, start a load.
			forceLoad();
		}
	}

	/**
	 * Handles a request to stop the Loader.
	 */
	@Override
	protected void onStopLoading() {
		// Attempt to cancel the current load task if possible.
		cancelLoad();
	}

	/**
	 * Handles a request to cancel a load.
	 */
	@Override
	public void onCanceled(String places) {
		super.onCanceled(places);

		// At this point we can release the resources associated with 'apps'
		// if needed.
		onReleaseResources(places);
	}

	/**
	 * Handles a request to completely reset the Loader.
	 */
	@Override
	protected void onReset() {
		super.onReset();

		// Ensure the loader is stopped
		onStopLoading();

		// At this point we can release the resources associated with 'apps'
		// if needed.
		if (mUrl != null) {
			onReleaseResources(mUrl);
			mUrl = null;
		}

	}

	/**
	 * Helper function to take care of releasing resources associated with an
	 * actively loaded data set.
	 */
	protected void onReleaseResources(String places) {
		// For a simple List<> there is nothing to do. For something
		// like a Cursor, we would close it here.
	}

	/**
	 * Helper for determining if the configuration has changed in an interesting
	 * way so we need to rebuild the app list.
	 */
	public static class InterestingConfigChanges {
		final Configuration mLastConfiguration = new Configuration();
		int mLastDensity;

		boolean applyNewConfig(Resources res) {
			int configChanges = mLastConfiguration.updateFrom(res
					.getConfiguration());
			boolean densityChanged = mLastDensity != res.getDisplayMetrics().densityDpi;
			if (densityChanged
					|| (configChanges & (ActivityInfo.CONFIG_LOCALE
							| ActivityInfo.CONFIG_UI_MODE | ActivityInfo.CONFIG_SCREEN_LAYOUT)) != 0) {
				mLastDensity = res.getDisplayMetrics().densityDpi;
				return true;
			}
			return false;
		}
	}

}
