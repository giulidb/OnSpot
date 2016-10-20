package it.unipi.iet.onspot;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

import it.unipi.iet.onspot.utilities.AuthUtilities;

public class MainActivity extends AppCompatActivity  {

    static final String EXTRA_MESSAGE = "it.unipi.iet.onspot.MESSAGE";
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    private AuthUtilities AuthUt;
    private String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                intent.putExtra(EXTRA_MESSAGE,"button");
                startActivity(intent);
            }
        });

        // Sign up with email Button
        final Button SignUp = (Button) findViewById(R.id.button2);
        SignUp.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                intent.putExtra(EXTRA_MESSAGE,"button2");
                startActivity(intent);
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
}
