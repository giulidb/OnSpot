package it.unipi.iet.onspot;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/*
*  Launch loading activity
*/
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Main Activity start
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
