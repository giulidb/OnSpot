package it.unipi.iet.onspot;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;
import com.squareup.picasso.Picasso;

/* Activity to play streaming video or audio file and to visualize image in full screen */
public class MediaStreamer extends Activity {

    private static final String CONTENT_URL = "it.unipi.iet.onspot.CONTENT_URL";
    private static final String TYPE = "it.unipi.iet.onspot.TYPE";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_streamer);

        ImageView imageView = (ImageView) findViewById(R.id.myImage);
        // Retrieve File URL from intent
        String url = getIntent().getStringExtra(CONTENT_URL);

        // discriminate image case from the video or audio ones
      if(!(getIntent().getStringExtra(TYPE).equals("image"))){

          //initialization video view
        Uri vidUri = Uri.parse(url);
        VideoView vidView = (VideoView)findViewById(R.id.myVideo);
        vidView.setVideoURI(vidUri);

          // add command line to video view
        MediaController vidControl = new MediaController(this);
        vidControl.setAnchorView(vidView);
        vidView.setMediaController(vidControl);

       if((getIntent().getStringExtra(TYPE).equals("video"))){
          imageView.setVisibility(View.INVISIBLE);}
       else{
           imageView.setVisibility(View.VISIBLE);}



        vidView.start();

        }
        else{

          // visualize image in full screen
          imageView.setVisibility(View.VISIBLE);
          Picasso.with(this).load(url).into(imageView);

        }



    }


}
