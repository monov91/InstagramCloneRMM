package com.projects.radomonov.instagramclone.Share;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.projects.radomonov.instagramclone.R;
import com.projects.radomonov.instagramclone.Utils.FirebaseHelper;
import com.projects.radomonov.instagramclone.Utils.UniversalImageLoader;

public class PostActivity extends AppCompatActivity{
    private static final String TAG = "PostActivity";

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseHelper mFirebaseHelper;

    // vars
    private String mAppend = "file:/";
    private int imageCount = 0;
    private String imgURL;

    //widgets
    private EditText mCaption;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        setupFirebaseAuth();
        mFirebaseHelper = new FirebaseHelper(PostActivity.this);
        mCaption = findViewById(R.id.description);
        setImage();

        ImageView backArrow = findViewById(R.id.ivBackarrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: closing the post activity");
                finish();
            }
        });

        TextView tvShare = findViewById(R.id.tvShare);
        tvShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //upload the image to firebase
                Log.d(TAG, "onClick: Attempting to upload new photo");
                String caption = mCaption.getText().toString();
                mFirebaseHelper.uploadNewPhoto(getString(R.string.new_photo),caption,imageCount,imgURL);
            }
        });



    }



    private void someMethod(){
        /* Step 1
         Create a data model for photos

         Step 2
         add properties to the photo objects

         Step 3
         count the number of photos the user has

         Step 4
            -Upload a photo to Firebase Storage and insert 2 new nodes in the Firbase database
            -insert into photos node
            -insert into urer_photos node
*/
    }

    /**
     * get the image from the incoming intent and display it
     */
    private void setImage(){
        Intent intent = getIntent();
        imgURL = intent.getStringExtra(getString(R.string.selected_image));
        ImageView imageView = findViewById(R.id.imageShare);
        UniversalImageLoader.setImage(imgURL,imageView,null,mAppend);
    }
       /*
    ----------------------------------------- Firebase ----------------------------------
     */



    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mAuthStateListener != null){
            mAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    /*
        Setup the Firebase auth object
         */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting up Firebase auth");

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        Log.d(TAG, "onDataChange: imagecount : " + imageCount);

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if(user != null){
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged: signed_id: " + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged: signed_out " );
                }
            }
        };

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                imageCount = mFirebaseHelper.getImageCount(dataSnapshot);
                Log.d(TAG, "onDataChange: imagecount : " + imageCount);
                // Retrieve images for the user

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
