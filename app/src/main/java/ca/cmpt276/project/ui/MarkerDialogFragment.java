package ca.cmpt276.project.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.Collections;

import ca.cmpt276.project.R;
import ca.cmpt276.project.model.Inspection;
import ca.cmpt276.project.model.Restaurant;
import ca.cmpt276.project.model.RestaurantListManager;

/**
 * Marker information Pop Up on the map
 */
public class MarkerDialogFragment extends AppCompatDialogFragment {
    private static final String TAG = "DialogFragmentTag";
    private PopUpDialogListener popUpListener;

    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.custom_infowindow, null);

        TextView restaurant_Title = (TextView) view.findViewById(R.id.txt_name);
        TextView restaurant_address = (TextView) view.findViewById(R.id.txt_add);
        TextView restaurant_severity = (TextView) view.findViewById(R.id.txt_severity);
        ImageView img_icon = view.findViewById(R.id.img_icon);

        RestaurantListManager restaurantListManager = RestaurantListManager.getInstance();
        int index = getArguments().getInt("index");
        Restaurant restaurant = restaurantListManager.getRestaurant(index);

        restaurant_Title.setText(restaurant.getName());
        restaurant_address.setText(restaurant.getAddress());

        if(restaurant.getInspections().getInspections().size() > 0) {
            Inspection latestInspection = Collections.max(restaurant.getInspections().getInspections());
            switch (latestInspection.getLevel()) {
                case LOW:
                    restaurant_severity.setText(R.string.hazard_low);
                    restaurant_severity.setTextColor(Color.parseColor("#45DE08")); // Green
                    break;
                case MODERATE:
                    restaurant_severity.setText(R.string.hazard_moderate);
                    restaurant_severity.setTextColor(Color.parseColor("#FA9009")); // Orange
                    break;
                case HIGH:
                    restaurant_severity.setText(R.string.hazard_high);
                    restaurant_severity.setTextColor(Color.parseColor("#FA2828")); // Red
                    break;
                default:
                    assert false;
            }
        }
        if (restaurant.getName().contains("7-Eleven")) {
            img_icon.setBackgroundResource(R.drawable.seveneleven);
        }
        else if (restaurant.getName().contains("Sushi") || restaurant.getName().contains("japanese")) {
            img_icon.setBackgroundResource(R.drawable.sushi_generic);
        }
        else if (restaurant.getName().contains("Blenz")) {
            img_icon.setBackgroundResource(R.drawable.blenz);
        }
        else if (restaurant.getName().contains("Booster Juice")) {
            img_icon.setBackgroundResource(R.drawable.boosterjuice);
        }
        else if (restaurant.getName().contains("Boston Pizza")) {
            img_icon.setBackgroundResource(R.drawable.bostonpizza);
        }
        else if (restaurant.getName().contains("Browns Socialhouse")) {
            img_icon.setBackgroundResource(R.drawable.boosterjuice);
        }
        else if (restaurant.getName().contains("KFC")) {
            img_icon.setBackgroundResource(R.drawable.kfc_chicken);
        }
        else if (restaurant.getName().contains("Little Caesars Pizza")) {
            img_icon.setBackgroundResource(R.drawable.littleceasers);
        }
        else if (restaurant.getName().contains("McDonald's")) {
            img_icon.setBackgroundResource(R.drawable.mcdonalds);
        }
        else if (restaurant.getName().contains("A&W") || restaurant.getName().contains("A & W")) {
            img_icon.setBackgroundResource(R.drawable.a_and_w);
        }
        else if (restaurant.getName().contains("Pizza Pizza")) {
            img_icon.setBackgroundResource(R.drawable.pizzapizza);
        }
        else if (restaurant.getName().contains("Pizza Hut")) {
            img_icon.setBackgroundResource(R.drawable.pizza_hut);
        }
        else if (restaurant.getName().contains("Pizza")) {
            img_icon.setBackgroundResource(R.drawable.generic_pizza);
        }
        else if (restaurant.getName().contains("Catering")) {
            img_icon.setBackgroundResource(R.drawable.catering);
        }
        else if (restaurant.getName().contains("Coffee")) {
            img_icon.setBackgroundResource(R.drawable.coffee);
        }
        else if (restaurant.getName().contains("Pho") || restaurant.getName().contains("Vietnamese")) {
            img_icon.setBackgroundResource(R.drawable.pho);
        }
        else if (restaurant.getName().contains("Pub") || restaurant.getName().contains("Bar")) {
            img_icon.setBackgroundResource(R.drawable.coffee);
        }
        else if (restaurant.getName().contains("Market") || restaurant.getName().contains("Grocery")) {
            img_icon.setBackgroundResource(R.drawable.market);
        }
        else {
            img_icon.setBackgroundResource(R.drawable.food2);
        }

        view.setOnClickListener(v -> popUpListener.popUp(index));

        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            popUpListener = (PopUpDialogListener) context;
        } catch (ClassCastException e) {
            Log.e(TAG, "On attach: ClassCastException" + e.getMessage());
        }
    }

    public interface PopUpDialogListener{
        void popUp(int index);
    }
}
