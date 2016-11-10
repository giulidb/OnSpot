package it.unipi.iet.onspot.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import it.unipi.iet.onspot.R;
import it.unipi.iet.onspot.utilities.CircleTransform;
import it.unipi.iet.onspot.MediaStreamer;
import it.unipi.iet.onspot.utilities.Spot;
import it.unipi.iet.onspot.utilities.SpotViewHolder;
import it.unipi.iet.onspot.utilities.User;

/**
 *  Fragment that shows single spots information when users click on markers
 */

public  class VisualizeSpotFragment extends BottomSheetDialogFragment {

    private CoordinatorLayout.Behavior behavior;
    private String spot_key;
    private FirebaseRecyclerAdapter<Spot, SpotViewHolder> mAdapter;

    private TextView title;
    private TextView date;
    private TextView category;
    private TextView description;
    private TextView user_name;
    private TextView heartTextView;
    private ImageView user_photo;
    private ImageView heartImageView;
    private ImageView content;

    private DatabaseReference mDatabase;

    private final String TAG = "VisualizeSpot";
    public static final String CONTENT_URL = "it.unipi.iet.onspot.CONTENT_URL";
    public static final String TYPE = "it.unipi.iet.onspot.TYPE";


    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {

        //create and associate a BottomSheetCallback to dismiss the Fragment when the sheet is hidden
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss();
            }

        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        }
    };

    // Inflate a new layout file and retrieve the BottomSheetBehavior of the container view
    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.fragment_visualizespot, null);
        dialog.setContentView(contentView);

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        behavior = params.getBehavior();

        if( behavior != null && behavior instanceof BottomSheetBehavior ) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_visualizespot, container, false);

        title = (TextView)view.findViewById(R.id.spot);
        date = (TextView)view.findViewById(R.id.date);
        category = (TextView)view.findViewById(R.id.category);
        description = (TextView)view.findViewById(R.id.description);
        user_name = (TextView)view.findViewById(R.id.user_name);
        heartTextView = (TextView) view.findViewById(R.id.heart_number);
        user_photo = (ImageView)view.findViewById(R.id.user_photo);
        content = (ImageView)view.findViewById(R.id.content);
        heartImageView = (ImageView)view.findViewById(R.id.heart_image);
        user_name = (TextView)view.findViewById(R.id.user_name);



        mDatabase = FirebaseDatabase.getInstance().getReference();

        final DatabaseReference newRef = mDatabase.child("spots").child(spot_key);
        newRef.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Determine if the current user has liked this image and set UI accordingly
                Log.d(TAG,"On data change");
                final Spot s = dataSnapshot.getValue(Spot.class);

                if (s.hearts.containsKey(getUid())) {
                    heartImageView.setImageResource(R.drawable.heart_full);
                } else {
                    heartImageView.setImageResource(R.drawable.heart_empty);
                }

                Log.d(TAG,"num hearts"+ s.heartCount);
                heartTextView.setText(" "+s.heartCount);

                heartImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View heartView) {
                        // Do not allow liking own images
                        if (s.userId.compareTo(getUid()) == 0) {
                            return;
                        }

                        // Need to write to the place where the post is stored
                        DatabaseReference uploadsRef = mDatabase.child("spots").child(spot_key);
                        onHeartClicked(uploadsRef);
                    }


                });

                load_info(s);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.d(TAG, "loadUser:onCancelled", databaseError.toException());
                // ...
            }
        });


        return view;
    }



    public void setSpot(String spot_key) {

        this.spot_key = spot_key;
    }


    // Function for handling favourites
    private void onHeartClicked(DatabaseReference uploadRef) {
        uploadRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Spot p = mutableData.getValue(Spot.class);
                if (p == null) {
                    return Transaction.success(mutableData);
                }

                if (p.hearts.containsKey(getUid())) {
                    // Unlove the upload and remove self from hearts
                    p.heartCount = p.heartCount - 1;
                    p.hearts.remove(getUid());

                } else {
                    // Love the upload and add self to hearts
                    p.heartCount = p.heartCount + 1;
                    p.hearts.put(getUid(), true);

                }

                // Set value and report transaction success
                mutableData.setValue(p);
                Log.d(TAG,"Num hearts: "+p.heartCount);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                // Transaction completed
                Log.d(TAG, "uploadTransaction:onComplete:" + databaseError);
            }
        });
    }


    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }


    public void load_info(final Spot spot){


        title.setText(spot.title);
        date.setText(" "+spot.time);
        category.setText(" "+spot.category);
        description.setText(" "+spot.description);

        switch(spot.Type){
            case "image":
                Picasso.with(getActivity()).load(spot.contentURL).resize(180, 180).centerCrop().into(content);
                break;

            case "audio":
                content.setImageResource(R.drawable.volume);
                content.setPadding(60,60,60,60);
                content.setBackgroundColor(Color.parseColor("#C45852"));
                break;

            case "video":
                content.setImageResource(R.drawable.video);
                content.setPadding(60,60,60,60);
                content.setBackgroundColor(Color.parseColor("#C45852"));


        }

        content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start MediaStreamer to Reproduce Media
                Intent i = new Intent(getActivity(), MediaStreamer.class);
                i.putExtra(CONTENT_URL, spot.contentURL);
                Log.d(TAG,spot.Type);
                i.putExtra(TYPE,spot.Type);
                startActivity(i);
            }
        });


        final DatabaseReference Ref = mDatabase.child("users").child(spot.userId);
        Ref.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                User user = dataSnapshot.getValue(User.class);
                Log.d(TAG,"User: "+ user.firstName);
                user_name.setText(user.firstName);
                Picasso.with(getActivity()).load(user.photoURL).transform(new CircleTransform()).into(user_photo);


            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.d(TAG, "loadUser:onCancelled", databaseError.toException());
                // ...
            }
        });
    }

}


