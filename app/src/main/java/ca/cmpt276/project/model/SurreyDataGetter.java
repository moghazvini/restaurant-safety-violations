package ca.cmpt276.project.model;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// Android Programming: The Big Nerd Ranch Guide Chapter 25
public class SurreyDataGetter {
    private final String url = "https://data.surrey.ca/api/3/action/";
    private final String id_restaurant = "restaurants";
    private final String id_inspections = "fraser-health-restaurant-inspection-reports";

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

            int bytesRead;
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

    public BufferedReader getCSVData(String urlSpec){
        try {
            System.out.println(urlSpec);
            String csvString = getUrlString(urlSpec);
            Reader csvReader = new StringReader(csvString);
            return new BufferedReader(csvReader);
            //return new BufferedReader(csvData);
        } catch (IOException e) {
            Log.e("API Request", "Failed to get CSV data", e);
        }
        return null;
    }

    public List<SurreyData> getDataLink() {
        List<SurreyData> data = new ArrayList<>();
        try {
            // Get restaurant list
            String restaurantUrl = Uri.parse(url)
                    .buildUpon()
                    .appendPath("package_show")
                    .appendQueryParameter("id", id_restaurant)
                    .build().toString();
            String jsonRestaurant = getUrlString(restaurantUrl);
            JSONObject jsonBodyRestaurant = new JSONObject(jsonRestaurant);
            SurreyData restaurant = new SurreyData();

            // Get Inspection list
            String inspectionUrl = Uri.parse(url)
                    .buildUpon()
                    .appendPath("package_show")
                    .appendQueryParameter("id", id_inspections)
                    .build().toString();
            String jsonInspection = getUrlString(inspectionUrl);
            JSONObject jsonBodyInspection = new JSONObject(jsonInspection);
            SurreyData inspection = new SurreyData();

            parseData(restaurant, jsonBodyRestaurant);
            parseData(inspection, jsonBodyInspection);
            data.add(restaurant);
            data.add(inspection);
        } catch (IOException | JSONException e) {
            Log.e("API Request", "Failed to get data", e);
        }
        return data;
    }

    private void parseData(SurreyData data, JSONObject jsonBody) throws JSONException {
        JSONObject  resultJsonObject = jsonBody.getJSONObject("result");
        JSONArray resourcesJsonArray = resultJsonObject.getJSONArray("resources");

        // need the first index for all required information
        JSONObject resourcesJsonObject = resourcesJsonArray.getJSONObject(0);

        // csv datalink
        data.setUrl(resourcesJsonObject.getString("url"));

        // format last modified string into a Date
        String time = resourcesJsonObject.getString("last_modified");
        time = time.substring(0,10);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        data.setLast_modified(LocalDate.parse(time, formatter));
    }
}
