package ca.cmpt276.project.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.Collections;

import ca.cmpt276.project.R;
import ca.cmpt276.project.model.Inspection;
import ca.cmpt276.project.model.Restaurant;
import ca.cmpt276.project.model.RestaurantListManager;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private final View mwindow;
    private Context mcontext;
    private RestaurantListManager restaurantManager;

    public CustomInfoWindowAdapter(Context context) {
        mcontext = context;
        mwindow = LayoutInflater.from(context).inflate(R.layout.custom_infowindow, null);
    }

    private void renderWindow(Marker marker, View view){
        restaurantManager = RestaurantListManager.getInstance();
        Restaurant rest = restaurantManager.getRestaurant(Integer.parseInt(marker.getSnippet()));
        TextView rest_Title = (TextView) view.findViewById(R.id.txt_name);
        TextView rest_address = (TextView) view.findViewById(R.id.txt_add);
        TextView rest_severity = (TextView) view.findViewById(R.id.txt_severity);
        ImageView img_icon = view.findViewById(R.id.img_icon);

        if(!rest.getName().equals("")){
            rest_Title.setText(rest.getName());
        }
        if (!rest.getAddress().isEmpty()){
            rest_address.setText(rest.getAddress());
        }

        if(rest.getInspections().getInspections().size() > 0) {
            Inspection latestInspection = Collections.max(rest.getInspections().getInspections());
            switch (latestInspection.getLevel()) {
                case LOW:
                    rest_severity.setText(R.string.hazard_low);
                    rest_severity.setTextColor(Color.parseColor("#45DE08")); // Green
                    break;
                case MODERATE:
                    rest_severity.setText(R.string.hazard_moderate);
                    rest_severity.setTextColor(Color.parseColor("#FA9009")); // Orange
                    break;
                case HIGH:
                    rest_severity.setText(R.string.hazard_high);
                    rest_severity.setTextColor(Color.parseColor("#FA2828")); // Red
                    break;
                default:
                    assert false;
            }
        }
        if (rest.getName().contains("7-Eleven")) {
            img_icon.setBackgroundResource(R.drawable.seveneleven);
        }
        else if (rest.getName().contains("Sushi") || rest.getName().contains("japanese")) {
            img_icon.setBackgroundResource(R.drawable.sushi_generic);
        }
        else if (rest.getName().contains("Blenz")) {
            img_icon.setBackgroundResource(R.drawable.blenz);
        }
        else if (rest.getName().contains("Booster Juice")) {
            img_icon.setBackgroundResource(R.drawable.boosterjuice);
        }
        else if (rest.getName().contains("Boston Pizza")) {
            img_icon.setBackgroundResource(R.drawable.bostonpizza);
        }
        else if (rest.getName().contains("Browns Socialhouse")) {
            img_icon.setBackgroundResource(R.drawable.boosterjuice);
        }
        else if (rest.getName().contains("KFC")) {
            img_icon.setBackgroundResource(R.drawable.kfc_chicken);
        }
        else if (rest.getName().contains("Little Caesars Pizza")) {
            img_icon.setBackgroundResource(R.drawable.littleceasers);
        }
        else if (rest.getName().contains("McDonald's")) {
            img_icon.setBackgroundResource(R.drawable.mcdonalds);
        }
        else if (rest.getName().contains("A&W") || rest.getName().contains("A & W")) {
            img_icon.setBackgroundResource(R.drawable.a_and_w);
        }
        else if (rest.getName().contains("Pizza Pizza")) {
            img_icon.setBackgroundResource(R.drawable.pizzapizza);
        }
        else if (rest.getName().contains("Pizza Hut")) {
            img_icon.setBackgroundResource(R.drawable.pizza_hut);
        }
        else if (rest.getName().contains("Pizza")) {
            img_icon.setBackgroundResource(R.drawable.generic_pizza);
        }
        else if (rest.getName().contains("Catering")) {
            img_icon.setBackgroundResource(R.drawable.catering);
        }
        else if (rest.getName().contains("Coffee")) {
            img_icon.setBackgroundResource(R.drawable.coffee);
        }
        else if (rest.getName().contains("Pho") || rest.getName().contains("Vietnamese")) {
            img_icon.setBackgroundResource(R.drawable.pho);
        }
        else if (rest.getName().contains("Pub") || rest.getName().contains("Bar")) {
            img_icon.setBackgroundResource(R.drawable.coffee);
        }
        else if (rest.getName().contains("Market") || rest.getName().contains("Grocery")) {
            img_icon.setBackgroundResource(R.drawable.market);
        }
        else {
            img_icon.setBackgroundResource(R.drawable.food2);
        }
    }


    @Override
    public View getInfoWindow(Marker marker) {
        renderWindow(marker,mwindow);
        return mwindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
        renderWindow(marker,mwindow);
        return mwindow;
    }
}
