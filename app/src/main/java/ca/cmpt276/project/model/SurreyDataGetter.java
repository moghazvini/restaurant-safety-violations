package ca.cmpt276.project.model;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.Buffer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

// Android Programming: The Big Nerd Ranch Guide Chapter 25
public class SurreyDataGetter {
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

    public BufferedReader getCSVData(String urlSpec) throws IOException{
        try {
            String csvString = getUrlString(urlSpec);
            Reader csvReader = new StringReader(csvString);
            return new BufferedReader(csvReader);
            //return new BufferedReader(csvData);
        } catch (IOException e) {
            Log.e("API Request", "Failed to get CSV data", e);
        }
        return null;
    }

    public SurreyData getDataLink(String url) {
        SurreyData data = new SurreyData();
        try {
            String jsonString = getUrlString(url);
            Log.i("API Request", "Received JSON: " + jsonString);
            JSONObject jsonBody = new JSONObject(jsonString);
            parseData(data, jsonBody);
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
