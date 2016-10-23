package it.unipi.iet.onspot;
import android.app.DatePickerDialog;
import java.io.IOException;
import java.util.Calendar;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;

import it.unipi.iet.onspot.utilities.AuthUtilities;
import it.unipi.iet.onspot.utilities.MultimediaUtilities;
import it.unipi.iet.onspot.utilities.PermissionUtilities;
import it.unipi.iet.onspot.utilities.RoundedImageView;


public class ProfileActivity extends AppCompatActivity implements View.OnClickListener{

    // variable declaration
    private EditText birthday;
    private EditText gender;
    EditText firstName;
    EditText lastName;
    AlertDialog.Builder builder;
    Uri photo_uri = null;
    AlertDialog dialog;
    private String TAG = "ProfileActivity";
    String [] GENDERS = {"Male","Female"};
    AuthUtilities AuthUt;
    int PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // variables initialization
        firstName = (EditText)findViewById(R.id.firstName);
        lastName = (EditText)findViewById(R.id.lastName);
        birthday = (EditText)findViewById(R.id.birthday);
        gender = (EditText)findViewById(R.id.gender);
        Button butt = (Button)findViewById(R.id.join);
        ImageView edit_photo = (ImageView)findViewById(R.id.edit_photo);

        // Set views clickable
        birthday.setOnClickListener(this);
        gender.setOnClickListener(this);
        butt.setOnClickListener(this);
        edit_photo.setOnClickListener(this);

        //Authentication
        AuthUt = new AuthUtilities();

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

    /*
    * Function to change profile photo loading it from camera o gallery
    */

    public void change_photo(){

        // Permission
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d(TAG, "Permission check at runtime");
            if(PermissionUtilities.checkPermission(this, android.Manifest.permission.CAMERA,
                    PERMISSION_REQUEST_CODE)&&
                    (PermissionUtilities.checkPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE,
                            PERMISSION_REQUEST_CODE))){
                MultimediaUtilities.create_intent("image", this);
            }
        }
        else
            MultimediaUtilities.create_intent("image", this);


    }

    // Click on join button
    public void join_user(){

        String Name = firstName.getText().toString();
        String Surname = lastName.getText().toString();

        //check format email and password
        if(!validate_form(Name,Surname))
            return;

        // check if photo was modified or not
        Uri avatar = Uri.parse("android.resource://my.package.name/" + R.drawable.avatar_guest);
        Uri uri = (photo_uri != null)? photo_uri : avatar;

        // update profile info
        AuthUt.updateUserProfile(Name + " " + Surname,uri);
        Log.d(TAG,"Profile updated: "+ AuthUt.getUser().getDisplayName());

        //TODO: save birthday and gender in the db

        // Redirect user to MapsFragment after saved info
        Intent i = new Intent(ProfileActivity.this,MapsActivity.
                class);
        startActivity(i);

    }

    // function callback automatically after choosing picture from gallery or camera
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // retrieve view contained photo profile
        ImageView photo = (ImageView)findViewById(R.id.imageProfile);
        int max_size = photo.getHeight();
        int width = photo.getWidth();
        Log.d(TAG,"Logo size: height="+max_size+" width: "+width);

        Bitmap bm = null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Get uri of the bitmap
           photo_uri = data.getData();
            // Get the actual path
            String path = MultimediaUtilities.getRealPathFromURI(this,photo_uri);
            Log.d(TAG,"Path of bitmap= "+ path);
            // Get right orientation of the image
            bm = MultimediaUtilities.rotateBitmap(bm,path);

            // Rounded cropped image
            bm = RoundedImageView.getRoundedCroppedBitmap(bm,max_size);
            // Set image as profile photo
            photo.setImageBitmap(bm);
        }



    }

    // Check if form's fields are compiled properly
    public boolean validate_form(String Name, String Surname){
        boolean valid = true;
        if(TextUtils.isEmpty(Name)){
            firstName.setError("Enter a name");
            valid = false;
        }else{
            firstName.setError(null);
        }
        if(TextUtils.isEmpty(Surname)){
            lastName.setError("Enter a last-name");
            valid = false;
        }else{
            lastName.setError(null);
        }

        return valid;
    }

    @Override
    public void onClick(View view) {
        // Discriminate view clicked
        switch(view.getId()){
            case R.id.birthday:
                create_date_picker();
                break;
            case R.id.gender:
                create_dialog_box();
                break;
            case R.id.join:
                join_user();
                break;
            case R.id.edit_photo:
                change_photo();}
    }

@Override
    public void onBackPressed() {
        // disable going back to the SignUpActivity
        moveTaskToBack(true);
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
   * Functions for permission handling.
   */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        MultimediaUtilities.create_intent("image",this);

    }


}
