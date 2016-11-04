package it.unipi.iet.onspot.utilities;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import it.unipi.iet.onspot.R;

/**
 * Async Task to download file from http url
 */

public class DownloadTask extends AsyncTask<String, Integer, Bitmap> {

    private Activity mActivity;
    private ImageView image;

    private String TAG = "DownloadTask";

    public DownloadTask(Activity mActivity, ImageView image) {
        super();
        this.mActivity = mActivity;
        this.image = image;
    }

    @Override
    protected Bitmap doInBackground(String... urls) {
        return download_Image(urls[0]);
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        LinearLayout layout = (LinearLayout) mActivity.findViewById(R.id.spot_container);
        result = MultimediaUtilities.resize(result,200,200);
        image.setImageBitmap(result);
        // Adds the view to the layout
        layout.addView(image); }

    private Bitmap download_Image(String url) {

        Bitmap bmp;
        try{
            URL ulrn = new URL(url);
            HttpURLConnection con = (HttpURLConnection)ulrn.openConnection();
            InputStream is = con.getInputStream();
            bmp = BitmapFactory.decodeStream(is);
            if (null != bmp)
                return bmp;

        }catch(Exception e){
            Log.d(TAG,"Error: " +e.getMessage());
        }
        return null;
    }
}