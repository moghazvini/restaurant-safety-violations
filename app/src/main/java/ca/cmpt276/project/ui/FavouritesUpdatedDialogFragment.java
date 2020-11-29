package ca.cmpt276.project.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.List;

import ca.cmpt276.project.R;
import ca.cmpt276.project.model.Restaurant;
import ca.cmpt276.project.model.RestaurantListManager;

public class FavouritesUpdatedDialogFragment extends AppCompatDialogFragment {
    public static final String RESTAURANT_LIST_KEY = "favourites updated";

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.favourites_dialog_layout, null);

        List<Restaurant> restaurantList = RestaurantListManager.getInstance().getFavourited();

        ListView updated = view.findViewById(R.id.list_favourites);
        RestaurantListAdapter adapter = new RestaurantListAdapter(this.getContext(), restaurantList);
        if (adapter != null || updated != null) {
            updated.setAdapter(adapter);
        }

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.updated)
                .setView(view)
                .create();
    }
}
