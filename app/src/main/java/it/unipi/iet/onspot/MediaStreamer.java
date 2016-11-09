package it.unipi.iet.onspot;

import android.app.ActionBar;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;
import com.squareup.picasso.Picasso;
import it.unipi.iet.onspot.R;


/* Activity to play streaming video or audio file and to visualize image in full screen */
public class MediaStreamer extends Activity {



    private String url;


    private static final String CONTENT_URL = "it.unipi.iet.onspot.CONTENT_URL";
    private static final String TYPE = "it.unipi.iet.onspot.TYPE";
    private static final String TAG = "VideoStreamer";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_streamer);

        // Retrieve File URL from intent
        url = getIntent().getStringExtra(CONTENT_URL);

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

        vidView.start();

        }
        else{

          // visualize image in full screen
            ImageView imageView = (ImageView) findViewById(R.id.myImage);
            Picasso.with(this).load(url).into(imageView);

        }



    }


}
