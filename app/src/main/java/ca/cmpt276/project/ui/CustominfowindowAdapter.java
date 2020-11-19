package ca.cmpt276.project.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.Collections;

import ca.cmpt276.project.R;
import ca.cmpt276.project.model.Inspection;
import ca.cmpt276.project.model.Restaurant;
import ca.cmpt276.project.model.RestaurantListManager;

public class CustominfowindowAdapter implements GoogleMap.InfoWindowAdapter {

    private final View mwindow;
    private Context mcontext;
    private RestaurantListManager restaurantManager;

    public CustominfowindowAdapter(Context context) {
        mcontext = context;
        mwindow = LayoutInflater.from(context).inflate(R.layout.custom_infowindow, null);
    }

    private void renderWindow(Marker marker, View view){
        restaurantManager = RestaurantListManager.getInstance();
        Restaurant rest = restaurantManager.getRestaurant(Integer.parseInt(marker.getSnippet()));
        TextView rest_Title = (TextView) view.findViewById(R.id.txt_name);
        TextView rest_address = (TextView) view.findViewById(R.id.txt_add);
        TextView rest_severity = (TextView) view.findViewById(R.id.txt_severity);

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
        //add code to get restaurant logo
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
