package ca.cmpt276.project.model;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

// Android Programming: The Big Nerd Ranch Guide Chapter 25
public class SurreyDataGetter {
    private final String url_restaurant = "https://data.surrey.ca/api/3/action/package_show?id=restaurants";
    private final String url_inspections = "https://data.surrey.ca/api/3/action/package_show?id=fraser-health-restaurant-inspection-reports";

    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL (urlSpec);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() +
                                        ": with " +
                                        urlSpec);
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public void getData(String url) {
        try {
            String jsonString = getUrlString(url);
            Log.i("API Request", "Received JSON: " + jsonString);
            JSONObject jsonBody = new JSONObject(jsonString);
        } catch (IOException | JSONException e) {
            Log.e("API Request", "Failed to get data", e);
        }
    }
}
