package com.ottmatt.munchies.parsers;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.util.JsonReader;

public class PlaceSearchParser {
	String placesUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?key=AIzaSyDQTv-BHPQiKVZiCapQX0ELaQHnOOHRA6M&types=food&sensor=false&rankby=distance&opennow=true&location=";

	public List<Message> retrieveStream(String location) throws IOException {
		URL url = new URL(placesUrl + location);
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
		int price_level = -1;
		double rating = -1.0;
		String vicinity = null;

		reader.beginObject();
		while (reader.hasNext()) {
			String name = reader.nextName();
			if (name.equals("icon"))
				icon = reader.nextString();
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

		return new Message(icon, reference, store_name, vicinity, price_level,
				rating);
	}

	public class Message {
		public String mIcon;
		public String mReference;
		public String mStore;
		public String mVicinity;
		public int mPrice_level;
		public double mRating;

		public Message(String icon, String reference, String store_name,
				String vicinity, int price_level, double rating) {
			mIcon = icon;
			mReference = reference;
			mStore = store_name;
			mVicinity = vicinity;
			mPrice_level = price_level;
			mRating = rating;
		}

		public String getIcon() {
			return mIcon;
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
