package ca.cmpt276.project.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.SnackbarContentLayout;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.cmpt276.project.R;
import ca.cmpt276.project.model.ClusterManagerRenderer;
import ca.cmpt276.project.model.ClusterMarker;
import ca.cmpt276.project.model.CsvInfo;
import ca.cmpt276.project.model.Inspection;
import ca.cmpt276.project.model.LastModified;
import ca.cmpt276.project.model.Restaurant;
import ca.cmpt276.project.model.RestaurantListManager;
import ca.cmpt276.project.model.SurreyDataDownloader;
import ca.cmpt276.project.model.types.HazardLevel;

public class MapsActivity extends AppCompatActivity implements GoogleMap.OnCameraMoveStartedListener, OnMapReadyCallback, UpdateFragment.UpdateDialogListener, LoadingDialogFragment.CancelDialogListener, MarkerDialogFragment.PopUpDialogListener, ClusterManager.OnClusterClickListener<ClusterMarker>, ClusterManager.OnClusterItemClickListener<ClusterMarker> {
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    //SupportMapFragment mapFragment;
    private GoogleMap mMap;
    private RestaurantListManager restaurantManager;
    private LastModified lastModified;
    private List<CsvInfo> restaurantUpdate;
    private List<LatLng> restaurantlatlog;

    // custom markers
    private ClusterManager<ClusterMarker> mClusterManager;
    private ClusterManagerRenderer mClusterManagerRenderer;
    private static final String REST_DETAILS_INDEX = "restaurant details index";
    private final List<ClusterMarker> Markerlist = new ArrayList<>();

    //User Locations permission
    private Location currentLocation;
    private Boolean mLocationPermissionsGranted = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationRequest mLocationRequest;
    private static boolean read = false;
    private ListUpdateTask listUpdateTask = null;
    private LoadingDialogFragment loadingDialog;

    FragmentManager manager;

    //Location callBack
    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult == null) {
                return;
            }
            for(Location location : locationResult.getLocations()){
                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(13f));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Toolbar toolbar = findViewById(R.id.map_toolbar);
        setSupportActionBar(toolbar);

        restaurantManager = RestaurantListManager.getInstance();
        lastModified = LastModified.getInstance(this);
        manager = getSupportFragmentManager();

        if(!read){
            fillInitialRestaurantList();
            read = true;
            getUpdatedFiles();
        }

        if (lastModified.getAppStart() && past20Hours()) {
            lastModified.setAppStart();
            new GetDataTask().execute();
        }
        getLocationPermission();

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationUpdates();
        }
    }

    private void LocationUpdates() {
        LocationSettingsRequest request = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest).build();
        SettingsClient client = LocationServices.getSettingsClient(this);

        Task<LocationSettingsResponse> locationSettingsResponseTask = client.checkLocationSettings(request);
        locationSettingsResponseTask.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                //Settings of device are satisfied and we can start location updates
                startLocationUpdates();
            }
        });
        locationSettingsResponseTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    ResolvableApiException apiException = (ResolvableApiException) e;
                    try {
                        apiException.startResolutionForResult(MapsActivity.this, 1001);
                    } catch (IntentSender.SendIntentException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    private void getLocationPermission() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionsGranted = true;
                initialMap();
            }else{
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionsGranted = false;

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        mLocationPermissionsGranted = false;
                        return;
                    }
                }
                mLocationPermissionsGranted = true;
                //initialize our map
                initialMap();
                LocationUpdates();
            }
        }
    }

    private void initialMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_map,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        if (item.getItemId() == R.id.action_list) {
            startActivity(new Intent(MapsActivity.this,RestaurantListActivity.class));
            finish();
            return true;
        }
        return false;
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
        if (mLocationPermissionsGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
        }
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener(){
            @Override
            public boolean onMyLocationButtonClick()
            {
                startLocationUpdates();
                return true;
            }
        });
    }

    private void getDeviceLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try{
            if(mLocationPermissionsGranted){
                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        currentLocation = (Location) task.getResult();
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(13f));
                        startLocationUpdates();
                        extractDataFromIntent();
                    }else{
                        Toast.makeText(MapsActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }catch (SecurityException e){
            Log.e("user location", "getDeviceLocation: SecurityException: " + e.getMessage() );
        }
    }

    @Override
    public void onCameraMoveStarted(int reason) {
        if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
            Toast.makeText(this, "sensed", Toast.LENGTH_SHORT).show();
            stopLocationUpdates();
        }
    }

    public void setupMap(){

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mClusterManager = new ClusterManager<>(this, mMap);
        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);
        mMap.setOnInfoWindowClickListener(mClusterManager);
        //mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(MapsActivity.this));
        mClusterManager.setOnClusterClickListener(this);
        //mClusterManager.setOnClusterInfoWindowClickListener(this);
        mClusterManager.setOnClusterItemClickListener(this);
        //mClusterManager.setOnClusterItemInfoWindowClickListener(this);

        popLatlong();
        addMarkers(mMap);
    }

    private void popLatlong() {
        restaurantlatlog = new ArrayList<>();
        for (Restaurant current : restaurantManager.getList()){
            LatLng temp = new LatLng(current.getGpsLat(),current.getGpsLong());
            restaurantlatlog.add(temp);
        }
    }

    private void addMarkers(GoogleMap googleMap) {
        if(googleMap != null) {

            if (mClusterManager == null) {
                mClusterManager = new ClusterManager<>(this.getApplicationContext(), googleMap);
            }
            if (mClusterManagerRenderer == null) {
                mClusterManagerRenderer = new ClusterManagerRenderer(this, googleMap, mClusterManager );
                mClusterManager.setRenderer(mClusterManagerRenderer);
            }
            int pos = 0 ;
            // set the severity icons
            int low = R.drawable.green_hazard;
            int med = R.drawable.orange_hazard;
            int high = R.drawable.red_hazard;
            for (LatLng current : restaurantlatlog) {
                try {
                    if (restaurantManager.getRestaurant(pos).getInspections().getInspections().size() > 0) {
                        String snippet = "";
                        int severity_icon = R.drawable.green_hazard;
                        Inspection latestInspection = Collections.max(restaurantManager.getRestaurant(pos).getInspections().getInspections());
                        if (latestInspection.getLevel() == HazardLevel.LOW) {
                            snippet = ""+ pos;
                            severity_icon = low;
                        } else if (latestInspection.getLevel() == HazardLevel.MODERATE) {
                            snippet = ""+ pos;
                            severity_icon = med;
                        } else if (latestInspection.getLevel() == HazardLevel.HIGH) {
                            snippet = ""+ pos;
                            severity_icon = high;
                        }
                        ClusterMarker newClusterMarker = new ClusterMarker(current,restaurantManager.getRestaurant(pos).getName(), snippet, severity_icon, restaurantManager.getRestaurant(pos));
                        mClusterManager.addItem(newClusterMarker);
                        Markerlist.add(newClusterMarker);
                    }
                } catch(NullPointerException e){
                    Log.e("CMarker", "addMapMarkers: NullPointerException: " + e.getMessage());
                }
                pos++;
            }
            mClusterManager.cluster();
        }
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, locationCallback, Looper.getMainLooper());
    }

    private void stopLocationUpdates() {
        mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }



    @Override
    public boolean onClusterClick(Cluster<ClusterMarker> cluster) {
        String Names = "";
        // Create the builder to collect all essential cluster items for the bounds.
        LatLngBounds.Builder builder = LatLngBounds.builder();
        for (ClusterItem item : cluster.getItems()) {
            Names = Names + item.getTitle() +", ";
            builder.include(item.getPosition());
        }
        // Get the LatLngBounds
        final LatLngBounds bounds = builder.build();

        // Animate camera to the bounds
        try {
           mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        } catch (Exception e) {
            e.printStackTrace();
        }
        stopLocationUpdates();
        return true;
    }

    @Override
    public boolean onClusterItemClick(ClusterMarker item) {
        Toast.makeText(this, "marker clicked", Toast.LENGTH_SHORT).show();

        Restaurant restaurant = item.getRest();
        int index = restaurantManager.getList().indexOf(restaurant);
        openPopUpWindow(index);

        return true;
    }

    private void openPopUpWindow(int index) {
        mMap.moveCamera(CameraUpdateFactory.newLatLng(restaurantlatlog.get(index)));
        Bundle info = new Bundle();
        info.putInt("index", index);

        MarkerDialogFragment markerFragment = new MarkerDialogFragment();
        markerFragment.setArguments(info);
        markerFragment.show(manager,"popup");
        stopLocationUpdates();
    }

    @Override
    public void popUp(int index) {
        Intent intent = RestaurantDetailsActivity.makeLaunchIntent(MapsActivity.this, index);
        startActivity(intent);
    }

    // Get the CSV links and timestamps
    private class GetDataTask extends AsyncTask<Void,Void,List<CsvInfo>> {
        @Override
        protected List<CsvInfo> doInBackground(Void... voids) {
            return new SurreyDataDownloader().getDataLink(MapsActivity.this);
        }

        @Override
        protected void onPostExecute(List<CsvInfo> data) {
            restaurantUpdate = data;
            if (restaurantUpdate.get(0).getChanged() // check if restaurant list changed
                    || restaurantUpdate.get(1).getChanged()) { // if inspection list changed
                // Want update? Execute function
                UpdateFragment dialog = new UpdateFragment(); // ask if user wants to update
                dialog.show(manager, "MessageDialog");
            } else {
                lastModified.setLastCheck(MapsActivity.this, LocalDateTime.now());
            }
        }
    }

    @Override
    public void sendInput(boolean input) {
        if(input) {
            listUpdateTask = (ListUpdateTask) new ListUpdateTask().execute();
        }
    }

    @Override
    public void sendCancel(boolean input) {
        if (input) {
            listUpdateTask.cancel(true);
            Toast.makeText(this, "CANCELLED DOWNLOAD", Toast.LENGTH_LONG).show();
        }
    }

    // Download CSV files
    private class ListUpdateTask extends AsyncTask<Void,Void, Boolean> {
        @Override
        protected void onPreExecute() {
            loadingDialog = new LoadingDialogFragment(); // loading dialog
            loadingDialog.show(manager, "LoadingDialog");
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if (!isCancelled()) {
                new SurreyDataDownloader().getCSVData(restaurantUpdate, MapsActivity.this);
                return true;
            } else {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean receivedUpdate) {
            boolean update = receivedUpdate;
            if (update) {
                lastModified.setLastCheck(MapsActivity.this, LocalDateTime.now());
                lastModified.setLast_mod_restaurants(MapsActivity.this, restaurantUpdate.get(0).getLast_modified());
                lastModified.setLast_mod_inspections(MapsActivity.this, restaurantUpdate.get(1).getLast_modified());
                getUpdatedFiles();
                setupMap();
                finish();
                startActivity(getIntent());
                loadingDialog.dismiss();
            }
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
            inputStream_rest = MapsActivity.this.openFileInput(SurreyDataDownloader.DOWNLOAD_RESTAURANTS);
            inputStream_insp = MapsActivity.this.openFileInput(SurreyDataDownloader.DOWNLOAD_INSPECTIONS);
            InputStreamReader inputReader_rest = new InputStreamReader(inputStream_rest, StandardCharsets.UTF_8);
            InputStreamReader inputReader_insp = new InputStreamReader(inputStream_insp, StandardCharsets.UTF_8);

            restaurantManager.fillRestaurantManager(new BufferedReader(inputReader_rest));
            restaurantManager.fillInspectionManager(new BufferedReader(inputReader_insp));
        } catch (FileNotFoundException e) {
            // No update files downloaded
        }
    }

    private boolean past20Hours() {
        lastModified = LastModified.getInstance(MapsActivity.this);
        LocalDateTime previous = lastModified.getLastCheck();
        LocalDateTime current = LocalDateTime.now();
        LocalDateTime compare = current.minusHours(20);
        return previous.isBefore(compare) || compare.isEqual(previous);
    }

    public static Intent makeLaunchIntentMapsActivity(Context context, int restIdx) {
        Intent intent = new Intent(context, MapsActivity.class);
        intent.putExtra(REST_DETAILS_INDEX, restIdx);
        return intent;
    }

    private void extractDataFromIntent() {
        Intent intent = getIntent();
        int restaurant_details_idx = intent.getIntExtra(REST_DETAILS_INDEX, -1);

        if(restaurant_details_idx > 0) {
            openPopUpWindow(restaurant_details_idx);
            mMap.animateCamera(CameraUpdateFactory.zoomTo(20));
        }
    }
}
