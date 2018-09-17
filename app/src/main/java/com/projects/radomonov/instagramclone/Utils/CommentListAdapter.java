package com.projects.radomonov.instagramclone.Utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.projects.radomonov.instagramclone.Models.Comment;
import com.projects.radomonov.instagramclone.Models.UserAccountSettings;
import com.projects.radomonov.instagramclone.R;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentListAdapter extends ArrayAdapter<Comment> {

    private static final String TAG = "CommentListAdapter";

    private LayoutInflater mLayoutInflater;
    private int layoutResource;
    private Context mContext;

    public CommentListAdapter(@NonNull Context context, int resource, @NonNull List<Comment> objects) {
        super(context, resource, objects);
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
        layoutResource = resource;


    }

    private static class ViewHolder {
        TextView comment,username,timestamp,reply,likes;
        CircleImageView profileImage;
        ImageView like;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ViewHolder viewHolder;
        if(convertView == null){
            convertView = mLayoutInflater.inflate(layoutResource,parent,false);
            viewHolder = new ViewHolder();
            viewHolder.comment = convertView.findViewById(R.id.comment);
            viewHolder.username = convertView.findViewById(R.id.comment_username);
            viewHolder.timestamp = convertView.findViewById(R.id.comment_time_posted);
            viewHolder.reply = convertView.findViewById(R.id.comment_reply);
            viewHolder.like = convertView.findViewById(R.id.comment_like);
            viewHolder.profileImage = convertView.findViewById(R.id.comment_profile_image );
            viewHolder.likes = convertView.findViewById(R.id.comment_likes);

            //Store the widgets in memory
            convertView.setTag(viewHolder);
        } else {
            //Retrieve widgets
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Comment mComment = getItem(position);

        //Set the comment
        viewHolder.comment.setText(mComment.getComment());

        //Set the timestamp difference
        String timeStampDifference = getTimestampDifference(mComment);
        if(!timeStampDifference.equals("0")){
            viewHolder.timestamp.setText(timeStampDifference + "d");
        } else {
            viewHolder.timestamp.setText("today");
        }

        //set the username and profile image
            //Query the the user account info for the person that commented
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(mComment.getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for ( DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                    viewHolder.username.setText(
                            singleSnapshot.getValue(UserAccountSettings.class).getUsername());
//                    UniversalImageLoader universalImageLoader = new UniversalImageLoader(mContext);
//                    ImageLoader.getInstance().init(universalImageLoader.getConfig());
//                    universalImageLoader.setImage(singleSnapshot.getValue(UserAccountSettings.class).getProfile_photo(),viewHolder.profileImage,null,"");
                    ImageLoader imageLoader = ImageLoader.getInstance();
                    imageLoader.displayImage(singleSnapshot.getValue(UserAccountSettings.class).getProfile_photo(),viewHolder.profileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled.");
            }
        });
        //Hide some widgets for the first "comment" which is the photo info
        if(position == 0) {
            viewHolder.like.setVisibility(View.GONE);
            viewHolder.likes.setVisibility(View.GONE);
            viewHolder.reply.setVisibility(View.GONE);
        }

        return convertView;
    }

    private String getTimestampDifference(Comment comment){
        String difference = null;
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.UK);
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Athens"));
        Date today = c.getTime();
        sdf.format(today);
        Date timeStamp;
        final String photoTimestamp = comment.getDate_created();
        Log.d(TAG, "getTimestampDifference: date in photo : " + comment.getDate_created());
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
