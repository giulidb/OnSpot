package it.unipi.iet.onspot.fragments;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLngBounds;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import it.unipi.iet.onspot.R;
import it.unipi.iet.onspot.utilities.Spot;

/**
 *  Fragment that shows list of visible spots when users click on the list button in the toolbar
 */

public class ListSpotFragment extends BottomSheetDialogFragment implements View.OnClickListener {

        private CoordinatorLayout.Behavior behavior;

        /* Coordinates Visible Regions */
        double lowLat;
        double lowLng;
        double highLat;
        double highLng;

        private String TAG = "ListSpotFragment";


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
            View contentView = View.inflate(getContext(), R.layout.fragment_listspot, null);
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
            final View view = inflater.inflate(R.layout.fragment_listspot, container, false);

            final LinearLayout layout = (LinearLayout) view.findViewById(R.id.spot_list_container);

            /* Loading of the list of Visible Spots */
            final DatabaseReference spotRef = FirebaseDatabase.getInstance().getReference().child("spots");
            spotRef.orderByChild("Lat").startAt(lowLat).endAt(highLat).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        Spot spot = child.getValue(Spot.class);
                        if(spot.Lng > lowLng && spot.Lng < highLng){
                            Log.d(TAG, spot.description);
                            Log.d(TAG,"Spot lat: "+spot.Lat+ ", LowLat: "+lowLat+", HighLat: "+highLat);
                            Log.d(TAG,"Spot lng: "+spot.Lng+ ", LowLng: "+lowLng+", HighLng: "+highLng);

                            LinearLayout innerLayout = new LinearLayout(getActivity());
                            innerLayout.setOrientation(LinearLayout.HORIZONTAL);

                            ImageView image = new ImageView(getActivity());
                            image.setOnClickListener(ListSpotFragment.this);
                            image.setTag(spot);

                            switch (spot.Type){

                                case "image":
                                      Picasso.with(getActivity()).load(spot.contentURL).resize(150, 150).centerCrop().into(image);
                                      break;

                                //TODO: fare in modo che siano decenti
                                case "audio":
                                    image.setImageResource(R.drawable.volume);
                                    image.setBackgroundColor(Color.parseColor("#C45852"));
                                    image.setLayoutParams(new LinearLayout.LayoutParams(150,150));
                                    break;
                                case "video":
                                    image.setImageResource(R.drawable.video_big);
                                    image.setBackgroundColor(Color.parseColor("#C45852"));
                                    image.setLayoutParams(new LinearLayout.LayoutParams(150,150));

                                    break;
                            }


                            TextView title = new TextView(getActivity());
                            title.setText(spot.title);
                            title.setPadding(16,16,16,16);

                            innerLayout.addView(image);
                            innerLayout.addView(title);

                            layout.addView(innerLayout);


                        }

                    }
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

    public void setLatLng(LatLngBounds mLatLngBounds){


        /* Initialization of coordinates bounds of the visible regions */
            if (mLatLngBounds.northeast.latitude < mLatLngBounds.southwest.latitude) {
                lowLat = mLatLngBounds.northeast.latitude;
                highLat = mLatLngBounds.southwest.latitude;
            } else {
                highLat = mLatLngBounds.northeast.latitude;
                lowLat = mLatLngBounds.southwest.latitude;
            }
            if (mLatLngBounds.northeast.longitude < mLatLngBounds.southwest.longitude) {
                lowLng = mLatLngBounds.northeast.longitude;
                highLng = mLatLngBounds.southwest.longitude;
            } else {
                highLng = mLatLngBounds.northeast.longitude;
                lowLng = mLatLngBounds.southwest.longitude;
            }

    }

    public void onClick(View view){

        Spot spot = (Spot)view.getTag();
        Log.d(TAG, "Spot clicked "+ spot.description);

        VisualizeSpotFragment VisualizeSpotFrag = new VisualizeSpotFragment();
        VisualizeSpotFrag.setSpot(spot);
        this.dismiss();
        VisualizeSpotFrag.show(getActivity().getSupportFragmentManager(), VisualizeSpotFrag.getTag());
    }
}

