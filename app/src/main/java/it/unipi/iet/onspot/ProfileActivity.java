package it.unipi.iet.onspot;

import android.app.DatePickerDialog;
import java.util.Calendar;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;


public class ProfileActivity extends AppCompatActivity implements View.OnClickListener{

    // variable declaration
    private EditText birthday;
    private EditText gender;
    private Button butt;
    private DatePickerDialog DatePickerDialog;
    AlertDialog.Builder builder;
    String [] GENDERS = {"Male","Female"};
    AlertDialog dialog;
    private String TAG = "ProfileActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        // variables initialization
        birthday = (EditText)findViewById(R.id.editText3);
        gender = (EditText)findViewById(R.id.editText4);
        butt = (Button)findViewById(R.id.button5);

        // Set views clickable
        birthday.setOnClickListener(this);
        gender.setOnClickListener(this);
        butt.setOnClickListener(this);

    }

    // Create DataPickerDialog to display when birthday view is clicked
    public void create_date_picker(){
        Calendar newCalendar = Calendar.getInstance();
        DatePickerDialog = new DatePickerDialog(ProfileActivity.this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                birthday.setText(newDate.get(Calendar.DATE) + "/"
                        + (newDate.get(Calendar.MONTH) + 1) + "/"
                        + newDate.get(Calendar.YEAR));
            }

        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
        DatePickerDialog.show();
    }

    // Create Dialog Box to choose gender info
    public void create_dialog_box(){
        builder = new AlertDialog.Builder(ProfileActivity.this);
        builder.setTitle("Select gender")
                .setItems(R.array.genders, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        gender.setText(GENDERS[which]);
                        dialog.dismiss();
                    }
                });
        dialog = builder.create();
        dialog.show();

    }

    //TODO: non ho ancora capito se funziona XD
    public void join_user(){
        AuthUtilities ut = new AuthUtilities(FirebaseAuth.getInstance().getCurrentUser());
        EditText firstName = (EditText)findViewById(R.id.editText);
        EditText lastName = (EditText)findViewById(R.id.editText2);
        ut.updateUserProfile(firstName.getText().toString() + lastName.getText().toString(),null);
        Log.v(TAG,"Profile updated: "+ ut.getUser().getDisplayName());

        Intent i = new Intent(ProfileActivity.this,MapsActivity.class);
        startActivity(i);

    }

    @Override
    public void onClick(View view) {
        // Discriminate if birthday or Gender EditText is clicked
        if(view == birthday){
            create_date_picker();
        }else if(view == gender){
            create_dialog_box();
        }else if (view == butt){
            join_user();
        }
    }

@Override
    public void onBackPressed() {
        // disable going back to the SignUpActivity
        moveTaskToBack(true);
    }


}
