package it.unipi.iet.onspot.fragments;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.vision.text.Text;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import it.unipi.iet.onspot.MediaStreamer;
import it.unipi.iet.onspot.R;
import it.unipi.iet.onspot.utilities.Spot;
import it.unipi.iet.onspot.utilities.SpotViewHolder;

/**
 *  Fragment that shows list of visible spots when users click on the list button in the toolbar
 */

public class ListSpotFragment extends BottomSheetDialogFragment implements View.OnClickListener {


    /* Coordinates Visible Regions */
    double lowLat;
    double lowLng;
    double highLat;
    double highLng;

    private FirebaseRecyclerAdapter<Spot, SpotViewHolder> mAdapter;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;
    private DatabaseReference mDatabase;

    private static final String CONTENT_URL = "it.unipi.iet.onspot.CONTENT_URL";
    private static final String TYPE = "it.unipi.iet.onspot.TYPE";
    private final String TAG = "ListSpotFragment";


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
        CoordinatorLayout.Behavior behavior;

        View contentView = View.inflate(getContext(), R.layout.fragment_image_list, null);
        dialog.setContentView(contentView);

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        behavior = params.getBehavior();

        if (behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_image_list, container, false);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        Log.d(TAG, "Database reference created");
        mRecycler = (RecyclerView) view.findViewById(R.id.images_list);
        mRecycler.setHasFixedSize(true);
        Log.d(TAG, "Recycler reference created");

        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(getActivity());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);
        Log.d(TAG, "Layout Manager created");
        final Context context = this.getContext();

        //Set title
        TextView title = (TextView)view.findViewById(R.id.title);
        title.setText("Visible spots");
        title.setBackgroundColor(Color.parseColor("#4ECDC4"));

        // Set up FirebaseRecyclerAdapter with the Query
        Query uploadsQuery = getQuery(mDatabase);
        mAdapter = new FirebaseRecyclerAdapter<Spot, SpotViewHolder>(Spot.class, R.layout.item_image,
                SpotViewHolder.class, uploadsQuery) {
            @Override
            protected void populateViewHolder(final SpotViewHolder viewHolder, final Spot model,
                                              final int position) {
                final DatabaseReference upsRef = getRef(position);
                Log.d("ImageListFragment", "populate view holder");

                if (model.Lng > lowLng && model.Lng < highLng) {
                    // Determine if the current user has liked this image and set UI accordingly
                    if (model.hearts.containsKey(getUid())) {
                        viewHolder.heartImageView.setImageResource(R.drawable.heart_full);
                    } else {
                        viewHolder.heartImageView.setImageResource(R.drawable.heart_empty);
                    }

                    // Bind Upload to ViewHolder, setting OnClickListener for the star button
                    viewHolder.bindToUpload(context, model, new View.OnClickListener() {
                        //Click listener for hearts
                        @Override
                        public void onClick(View heartView) {
                            // Do not allow liking own images
                            if (model.userId.compareTo(getUid()) == 0) {
                                return;
                            }

                            // Need to write to the place where the post is stored
                            DatabaseReference uploadsRef = mDatabase.child("spots").child(upsRef.getKey());
                            onHeartClicked(uploadsRef);
                        }
                    }, new View.OnClickListener() {
                        // Click listener for reproducing media
                        @Override
                        public void onClick(View contentView) {

                            Log.d(TAG, "click on View ");
                            // Start MediaStreamer to Reproduce Media
                            Intent i = new Intent(getActivity(), MediaStreamer.class);
                            String content_url = contentView.getTag().toString().split(";")[0];
                            content_url = content_url.substring(0, content_url.length() - 1);
                            String type = contentView.getTag().toString().split(";")[1];
                            Log.d(TAG, content_url);
                            Log.d(TAG, type);
                            i.putExtra(CONTENT_URL, content_url);
                            i.putExtra(TYPE, type);
                            startActivity(i);

                        }
                    });
                } else {
                    viewHolder.removeView();
                }
            }
        };
                Log.d(TAG, "FirebaseRecyclerAdapter created");

                mAdapter.setHasStableIds(true);

                RecyclerView.ItemAnimator animator = mRecycler.getItemAnimator();
                if (animator instanceof SimpleItemAnimator) {
                    ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
                }

                // Scroll to top on new uploads
                mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                    @Override
                    public void onItemRangeInserted(int positionStart, int itemCount) {
                        mManager.smoothScrollToPosition(mRecycler, null, mAdapter.getItemCount());
                    }
                });

                mRecycler.setAdapter(mAdapter);


        return view;
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAdapter != null) {
            mAdapter.cleanup();
        }
    }

    public void setLatLng(LatLngBounds mLatLngBounds) {


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

    public void onClick(View view) {

        String spot_key = (String) view.getTag();

        Log.d(TAG, spot_key);

        VisualizeSpotFragment VisualizeSpotFrag = new VisualizeSpotFragment();
        VisualizeSpotFrag.setSpot(spot_key);
        this.dismiss();
        VisualizeSpotFrag.show(getActivity().getSupportFragmentManager(), VisualizeSpotFrag.getTag());
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }


    public Query getQuery(DatabaseReference databaseReference) {

        Query listQuery = databaseReference.child("spots").orderByChild("Lat").startAt(lowLat).endAt(highLat);
        /*listQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot spotSnapshot : dataSnapshot.getChildren()) {
                    double Long = (double)spotSnapshot.child("Lat").getValue();
                    Query k = listQuery.orderByChild("Lng").startAt(lowLng).endAt(highLng);
                    if (Long>lowLng && Long<highLng) {
                        String key = spotSnapshot.getKey();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {

            }
        });*/

        return listQuery;
    }


}

