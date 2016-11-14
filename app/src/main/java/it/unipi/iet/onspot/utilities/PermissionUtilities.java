package it.unipi.iet.onspot.utilities;

import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;


public abstract class PermissionUtilities {


    /*
     * Checks for the permission specified by the arguments and, if not found, calls the
     * requestPermission function.
     */
    private static boolean checkPermission(AppCompatActivity activity, String permission) {
        return (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED);
    }

    /**
     * Requests the permission. If a rationale with an additional explanation should
     * be shown to the user, displays a dialog that triggers the request.
     */

    public static boolean checkAndRequestPermissions(AppCompatActivity activity, String[] permissions, int requestId) {

        List<String> listPermissionsNeeded = new ArrayList<>();

        for (String permission : permissions) {
            if (!checkPermission(activity, permission)) {
                listPermissionsNeeded.add(permission);
            }
        }

        if (!listPermissionsNeeded.isEmpty()) {

            ActivityCompat.requestPermissions(activity, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), requestId);
            return false;
        }
        return true;
    }

    /**
     * Checks if the result contains a PERMISSION_GRANTED result for a
     * permission from a runtime permissions request.
     */
    public static boolean isPermissionGranted(String[] grantPermissions, int[] grantResults,
                                              String permission) {
        for (int i = 0; i < grantPermissions.length; i++) {
            if (permission.equals(grantPermissions[i])) {
                return grantResults[i] == PackageManager.PERMISSION_GRANTED;
            }
        }
        return false;
    }
}