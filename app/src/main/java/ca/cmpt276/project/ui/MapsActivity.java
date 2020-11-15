package ca.cmpt276.project.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.cmpt276.project.R;
import ca.cmpt276.project.model.CsvInfo;
import ca.cmpt276.project.model.Inspection;
import ca.cmpt276.project.model.LastModified;
import ca.cmpt276.project.model.Restaurant;
import ca.cmpt276.project.model.RestaurantListManager;
import ca.cmpt276.project.model.SurreyDataGetter;
import ca.cmpt276.project.model.types.HazardLevel;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, DialogFragment.UpdateDialogListener{

    //SupportMapFragment mapFragment;
    private GoogleMap mMap;
    private RestaurantListManager restaurantManager;
    private LastModified lastModified;
    private List<CsvInfo> restaurantUpdate;
    List<LatLng> restaurantlatlag;
    private static boolean read = false;
    private static final String KEY = "KEY";
    private LoadingDialogFragment loadingDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Toolbar toolbar = findViewById(R.id.map_toolbar);
        setSupportActionBar(toolbar);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        restaurantManager = RestaurantListManager.getInstance();
        lastModified = LastModified.getInstance(this);

        if(!read){
            fillInitialRestaurantList();
            read = true;
            getUpdatedFiles();
        }
        String toastmsg = "Last Check: " + lastModified.getLastCheck();

        Log.d(KEY, toastmsg);
        if (lastModified.getAppStart() && past20Hours()) {
            lastModified.setAppStart();
            new GetDataTask().execute();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_map,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        switch (item.getItemId()){
            case R.id.action_list:
                startActivity(new Intent(MapsActivity.this,RestaurantListActivity.class));
                finish();
                return  true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setupMap();
        // Add a marker in Sydney and move the camera
        /*LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/
    }

    public void setupMap(){
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        popLatlong();
        addMarkers(mMap);
    }

    private void popLatlong() {
        restaurantlatlag = new ArrayList<>();
        for (Restaurant current : restaurantManager.getList()){
            LatLng temp = new LatLng(current.getGpsLat(),current.getGpsLong());
            restaurantlatlag.add(temp);
        }
    }

    private void addMarkers(GoogleMap googleMap) {
        mMap = googleMap;
        int pos = 0 ;/*
        Drawable Hazard_low = getResources().getDrawable(R.drawable.green_hazard);
        BitmapDescriptor marker_low = getMarkerIconFromDrawable(Hazard_low);
        Drawable Hazard_med = getResources().getDrawable(R.drawable.orange_hazard);
        BitmapDescriptor marker_med = getMarkerIconFromDrawable(Hazard_med);
        Drawable Hazard_high = getResources().getDrawable(R.drawable.red_hazard);
        BitmapDescriptor marker_high= getMarkerIconFromDrawable(Hazard_high);*/
        for (LatLng current : restaurantlatlag){
            if(restaurantManager.getRestaurant(pos).getInspections().getInspections().size()>0) {
                Inspection latestInspection = Collections.max(restaurantManager.getRestaurant(pos).getInspections().getInspections());
                if (latestInspection.getLevel() == HazardLevel.LOW) {
                    mMap.addMarker(new MarkerOptions().position(current).title(restaurantManager.getRestaurant(pos).getName()).snippet("SEVERITY LEVEL: LOW").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                }else if (latestInspection.getLevel() == HazardLevel.MODERATE) {
                    mMap.addMarker(new MarkerOptions().position(current).title(restaurantManager.getRestaurant(pos).getName()).snippet("SEVERITY LEVEL: MODERATE").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                }else if (latestInspection.getLevel() == HazardLevel.HIGH) {
                    mMap.addMarker(new MarkerOptions().position(current).title(restaurantManager.getRestaurant(pos).getName()).snippet("SEVERITY LEVEL: HIGH").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                }
            }
            pos++;
            int finalPos = pos;
            /*mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    Toast.makeText(MapsActivity.this,"Launching Restaurant detail for" + restaurantManager.getRestaurant(finalPos).getName(),Toast.LENGTH_LONG).show();
                    return false;
                }
            });*/
        }
        //change to user location
        mMap.moveCamera(CameraUpdateFactory.newLatLng(restaurantlatlag.get(5)));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
    }

    @Override
    public void sendInput(boolean input) {

        if(input) {
            if (restaurantUpdate.get(0).getChanged() // check if restaurant list changed
                    || restaurantUpdate.get(1).getChanged()) { // if inspection list changed
                // Want update? Execute function
                Log.d(KEY, "bruh moment " + input);
                System.out.println("FOUND AN UPDATE!!!!--------------");
                Log.d(KEY, "UPDATE INPUT TRUE");
                lastModified.setLastCheck(MapsActivity.this, LocalDateTime.now());
                lastModified.setLast_mod_restaurants(MapsActivity.this, restaurantUpdate.get(0).getLast_modified());
                lastModified.setLast_mod_inspections(MapsActivity.this, restaurantUpdate.get(1).getLast_modified());
                FragmentManager manager = getSupportFragmentManager();
                loadingDialog = new LoadingDialogFragment(); // loading dialog
                loadingDialog.show(manager, "LoadingDialog");
                new ListUpdateTask().execute();
            }
        }
    }
    /*private BitmapDescriptor getMarkerIconFromDrawable(Drawable drawable) {
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }*/

    // Get the CSV links and timestamps
    private class GetDataTask extends AsyncTask<Void,Void,List<CsvInfo>> {
        @Override
        protected List<CsvInfo> doInBackground(Void... voids) {
            return new SurreyDataGetter().getDataLink(MapsActivity.this);
        }

        @Override
        protected void onPostExecute(List<CsvInfo> data) {
            restaurantUpdate = data;
            FragmentManager manager = getSupportFragmentManager();
            DialogFragment dialog = new DialogFragment(); // ask if user wants to update
            dialog.show(manager, "MessageDialog");
            System.out.println(data.get(0));
            System.out.println(data.get(1));
        }
    }

    // Download CSV files
    private class ListUpdateTask extends AsyncTask<Void,Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {

            return new SurreyDataGetter().getCSVData(restaurantUpdate, MapsActivity.this);
        }

        @Override
        protected void onPostExecute(Boolean receivedUpdate) {
            boolean update = receivedUpdate;
            if (update) {
                getUpdatedFiles();
            }
            setupMap();
            loadingDialog.dismiss();

        }
    }

    private void fillInitialRestaurantList() {
        InputStream inputStream = getResources().openRawResource(R.raw.restaurants_itr1);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8)
        );

        restaurantManager.fillRestaurantManager(reader);
        InputStream is = getResources().openRawResource(R.raw.inspectionreports_itr1);
        BufferedReader inspectionReader = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8)
        );
        restaurantManager.fillInspectionManager(inspectionReader);
    }

    private void getUpdatedFiles() {
        FileInputStream inputStream_rest;
        FileInputStream inputStream_insp;
        try {
            inputStream_rest = MapsActivity.this.openFileInput(SurreyDataGetter.DOWNLOAD_RESTAURANTS);
            inputStream_insp = MapsActivity.this.openFileInput(SurreyDataGetter.DOWNLOAD_INSPECTIONS);
            InputStreamReader inputReader_rest = new InputStreamReader(inputStream_rest, StandardCharsets.UTF_8);
            InputStreamReader inputReader_insp = new InputStreamReader(inputStream_insp, StandardCharsets.UTF_8);

            restaurantManager.fillRestaurantManager(new BufferedReader(inputReader_rest));
            restaurantManager.fillInspectionManager(new BufferedReader(inputReader_insp));
        } catch (FileNotFoundException e) {
            // No update files downloaded
            Toast.makeText(this, "CAN'T FIND FILES",Toast.LENGTH_LONG).show();
        }
    }

    private boolean past20Hours() {
        lastModified = LastModified.getInstance(MapsActivity.this);
        LocalDateTime previous = lastModified.getLastCheck();
        LocalDateTime current = LocalDateTime.now();
        LocalDateTime compare = current.minusHours(20);
        if (previous.isBefore(compare) || compare.isEqual(previous)) {
            Toast.makeText(this, "Checking for Update", Toast.LENGTH_LONG).show();
            return true;
        } else {
            Toast.makeText(this, "hasn't been 20 hours since the last check", Toast.LENGTH_LONG).show();
            return false;
        }
    }
}
