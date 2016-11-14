package it.unipi.iet.onspot.fragments;


import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;


public class LikedImagesFragment extends ImageListFragment {

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        Log.d("ImageListFragment","Query for favourite images");
        String myUserId = getUid();
        return databaseReference.child("spots").orderByChild("hearts/"+myUserId).equalTo(true);
    }
}