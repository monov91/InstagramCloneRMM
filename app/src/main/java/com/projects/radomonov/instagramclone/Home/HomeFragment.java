package com.projects.radomonov.instagramclone.Home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.projects.radomonov.instagramclone.Models.Comment;
import com.projects.radomonov.instagramclone.Models.Like;
import com.projects.radomonov.instagramclone.Models.Photo;
import com.projects.radomonov.instagramclone.Models.UserAccountSettings;
import com.projects.radomonov.instagramclone.R;
import com.projects.radomonov.instagramclone.Utils.MainfeedListAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    //Vars
    private ArrayList<Photo> mPhotos;
    private ArrayList<String> mFollowing;
    private ArrayList<Photo> mPaginatedPhotos;
    private ListView mListView;
    private MainfeedListAdapter mAdapter;
    private int mResults;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mListView = view.findViewById(R.id.listView);
        mFollowing = new ArrayList<>();
        mPhotos = new ArrayList<>();

        getFollowing();
        return view;
    }

    private void getFollowing() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            Query query = reference
                    .child(getString(R.string.dbname_following))
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                        Log.d(TAG, "onDataChange: found user " +
                                singleSnapshot.child(getString(R.string.field_user_id)).getValue());
                        mFollowing.add(singleSnapshot.child(getString(R.string.field_user_id)).getValue().toString());
                    }
                    //Also add logged user to the list so that he sees his posts in the main feed
                    mFollowing.add(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    getPhotos();
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }

    private void getPhotos() {
        Log.d(TAG, "getPhotos: getting photos");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        for (int i = 0; i < mFollowing.size(); i++) {
            final int count = i;
            Query query = reference
                    .child(getString(R.string.dbname_user_photos))
                    .child(mFollowing.get(i))
                    .orderByChild(getString(R.string.field_user_id))
                    .equalTo(mFollowing.get(i));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                        Log.d(TAG, "onDataChange: SNAPSHOT " + singleSnapshot.getValue().toString());
                        //Fixing common Firebase issue where it sees a list of values in the Database as a Map
                        Photo photo = new Photo();
                        try {
                            Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();
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
                            mPhotos.add(photo);
                        } catch (NullPointerException e) {
                            Log.d(TAG, "onDataChange: NullPointerException " + e.getMessage());
                        }
                    }
                    if (count >= mFollowing.size() - 1) {
                        // display photos
                        displayPhotos();
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }

    // TYPO
    private void displayPhotos() {
        mPaginatedPhotos = new ArrayList<>();
        if (mPhotos != null) {
            try {
                Collections.sort(mPhotos, new Comparator<Photo>() {
                    @Override
                    public int compare(Photo p1, Photo p2) {
                        return p2.getDate_created().compareTo(p1.getDate_created());
                    }
                });
                int iterations  = mPhotos.size();
                if(iterations > 10){
                    iterations = 10;
                }
                mResults = iterations;
                for(int i = 0; i < iterations; i++){
                    mPaginatedPhotos.add(mPhotos.get(i));
                }
                mAdapter = new MainfeedListAdapter(getActivity(), R.layout.layout_mainfeed_listitem, mPaginatedPhotos);
                mListView.setAdapter(mAdapter);
            } catch (NullPointerException e) {
                Log.d(TAG, "displayPhotos: NullPointerException " + e.getMessage());
            } catch (IndexOutOfBoundsException e) {
                Log.d(TAG, "displayPhotos: IndexOutOfBoundsException " + e.getMessage());
            }
        }
    }

    public void displayMorePhotos(){
        Log.d(TAG, "displayMorePhotos: displaying more photos ");
        try{
            if(mPhotos.size() > mResults && mPhotos.size() > 0){
                int iterations;
                if(mPhotos.size() > mResults + 10){
                    Log.d(TAG, "displayMorePhotos: there are MORE than 10 other photos");
                    iterations = 10;
                } else {
                    Log.d(TAG, "displayMorePhotos: there are LESS than 10 other photos");
                    iterations = mPhotos.size() - mResults;
                }
                //add the photos to the paginated results
                for(int i = mResults; i <mResults + iterations; i++){
                    mPaginatedPhotos.add(mPhotos.get(i));
                }
                mResults += iterations;
                mAdapter.notifyDataSetChanged();
            }
        } catch (NullPointerException e) {
            Log.d(TAG, "displayPhotos: NullPointerException " + e.getMessage());
        } catch (IndexOutOfBoundsException e) {
            Log.d(TAG, "displayPhotos: IndexOutOfBoundsException " + e.getMessage());
        }
    }

}
