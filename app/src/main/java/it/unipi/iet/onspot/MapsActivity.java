package it.unipi.iet.onspot;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.io.IOException;
import it.unipi.iet.onspot.utilities.AuthUtilities;
import it.unipi.iet.onspot.utilities.MultimediaUtilities;
import it.unipi.iet.onspot.utilities.PermissionUtilities;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
                                                                ConnectionCallbacks,
                                                                OnConnectionFailedListener,
                                                                LocationListener {

    // GoogleMaps variables
    GoogleMap mGoogleMap;
    SupportMapFragment mapFrag;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;

    // Firebase variables
    private AuthUtilities AuthUt;

    // Permission request codes
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    public static final int CAMERA_PERMISSION_REQUEST_CODE = 2;
    public static final int READ_EXTERNAL_PERMISSION_REQUEST_CODE = 3;
    public static final int WRITE_EXTERNAL_PERMISSION_REQUEST_CODE = 4;
    public static final int MICROPHONE_PERMISSION_REQUEST_CODE = 5;

    // Multimedia Intent request codes
    final int IMAGE_REQUEST_CODE = 1;
    final int VIDEO_REQUEST_CODE = 2;
    final int AUDIO_REQUEST_CODE = 3;

    // BottomSheetFragment
    BottomSheetDialogFragment AddSpotFrag;

    // Media file path
    String path;

    String TAG = "MapsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        /*
         * GoogleMaps part
         */

        // Check for location permission at runtime for Android version 6.0 or greater (API level 23)
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d(TAG, "Permission check at runtime");
            PermissionUtilities.checkPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION,
                            LOCATION_PERMISSION_REQUEST_CODE);
        }
        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);

        /*
         * Firebase part
         */

        AuthUt = new AuthUtilities();
        if(AuthUt.getUser() != null ){
            // if user is already logged changes activity
            Log.d(TAG,"User already logged");}

    }

    /*
     * GoogleMaps functions
     */

    //Manipulates the map once available.
    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mGoogleMap=googleMap;

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mGoogleMap.setMyLocationEnabled(true);
            }
        }
        else {
            buildGoogleApiClient();
            mGoogleMap.setMyLocationEnabled(true);
        }
    }

    //Builds a GoogleApiClient. Uses the addApi() method to request the LocationServices API.
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    //Runs when a GoogleApiClient object successfully connects.
    @Override
    public void onConnected(Bundle bundle)
    {
        //Sets up the location request.
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        //Requests location updates from the FusedLocationApi.
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {}

    //Callback that fires when the location changes.
    @Override
    public void onLocationChanged(Location location)
    {
        mLastLocation = location;

        //move map camera
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(11));

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
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

    /* Functions for the menu*/
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
                //logout
                AuthUt.signOut();
                Intent i = new Intent(MapsActivity.this,MainActivity.class);
                startActivity(i);

        }
        return false;
    }

    @Override
    public void onStart() {
        super.onStart();
        AuthUt.addListner();
    }

    @Override
    public void onStop() {
        super.onStop();
        AuthUt.removeListener();

    }

    @Override
    public void onPause() {
        super.onPause();

        //stop location updates when Activity is no longer active
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    // onClick for toolbar
    public void toolbarOnClick(View view){

        switch(view.getId()){
            case R.id.plus:
                AddSpotFrag = new AddSpotFragment();
                AddSpotFrag.show(getSupportFragmentManager(), AddSpotFrag.getTag());

        }
    }

    // onClick for add_spot functions
    public void AddSpot_onClick(View view){
        Log.d(TAG,"View clicked"+ view.getId());

            switch (view.getId()) {
                case R.id.image_add_spot:
                    Log.d(TAG, "Image clicked");

                    // Permission
                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Log.d(TAG, "Permission check at runtime");
                        if(PermissionUtilities.checkPermission(this,Manifest.permission.CAMERA,
                                CAMERA_PERMISSION_REQUEST_CODE)&&
                                (PermissionUtilities.checkPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE,
                                READ_EXTERNAL_PERMISSION_REQUEST_CODE))){
                            MultimediaUtilities.create_intent("image", this);
                        }
                    }
                    else
                            MultimediaUtilities.create_intent("image", this);

                    break;

                case R.id.video_add_spot:
                    Log.d(TAG, "Video clicked");
                    // Permission
                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Log.d(TAG, "Permission check at runtime");
                        if(PermissionUtilities.checkPermission(this,Manifest.permission.CAMERA,
                                CAMERA_PERMISSION_REQUEST_CODE)&&
                                PermissionUtilities.checkPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE,
                                        WRITE_EXTERNAL_PERMISSION_REQUEST_CODE)){
                        MultimediaUtilities.create_intent("video", this);
                        }
                    }
                    else
                        MultimediaUtilities.create_intent("video", this);

                    break;

                case R.id.audio_add_spot:
                    Log.d(TAG, "Audio clicked");
                    // Permission
                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Log.d(TAG, "Permission check at runtime");
                        if(PermissionUtilities.checkPermission(this,Manifest.permission.RECORD_AUDIO,
                                MICROPHONE_PERMISSION_REQUEST_CODE)&&
                        (PermissionUtilities.checkPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE,
                                READ_EXTERNAL_PERMISSION_REQUEST_CODE))){
                            MultimediaUtilities.create_intent("audio", this);
                        }
                    }
                    else
                        MultimediaUtilities.create_intent("audio", this);

                    break;

            }
        }



    // function callback automatically after choosing picture from gallery or camera
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Retrieve Fragment
        AddSpotFragment fragment = (AddSpotFragment) getSupportFragmentManager().findFragmentById(AddSpotFrag.getId());

                if (data != null) {
                    // Get uri of the file
                    Uri uri = data.getData();
                    // Get the actual path
                    path = MultimediaUtilities.getRealPathFromURI(this,uri);
                    Bitmap bm = null;

                    switch(requestCode){
                        case IMAGE_REQUEST_CODE:
                            try {
                                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                                } catch (IOException e) {
                                e.printStackTrace();
                            }
                            Log.d(TAG,"Path of bitmap= "+ path);
                            // Get right orientation of the image
                            bm = MultimediaUtilities.rotateBitmap(bm,path);
                            // Resize image
                            //TODO:decidere se va bene la max size
                            bm = MultimediaUtilities.resize(bm,180,180);
                            // Set image as preview in fragment
                            fragment.set_preview(bm);
                            break;

                        case AUDIO_REQUEST_CODE:
                            fragment.add_audio_button();
                            break;

                        case VIDEO_REQUEST_CODE:

                            Log.d(TAG,"Path of video= "+ path);
                            // extract frame from video
                            MediaMetadataRetriever mRetriever = new MediaMetadataRetriever();
                            mRetriever.setDataSource(path);
                            bm = (mRetriever.getFrameAtTime(0));
                            bm = MultimediaUtilities.resize(bm,180,180);
                            // Set image as preview in fragment
                            fragment.set_preview(bm);
                            // Add play button to reproduce video if clicked
                            fragment.add_play_button();
                    }





                }
    }

    /* Function to reproduce media */
    public void reproduce_media(View view){

        if (path != null){
        Log.d(TAG,"reproduce_media");
            MultimediaUtilities.open_media(view.getId(),path,this);
       }
    }

    /* Function for categories */
    public void show_categories(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this,R.style.AppDialog);
        builder.setTitle("Choose a category")
                .setItems(R.array.categories, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        String[] categories = getResources().getStringArray(R.array.categories);
                        Log.d(TAG,"Category "+ categories[which] + " selected");
                        // Retrieve Fragment
                        AddSpotFragment fragment = (AddSpotFragment) getSupportFragmentManager().findFragmentById(AddSpotFrag.getId());
                        fragment.setCategory(categories[which]);
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /*
     * Functions for permission handling.
     */

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {

            //Calls buildGoogleApiClient() if the fine location permission has been granted, after being requested.
            case LOCATION_PERMISSION_REQUEST_CODE:
                // If request is cancelled, the result arrays are empty.
                if (PermissionUtilities.isPermissionGranted(permissions, grantResults,
                        android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                    // permission was granted
                    if (ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mGoogleMap.setMyLocationEnabled(true);
                    }

                } else {
                    // permission denied
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                break;


            case CAMERA_PERMISSION_REQUEST_CODE:
                if (PermissionUtilities.isPermissionGranted(permissions, grantResults,
                        Manifest.permission.CAMERA)) {

                    // permission was granted
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_GRANTED) {
                    }
                }
                    else {
                    // permission denied
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();}
                break;

            case READ_EXTERNAL_PERMISSION_REQUEST_CODE:
                if (PermissionUtilities.isPermissionGranted(permissions, grantResults,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {

                    // permission was granted
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.READ_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED) {


                    }
                }else {
                        // permission denied
                        Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                    }
                break;

            case WRITE_EXTERNAL_PERMISSION_REQUEST_CODE:
                if (PermissionUtilities.isPermissionGranted(permissions, grantResults,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                    // permission was granted
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED) {
                        MultimediaUtilities.create_intent("video", this);
                    }
                } else {
                    // permission denied
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                    break;

            case MICROPHONE_PERMISSION_REQUEST_CODE:
                if (PermissionUtilities.isPermissionGranted(permissions, grantResults,
                        Manifest.permission.RECORD_AUDIO)) {

                    // permission was granted
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.RECORD_AUDIO)
                            == PackageManager.PERMISSION_GRANTED) {
                        MultimediaUtilities.create_intent("audio", this);
                    }
                } else {
                    // permission denied
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }

        }
    }

}

