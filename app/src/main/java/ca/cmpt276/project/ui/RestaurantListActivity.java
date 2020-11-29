package ca.cmpt276.project.ui;

import android.content.Intent;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import ca.cmpt276.project.R;
import ca.cmpt276.project.model.DBAdapter;
import ca.cmpt276.project.model.Restaurant;
import ca.cmpt276.project.model.RestaurantListManager;

/**
 * Displays the list of all restaurants in alphabetical order
 */
public class RestaurantListActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_DETAILS = 101;
    private RestaurantListManager restaurantManager;
    private ArrayAdapter<Restaurant> adapter;
    private DBAdapter myDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_list);
        Toolbar toolbar = findViewById(R.id.toolbar_restaurant_list);
        setSupportActionBar(toolbar);

        restaurantManager = RestaurantListManager.getInstance();
        openDB();

        populateListView();
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

    private void populateListView() {
        //code to sort alphabetically taken from https://www.youtube.com/watch?v=dZQqrPdqT1E
        //Collections.sort(restaurantManager.getList());
        Cursor cursor = myDb.getAllRows();

        adapter = new RestaurantListAdapter(RestaurantListActivity.this,restaurantManager.getList());
        ListView list = findViewById(R.id.listViewRestaurants);
        list.setAdapter(adapter);
    }

    private void registerCallBack(){
        ListView list = findViewById(R.id.listViewRestaurants);
        list.setOnItemClickListener((parent, viewClicked, position, id) -> {
            Intent i = RestaurantDetailsActivity.makeLaunchIntent(RestaurantListActivity.this,position);
            startActivityForResult(i, REQUEST_CODE_DETAILS);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_DETAILS) {
            adapter.notifyDataSetChanged();
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
        }
        return super.onOptionsItemSelected(item);
    }

}