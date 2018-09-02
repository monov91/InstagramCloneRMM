package com.projects.radomonov.instagramclone.Utils;

import android.os.Environment;

public class FilePaths {
    //"storage/emulated/0"
    public String ROOT_DIRECTORY = Environment.getExternalStorageDirectory().getPath();

    public String CAMERA = ROOT_DIRECTORY + "/DCIM/Camera";

    public String PICTURES = ROOT_DIRECTORY + "/Pictures";

    public String FIREBASE_IMAGE_STORAGE = "photos/users/";
}
