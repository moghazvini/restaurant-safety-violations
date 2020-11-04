package ca.cmpt276.project.ui;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import java.util.Collections;


import ca.cmpt276.project.R;
import ca.cmpt276.project.model.Inspection;
import ca.cmpt276.project.model.InspectionListManager;
import ca.cmpt276.project.model.Restaurant;
import ca.cmpt276.project.model.RestaurantListManager;


public class InspectionDetailsActivity extends AppCompatActivity {

     private final static String INDEX = "Index";
     private ListView listView;
    private  static RestaurantListManager restaurantManager;
    private static InspectionListManager inspectionManager;
    private static Inspection inspection;
    static Restaurant restaurant;

    private ActionBar back;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inspection_details);
        Toolbar toolbar = findViewById(R.id.toolbar_restaurant_det);
        setSupportActionBar(toolbar);


        back = getSupportActionBar();
        if (back != null) {
            back.setDisplayHomeAsUpEnabled(true);
        }


        restaurantManager = RestaurantListManager.getInstance();

       // setData();
        setValues();
        populateListView();
        onClick();
        
    }

    private void populateListView() {
        Collections.sort(restaurantManager.getList());
        listView = (ListView) findViewById(R.id.listView);
        InspectionListManager inspectionMgr = new InspectionListManager();

        ArrayAdapter<Inspection> arrayAdapter = new ArrayAdapter<Inspection>(
                this,
                android.R.layout.simple_list_item_1,
                inspectionMgr.getInspections() );

        listView.setAdapter(arrayAdapter);
    }

   private void setValues() {
        TextView inspectionDate_txt = findViewById(R.id.txt_inspDate);
        inspectionDate_txt.setText(inspection.getDate().toString());
        back.setTitle(restaurant.getName());
        TextView inspectionType_txt = findViewById(R.id.txt_inspection_type);
        String type = inspection.getType().toString();
        inspectionType_txt.setText(String.format(type));
        TextView critIssues_txt = findViewById(R.id.txt_num_critical);
        critIssues_txt.setText(String.format("Critical issues: %d", inspection.getCritical()));
        TextView nonCritIssues_txt = findViewById(R.id.txt_num_non_crtitical);
        nonCritIssues_txt.setText(String.format("Non-critical issues: %d", inspection.getNonCritical()));

    }

    private void onClick(){
        ListView list = findViewById(R.id.listView);
        list.setOnItemClickListener((parent, viewClicked, position, id) -> {

            Toast.makeText(InspectionDetailsActivity.this, "Open Inspection list activity for position: " + position, Toast.LENGTH_SHORT).show();

        });
    }

    public static Intent Launch(RestaurantDetails restaurantDetails, int position) {
        Intent intent = new Intent(restaurantDetails, InspectionDetailsActivity.class);
        intent.putExtra(INDEX, position);
        restaurantManager = restaurantManager.getInstance();
        restaurant = restaurantManager.getRestaurant(position);
        inspectionManager = restaurant.getInspections();
        inspection = inspectionManager.getInspection(position);

        return intent;
    }
    }


