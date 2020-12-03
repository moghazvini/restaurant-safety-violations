package ca.cmpt276.project.ui;

import android.content.Intent;
import android.database.Cursor;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import ca.cmpt276.project.R;
import ca.cmpt276.project.model.DBAdapter;
import ca.cmpt276.project.model.Inspection;
import ca.cmpt276.project.model.LocalDateAdapter;
import ca.cmpt276.project.model.Violation;

/**
 *  Displays details of a single inspection
 */
public class InspectionDetailsActivity extends AppCompatActivity {

    private final static String INDEX = "Inspection Report Index";
    private final static String REST_TRACKING = "Restaurant Tracking";
    private static Inspection inspection;
    private DBAdapter myDb;

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

        openDB();

        getData();
        setValues();
        populateListView();
        onClick();

    }

    private void populateListView() {
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

            String crit_or_not = violation.getSeverity().severity;
            boolean crit = crit_or_not.equals("Critical");

            if(crit){
                crit_or_not_txt.setTextColor(Color.parseColor("#FA2828")); // Red
            }else {
                crit_or_not_txt.setTextColor(Color.parseColor("#45DE08")); // Green
            }

            if(crit_or_not.equals("Critical")) {
                crit_or_not_txt.setText(R.string.critical);
            } else if (crit_or_not.equals("Non-Critical")) {
                crit_or_not_txt.setText(R.string.non_critical);
            }

            String violationType = "";
            switch (violation.getType()) {
                case OPERATOR:
                    violation_img.setBackgroundResource(R.drawable.non_crit_operator_violations);
                    violationType = getString(R.string.operator_violation);
                    break;
                case FOOD:
                    violationType = getString(R.string.food_violation);
                    if (crit) {
                        violation_img.setBackgroundResource(R.drawable.crit_food_violations);
                    } else {
                        violation_img.setBackgroundResource(R.drawable._07_212ncrit_food_violations);
                    }
                    break;
                case EQUIPMENT:
                    violationType = getString(R.string.equipment_violation);
                    if (crit) {
                        violation_img.setBackgroundResource(R.drawable.crit_equiptment_violations);
                    } else {
                        violation_img.setBackgroundResource(R.drawable._06_308_315ncrit_equipment_violations);
                    }
                    break;
                case PESTS:
                    violationType = getString(R.string.pest_violation);
                    violation_img.setBackgroundResource(R.drawable.non_crit_pest_violations);
                    break;
                case EMPLOYEES:
                    violationType = getString(R.string.employee_violation);
                    if (crit) {
                        violation_img.setBackgroundResource(R.drawable.crit_employees_violations);
                    } else {
                        violation_img.setBackgroundResource(R.drawable.non_crit_employee_violations);
                    }
                    break;
                case ESTABLISHMENT:
                    violationType = getString(R.string.establishment_violation);
                    violation_img.setBackgroundResource(R.drawable._01_104_violations);
                    break;
                case CHEMICAL:
                    violationType = getString(R.string.chemical_violation);
                    violation_img.setBackgroundResource(R.drawable._09ncrit_chems);
                    break;
                default:
                    assert false;
            }

            String brief_description = violation.getCode() +"  "+ violationType;
            brief_description_txt.setText(brief_description);
            return itemView;
        }
    }

    private void setValues() {
        TextView inspectionType_txt = findViewById(R.id.txt_inspection_type);
        TextView critissues = findViewById(R.id.txt_critissues);
        TextView Ncritissues = findViewById(R.id.txt_num_non_crtitical);

        // Format inspection date for the toolbar
        LocalDate inspectionDate = inspection.getDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        String inspectionDateText = formatter.format(inspectionDate);
        back.setTitle(inspectionDateText);

        TextView plain_hazard_txt = findViewById(R.id.plain_txt_hazard);
        plain_hazard_txt.setText(R.string.hazard_level);

        String type = inspection.getType().value;
        if(type.equals("Routine Check")) {
            inspectionType_txt.setText(R.string.routine_check);
        }
        if(type.equals("Follow-up Check")) {
            inspectionType_txt.setText(R.string.follow_up_check);
        }

        String nonCrit = getString(R.string.critical_issue, inspection.getCritical());
        String crit = getString(R.string.non_critical_issue, inspection.getNonCritical());

        critissues.setText(nonCrit);
        Ncritissues.setText(crit);

        TextView hazard_txt = findViewById(R.id.txt_hazard);
        ImageView hazard_img = findViewById(R.id.img_hazard);
        switch (inspection.getLevel()) {
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
                hazard_txt.setText(R.string.hazard_low);
                hazard_txt.setTextColor(Color.parseColor("#45DE08")); // Green
                hazard_img.setBackgroundResource(R.drawable.green_hazard);
        }

    }

    private void onClick() {
        ListView list = findViewById(R.id.listView);
        list.setOnItemClickListener((parent, viewClicked, position, id) -> Toast.makeText(InspectionDetailsActivity.this, inspection.getViolation(position).getLongDis(), Toast.LENGTH_LONG).show());
    }

    public static Intent makeLaunchIntent(RestaurantDetailsActivity restaurantDetails, int position, String tracking) {
        Intent intent = new Intent(restaurantDetails, InspectionDetailsActivity.class);
        intent.putExtra(INDEX, position);
        intent.putExtra(REST_TRACKING, tracking);
        return intent;
    }

    private void getData() {
        Intent intent = getIntent();
        int index = intent.getIntExtra(INDEX, 0);
        String rest_tracking = intent.getStringExtra(REST_TRACKING);
        getRestaurantFromTracking(rest_tracking, index);
    }

    public void getRestaurantFromTracking(String tracking, int index){
        Cursor restaurantCursor = myDb.searchRestaurants(DBAdapter.KEY_TRACKING, tracking, DBAdapter.MatchString.EQUALS);
        if(restaurantCursor.moveToFirst()){
            inspection = extractInspectionList(restaurantCursor, index);
        }
    }

    private Inspection extractInspectionList(Cursor cursor, int index){
        Gson gson = new GsonBuilder().registerTypeAdapter(LocalDate.class, new LocalDateAdapter().nullSafe()).create();
        Type type = new TypeToken<ArrayList<Inspection>>() {}.getType();
        String outputString = cursor.getString(DBAdapter.COL_INSPECTION_LIST);
        ArrayList<Inspection> inspectionsArray = gson.fromJson(outputString, type);
        return inspectionsArray.get(index);
    }

    private void openDB() {
        myDb = new DBAdapter(this);
        myDb.open();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        myDb.close();
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
