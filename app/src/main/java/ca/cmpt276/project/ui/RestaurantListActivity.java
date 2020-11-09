package ca.cmpt276.project.ui;

import android.content.Intent;
import android.graphics.Color;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ca.cmpt276.project.R;
import ca.cmpt276.project.model.Inspection;
import ca.cmpt276.project.model.InspectionListManager;
import ca.cmpt276.project.model.Restaurant;
import ca.cmpt276.project.model.RestaurantListManager;
import ca.cmpt276.project.model.SurreyData;
import ca.cmpt276.project.model.SurreyDataGetter;
import ca.cmpt276.project.model.types.HazardLevel;
import ca.cmpt276.project.model.types.InspectionType;

/**
 * Displays the list of all restaurants in alphabetical order
 */
public class RestaurantListActivity extends AppCompatActivity {
    private RestaurantListManager restaurantManager;
    private List<SurreyData> restaurantUpdate;
    private BufferedReader updatedInspections;
    private BufferedReader updatedRestaurants;

    private static boolean read = false;
    private static boolean downloaded = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_list);
        Toolbar toolbar = findViewById(R.id.toolbar_restaurant_list);
        setSupportActionBar(toolbar);

        restaurantManager = RestaurantListManager.getInstance();

        if(!read){
            fillInitialRestaurantList();
            read = true;
        }

        populateListView();
        registerCallBack();
        if (!downloaded) {
            new GetDataTask().execute();
            downloaded = true;
        }

    }

    private class GetDataTask extends AsyncTask<Void,Void,List<SurreyData>> {
        @Override
        protected List<SurreyData> doInBackground(Void... voids) {
            return new SurreyDataGetter().getDataLink();
        }

        @Override
        protected void onPostExecute(List<SurreyData> data) {
            restaurantUpdate = data;
            // TODO: Dialog Box for updating
            new InspectionUpdateTask().execute();
        }
    }

    private class RestaurantUpdateTask extends AsyncTask<Void,Void,BufferedReader> {
        @Override
        protected BufferedReader doInBackground(Void... voids) {
            // TODO: Dialog Box for updating
            return new SurreyDataGetter().getCSVData(restaurantUpdate.get(0).getUrl());
        }

        @Override
        protected void onPostExecute(BufferedReader data) {
            updatedRestaurants = data;
            fillRestaurantManager(updatedRestaurants);
        }
    }

    private class InspectionUpdateTask extends AsyncTask<Void,Void,BufferedReader> {
        @Override
        protected BufferedReader doInBackground(Void... voids) {
            return new SurreyDataGetter().getCSVData(restaurantUpdate.get(1).getUrl());
        }

        @Override
        protected void onPostExecute(BufferedReader bufferedReader) {
            updatedInspections = bufferedReader;
            new RestaurantUpdateTask().execute();
        }
    }

    private void fillInspectionManager() {
        BufferedReader reader;
        if (!read) {
            InputStream is = getResources().openRawResource(R.raw.inspectionreports_itr1);
            reader = new BufferedReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8)
            );
        } else {
            reader = updatedInspections;
        }

        String line = "";
        try {
            Restaurant restaurant = restaurantManager.getRestaurant(0);

            reader.readLine();
            while ((line = reader.readLine()) != null) {
                line = line.replace("\"", "");
                //split by ','
                String[] tokens = line.split(",");

                //read data
                if (tokens.length > 0) {
                    // Get the restaurant that matches the tracking number of the inspection
                    String inspectionTracking = tokens[0];
                    if (!restaurant.getTracking().equals(inspectionTracking)) {
                        restaurant = restaurantManager.find(inspectionTracking);
                    }
                    if (restaurant != null) {
                        InspectionListManager inspectionList = restaurant.getInspections();

                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                        LocalDate date = LocalDate.parse(tokens[1], formatter);
                        String stringType = tokens[2];
                        InspectionType type = InspectionType.FOLLOWUP;
                        if (stringType.equals("Routine")) {
                            type = InspectionType.ROUTINE;
                        }

                        int numCritical = Integer.parseInt(tokens[3]);

                        int numNonCritical = Integer.parseInt(tokens[4]);

                        String stringHazardLevel = tokens[tokens.length-1];
                        HazardLevel hazardLevel = HazardLevel.LOW;
                        if (stringHazardLevel.equals("Moderate")) {
                            hazardLevel = HazardLevel.MODERATE;
                        } else if (stringHazardLevel.equals("High")) {
                            hazardLevel = HazardLevel.HIGH;
                        }

                        Inspection inspection;
                        if (tokens.length > 5) {
                            if (tokens[5].length() > 0) {
                                StringBuilder lump = new StringBuilder();
                                for (int i = 5; i < tokens.length - 1; i++) {
                                    lump.append(tokens[i]).append(",");
                                }
                                inspection = new Inspection(date, type, numCritical, numNonCritical, hazardLevel, lump.toString());
                            } else {
                                inspection = new Inspection(date, type, numCritical, numNonCritical, hazardLevel);
                            }
                        } else {
                            inspection = new Inspection(date, type, numCritical, numNonCritical, hazardLevel);
                        }
                        inspectionList.add(inspection);
                    } else {
                        restaurant = restaurantManager.getRestaurant(0);
                    }
                }
            }
            populateListView();
        } catch(IOException e){
            Log.wtf("RestaurantListActivity", "error reading data file on line " + line, e);
        }
    }

    private void fillInitialRestaurantList() {
        InputStream inputStream = getResources().openRawResource(R.raw.restaurants_itr1);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8)
        );

        fillRestaurantManager(reader);
    }

    private void fillRestaurantManager(BufferedReader reader) {
        String line = "";
        try {
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                //System.out.println(line);
                line = line.replace("\"", "");
                line = line.replace(" ", "");
                String[] attributes = line.split(",");
                String tracking = attributes[0];
                String name = attributes[1];

                int addrIndex = attributes.length - 5;
                for (int i = 2; i < addrIndex; i++) {
                    name = name.concat(attributes[i]);
                }
                String addr = attributes[addrIndex];
                String city = attributes[addrIndex + 1];
                float gpsLong = Float.parseFloat(attributes[addrIndex + 3]);
                float gpsLat = Float.parseFloat(attributes[addrIndex + 4]);

                //read data
                Restaurant restaurant = new Restaurant(
                        tracking,
                        name,
                        addr,
                        city,
                        gpsLong, // Restaurant Longitude
                        gpsLat // Restaurant Latitude
                );
                restaurantManager.add(restaurant);
            }
            fillInspectionManager();
        } catch(IOException e){
            Log.wtf("RestaurantListActivity", "error reading data file on line " + line, e);
        }
    }

    private void populateListView() {
        //code to sort alphabetically taken from https://www.youtube.com/watch?v=dZQqrPdqT1E
        Collections.sort(restaurantManager.getList());
        ArrayAdapter<Restaurant> adapter = new RestaurantListAdapter();
        ListView list = findViewById(R.id.listViewRestaurants);
        list.setAdapter(adapter);
    }

    private class RestaurantListAdapter extends ArrayAdapter<Restaurant>{
        public RestaurantListAdapter(){
            super(RestaurantListActivity.this, R.layout.restaurant_list_item, restaurantManager.getList());
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if(itemView == null){
                itemView = getLayoutInflater().inflate(R.layout.restaurant_list_item, parent, false);
            }
            // find restaurant
            Restaurant currentRestaurant = restaurantManager.getRestaurant(position);
            InspectionListManager currentInspectionList = currentRestaurant.getInspections();

            ImageView hazardImageView = itemView.findViewById(R.id.item_hazard_icon);
            TextView nameText = itemView.findViewById(R.id.item_txt_restaurant_name);
            TextView issuesText = itemView.findViewById(R.id.item_txt_issues_found);
            TextView inspectionText = itemView.findViewById(R.id.item_txt_latest_inspection);
            TextView hazardText = itemView.findViewById(R.id.item_txt_hazard);
            nameText.setText(currentRestaurant.getName());

            if(currentInspectionList.getInspections().size() > 0) {
                Inspection latestInspection;
                latestInspection = Collections.max(currentInspectionList.getInspections());
                String issuesMessage = (latestInspection.getCritical() + latestInspection.getNonCritical()) + " issue(s)";
                issuesText.setText(issuesMessage);

                switch (latestInspection.getLevel()) {
                    case LOW:
                        hazardText.setText(R.string.hazard_low);
                        hazardText.setTextColor(Color.parseColor("#45DE08")); // Green
                        hazardImageView.setBackgroundResource(R.drawable.green_hazard);
                        break;
                    case MODERATE:
                        hazardText.setText(R.string.hazard_moderate);
                        hazardText.setTextColor(Color.parseColor("#FA9009")); // Orange
                        hazardImageView.setBackgroundResource(R.drawable.orange_hazard);
                        break;
                    case HIGH:
                        hazardText.setText(R.string.hazard_high);
                        hazardText.setTextColor(Color.parseColor("#FA2828")); // Red
                        hazardImageView.setBackgroundResource(R.drawable.red_hazard);
                        break;
                    default:
                        assert false;
                }

                LocalDate currentDate = LocalDate.now();

                String inspectionDateText;
                if(Math.abs(currentDate.getYear() - latestInspection.getDate().getYear()) != 0){
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
                    inspectionDateText = formatter.format(latestInspection.getDate());
                }
                else if(Math.abs(currentDate.getMonthValue() - latestInspection.getDate().getMonthValue()) != 0){
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");
                    inspectionDateText = formatter.format(latestInspection.getDate());
                }
                else{
                    inspectionDateText = latestInspection.getDate().getDayOfMonth() + " days ago";
                }
                inspectionText.setText(inspectionDateText);
            }
            else{
                issuesText.setText(R.string.no_inspection_found);
                inspectionText.setText("");
            }
            return itemView;
        }
    }

    private void registerCallBack(){
        ListView list = findViewById(R.id.listViewRestaurants);
        list.setOnItemClickListener((parent, viewClicked, position, id) -> {
            Intent i = RestaurantDetailsActivity.makeLaunchIntent(RestaurantListActivity.this,position);
            startActivity(i);
        });
    }
}