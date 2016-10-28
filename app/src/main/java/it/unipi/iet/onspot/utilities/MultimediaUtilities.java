package it.unipi.iet.onspot.utilities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import it.unipi.iet.onspot.R;

/**
 * Class to manage Multimedia Files;
 */
public class MultimediaUtilities {

    /* function to create and launch an intent to pick multimedia file from other app like
     camera or audio recorder or from the file system */
    public static void create_intent(String file, Activity activity) {

        final int IMAGE_REQUEST_CODE = 1;
        final int VIDEO_REQUEST_CODE = 2;
        final int AUDIO_REQUEST_CODE = 3;


        // Camera or VideoCamera or AudioRegister
        final List<Intent> fileIntents = new ArrayList<>();
        final Intent captureIntent;

        // Discriminate app to launch in base on the String file: image,video or audio
        switch (file) {
            case "image":
                captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                break;
            case "video":
                captureIntent = new Intent(android.provider.MediaStore.ACTION_VIDEO_CAPTURE);
                break;
            default:
                captureIntent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
        }

        final PackageManager packageManager = activity.getPackageManager();
        final List<ResolveInfo> listFile = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listFile) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            fileIntents.add(intent);
        }

        // Filesystem, String file determine the kind of file to search in the filesystem
        final Intent galleryIntent = new Intent();
        galleryIntent.setType(file + "/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

        // Chooser of filesystem options.
        final Intent chooserIntent = Intent.createChooser(galleryIntent, "Complete action using");

        // Add the multimedia app options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, fileIntents.toArray(new Parcelable[fileIntents.size()]));

        switch (file) {
            case "image":
                activity.startActivityForResult(chooserIntent, IMAGE_REQUEST_CODE);
                break;
            case "video":
                activity.startActivityForResult(chooserIntent, VIDEO_REQUEST_CODE);
                break;
            case "audio":
                activity.startActivityForResult(chooserIntent, AUDIO_REQUEST_CODE);
        }

    }

    /* Function that reads orientation TAG from a bitmap file and return tue image in the
    * the correct rotation
    */

    @Nullable
    public static Bitmap rotateBitmap(Bitmap bm, String path) {

        String TAG = "MultimediaUtilities";

        // create ExifInterface
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // get current orientation
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        Log.d(TAG, "bitmap orientation: " + orientation);

        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bm;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bm;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
            bm.recycle();
            return bmRotated;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    /*  Scales the image the minimum amount while making sure that at least
      * one of the two dimensions fit inside the requested destination area.
      * Parts of the source image will be cropped to realize this.*/
    public static Bitmap resize(Bitmap image, int width, int heigth) {

        Rect srcRect;
        int srcWidth = image.getWidth();
        int srcHeight = image.getHeight();

        // Calculates source rectangle for scaling bitmap
        final float srcAspect = (float) srcWidth / (float) srcHeight;
        final float dstAspect = (float) width / (float) heigth;

        if (srcAspect > dstAspect) {
            final int srcRectWidth = (int) (srcHeight * dstAspect);
            final int srcRectLeft = (srcWidth - srcRectWidth) / 2;
            srcRect = new Rect(srcRectLeft, 0, srcRectLeft + srcRectWidth, srcHeight);
        } else {
            final int srcRectHeight = (int) (srcWidth / dstAspect);
            final int scrRectTop = (srcHeight - srcRectHeight) / 2;
            srcRect = new Rect(0, scrRectTop, srcWidth, scrRectTop + srcRectHeight);
        }

        Rect dstRect = new Rect(0, 0, width, heigth);

        Bitmap scaledBitmap = Bitmap.createBitmap(dstRect.width(), dstRect.height(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(scaledBitmap);
        canvas.drawBitmap(image, srcRect, dstRect, new Paint(Paint.FILTER_BITMAP_FLAG));
        return scaledBitmap;
    }




    /* Function that return the real path of an image from its URI */
    public static String getRealPathFromURI(Activity activity, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = activity.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /* Function to reproduce media in full screen */
    public static void open_media(int viewId, String path, Activity activity){

        Intent viewMediaIntent = new Intent();
        viewMediaIntent.setAction(android.content.Intent.ACTION_VIEW);
        File file = new File(path);
        switch(viewId){
            case R.id.play:
                viewMediaIntent.setDataAndType(Uri.fromFile(file), "video/*");
                break;
            case R.id.load:
                viewMediaIntent.setDataAndType(Uri.fromFile(file), "image/*");
                break;
            case R.id.imageProfile:
                viewMediaIntent.setDataAndType(Uri.fromFile(file), "image/*");
                break;
            case R.id.audio:
                viewMediaIntent.setDataAndType(Uri.fromFile(file), "audio/*");}
        viewMediaIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        activity.startActivity(viewMediaIntent);
        }


    /* function to Rounded crop an Image */
    public static Bitmap getRoundedCroppedBitmap(Bitmap bitmap, int radius) {
        Bitmap finalBitmap;
        if (bitmap.getWidth() != radius || bitmap.getHeight() != radius)
            finalBitmap = MultimediaUtilities.resize(bitmap,radius,radius);
        else
            finalBitmap = bitmap;
        Bitmap output = Bitmap.createBitmap(finalBitmap.getWidth(),
                finalBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, finalBitmap.getWidth(),
                finalBitmap.getHeight());

        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(Color.parseColor("#BAB399"));
        if(finalBitmap.getWidth() > finalBitmap.getHeight()){
            canvas.drawCircle(finalBitmap.getWidth() / 2 + 0.7f,
                    finalBitmap.getHeight() / 2 + 0.7f,
                    finalBitmap.getHeight() / 2 +0.1f, paint);}
        else{
            canvas.drawCircle(finalBitmap.getWidth() / 2 + 0.7f,
                    finalBitmap.getHeight() / 2 + 0.7f,
                    finalBitmap.getWidth() / 2 + 0.1f, paint);
        }
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(finalBitmap, rect, rect, paint);

        return output;
    }
    }

