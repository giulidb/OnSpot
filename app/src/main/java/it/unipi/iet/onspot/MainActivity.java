package it.unipi.iet.onspot;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity  {

    static final String EXTRA_MESSAGE = "it.unipi.iet.onspot.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
}
