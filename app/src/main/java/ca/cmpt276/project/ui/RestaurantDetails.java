package ca.cmpt276.project.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import ca.cmpt276.project.R;
import ca.cmpt276.project.model.Inspection;
import ca.cmpt276.project.model.InspectionListManager;
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
        OnClick();
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
            if(iv == null){
                iv = getLayoutInflater().inflate(R.layout.inspection_list, parent, false);
            }
            Inspection insp = rest.getInspections().getInspection(position);
            ImageView hazard_img = iv.findViewById(R.id.img_hazard);
            TextView hazard_txt = iv.findViewById(R.id.txt_hazard);
            TextView critissues_txt = iv.findViewById(R.id.txt_critIssues);
            TextView Ncritissues_txt = iv.findViewById(R.id.txt_NcritIssues);
            TextView inspDate_txt = iv.findViewById(R.id.txt_inspDate);
            //setting stuff
            critissues_txt.setText("# of Critical Issues: " + insp.getCritical());
            Ncritissues_txt.setText("# of Non-Critical Issues: " + insp.getNonCritical());

            switch (insp.getLevel()) {
                case LOW:
                    hazard_txt.setText(R.string.hazard_low);
                    hazard_txt.setTextColor(Color.parseColor("#45DE08")); // Green
                    hazard_img.setBackgroundResource(R.drawable.green_hazard);
                    break;
                case MODERATE:
                    hazard_txt.setText(R.string.hazard_moderate);
                    hazard_txt.setTextColor(Color.parseColor("#FA9009")); // Orange
                    hazard_img.setBackgroundResource(R.drawable.orange_hazard);
                    break;
                case HIGH:
                    hazard_txt.setText(R.string.hazard_high);
                    hazard_txt.setTextColor(Color.parseColor("#FA2828")); // Red
                    hazard_img.setBackgroundResource(R.drawable.red_hazard);
                    break;
            }
            Date currentDate = new Date();
            SimpleDateFormat formatter1 = new SimpleDateFormat("MMM yyyy");
            long diffInMillies = Math.abs(currentDate.getTime() - insp.getDate().getTime());
            long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
            String inspectionDateText;
            if(diff > 365){
                inspectionDateText = formatter1.format(insp.getDate());
            } else if(diff > 30){
                formatter1 = new SimpleDateFormat("MMM dd");
                inspectionDateText = formatter1.format(insp.getDate());
            } else{
                inspectionDateText = diff + " days ago";
            }
            inspDate_txt.setText(inspectionDateText);
            return iv;
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

    private void OnClick() {
        ListView insp_list = findViewById(R.id.list_insp);
        insp_list.setOnItemClickListener(((parent, view, position, id) -> {
            Toast.makeText(RestaurantDetails.this, "Open Inspection details activity for position: " + position, Toast.LENGTH_SHORT).show();
        }));
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