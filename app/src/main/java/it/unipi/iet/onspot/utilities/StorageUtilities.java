package it.unipi.iet.onspot.utilities;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import android.net.Uri;
import android.support.annotation.NonNull;
import java.io.File;

/**
 * Firebase Storage utilities
 */

public class StorageUtilities {

    private StorageReference storageRef;
    private String latestURL;

    public StorageUtilities() {
        // Create a storage reference
        storageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://onspot-8c6f4.appspot.com");
        latestURL = "";
    }

    public String storeNewFile(String path, String userId) {
        Uri file = Uri.fromFile(new File(path));
        StorageReference childRef = storageRef.child(userId+"/"+file.getLastPathSegment());
        UploadTask uploadTask = childRef.putFile(file);

        // Register observers to listen for when the download is progressing, is done or is failed
        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                System.out.println("Upload is " + progress + "% done");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                latestURL="";
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                latestURL = downloadUrl.toString();
            }
        });

        return latestURL;
    }



}
