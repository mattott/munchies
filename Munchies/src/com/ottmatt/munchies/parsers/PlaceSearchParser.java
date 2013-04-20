package com.ottmatt.munchies.parsers;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.location.Location;
import android.util.JsonReader;
import android.util.Log;

public class PlaceSearchParser {
	protected final String placesUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?key=AIzaSyDQTv-BHPQiKVZiCapQX0ELaQHnOOHRA6M&types=food&sensor=false&rankby=distance&opennow=true&location=";
	Location mOrigin;

	public List<Message> retrieveStream(Location startLocation)
			throws IOException {
		mOrigin = startLocation;
		URL url = new URL(placesUrl
				+ Double.toString(startLocation.getLatitude()) + ","
				+ Double.toString(startLocation.getLongitude()));
		HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
		try {
			InputStream in = new BufferedInputStream(urlConn.getInputStream());
			return readStream(in);
		} catch (Exception e) {

		} finally {
			urlConn.disconnect();
		}
		return null;
	}

	public List<Message> readStream(InputStream in) throws IOException {
		JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
		try {
			return readMessagesArray(reader);
		} finally {
			reader.close();
		}
	}

	public List<Message> readMessagesArray(JsonReader reader)
			throws IOException {
		List<Message> messages = new ArrayList<Message>();
		reader.beginObject();
		while (reader.hasNext()) {
			String token = reader.nextName();
			if (token.equals("results")) {
				reader.beginArray();
				while (reader.hasNext())
					messages.add(readMessage(reader));
				reader.endArray();
			} else
				reader.skipValue();
		}
		reader.endObject();
		return messages;
	}

	public Message readMessage(JsonReader reader) throws IOException {
		String icon = null;
		String reference = null;
		String store_name = null;
		String vicinity = null;
		int price_level = -1;
		double rating = -1.0;
		String[] dMatrix = null;

		reader.beginObject();
		while (reader.hasNext()) {
			String name = reader.nextName();
			if (name.equals("icon"))
				icon = reader.nextString();
			else if (name.equals("geometry"))
				dMatrix = readDistance(reader);
			else if (name.equals("reference"))
				reference = reader.nextString();
			else if (name.equals("name"))
				store_name = reader.nextString();
			else if (name.equals("price_level"))
				price_level = reader.nextInt();
			else if (name.equals("rating"))
				rating = reader.nextDouble();
			else if (name.equals("vicinity"))
				vicinity = reader.nextString();
			else
				reader.skipValue();
		}
		reader.endObject();

		return new Message(icon, dMatrix, reference, store_name, vicinity,
				price_level, rating);
	}

	public String[] readDistance(JsonReader reader) throws IOException {
		double lat = 0.0;
		double lng = 0.0;
		reader.beginObject();
		while (reader.hasNext()) {
			String token = reader.nextName();
			if (token.equals("location")) {
				reader.beginObject();
				while (reader.hasNext()) {
					String geo = reader.nextName();
					if (geo.equals("lat"))
						lat = reader.nextDouble();
					else if (geo.equals("lng"))
						lng = reader.nextDouble();
					else
						reader.skipValue();
				}
				reader.endObject();
			} else
				reader.skipValue();
		}
		reader.endObject();
		Log.d("dest", Double.toString(lat) + Double.toString(lng));
		Location dest = new Location("dest");
		dest.setLatitude(lat);
		dest.setLongitude(lng);
		DistanceMatrixParser dMp = new DistanceMatrixParser();
		return dMp.retrieveStream(mOrigin, dest);
	}

	public double toRad(double degree) {
		return degree * (Math.PI / 180);
	}

	public class Message {
		public String mIcon, mReference, mStore, mVicinity;
		public String[] dMatrix;
		public int mPrice_level;
		public double mRating;

		public Message(String icon, String[] matrix, String reference,
				String store_name, String vicinity, int price_level,
				double rating) {
			mIcon = icon;
			dMatrix = matrix;
			mReference = reference;
			mStore = store_name;
			mVicinity = vicinity;
			mPrice_level = price_level;
			mRating = rating;
		}

		public String getIcon() {
			return mIcon;
		}

		public String getDuration() {
			return dMatrix[0];
		}

		public String getDistance() {
			return dMatrix[1];
		}

		public String getReference() {
			return mReference;
		}

		public String getStore() {
			return mStore;
		}

		public String getVicinity() {
			return mVicinity;
		}

		public int getPriceLevel() {
			return mPrice_level;
		}

		public double getRating() {
			return mRating;
		}
	}

}
