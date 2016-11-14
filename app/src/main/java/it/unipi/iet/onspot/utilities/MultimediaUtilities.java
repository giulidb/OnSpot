package it.unipi.iet.onspot.utilities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
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
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.os.Parcelable;
import android.provider.DocumentsContract;
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



    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     *
     */
    public static String getRealPathFromURI(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
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

