package it.unipi.iet.onspot.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;

import it.unipi.iet.onspot.R;
import it.unipi.iet.onspot.utilities.Spot;
import it.unipi.iet.onspot.utilities.SpotViewHolder;


public abstract class ImageListFragment extends Fragment {

    private static final String TAG = "ImageListFragment";

    private DatabaseReference mDatabase;

    private FirebaseRecyclerAdapter<Spot, SpotViewHolder> mAdapter;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;

    public ImageListFragment() {}

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        Log.d(TAG, "Entered in onCreateView");
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_image_list, container, false);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        Log.d(TAG, "Database reference created");
        mRecycler = (RecyclerView) rootView.findViewById(R.id.images_list);
        mRecycler.setHasFixedSize(true);
        Log.d(TAG, "Recycler reference created");
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "Entered in onActivityCreated");
        super.onActivityCreated(savedInstanceState);

        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(getActivity());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);
        Log.d(TAG, "Layout Manager created");
        final Context context = this.getContext();

        // Set up FirebaseRecyclerAdapter with the Query
        Query uploadsQuery = getQuery(mDatabase);
        mAdapter = new FirebaseRecyclerAdapter<Spot, SpotViewHolder>(Spot.class, R.layout.item_image,
                SpotViewHolder.class, uploadsQuery) {
            @Override
            protected void populateViewHolder(final SpotViewHolder viewHolder, final Spot model,
                                              final int position) {
                final DatabaseReference upsRef = getRef(position);
                Log.d("ImageListFragment","populate view holder");
                // Bind Upload to ViewHolder, setting OnClickListener for the star button
                viewHolder.bindToUpload(context, model, new View.OnClickListener() {
                    //Click listener for hearts
                    @Override
                    public void onClick(View starView) {

                    }
                }, new View.OnClickListener() {
                    // Click listener for reproducing media
                    @Override
                    public void onClick(View starView) {

                    }
                });
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
        Log.d(TAG, "Activity created");
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAdapter != null) {
            mAdapter.cleanup();
        }
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public abstract Query getQuery(DatabaseReference databaseReference);
}