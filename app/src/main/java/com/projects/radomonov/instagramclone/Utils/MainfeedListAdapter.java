package com.projects.radomonov.instagramclone.Utils;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.projects.radomonov.instagramclone.Home.MainActivity;
import com.projects.radomonov.instagramclone.Models.Comment;
import com.projects.radomonov.instagramclone.Models.Like;
import com.projects.radomonov.instagramclone.Models.Photo;
import com.projects.radomonov.instagramclone.Models.User;
import com.projects.radomonov.instagramclone.Models.UserAccountSettings;
import com.projects.radomonov.instagramclone.Profile.ProfileActivity;
import com.projects.radomonov.instagramclone.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainfeedListAdapter extends ArrayAdapter<Photo> {

    public interface OnLoadMoreItemsListener {
        void onLoadMoreItems();
    }
    OnLoadMoreItemsListener onLoadMoreItemsListener;

    private LayoutInflater mLayoutInflater;
    private int mLayoutResource;
    private Context mContext;
    private DatabaseReference mReference;
    private String mCurrentUsername;

    private static final String TAG = "MainfeedListAdapter";

    public MainfeedListAdapter(@NonNull Context context, int resource, @NonNull List<Photo> objects) {
        super(context, resource, objects);
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLayoutResource = resource;
        this.mContext = context;
        mReference = FirebaseDatabase.getInstance().getReference();
    }

    static class ViewHolder {
        CircleImageView mProfileImage;
        String likesString;
        TextView username, timeDelta, caption, likes, comments;
        SquareImageView image;
        ImageView heartRed, heartWhite, comment;

        UserAccountSettings settings = new UserAccountSettings();
        User user = new User();
        StringBuilder users;
        boolean likedByCurrentUser;
        Heart heart;
        GestureDetector detector;
        Photo photo;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(mLayoutResource, parent, false);
            holder = new ViewHolder();

            holder.username = convertView.findViewById(R.id.username);
            holder.image = convertView.findViewById(R.id.post_image);
            holder.heartRed = convertView.findViewById(R.id.img_heart_red);
            holder.heartWhite = convertView.findViewById(R.id.img_heart);
            holder.comment = convertView.findViewById(R.id.speech_bubble);
            holder.likes = convertView.findViewById(R.id.image_likes);
            holder.comments = convertView.findViewById(R.id.image_comments_link);
            holder.caption = convertView.findViewById(R.id.image_caption);
            holder.timeDelta = convertView.findViewById(R.id.image_time_posted);
            holder.mProfileImage = convertView.findViewById(R.id.profile_photo);
            holder.heart = new Heart(holder.heartWhite, holder.heartRed);
            holder.photo = getItem(position);
            holder.detector = new GestureDetector(mContext, new GestureListener(holder));

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        //get the current user's username (for checking in like's string)
        getCurrentUsername();
        //get the likes string
        getLikesString(holder);

        //set the caption
        holder.caption.setText(getItem(position).getCaption());

        //set the comment
        List<Comment> comments = getItem(position).getComments();
        holder.comments.setText("View all " + comments.size() + " comments");
        holder.comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: loading commen threado for " + getItem(position).getPhoto_id());
                ((MainActivity)mContext).onCommentThreadSelected(getItem(position),
                        mContext.getString(R.string.main_activity));

                //going to need to do something else
                ((MainActivity)mContext).hideLayout();
            }
        });
        // set the time it was posted
        String timeStampDifference = getTimestampDifference(getItem(position));
        if(!timeStampDifference.equals("0")){
            holder.timeDelta.setText(timeStampDifference + " DAYS AGO");
        } else {
            holder.timeDelta.setText("TODAY");
        }

        // set the post image
        final ImageLoader imageLoader  = ImageLoader.getInstance();
        imageLoader.displayImage(getItem(position).getImage_path(),holder.image);

        //get the post owner User object
        Query userQuery = mReference
                .child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild(getContext().getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());
        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: found user " + singleSnapshot.getValue(User.class).getUsername());
                    holder.user = singleSnapshot.getValue(User.class);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //set the post's owner username and profile photo
        Query query = mReference
                .child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild(getContext().getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    //mCurrentUsername = singleSnapshot.getValue(UserAccountSettings.class).getUsername();
                    Log.d(TAG, "onDataChange: found the post's owner " +singleSnapshot.getValue(UserAccountSettings.class).getUsername());
                    holder.username.setText(singleSnapshot.getValue(UserAccountSettings.class).getUsername());
                    holder.username.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Log.d(TAG, "onClick: Navigating to profile of the post's owner " +holder.user.getUsername());
                            Intent intent = new Intent(mContext, ProfileActivity.class);
                            intent.putExtra(mContext.getString(R.string.calling_activity),
                                    mContext.getString(R.string.main_activity));
                            intent.putExtra(mContext.getString(R.string.intent_user),holder.user);
                            mContext.startActivity(intent);
                        }
                    });
                    UniversalImageLoader.setImage(singleSnapshot.getValue(UserAccountSettings.class).getProfile_photo(),holder.mProfileImage,null,"");
                    holder.mProfileImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Log.d(TAG, "onClick: Navigating to profile of the post's owner " +holder.user.getUsername());
                            Intent intent = new Intent(mContext, ProfileActivity.class);
                            intent.putExtra(mContext.getString(R.string.calling_activity),
                                    mContext.getString(R.string.main_activity));
                            intent.putExtra(mContext.getString(R.string.intent_user),holder.user);
                            mContext.startActivity(intent);
                        }
                    });

                    holder.settings = singleSnapshot.getValue(UserAccountSettings.class);
                    holder.comment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ((MainActivity)mContext).onCommentThreadSelected(getItem(position)
                                    ,mContext.getString(R.string.main_activity));

                            //another thing ?
                            ((MainActivity)mContext).hideLayout();
                        }
                    });
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        if(reachedEndofTheList(position)){
            loadMoreData();
        }
        return convertView;
    }

    private boolean reachedEndofTheList(int position){
        return position == getCount() - 1;
    }

    private void loadMoreData(){
        try {
            onLoadMoreItemsListener = (OnLoadMoreItemsListener) mContext;
        } catch (ClassCastException e){
            Log.d(TAG, "loadMoreData: ClassCastException " + e.getMessage());
        }

        try {
            onLoadMoreItemsListener.onLoadMoreItems();
        } catch (NullPointerException e){
            Log.d(TAG, "loadMoreData: ClassCastException " + e.getMessage());
        }
    }

    private void getCurrentUsername(){
        Log.d(TAG, "getUsername: Retrieving user account settings");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild(getContext().getString(R.string.field_user_id))
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    mCurrentUsername = singleSnapshot.getValue(UserAccountSettings.class).getUsername();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public class GestureListener extends GestureDetector.SimpleOnGestureListener {
        ViewHolder mHolder;

        public GestureListener(ViewHolder holder) {
            mHolder = holder;
        }

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
                    .child(mContext.getString(R.string.dbname_photos))
                    .child(mHolder.photo.getPhoto_id())
                    .child(mContext.getString(R.string.field_likes));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot singleDatasnapshot : dataSnapshot.getChildren()) {
                        // case 1: The user already liked the photo
                        String keyID = singleDatasnapshot.getKey();
                        if (mHolder.likedByCurrentUser &&
                                singleDatasnapshot.getValue(Like.class).getUser_id()
                                        .equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            mReference.child(mContext.getString(R.string.dbname_photos))
                                    .child(mHolder.photo.getPhoto_id())
                                    .child(mContext.getString(R.string.field_likes))
                                    .child(keyID)
                                    .removeValue();
                            mReference.child(mContext.getString(R.string.dbname_user_photos))
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child(mHolder.photo.getPhoto_id())
                                    .child(mContext.getString(R.string.field_likes))
                                    .child(keyID)
                                    .removeValue();
                            mHolder.heart.toggleLike();
                            getLikesString(mHolder);
                        }
                        // case 2: The user hasn't liked the photo
                        else {
                            if (!mHolder.likedByCurrentUser) {
                                //Add new like
                                addNewLike(mHolder);
                                break;
                            }
                        }
                    }
                    if (!dataSnapshot.exists()) {
                        //Add new like
                        addNewLike(mHolder);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            return true;
        }
    }

    private void addNewLike(final ViewHolder holder) {
        Log.d(TAG, "addNewLike: Adding new like");
        String newLikeID = mReference.push().getKey();
        Like like = new Like();
        like.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

        mReference.child(mContext.getString(R.string.dbname_photos))
                .child(holder.photo.getPhoto_id())
                .child(mContext.getString(R.string.field_likes))
                .child(newLikeID)
                .setValue(like);
        mReference.child(mContext.getString(R.string.dbname_user_photos))
                .child(holder.photo.getUser_id())
                .child(holder.photo.getPhoto_id())
                .child(mContext.getString(R.string.field_likes))
                .child(newLikeID)
                .setValue(like);
        holder.heart.toggleLike();
        getLikesString(holder);
    }

    private void getLikesString(final ViewHolder holder) {
        Log.d(TAG, "getLikesString: getting likes string");

        try {
            holder.likesString = "";
            holder.likedByCurrentUser = false;
            //setup likes string

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference
                    .child(mContext.getString(R.string.dbname_photos))
                    .child(holder.photo.getPhoto_id())
                    .child(mContext.getString(R.string.field_likes));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    holder.users = new StringBuilder();
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                        Query query = reference
                                .child(mContext.getString(R.string.dbname_users))
                                .orderByChild(mContext.getString(R.string.field_user_id))
                                .equalTo(singleSnapshot.getValue(Like.class).getUser_id());
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                                    Log.d(TAG, "onDataChange: found like: " +
                                            singleSnapshot.getValue(User.class).getUsername());

                                    holder.users.append(singleSnapshot.getValue(User.class).getUsername());
                                    holder.users.append(",");
                                }

                                String[] splitUsers = holder.users.toString().split(",");

                                if (holder.users.toString().contains(mCurrentUsername + ",")) {
                                    holder.likedByCurrentUser = true;
                                } else {
                                    holder.likedByCurrentUser = false;
                                }

                                int length = splitUsers.length;
                                if (length == 1) {
                                    holder.likesString = "Liked by " + splitUsers[0];
                                } else if (length == 2) {
                                    holder.likesString = "Liked by " + splitUsers[0]
                                            + " and " + splitUsers[1];
                                } else if (length == 3) {
                                    holder.likesString = "Liked by " + splitUsers[0]
                                            + ", " + splitUsers[1]
                                            + " and " + splitUsers[2];
                                } else if (length == 4) {
                                    holder.likesString = "Liked by " + splitUsers[0]
                                            + ", " + splitUsers[1]
                                            + ", " + splitUsers[2]
                                            + " and " + splitUsers[3];
                                } else if (length > 4) {
                                    holder.likesString = "Liked by " + splitUsers[0]
                                            + ", " + splitUsers[1]
                                            + ", " + splitUsers[2]
                                            + " and " + (splitUsers.length - 3) + " others";
                                }
                                //setup likes string
                                setupLikesString(holder,holder.likesString);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                    if (!dataSnapshot.exists()) {
                        holder.likesString = "";
                        holder.likedByCurrentUser = false;
                        //setup likes string
                        setupLikesString(holder,holder.likesString);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } catch (NullPointerException ex) {
            Log.d(TAG, "getLikesString: NullPointerException: " + ex.getMessage());
            holder.likesString = "";
            holder.likedByCurrentUser = false;
            //setup likes string
            setupLikesString(holder,holder.likesString);
        }
    }

    private void setupLikesString(final ViewHolder holder,String likesString){
        Log.d(TAG, "setupLikesString: setting up the likes string" + holder.likesString);
        if(holder.likedByCurrentUser){
            Log.d(TAG, "setupLikesString: Photo is like by current user");
            holder.heartWhite.setVisibility(View.GONE);
            holder.heartRed.setVisibility(View.VISIBLE);
            holder.heartRed.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return  holder.detector.onTouchEvent(motionEvent);
                }
            });
        } else {
            Log.d(TAG, "setupLikesString: Photo is NOT like by current user");
            holder.heartWhite.setVisibility(View.VISIBLE);
            holder.heartRed.setVisibility(View.GONE);
            holder.heartWhite.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return  holder.detector.onTouchEvent(motionEvent);
                }
            });
        }
        holder.likes.setText(likesString);
    }

    private String getTimestampDifference(Photo photo){
        String difference = null;
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.UK);
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Athens"));
        Date today = c.getTime();
        sdf.format(today);
        Date timeStamp;
        final String photoTimestamp = photo.getDate_created();
        Log.d(TAG, "getTimestampDifference: date in photo : " + photo.getDate_created());
        try{
            timeStamp = sdf.parse(photoTimestamp);
            difference = String.valueOf(Math.round(today.getTime() - timeStamp.getTime()) / 1000 / 60 / 60 / 24);
        } catch(ParseException e){
            Log.d(TAG, "getTimestampDifference: Parseexception " + e.getMessage());
            difference = "0";
        }
        return difference;
    }
}
