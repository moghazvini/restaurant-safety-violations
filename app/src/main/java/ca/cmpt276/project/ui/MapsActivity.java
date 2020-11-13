package ca.cmpt276.project.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

import ca.cmpt276.project.R;
import ca.cmpt276.project.model.Restaurant;
import ca.cmpt276.project.model.RestaurantListManager;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    //SupportMapFragment mapFragment;
    private GoogleMap mMap;
    private Boolean loc_Permission = false;
    private RestaurantListManager restaurantManager;
    List<LatLng> restaurantlatlag;
    private Location currentloc;
    //permissions
    //https://www.youtube.com/watch?v=Vt6H9TOmsuo&list=PLgCYzUzKIBE-vInwQhGSdnbyJ62nixHCt&index=4
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2000;
    private FusedLocationProviderClient mlocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Toolbar toolbar = findViewById(R.id.map_toolbar);
        setSupportActionBar(toolbar);

        restaurantManager = RestaurantListManager.getInstance();
        getUserLocal();
    }

    private void getUserLocal() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),COURSE_LOCATION)== PackageManager.PERMISSION_GRANTED){
                loc_Permission = true;
            }else{
                ActivityCompat.requestPermissions(this,permissions,LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this,permissions,LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        loc_Permission = false;
        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0){
                    for(int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            loc_Permission = false;
                            return;
                        }
                    }
                    loc_Permission = true;
                    //initialize our map
                    SetupMap();
                }
            }
        }
    }

    private void SetupMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
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
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        popLatlong();
        addMarkers(googleMap);
        if (loc_Permission==true) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
                deviceLocation();
                mMap.setMyLocationEnabled(true);
        }
    }

    private void deviceLocation() {
        mlocationClient = LocationServices.getFusedLocationProviderClient(this);
        try{
            if(loc_Permission){

                final Task location = mlocationClient.getLastLocation();
                location.addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        currentloc = (Location) task.getResult();
                        LatLng curr = new LatLng(currentloc.getLatitude(),currentloc.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(curr));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
                    }else{
                        Toast.makeText(MapsActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }catch (SecurityException e){
            Log.e("user location failure", "getDeviceLocation: SecurityException: " + e.getMessage() );
        }
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
        int pos = 0 ;
        for (LatLng current : restaurantlatlag){
            mMap.addMarker(new MarkerOptions().position(current).title(restaurantManager.getRestaurant(pos).getName()));
            pos++;
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
                startActivity(new Intent(this,RestaurantListActivity.class));
                return  true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}