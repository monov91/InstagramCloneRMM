package com.projects.radomonov.instagramclone.Utils;

import android.util.Log;

import java.io.File;
import java.util.ArrayList;

public class FileSearch {
    private static final String TAG = "FileSearch";
    /**
     * Search a directory and
     * return all the directories inside of the directory
     * @param directory
     * @return
     */
    public static ArrayList<String> getDirectoryPaths(String directory){
        ArrayList<String> pathArray = new ArrayList<>();
        File file = new File(directory);
        File[] listFiles = file.listFiles();
        Log.d(TAG, "getDirectoryPaths: list files length " + listFiles.length);
        for(int i = 0; i < listFiles.length; i ++){
            if(listFiles[i].isDirectory()){
                pathArray.add(listFiles[i].getAbsolutePath());
            }
        }
        return pathArray;
    }

    /**
     * Return a list of all files inside of a directory
     * @param directory
     * @return
     */
    public static ArrayList<String> getFilePaths(String directory){
        ArrayList<String> pathArray = new ArrayList<>();
        File file = new File(directory);
        File[] listFiles = file.listFiles();
        Log.d(TAG, "getFilePaths: directory = " + directory);
        Log.d(TAG, "getFilePaths:  array : " + listFiles.toString());
        for(int i = 0; i < listFiles.length; i ++){
            if(listFiles[i].isFile()){
                pathArray.add(listFiles[i].getAbsolutePath());
            }
        }
        return pathArray;
    }
}
