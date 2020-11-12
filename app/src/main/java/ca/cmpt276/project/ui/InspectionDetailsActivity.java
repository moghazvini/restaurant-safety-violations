package ca.cmpt276.project.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;


import ca.cmpt276.project.R;
import ca.cmpt276.project.model.Inspection;
import ca.cmpt276.project.model.InspectionListManager;
import ca.cmpt276.project.model.Restaurant;
import ca.cmpt276.project.model.RestaurantListManager;
import ca.cmpt276.project.model.Violation;

/**
 *  Displays details of a single inspection
 */
public class InspectionDetailsActivity extends AppCompatActivity {

    private final static String INDEX = "Inspection Report Index";
    private final static String REST_INDEX = "Restaurant Index";
    private  static RestaurantListManager restaurantManager;
    private static InspectionListManager inspectionManager;
    private static Inspection inspection;

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
        ListView listView = findViewById(R.id.listView);

        ArrayAdapter<Violation> arrayAdapter = new ViolationListAdapter();
        TextView noViolations = findViewById(R.id.empty_violations);
        listView.setEmptyView(noViolations);

        listView.setAdapter(arrayAdapter);
    }

    private class ViolationListAdapter extends ArrayAdapter<Violation> {
                public ViolationListAdapter () {
                    super(InspectionDetailsActivity.this, R.layout.violation_list, inspection.getViolations());
                }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View itemView = convertView;
            if(itemView == null){
                itemView = getLayoutInflater().inflate(R.layout.violation_list, parent, false);
            }

            Violation violation = inspection.getViolation(position);

            ImageView violation_img = itemView.findViewById(R.id.img_hazard);
            TextView brief_description_txt = itemView.findViewById(R.id.txt_critissues);
            TextView crit_or_not_txt = itemView.findViewById(R.id.txt_critical_or_not);

            if(inspectionManager.getInspections().size()>0) {
                String brief_description = violation.getCode() +"  "+ violation.getType().violation;
                String crit_or_not = violation.getSeverity().severity;
                brief_description_txt.setText(brief_description);
                if(crit_or_not.equals("Critical")){
                    crit_or_not_txt.setTextColor(Color.parseColor("#FA2828")); // Red
                }else {
                    crit_or_not_txt.setTextColor(Color.parseColor("#45DE08")); // Green
                }
                crit_or_not_txt.setText(crit_or_not);

                switch (violation.getType()) {
                    case OPERATOR:
                        violation_img.setBackgroundResource(R.drawable.non_crit_operator_violations);
                        break;
                    case FOOD:
                        violation_img.setBackgroundResource(R.drawable.crit_food_violations);
                        break;
                    case EQUIPMENT:
                        violation_img.setBackgroundResource(R.drawable.crit_equiptment_violations);
                        break;
                    case PESTS:
                        violation_img.setBackgroundResource(R.drawable.non_crit_pest_violations);
                        break;
                    case EMPLOYEES:
                        violation_img.setBackgroundResource(R.drawable.non_crit_employee_violations);
                        break;
                    case ESTABLISHMENT:
                        violation_img.setBackgroundResource(R.drawable.crit_employees_violations);
                        break;
                    case CHEMICAL:
                        violation_img.setBackgroundResource(R.drawable._09ncrit_chems);
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
        LocalDate currentDate = LocalDate.now();
        String inspectionDateText;
        if(Math.abs(currentDate.getYear() - inspection.getDate().getYear()) != 0){
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
            inspectionDateText = formatter.format(inspection.getDate());
        }
        else if(Math.abs(currentDate.getMonthValue() - inspection.getDate().getMonthValue()) != 0){
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");
            inspectionDateText = formatter.format(inspection.getDate());
        }
        else{
            inspectionDateText = inspection.getDate().getDayOfMonth() + " days ago";
        }

        TextView inspectionType_txt = findViewById(R.id.txt_inspection_type);
        TextView critissues = findViewById(R.id.txt_critissues);
        TextView Ncritissues = findViewById(R.id.txt_num_non_crtitical);

        back.setTitle(inspectionDateText);
        String type = inspection.getType().value;
        inspectionType_txt.setText(type);
        String nonCrit = "# of Critical Issues: " + inspection.getCritical();
        String crit = "# of Non-Critical Issues: " + inspection.getNonCritical();

        critissues.setText(nonCrit);
        Ncritissues.setText(crit);

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
        list.setOnItemClickListener((parent, viewClicked, position, id) -> Toast.makeText(InspectionDetailsActivity.this, inspection.getViolation(position).getLongDis(), Toast.LENGTH_LONG).show());
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
        Restaurant restaurant = restaurantManager.getRestaurant(rest_index);
        inspectionManager = restaurant.getInspections();
        inspection = inspectionManager.getInspection(index);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}