package it.unipi.iet.onspot;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.Calendar;

import it.unipi.iet.onspot.utilities.AuthUtilities;
import it.unipi.iet.onspot.utilities.CircleTransform;
import it.unipi.iet.onspot.utilities.DatabaseUtilities;
import it.unipi.iet.onspot.utilities.MultimediaUtilities;
import it.unipi.iet.onspot.utilities.User;

import it.unipi.iet.onspot.fragments.LikedImagesFragment;
import it.unipi.iet.onspot.fragments.MyImagesFragment;



public class myProfileActivity extends AppCompatActivity {

    private AuthUtilities AuthUt;

    private PagerAdapter mPagerAdapter;
    private ViewPager mViewPager;

    private String TAG = "myProfileActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        //Authentication
        AuthUt = new AuthUtilities();

        // Retrieve User info from FIREBASE DATABASE
        DatabaseUtilities db = new DatabaseUtilities();
        db.loadProfile(AuthUt.getUser().getUid(),this);
        Log.d(TAG, "Profile loaded");

        // Create the adapter that will return a fragment for each section
        mPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            private final Fragment[] mFragments = new Fragment[] {
                    new LikedImagesFragment(),
                    new MyImagesFragment()
            };
            private final String[] mFragmentNames = new String[] {
                    getString(R.string.heading_liked),
                    getString(R.string.heading_my_images)
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
        Log.d(TAG, "ViewPager created");

    }

    public void setUserInfo(User user){

        TextView Name = (TextView)findViewById(R.id.completeName);
        TextView Age = (TextView)findViewById(R.id.age);
        TextView Gender = (TextView)findViewById(R.id.sex);
        ImageView profile_photo = (ImageView) findViewById(R.id.photoProfile);


        Name.setText(user.firstName + " " + user.lastName);
        Gender.setText(user.gender);

        Calendar today = Calendar.getInstance();
        int year = Integer.parseInt(user.birthday.substring(user.birthday.length()-4));
        int age = today.get(Calendar.YEAR) - year ;
        Age.setText("Age: "+age);

        Picasso.with(this).load(user.photoURL).transform(new CircleTransform()).into(profile_photo);


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
