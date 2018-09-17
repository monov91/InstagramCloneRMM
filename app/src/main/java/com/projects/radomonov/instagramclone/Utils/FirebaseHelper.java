package com.projects.radomonov.instagramclone.Utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.projects.radomonov.instagramclone.Home.MainActivity;
import com.projects.radomonov.instagramclone.Models.Photo;
import com.projects.radomonov.instagramclone.Models.User;
import com.projects.radomonov.instagramclone.Models.UserAccountSettings;
import com.projects.radomonov.instagramclone.Models.UserSettings;
import com.projects.radomonov.instagramclone.Profile.AccountSettingsActivity;
import com.projects.radomonov.instagramclone.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class FirebaseHelper {
    private static final String TAG = "FirebaseHelper";

    //firebase authentication
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private StorageReference mStorageReference;
    private String userID;

    //vars
    private Context mContext;
    private double mPhotoUploadProgress = 0;


    public FirebaseHelper(Context context) {
        mAuth = FirebaseAuth.getInstance();
        mContext = context;
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        if (FirebaseAuth.getInstance() != null) {
            userID = FirebaseAuth.getInstance().getUid();
        }
        mStorageReference = FirebaseStorage.getInstance().getReference();
    }

    public void uploadNewPhoto(final String photoType, final String caption, int count, String imgURL,Bitmap bm) {
        Log.d(TAG, "uploadNewPhoto: attempting to upload a new photo");

        FilePaths filePaths = new FilePaths();


            Log.d(TAG, "uploadNewPhoto: new photo");

            //Specify the location and name for the photo (name is "photo" + count + 1)
            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            final StorageReference storageReference;
            //checking wheter it is profile photo or not
            if(photoType.equals(mContext.getString(R.string.new_photo))){
                storageReference= mStorageReference
                        .child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/photo" + (count + 1));
            } else {
                storageReference= mStorageReference
                        .child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/profile_photo");
            }

            //Convert image URL to bitmap
            if(bm == null){
                bm = ImageManager.getBitmap(imgURL);
            }
            byte[] bytes = ImageManager.getBytesFromBitmap(bm, 100);

            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(bytes);
            uploadTask
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            if (progress - 30 > mPhotoUploadProgress) {
                                Toast.makeText(mContext, "photo upload progress" + String.format("%.0f", progress), Toast.LENGTH_SHORT).show();
                                mPhotoUploadProgress = progress;
                            }
                            Log.d(TAG, "onProgress: upload progress " + progress + " % done");
                        }
                    })
                    .continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }
                            Log.d(TAG, "then: successful task");
                            return storageReference.getDownloadUrl();
                        }
                    })
                    .addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                Uri downloadURL = task.getResult();
                                Log.d(TAG, "onComplete: DL URL : " + task.getResult());
                                Toast.makeText(mContext, "Upload successful", Toast.LENGTH_SHORT).show();
                                //If it isn't called for chaning the profile photo
                                if(photoType.equals(mContext.getString(R.string.new_photo))){
                                    //Add new Photo to the DB
                                    addPhotoToDatabase(caption,downloadURL.toString());
                                    //Navigate to the main feed
                                    Intent intent = new Intent(mContext, MainActivity.class);
                                    mContext.startActivity(intent);
                                }
                                else {
                                    //insert into 'user_accounts_settings node'
                                    setProfilePhoto(downloadURL.toString());
                                    //Navigate back to edit profile while it is being uploaded
                                    ((AccountSettingsActivity)mContext).setViewPager(((AccountSettingsActivity)mContext)
                                            .pagerAdapter.getFragmentNumber(mContext.getString(R.string.edit_profile_fragment)));
                                }
                            }
                        }
                    })
            ;
    }

    private void setProfilePhoto(String url){
        Log.d(TAG, "setProfilePhoto:  setting new progile image " + url);

        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(mAuth.getCurrentUser().getUid())
                .child(mContext.getString(R.string.profile_photo))
                .setValue(url);
    }

    private void addPhotoToDatabase(String caption,String url){
        Log.d(TAG, "addPhotoToDatabase: adding photo to the DB");

        String tags = StringManipulation.getTags(caption);
        String newPhotoKey =  myRef.child(mContext.getString(R.string.dbname_photos)).push().getKey();
        Photo photo = new Photo();
        photo.setCaption(caption);
        photo.setDate_created(getTimeStamp());
        photo.setImage_path(url);
        photo.setTags(tags);
        photo.setUser_id(mAuth.getCurrentUser().getUid());
        photo.setPhoto_id(newPhotoKey);

        myRef.child(mContext.getString(R.string.dbname_user_photos)).child(mAuth.getCurrentUser().getUid())
                .child(newPhotoKey).setValue(photo);
        myRef.child(mContext.getString(R.string.dbname_photos)).child(newPhotoKey).setValue(photo);
    }

    private String getTimeStamp(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.UK);
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Athens"));
        return sdf.format(new Date());
    }
    public int getImageCount(DataSnapshot dataSnapshot) {
        int count = 0;
        for (DataSnapshot ds : dataSnapshot
                .child(mContext.getString(R.string.dbname_user_photos))
                .child(userID)
                .getChildren()) {
            count++;
        }
        return count;
    }

    /**
     * update the email in the 'user' node
     *
     * @param email
     */
    public void updateEmail(String email) {
        Log.d(TAG, "updateUsername: updating email to " + email);

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .child(mContext.getString(R.string.field_email))
                .setValue(email);
    }

    public void updateWebsite(String website) {
        Log.d(TAG, "updateUserAccountSettings: Updating user account settings");

        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userID)
                .child(mContext.getString(R.string.field_website))
                .setValue(website);

    }

    public void updateDisplayName(String displayName) {
        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userID)
                .child(mContext.getString(R.string.field_display_name))
                .setValue(displayName);
    }

    public void updateDescription(String description) {
        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userID)
                .child(mContext.getString(R.string.field_description))
                .setValue(description);
    }

    /**
     * update the username in the user node and the user_account_settings node
     *
     * @param username
     */
    public void updateUsername(String username) {
        Log.d(TAG, "updateUsername: updating username to " + username);

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .child(mContext.getString(R.string.field_username))
                .setValue(username);
        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userID)
                .child(mContext.getString(R.string.field_username))
                .setValue(username);
    }

    public void updatePhoneNumber(long phoneNumber) {
        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .child(mContext.getString(R.string.field_phone_number))
                .setValue(phoneNumber);
    }

   /* public boolean checkIfUsernameExists(String username, DataSnapshot dataSnapshot){
        Log.d(TAG, "checkIfUsernameExists: checking if username already exists");
        User user = new User();

        for(DataSnapshot ds :  dataSnapshot.child(mContext.getString(R.string.dbname_users)).getChildren()){
            Log.d(TAG, "checkIfUsernameExists: data snap shot" + ds);
            user.setUsername(ds.getValue(User.class).getUsername());
            Log.d(TAG, "checkIfUsernameExists: username : " + user.getUsername());

            if(StringManipulation.expandUsername(user.getUsername()).equals(username)){
                Log.d(TAG, "checkIfUsernameExists: found a match");
                return true;
            }
        }

        return false;
    }*/

    /**
     * Register a new email and password to Firebase Authentication
     *
     * @param email
     * @param password
     * @param username
     */
    public void registerNewEmail(String email, String password, String username) {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, mContext.getString(R.string.auth_complete));
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(mContext, mContext.getString(R.string.auth_complete),
                                    Toast.LENGTH_SHORT).show();
                            userID = mAuth.getCurrentUser().getUid();
                            sendVerificationEmail();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:Failed to authenticate", task.getException());
                            Toast.makeText(mContext, mContext.getString(R.string.auth_failed),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void sendVerificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {

                    } else {
                        Log.d(TAG, "onComplete: Couldn't send verification email");
                    }
                }
            });
        }
    }

    /**
     * Add information to the users node and user_account_settings in DB
     *
     * @param email
     * @param username
     * @param description
     * @param website
     * @param profile_photo
     */
    public void addNewUser(String email, String username, String description, String website, String profile_photo) {

        User user = new User(userID, 1, email, StringManipulation.condenseUsername(username)    );

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userID).setValue(user);

        UserAccountSettings settings = new UserAccountSettings(
                description,
                username,
                0,
                0,
                0,
                profile_photo,
                StringManipulation.condenseUsername(username),
                website,
                userID
        );

        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userID)
                .setValue(settings);

    }

    /**
     * Retrieves the account settings for user currently logged in
     * Database = user_account_settings node
     *
     * @param dataSnapshot
     * @return
     */
    public UserSettings getUserSettings(DataSnapshot dataSnapshot) {
        Log.d(TAG, "getUserAccountSettings: retrieving user account settings from Firebase");
        UserAccountSettings settings = new UserAccountSettings();
        User user = new User();

        for (DataSnapshot ds : dataSnapshot.getChildren()) {
            // user_account_settings Node
            if (ds.getKey().equals(mContext.getString(R.string.dbname_user_account_settings))) {
                Log.d(TAG, "getUserSettings: datasnapshot" + ds);

                try {

                    settings.setDisplay_name(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getDisplay_name());
                    settings.setUsername(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getUsername());
                    settings.setWebsite(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getWebsite());
                    settings.setDescription(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getDescription());
                    settings.setProfile_photo(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getProfile_photo());
                    settings.setPosts(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getPosts());
                    settings.setFollowing(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getFollowing());
                    settings.setFollowers(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getFollowers());
                    Log.d(TAG, "getUserAccountSettings: Retrieved user_account_seettings info :" + settings.toString());

                } catch (NullPointerException e) {
                    Log.d(TAG, "getUserAccountSettings: NullPointerException : " + e.getMessage());
                }
            }
            // users Node
            if (ds.getKey().equals(mContext.getString(R.string.dbname_users))) {
                Log.d(TAG, "getUserSettings: datasnapshot" + ds);
                user.setUsername(
                        ds.child(userID)
                                .getValue(User.class)
                                .getUsername());
                user.setEmail(
                        ds.child(userID)
                                .getValue(User.class)
                                .getEmail());
                user.setPhone_number(
                        ds.child(userID)
                                .getValue(User.class)
                                .getPhone_number());
                user.setUser_id(
                        ds.child(userID)
                                .getValue(User.class)
                                .getUser_id());
                Log.d(TAG, "getUserAccountSettings: Retrieved user info : " + user.toString());
            }
        }
        return new UserSettings(user, settings);

    }
}
