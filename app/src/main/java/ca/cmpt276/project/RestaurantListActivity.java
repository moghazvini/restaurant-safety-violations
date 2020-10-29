package ca.cmpt276.project;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import ca.cmpt276.project.model.Inspection;
import ca.cmpt276.project.model.InspectionListManager;
import ca.cmpt276.project.model.Restaurant;
import ca.cmpt276.project.model.RestaurantListManager;
import ca.cmpt276.project.model.types.HazardLevel;
import ca.cmpt276.project.model.types.InspectionType;

public class RestaurantListActivity extends AppCompatActivity {
    private RestaurantListManager restaurantManager;

    private static boolean read = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_list);
        restaurantManager = RestaurantListManager.getInstance();

        if(!read){
            fillRestaurantManager();
            read = true;
        }

        populateListView();
        registerCallBack();
    }



    private InspectionListManager fillInspectionManager(InspectionListManager inspectionList, String restaurantTracking) {

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

                    SimpleDateFormat formatter1 = new SimpleDateFormat("yyyyMMdd");
                    Date date = formatter1.parse(tokens[1]);
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
                    //Log.d("TAG", "new inspection made for Tracking: " + restaurantTracking);
                    Inspection inspection = new Inspection(date, type, numCritical, numNonCritical, hazardLevel);
                    inspectionList.add(inspection);
                }
            }
        } catch(IOException | ParseException e){
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
                        Float.parseFloat(tokens[6]), // Restaurant Longitude
                        Float.parseFloat(tokens[5]), // Restaurant Latitude
                        tokens[0].replace("\"", "") // Restaurant tracking number
                );
                //
                InspectionListManager inspectionManager = new InspectionListManager();
                String restaurantTracking = restaurant.getTracking();
                InspectionListManager filled;

                filled = fillInspectionManager(inspectionManager, restaurantTracking);
                restaurant.setInspections(filled);
                //END
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

            //ImageView imageView = itemView.findViewById(R.id.item_restaurantIcon);
            //imageView.setImageResource();

            TextView nameText = itemView.findViewById(R.id.item_txt_restaurant_name);
            TextView issuesText = itemView.findViewById(R.id.item_txt_issues_found);
            TextView inspectionText = itemView.findViewById(R.id.item_txt_latest_inspection);
            nameText.setText(currentRestaurant.getName());

            if(currentInspectionList.getInspections().size() > 0) {
                Inspection latestInspection;
                latestInspection = Collections.max(currentInspectionList.getInspections());
                String issuesMessage = (latestInspection.getCritical() + latestInspection.getNonCritical()) + " issue(s)";
                issuesText.setText(issuesMessage);
                // code to find difference between dates from https://www.baeldung.com/java-date-difference
                Date currentDate = new Date();
                SimpleDateFormat formatter1 = new SimpleDateFormat("MMM yyyy");
                long diffInMillies = Math.abs(currentDate.getTime() - latestInspection.getDate().getTime());
                long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
                String inspectionDateText;
                if(diff > 365){
                    inspectionDateText = formatter1.format(latestInspection.getDate());
                }
                else if(diff > 30){
                    formatter1 = new SimpleDateFormat("MMM dd");
                    inspectionDateText = formatter1.format(latestInspection.getDate());
                }
                else{
                    inspectionDateText = diff + " days ago";
                }
                inspectionText.setText(inspectionDateText);
            }
            else{
                issuesText.setText(R.string.no_inspection_found);
                inspectionText.setText(R.string.no_inspection_found);
            }
            return itemView;
        }
    }

    private void registerCallBack(){
        ListView list = findViewById(R.id.listViewRestaurants);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {
                // Code to start Inspection List activity goes here, that activity does not exist yet so this is left empty for now
                Toast.makeText(RestaurantListActivity.this, "Open Inspection list activity for position: " + position, Toast.LENGTH_SHORT).show();
            }
        });
    }
}