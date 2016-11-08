package it.unipi.iet.onspot.utilities;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import it.unipi.iet.onspot.R;


public class SpotViewHolder extends RecyclerView.ViewHolder {

    private ImageView categoryImageView;
    private TextView headlineView;
    private TextView categoryTextView;
    private TextView descriptionView;
    private ImageView contentImageView;
    private Button play;
    private Button audio;
    private ImageView userImageView;
    private TextView userTextView;
    private TextView heartTextView;
    public ImageView heartImageView;
    private Context context;

    public SpotViewHolder(View itemView) {
        super(itemView);

        categoryImageView = (ImageView) itemView.findViewById(R.id.image_category);
        headlineView = (TextView) itemView.findViewById(R.id.headline);
        categoryTextView = (TextView) itemView.findViewById(R.id.text_category);
        descriptionView = (TextView) itemView.findViewById(R.id.description);
        contentImageView = (ImageView) itemView.findViewById(R.id.image);
        userImageView = (ImageView) itemView.findViewById(R.id.image_user);
        userTextView = (TextView) itemView.findViewById(R.id.text_user);
        heartTextView = (TextView) itemView.findViewById(R.id.heart_number);
        heartImageView = (ImageView) itemView.findViewById(R.id.heart_image);
    }

    public void bindToUpload(Context cont, Spot upload, View.OnClickListener heartClickListener,
                             View.OnClickListener mediaClickListener) {

        context = cont;
        // Title layout
        headlineView.setText(upload.title);
        categoryTextView.setText(upload.category);
        categoryImageView.setImageResource(categoryResource(upload.category));

        // Center content layout
        //TODO: fare in modo che funzioni anche con video e audio
        handleContent(upload, mediaClickListener);
        descriptionView.setText(upload.description);

        // Bottom line layout
        loadProfile(upload);
        heartTextView.setText(String.valueOf(upload.heartCount));
        heartImageView.setOnClickListener(heartClickListener);
    }

    // Function that returns the category icon correspondent to a certain category name
    public int categoryResource(String category) {
        final String [] categories = context.getResources().getStringArray(R.array.categories_names);
        final TypedArray icons = context.getResources().obtainTypedArray(R.array.categories_icons);
        for(int i=0; i<categories.length; i++) {
            if(category.equals(categories[i]))
                return icons.getResourceId(i,R.drawable.dots);
        }
        return R.drawable.dots;
    }

    public void handleContent(Spot upload, View.OnClickListener mediaClickListener) {
        switch(upload.Type) {
            case "image":
                Picasso.with(context).load(upload.contentURL).into(contentImageView);
                break;
            case "video":
                //Picasso.with(context).load(upload.frameURL).into(contentImageView);
                //play.setVisibility(View.VISIBLE);
                contentImageView.setOnClickListener(mediaClickListener);
                break;
            case "audio":
                contentImageView.setImageResource(R.drawable.volume);
                //audio.setVisibility(View.VISIBLE);
                contentImageView.setOnClickListener(mediaClickListener);
                break;
        }
    }

    public void loadProfile(Spot spot) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        final DatabaseReference newRef = mDatabase.child("users").child(spot.userId);
        newRef.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                User user = dataSnapshot.getValue(User.class);
                userTextView.setText(user.firstName);
                Picasso.with(context).load(user.photoURL).transform(new CircleTransform()).into(userImageView);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.d("SpotViewHolder", "loadUser:onCancelled", databaseError.toException());
                // ...
            }
        });
    }
}