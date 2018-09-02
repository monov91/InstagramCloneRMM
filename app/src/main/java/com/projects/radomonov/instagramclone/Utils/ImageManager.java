package com.projects.radomonov.instagramclone.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ImageManager {

    private static final String TAG = "ImageManager";

    public static final Bitmap getBitmap(String imgURL){
        File imgFile = new File(imgURL);
        FileInputStream fis = null;
        Bitmap bitmap = null;

        try {
            fis = new FileInputStream(imgFile);
            bitmap = BitmapFactory.decodeStream(fis);
        } catch(FileNotFoundException e){
            Log.d(TAG, "getBitmap: FileNotFoundException    " + e.getMessage());
        } finally {
            try {
                fis.close();
            } catch (IOException e) {
                Log.d(TAG, "getBitmap: IOException while closing File input stream : " + e.getMessage());
            }
        }
        return bitmap;
    }

    /**
     * return byte array from a bitmap
     * quality is percentage -   [1;100]
     * @param bitmap
     * @param quality
     * @return
     */
    public static byte[] getBytesFromBitmap(Bitmap bitmap,int quality){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,quality,stream);
        return stream.toByteArray();
    }




}
