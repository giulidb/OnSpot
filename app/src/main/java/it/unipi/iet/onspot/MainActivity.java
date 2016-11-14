package it.unipi.iet.onspot;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import it.unipi.iet.onspot.utilities.AuthUtilities;
import it.unipi.iet.onspot.utilities.DatabaseUtilities;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseUser;



public class MainActivity extends Activity{

    public static final String SIGN = "it.unipi.iet.onspot.SIGN";
    private final String TAG = "MainActivity";
    private AuthUtilities AuthUt;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_main);


        // Check Authentication
        AuthUt = new AuthUtilities();
        if(AuthUt.getUser() != null ){
            // if user is already logged changes activity
            Log.d(TAG,"User already logged");
            Intent i = new Intent(MainActivity.this,MapsActivity.class);
            startActivity(i);}
        else{
            Log.d(TAG,"User not logged yet");
            }

        // Sign in Button
        final Button SignIn = (Button) findViewById(R.id.button);
        SignIn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                intent.putExtra(SIGN,"sign-in");
                startActivity(intent);
            }
        });

        // Sign up with email Button
        final Button SignUp = (Button) findViewById(R.id.button2);
        SignUp.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                intent.putExtra(SIGN,"sign-up");
                startActivity(intent);
            }
        });

        // Connect with Facebook button
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = (LoginButton) findViewById(R.id.button3);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());

            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                // ...
            }
        });

        // Browse as a guest button
        final Button GuestUser = (Button) findViewById(R.id.button4);
        GuestUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // signin_anonymously
                AuthUt.get_mAuth().signInAnonymously()
                        .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Log.d(TAG, "signInAnonymously:onComplete:" + task.isSuccessful());
                                Log.d(TAG,"User Provider: "+ AuthUt.get_mAuth().getCurrentUser().getProviderId());
                                // start MapsActivity
                                Intent i = new Intent(MainActivity.this,MapsActivity.class);
                                startActivity(i);

                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    Log.w(TAG, "signInAnonymously", task.getException());
                                    Toast.makeText(MainActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }


                            }
                        });
            }
        });



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

    /*
     * Disable going back function
     */
    @Override
    public void onBackPressed() {
        // disable going back to the previous Activity
        moveTaskToBack(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());

        AuthUt.get_mAuth().signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());
                        FirebaseUser user = AuthUt.get_mAuth().getCurrentUser();
                        Log.d(TAG,"url "+user.getPhotoUrl());
                        DatabaseUtilities db = new DatabaseUtilities();
                        db.writeNewUser(user.getUid(),user.getDisplayName(),null,user.getPhotoUrl().toString(),null, null);
                        Log.d(TAG,"Fb user created");
                        // start MapsActivity
                        Intent i = new Intent(MainActivity.this,MapsActivity.class);
                        startActivity(i);
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }




}
