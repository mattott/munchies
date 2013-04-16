package com.ottmatt.munchies.parsers;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.util.JsonReader;

public class PlaceDetailsParser {
	String placesUrl = "https://maps.googleapis.com/maps/api/place/details/json?key=AIzaSyDQTv-BHPQiKVZiCapQX0ELaQHnOOHRA6M&sensor=false&reference=";

	public String retrieveStream(String reference) throws IOException {
		URL url = new URL(placesUrl + reference);
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

	public String readStream(InputStream in) throws IOException {
		JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
		try {
			return readMessagesArray(reader);
		} finally {
			reader.close();
		}
	}

	public String readMessagesArray(JsonReader reader) throws IOException {
		String url = "";
		reader.beginObject();
		while (reader.hasNext()) {
			String token = reader.nextName();
			if (token.equals("result")) {
				url += readMessage(reader);
			} else
				reader.skipValue();
		}
		reader.endObject();
		return url;
	}

	public String readMessage(JsonReader reader) throws IOException {
		reader.beginObject();
		while (reader.hasNext()) {
			String name = reader.nextName();
			if (name.equals("url"))
				return reader.nextString();
			else
				reader.skipValue();
		}
		reader.endObject();
		return null;
	}
}