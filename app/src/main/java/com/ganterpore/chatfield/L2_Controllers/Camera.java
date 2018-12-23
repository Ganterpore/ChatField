package com.ganterpore.chatfield.L2_Controllers;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class Camera {
    private static String currentPhotoPath;

    /**
     * Takes a picture with the camera app, and saves it to the device
     * @param activity, the activity the photo is being taken from
     * @param resultCode, When the camera activity is finished, it will return this code
     * @throws IOException, if it fails to find the file location
     */
    public static void takePicture(Activity activity, int resultCode) throws IOException {
        Intent photoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //check there is an app available to handle photos
        if (photoIntent.resolveActivity(activity.getPackageManager()) != null) {
            //get the file for the photo locations
            File photoFile = createImageFile(activity);
            if(photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(activity,
                        "com.ganterpore.chatfield.fileprovider",
                        photoFile);
                photoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                //start activity
                activity.startActivityForResult(photoIntent, resultCode);
            }
        }
    }

    /**
     * gets the file path of the last photo taken with the Camera
     * @return the File of the last photo
     */
    public static File getLastPhoto() {
        return new File(currentPhotoPath);
    }

    private static File createImageFile(Activity activity) throws IOException {
        // Create a unique image file name
        String timeStamp = Long.toString(System.currentTimeMillis());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        Log.d("A", "createImageFile: "+storageDir.getAbsolutePath());
        File image = File.createTempFile(imageFileName,".jpg",storageDir);

        //save the path to the recent photo
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
}
