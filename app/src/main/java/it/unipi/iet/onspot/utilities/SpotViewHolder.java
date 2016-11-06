package it.unipi.iet.onspot.utilities;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import it.unipi.iet.onspot.R;


public class SpotViewHolder extends RecyclerView.ViewHolder {
    public TextView nicknameView;
    public ImageView thumbView;

    public SpotViewHolder(View itemView) {
        super(itemView);

        nicknameView = (TextView) itemView.findViewById(R.id.nickname);
        thumbView = (ImageView) itemView.findViewById(R.id.image);
    }

    public void bindToUpload(Context context, Spot upload) {
        nicknameView.setText(upload.userId);
        // Load the image with the help of the Picasso library
        Picasso.with(context).load(upload.contentURL).into(thumbView);
    }
}