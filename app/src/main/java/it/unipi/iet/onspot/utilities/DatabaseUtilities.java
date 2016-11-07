package it.unipi.iet.onspot.utilities;

import android.app.Activity;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import it.unipi.iet.onspot.myProfileActivity;

/**
 * Firebase Database utilities
 */

public class DatabaseUtilities {

    private DatabaseReference mDatabase;
    private String TAG = "DatabaseUtilities";

    public DatabaseUtilities(){

        // initialize reference
        Log.d(TAG,"DB initilized");
        mDatabase = FirebaseDatabase.getInstance().getReference();

    }

    // Insert a new user in the db
    public void writeNewUser(String userId, String name, String lastName,String photoURL, String birthday,String gender) {

        User user = new User(name, lastName, photoURL, birthday, gender);
        mDatabase.child("users").child(userId).setValue(user);

    }

    // Insert a new spot in the db
    public void writeNewSpot(String userId, String description, String title,String category, String contentURL,
                             double Lat, double Lng, String time, String type) {

        DatabaseReference newRef = mDatabase.child("spots").push();
        newRef.setValue(new Spot(userId, description, title, category, contentURL, Lat, Lng, time, type));

    }

    // Retrieve data from the db
    public void loadProfile(String userId, final myProfileActivity activity){

        Log.d(TAG,"Get User Info");
        final DatabaseReference newRef = mDatabase.child("users").child(userId);
        newRef.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                User user = dataSnapshot.getValue(User.class);
                Log.d(TAG,"User: "+ user.firstName);
                activity.setUserInfo(user);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.d(TAG, "loadUser:onCancelled", databaseError.toException());
                // ...
            }
        });

    }



}
