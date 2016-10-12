package it.unipi.iet.onspot;

import android.app.DatePickerDialog;
import java.util.Calendar;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;


public class ProfileActivity extends AppCompatActivity implements View.OnClickListener{

    // variable declaration
    private EditText birthday;
    private EditText gender;
    private Button butt;
    private ImageView edit_photo;
    AlertDialog.Builder builder;
    AlertDialog dialog;
    private String TAG = "ProfileActivity";
    String [] GENDERS = {"Male","Female"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        // variables initialization
        birthday = (EditText)findViewById(R.id.editText3);
        gender = (EditText)findViewById(R.id.editText4);
        butt = (Button)findViewById(R.id.button5);
        edit_photo = (ImageView)findViewById(R.id.edit_photo);

        // Set views clickable
        birthday.setOnClickListener(this);
        gender.setOnClickListener(this);
        butt.setOnClickListener(this);
        edit_photo.setOnClickListener(this);

    }

    // Create DataPickerDialog to display when birthday view is clicked
    public void create_date_picker(){

        Calendar newCalendar = Calendar.getInstance();
        DatePickerDialog DatePickerDialog = new DatePickerDialog(ProfileActivity.this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                String day = newDate.get(Calendar.DATE) + "/"
                        + (newDate.get(Calendar.MONTH) + 1) + "/"
                        + newDate.get(Calendar.YEAR);
                Log.d(TAG,"Day "+ day + " selected");
                birthday.setText(day);
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
                        Log.d(TAG,"Genders "+ GENDERS[which] + " selected");
                        gender.setText(GENDERS[which]);
                        dialog.dismiss();
                    }
                });
        dialog = builder.create();
        dialog.show();

    }

    public void change_photo(){

        builder = new AlertDialog.Builder(ProfileActivity.this);
        builder.setTitle("Load a picture from...");


        dialog = builder.create();
        dialog.show();

    }

    // Click on join button
    public void join_user(){

        //TODO: check if and what fields are required and how to save others info in FRDB
        AuthUtilities ut = new AuthUtilities(FirebaseAuth.getInstance().getCurrentUser());
        EditText firstName = (EditText)findViewById(R.id.editText);
        EditText lastName = (EditText)findViewById(R.id.editText2);
        ut.updateUserProfile(firstName.getText().toString() + " " + lastName.getText().toString(),null);
        Log.d(TAG,"Profile updated: "+ ut.getUser().getDisplayName());

        // Redirect user to MapsActivity after saved info
        Intent i = new Intent(ProfileActivity.this,MapsActivity.class);
        startActivity(i);

    }


    @Override
    public void onClick(View view) {
        // Discriminate if birthday or Gender EditText or Join button is clicked
        if(view == birthday){
            create_date_picker();
        }else if(view == gender){
            create_dialog_box();
        }else if (view == butt){
            join_user();
        }else if(view == edit_photo)
            change_photo();
    }

@Override
    public void onBackPressed() {
        // disable going back to the SignUpActivity
        moveTaskToBack(true);
    }


}
