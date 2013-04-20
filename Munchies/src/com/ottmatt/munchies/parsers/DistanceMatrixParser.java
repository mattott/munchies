package com.ottmatt.munchies.parsers;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.location.Location;
import android.util.JsonReader;

public class DistanceMatrixParser {
	protected final String matrixUrl = "http://maps.googleapis.com/maps/api/distancematrix/json?sensor=false&avoid=tolls&units=imperial";

	public String[] retrieveStream(Location origin, Location dest)
			throws IOException {
		String sOrigins = "&origins=" + Double.toString(origin.getLatitude())
				+ "," + Double.toString(origin.getLongitude());
		String sDest = "&destinations=" + Double.toString(dest.getLatitude())
				+ "," + Double.toString(dest.getLongitude());
		URL url = new URL(matrixUrl + sOrigins + sDest);
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

	public String[] readStream(InputStream in) throws IOException {
		JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
		try {
			return readMessagesArray(reader);
		} finally {
			reader.close();
		}
	}

	public String[] readMessagesArray(JsonReader reader) throws IOException {
		String[] messages = null;
		reader.beginObject();
		while (reader.hasNext()) {
			String token = reader.nextName();
			if (token.equals("rows")) {
				reader.beginArray();
				reader.beginObject();
				while (reader.hasNext()) {
					if (reader.nextName().equals("elements"))
						messages = readMessage(reader);
					else
						reader.skipValue();
				}
				reader.endObject();
				reader.endArray();
			} else
				reader.skipValue();
		}
		reader.endObject();
		return messages;
	}

	public String[] readMessage(JsonReader reader) throws IOException {
		String distance = null;
		String duration = null;
		reader.beginArray();
		reader.beginObject();
		while (reader.hasNext()) {
			String name = reader.nextName();
			if (name.equals("duration"))
				duration = readElement(reader);
			else if (name.equals("distance"))
				distance = readElement(reader);
			else
				reader.skipValue();
		}
		reader.endObject();
		reader.endArray();

		return new String[] { duration, distance };
	}

	public String readElement(JsonReader reader) throws IOException {
		String element = null;
		reader.beginObject();
		while (reader.hasNext()) {
			String token = reader.nextName();
			if (token.equals("text"))
				element = reader.nextString();
			else
				reader.skipValue();
		}
		reader.endObject();
		return element;
	}
}
