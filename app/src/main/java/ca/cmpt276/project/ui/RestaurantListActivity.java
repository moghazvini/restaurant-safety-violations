package ca.cmpt276.project.ui;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.Menu;
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
import ca.cmpt276.project.model.DBAdapter;
import ca.cmpt276.project.model.Inspection;
import ca.cmpt276.project.model.InspectionListManager;
import ca.cmpt276.project.model.Restaurant;
import ca.cmpt276.project.model.RestaurantListManager;

/**
 * Displays the list of all restaurants in alphabetical order
 */
public class RestaurantListActivity extends AppCompatActivity {

    private RestaurantListManager restaurantManager;
    DBAdapter myDb;

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
        ArrayAdapter<Restaurant> adapter = new RestaurantListAdapter();
        ListView list = findViewById(R.id.listViewRestaurants);
        list.setAdapter(adapter);
    }

    private class RestaurantListAdapter extends ArrayAdapter<Restaurant>{
        public RestaurantListAdapter(){
            super(RestaurantListActivity.this, R.layout.restaurant_list_item, restaurantManager.getList());
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if(itemView == null){
                itemView = getLayoutInflater().inflate(R.layout.restaurant_list_item, parent, false);
            }
            // find restaurant
            Restaurant currentRestaurant = restaurantManager.getRestaurant(position);
            InspectionListManager currentInspectionList = currentRestaurant.getInspections();

            ImageView hazardImageView = itemView.findViewById(R.id.item_hazard_icon);
            ImageView restaurantLogo = itemView.findViewById(R.id.item_restaurantIcon);
            TextView nameText = itemView.findViewById(R.id.item_txt_restaurant_name);
            TextView issuesText = itemView.findViewById(R.id.item_txt_issues_found);
            TextView inspectionText = itemView.findViewById(R.id.item_txt_latest_inspection);
            TextView hazardText = itemView.findViewById(R.id.item_txt_hazard);
            nameText.setText(currentRestaurant.getName());

            if(currentInspectionList.getInspections().size() > 0) {
                Inspection latestInspection;
                latestInspection = Collections.max(currentInspectionList.getInspections());
                String issuesMessage = (latestInspection.getCritical() + latestInspection.getNonCritical()) + " issue(s)";
                issuesText.setText(issuesMessage);

                switch (latestInspection.getLevel()) {
                    case LOW:
                        hazardText.setText(R.string.hazard_low);
                        hazardText.setTextColor(Color.parseColor("#45DE08")); // Green
                        hazardImageView.setBackgroundResource(R.drawable.green_hazard);
                        break;
                    case MODERATE:
                        hazardText.setText(R.string.hazard_moderate);
                        hazardText.setTextColor(Color.parseColor("#FA9009")); // Orange
                        hazardImageView.setBackgroundResource(R.drawable.orange_hazard);
                        break;
                    case HIGH:
                        hazardText.setText(R.string.hazard_high);
                        hazardText.setTextColor(Color.parseColor("#FA2828")); // Red
                        hazardImageView.setBackgroundResource(R.drawable.red_hazard);
                        break;
                    default:
                        assert false;
                }

                if (currentRestaurant.getName().contains("7-Eleven")) {
                    restaurantLogo.setBackgroundResource(R.drawable.seveneleven);
                }
                else if (currentRestaurant.getName().contains("Sushi") || currentRestaurant.getName().contains("japanese")) {
                    restaurantLogo.setBackgroundResource(R.drawable.sushi_generic);
                }
                else if (currentRestaurant.getName().contains("Blenz")) {
                    restaurantLogo.setBackgroundResource(R.drawable.blenz);
                }
                else if (currentRestaurant.getName().contains("Booster Juice")) {
                    restaurantLogo.setBackgroundResource(R.drawable.boosterjuice);
                }
                else if (currentRestaurant.getName().contains("Boston Pizza")) {
                    restaurantLogo.setBackgroundResource(R.drawable.bostonpizza);
                }
                else if (currentRestaurant.getName().contains("Browns Socialhouse")) {
                    restaurantLogo.setBackgroundResource(R.drawable.boosterjuice);
                }
                else if (currentRestaurant.getName().contains("KFC")) {
                    restaurantLogo.setBackgroundResource(R.drawable.kfc_chicken);
                }
                else if (currentRestaurant.getName().contains("Little Caesars Pizza")) {
                    restaurantLogo.setBackgroundResource(R.drawable.littleceasers);
                }
                else if (currentRestaurant.getName().contains("McDonald's")) {
                    restaurantLogo.setBackgroundResource(R.drawable.mcdonalds);
                }
                else if (currentRestaurant.getName().contains("A&W") || currentRestaurant.getName().contains("A & W")) {
                    restaurantLogo.setBackgroundResource(R.drawable.a_and_w);
                }
                else if (currentRestaurant.getName().contains("Pizza Pizza")) {
                    restaurantLogo.setBackgroundResource(R.drawable.pizzapizza);
                }
                else if (currentRestaurant.getName().contains("Pizza Hut")) {
                    restaurantLogo.setBackgroundResource(R.drawable.pizza_hut);
                }
                else if (currentRestaurant.getName().contains("Pizza")) {
                    restaurantLogo.setBackgroundResource(R.drawable.generic_pizza);
                }
                else if (currentRestaurant.getName().contains("Catering")) {
                    restaurantLogo.setBackgroundResource(R.drawable.catering);
                }
                else if (currentRestaurant.getName().contains("Coffee")) {
                    restaurantLogo.setBackgroundResource(R.drawable.coffee);
                }
                else if (currentRestaurant.getName().contains("Pho") || currentRestaurant.getName().contains("Vietnamese")) {
                    restaurantLogo.setBackgroundResource(R.drawable.pho);
                }
                else if (currentRestaurant.getName().contains("Pub") || currentRestaurant.getName().contains("Bar")) {
                    restaurantLogo.setBackgroundResource(R.drawable.coffee);
                }
                else if (currentRestaurant.getName().contains("Market") || currentRestaurant.getName().contains("Grocery")) {
                    restaurantLogo.setBackgroundResource(R.drawable.market);
                }
                else {
                    restaurantLogo.setBackgroundResource(R.drawable.food2);
                }

                LocalDate currentDate = LocalDate.now();

                String inspectionDateText;
                if(Math.abs(currentDate.getYear() - latestInspection.getDate().getYear()) != 0){
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
                    inspectionDateText = formatter.format(latestInspection.getDate());
                }
                else if(Math.abs(currentDate.getMonthValue() - latestInspection.getDate().getMonthValue()) != 0){
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");
                    inspectionDateText = formatter.format(latestInspection.getDate());
                }
                else{
                    inspectionDateText = latestInspection.getDate().getDayOfMonth() + " days ago";
                }
                inspectionText.setText(inspectionDateText);
            }
            else{
                issuesText.setText(R.string.no_inspection_found);
                inspectionText.setText("");
            }
            return itemView;
        }
    }

    private void registerCallBack(){
        ListView list = findViewById(R.id.listViewRestaurants);
        list.setOnItemClickListener((parent, viewClicked, position, id) -> {
            Intent i = RestaurantDetailsActivity.makeLaunchIntent(RestaurantListActivity.this,position);
            startActivity(i);
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_list,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        switch (item.getItemId()){
            case R.id.action_map:
                startActivity(new Intent(this, MapsActivity.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}