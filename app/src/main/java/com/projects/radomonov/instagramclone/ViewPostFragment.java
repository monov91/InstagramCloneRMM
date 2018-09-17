package com.projects.radomonov.instagramclone;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
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
import com.projects.radomonov.instagramclone.Models.Comment;
import com.projects.radomonov.instagramclone.Models.Like;
import com.projects.radomonov.instagramclone.Models.Photo;
import com.projects.radomonov.instagramclone.Models.User;
import com.projects.radomonov.instagramclone.Models.UserAccountSettings;
import com.projects.radomonov.instagramclone.Utils.BottomNavigatinoViewHelper;
import com.projects.radomonov.instagramclone.Utils.FirebaseHelper;
import com.projects.radomonov.instagramclone.Utils.GridImageAdapter;
import com.projects.radomonov.instagramclone.Utils.Heart;
import com.projects.radomonov.instagramclone.Utils.SquareImageView;
import com.projects.radomonov.instagramclone.Utils.UniversalImageLoader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class ViewPostFragment extends android.support.v4.app.Fragment {
    private static final String TAG = "ViewPostFragment";

    public interface OnCommentThreadSelectedListener{
        void onCommentThreadSelected(Photo photo);
    }
    OnCommentThreadSelectedListener mOnCommentThreadSelectedListener;


    //Vars
    private Photo mPhoto;
    private int mActivityNumber;
    private String mPhotousername;
    private String mProfileUrl;
    private UserAccountSettings mUserAccountSettings;
    private GestureDetector mGestureDetector;
    private Heart mHeart;
    private Boolean mLikedByCurrentUser;
    private StringBuilder mUsers;
    private String mLikesString = "";
    private User mCurrentUser;

    //Widgets
    private SquareImageView image;
    private BottomNavigationViewEx bottomNavigationViewEx;
    private TextView backLabel,caption,username,timestamp,likes,comments;
    private ImageView backArrow,ellipses,heartRed,heartWhite,profileImage,comment;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseHelper mFirebaseHelper;


    public ViewPostFragment(){
        super();
        setArguments(new Bundle());
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_post,container,false);

        image = (SquareImageView) view.findViewById(R.id.post_image);
        bottomNavigationViewEx =(BottomNavigationViewEx) view.findViewById(R.id.bottomNavViewBar);
        backArrow = view.findViewById(R.id.backArrow);
        backLabel = view.findViewById(R.id.tvBackLabel);
        caption = view.findViewById(R.id.image_caption);
        username = view.findViewById(R.id.username);
        timestamp = view.findViewById(R.id.image_time_posted);
        ellipses = view.findViewById(R.id.ivEllipses);
        heartRed = view.findViewById(R.id.img_heart_red);
        heartWhite = view.findViewById(R.id.img_heart);
        profileImage = view.findViewById(R.id.profile_photo);
        likes = view.findViewById(R.id.image_likes);
        mGestureDetector = new GestureDetector(getActivity(),new GestureListener());
        mHeart = new Heart(heartWhite,heartRed);
        comment = view.findViewById(R.id.speech_bubble);
        comments = view.findViewById(R.id.image_comments_link);

        mHeart = new Heart(heartWhite, heartRed);
        mGestureDetector = new GestureDetector(getActivity(), new GestureListener());


        setupFirebaseAuth();
        setUpBottomNavigationView();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        //If the fragment is added (fixed bug when sometimes the fragment isn't attached
        if(isAdded()){
            init();
        }
    }

    private void init(){
        try{
//            mPhoto = getPhotoFromBundle();
            UniversalImageLoader.setImage(getPhotoFromBundle().getImage_path(), image, null, "");
            mActivityNumber = getActivityNumberFromBundle();
            //Instead of using photo from bundle , get the photo_id  and query the photo from DB
            // so that it has all of its info
            String photo_id = getPhotoFromBundle().getPhoto_id();

            Query query = FirebaseDatabase.getInstance().getReference()
                    .child(getString(R.string.dbname_photos))
                    .orderByChild(getString(R.string.field_photo_id))
                    .equalTo(photo_id);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onDataChange:");
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                        //photos.add(singleSnapshot.getValue(Photo.class));

                        //Fixing common Firebase issue where it sees a list of values in the Database as a Map
                        Photo photo = new Photo();
                        Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();
                        photo.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
                        photo.setTags(objectMap.get(getString(R.string.field_tags)).toString());
                        photo.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
                        photo.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                        photo.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                        photo.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());

                        List<Comment> commentList = new ArrayList<>();
                        for (DataSnapshot dataSnapshot1 : singleSnapshot
                                .child(getString(R.string.field_comments)).getChildren()) {
                            Comment readComment = dataSnapshot1.getValue(Comment.class);
                            Comment comment = new Comment();
                            comment.setUser_id(readComment.getUser_id());
                            comment.setDate_created(readComment.getDate_created());
                            comment.setComment(readComment.getComment());
                            //comment.setLikes(readComment.getLikes());
                            commentList.add(comment);
                        }
                        photo.setComments(commentList);

                        mPhoto = photo;

                        getCurrentUser();
                        getPhotoDetails();
                        //getLikesString();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d(TAG, "onCancelled: ");
                }
            });
            getPhotoDetails();
            getLikesString();

        }catch (NullPointerException e){
            Log.e(TAG, "onCreateView: NullPointerException: " + e.getMessage() );
        }

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            mOnCommentThreadSelectedListener = (OnCommentThreadSelectedListener) getActivity();
        } catch(ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException " +e.getMessage() );
        }
    }

    private void getPhotoDetails(){
        Log.d(TAG, "getPhotoDetails: retrieving photo details.");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_user_account_settings))
                .orderByChild(getString(R.string.field_user_id))
                .equalTo(mPhoto.getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for ( DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                    mUserAccountSettings = singleSnapshot.getValue(UserAccountSettings.class);
                }
                //setupWidgets();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled.");
            }
        });
    }

    private void setupWidgets(){
        String timestampDiff = getTimestampDifference();
        if(!timestampDiff.equals("0")){
            timestamp.setText(timestampDiff + " DAYS AGO");
        }else{
            timestamp.setText("TODAY");
        }
        UniversalImageLoader.setImage(mUserAccountSettings.getProfile_photo(), profileImage, null, "");
        username.setText(mUserAccountSettings.getUsername());
        likes.setText(mLikesString);
        caption.setText(mPhoto.getCaption());

        if(mPhoto.getComments().size() > 0){
            comments.setText("View all " + mPhoto.getComments().size() + " comments");
        } else {
            comments.setVisibility(View.INVISIBLE);
        }

        comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnCommentThreadSelectedListener.onCommentThreadSelected(mPhoto);
            }
        });

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: navigate back");
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: navigate back");
                mOnCommentThreadSelectedListener.onCommentThreadSelected(mPhoto);

            }
        });

        if(mLikedByCurrentUser){
            heartWhite.setVisibility(View.GONE);
            heartRed.setVisibility(View.VISIBLE);
            heartRed.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Log.d(TAG, "onTouch: red heart touch detected.");
                    return mGestureDetector.onTouchEvent(event);
                }
            });
        }
        else{
            heartWhite.setVisibility(View.VISIBLE);
            heartRed.setVisibility(View.GONE);
            heartWhite.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Log.d(TAG, "onTouch: white heart touch detected.");
                    return mGestureDetector.onTouchEvent(event);
                }
            });
        }


    }
    /**
     * Returns a string representing number of days ago the post was made
     * @return
     */

    private void testToggle(){

    }

    private void getLikesString(){
        Log.d(TAG, "getLikesString: getting likes string");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_photos))
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_likes));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mUsers = new StringBuilder();
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                    Query query = reference
                            .child(getString(R.string.dbname_users))
                            .orderByChild(getString(R.string.field_user_id))
                            .equalTo(singleSnapshot.getValue(Like.class).getUser_id());
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                                Log.d(TAG, "onDataChange: found like: " +
                                        singleSnapshot.getValue(User.class).getUsername());

                                mUsers.append(singleSnapshot.getValue(User.class).getUsername());
                                mUsers.append(",");
                            }

                            String[] splitUsers = mUsers.toString().split(",");

                            if(mUsers.toString().contains(mCurrentUser.getUsername() + ",")){
                                mLikedByCurrentUser = true;
                            }else{
                                mLikedByCurrentUser = false;
                            }

                            int length = splitUsers.length;
                            if(length == 1){
                                mLikesString = "Liked by " + splitUsers[0];
                            }
                            else if(length == 2){
                                mLikesString = "Liked by " + splitUsers[0]
                                        + " and " + splitUsers[1];
                            }
                            else if(length == 3){
                                mLikesString = "Liked by " + splitUsers[0]
                                        + ", " + splitUsers[1]
                                        + " and " + splitUsers[2];

                            }
                            else if(length == 4){
                                mLikesString = "Liked by " + splitUsers[0]
                                        + ", " + splitUsers[1]
                                        + ", " + splitUsers[2]
                                        + " and " + splitUsers[3];
                            }
                            else if(length > 4){
                                mLikesString = "Liked by " + splitUsers[0]
                                        + ", " + splitUsers[1]
                                        + ", " + splitUsers[2]
                                        + " and " + (splitUsers.length - 3) + " others";
                            }
                            setupWidgets();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                if(!dataSnapshot.exists()){
                    mLikesString = "";
                    mLikedByCurrentUser = false;
                    setupWidgets();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void getCurrentUser(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_users))
                .orderByChild(getString(R.string.field_user_id))
                .equalTo(mAuth.getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for ( DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                    mCurrentUser = singleSnapshot.getValue(User.class);
                }
                getLikesString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled.");
            }
        });
    }


    public class GestureListener extends  GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onDown(MotionEvent e) {
            Log.d(TAG, "onDown: detected");
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.d(TAG, "onDoubleTap: detected");
            //mHeart.toggleLike();
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
            Query query = databaseReference
                    .child(getString(R.string.dbname_photos))
                    .child(mPhoto.getPhoto_id())
                    .child(getString(R.string.field_likes));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot singleDatasnapshot : dataSnapshot.getChildren()){
                        // case 1: The user already liked the photo
                        String keyID = singleDatasnapshot.getKey();
                        if(mLikedByCurrentUser &&
                                singleDatasnapshot.getValue(Like.class).getUser_id()
                                .equals(mAuth.getCurrentUser().getUid())){
                            myRef.child(getString(R.string.dbname_photos))
                                    .child(mPhoto.getPhoto_id())
                                    .child(getString(R.string.field_likes))
                                    .child(keyID)
                                    .removeValue();
                            myRef.child(getString(R.string.dbname_user_photos))
                                    .child(mAuth.getCurrentUser().getUid())
                                    .child(mPhoto.getPhoto_id())
                                    .child(getString(R.string.field_likes))
                                    .child(keyID)
                                    .removeValue();
                            mHeart.toggleLike();
                            getLikesString();
                        }
                        // case 2: The user hasn't liked the photo
                        else {
                            if(!mLikedByCurrentUser){
                                //Add new like
                                addNewLike();
                                break;
                            }
                        }
                    }
                    if(!dataSnapshot.exists()){
                        //Add new like
                        addNewLike();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            return true;
        }
    }

    private void addNewLike(){
        Log.d(TAG, "addNewLike: Adding new like");
        String newLikeID = myRef.push().getKey();
        Like like = new Like();
        like.setUser_id(mAuth.getCurrentUser().getUid());

        myRef.child(getString(R.string.dbname_photos))
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_likes))
                .child(newLikeID)
                .setValue(like);
        myRef.child(getString(R.string.dbname_user_photos))
                .child(mPhoto.getUser_id())
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_likes))
                .child(newLikeID)
                .setValue(like);
        mHeart.toggleLike();
        getLikesString();
    }

    private String getTimestampDifference(){
        String difference = null;
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.UK);
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Athens"));
        Date today = c.getTime();
        sdf.format(today);
        Date timeStamp;
        final String photoTimestamp = mPhoto.getDate_created();
        Log.d(TAG, "getTimestampDifference: date in photo : " + mPhoto.getDate_created());
        try{
            timeStamp = sdf.parse(photoTimestamp);
            difference = String.valueOf(Math.round(today.getTime() - timeStamp.getTime()) / 1000 / 60 / 60 / 24);
        } catch(ParseException e){
            Log.d(TAG, "getTimestampDifference: Parseexception " + e.getMessage());
            difference = "0";
        }
        return difference;
    }

    /**
     * Retrieve the activty number from the bundle from profileActivity interface
     * @return
     */
    private int getActivityNumberFromBundle(){
        Log.d(TAG, "getPhotoFromBundle: arguments : " + getArguments());
        Bundle bundle = this.getArguments();
        if(bundle != null){
            return bundle.getInt(getString(R.string.activity_number));
        } else {
            return 0;
        }
    }

    /**
     * Retrive the photo from the incoming bundle from profileActivity interface
     * @return
     */
    private Photo getPhotoFromBundle(){
        Log.d(TAG, "getPhotoFromBundle: arguments : " + getArguments());
        Bundle bundle = this.getArguments();
        if(bundle != null){
            return bundle.getParcelable(getString(R.string.photo));
        } else {
            return null;
        }
    }

    private void setUpBottomNavigationView() {
        BottomNavigatinoViewHelper.setUpBottomNavigationView(bottomNavigationViewEx);
        BottomNavigatinoViewHelper.enableNavigation(getActivity(), bottomNavigationViewEx,getActivity());
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem item = menu.getItem(mActivityNumber);
        item.setChecked(true);
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
    }
}
