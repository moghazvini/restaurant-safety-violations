package ca.cmpt276.project.ui;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import ca.cmpt276.project.R;

public class InspectionDetailsActivity extends AppCompatActivity {

    private final static String INDEX = "Inspection Report Index";
    private ActionBar back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inspection_details);
        Toolbar toolbar = findViewById(R.id.toolbar_inspection_det);
        setSupportActionBar(toolbar);

        // Enable "up" on toolbar
        back = getSupportActionBar();
        if (back != null) {
            back.setDisplayHomeAsUpEnabled(true);
        }


        Button backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(v -> finish());
    }
       public static Intent makeLaunchIntent(RestaurantDetailsActivity restaurantDetails, int position) {
        Intent intent = new Intent(restaurantDetails, InspectionDetailsActivity.class);
        intent.putExtra(INDEX, position);
        return intent;
    }
}