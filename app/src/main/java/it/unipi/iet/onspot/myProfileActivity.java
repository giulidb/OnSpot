package it.unipi.iet.onspot;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;
import java.util.Calendar;
import it.unipi.iet.onspot.utilities.AuthUtilities;
import it.unipi.iet.onspot.utilities.CircleTransform;
import it.unipi.iet.onspot.utilities.DatabaseUtilities;
import it.unipi.iet.onspot.utilities.User;
import it.unipi.iet.onspot.fragments.LikedImagesFragment;
import it.unipi.iet.onspot.fragments.MyImagesFragment;



public class myProfileActivity extends AppCompatActivity {

    private AuthUtilities AuthUt;

    private final String TAG = "myProfileActivity";
    public static final String SIGN = "it.unipi.iet.onspot.SIGN";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Make the logo appear on the action bar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.logo1_small);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        setContentView(R.layout.activity_my_profile);

        Intent i = getIntent();
        Bundle b = i.getExtras();
        PagerAdapter mPagerAdapter;
        ViewPager mViewPager;

        //Authentication
        AuthUt = new AuthUtilities();

        // Retrieve User info from FIREBASE DATABASE
        DatabaseUtilities db = new DatabaseUtilities();
        db.loadProfile(AuthUt.getUser().getUid(),this);
        Log.d(TAG, "Profile loaded");

        // Create the adapter that will return a fragment for each section
        mPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            private final Fragment[] mFragments = new Fragment[] {
                    new MyImagesFragment(),
                    new LikedImagesFragment()
            };
            private final String[] mFragmentNames = new String[] {
                    getString(R.string.heading_my_images),
                    getString(R.string.heading_liked)
            };
            @Override
            public Fragment getItem(int position) { return mFragments[position]; }
            @Override
            public int getCount() { return mFragments.length; }
            @Override
            public CharSequence getPageTitle(int position) { return mFragmentNames[position]; }
        };
        Log.d(TAG, "PagerAdapter created");

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mPagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        // If the user clicked on the Favourites button, switch tabs
        if(b!=null && b.get("extra").toString().equals("heart")) {
            TabLayout.Tab tab = tabLayout.getTabAt(1);
            tab.select();
        }

        Log.d(TAG, "ViewPager created");
    }

    public void setUserInfo(User user){

        TextView Name = (TextView)findViewById(R.id.completeName);
        TextView Age = (TextView)findViewById(R.id.age);
        TextView Gender = (TextView)findViewById(R.id.sex);
        ImageView profile_photo = (ImageView) findViewById(R.id.photoProfile);

        // Check fields is necessary because someone could be null if user is logged with fb
        if(user.firstName!=null && user.lastName !=null){
            Name.setText(user.firstName + " " + user.lastName);}
        else
            Name.setText(user.firstName);

        if(user.gender != null)
            Gender.setText(user.gender);

        if(user.birthday != null){
            Calendar today = Calendar.getInstance();
            int year = Integer.parseInt(user.birthday.substring(user.birthday.length()-4));
            int age = today.get(Calendar.YEAR) - year ;
            Age.setText("Age: "+age);}

        // If user does not have an image it loads a default one
        if(user.photoURL != null )
            Picasso.with(this).load(user.photoURL).transform(new CircleTransform()).into(profile_photo);
        else
            Picasso.with(this).load("https://ssl.gstatic.com/images/branding/product/1x/avatar_circle_blue_512dp.png").transform(new CircleTransform()).into(profile_photo);



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
     * Menu functions
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent i;
        final String ACTIVITY ="it.unipi.iet.onspot.ACTIVITY";

        switch (id) {
            case R.id.logout:
                //logout
                AuthUt.signOut();
                LoginManager.getInstance().logOut();
                i = new Intent(myProfileActivity.this,MainActivity.class);
                startActivity(i);
                break;

            case R.id.modify:
                // modify profile
                if(!isAnonymous(AuthUt.get_mAuth().getCurrentUser())){
                    i= new Intent(myProfileActivity.this, ProfileActivity.class);
                    i.putExtra(ACTIVITY,"MapsActivity");
                    startActivity(i);}
                else{
                    alert_user();
                }
                break;

            case R.id.info:
                i= new Intent(myProfileActivity.this, InfoActivity.class);
                startActivity(i);
                break;


        }
        return false;
    }

     /*
     * Functions to handle anonymous users
     */

    // Function to check if user is anonymous
    private boolean isAnonymous(FirebaseUser user){

        return (user.getEmail() == null);

    }

    // Show alert dialog if anonymous user want to access to registered-user-only's areas
    public void alert_user(){

        AlertDialog alertDialog = new AlertDialog.Builder(this).create();

        alertDialog.setTitle("Anonymous User");
        alertDialog.setMessage("This function is enable only for non-anonymous-user. Would you join us?");
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE,"Yes ",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                // if user click on Yes, he is redirected to the sign-up Activity
                Log.d(TAG,"User clicked Yes");
                Intent i = new Intent(myProfileActivity.this,LoginActivity.class);
                i.putExtra(SIGN,"sign-up");
                //logout from anonymous user
                AuthUt.signOut();
                startActivity(i);

            }
        });
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE,"No ",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                Log.d(TAG,"User clicked No");

            }
        });
        alertDialog.show();

    }


}
