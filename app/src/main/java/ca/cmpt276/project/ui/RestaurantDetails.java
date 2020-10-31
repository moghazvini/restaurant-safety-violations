package ca.cmpt276.project.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import ca.cmpt276.project.R;
import ca.cmpt276.project.model.Inspection;
import ca.cmpt276.project.model.Restaurant;
import ca.cmpt276.project.model.RestaurantListManager;

public class RestaurantDetails extends AppCompatActivity {

    private static String INDEX = "0";
    RestaurantListManager restaurantManager;
    static Restaurant rest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_details);

        restaurantManager = RestaurantListManager.getInstance();

        Getdata();
        populateList();
        setValues();
    }

    private void populateList() {
        ArrayAdapter<Inspection> adapter = new InspectionListAdapter();
        ListView list = findViewById(R.id.list_insp);
        list.setAdapter(adapter);
    }

    private class InspectionListAdapter extends ArrayAdapter<Inspection>{
        public InspectionListAdapter(){
            super(RestaurantDetails.this,R.layout.inspection_list);
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View iv = convertView;
            if(iv == null)
        }

        }

    private void setValues() {
        TextView ResName_txt = findViewById(R.id.txt_restname);
        ResName_txt.setText(rest.getName());
        TextView ResAdd_txt = findViewById(R.id.txt_restAdd);
        ResAdd_txt.setText("Facility Location: \n"+rest.getAddress());
        TextView ResGps_txt = findViewById(R.id.txt_gps);
        ResGps_txt.setText(rest.getGPS());
    }

    private void Getdata() {
        Intent intent = getIntent();
        int rest_index = intent.getIntExtra(INDEX,0);
        rest = restaurantManager.getRestaurant(rest_index);
    }

    public static Intent makeLaunchIntent(RestaurantListActivity restaurantListActivity, int position) {
        Intent intent = new Intent(restaurantListActivity, RestaurantDetails.class);
        intent.putExtra(INDEX, position);
        return intent;
    }
}