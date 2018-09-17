package com.projects.radomonov.instagramclone.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.projects.radomonov.instagramclone.Models.Photo;
import com.projects.radomonov.instagramclone.Models.User;
import com.projects.radomonov.instagramclone.R;
import com.projects.radomonov.instagramclone.Utils.BottomNavigatinoViewHelper;
import com.projects.radomonov.instagramclone.Utils.GridImageAdapter;
import com.projects.radomonov.instagramclone.Utils.UniversalImageLoader;
import com.projects.radomonov.instagramclone.Utils.ViewCommentsFragment;
import com.projects.radomonov.instagramclone.Utils.ViewProfileFragment;
import com.projects.radomonov.instagramclone.ViewPostFragment;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity implements
        ProfileFragment.OnClickImageSelectedListener,
        ViewPostFragment.OnCommentThreadSelectedListener,
        ViewProfileFragment.OnClickImageSelectedListener {
    private static final String TAG = "ProfileActivity";

    //Navigate to comments fragment
    @Override
    public void onCommentThreadSelected(Photo photo) {
        Log.d(TAG, "onCommentThreadSelected: selected a comment thread");
        ViewCommentsFragment fragment = new ViewCommentsFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(getString(R.string.photo), photo);
        fragment.setArguments(bundle);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(getString(R.string.view_comments_fragment));
        transaction.commit();
    }

    //Navigate to View Post fragment
    @Override
    public void onGridImageSelected(Photo photo, int activityNumber) {
        Log.d(TAG, "onGridImageSelected: selected an image from gridview" + photo.toString());

        ViewPostFragment fragment = new ViewPostFragment();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.photo), photo);
        args.putInt(getString(R.string.activity_number), activityNumber);
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(getString(R.string.view_post_fragment));
        transaction.commit();
    }

    private static final int ACTIVITY_NUM = 4;
    private static final int NUM_GRID_COLUMNS = 3;
    private Context mContext = ProfileActivity.this;
    private ProgressBar mProrgressBar;
    private ImageView profilePhoto;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Log.d(TAG, "onCreate: Started");

        init();

     /*   setUpBottomNavigationView();
        setUpToolbar();
        setupActivityWidgets();
        setProfileImage();
        tempGridSetup();*/
    }

    private void init() {
        Log.d(TAG, "init: inflating " + getString(R.string.profile_fragment));

        Intent intent = getIntent();
        if (intent.hasExtra(getString(R.string.calling_activity))) {
            Log.d(TAG, "init: Profile activity has been opened from Search activity");
            //Navigate to ViewProfileFragment with attached user in the bundle
            if (intent.hasExtra(getString(R.string.intent_user))) {
                User user = intent.getParcelableExtra(getString(R.string.intent_user));
                if(!user.getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                    ViewProfileFragment fragment = new ViewProfileFragment();
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(getString(R.string.intent_user),
                            intent.getParcelableExtra(getString(R.string.intent_user)));
                    fragment.setArguments(bundle);

                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.container, fragment);
                    transaction.addToBackStack(getString(R.string.view_profile_fragment));
                    transaction.commit();
                }
                else {
                    //The logged user is opening his profile
                    ProfileFragment fragment = new ProfileFragment();
                    android.support.v4.app.FragmentTransaction transaction = ProfileActivity.this.getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.container, fragment);

                    transaction.addToBackStack(getString(R.string.profile_fragment));
                    transaction.commit();
                }
            } else {
                Toast.makeText(mContext, "something went wrong", Toast.LENGTH_SHORT).show();
            }
        } else {
            //The logged user is opening his profile
            ProfileFragment fragment = new ProfileFragment();
            android.support.v4.app.FragmentTransaction transaction = ProfileActivity.this.getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container, fragment);

            transaction.addToBackStack(getString(R.string.profile_fragment));
            transaction.commit();
        }
    }
}
