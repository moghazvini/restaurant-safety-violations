package ca.cmpt276.project.model;

import android.content.Context;
import android.content.SharedPreferences;
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
import java.security.acl.LastOwnerException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// Android Programming: The Big Nerd Ranch Guide Chapter 25
public class SurreyDataGetter {
    public static final String DOWNLOAD_RESTAURANTS = "dl_restaurants";
    public static final String DOWNLOAD_INSPECTIONS = "dl_inspections";

    private static final String LAST_UPDATED = "last updated";

    public LocalDateTime getLastUpdate(Context context) {
        long defaultTime = getDefaultTime();
        long lastUpdated = readLastUpdated(context, defaultTime);
        // convert long to LocalTimeDate
        return Instant.ofEpochMilli(lastUpdated).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    private static Long readLastUpdated(Context context, long defaultTime) {
        SharedPreferences stored = context.getSharedPreferences("AppPrefs",Context.MODE_PRIVATE);
        return stored.getLong(LAST_UPDATED, defaultTime);
    }

    public void writeLastUpdated(Context context) {
        SharedPreferences stored = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = stored.edit();

        long save = convertToLong(LocalDateTime.now());

        editor.putLong(LAST_UPDATED, save);
        editor.apply();
    }

    private long convertToLong(LocalDateTime date) {
        ZonedDateTime zdt = ZonedDateTime.of(date, ZoneId.systemDefault());
        return zdt.toInstant().toEpochMilli();
    }

    // https://www.javaguides.net/2020/03/convert-localdatetime-to-long-in-java.html
    // Convert LocalDateTime to a Long
    private long getDefaultTime() {
        LocalDateTime yesterday = LocalDateTime.now().plusDays(-1);
        ZonedDateTime zdt = ZonedDateTime.of(yesterday, ZoneId.systemDefault());

        return zdt.toInstant().toEpochMilli();
    }

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

    public boolean getCSVData(List<SurreyData> csvLinks, Context context){
        try {
            //System.out.println(urlSpec);
            String csvRestaurant = getUrlString(csvLinks.get(0).getUrl());
            String csvInspection = getUrlString(csvLinks.get(1).getUrl());

            // Save CSV files for downloaded restaurant and inspection lists
            try(FileOutputStream outputStream = context.openFileOutput(DOWNLOAD_RESTAURANTS, Context.MODE_PRIVATE)) {
                outputStream.write(csvRestaurant.getBytes(StandardCharsets.UTF_8));
            }
            try(FileOutputStream outputStream = context.openFileOutput(DOWNLOAD_INSPECTIONS, Context.MODE_PRIVATE)) {
                outputStream.write(csvInspection.getBytes(StandardCharsets.UTF_8));
            }

            return true;
            
        } catch (IOException e) {
            Log.e("API Request", "Failed to get CSV data", e);
        }
        return false;
    }

    public List<SurreyData> getDataLink() {
        List<SurreyData> data = new ArrayList<>();
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
            SurreyData restaurant = new SurreyData();

            // Get Inspection list
            String id_inspections = "fraser-health-restaurant-inspection-reports";
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
