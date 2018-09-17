package com.projects.radomonov.instagramclone.Share;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.projects.radomonov.instagramclone.R;
import com.projects.radomonov.instagramclone.Utils.BottomNavigatinoViewHelper;
import com.projects.radomonov.instagramclone.Utils.Permissions;
import com.projects.radomonov.instagramclone.Utils.SectionsPagerAdapter;

public class ShareActivity extends AppCompatActivity {
    private static final String TAG = "ShareActivity";

    //  Constants
    private static final int ACTIVITY_NUM = 2;
    private static final int VERIFY_PERMISSIONS_REQUEST = 1;

    private ViewPager mViewPager;

    private Context mContext = ShareActivity.this;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        Log.d(TAG, "onCreate: Started");

        //Check if we have permissions
        if(!checkPermissionsArray(Permissions.PERMISSIONS)){
            // If not - ask for permissions
            verifyPermissions(Permissions.PERMISSIONS);
            // If permissions still are not granted , close the activity
            if(!checkPermissionsArray(Permissions.PERMISSIONS)){
                finish();
            }
        }
        setupViewPager();


        //setUpBottomNavigationView();
    }

    public int getTask(){
        Log.d(TAG, "getTask: TASK : " + getIntent().getFlags());
        return getIntent().getFlags();
    }

    /**
     * Setup viewPager for managing the tabs
     */
    private void setupViewPager(){
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new GalleryFragment());
        adapter.addFragment(new PhotoFragment());

        mViewPager = findViewById(R.id.viewpager_container);
        mViewPager.setAdapter(adapter);
        TabLayout tabLayout = findViewById(R.id.tabsBottom);
        tabLayout.setupWithViewPager(mViewPager);

        tabLayout.getTabAt(0).setText(getString(R.string.gallery));
        tabLayout.getTabAt(1).setText(getString(R.string.photo));
    }


    /**
     * return the current tab number
     * 0 = GalleryFragment
     * 1 = PhotoFragment
     * @return
     */
    public int getCurrentTabNumber(){
        return mViewPager.getCurrentItem();
    }

    /**
     * Verify all the permissions passed to the array
     * @param permissions
     */
    public void verifyPermissions(String[] permissions){
        Log.d(TAG, "verifyPermissions: verifying permissions");
        ActivityCompat.requestPermissions(
                ShareActivity.this,
                permissions,
                VERIFY_PERMISSIONS_REQUEST
        );

    }

    /**
     * Check an array of permissions if they are all granted
     * @param permissions
     * @return
     */
    public boolean checkPermissionsArray(String[] permissions){
        Log.d(TAG, "checkPermissionsArray: check permissions array");
        for (int i = 0; i < permissions.length; i++){
            String check = permissions[i];
            if(!checkSinglePermission(check)){
                return false;
            }
        }
        return true;
    }

    /**
     * Check if a permission is granted
     * @param permission
     * @return
     */
    public boolean checkSinglePermission(String permission){
        Log.d(TAG, "checkSinglePermission: checking permission " + permission);

        int permissionRequest = ActivityCompat.checkSelfPermission(ShareActivity.this,permission);

        if(permissionRequest != PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "checkSinglePermission: Permission was not granted for " + permission);
            return false;
        } else {
            Log.d(TAG, "checkSinglePermission: Permission is granted for " + permission);
            return true;
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
}
