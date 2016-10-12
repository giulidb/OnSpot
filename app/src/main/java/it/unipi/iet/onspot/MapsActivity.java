package it.unipi.iet.onspot;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class MapsActivity  extends AppCompatActivity implements OnMyLocationButtonClickListener, OnMapReadyCallback,
                                                                ActivityCompat.OnRequestPermissionsResultCallback {

        private GoogleMap mMap;
        //Request code for location permission request.
        private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
        //Flag indicating whether a requested permission has been denied after returning in.
        private boolean mPermissionDenied = false;

        // variabili di firebase
        private FirebaseAuth mAuth;
        private FirebaseAuth.AuthStateListener mAuthListener;
        String TAG = "MapsActivity";

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_maps);

            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

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


        }


        /*
         *  Google Maps functions
         */

        //Manipulates the map once available.
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;

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

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
            if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
                return;
            }

            if (PermissionUtilities.isPermissionGranted(permissions, grantResults,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Enable the my location layer if the permission has been granted.
                enableMyLocation();
            } else {
                // Display the missing permission error dialog when the fragments resume.
                mPermissionDenied = true;
            }
        }

        @Override
        protected void onResumeFragments() {
            super.onResumeFragments();
            if (mPermissionDenied) {
                // Permission was not granted, display error dialog.
                showMissingPermissionError();
                mPermissionDenied = false;
            }
        }

        //Displays a dialog with error message explaining that the location permission is missing.
        private void showMissingPermissionError() {
            PermissionUtilities.PermissionDeniedDialog.newInstance(true).show(getSupportFragmentManager(), "dialog");
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
                case R.id.menu:
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
        }

        @Override
        public void onStop() {
            super.onStop();
            if (mAuthListener != null) {
               mAuth.removeAuthStateListener(mAuthListener);
            }
        }


    }

