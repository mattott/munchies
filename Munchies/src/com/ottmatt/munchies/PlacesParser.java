package com.ottmatt.munchies;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.util.JsonReader;

public class PlacesParser {
	String placesUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?key=AIzaSyDQTv-BHPQiKVZiCapQX0ELaQHnOOHRA6M&types=food&location=37.347627,-122.062515&radius=5000&sensor=false&opennow=true";

	public List retrieveStream() throws IOException {
		URL url = new URL(placesUrl);
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

	public List readStream(InputStream in) throws IOException {
		JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
		try {
			return readMessagesArray(reader);
		} finally {
			reader.close();
		}
	}

	public List readMessagesArray(JsonReader reader) throws IOException {
		List messages = new ArrayList();
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
		String id = null;
		String store_name = null;
		int price_level = -1;
		double rating = -1.0;
		String vicinity = null;

		reader.beginObject();
		while (reader.hasNext()) {
			String name = reader.nextName();
			if (name.equals("icon"))
				icon = reader.nextString();
			else if (name.equals("id"))
				id = reader.nextString();
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

		return new Message(icon, id, store_name, vicinity, price_level, rating);
	}

	public class Message {
		public String mIcon;
		public String mId;
		public String mStore;
		public String mVicinity;
		public int mPrice_level;
		public double mRating;

		public Message(String icon, String id, String store_name,
				String vicinity, int price_level, double rating) {
			mIcon = icon;
			mId = id;
			mStore = store_name;
			mVicinity = vicinity;
			mPrice_level = price_level;
			mRating = rating;
		}

		public String getIcon() {
			return mIcon;
		}

		public String getId() {
			return mId;
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
