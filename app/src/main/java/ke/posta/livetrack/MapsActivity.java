package ke.posta.livetrack;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ke.posta.livetrack.databinding.ActivityMapsBinding;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private static final String TAG = "TAG";
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    float zoomLevel = 17.0f; //This goes up to 21
    private LocationManager locationManager;
    private final int MIN_TIME = 1000;
    private final int MIN_DISTANCE = 1;

    private static LatLng NEW_PLACE;
    private FusedLocationProviderClient mfusedLocationProviderClient;
    private Boolean mLocationPermissionGrated = false;
    Marker mMarker, mMarker1;

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 111;
    Location currentLocation;
    private static final int REQUEST_CHECK_SETTINGS = 11;

 //   private static final float DEFAULT_ZOOM = 17;

    private DatabaseReference databaseReference;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

       // FirebaseDatabase.getInstance().getReference().setValue("live track data app");
        databaseReference = FirebaseDatabase.getInstance().getReference().child("user_101");

        createLocationRequest();
        getLocationPermissions();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
        }

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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

        MapStyleOptions mapStyleOptions = MapStyleOptions.loadRawResourceStyle(this, R.raw.googlemap_json);
        googleMap.setMapStyle(mapStyleOptions);

        if (mLocationPermissionGrated) {
            getDeviceLocation(googleMap);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);

        }

        /**
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-1.290939,36.825430);
        mMap.addMarker(new MarkerOptions().position(NEW_PLACE).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(NEW_PLACE));

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(NEW_PLACE,zoomLevel));
        **/
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
       // location.getLatitude();
       // location.getLongitude();
        Log.d(TAG, "onLocationChanged: "+location.getLatitude()+" "+location.getLongitude());
        mMap.clear();
        NEW_PLACE = new LatLng(location.getLatitude(), location.getLongitude());

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(NEW_PLACE, zoomLevel);
        if(mMap != null) {
            mMap.animateCamera(cameraUpdate);
            mMap.addMarker(new MarkerOptions().position(NEW_PLACE).title("Marker in Sydney"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(NEW_PLACE));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(NEW_PLACE,zoomLevel));
        }

//        if(ourGlobalMarker == null) { // First time adding marker to map
//            ourGlobalMarker = mGoogleMap.addMarker(new MarkerOptions().position(latLng)
//                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));
//            MarkerAnimation.animateMarkerToICS(ourGlobalMarker, latLng, new LatLngInterpolator.Spherical());
//            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
//        } else {
//            MarkerAnimation.animateMarkerToICS(ourGlobalMarker, latLng, new LatLngInterpolator.Spherical());
//            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
//        }

    //    databaseReference.setValue(location);

        CurrentLocation currentLocation = new CurrentLocation(location.getLatitude(), location.getLongitude());
// pushing user to 'users' node using the userId
        databaseReference.child("user_101").setValue(currentLocation);

        databaseReference.child("user_101").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                CurrentLocation currentLocation = snapshot.getValue(CurrentLocation.class);

                if (currentLocation != null){
                    Log.d(TAG, "onDataChange: "+currentLocation.getLat());
                } else {
                    Log.d(TAG, "onDataChange: "+" NOT FOUND");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: "+error);
            }


        });


    }

    @Override
    public void onFlushComplete(int requestCode) {
        LocationListener.super.onFlushComplete(requestCode);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        LocationListener.super.onStatusChanged(provider, status, extras);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        LocationListener.super.onProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        LocationListener.super.onProviderDisabled(provider);
    }

    private void getLocationPermissions() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGrated = true;
                //initialize map

            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mLocationPermissionGrated = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGrated = false;
                            return;
                        }
                    }
                    mLocationPermissionGrated = true;
                    //initialize map

                }
            }
        }

    }




    private void getDeviceLocation(GoogleMap googleMap){
        Log.d(TAG, "getDeviceLocation: getting device location");


        mfusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if(mLocationPermissionGrated){

                Task location = mfusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        Log.d(TAG, "onComplete: "+ task.getResult());
                        if (task.getResult() != null){
                            if(task.isSuccessful()){
                                Log.d(TAG, "getDeviceLocation: device location FOUND");

                                currentLocation = (Location) task.getResult();
                                Log.d(TAG, "onComplete: "+"lat "+ currentLocation.getLatitude());
                                Log.d(TAG, "onComplete: "+"lng "+ currentLocation.getLongitude());
                                Log.d(TAG, "onComplete: "+"place ");
                                NEW_PLACE = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

                                mMap = googleMap;
                                // Add a marker in Sydney and move the camera
                                LatLng sydney = new LatLng(-1.290939,36.825430);
                                mMap.addMarker(new MarkerOptions().position(NEW_PLACE).title("Marker in Sydney"));
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(NEW_PLACE));
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(NEW_PLACE,zoomLevel));




                            } else{
                                Log.d(TAG, "getDeviceLocation: device location NOT FOUND");
                                Snackbar.make(findViewById(android.R.id.content), "Device Location NOT FOUND!", Snackbar.LENGTH_LONG)
                                        .setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.black))
                                        .setTextColor(Color.parseColor("#FFFFFF"))

                                        .show();
                            }
                        }

                    }
                });
            }
        } catch (SecurityException e) {
            Log.d(TAG, "getDeviceLocation: SecurityException:" + e.getMessage());
            e.printStackTrace();
        }

    }

    protected void createLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                locationSettingsResponse.getLocationSettingsStates();

            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    Toast.makeText(getBaseContext(), "turn on location", Toast.LENGTH_LONG).show();
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MapsActivity.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
    }






    private void getLocationUpdate() {
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME,
                            MIN_DISTANCE,
                            this
                    );

                } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME,
                            MIN_DISTANCE,
                            this
                    );
                } else {
                    Toast.makeText(this, "no provider", Toast.LENGTH_LONG).show();
                }
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }

        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == 101){
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
//                getLocationUpdate();
//            } else {
//                Toast.makeText(this, "permission required", Toast.LENGTH_LONG).show();
//            }
//        }
//    }





}// end class