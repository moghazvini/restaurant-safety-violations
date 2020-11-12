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
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import ca.cmpt276.project.R;
import ca.cmpt276.project.model.Inspection;
import ca.cmpt276.project.model.InspectionListManager;
import ca.cmpt276.project.model.LastModified;
import ca.cmpt276.project.model.Restaurant;
import ca.cmpt276.project.model.RestaurantListManager;
import ca.cmpt276.project.model.CsvInfo;
import ca.cmpt276.project.model.SurreyDataGetter;
import ca.cmpt276.project.model.types.HazardLevel;
import ca.cmpt276.project.model.types.InspectionType;

/**
 * Displays the list of all restaurants in alphabetical order
 */
public class RestaurantListActivity extends AppCompatActivity {
    private RestaurantListManager restaurantManager;
    private LastModified lastModified;
    private List<CsvInfo> restaurantUpdate;
    private BufferedReader updatedInspections;

    private static boolean read = false;

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
            getUpdatedFiles();
        }

        populateListView();
        registerCallBack();

        // Check if it has been 20 hours since last check
        if (past20Hours()) {
            Toast.makeText(this, "Checking for Update", Toast.LENGTH_LONG).show();
            new GetDataTask().execute();
        }

    }

    // Get the CSV links and timestmps
    private class GetDataTask extends AsyncTask<Void,Void,List<CsvInfo>> {
        @Override
        protected List<CsvInfo> doInBackground(Void... voids) {
            return new SurreyDataGetter().getDataLink(RestaurantListActivity.this);
        }

        @Override
        protected void onPostExecute(List<CsvInfo> data) {
            restaurantUpdate = data;
            // TODO: Dialog Box for updating if update is available
            if (data.get(0).getChanged() // check if restaurant list changed
                    || data.get(1).getChanged()) { // if inspection list changed
                // Want update? Execute function
                new ListUpdateTask().execute();
            }
        }
    }

    // Download CSV files
    private class ListUpdateTask extends AsyncTask<Void,Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            // TODO: Dialog Box for updating
            return new SurreyDataGetter().getCSVData(restaurantUpdate, RestaurantListActivity.this);
        }

        @Override
        protected void onPostExecute(Boolean receivedUpdate) {
            boolean update = receivedUpdate;
            if (update) {
                getUpdatedFiles();
            }
        }
    }

    private void fillInitialRestaurantList() {
        InputStream inputStream = getResources().openRawResource(R.raw.restaurants_itr1);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8)
        );

        fillRestaurantManager(reader);
    }

    private void getUpdatedFiles() {
        Toast.makeText(this, "THERE'S AN UPDATE", Toast.LENGTH_SHORT).show();
        FileInputStream inputStream_rest;
        FileInputStream inputStream_insp;
        try {
            inputStream_rest = RestaurantListActivity.this.openFileInput(SurreyDataGetter.DOWNLOAD_RESTAURANTS);
            inputStream_insp = RestaurantListActivity.this.openFileInput(SurreyDataGetter.DOWNLOAD_INSPECTIONS);
            InputStreamReader inputReader_rest = new InputStreamReader(inputStream_rest, StandardCharsets.UTF_8);
            InputStreamReader inputReader_insp = new InputStreamReader(inputStream_insp, StandardCharsets.UTF_8);
            updatedInspections = new BufferedReader(inputReader_insp);
            fillRestaurantManager(new BufferedReader(inputReader_rest));
        } catch (FileNotFoundException e) {
            // No update files downloaded
            Toast.makeText(this, "CAN'T FIND FILES",Toast.LENGTH_LONG).show();
        }
    }

    private boolean past20Hours() {
        lastModified = LastModified.getInstance(RestaurantListActivity.this);
        LocalDateTime previous = lastModified.getLastCheck();
        LocalDateTime current = LocalDateTime.now();
        return current.minusHours(20).isAfter(previous) || current.minusHours(20).isEqual(previous);
    }

    private void fillRestaurantManager(BufferedReader reader) {
        String line = "";
        try {
            reader.readLine();
            restaurantManager.getList().clear();
            while ((line = reader.readLine()) != null) {
                //System.out.println(line);
                line = line.replace("\"", "");

                String[] attributes = line.split(",");
                String tracking = attributes[0];
                tracking = tracking.replace(" ", "");
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
            populateListView();
        } catch(IOException e){
            Log.wtf("RestaurantListActivity", "error reading data file on line " + line, e);
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
                    // find the restaurant that has the same tracking number
                    if (!restaurant.getTracking().equals(inspectionTracking)) {
                        restaurant = restaurantManager.find(inspectionTracking);
                    }
                    if (restaurant != null) {
                        InspectionListManager inspectionList = restaurant.getInspections();

                        // Format string date
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                        LocalDate date = LocalDate.parse(tokens[1], formatter);

                        InspectionType type = getInspectionType(tokens[2]);
                        int numCritical = Integer.parseInt(tokens[3]);
                        int numNonCritical = Integer.parseInt(tokens[4]);
                        HazardLevel hazardLevel = getHazardLevel(tokens[tokens.length-1]);

                        Inspection inspection;
                        if (tokens.length > 5) {
                            if (tokens[5].length() > 0) {
                                String violationLump = getVioLump(tokens);
                                inspection = new Inspection(date, type, numCritical, numNonCritical, hazardLevel, violationLump);
                            } else {
                                inspection = new Inspection(date, type, numCritical, numNonCritical, hazardLevel);
                            }
                        } else {
                            inspection = new Inspection(date, type, numCritical, numNonCritical, hazardLevel);
                        }
                        inspectionList.add(inspection);
                    } else {
                        // reset restaurant if restaurant wasn't found
                        // otherwise restaurant would be null for next iteration
                        restaurant = restaurantManager.getRestaurant(0);
                    }
                }
            }
        } catch(IOException e){
            Log.wtf("RestaurantListActivity", "error reading data file on line " + line, e);
        }
    }

    private InspectionType getInspectionType(String type) {
        if (type.equals("Routine")) {
            return InspectionType.ROUTINE;
        } else {
            return InspectionType.FOLLOWUP;
        }
    }

    private HazardLevel getHazardLevel(String hazard) {
        if (hazard.equals("High")) {
            return HazardLevel.HIGH;
        } else if (hazard.equals("Moderate")) {
            return HazardLevel.MODERATE;
        } else {
            return HazardLevel.LOW;
        }
    }

    private String getVioLump(String[] inspectionRow) {
        StringBuilder lump = new StringBuilder();
        for (int i = 5; i < inspectionRow.length - 1; i++) {
            lump.append(inspectionRow[i]).append(",");
        }
        return lump.toString();
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