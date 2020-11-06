package ca.cmpt276.project.ui;

import android.content.Intent;
import android.graphics.Color;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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
import java.util.Collections;

import ca.cmpt276.project.R;
import ca.cmpt276.project.model.Inspection;
import ca.cmpt276.project.model.InspectionListManager;
import ca.cmpt276.project.model.Restaurant;
import ca.cmpt276.project.model.RestaurantListManager;
import ca.cmpt276.project.model.types.HazardLevel;
import ca.cmpt276.project.model.types.InspectionType;

/**
 * used to display the list of all restaurants
 */
public class RestaurantListActivity extends AppCompatActivity {
    private RestaurantListManager restaurantManager;

    private static boolean read = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_list);
        Toolbar toolbar = findViewById(R.id.toolbar_restaurant_list);
        setSupportActionBar(toolbar);

        restaurantManager = RestaurantListManager.getInstance();

        if(!read){
            fillRestaurantManager();
            read = true;
        }

        populateListView();
        registerCallBack();
    }



    private InspectionListManager fillInspectionManager(String restaurantTracking) {
        InspectionListManager inspectionList = new InspectionListManager();
        InputStream is = getResources().openRawResource(R.raw.inspectionreports_itr1);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8)
        );
        String line = "";
        try {
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                //split by ','
                String[] tokens = line.split(",");
                //read data
                String inspectionTracking = tokens[0].replace("\"", "");

                if(restaurantTracking.equals(inspectionTracking)) {

                    //SimpleDateFormat formatter1 = new SimpleDateFormat("yyyyMMdd");
                    //Date date = formatter1.parse(tokens[1]);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                    LocalDate date = LocalDate.parse(tokens[1], formatter);
                    String stringType = tokens[2].replace("\"", "");
                    InspectionType type = InspectionType.FOLLOWUP;
                    if (stringType.equals("Routine")) {
                        type = InspectionType.ROUTINE;
                    }

                    int numCritical = Integer.parseInt(tokens[3]);

                    int numNonCritical = Integer.parseInt(tokens[4]);

                    String stringHazardLevel = tokens[5].replace("\"", "");
                    HazardLevel hazardLevel = HazardLevel.LOW;
                    if (stringHazardLevel.equals("Moderate")) {
                        hazardLevel = HazardLevel.MODERATE;
                    } else if (stringHazardLevel.equals("High")) {
                        hazardLevel = HazardLevel.HIGH;
                    }

                    Inspection inspection;

                    if (tokens.length >= 7) {
                        StringBuilder lump = new StringBuilder();
                        for (int i=6; i<tokens.length; i++) {
                            lump.append(tokens[i]).append(",");
                        }
                        inspection = new Inspection(date, type, numCritical, numNonCritical, hazardLevel, lump.toString());
                    } else {
                        inspection = new Inspection(date, type, numCritical, numNonCritical, hazardLevel);
                    }
                    inspectionList.add(inspection);
                }
            }
        } catch(IOException e){
            Log.wtf("RestaurantListActivity", "error reading data file on line " + line, e);
        }
        return inspectionList;
    }

    private void fillRestaurantManager() {
        InputStream is = getResources().openRawResource(R.raw.restaurants_itr1);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8)
        );
        String line = "";
        try {
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                //split by ','
                String[] tokens = line.split(",");

                //read data
                Restaurant restaurant = new Restaurant(
                        tokens[1].replace("\"", ""), // Restaurant name
                        tokens[2].replace("\"", ""), // Restaurant Address
                        tokens[3].replace("\"", ""), // Restaurant city
                        Float.parseFloat(tokens[6]), // Restaurant Longitude
                        Float.parseFloat(tokens[5]), // Restaurant Latitude
                        tokens[0].replace("\"", "") // Restaurant tracking number
                );
                String restaurantTracking = restaurant.getTracking();
                InspectionListManager filled = fillInspectionManager(restaurantTracking);
                restaurant.setInspections(filled);
                restaurantManager.add(restaurant);
            }
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