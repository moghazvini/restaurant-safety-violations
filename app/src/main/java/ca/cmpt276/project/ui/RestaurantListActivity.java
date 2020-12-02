package ca.cmpt276.project.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

import ca.cmpt276.project.R;
import ca.cmpt276.project.model.DBAdapter;
import ca.cmpt276.project.model.Inspection;
import ca.cmpt276.project.model.InspectionListManager;
import ca.cmpt276.project.model.Restaurant;
import ca.cmpt276.project.model.RestaurantListManager;

/**
 * Displays the list of all restaurants in alphabetical order
 */
public class RestaurantListActivity extends AppCompatActivity implements SearchDialogFragment.SearchDialogListener {

    public static final int REQUEST_CODE_DETAILS = 101;
    private RestaurantListManager restaurantManager;
    private RestaurantListCursorAdapter adapter;
    Cursor cursor;
    DBAdapter myDb;
    private static final String PREFS_NAME = "AppPrefs";
    private static final String NAME_FILTER_PREF = "name shared pref";
    private static final String HAZARD_FILTER_PREF = "hazard shared pref";
    private static final String NUM_FILTER_PREF = "num critical shared pref";
    private static final String LESS_FILTER_PREF = "less shared pref";
    private static final String FAV_FILTER_PREF = "favourite shared pref";
    private String searchTerm;
    private String hazardFilter;
    private int numCriticalFilter;
    private boolean favFilter;
    private String lessMore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_list);
        Toolbar toolbar = findViewById(R.id.toolbar_restaurant_list);
        setSupportActionBar(toolbar);

        restaurantManager = RestaurantListManager.getInstance();
        openDB();

        popListViewDB();
        registerCallBack();
        // Check if it has been 20 hours since last check
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

    private void popListViewDB() {
        cursor = getFilterPrefs();
        cursor.moveToFirst();
        ListView list = findViewById(R.id.listViewRestaurants);
        adapter = new RestaurantListCursorAdapter(this,cursor,false);
        list.setAdapter(adapter);
    }

    private Cursor getFilterPrefs(){
        SharedPreferences prefs = this.getSharedPreferences(PREFS_NAME, 0);
        searchTerm = prefs.getString(NAME_FILTER_PREF, "");
        hazardFilter = prefs.getString(HAZARD_FILTER_PREF, "OFF");
        numCriticalFilter = prefs.getInt(NUM_FILTER_PREF, -1);
        lessMore = prefs.getString(LESS_FILTER_PREF, "OFF");
        favFilter = prefs.getBoolean(FAV_FILTER_PREF, false);
        Cursor cursor = myDb.filterRestaurants(searchTerm, hazardFilter, numCriticalFilter, lessMore, favFilter);
        if(cursor != null){
            return cursor;
        } else {
            return myDb.getAllRows();
        }

    }

    private void registerCallBack(){
        ListView list = findViewById(R.id.listViewRestaurants);
        list.setOnItemClickListener((parent, viewClicked, position, id) -> {
            Cursor cursor = myDb.getRestaurantRow(id);
            String tracking = cursor.getString(DBAdapter.COL_TRACKING);
            Intent i = RestaurantDetailsActivity.makeLaunchIntent(RestaurantListActivity.this, tracking);
            startActivityForResult(i, REQUEST_CODE_DETAILS);
        });
    }

    @Override
    public void sendSearchInput(String name, String hazard_filter, int num_critical_filter, String lessMore, boolean favFilter,boolean reset) {
        if((name.length() > 0 || hazard_filter.length() > 0 || num_critical_filter > 0) && (!reset)) {
            Cursor relevantRowsCursor = myDb.filterRestaurants(name, hazard_filter, num_critical_filter, lessMore, favFilter);
            if (relevantRowsCursor != null) {
                adapter.changeCursor(relevantRowsCursor);
            }
        } else if (reset){
            displayAllRows();
        }
    }

    private void displayAllRows() {
        Cursor c = myDb.getAllRows();
        c.moveToFirst();
        adapter.changeCursor(c);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_DETAILS) {
            displayAllRows();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_list,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        if (item.getItemId() == R.id.action_map) {
            startActivity(new Intent(this, MapsActivity.class));
            finish();
            return true;
        } else if(item.getItemId() ==  R.id.action_search){
            FragmentManager manager = getSupportFragmentManager();
            SearchDialogFragment dialog = new SearchDialogFragment(); // open search
            dialog.show(manager, "SearchDialog");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}