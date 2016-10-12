package it.unipi.iet.onspot;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    // auth and auth state listener declaration
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String TAG = "LoginActivity";
    private  EditText emailField;
    private EditText passwordField;
    private Button Login;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Login = (Button)findViewById(R.id.email_sign_in_button);

        // ProgressDialog Initialization
        progressDialog = new ProgressDialog(LoginActivity.this,ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setIndeterminate(true);


        // auth initialization
        mAuth = FirebaseAuth.getInstance();

        // auth state listener initialization
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

    }

    // Create a new account by passing the new user's email address and password
    public void create_new_account(String email, String password){

        Log.d(TAG, "createAccount:" + email);

        //check format email and password
        if(!validate_form(email,password))
            return;

        Login.setEnabled(false);

        // Show screen spinner while authenticating
        progressDialog.setMessage("Creating new user...");
        progressDialog.show();

      mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, R.string.create_user_failed,
                                    Toast.LENGTH_SHORT).show();
                                    Login.setEnabled(true);

                        }
                        else{
                            Log.d(TAG,"New User Created");
                            // After user creation redirect user to ProfileActivity to insert basic info
                            Intent i = new Intent(LoginActivity.this,ProfileActivity.class);
                            startActivity(i);
                       }

                    }
                });

    }

    // Signing in a user with a password
    public void sign_in(String email,String password){

        //check format email and password
        if(!validate_form(email,password))
            return;

        Login.setEnabled(false);

        // Show screen spinner while authenticating
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithEmail:failed", task.getException());
                            Toast.makeText(LoginActivity.this, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                            Login.setEnabled(true);
                        }else{
                            Log.d(TAG,"sign-in successful");
                            Intent i = new Intent(LoginActivity.this,MapsActivity.class);
                            startActivity(i);
                        }
                    }
                });
        // [END sign_in_with_email]
    }

    public void onClick(View v){

       emailField = (EditText)findViewById(R.id.email);
       passwordField = (EditText)findViewById(R.id.password);

        Intent intent = getIntent();
        String butt = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        // if sign_in or sign up button was clicked
        if(butt.equals("button")){
            sign_in(emailField.getText().toString(),passwordField.getText().toString());
        } else if(butt.equals("button2")){
            create_new_account(emailField.getText().toString(),passwordField.getText().toString());
        }
    }

    // Check if form's fields are compiled properly
    public boolean validate_form(String email, String password){
        boolean valid = true;
        if(TextUtils.isEmpty(email)|| !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailField.setError("Enter a valid email address");
            valid = false;
        }else{
            emailField.setError(null);
        }

        if(TextUtils.isEmpty(password) || password.length() < 6 || password.length() > 10){
            passwordField.setError("between 6 and 10 alphanumeric characters");
            valid = false;
        }else{
            passwordField.setError(null);
        }

        return valid;
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

}

