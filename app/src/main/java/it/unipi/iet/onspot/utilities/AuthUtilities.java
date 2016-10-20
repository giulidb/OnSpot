//TODO: vedere se è effettivamente utile questa classe o no

package it.unipi.iet.onspot.utilities;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

/**
 *  Class implemented client side function to support authentication
 */


public class AuthUtilities {

    private String TAG = "AuthUtilities";
    private FirebaseUser user;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;



    // Constructor
    public AuthUtilities(){

        // Auth initialization
        mAuth = FirebaseAuth.getInstance();

        // Auth state listener initialization
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
                }

            }
        };

        // User initialization
        user = mAuth.getCurrentUser();

    }


    // Get User's id
    public FirebaseUser getUser(){
        return mAuth.getCurrentUser();
    }


    // Return Firebase Auth
    public FirebaseAuth get_mAuth(){return mAuth;}

    // Logout
    public void signOut(){
        Log.d(TAG,"signOut: "+mAuth);
        mAuth.signOut();}



    // Set User's Mail
    public void setUserMail(String email){

        user.updateEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User email address updated.");
                        }
                    }
                });
    }

    // Set User's Password
    public void setUserPassword(String newPassword){

        user.updatePassword(newPassword)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User password updated.");
                        }
                    }
                });
    }

    // Update User's profile
    public void updateUserProfile(String Name, Uri PhotoUri){

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(Name)
                .setPhotoUri(PhotoUri)
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User profile updated.");
                        }
                    }
                });
    }

    // Add Auth State listener

    public void addListner(){
        mAuth.addAuthStateListener(mAuthListener);
    }

    // Remove Listener
    public void removeListener(){
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }

    }
}
