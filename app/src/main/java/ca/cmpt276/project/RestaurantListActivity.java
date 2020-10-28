package ca.cmpt276.project;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import ca.cmpt276.project.model.Inspection;
import ca.cmpt276.project.model.InspectionListManager;
import ca.cmpt276.project.model.Restaurant;
import ca.cmpt276.project.model.RestaurantListManager;
import ca.cmpt276.project.model.types.HazardLevel;
import ca.cmpt276.project.model.types.InspectionType;

public class RestaurantListActivity extends AppCompatActivity {
    private RestaurantListManager restaurantManager;
    private InspectionListManager inspectionManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_list);
        restaurantManager = RestaurantListManager.getInstance();
        inspectionManager = InspectionListManager.getInstance();
        fillRestaurantManager();

        populateListView();
        registerCallBack();
    }

    private void fillInspectionManager() {

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
                SimpleDateFormat formatter1 = new SimpleDateFormat("yyyyMMdd");
                Date date = formatter1.parse(tokens[1]);
                String stringType = tokens[2].replace("\"", "");
                InspectionType type = InspectionType.FOLLOWUP;
                if(stringType.equals("Routine")){
                    type = InspectionType.ROUTINE;
                }
                int numCritical = Integer.parseInt(tokens[3]);
                int numNonCritical = Integer.parseInt(tokens[4]);
                String stringHazardLevel = tokens[5].replace("\"", "");
                HazardLevel hazardLevel = HazardLevel.LOW;
                if(stringHazardLevel.equals("Moderate")){
                    hazardLevel = HazardLevel.MODERATE;
                }
                else if(stringHazardLevel.equals("High")){
                    hazardLevel = HazardLevel.HIGH;
                }
                Inspection inspection = new Inspection(date, type, numCritical, numNonCritical, hazardLevel);
                inspectionManager.add(inspection);
            }
        } catch(IOException | ParseException e){
            Log.wtf("RestaurantListActivity", "error reading data file on line " + line, e);
        }

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

                //
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

            ImageView imageView = itemView.findViewById(R.id.item_restaurantIcon);
            //imageView.setImageResource();

            TextView nameText = itemView.findViewById(R.id.item_txt_restaurant_name);
            nameText.setText(currentRestaurant.getName());
            TextView issuesText = itemView.findViewById(R.id.item_txt_issues_found);
            //issuesText.setText("issues found: " + );
            TextView inspectionText = itemView.findViewById(R.id.item_txt_latest_inspection);

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