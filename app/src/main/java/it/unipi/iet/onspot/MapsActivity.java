package it.unipi.iet.onspot;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnMenuTabSelectedListener;


public class MapsActivity extends AppCompatActivity implements ConnectionCallbacks, OnConnectionFailedListener, OnMapReadyCallback, OnMyLocationButtonClickListener, ActivityCompat.OnRequestPermissionsResultCallback, LocationListener  {

        private GoogleMap mMap;

        //Request code for location permission request.
        private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
        //Flag indicating whether a requested permission has been denied after returning in.
        private boolean mPermissionDenied = false;

        // Keys for storing activity state in the Bundle.
        protected final static String LOCATION_KEY = "location-key";
        //The desired interval for location updates. Inexact. Updates may be more or less frequent.
        public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
        //The fastest rate for active location updates. Exact. Updates will never be more frequent than this value.
        public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
        //Tracks the status of the location updates request. Value changes when the user presses the Start Updates and Stop Updates buttons.
        protected Boolean mRequestingLocationUpdates;

        //Provides the entry point to Google Play services.
        protected GoogleApiClient mGoogleApiClient;
        //Stores parameters for requests to the FusedLocationProviderApi.
        protected LocationRequest mLocationRequest;
        //Represents a geographical location.
        protected Location mCurrentLocation;

        // variabili di firebase
        private FirebaseAuth mAuth;
        private FirebaseAuth.AuthStateListener mAuthListener;
        String TAG = "MapsActivity";

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_maps);

            mRequestingLocationUpdates = true;

            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = SupportMapFragment.newInstance();
            FragmentTransaction fragTransaction = getSupportFragmentManager().beginTransaction();
            fragTransaction.add(R.id.fragment_container, mapFragment);
            fragTransaction.commit();
            mapFragment.getMapAsync(this);

            // Update values using data stored in the Bundle.
            updateValuesFromBundle(savedInstanceState);
            // Kick off the process of building a GoogleApiClient and requesting the LocationServices API.
            buildGoogleApiClient();

            /*
            * Parte di Firebase aggiunta
            * */
            mAuth = FirebaseAuth.getInstance();
            // auth state listener initialization
            mAuthListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user != null) {
                        // User is signed in
                        Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    } else {
                        // User is signed out
                        Log.d(TAG, "onAuthStateChanged:signed_out");
                        //if user logged out changes activity to Main Activity
                        Intent i = new Intent(MapsActivity.this,MainActivity.class);
                        startActivity(i);
                    }

                }
            };

              /*
            Bottom bar
             */
            Log.d(TAG,"Pre bottom action bar");
            BottomBar bottomBar = BottomBar.attach(this, savedInstanceState);
            bottomBar.useDarkTheme(true);
            bottomBar.setItemsFromMenu(R.menu.bottom_menu, new OnMenuTabSelectedListener() {
                @Override
                public void onMenuItemSelected(int itemId) {
                    switch (itemId) {
                        //TODO: switch case
                    }
                }
            });

        }

        /*
        *  Google Maps functions
        */

        //Updates fields based on data stored in the bundle.
        private void updateValuesFromBundle(Bundle savedInstanceState) {
            Log.i(TAG, "Updating values from bundle");
            if (savedInstanceState != null) {
                // Update the value of mCurrentLocation from the Bundle
                if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                    // Since LOCATION_KEY was found in the Bundle, we can be sure that mCurrentLocation
                    // is not null.
                    mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
                }
            }
        }

        //Builds a GoogleApiClient. Uses the addApi() method to request the LocationServices API.
        protected synchronized void buildGoogleApiClient() {
            Log.i(TAG, "Building GoogleApiClient");
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            createLocationRequest();
        }

        //Sets up the location request.
        protected void createLocationRequest() {
            mLocationRequest = new LocationRequest();

            // Sets the desired interval for active location updates.
            mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

            // Sets the fastest rate for active location updates.
            mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }

        //Requests location updates from the FusedLocationApi.
        protected void startLocationUpdates() {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

        //Removes location updates from the FusedLocationApi.
        protected void stopLocationUpdates() {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }

        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        @Override
        protected void onPause() {
            super.onPause();
            if (mGoogleApiClient.isConnected()) {
                stopLocationUpdates();
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
                startLocationUpdates();
            }
        }

        //Runs when a GoogleApiClient object successfully connects.
        @Override
        public void onConnected(Bundle connectionHint) {
            // If the initial location was never previously requested, we use
            // FusedLocationApi.getLastLocation() to get it. If it was previously requested, we store
            // its value in the Bundle and check for it in onCreate(). We
            // do not request it again unless the user specifically requests location updates by pressing
            // the Start Updates button.
            //
            // Because we cache the value of the initial location in the Bundle, it means that if the
            // user launches the activity,
            // moves to a new location, and then changes the device orientation, the original location
            // is displayed as the activity is re-created.
            if (mCurrentLocation == null) {
                mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            }

            startLocationUpdates();
        }

        //Callback that fires when the location changes.
        @Override
        public void onLocationChanged(Location location) {
            mCurrentLocation = location;
            Toast.makeText(this, getResources().getString(R.string.location_updated_message),
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onConnectionFailed(ConnectionResult result) {
            Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
        }


        @Override
        public void onConnectionSuspended(int cause) {
            // The connection to Google Play services was lost for some reason. We call connect() to
            // attempt to re-establish the connection.
            Log.i(TAG, "Connection suspended");
            mGoogleApiClient.connect();
        }

        //Stores activity data in the Bundle.
        public void onSaveInstanceState(Bundle savedInstanceState) {
            savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
            super.onSaveInstanceState(savedInstanceState);
        }

        //Manipulates the map once available.
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;
            enableMyLocation();
            LatLng pos;
            Log.d(TAG, "entrato in map ready");

            if (mCurrentLocation != null) {
                pos = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                Log.d(TAG, "location ottenuta");
            } else {
                Log.d(TAG, "location nulla");
                Toast.makeText(this, R.string.no_location_detected, Toast.LENGTH_LONG).show();
                pos = new LatLng(0,0);
            }
            // Add a marker in the current position and move the camera
            mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(20), 2000, null);
            mMap.setOnMyLocationButtonClickListener(this);
        }

        //Enables the My Location layer if the fine location permission has been granted.
        private void enableMyLocation() {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // Permission to access the location is missing.
                PermissionUtilities.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE, android.Manifest.permission.ACCESS_FINE_LOCATION, true);
            } else if (mMap != null) {
                // Access to the location has been granted to the app.
                mMap.setMyLocationEnabled(true);
            }
        }

        @Override
        public boolean onMyLocationButtonClick() {
            Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
            // Return false so that we don't consume the event and the default behavior still occurs
            // (the camera animates to the user's current position).
            return false;
        }

        //Calls enableMyLocation() if the fine location permission has been granted, after being requested.
        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
            if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
                return;
            }

            if (PermissionUtilities.isPermissionGranted(permissions, grantResults,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                enableMyLocation();
            } else {
                // Display the missing permission error dialog when the fragments resume.
                mPermissionDenied = true;
            }
        }

        // Display the missing permission error dialog when the fragments resume.
        @Override
        protected void onResumeFragments() {
            super.onResumeFragments();
            if (mPermissionDenied) {
                // Permission was not granted, display error dialog.
                PermissionUtilities.PermissionDeniedDialog.newInstance(true).show(getSupportFragmentManager(), "dialog");
                mPermissionDenied = false;
            }
        }


        /*
         * Disable going back function
         */
        @Override
        public void onBackPressed() {
            // disable going back to the previous Activity
            moveTaskToBack(true);
        }


        /*
        Parte di codice aggiunto solo per provare la funzione logout e se le altre activity
        funzionano poi la puoi riorganizzare come ti torna
        */
        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu, menu);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            switch (id) {
                case R.id.logout:
            /*
                 logout
             */
                    mAuth.signOut();

            }
            return false;
        }

        @Override
        public void onStart() {
            super.onStart();
            mAuth.addAuthStateListener(mAuthListener);
            mGoogleApiClient.connect();
        }

        @Override
        public void onStop() {
            super.onStop();
            if (mAuthListener != null) {
               mAuth.removeAuthStateListener(mAuthListener);
            }
            if (mGoogleApiClient.isConnected()) {
                mGoogleApiClient.disconnect();
            }
        }

}

