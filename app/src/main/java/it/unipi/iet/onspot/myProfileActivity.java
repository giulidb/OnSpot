package it.unipi.iet.onspot;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;

import it.unipi.iet.onspot.utilities.AuthUtilities;
import it.unipi.iet.onspot.utilities.DatabaseUtilities;
import it.unipi.iet.onspot.utilities.MultimediaUtilities;
import it.unipi.iet.onspot.utilities.User;

public class myProfileActivity extends AppCompatActivity {

    private AuthUtilities AuthUt;
    private String TAG = "myProfileActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);
        ImageView profile_photo = (ImageView) findViewById(R.id.photoProfile);

        //Authentication
        AuthUt = new AuthUtilities();

        // Retrieve Profile Photo by FIREBASE_USER
        //// TODO: 03/11/2016 Se vogliamo che anche altri utenti vedano la foto usare Storage per salvare e recuperare
        Bitmap bm = BitmapFactory.decodeFile(MultimediaUtilities.getRealPathFromURI(this,AuthUt.getPhoto_url()));
        bm = MultimediaUtilities.rotateBitmap(bm,MultimediaUtilities.getRealPathFromURI(this,AuthUt.getPhoto_url()));
        bm = MultimediaUtilities.getRoundedCroppedBitmap(bm, 200);
        profile_photo.setImageBitmap(bm);

        // Retrieve User info from FIREBASE DATABASE
        DatabaseUtilities db = new DatabaseUtilities();
        db.getUserInfo(AuthUt.getUser().getUid(),this);
    }

    public void setUserInfo(User user){

        TextView Name = (TextView)findViewById(R.id.completeName);
        TextView Age = (TextView)findViewById(R.id.age);
        TextView Gender = (TextView)findViewById(R.id.sex);

        Name.setText(user.firstName + " " + user.lastName);
        Gender.setText(user.gender);

        Calendar today = Calendar.getInstance();
        int year = Integer.parseInt(user.birthday.substring(user.birthday.length()-4));
        int age = today.get(Calendar.YEAR) - year ;
        Age.setText("Age: "+age);



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