package ca.cmpt276.project.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

import ca.cmpt276.project.R;
import ca.cmpt276.project.model.Inspection;
import ca.cmpt276.project.model.InspectionListManager;
import ca.cmpt276.project.model.Restaurant;
import ca.cmpt276.project.model.RestaurantListManager;

/**
 * Displays the details of a single restaurant and lists any inspections.
 */
public class RestaurantDetailsActivity extends AppCompatActivity {

    private final static String INDEX = "Inspection Report Index";
    private RestaurantListManager restaurantManager;
    private InspectionListManager inspectionManager;
    static Restaurant rest;
    private int rest_index;

    private ActionBar back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_details);
        Toolbar toolbar = findViewById(R.id.toolbar_restaurant_det);
        setSupportActionBar(toolbar);

        // Enable "up" on toolbar
        back = getSupportActionBar();
        if (back != null) {
            back.setDisplayHomeAsUpEnabled(true);
        }

        restaurantManager = RestaurantListManager.getInstance();
        GetData();
        setupGpsClick();
        populateList();
        setValues();
        OnClick();
        setupFavouriteClick();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            setResult(101,getIntent());
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void populateList() {
        inspectionManager.getInspections().sort(Collections.reverseOrder());
        ArrayAdapter<Inspection> adapter = new InspectionListAdapter();
        ListView list = findViewById(R.id.list_insp);
        TextView noInspections = findViewById(R.id.empty_inspections);
        list.setEmptyView(noInspections);
        list.setAdapter(adapter);
    }

    private class InspectionListAdapter extends ArrayAdapter<Inspection>{
        public InspectionListAdapter(){
            super(RestaurantDetailsActivity.this,R.layout.inspection_list,inspectionManager.getInspections());
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View iv = convertView;
            if(iv == null){
                iv = getLayoutInflater().inflate(R.layout.inspection_list, parent, false);
            }
            InspectionListManager insplist = rest.getInspections();
            Inspection insp = insplist.getInspection(position);

            ImageView hazard_img = iv.findViewById(R.id.img_hazard);
            TextView hazard_txt = iv.findViewById(R.id.txt_hazard);
            TextView critissues_txt = iv.findViewById(R.id.txt_critIssues);
            TextView Ncritissues_txt = iv.findViewById(R.id.txt_NcritIssues);
            TextView inspDate_txt = iv.findViewById(R.id.txt_inspDate);
            //setting stuff
            if(insplist.getInspections().size()>0) {
                String nonCrit = getString(R.string.critical_issue, insp.getCritical());
                String crit = getString(R.string.non_critical_issue, insp.getNonCritical());
                critissues_txt.setText(nonCrit);
                Ncritissues_txt.setText(crit);

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
                    default:
                        assert false;
                }

                LocalDate currentDate = LocalDate.now();

                String inspectionDateText;
                if(Math.abs(currentDate.getYear() - insp.getDate().getYear()) != 0){
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
                    inspectionDateText = formatter.format(insp.getDate());
                }
                else if(Math.abs(currentDate.getMonthValue() - insp.getDate().getMonthValue()) != 0){
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");
                    inspectionDateText = formatter.format(insp.getDate());
                }
                else{
                    inspectionDateText = insp.getDate().getDayOfMonth() + " days ago";
                }
                inspDate_txt.setText(inspectionDateText);
            }
            else {
                critissues_txt.setText(R.string.no_inspection_found);
                Ncritissues_txt.setText(R.string.no_inspection_found);
                inspDate_txt.setText(R.string.no_inspection_found);
            }
            return iv;
        }
    }

    private void setValues() {
        back.setTitle(rest.getName());
        TextView ResAdd_txt = findViewById(R.id.txt_restAdd);

        String address = getString(R.string.restaurant_add, rest.getAddress(), rest.getCity());
        ResAdd_txt.setText(address);

        TextView ResGps_txt = findViewById(R.id.txt_gps);
        String gps = getString(R.string.rest_gps, rest.getGpsLat(), rest.getGpsLong());
        ResGps_txt.setText(gps);
        ResGps_txt.setTextColor(Color.parseColor("#A576F1"));
        ResGps_txt.setTypeface(null, Typeface.ITALIC);

        ImageView favourite = findViewById(R.id.add_favourite);
        if (rest.isFavourite()) {
            favourite.setBackgroundResource(R.drawable.fav_color_2);
        } else {
            favourite.setBackgroundResource(R.drawable.not_fav_2);
        }
    }

    private void OnClick() {
        ListView insp_list = findViewById(R.id.list_insp);
        insp_list.setOnItemClickListener((parent, view, position, id) -> {
            Intent i = InspectionDetailsActivity.makeLaunchIntent(RestaurantDetailsActivity.this, position, rest_index);
            startActivity(i);
        });

    }

    private void setupFavouriteClick() {
        ImageView favourite = findViewById(R.id.add_favourite);
        favourite.setOnClickListener(v -> {
            rest.setFavourite(!rest.isFavourite());
            if (rest.isFavourite()) {
                favourite.setBackgroundResource(R.drawable.fav_color_2); //TODO colour error with fav_color
            } else {
                favourite.setBackgroundResource(R.drawable.not_fav_2);
            }
        });
    }

    private void setupGpsClick() {
        TextView gpsText = findViewById(R.id.txt_gps);
        gpsText.setOnClickListener(v -> {
            Intent i = MapsActivity.makeLaunchIntentMapsActivity(RestaurantDetailsActivity.this, rest);
            startActivity(i);
            finish();
        });
    }

    private void GetData() {
        Intent intent = getIntent();
        rest_index = intent.getIntExtra(INDEX,0);
        rest = restaurantManager.getRestaurant(rest_index);
        inspectionManager = rest.getInspections();
    }

    public static Intent makeLaunchIntent(RestaurantListActivity restaurantListActivity, int position) {
        Intent intent = new Intent(restaurantListActivity, RestaurantDetailsActivity.class);
        intent.putExtra(INDEX, position);
        return intent;
    }
    public static Intent makeLaunchIntent(MapsActivity mapsActivity, int position){
        Intent intent = new Intent(mapsActivity, RestaurantDetailsActivity.class);
        intent.putExtra(INDEX, position);
        return intent;
    }
}
