package com.projects.radomonov.instagramclone.Utils;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.projects.radomonov.instagramclone.Home.MainActivity;
import com.projects.radomonov.instagramclone.Models.Comment;
import com.projects.radomonov.instagramclone.Models.Like;
import com.projects.radomonov.instagramclone.Models.Photo;
import com.projects.radomonov.instagramclone.Models.User;
import com.projects.radomonov.instagramclone.Models.UserAccountSettings;
import com.projects.radomonov.instagramclone.R;

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

public class ViewCommentsFragment extends android.support.v4.app.Fragment {
    private static final String TAG = "ViewCommentsFragment";

    //Prevent a nullpointer exception
    public ViewCommentsFragment() {
        super.setArguments(new Bundle());
    }

    //vars
    private Photo mPhoto;
    private ArrayList<Comment> mComments;
    private Context mContext;

    //widgets
    private ImageView mBackArrow;
    private ImageView mCheckMark;
    private EditText mComment;
    private ListView mListView;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_comments, container, false);
        mContext = getActivity();


        mBackArrow = view.findViewById(R.id.backArrow);
        mCheckMark = view.findViewById(R.id.ivPostComment);
        mComment = view.findViewById(R.id.comment);
        mListView = view.findViewById(R.id.listView);
        mComments = new ArrayList<>();

        //setupFirebaseAuth();

        try {
            mPhoto = getPhotoFromBundle();
        } catch (NullPointerException e) {
            Log.e(TAG, "onCreateView: NullPointerException: " + e.getMessage());
        }
        //trqa da e gore
        setupFirebaseAuth();
        return view;
    }

    private void setupWidgets() {

        CommentListAdapter adapter = new CommentListAdapter(mContext, R.layout.layout_comment, mComments);
        mListView.setAdapter(adapter);

        mCheckMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mComment.getText().toString().trim().equals("")) {
                    Log.d(TAG, "onClick: attempting to submit a comment");
                    addNewComment(mComment.getText().toString().trim());
                    mComment.setText("");
                    closeKeyboard();
                } else {
                    Toast.makeText(mContext, "Enter a comment", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Navigating back");
                if(getCallingActivity() != null ){
                    if(getCallingActivity().equals(mContext.getString(R.string.main_activity))){
                        getActivity().getSupportFragmentManager().popBackStack();
                        ((MainActivity)getActivity()).showLayout();
                    }
                }
                else {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });
    }

    private void closeKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void addNewComment(String commentText) {
        String commentID = myRef.push().getKey();
        Comment comment = new Comment();
        comment.setComment(commentText);
        comment.setDate_created(getTimeStamp());
        comment.setUser_id(mAuth.getCurrentUser().getUid());

        //Add to photos node in DB
        myRef.child(mContext.getString(R.string.dbname_photos))
                .child(mPhoto.getPhoto_id())
                .child(mContext.getString(R.string.field_comments))
                .child(commentID)
                .setValue(comment);
        //Add to user_photos node in DB
        myRef.child(mContext.getString(R.string.dbname_user_photos))
                .child(mAuth.getCurrentUser().getUid())
                .child(mPhoto.getPhoto_id())
                .child(mContext.getString(R.string.field_comments))
                .child(commentID)
                .setValue(comment);

    }

    private String getTimeStamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.UK);
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Athens"));
        return sdf.format(new Date());
    }

    private Photo getPhotoFromBundle() {
        Log.d(TAG, "getPhotoFromBundle: arguments : " + getArguments());
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            return bundle.getParcelable(mContext.getString(R.string.photo));
        } else {
            return null;
        }
    }
    private String getCallingActivity() {
        Log.d(TAG, "getCallingActivity: arguments : " + getArguments());
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            return bundle.getString(mContext.getString(R.string.main_activity));
        } else {
            return null;
        }
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
        // If the photo has no comments the childEventListener won't fire up,
        // so we have to add the caption and setup widgets here
        if(mPhoto.getComments().size() == 0){
            mComments.clear();

            //Add the photo info as the first comment
            Comment firstComment = new Comment();
            firstComment.setComment(mPhoto.getCaption());
            firstComment.setDate_created(mPhoto.getDate_created());
            firstComment.setUser_id(mPhoto.getUser_id());
            mComments.add(firstComment);
            setupWidgets();
        }


        myRef.child(mContext.getString(R.string.dbname_photos))
                .child(mPhoto.getPhoto_id())
                .child(mContext.getString(R.string.field_comments))
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        Query query = myRef
                                .child(mContext.getString(R.string.dbname_photos))
                                .orderByChild(mContext.getString(R.string.field_photo_id))
                                .equalTo(mPhoto.getPhoto_id());
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Log.d(TAG, "onDataChange:");
                                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                                    //photos.add(singleSnapshot.getValue(Photo.class));

                                    //Fixing common Firebase issue where it sees a list of values in the Database as a Map
                                    Photo photo = new Photo();
                                    Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();
                                    photo.setCaption(objectMap.get(mContext.getString(R.string.field_caption)).toString());
                                    photo.setTags(objectMap.get(mContext.getString(R.string.field_tags)).toString());
                                    photo.setPhoto_id(objectMap.get(mContext.getString(R.string.field_photo_id)).toString());
                                    photo.setUser_id(objectMap.get(mContext.getString(R.string.field_user_id)).toString());
                                    photo.setDate_created(objectMap.get(mContext.getString(R.string.field_date_created)).toString());
                                    photo.setImage_path(objectMap.get(mContext.getString(R.string.field_image_path)).toString());

                                    mComments.clear();

                                    //Add the photo info as the first comment
                                    Comment firstComment = new Comment();
                                    firstComment.setComment(mPhoto.getCaption());
                                    firstComment.setDate_created(mPhoto.getDate_created());
                                    firstComment.setUser_id(mPhoto.getUser_id());
                                    mComments.add(firstComment);

                                    for (DataSnapshot dataSnapshot1 : singleSnapshot
                                            .child(mContext.getString(R.string.field_comments)).getChildren()) {
                                        Comment readComment = dataSnapshot1.getValue(Comment.class);
                                        Comment comment = new Comment();
                                        comment.setUser_id(readComment.getUser_id());
                                        comment.setDate_created(readComment.getDate_created());
                                        comment.setComment(readComment.getComment());
                                        //comment.setLikes(readComment.getLikes());
                                        mComments.add(comment);
                                    }
                                    photo.setComments(mComments);

                                    mPhoto = photo;

                                    setupWidgets();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.d(TAG, "onCancelled: ");
                            }
                        });
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}
