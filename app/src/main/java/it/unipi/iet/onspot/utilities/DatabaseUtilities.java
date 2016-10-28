package it.unipi.iet.onspot.utilities;

import android.net.Uri;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Firebase Database utilities
 */

public class DatabaseUtilities {

    private DatabaseReference mDatabase;

    public DatabaseUtilities(){

        // initialize reference
        mDatabase = FirebaseDatabase.getInstance().getReference();

    }

    // Insert a new user in the db
    public void writeNewUser(String userId, String name, String lastName, String birthday,String gender) {

        User user = new User(name, lastName, birthday, gender);
        mDatabase.child("users").child(userId).setValue(user);


    }




}
