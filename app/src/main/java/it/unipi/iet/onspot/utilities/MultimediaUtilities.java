package it.unipi.iet.onspot.utilities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to manage Multimedia Files;
 */
public class MultimediaUtilities {

    /* function to create and launch an intent to pick multimedia file from other app like
     camera or audio recorder or from the file system */
    public static void create_intent(String file, Activity activity){

        String TAG = "MultimediaUtilities";
        final int RESULT_LOAD = 1;

        // Camera or VideoCamera or AudioRegister
        final List<Intent> fileIntents = new ArrayList<>();
        final Intent captureIntent;

        // Discriminate app to launch in base on the String file: image,video or audio
            if(file.equals("image")){
                captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);}
            else if(file.equals("video")){
                captureIntent = new Intent(android.provider.MediaStore.ACTION_VIDEO_CAPTURE);
            }else
                captureIntent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);

        final PackageManager packageManager = activity.getPackageManager();
        final List<ResolveInfo> listFile = packageManager.queryIntentActivities(captureIntent, 0);
        for(ResolveInfo res : listFile) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            fileIntents.add(intent);
        }

        // Filesystem, String file determine the kind of file to search in the filesystem
        final Intent galleryIntent = new Intent();
        galleryIntent.setType(file+"/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

        // Chooser of filesystem options.
        final Intent chooserIntent = Intent.createChooser(galleryIntent, "Complete action using");

        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, fileIntents.toArray(new Parcelable[fileIntents.size()]));
        activity.startActivityForResult(chooserIntent, RESULT_LOAD);

    }

    /* Function that reads orientation TAG from a bitmap file and return tue image in the
    * the correct rotation
    */

    public static Bitmap rotateBitmap(Bitmap bm, String path ) {

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

        Log.d(TAG,"bitmap orientation: " + orientation);

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
        }
        catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    /* Function that return the real path of an image from its URI */

    public static String getImagePath(Activity activity,Uri uri){
        Cursor cursor = activity.getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":")+1);
        cursor.close();

        cursor = activity.getContentResolver().query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }


}
