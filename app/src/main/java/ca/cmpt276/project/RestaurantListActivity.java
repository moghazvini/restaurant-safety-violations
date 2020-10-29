package ca.cmpt276.project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import ca.cmpt276.project.model.Restaurant;
import ca.cmpt276.project.model.RestaurantListManager;

public class RestaurantListActivity extends AppCompatActivity {
    private RestaurantListManager restaurantManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        restaurantManager = RestaurantListManager.getInstance();
        fillRestaurantManager();
        populateListView();
        registerCallBack();
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
                restaurantManager.add(restaurant);
            }
        } catch(IOException e){
            Log.wtf("RestaurantListActivity", "error reading data file on line " + line, e);
        }
    }

    private void populateListView() {
        List<String> StringRestaurants = new ArrayList<>();
        String restaurantString;

        for(int i = 0; i < restaurantManager.getList().size(); i++){
            restaurantString = restaurantManager.getRestaurant(i).getName();
            StringRestaurants.add(restaurantString);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(RestaurantListActivity.this, R.layout.restaurant_list_item, StringRestaurants);

        ListView list = findViewById(R.id.listViewRestaurants);
        list.setAdapter(adapter);
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