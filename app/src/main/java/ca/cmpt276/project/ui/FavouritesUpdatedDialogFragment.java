package ca.cmpt276.project.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
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

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.favourites_dialog_layout, null);

        List<Restaurant> restaurantList = RestaurantListManager.getInstance().getFavourited();
        System.out.println(restaurantList.size() + "----SIZE IN FRAGMENT");

        ListView updated = view.findViewById(R.id.list_favourites);
        FavouritesUpdatedAdapter adapter = new FavouritesUpdatedAdapter(this.getContext(), restaurantList);

        updated.setAdapter(adapter);

        DialogInterface.OnDismissListener listener = new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                restaurantList.clear();
            }
        };

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.updated)
                .setView(view)
                .create();
    }
}
