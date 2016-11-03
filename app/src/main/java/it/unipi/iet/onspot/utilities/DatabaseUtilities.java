package it.unipi.iet.onspot.utilities;

import android.app.Activity;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
    public void writeNewUser(String userId, String name, String lastName, String birthday,String gender) {

        User user = new User(name, lastName, birthday, gender);
        mDatabase.child("users").child(userId).setValue(user);

    }

    // Insert a new spot in the db
    public void writeNewSpot(String userId, String description, String category, String contentURL,
                             double Lat, double Lng, String time) {

        DatabaseReference newRef = mDatabase.child("spots").push();
        newRef.setValue(new Spot(userId, description, category, contentURL, Lat, Lng, time));

    }

    // Retrieve data from the db
    public void getUserInfo(String userId, final myProfileActivity activity){

        Log.d(TAG,"Get User Info");
        DatabaseReference newRef = mDatabase.child("users").child(userId);
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
