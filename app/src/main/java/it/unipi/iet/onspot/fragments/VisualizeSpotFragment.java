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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import it.unipi.iet.onspot.R;
import it.unipi.iet.onspot.utilities.CircleTransform;
import it.unipi.iet.onspot.MediaStreamer;
import it.unipi.iet.onspot.utilities.Spot;
import it.unipi.iet.onspot.utilities.User;

/**
 *  Fragment that shows single spots information when users click on markers
 */

public class VisualizeSpotFragment extends BottomSheetDialogFragment {

    private CoordinatorLayout.Behavior behavior;
    private Spot spot;

    private TextView title;
    private TextView date;
    private TextView category;
    private TextView description;
    private ImageView user_photo;
    private ImageView content;
    private TextView user_name;

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
        user_photo = (ImageView)view.findViewById(R.id.user_photo);
        content = (ImageView)view.findViewById(R.id.content);


        title.setText(spot.title);
        date.setText(" "+spot.time);
        category.setText(" "+spot.category);
        description.setText(" "+spot.description);

        switch(spot.Type){
            case "image":
            Picasso.with(getActivity()).load(spot.contentURL).resize(180, 180).centerCrop().into(content);
            break;

            //TODO: fare in modo che sia decente
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
                    //TODO: vedere se può andare anche per le immagini
                    Intent i = new Intent(getActivity(), MediaStreamer.class);
                    i.putExtra(CONTENT_URL, spot.contentURL);
                    Log.d(TAG,spot.Type);
                    i.putExtra(TYPE,spot.Type);
                    startActivity(i);
                }
            });


        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        final DatabaseReference newRef = mDatabase.child("users").child(spot.userId);
        newRef.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                User user = dataSnapshot.getValue(User.class);
                Log.d(TAG,"User: "+ user.firstName);
                user_name = (TextView)view.findViewById(R.id.user_name);
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


        return view;
    }

    public void setSpot(Spot spot) {
       this.spot = spot;
        Log.d(TAG,"Spot "+spot.time);
    }


}


