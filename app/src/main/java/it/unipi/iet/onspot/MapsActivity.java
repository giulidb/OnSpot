package it.unipi.iet.onspot;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.text.DateFormat;
import java.util.List;

import it.unipi.iet.onspot.fragments.AddSpotFragment;
import it.unipi.iet.onspot.fragments.ListSpotFragment;
import it.unipi.iet.onspot.fragments.SearchFragment;
import it.unipi.iet.onspot.fragments.VisualizeSpotFragment;
import it.unipi.iet.onspot.utilities.ArrayAdapterWithIcon;
import it.unipi.iet.onspot.utilities.AuthUtilities;
import it.unipi.iet.onspot.utilities.DatabaseUtilities;
import it.unipi.iet.onspot.utilities.MarkerItem;
import it.unipi.iet.onspot.utilities.MultimediaUtilities;
import it.unipi.iet.onspot.utilities.PermissionUtilities;
import it.unipi.iet.onspot.utilities.Spot;


public class MapsActivity extends BaseActivity implements OnMapReadyCallback,
                                                                ConnectionCallbacks,
                                                                OnConnectionFailedListener,
                                                                LocationListener,
                                                                ClusterManager.OnClusterItemClickListener<MarkerItem>{

    // GoogleMaps variables
    private GoogleMap mGoogleMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LatLngBounds mLatLngBounds;
    private ClusterManager<MarkerItem> mClusterManager;
    private Marker marker;

    // Firebase variables
    private AuthUtilities AuthUt;

    // Permission request codes
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    public static final int PHOTO_PERMISSION_REQUEST_CODE = 2;
    public static final int VIDEO_PERMISSION_REQUEST_CODE = 3;
    public static final int MICROPHONE_PERMISSION_REQUEST_CODE = 5;

    // Multimedia Intent request codes
    final int IMAGE_REQUEST_CODE = 1;
    final int VIDEO_REQUEST_CODE = 2;
    final int AUDIO_REQUEST_CODE = 3;


    // Add Spot variables
    private BottomSheetDialogFragment AddSpotFrag;
    private String description;
    private String title;
    private String category;
    private String path;
    private String type;

    //Search variables
    private SearchFragment SearchFrag;

    String TAG = "MapsActivity";


    /*
     * Basic functions
     */

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
        SupportMapFragment mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);

        /*
         * Firebase part
         */

        AuthUt = new AuthUtilities();
        if(AuthUt.getUser() != null ){
            // if user is already logged changes activity
            Log.d(TAG,"User already logged");}

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

    //Disable going back function
    @Override
    public void onBackPressed() {
        // disable going back to the previous Activity
        moveTaskToBack(true);
    }


    /*
     * GoogleMaps functions
     */

    //TODO: sistemare parte di google maps in modo che zoommi di più senza sbagliare
    //TODO: e che quando riapri rimanga dov'era l'ultima volta

    //Manipulates the map once available.
    @Override
    public void onMapReady(final GoogleMap googleMap)
    {

        mGoogleMap=googleMap;

        // Initialize the Cluster marker manager with the context and the map.
        mClusterManager = new ClusterManager<>(this, mGoogleMap);
        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        mGoogleMap.setOnCameraIdleListener(mClusterManager);
        mGoogleMap.setOnMarkerClickListener(mClusterManager);
        mClusterManager.setOnClusterItemClickListener(this);
        //set ClusterRendered for marker icon and cluster color
        mClusterManager.setRenderer(new MarkerRenderer());


        /* Loading Markers from Firebase Database */
        final DatabaseReference newRef = FirebaseDatabase.getInstance().getReference().child("spots");
        newRef.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {

                    Spot spot = child.getValue(Spot.class);
                    Log.d(TAG, "Spot: " + spot.description);
                    // get category icon id
                    Integer id = getIconId(spot.category);
                    // Marker creation
                    MarkerItem offsetItem = new MarkerItem(id,spot);
                    // Add marker to Cluster Manager
                    mClusterManager.addItem(offsetItem);

                }

                mClusterManager.cluster();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.d(TAG, "loadUser:onCancelled", databaseError.toException());
                // ...
            }
        });


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

        /* Set mGooleMap on CameraMoveListener to get current visible region */
        mGoogleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                /* Initialization of coordinates of the projection of the viewing part map in the current state
        need for list of visible fragments */
                mLatLngBounds = mGoogleMap.getProjection().getVisibleRegion().latLngBounds;
            }
        });



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
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        //Requests location updates from the FusedLocationApi.
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                                                                    mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}

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
     * Menu functions
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
                //logout
                AuthUt.signOut();
                Intent i = new Intent(MapsActivity.this,MainActivity.class);
                startActivity(i);

        }
        return false;
    }

    // onClick for toolbar
    public void toolbarOnClick(View view){

        switch(view.getId()){
            case R.id.search_icon:
                SearchFrag = new SearchFragment();
                SearchFrag.show(getSupportFragmentManager(), SearchFrag.getTag());
                break;
            case R.id.list_spot:
                ListSpotFragment ListFrag = new ListSpotFragment();
                ListFrag.setLatLng(mLatLngBounds);
                ListFrag.show(getSupportFragmentManager(), ListFrag.getTag());
                break;
            case R.id.plus:
                AddSpotFrag = new AddSpotFragment();
                AddSpotFrag.show(getSupportFragmentManager(), AddSpotFrag.getTag());
                break;
            case R.id.account:
                Intent i = new Intent(MapsActivity.this,myProfileActivity.class);
                startActivity(i);
                break;

        }
    }


    /*
     * AddSpot functions
     */

    // Retrieve fragment
    public AddSpotFragment retrieveFragment() {
        return (AddSpotFragment) getSupportFragmentManager()
                .findFragmentById(AddSpotFrag.getId());
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
                                PHOTO_PERMISSION_REQUEST_CODE)&&
                                (PermissionUtilities.checkPermission(this,
                                        Manifest.permission.READ_EXTERNAL_STORAGE,
                                        PHOTO_PERMISSION_REQUEST_CODE))){
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
                                VIDEO_PERMISSION_REQUEST_CODE)){
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
                        (PermissionUtilities.checkPermission(this,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                MICROPHONE_PERMISSION_REQUEST_CODE))){
                            MultimediaUtilities.create_intent("audio", this);
                        }
                    }
                    else
                        MultimediaUtilities.create_intent("audio", this);

                    break;

            }
        }

    // Function callback automatically after choosing picture from gallery or camera
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Retrieve Fragment
        AddSpotFragment fragment = retrieveFragment();

                if (data != null) {
                    // Get uri of the file
                    Uri uri = data.getData();
                    // Get the actual path
                    path = MultimediaUtilities.getRealPathFromURI(this,uri);
                    Bitmap bm = null;

                    switch(requestCode){
                        case IMAGE_REQUEST_CODE:
                            try {
                                bm = MediaStore.Images.Media.getBitmap(getApplicationContext()
                                        .getContentResolver(), data.getData());
                                } catch (IOException e) {
                                e.printStackTrace();
                            }
                            Log.d(TAG,"Path of bitmap= "+ path);
                            // Get right orientation of the image
                            bm = MultimediaUtilities.rotateBitmap(bm,path);
                            // Resize image
                            //TODO: decidere se va bene la max size
                            bm = MultimediaUtilities.resize(bm,180,180);
                            //save file type
                            type = "image";
                            // Set image as preview in fragment
                            fragment.set_preview(bm);
                            break;

                        case AUDIO_REQUEST_CODE:
                            //save file type
                            type = "audio";
                            fragment.add_audio_button();
                            break;

                        case VIDEO_REQUEST_CODE:

                            Log.d(TAG,"Path of video= "+ path);
                            // extract frame from video
                            MediaMetadataRetriever mRetriever = new MediaMetadataRetriever();
                            mRetriever.setDataSource(path);
                            bm = (mRetriever.getFrameAtTime(0));
                            bm = MultimediaUtilities.resize(bm,180,180);
                            //save file type
                            type = "video";
                            // Set image as preview in fragment
                            fragment.set_preview(bm);
                            // Add play button to reproduce video if clicked
                            fragment.add_play_button();
                    }





                }
    }

    // Function to reproduce media
    public void reproduce_media(View view){

        if (path != null){
        Log.d(TAG,"reproduce_media");
            MultimediaUtilities.open_media(view.getId(),path,this);
       }
    }

    // Function for categories
    public void show_categories(View view){
        final String [] items = getResources().getStringArray(R.array.categories_names);
        final TypedArray icons = getResources().obtainTypedArray(R.array.categories_icons);
        ListAdapter adapter = new ArrayAdapterWithIcon(this, items,icons);
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this,R.style.AppDialog);
        builder.setTitle("Choose a category")
                .setAdapter(adapter,new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        String[] categories = getResources().getStringArray(R.array.categories_names);
                        Log.d(TAG,"Category "+ categories[which] + " selected");
                        //Retrieve Fragment
                        AddSpotFragment fragment = retrieveFragment();
                        fragment.setCategory(categories[which]);
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Function to store the spot just added by the user in the DB
    public void saveSpot(View view) {

        // Retrieve description and category
        final AddSpotFragment fragment = retrieveFragment();
        description = fragment.getDescription();
        category = fragment.getCategory();
        title = fragment.getTitle();
        Log.d(TAG,"Path to media file is"+path);

        //Check if there is something missing in the fields
        if(!formIsValid())
            return;

        showProgressDialog();

        //Retrieve userId
        String userId = AuthUt.getUser().getUid();

        //Start the task to save spot multimedia content in the storage
        StorageReference storageRef = FirebaseStorage.getInstance()
                                        .getReferenceFromUrl("gs://onspot-8c6f4.appspot.com/");
        Uri file = Uri.fromFile(new File(path));

        StorageReference childRef = storageRef.child(userId+"/"+file.getLastPathSegment());
        Log.d(TAG,"File Storage Reference: "+ childRef.getPath());
        UploadTask uploadTask = childRef.putFile(file);
        uploadTask.addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Upload succeeded
                Log.d(TAG, "Entered in onSuccess");
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                String contentURL = null;
                if (downloadUrl != null) {
                    contentURL = downloadUrl.toString();
                }
                saveSpotOnDatabase(contentURL);
                hideProgressDialog();
                fragment.dismiss();

                Toast.makeText(MapsActivity.this, "Spot saved correctly",
                        Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Upload failed
                Log.w(TAG, "Entered in onFailure", exception);
                hideProgressDialog();
                Toast.makeText(MapsActivity.this, "Error: upload failed",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Observe state change events such as progress, pause, and resume
        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                Log.d(TAG,"Upload is " + progress + "% done");
                setmProgressDialog((int)progress);
            }
        }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG,"Upload is paused");
            }
        });


    }

    //Check if there is something missing in the fields
    boolean formIsValid() {

        if(title.isEmpty()) {
            Toast.makeText(this, "Title field cannot be empty.", Toast.LENGTH_LONG).show();
            return false;
        }

        if(description.isEmpty()) {
            Toast.makeText(this, "Description field cannot be empty.", Toast.LENGTH_LONG).show();
            return false;
        }
        if(category.isEmpty()) {
            Toast.makeText(this, "Category field cannot be empty.", Toast.LENGTH_LONG).show();
            return false;
        }
        if(path.isEmpty()) {
            Toast.makeText(this, "You have to load a photo, video or audio track.",
                    Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    // Function to retrieve the final info to store in the database
    void saveSpotOnDatabase(String contentURL) {

        Log.d(TAG, "Entered in saveSpotonDatabase with contentURL "+contentURL);

        //Retrieve userId
        String userId = AuthUt.getUser().getUid();

        //Retrieve time and date
        String currentTime = DateFormat.getDateTimeInstance().format(new Date());
        Log.d(TAG, "currentTime "+currentTime);

        //Retrieve location info
        //TODO: vedere se c'è un modo migliore di prendere la location
        //TODO: se location non è abilitata crasha

            double Lat = mLastLocation.getLatitude();
            double Lng = mLastLocation.getLongitude();
            //Save spot info in the db
            DatabaseUtilities db = new DatabaseUtilities();
            db.writeNewSpot(userId, description, title,category, contentURL, Lat, Lng, currentTime,type);


    }


    /*
     * Search functions
     */

    // Function called when the user presses Search
    public void onMapSearch(View view) {
        SearchFragment fragment = (SearchFragment) getSupportFragmentManager()
                .findFragmentById(SearchFrag.getId());
        String location = fragment.getLocation();
        List<Address> addressList = null;

        Geocoder geocoder = new Geocoder(this);
        try {
            addressList = geocoder.getFromLocationName(location, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(addressList!=null) {
            Address address = addressList.get(0);
            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

            // Check if the user has previously used search function if does remove previous marker
            if(marker != null )
                marker.remove();

            marker = mGoogleMap.addMarker(new MarkerOptions().position(latLng).title("Marker"));

            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        } else {
            Toast.makeText(this, "Invalid location", Toast.LENGTH_SHORT).show();
        }
        fragment.dismiss();
    }

      /*
     *  Markers functions
     */


     // Function that returnd marker drawable id for spot category
      public Integer getIconId(String category){
          final String [] items = getResources().getStringArray(R.array.categories_names);
          int index = Arrays.asList(items).indexOf(category);
          TypedArray marker =  getResources().obtainTypedArray(R.array.categories_markers);
          Log.d(TAG,"id: "+index);
          return marker.getResourceId(index,-1);

      }

    /**
     * Draws category icons inside markers.
     * Customize color clusters
     */

    public class MarkerRenderer  extends DefaultClusterRenderer<MarkerItem> {

        public MarkerRenderer() {
            super(getApplicationContext(), mGoogleMap, mClusterManager);
        }

        @Override
        protected void onBeforeClusterItemRendered(MarkerItem item, MarkerOptions markerOptions) {
            // Draw a single marker.
            // Set the info window to show their name.
            markerOptions.icon(BitmapDescriptorFactory.fromResource(item.icon_id)).title(item.spot.title);
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            // Render clusters for more than 2 markers
            return cluster.getSize() > 2;
        }

        // Customize Cluster Color with primary Color of the app
        @Override
        protected int getColor(int clusterSize) {
            return Color.parseColor("#C45852");
        }
    }

    // Function to menage clicks on markers

    @Override
    public boolean onClusterItemClick(MarkerItem item) {
        // Does nothing, but you could go into the user's profile page, for example.
        // Retrieve the data from the marker.
        Log.d(TAG, "onClusterItemClick Marker Clicked: "+ item.spot.description);

        VisualizeSpotFragment VisualizeSpotFrag = new VisualizeSpotFragment();
        VisualizeSpotFrag.setSpot(item.spot);
        VisualizeSpotFrag.show(getSupportFragmentManager(), VisualizeSpotFrag.getTag());
        return false;
    }




    /*
     * Functions for permission handling.
     */

    //TODO: controllare se effettivamente funziona tutta 'sta roba
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {

            //Calls buildGoogleApiClient() if the fine location permission has been granted,
            // after being requested.
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


            case PHOTO_PERMISSION_REQUEST_CODE:
                if (PermissionUtilities.isPermissionGranted(permissions, grantResults,
                        Manifest.permission.CAMERA) ||PermissionUtilities
                        .isPermissionGranted(permissions, grantResults,
                        Manifest.permission.READ_EXTERNAL_STORAGE) ) {

                    // permission was granted
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(this,
                                    Manifest.permission.READ_EXTERNAL_STORAGE)
                                    == PackageManager.PERMISSION_GRANTED) {

                        // Call intent for image
                        MultimediaUtilities.create_intent("image", this);
                    }
                }
                    else {
                    // permission denied
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();}

            case VIDEO_PERMISSION_REQUEST_CODE:
                if (PermissionUtilities.isPermissionGranted(permissions, grantResults,
                        Manifest.permission.CAMERA)) {

                    // permission was granted
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_GRANTED)  {

                        // Call intent for video
                        MultimediaUtilities.create_intent("video", this);
                    }
                } else {
                    // permission denied
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                    break;

            case MICROPHONE_PERMISSION_REQUEST_CODE:
                if (PermissionUtilities.isPermissionGranted(permissions, grantResults,
                        Manifest.permission.RECORD_AUDIO) || PermissionUtilities
                        .isPermissionGranted(permissions, grantResults,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {

                    // permission was granted
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.RECORD_AUDIO)
                            == PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(this,
                                    Manifest.permission.READ_EXTERNAL_STORAGE)
                                    == PackageManager.PERMISSION_GRANTED) {

                        // Call intent for audio
                        MultimediaUtilities.create_intent("audio", this);
                    }
                } else {
                    // permission denied
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }

        }
    }




}




