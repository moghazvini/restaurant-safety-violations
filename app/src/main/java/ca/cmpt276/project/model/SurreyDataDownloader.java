package ca.cmpt276.project.model;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Connects to the Surrey data api and parses and stores the data received.
 *
 * Referenced the textbook for most of the API connection code:
 * Android Programming: The Big Nerd Ranch Guide Chapter 25
 */
public class SurreyDataDownloader {
    public static final String DOWNLOAD_RESTAURANTS = "dl_restaurants";
    public static final String DOWNLOAD_INSPECTIONS = "dl_inspections";

    /**
     * Connects to the URL and receives the data in a byte array.
     * @param urlSpec The URl to connect to
     * @return The byte array received from the API
     */
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

    /**
     * Downloads the CSV file from the link provided and saves it as a
     * File in internal storage.
     */
    public void getCSVData(List<CsvInfo> csvLinks, Context context){
        try {
            //System.out.println(urlSpec);
            String csvRestaurant = getUrlString(csvLinks.get(0).getUrl());
            String csvInspection = getUrlString(csvLinks.get(1).getUrl());

            // Save CSV files for downloaded restaurant and inspection lists
            try {
                FileOutputStream outputRestaurant = context.openFileOutput(DOWNLOAD_RESTAURANTS, Context.MODE_PRIVATE);
                FileOutputStream outputInspection = context.openFileOutput(DOWNLOAD_INSPECTIONS, Context.MODE_PRIVATE);
                outputRestaurant.write(csvRestaurant.getBytes(StandardCharsets.UTF_8));
                outputInspection.write(csvInspection.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                Log.e("File Failure", "Failed to save file", e);
            }
            
        } catch (IOException e) {
            Log.e("API Request", "Failed to get CSV data", e);
        }
    }

    /**
     * Builds the URL for the restaurant and inspection list and gets the
     * JsonBody, ready to be parsed for information.
     */
    public List<CsvInfo> getDataLink(Context context) {
        List<CsvInfo> data = new ArrayList<>();
        try {
            String url = "https://data.surrey.ca/api/3/action/";
            // Get restaurant list
            String id_restaurant = "restaurants";
            String restaurantUrl = Uri.parse(url)
                    .buildUpon()
                    .appendPath("package_show")
                    .appendQueryParameter("id", id_restaurant)
                    .build().toString();
            String jsonRestaurant = getUrlString(restaurantUrl);
            JSONObject jsonBodyRestaurant = new JSONObject(jsonRestaurant);
            CsvInfo restaurant = new CsvInfo();

            // Get Inspection list
            String id_inspections = "fraser-health-restaurant-inspection-reports";
            String inspectionUrl = Uri.parse(url)
                    .buildUpon()
                    .appendPath("package_show")
                    .appendQueryParameter("id", id_inspections)
                    .build().toString();
            String jsonInspection = getUrlString(inspectionUrl);
            JSONObject jsonBodyInspection = new JSONObject(jsonInspection);
            CsvInfo inspection = new CsvInfo();

            parseData(restaurant, jsonBodyRestaurant);
            parseData(inspection, jsonBodyInspection);
            data.add(restaurant);
            data.add(inspection);

            checkModified(data, context);
        } catch (IOException | JSONException e) {
            Log.e("API Request", "Failed to get data", e);
        }
        return data;
    }

    /**
     * Parses the data received from the API call into a CsvData class
     */
    private void parseData(CsvInfo data, JSONObject jsonBody) throws JSONException {
        JSONObject  resultJsonObject = jsonBody.getJSONObject("result");
        JSONArray resourcesJsonArray = resultJsonObject.getJSONArray("resources");

        // need the first index for all required information
        JSONObject resourcesJsonObject = resourcesJsonArray.getJSONObject(0);

        // csv datalink
        data.setUrl(resourcesJsonObject.getString("url"));

        // format last modified string into a Date
        String time = resourcesJsonObject.getString("last_modified");
        time = time.substring(0,19);
        time = time.replace("T","");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-ddHH:mm:ss");
        data.setLast_modified(LocalDateTime.parse(time, formatter));
    }

    /**
     * Checks if the CSV data have been modified
     * @param data List of CSV file information to be filled
     */
    private void checkModified(List<CsvInfo> data, Context context) {
        LastModified lastModified = LastModified.getInstance(context);
        CsvInfo restaurant = data.get(0);
        CsvInfo inspection = data.get(1);

        if (restaurant.getLast_modified().isAfter(lastModified.getLast_mod_restaurants())) {
            System.out.println("SERVER: " + restaurant.getLast_modified() + " SAVED: " + lastModified.getLast_mod_restaurants());
            restaurant.setChanged(true);
        } else {
            restaurant.setChanged(false);
        }

        if (inspection.getLast_modified().isAfter(lastModified.getLast_mod_inspections())) {
            System.out.println("SERVER: " + inspection.getLast_modified() + " SAVED: " + lastModified.getLast_mod_inspections());
            inspection.setChanged(true);
        } else {
            inspection.setChanged(false);
        }
    }
}
