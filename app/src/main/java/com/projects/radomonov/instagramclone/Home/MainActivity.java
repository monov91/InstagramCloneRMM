package com.projects.radomonov.instagramclone.Home;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.projects.radomonov.instagramclone.Login.LoginActivity;
import com.projects.radomonov.instagramclone.Models.Photo;
import com.projects.radomonov.instagramclone.Models.UserAccountSettings;
import com.projects.radomonov.instagramclone.R;
import com.projects.radomonov.instagramclone.Utils.BottomNavigatinoViewHelper;
import com.projects.radomonov.instagramclone.Utils.MainfeedListAdapter;
import com.projects.radomonov.instagramclone.Utils.SectionsPagerAdapter;
import com.projects.radomonov.instagramclone.Utils.UniversalImageLoader;
import com.projects.radomonov.instagramclone.Utils.ViewCommentsFragment;

public class MainActivity extends AppCompatActivity
        implements MainfeedListAdapter.OnLoadMoreItemsListener {

    @Override
    public void onLoadMoreItems() {
        Log.d(TAG, "onLoadMoreItems: displaying more photos");
        HomeFragment fragment = (HomeFragment)getSupportFragmentManager()
                //Because we don't have a tag set we get the default tag that android asigns to the fragment
                .findFragmentByTag("android:switcher:" + R.id.viewpager_container + ":" + mViewPager.getCurrentItem());
        if(fragment != null){
            fragment.displayMorePhotos();
        }
    }
    private static final String TAG = "HomeActivity";
    private static final int ACTIVITY_NUM = 0;
    private static final int HOME_FRAGMENT = 1;

    //Vars
    private Context mContext = MainActivity.this;

    //firebase authentication
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    //Widgets
    private ViewPager mViewPager;
    private FrameLayout mFrameLayout;
    private RelativeLayout mRelativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: Starting");

        mViewPager = findViewById(R.id.viewpager_container);
        mFrameLayout = findViewById(R.id.container);
        mRelativeLayout = findViewById(R.id.relLayoutParent);

        setupFirebaseAuth();
        initImageLoader();
        setUpBottomNavigationView();
        setUpViewPager();
    }

    public void onCommentThreadSelected(Photo photo,String callingActivity){
        Log.d(TAG, "onCommentThreadSelected: selected a comment thread");

        ViewCommentsFragment fragment = new ViewCommentsFragment();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.photo),photo);
        //Add MainActivity extra so view comments thread knows how to navigate back
        args.putString(mContext.getString(R.string.main_activity),mContext.getString(R.string.main_activity));
       // args.putParcelable(getString(R.string.bundle_photo),settings);
        fragment.setArguments(args);

        android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container,fragment);
        transaction.addToBackStack(getString(R.string.view_comments_fragment));
        transaction.commit();
    }

    /*
        Responsible for adding the 3 tabs
     */
    private void setUpViewPager(){
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new CameraFragment());//Index 0
        adapter.addFragment(new HomeFragment());//Index 1
        adapter.addFragment(new MessagesFragment());// Index 2

        mViewPager.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_camera);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_house);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_mess);
    }

    public void hideLayout(){
        Log.d(TAG, "hideLayout: hiding layout");
        mRelativeLayout.setVisibility(ViewPager.GONE);
        mFrameLayout.setVisibility(View.VISIBLE);
    }
    public void showLayout(){
        Log.d(TAG, "showLayout: showing layout");
        mRelativeLayout.setVisibility(ViewPager.VISIBLE);
        mFrameLayout.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //when Navigating away from the comments thread hide the frame layout
        // and display the main feed
        if(mFrameLayout.getVisibility() == View.VISIBLE){
            showLayout();
        }
    }

    private void setUpBottomNavigationView(){
        BottomNavigationViewEx bottomNavigationViewEx  = findViewById(R.id.bottomNavViewBar);
        BottomNavigatinoViewHelper.setUpBottomNavigationView(bottomNavigationViewEx);
        BottomNavigatinoViewHelper.enableNavigation(mContext,bottomNavigationViewEx,this);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem item = menu.getItem(ACTIVITY_NUM);
        item.setChecked(true);
    }

    private void initImageLoader(){
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(mContext);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }

    /*
    ----------------------------------------- Firebase ----------------------------------
     */

    private void checkCurrentUser(FirebaseUser user){
        Log.d(TAG, "checkCurrentUser: checking if user is logged in");
        if(user == null){
            Log.d(TAG, "checkCurrentUser: user is null");
            Intent intent = new Intent(mContext, LoginActivity.class);
            startActivity(intent);
        } else {
            mViewPager.setCurrentItem(HOME_FRAGMENT);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        //FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);

        mAuth.addAuthStateListener(mAuthStateListener);
        checkCurrentUser(mAuth.getCurrentUser());

    }

    @Override
    protected void onStop() {
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
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                //Check if user is signed in
                checkCurrentUser(user);

                if(user != null){
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged: signed_id: " + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged: signed_out " );
                }
            }
        };
    }
}
