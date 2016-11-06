package it.unipi.iet.onspot.utilities;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import it.unipi.iet.onspot.R;


public class SpotViewHolder extends RecyclerView.ViewHolder {

    public ImageView categoryImageView;
    public TextView headlineView;
    public TextView categoryTextView;
    public TextView descriptionView;
    public ImageView thumbView;
    public ImageView userImageView;
    public TextView userTextView;
    public TextView heartTextView;
    public ImageView heartImageView;

    public SpotViewHolder(View itemView) {
        super(itemView);

        categoryImageView = (ImageView) itemView.findViewById(R.id.image_category);
        headlineView = (TextView) itemView.findViewById(R.id.headline);
        categoryTextView = (TextView) itemView.findViewById(R.id.text_category);
        descriptionView = (TextView) itemView.findViewById(R.id.description);
        thumbView = (ImageView) itemView.findViewById(R.id.image);
        userImageView = (ImageView) itemView.findViewById(R.id.image_user);
        userTextView = (TextView) itemView.findViewById(R.id.text_user);
        heartTextView = (TextView) itemView.findViewById(R.id.heart_number);
        heartImageView = (ImageView) itemView.findViewById(R.id.heart_image);
    }

    public void bindToUpload(Context context, Spot upload) {
        //TODO: aggiungere titolo alla add spot e poi recuperarlo qui
        //TODO: sistemare dimensioni perchè la seconda riga non si vede
        // Title layout
        headlineView.setText("Title");
        categoryTextView.setText(upload.category);
        categoryImageView.setImageResource(categoryResource(upload.category, context));

        // Center content layout
        Picasso.with(context).load(upload.contentURL).into(thumbView);
        descriptionView.setText(upload.description);

        // Bottom line layout
        //Picasso.with(context).load("").into(userImageView);
        //TODO: sistemare layout perchè si vede solo la prima immagine
        userImageView.setImageResource(R.drawable.avataryellow);
        userTextView.setText("Erica");
        heartTextView.setText("5");
        heartImageView.setImageResource(R.drawable.heart);
    }

    // Function that returns the category icon correspondent to a certain category name
    public int categoryResource(String category, Context context) {
        final String [] categories = context.getResources().getStringArray(R.array.categories_names);
        final TypedArray icons = context.getResources().obtainTypedArray(R.array.categories_icons);
        for(int i=0; i<categories.length; i++) {
            if(category.equals(categories[i]))
                return icons.getResourceId(i,R.drawable.avataryellow);
        }
        //TODO: cambiare immagine di default
        return R.drawable.avataryellow;
    }
}