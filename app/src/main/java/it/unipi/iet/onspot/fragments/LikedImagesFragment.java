package it.unipi.iet.onspot.fragments;


import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class LikedImagesFragment extends ImageListFragment {
    public LikedImagesFragment() {}

    //TODO: change query
    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        Log.d("ImageListFragment","Query for favourite images");
        Query favQuery = databaseReference.child("spots");

        return favQuery ;
    }
}