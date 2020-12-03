package ca.cmpt276.project.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import android.content.Context;
import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
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
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import ca.cmpt276.project.R;
import ca.cmpt276.project.model.ClusterManagerRenderer;
import ca.cmpt276.project.model.ClusterMarker;
import ca.cmpt276.project.model.CsvInfo;
import ca.cmpt276.project.model.DBAdapter;
import ca.cmpt276.project.model.Inspection;
import ca.cmpt276.project.model.InspectionListManager;
import ca.cmpt276.project.model.LastModified;
import ca.cmpt276.project.model.LocalDateAdapter;
import ca.cmpt276.project.model.Restaurant;
import ca.cmpt276.project.model.RestaurantListManager;
import ca.cmpt276.project.model.SurreyDataDownloader;
import ca.cmpt276.project.model.types.HazardLevel;
import ca.cmpt276.project.model.types.InspectionType;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, UpdateFragment.UpdateDialogListener, LoadingDialogFragment.CancelDialogListener, SearchDialogFragment.SearchDialogListener, ClusterManager.OnClusterClickListener<ClusterMarker>, ClusterManager.OnClusterItemClickListener<ClusterMarker>,MarkerDialogFragment.PopUpDialogListener {

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final String TAG = "MapsTag";

    //SupportMapFragment mapFragment;
    private GoogleMap mMap;
    private RestaurantListManager restaurantManager;
    private LastModified lastModified;
    private List<CsvInfo> restaurantUpdate;
    private List<LatLng> restaurantlatlong;

    // custom markers
    private ClusterManager<ClusterMarker> mClusterManager;
    private ClusterManagerRenderer mClusterManagerRenderer;
    private static final String REST_DETAILS = "restaurant details tracking";

    //User Locations permission
    private Boolean mLocationPermissionsGranted = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationRequest mLocationRequest;
    private static boolean read = false;
    private ListUpdateTask listUpdateTask = null;
    private LoadingDialogFragment loadingDialog;

    private static final String PREFS_NAME = "AppPrefs";
    private static final String NAME_FILTER_PREF = "name shared pref";
    private static final String HAZARD_FILTER_PREF = "hazard shared pref";
    private static final String NUM_FILTER_PREF = "num critical shared pref";
    private static final String LESS_FILTER_PREF = "less shared pref";
    private static final String FAV_FILTER_PREF = "favourite shared pref";

    FragmentManager manager;
    Gson gson;
    Type type;
    DBAdapter myDb;
    List<Restaurant> favouritesUpdated;
    //Location callBack

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Toolbar toolbar = findViewById(R.id.map_toolbar);
        setSupportActionBar(toolbar);
        openDB();

        restaurantManager = RestaurantListManager.getInstance();
        lastModified = LastModified.getInstance(this);
        manager = getSupportFragmentManager();
        type = new TypeToken<ArrayList<Inspection>>() {}.getType();
        gson = new GsonBuilder().registerTypeAdapter(LocalDate.class, new LocalDateAdapter().nullSafe()).create();

        if(!read){
            fillInitialRestaurantList();
            read = true;
            getUpdatedFiles();
        }

        if(lastModified.readInitialStart(this)){
            fillInitialDatabase();
            lastModified.writeInitialStart(this);
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

    ////////////////////////////////////////////////////////
    // MAP LOCATION PERMISSIONS AND CAMERA UPDATE
    ///////////////////////////////////////////////////////
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
        locationSettingsResponseTask.addOnFailureListener(e -> {
            if (e instanceof ResolvableApiException) {
                ResolvableApiException apiException = (ResolvableApiException) e;
                try {
                    apiException.startResolutionForResult(MapsActivity.this, 1001);
                } catch (IntentSender.SendIntentException ex) {
                    ex.printStackTrace();
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

    private void getDeviceLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try{
            if(mLocationPermissionsGranted){
                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Location currentLocation = (Location) task.getResult();

                        if(currentLocation != null) {
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())));
                            mMap.animateCamera(CameraUpdateFactory.zoomTo(13f));
                        }

                        extractDataFromIntent();
                    }
                });
            }
        }catch (SecurityException e){
            Log.e("user location", "getDeviceLocation: SecurityException: " + e.getMessage() );
        }
    }

    ////////////////////////////////////////////////////
    // DRAW MAP AND MARKERS
    ////////////////////////////////////////////////////
    private void initialMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
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
    }

    public void setupMap(){
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mClusterManager = new ClusterManager<>(this, mMap);
        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);
        mClusterManager.setOnClusterClickListener(this);
        mClusterManager.setOnClusterItemClickListener(this);
        Cursor allRestCursor = getFilterPrefs();
        popLatlong();
        addRelevantMarkers(mMap, allRestCursor);
    }

    private Cursor getFilterPrefs(){
        SharedPreferences prefs = this.getSharedPreferences(PREFS_NAME, 0);
        String searchTerm = prefs.getString(NAME_FILTER_PREF, "");
        String hazardFilter = prefs.getString(HAZARD_FILTER_PREF, "OFF");
        int numCriticalFilter = prefs.getInt(NUM_FILTER_PREF, -1);
        String lessMore = prefs.getString(LESS_FILTER_PREF, "OFF");
        boolean favFilter = prefs.getBoolean(FAV_FILTER_PREF, false);
        Cursor cursor = myDb.filterRestaurants(searchTerm, hazardFilter, numCriticalFilter, lessMore, favFilter);
        if(cursor != null){
            return cursor;
        } else {
            return myDb.getAllRows();
        }

    }

    private void popLatlong() {
        restaurantlatlong = new ArrayList<>();
        for (Restaurant current : restaurantManager.getList()){
            LatLng temp = new LatLng(current.getGpsLat(),current.getGpsLong());
            restaurantlatlong.add(temp);
        }
    }

    private void addRelevantMarkers(GoogleMap map, Cursor cursor){
        if (mClusterManager == null) {
            mClusterManager = new ClusterManager<>(this.getApplicationContext(), map);
        }
        if (mClusterManagerRenderer == null) {
            mClusterManagerRenderer = new ClusterManagerRenderer(this, map, mClusterManager );
        }
        mClusterManager.setRenderer(mClusterManagerRenderer);
        mClusterManager.clearItems();
        mClusterManager.cluster();
        int pos = 0;

        if(cursor.moveToFirst()){
            do{
                String tracking = cursor.getString(DBAdapter.COL_TRACKING);
                String address = cursor.getString(DBAdapter.COL_ADDRESS);
                String city = cursor.getString(DBAdapter.COL_CITY);
                String name = cursor.getString(DBAdapter.COL_NAME);
                float latitude = cursor.getFloat(DBAdapter.COL_LATITUDE);
                float longitude = cursor.getFloat(DBAdapter.COL_LONGITUDE);
                boolean favourite = false;
                if (cursor.getInt(DBAdapter.COL_FAVOURITE) == 1) {
                    favourite = true;
                }
                LatLng coords = new LatLng(latitude, longitude);

                ArrayList<Inspection> inspectionArrayList = extractInspectionList(cursor);
                InspectionListManager inspectionListManager = new InspectionListManager();
                inspectionListManager.setInspectionsList(inspectionArrayList);
                Restaurant newRestaurant = new Restaurant(tracking, name, address, city, longitude, latitude, favourite);
                newRestaurant.setInspections(inspectionListManager);
                Inspection latestInspection = null;
                if(inspectionArrayList.size() > 0) {
                    latestInspection = Collections.max(inspectionArrayList);
                }
                int low = R.drawable.green_hazard;
                int med = R.drawable.orange_hazard;
                int high = R.drawable.red_hazard;
                String snippet = "";
                int severity_icon = R.drawable.green_hazard;

                if(latestInspection != null) {
                    if (latestInspection.getLevel() == HazardLevel.LOW) {
                        snippet = "" + pos;
                        severity_icon = low;
                    } else if (latestInspection.getLevel() == HazardLevel.MODERATE) {
                        snippet = "" + pos;
                        severity_icon = med;
                    } else if (latestInspection.getLevel() == HazardLevel.HIGH) {
                        snippet = "" + pos;
                        severity_icon = high;
                    }
                    ClusterMarker newClusterMarker = new ClusterMarker(
                            coords,
                            name,
                            snippet,
                            severity_icon,
                            newRestaurant);
                    mClusterManager.addItem(newClusterMarker);
                    pos++;
                } else {
                    Log.d(TAG, "null inspection");
                }

            } while(cursor.moveToNext());

            mClusterManager.cluster();
        }
        cursor.close();
    }

    //////////////////////////////////////////////
    // CLUSTER MARKERS
    //////////////////////////////////////////////
    @Override
    public boolean onClusterClick(Cluster<ClusterMarker> cluster) {

        // Create the builder to collect all essential cluster items for the bounds.
        LatLngBounds.Builder builder = LatLngBounds.builder();
        for (ClusterItem item : cluster.getItems()) {
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
        return true;
    }

    @Override
    public boolean onClusterItemClick(ClusterMarker item) {
        Restaurant restaurant = item.getRest();
        openPopUpWindow(restaurant);

        return true;
    }

    private void openPopUpWindow(Restaurant restaurant) {
        LatLng coords = new LatLng(restaurant.getGpsLat(), restaurant.getGpsLong());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(coords));
        MarkerDialogFragment markerFragment = MarkerDialogFragment.newInstance(restaurant.getTracking());
        markerFragment.show(manager,"popup");
    }

    @Override
    public void popUp(String tracking) {
        Intent intent = RestaurantDetailsActivity.makeLaunchIntent(MapsActivity.this, tracking);
        startActivity(intent);
    }

    ////////////////////////////////////////////////
    // SEARCH AND FILTER
    ///////////////////////////////////////////////
    @Override
    public void sendSearchInput(String name, String hazard_filter, int num_critical_filter, String lessMore, boolean favFilter, boolean reset) {
        Cursor relevantRowsCursor;
        if((name.length() > 0 || hazard_filter.length() > 0 || num_critical_filter > 0) && (!reset)) {
            relevantRowsCursor = myDb.filterRestaurants(name, hazard_filter, num_critical_filter, lessMore, favFilter);
            if (relevantRowsCursor != null) {
                addRelevantMarkers(mMap, relevantRowsCursor);
            }
        } else if (reset){
            relevantRowsCursor = myDb.getAllRows();
            addRelevantMarkers(mMap, relevantRowsCursor);
        }
    }

    private ArrayList<Inspection> extractInspectionList(Cursor cursor){
        String outputString = cursor.getString(DBAdapter.COL_INSPECTION_LIST);
        ArrayList<Inspection> inspectionsArray = gson.fromJson(outputString, type);
        return inspectionsArray;
    }

    //////////////////////////////////////////////
    // DOWNLOAD DATA UPDATES
    //////////////////////////////////////////////
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
                favouritesUpdated = new ArrayList<>();
                fillDatabaseWithUpdated();
                lastModified.setLastCheck(MapsActivity.this, LocalDateTime.now());
                lastModified.setLast_mod_restaurants(MapsActivity.this, restaurantUpdate.get(0).getLast_modified());
                lastModified.setLast_mod_inspections(MapsActivity.this, restaurantUpdate.get(1).getLast_modified());
                getUpdatedFiles();
                Cursor allRestCursor = myDb.getAllRows();
                addRelevantMarkers(mMap, allRestCursor);
                loadingDialog.dismiss();
                showUpdatedFavourites();
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
            Toast.makeText(this, R.string.cancelled, Toast.LENGTH_LONG).show();
        }
    }

    private void showUpdatedFavourites() {
        favouritesUpdated = restaurantManager.getFavourited();
        if (favouritesUpdated.size() > 0) {
            FavouritesUpdatedDialogFragment updated = new FavouritesUpdatedDialogFragment();
            updated.show(manager,"Updated favourites");
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

    // ALL DATABASE RELATED FUNCTIONS BELOW
    private void openDB() {
        myDb = new DBAdapter(this);
        myDb.open();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myDb.close();

    }

    private void fillInitialDatabase() {
        InputStream inputStream = getResources().openRawResource(R.raw.restaurants_itr1);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8)
        );
        fillRestaurantDatabase(reader);
        InputStream is = getResources().openRawResource(R.raw.inspectionreports_itr1);
        BufferedReader inspectionReader = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8)
        );
        fillInspectionsDatabase(inspectionReader);
        addHazardAndCriticalToDB();
    }

    private void fillDatabaseWithUpdated(){
        FileInputStream inputStream_rest;
        FileInputStream inputStream_insp;
        try {
            inputStream_rest = MapsActivity.this.openFileInput(SurreyDataDownloader.DOWNLOAD_RESTAURANTS);
            inputStream_insp = MapsActivity.this.openFileInput(SurreyDataDownloader.DOWNLOAD_INSPECTIONS);
            InputStreamReader inputReader_rest = new InputStreamReader(inputStream_rest, StandardCharsets.UTF_8);
            InputStreamReader inputReader_insp = new InputStreamReader(inputStream_insp, StandardCharsets.UTF_8);
            fillRestaurantDatabase(new BufferedReader(inputReader_rest));
            fillInspectionsDatabase(new BufferedReader(inputReader_insp));
            addHazardAndCriticalToDB();
        } catch (FileNotFoundException e) {
            // No update files downloaded
        }
    }

    private List<String> favouriteTrackings() {
        Cursor cursor = myDb.getAllRows();
        if (cursor.getCount() > 0) {
            List<String> trackings = new ArrayList<>();
            do {
                if (Integer.parseInt(cursor.getString(DBAdapter.COL_FAVOURITE)) == 1) {
                    trackings.add(cursor.getString(DBAdapter.COL_TRACKING));
                }
            } while (cursor.moveToNext());
            cursor.close();
            return trackings;
        } else {
            return null;
        }
    }

    private void fillRestaurantDatabase(BufferedReader reader){
        List<String> savedFavourites = favouriteTrackings();
        String line = "";
        try {
            reader.readLine();
            myDb.deleteAll();
            myDb.beginTransaction();
            while ((line = reader.readLine()) != null) {
                line = line.replace("\"", "");

                String[] attributes = line.split(",");
                String tracking = attributes[0];
                tracking = tracking.replace(" ", "");
                String name = attributes[1];
                int favourite = 0;

                if (savedFavourites != null && savedFavourites.size() > 0 && !name.equals("The Unfindable Bar")) {
                    if (savedFavourites.contains(tracking)) {
                        restaurantManager.getFavourited().add(restaurantManager.find(tracking));
                        favourite = 1;
                    }
                }
                // else
                    int addrIndex = attributes.length - 5;
                    for (int i = 2; i < addrIndex; i++) {
                        name = name.concat(attributes[i]);
                    }
                    String addr = attributes[addrIndex];
                    String city = attributes[addrIndex + 1];
                    float gpsLat = Float.parseFloat(attributes[addrIndex + 3]);
                    float gpsLong = Float.parseFloat(attributes[addrIndex + 4]);
                    ArrayList<Inspection> inspectionsArray = new ArrayList<>();

                    String inspections = gson.toJson(inspectionsArray);

                    //read data
                    myDb.insertRowRestaurant(tracking,
                            name,
                            addr,
                            city,
                            gpsLat,
                            gpsLong,
                            inspections,
                            favourite);
            }
            myDb.endTransactionSuccessful();

        } catch(IOException e){
            Log.wtf("MapsActivity", "error reading data file on line " + line, e);
        }

    }

    private void fillInspectionsDatabase(BufferedReader reader){
        String line = "";
        try {
            reader.readLine();

            myDb.beginTransaction();
            while ((line = reader.readLine()) != null) {
                line = line.replace("\"", "");
                String[] tokens = line.split(",");

                //read data
                if (tokens.length > 0) {
                    String inspectionTracking = tokens[0];
                    Cursor restaurantCursor = myDb.searchRestaurants(DBAdapter.KEY_TRACKING, inspectionTracking, DBAdapter.MatchString.EQUALS);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                    LocalDate date = LocalDate.parse(tokens[1], formatter);
                    String stringType = tokens[2];
                    InspectionType inspectionType;
                    if(stringType.equals("Routine")){
                        inspectionType = InspectionType.ROUTINE;
                    }
                    else{
                        inspectionType = InspectionType.FOLLOWUP;
                    }
                    int numCritical = Integer.parseInt(tokens[3]);
                    int numNonCritical = Integer.parseInt(tokens[4]);
                    String violationLump = "[empty]";
                    HazardLevel hazard = getHazardLevel(tokens[tokens.length-1]);
                    Inspection inspection;
                    if(tokens.length > 5 && tokens[5].length() > 0) {
                        violationLump = getVioLump(tokens);
                        inspection = new Inspection(date, inspectionType, numCritical, numNonCritical, hazard, violationLump);
                    } else {
                        inspection = new Inspection(date, inspectionType, numCritical, numNonCritical, hazard);
                    }

                    if(restaurantCursor.moveToFirst()) {
                        ArrayList<Inspection> inspectionsListDB = extractInspectionList(restaurantCursor);
                        inspectionsListDB.add(inspection);
                        String trackingID = restaurantCursor.getString(DBAdapter.COL_TRACKING);
                        String inputString = gson.toJson(inspectionsListDB);
                        myDb.updateRow(DBAdapter.KEY_INSPECTION_LIST, trackingID, inputString);
                    }
                    if(restaurantCursor != null){
                        restaurantCursor.close();
                    }
                }
            }
            myDb.endTransactionSuccessful();
        } catch(IOException e){
            Log.wtf("RestaurantListActivity", "error reading data file on line " + line, e);
        }
    }

    private HazardLevel getHazardLevel(String hazard) {
        if (hazard.equals("High")) {
            return HazardLevel.HIGH;
        } else if (hazard.equals("Moderate")) {
            return HazardLevel.MODERATE;
        } else {
            return HazardLevel.LOW;
        }
    }

    private String getVioLump(String[] inspectionRow){
        StringBuilder lump = new StringBuilder();
        for (int i = 5; i < inspectionRow.length - 1; i++) {
            lump.append(inspectionRow[i]).append(",");
        }
        return lump.toString();
    }

    private void addHazardAndCriticalToDB(){
        Cursor restaurantDBcursor = myDb.getAllRows();
        if(restaurantDBcursor.moveToFirst()){
            do{
                ArrayList<Inspection> inspectionArrayList = extractInspectionList(restaurantDBcursor);
                String hazardLevel = "N/A";
                int numCritical = 0;
                if(inspectionArrayList.size() > 0) {
                    Inspection latestInspection = Collections.max(inspectionArrayList);
                    hazardLevel = latestInspection.getStringHazard();

                    numCritical = getLatestYearSumCritical(inspectionArrayList);
                    String tracking = restaurantDBcursor.getString(DBAdapter.COL_TRACKING);
                    myDb.updateRestaurantRow(tracking, hazardLevel, numCritical);
                }
            } while (restaurantDBcursor.moveToNext());
        }
    }

    private int getLatestYearSumCritical(ArrayList<Inspection> inspectionsArrayList){
        LocalDateTime current = LocalDateTime.now();
        int totalCritical = 0;
        for(int i = 0; i < inspectionsArrayList.size(); i++){
            if(Math.abs(current.getYear() - inspectionsArrayList.get(i).getDate().getYear()) <= 1){
                totalCritical += inspectionsArrayList.get(i).getCritical();
            }
        }
        return totalCritical;
    }

    private boolean past20Hours() {
        lastModified = LastModified.getInstance(MapsActivity.this);
        LocalDateTime previous = lastModified.getLastCheck();
        LocalDateTime current = LocalDateTime.now();
        LocalDateTime compare = current.minusHours(20);
        return previous.isBefore(compare) || compare.isEqual(previous);
    }

    public static Intent makeLaunchIntentMapsActivity(Context context, String tracking) {
        Intent intent = new Intent(context, MapsActivity.class);
        intent.putExtra(REST_DETAILS, tracking);
        return intent;
    }

    private void extractDataFromIntent() {
        Intent intent = getIntent();
        String tracking = intent.getStringExtra(REST_DETAILS);
        if(tracking == null){
            tracking = "";
        }
        if(tracking.length() > 0) {
            Restaurant restaurant = getRestaurantFromTracking(tracking);
            if (restaurant != null) {
                openPopUpWindow(restaurant);
                mMap.animateCamera(CameraUpdateFactory.zoomTo(20));
            }
        }
    }

    private Restaurant getRestaurantFromTracking(String tracking){
        Cursor restaurantCursor = myDb.searchRestaurants(DBAdapter.KEY_TRACKING, tracking, DBAdapter.MatchString.EQUALS);
        Restaurant newRestaurant = null;
        if(restaurantCursor.moveToFirst()){
            String name = restaurantCursor.getString(DBAdapter.COL_NAME);
            String address = restaurantCursor.getString(DBAdapter.COL_ADDRESS);
            String city = restaurantCursor.getString(DBAdapter.COL_CITY);
            float gpsLong = restaurantCursor.getFloat(DBAdapter.COL_LONGITUDE);
            float gpsLat = restaurantCursor.getFloat(DBAdapter.COL_LATITUDE);
            boolean favourite = false;
            if (restaurantCursor.getInt(DBAdapter.COL_FAVOURITE) == 1) {
                favourite = true;
            }
            newRestaurant = new Restaurant(tracking, name, address, city, gpsLong, gpsLat, favourite);
        }
        return newRestaurant;
    }

    ////////////////////////////////////////////////////////
    // TOOL BAR ACTIONS
    ////////////////////////////////////////////////////////
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
        else if(item.getItemId() ==  R.id.action_search){
            FragmentManager manager = getSupportFragmentManager();
            SearchDialogFragment dialog = new SearchDialogFragment(); // open search
            dialog.show(manager, "SearchDialog");
            return true;
        }
        return false;
    }
}
