package ca.cmpt276.project.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.TimeUnit;


import ca.cmpt276.project.R;
import ca.cmpt276.project.model.Inspection;
import ca.cmpt276.project.model.InspectionListManager;
import ca.cmpt276.project.model.Restaurant;
import ca.cmpt276.project.model.RestaurantListManager;
import ca.cmpt276.project.model.Violation;

import static java.lang.Math.abs;


public class InspectionDetailsActivity extends AppCompatActivity {

    private final static String INDEX = "Inspection Report Index";
    private final static String REST_INDEX = "Restaurant Index";
    private ListView listView;
    private  static RestaurantListManager restaurantManager;
    private static InspectionListManager inspectionManager;
    private static Inspection inspection;
    private static Restaurant restaurant;
    private static Violation violation;

    private ActionBar back;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inspection_details);
        Toolbar toolbar = findViewById(R.id.toolbar_inspection_det);
        setSupportActionBar(toolbar);

            back = getSupportActionBar();
        if (back != null) {
                back.setDisplayHomeAsUpEnabled(true);
            }


            restaurantManager = RestaurantListManager.getInstance();

            getData();
            setValues();
            populateListView();
            onClick();

        }

        private void populateListView() {

            Collections.sort(restaurantManager.getList());
            listView = (ListView) findViewById(R.id.listView);
            InspectionListManager inspectionMgr = new InspectionListManager();

            ArrayAdapter<Inspection> arrayAdapter = new ViolationListAdapter();

            listView.setAdapter(arrayAdapter);
        }

        private class ViolationListAdapter extends ArrayAdapter<Inspection> {
                    public ViolationListAdapter () {
                        super(InspectionDetailsActivity.this, R.layout.violation_list);
                    }

            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View itemView = convertView;
                if(itemView == null){
                    itemView = getLayoutInflater().inflate(R.layout.violation_list, parent, false);
                }

                violation = inspection.getViolation(position);

                ImageView violation_img = itemView.findViewById(R.id.img_hazard);
                TextView violation_txt = itemView.findViewById(R.id.txt_hazard);
                TextView brief_description_txt = itemView.findViewById(R.id.txt_brief_description);
                TextView crit_or_not_txt = itemView.findViewById(R.id.txt_critical_or_not);

                if(inspectionManager.getInspections().size()>0) {
                    //String brief_description;
                    String crit_or_not = violation.getSeverity().toString();
                    //brief_description_txt.setText(brief_description);
                    crit_or_not_txt.setText(crit_or_not);

                    switch (violation.getType()) {
                        case OPERATOR:
                            violation_txt.setText(R.string.operator_violation);
                            violation_txt.setTextColor(Color.parseColor("#000000"));
                            violation_img.setBackgroundResource(R.drawable.non_crit_operator_violations);
                            break;
                        case FOOD:
                            violation_txt.setText(R.string.food_violation);
                            violation_txt.setTextColor(Color.parseColor("#000000"));
                            violation_img.setBackgroundResource(R.drawable.crit_food_violations);
                            break;
                        case EQUIPMENT:
                            violation_txt.setText(R.string.equipment_violation);
                            violation_txt.setTextColor(Color.parseColor("#000000"));
                            violation_img.setBackgroundResource(R.drawable.crit_equiptment_violations);
                            break;
                        case PESTS:
                            violation_txt.setText(R.string.pest_violation);
                            violation_txt.setTextColor(Color.parseColor("#000000"));
                            violation_img.setBackgroundResource(R.drawable.non_crit_pest_violations);
                            break;
                        case EMPLOYEES:
                            violation_txt.setText(R.string.employee_violation);
                            violation_txt.setTextColor(Color.parseColor("#000000"));
                            violation_img.setBackgroundResource(R.drawable.non_crit_employee_violations);
                            break;
                        case ESTABLISHMENT:
                            violation_txt.setText(R.string.establishment_violation);
                            violation_txt.setTextColor(Color.parseColor("#000000"));
                            violation_img.setBackgroundResource(R.drawable.crit_employees_violations);
                            break;
                        default:
                            assert false;
                    }
                }
                else {
                    brief_description_txt.setText(R.string.no_inspection_found);
                    crit_or_not_txt.setText(R.string.no_inspection_found);
                }
                return itemView;
            }
        }

        private void setValues() {
            TextView inspectionDate_txt = findViewById(R.id.txt_critical_or_not);

            Date currentDate = new Date();
            SimpleDateFormat formatter1 = new SimpleDateFormat("MMM yyyy");
            long diffInMillies = Math.abs(currentDate.getTime() - inspection.getDate().getTime());
            long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
            String inspectionDateText;
            if(diff > 365){
                inspectionDateText = formatter1.format(inspection.getDate());
            }
            else if(diff > 30){
                formatter1 = new SimpleDateFormat("MMM dd");
                inspectionDateText = formatter1.format(inspection.getDate());
            }
            else{
                inspectionDateText = diff + " days ago";
            }
            inspectionDate_txt.setText(inspectionDateText);
            back.setTitle(restaurant.getName());
            TextView inspectionType_txt = findViewById(R.id.txt_inspection_type);
            String type = inspection.getType().toString();
            inspectionType_txt.setText(String.format(type));



            TextView hazard_txt = findViewById(R.id.txt_hazard);
            ImageView hazard_img = findViewById(R.id.img_hazard);
            switch (inspection.getLevel()) {
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

        }

        private void onClick() {
            ListView list = findViewById(R.id.listView);
            list.setOnItemClickListener((parent, viewClicked, position, id) -> {
                Toast.makeText(InspectionDetailsActivity.this, " full violation text goes here ", Toast.LENGTH_SHORT).show();
            });
        }
        public static Intent makeLaunchIntent(RestaurantDetailsActivity restaurantDetails, int position, int rest_position) {
        Intent intent = new Intent(restaurantDetails, InspectionDetailsActivity.class);
        intent.putExtra(INDEX, position);
        intent.putExtra(REST_INDEX, rest_position);
        return intent;
    }

    private void getData() {
        Intent intent = getIntent();
        int index = intent.getIntExtra(INDEX, 0);
        int rest_index = intent.getIntExtra(REST_INDEX, 0);
        restaurant = restaurantManager.getRestaurant(rest_index);
        inspectionManager = restaurant.getInspections();
        inspection = inspectionManager.getInspection(index);
    }
}
