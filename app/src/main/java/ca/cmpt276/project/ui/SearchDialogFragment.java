package ca.cmpt276.project.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.appcompat.widget.SwitchCompat;

import ca.cmpt276.project.R;

public class SearchDialogFragment extends AppCompatDialogFragment {
    
    private SearchDialogListener dialogListener;
    private static final String TAG = "SearchDialogTag";
    View v;
    private String searchTerm;
    private String hazardFilter;
    private int numCriticalFilter;
    private boolean favFilter;
    private String lessMore;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // code for alert dialog via fragment from Dr Brian Fraser's video https://www.youtube.com/watch?v=y6StJRn-Y-A
        v = LayoutInflater.from(getActivity()).inflate(R.layout.search_dialog_layout, null);
        setupHazardRadioButtons();
        setupCriticalRadioButtons();
        setupFavFilter();
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(which){
                    case DialogInterface.BUTTON_POSITIVE:
                        EditText searchTxt = v.findViewById(R.id.inputSearchBar);
                        EditText criticalTxt = v.findViewById(R.id.inputCriticalFilter);
                        String stringCritical = criticalTxt.getText().toString();
                        if(stringCritical.length() > 0) {
                            numCriticalFilter = Integer.parseInt(criticalTxt.getText().toString());
                        } else {
                            numCriticalFilter = -1;
                        }
                        searchTerm = searchTxt.getText().toString();
                        if(hazardFilter == null){
                            hazardFilter = "OFF";
                        }
                        if(lessMore == null){
                            lessMore = "OFF";
                        } else if (lessMore.equals("Less than")){
                            lessMore = "LESS";
                        } else if (lessMore.equals("More than")){
                            lessMore = "MORE";
                        } else {
                            lessMore = "OFF";
                        }

                        dialogListener.sendSearchInput(searchTerm, hazardFilter, numCriticalFilter, lessMore, favFilter);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        Toast.makeText(getContext(), "Search cancelled", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };
        return new AlertDialog.Builder(getActivity())
                .setTitle("Search")
                .setView(v)
                .setPositiveButton(android.R.string.ok, listener)
                .setNegativeButton(android.R.string.cancel, listener)
                .create();
    }

    private void setupFavFilter() {
        CheckBox favouritesCheck = v.findViewById(R.id.checkBox_fav);
        favouritesCheck.setOnClickListener(v -> {
            Log.d(TAG, "is checked: " + favouritesCheck.isChecked());
            if(favouritesCheck.isChecked()){
                favFilter = true;
            } else {
                favFilter = false;
            }
        });
    }

    private void setupCriticalRadioButtons() {
        RadioGroup group = v.findViewById(R.id.radioGroupLessMore);
        String[] lessMoreSelections = getResources().getStringArray(R.array.less_more_selection);
        for (final String selectedLessMore : lessMoreSelections) {
            RadioButton btn = new RadioButton(getContext());
            btn.setText(selectedLessMore);
            btn.setOnClickListener(v -> {
                lessMore = selectedLessMore;
            });
            group.addView(btn);
            if (selectedLessMore.equals("OFF")) {
                btn.setChecked(true);
            }
        }

    }

    private void setupHazardRadioButtons() {
        RadioGroup group = v.findViewById(R.id.radioGroupHazard);
        String[] hazardSelectionsArray = getResources().getStringArray(R.array.hazard_selection);
        for (final String selectedHazardFilter : hazardSelectionsArray) {
            RadioButton btn = new RadioButton(getContext());
            btn.setText(selectedHazardFilter);
            btn.setOnClickListener(v -> {
                hazardFilter = selectedHazardFilter;
            });
            group.addView(btn);
            if(selectedHazardFilter.equals("OFF")){
                btn.setChecked(true);
            }
        }

    }

    // code to send data from dialog to activity from https://www.youtube.com/watch?v=ARezg1D9Zd0
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            dialogListener = (SearchDialogListener) context;
        } catch (ClassCastException e) {
            Log.e(TAG, "On attach: ClassCastException" + e.getMessage());
        }
    }

    public interface SearchDialogListener{
        void sendSearchInput(String input, String hazard_filter, int num_critical_filter, String lessMore, boolean favFilter);
    }

}
