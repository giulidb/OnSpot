package it.unipi.iet.onspot.fragments;


import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;


public class MyImagesFragment extends ImageListFragment {

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        // My top spots
        Log.d("ImageListFragment","Query for my images");
        String myUserId = getUid();
        Query myPostsQuery = databaseReference.child("spots").orderByChild("userId").equalTo(myUserId);
        return myPostsQuery;
    }
}
