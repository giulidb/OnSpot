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


public class MapsActivity extends AppCompatActivity implements ConnectionCallbacks, OnConnectionFailedListener, OnMapReadyCallback, OnMyLocationButtonClickListener, ActivityCompat.OnRequestPermissionsResultCallback  {

        private GoogleMap mMap;
        //Request code for location permission request.
        private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
        //Flag indicating whether a requested permission has been denied after returning in.
        private boolean mPermissionDenied = false;

        //Provides the entry point to Google Play services.
        protected GoogleApiClient mGoogleApiClient;

        //Represents a geographical location.
        protected Location mLastLocation;


        // variabili di firebase
        private FirebaseAuth mAuth;
        private FirebaseAuth.AuthStateListener mAuthListener;
        String TAG = "MapsActivity";

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_maps);

            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = SupportMapFragment.newInstance();
            FragmentTransaction fragTransaction = getSupportFragmentManager().beginTransaction();
            fragTransaction.add(R.id.fragment_container, mapFragment);
            fragTransaction.commit();
            mapFragment.getMapAsync(this);

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

    /**
     * Builds a GoogleApiClient. Uses the addApi() method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        // Provides a simple way of getting a device's location and is well suited for
        // applications that do not require a fine-grained location and that do not need location
        // updates. Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
        Log.d(TAG, "entrato in on connected");
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        Log.d(TAG, "esco da on connected");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

        /*
         *  Google Maps functions
         */

        //Manipulates the map once available.
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;
            LatLng pos;
            Log.d(TAG, "entrato in map ready");

            if (mLastLocation != null) {
                pos = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
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
            enableMyLocation();
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

