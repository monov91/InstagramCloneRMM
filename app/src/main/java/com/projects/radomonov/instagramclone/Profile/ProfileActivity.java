package com.projects.radomonov.instagramclone.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.projects.radomonov.instagramclone.R;
import com.projects.radomonov.instagramclone.Utils.BottomNavigatinoViewHelper;
import com.projects.radomonov.instagramclone.Utils.GridImageAdapter;
import com.projects.radomonov.instagramclone.Utils.UniversalImageLoader;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";
    private static final int ACTIVITY_NUM = 4   ;
    private static final int NUM_GRID_COLUMNS = 3;
    private Context mContext = ProfileActivity.this;
    private ProgressBar mProrgressBar ;
    private ImageView profilePhoto;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile );
        Log.d(TAG, "onCreate: Started");

        init();

     /*   setUpBottomNavigationView();
        setUpToolbar();
        setupActivityWidgets();
        setProfileImage();
        tempGridSetup();*/
    }

    private void init(){
        Log.d(TAG, "init: inflating " + getString(R.string.profile_fragment));

        ProfileFragment fragment = new ProfileFragment();
        android.support.v4.app.FragmentTransaction transaction = ProfileActivity.this.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container,fragment);

        transaction.addToBackStack(getString(R.string.profile_fragment));
        transaction.commit();
    }

    /*private void setupActivityWidgets(){
        mProrgressBar = findViewById(R.id.profileProgressBar);
        mProrgressBar.setVisibility(View.GONE);
        profilePhoto = findViewById(R.id.profile_photo);

    }




    private void setProfileImage(){
        Log.d(TAG, "setProfileImage: setting profile photo");
        String imgURL =  "support.appsflyer.com/hc/article_attachments/115011109089/android.png";
        UniversalImageLoader.setImage(imgURL,profilePhoto,mProrgressBar,"https://");
    }

    private void setupImageGrid(ArrayList<String> imgURLS){
        GridView gridView = findViewById(R.id.gridView);

        int gridWidth = getResources().getDisplayMetrics().widthPixels;
        int imageWidth = gridWidth/NUM_GRID_COLUMNS;
        gridView.setColumnWidth(imageWidth);

        GridImageAdapter adapter = new GridImageAdapter(mContext,R.layout.layout_grid_imageview,"",imgURLS);
        gridView.setAdapter(adapter);
    }

    private void tempGridSetup(){
        ArrayList<String> imgURLS = new ArrayList<>();
        imgURLS.add("http://res.cloudinary.com/simpleview/image/upload/v1457618103/clients/roanoke/Virginia_Scenic_Drives_2685d6d1-bc29-4201-a148-2b9558b45d24.jpg");
        imgURLS.add("https://cdn.static-economist.com/sites/default/files/images/print-edition/20170722_STP001_0.jpg");
        imgURLS.add("http://kb4images.com/images/scenic-pictures/37901448-scenic-pictures.jpg");
        imgURLS.add("http://res.cloudinary.com/simpleview/image/upload/v1457618103/clients/roanoke/Virginia_Scenic_Drives_2685d6d1-bc29-4201-a148-2b9558b45d24.jpg");
        imgURLS.add("https://cdn.static-economist.com/sites/default/files/images/print-edition/20170722_STP001_0.jpg");
        imgURLS.add("http://kb4images.com/images/scenic-pictures/37901448-scenic-pictures.jpg");
        imgURLS.add("http://res.cloudinary.com/simpleview/image/upload/v1457618103/clients/roanoke/Virginia_Scenic_Drives_2685d6d1-bc29-4201-a148-2b9558b45d24.jpg");
        imgURLS.add("https://cdn.static-economist.com/sites/default/files/images/print-edition/20170722_STP001_0.jpg");
        imgURLS.add("http://kb4images.com/images/scenic-pictures/37901448-scenic-pictures.jpg");
        imgURLS.add("http://res.cloudinary.com/simpleview/image/upload/v1457618103/clients/roanoke/Virginia_Scenic_Drives_2685d6d1-bc29-4201-a148-2b9558b45d24.jpg");
        imgURLS.add("https://cdn.static-economist.com/sites/default/files/images/print-edition/20170722_STP001_0.jpg");
        imgURLS.add("http://kb4images.com/images/scenic-pictures/37901448-scenic-pictures.jpg");
        imgURLS.add("https://cdn.static-economist.com/sites/default/files/images/print-edition/20170722_STP001_0.jpg");
        imgURLS.add("http://kb4images.com/images/scenic-pictures/37901448-scenic-pictures.jpg");
        imgURLS.add("http://res.cloudinary.com/simpleview/image/upload/v1457618103/clients/roanoke/Virginia_Scenic_Drives_2685d6d1-bc29-4201-a148-2b9558b45d24.jpg");
        imgURLS.add("https://cdn.static-economist.com/sites/default/files/images/print-edition/20170722_STP001_0.jpg");
        imgURLS.add("http://kb4images.com/images/scenic-pictures/37901448-scenic-pictures.jpg");
        imgURLS.add("https://cdn.static-economist.com/sites/default/files/images/print-edition/20170722_STP001_0.jpg");
        imgURLS.add("http://kb4images.com/images/scenic-pictures/37901448-scenic-pictures.jpg");
        imgURLS.add("http://res.cloudinary.com/simpleview/image/upload/v1457618103/clients/roanoke/Virginia_Scenic_Drives_2685d6d1-bc29-4201-a148-2b9558b45d24.jpg");
        imgURLS.add("https://cdn.static-economist.com/sites/default/files/images/print-edition/20170722_STP001_0.jpg");
        imgURLS.add("http://kb4images.com/images/scenic-pictures/37901448-scenic-pictures.jpg");

        setupImageGrid(imgURLS);
    }*/
}
