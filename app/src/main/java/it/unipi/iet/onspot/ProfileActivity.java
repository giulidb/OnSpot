package it.unipi.iet.onspot;
import android.app.DatePickerDialog;

import java.io.File;
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
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import it.unipi.iet.onspot.utilities.AuthUtilities;
import it.unipi.iet.onspot.utilities.DatabaseUtilities;
import it.unipi.iet.onspot.utilities.MultimediaUtilities;
import it.unipi.iet.onspot.utilities.PermissionUtilities;


public class ProfileActivity extends BaseActivity implements View.OnClickListener{

    // variable declaration
    private EditText birthday;
    private EditText gender;
    private EditText firstName;
    private EditText lastName;
    private AlertDialog.Builder builder;
    private Uri photo_uri = null;
    private AlertDialog dialog;
    private String TAG = "ProfileActivity";
    private AuthUtilities AuthUt;
    private int PERMISSION_REQUEST_CODE = 1;

    private String path;
    private String Name;
    private String Surname;
    private String day;
    private String gen;

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
        builder = new AlertDialog.Builder(ProfileActivity.this,R.style.AppDialog);
        builder.setTitle("Select gender")
                .setItems(R.array.genders, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        String[] genders = getResources().getStringArray(R.array.genders);
                        Log.d(TAG,"Genders "+ genders[which] + " selected");
                        gender.setText(genders[which]);
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

        Name = firstName.getText().toString();
        Surname = lastName.getText().toString();
        day = birthday.getText().toString();
        gen = gender.getText().toString();

        //check format email and password
        if(!validate_form(Name,Surname))
            return;

        // check if photo was modified or not
        Uri avatar = Uri.parse("android.resource://my.package.name/" + R.drawable.avataryellow);
        Uri uri = (photo_uri != null)? photo_uri : avatar;

        // update profile info
        AuthUt.updateUserProfile(Name + " " + Surname,uri);
        Log.d(TAG,"Profile updated: "+ AuthUt.getDisplayName());

        showProgressDialog();

        //Retrieve userId
        String userId = AuthUt.getUser().getUid();

        if(path != null ) {
            //Start the task to save spot multimedia content in the storage
            StorageReference storageRef = FirebaseStorage.getInstance()
                    .getReferenceFromUrl("gs://onspot-8c6f4.appspot.com/");
            Uri file = Uri.fromFile(new File(path));

            StorageReference childRef = storageRef.child(userId + "/" + file.getLastPathSegment());
            Log.d(TAG, "File Storage Reference: " + childRef.getPath());
            UploadTask uploadTask = childRef.putFile(file);
            uploadTask.addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // Upload succeeded
                    Log.d(TAG, "Entered in onSuccess");
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    String contentURL = null;
                    if (downloadUrl != null) {
                        contentURL = downloadUrl.toString();
                    }
                    saveUserOnDatabase(contentURL);
                    hideProgressDialog();

                    Toast.makeText(ProfileActivity.this, "Used created correctly",
                            Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Upload failed
                    Log.w(TAG, "Entered in onFailure", exception);
                    hideProgressDialog();
                    Toast.makeText(ProfileActivity.this, "Error: upload failed",
                            Toast.LENGTH_SHORT).show();
                }
            });

            // Observe state change events such as progress, pause, and resume
            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    Log.d(TAG, "Upload is " + progress + "% done");
                    setmProgressDialog((int) progress);
                }
            }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.d(TAG, "Upload is paused");
                }
            });
        }
    }

    // Save Data on DB

    public void saveUserOnDatabase(String photoURL){

        // save user extra personal info in the db
        DatabaseUtilities db = new DatabaseUtilities();
        db.writeNewUser(AuthUt.getUser().getUid(),Name,Surname,photoURL,day,gen);

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
        ImageView photo = (ImageView) findViewById(R.id.imageProfile);
        int max_size = photo.getHeight();
        int width = photo.getWidth();
        Log.d(TAG, "Logo size: height=" + max_size + " width: " + width);

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
            path = MultimediaUtilities.getRealPathFromURI(this, photo_uri);
            Log.d(TAG, "Path of bitmap= " + path);
            // Get right orientation of the image
            bm = MultimediaUtilities.rotateBitmap(bm, path);
            // Rounded cropped image
            bm = MultimediaUtilities.getRoundedCroppedBitmap(bm, max_size);
            // Set image as profile photo
            photo.setImageBitmap(bm);
            photo.setOnClickListener(this);
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
                change_photo();
                break;
            case R.id.imageProfile:
                if (path != null){
                    MultimediaUtilities.open_media(view.getId(), path, this);
                }
        }
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
