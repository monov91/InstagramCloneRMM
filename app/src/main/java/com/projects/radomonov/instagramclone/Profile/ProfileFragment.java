package com.projects.radomonov.instagramclone.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.projects.radomonov.instagramclone.Login.LoginActivity;
import com.projects.radomonov.instagramclone.Models.Comment;
import com.projects.radomonov.instagramclone.Models.Like;
import com.projects.radomonov.instagramclone.Models.Photo;
import com.projects.radomonov.instagramclone.Models.User;
import com.projects.radomonov.instagramclone.Models.UserAccountSettings;
import com.projects.radomonov.instagramclone.Models.UserSettings;
import com.projects.radomonov.instagramclone.R;
import com.projects.radomonov.instagramclone.Utils.BottomNavigatinoViewHelper;
import com.projects.radomonov.instagramclone.Utils.FirebaseHelper;
import com.projects.radomonov.instagramclone.Utils.GridImageAdapter;
import com.projects.radomonov.instagramclone.Utils.UniversalImageLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";
    public interface OnClickImageSelectedListener{
        void onGridImageSelected(Photo photo,int activityNumber);
    }

    OnClickImageSelectedListener mOnClickImageSelectedListener;

    private static final int ACTIVITY_NUM = 4;
    private static final int GRID_COLUMNS = 3;
    //Vars
    private Context mContext;
    private String mCurrentUserUid;
    private int mFollowersCount ;
    private int mPostsCount ;
    private int mFollowingCount ;

    //Widgets
    private TextView mPosts, mFollowers, mFollowing, mDisplayName, mUsername, mWebsite, mDescription, editProfile;
    private ProgressBar mProgressbar;
    private CircleImageView mProfilePhoto;
    private GridView gridView;
    private Toolbar toolbar;
    private ImageView profileMenu;
    private BottomNavigationViewEx bottomNavigationViewEx;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseHelper mFirebaseHelper;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        Log.d(TAG, "onCreateView: starting");

        mContext = getActivity();
        setupFirebaseAuth();
        setupGridView();

        mCurrentUserUid = mAuth.getCurrentUser().getUid();

        mDisplayName = view.findViewById(R.id.display_name);
        mUsername = view.findViewById(R.id.username);
        mWebsite = view.findViewById(R.id.website);
        mDescription = view.findViewById(R.id.description);
        mProfilePhoto = view.findViewById(R.id.profile_photo);
        mPosts = view.findViewById(R.id.tvPosts);
        mFollowers = view.findViewById(R.id.tvFollowers);
        mFollowing = view.findViewById(R.id.tvFollowing);
        mProgressbar = view.findViewById(R.id.profileProgressBar);
        gridView = view.findViewById(R.id.gridView);
        toolbar = view.findViewById(R.id.profileToolbar);
        profileMenu = view.findViewById(R.id.profileMenu);
        bottomNavigationViewEx = view.findViewById(R.id.bottomNavViewBar);
        mFirebaseHelper = new FirebaseHelper(mContext);

        setUpBottomNavigationView();
        setUpToolbar();

        editProfile = view.findViewById(R.id.textEditProfile);
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Navigating to " + mContext.getString(R.string.edit_profile_fragment));

                Intent intent = new Intent(mContext, AccountSettingsActivity.class);
                intent.putExtra(getString(R.string.calling_activity), getString(R.string.profile_activity));
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.fade_in,R.anim.fade_out);

            }
        });
        return view;
    }

    @Override
    public void onAttach(Context context) {
        try{
            mOnClickImageSelectedListener = (OnClickImageSelectedListener) getActivity();
        } catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException" + e.getMessage());
        }
        super.onAttach(context);
    }

    private void setupGridView() {
        Log.d(TAG, "setupGridView: Setting up image grid");
        final ArrayList<Photo> photos = new ArrayList<>();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        Query query = databaseReference
                .child(mContext.getString(R.string.dbname_user_photos))
                .child(mAuth.getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange:");
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    //photos.add(singleSnapshot.getValue(Photo.class));

                    //Fixing common Firebase issue where it sees a list of values in the Database as a Map
                    Photo photo = new Photo();
                    Map<String,Object> objectMap = (HashMap<String,Object>) singleSnapshot.getValue();
                    try {
                        photo.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
                        photo.setTags(objectMap.get(getString(R.string.field_tags)).toString());
                        photo.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
                        photo.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                        photo.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                        photo.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());

                        ArrayList<Comment> comments = new ArrayList<>();
                        for (DataSnapshot dataSnapshot1 : singleSnapshot
                                .child(getString(R.string.field_comments)).getChildren()) {
                            Comment readComment = dataSnapshot1.getValue(Comment.class);
                            Comment comment = new Comment();
                            comment.setUser_id(readComment.getUser_id());
                            comment.setDate_created(readComment.getDate_created());
                            comment.setComment(readComment.getComment());
                            //comment.setLikes(readComment.getLikes());
                            comments.add(comment);
                        }
                        photo.setComments(comments);

                        List<Like> likeList = new ArrayList<>();
                        for (DataSnapshot dataSnapshot1 : singleSnapshot
                                .child(getString(R.string.field_likes)).getChildren()) {
                            Like like = new Like();
                            like.setUser_id(dataSnapshot1.getValue(Like.class).getUser_id());
                            likeList.add(like);
                        }
                        photo.setLikes(likeList);
                        photos.add(photo);
                    } catch (NullPointerException e){
                        Log.d(TAG, "onDataChange: NullPointerException " + e.getMessage());
                    }
                }
                //Setup our image grid
                int gridWidth = getResources().getDisplayMetrics().widthPixels;
                int imageWidth = gridWidth / GRID_COLUMNS;
                gridView.setColumnWidth(imageWidth);
                ArrayList<String> imgUrls = new ArrayList<>();
                for (int i = 0; i < photos.size(); i++) {
                    imgUrls.add(photos.get(i).getImage_path());
                }
                GridImageAdapter gridImageAdapter = new GridImageAdapter(mContext,R.layout.layout_grid_imageview,"",imgUrls);
                gridView.setAdapter(gridImageAdapter);

                gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                        mOnClickImageSelectedListener.onGridImageSelected(photos.get(position),ACTIVITY_NUM);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: ");
            }
        });
    }

    private void setProfileWidgets(UserSettings userSettings) {
        Log.d(TAG, "setProfileWidgets: Setting widgets with data retrieved from firebase DB");

        User user = userSettings.getUser();
        UserAccountSettings settings = userSettings.getSettings();

        UniversalImageLoader.setImage(settings.getProfile_photo(), mProfilePhoto, null, "");

        mDisplayName.setText(settings.getDisplay_name());
        mUsername.setText(settings.getUsername());
        mWebsite.setText(settings.getWebsite());
        mDescription.setText(settings.getDescription());
        setFollowersCount();
        setFollowingCount();
        setPostsCount();
        mProgressbar.setVisibility(View.GONE);
    }

    private void setUpBottomNavigationView() {
        BottomNavigatinoViewHelper.setUpBottomNavigationView(bottomNavigationViewEx);
        BottomNavigatinoViewHelper.enableNavigation(mContext, bottomNavigationViewEx,getActivity());
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem item = menu.getItem(ACTIVITY_NUM);
        item.setChecked(true);
    }

    private void setUpToolbar() {

        ((ProfileActivity) getActivity()).setSupportActionBar(toolbar);
        profileMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: navigating to account settings");
                Intent intent = new Intent(mContext, AccountSettingsActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.fade_in,R.anim.fade_out);

            }
        });
    }

    private void setFollowersCount(){
        mFollowersCount = 0;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_followers))
                .child(mCurrentUserUid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot singlesnapshot : dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found follower " + singlesnapshot.getValue());
                    mFollowersCount++;
                }
                mFollowers.setText(String.valueOf(mFollowersCount));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }
    private void setFollowingCount(){
        mFollowingCount = 0;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_following))
                .child(mCurrentUserUid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot singlesnapshot : dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found followee " + singlesnapshot.getValue());
                    mFollowingCount++;
                }
                mFollowing.setText(String.valueOf(mFollowingCount));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void setPostsCount(){
        mPostsCount = 0;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_user_photos))
                .child(mCurrentUserUid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot singlesnapshot : dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found post " + singlesnapshot.getValue());
                    mPostsCount++;
                }
                mPosts.setText(String.valueOf(mPostsCount));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
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
        if (mAuthStateListener != null) {
            mAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    /*
        Setup the Firebase auth object
         */
    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: setting up Firebase auth");

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged: signed_id: " + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged: signed_out ");
                }
            }
        };

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // Retrieve user infofrmation from the database
                // and update UI
                setProfileWidgets(mFirebaseHelper.getUserSettings(dataSnapshot));

                // Retrieve images for the user

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


}
