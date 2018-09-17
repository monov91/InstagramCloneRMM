package com.projects.radomonov.instagramclone.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.ProviderQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.projects.radomonov.instagramclone.Models.User;
import com.projects.radomonov.instagramclone.Models.UserAccountSettings;
import com.projects.radomonov.instagramclone.Models.UserSettings;
import com.projects.radomonov.instagramclone.R;
import com.projects.radomonov.instagramclone.Share.ShareActivity;
import com.projects.radomonov.instagramclone.Utils.FirebaseHelper;
import com.projects.radomonov.instagramclone.Utils.UniversalImageLoader;
import com.projects.radomonov.instagramclone.dialogs.ConfirmPasswordDialog;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileFragment extends Fragment implements ConfirmPasswordDialog.onConfirmPasswordListener {

    @Override
    public void onConfirmPassword(String password) {
        Log.d(TAG, "onConfirmPassword: got the password" + password);
        final FirebaseUser user = mAuth.getCurrentUser();


        //Get auth credentials from the user for re-authenticating
        final AuthCredential credential = EmailAuthProvider
                .getCredential(mAuth.getCurrentUser().getEmail(),password);
        //Prompt the user to re-provide their sign-in credentials
        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "onComplete: User re-authenticated");


                            // Check if the email is not already present in the firebase authentication
                            mAuth.fetchProvidersForEmail(mEmail.getText().toString())
                                    .addOnCompleteListener(new OnCompleteListener<ProviderQueryResult>() {
                                @Override
                                public void onComplete(@NonNull Task<ProviderQueryResult> task) {
                                    try {


                                        if (task.isSuccessful()) {
                                            // If the task result returns != 0 , that means it found a match and the email is in use already
                                            if (task.getResult().getProviders().size() == 1) {
                                                Log.d(TAG, "onComplete: that email is already in use");
                                                Toast.makeText(mContext, "That email is already in use", Toast.LENGTH_SHORT).show();
                                            } else {
                                                // The email is available so update it
                                                Log.d(TAG, "onComplete: that email is available ");
                                                user.updateEmail(mEmail.getText().toString())
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Log.d(TAG, "onComplete: User email adress updated");
                                                                    Toast.makeText(mContext, "Email updated", Toast.LENGTH_SHORT).show();
                                                                    mFirebaseHelper.updateEmail(mEmail.getText().toString());
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    } catch (NullPointerException e){
                                        Log.d(TAG, "onComplete: Nullpointerexception : " + e.getMessage());
                                    }
                                }
                            });


                        } else {
                            Log.d(TAG, "onComplete: Re-authentication failed");
                        }
                    }
                });
    }
    
    private static final String TAG = "EditProfileFragment";

    private Context mContext;

    // Widgets
    private CircleImageView mProfilePhoto ;
    private ImageView backArrow,checkMark;
    private EditText mDisplayName,mUserName,mWebsite,mDescription,mEmail, mPhoneNumber;
    private TextView mChangeProfilePhoto;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseHelper mFirebaseHelper;
    private String userID ;

    private UserSettings mUserSettings;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_editprofile ,container,false);

        mContext = getActivity();
        mProfilePhoto = view.findViewById(R.id.profile_photo);
        mDisplayName = view.findViewById(R.id.display_name);
        mUserName = view.findViewById(R.id.username);
        mWebsite = view.findViewById(R.id.website);
        mDescription = view.findViewById(R.id.description);
        mEmail = view.findViewById(R.id.email);
        mPhoneNumber = view.findViewById(R.id.phoneNumber);
        mChangeProfilePhoto = view.findViewById(R.id.change_profile_photo);
        checkMark = view.findViewById(R.id.saveChanges);

        mFirebaseHelper = new FirebaseHelper(mContext);

        setupFirebaseAuth();
        //setProfileImage();

        //back arrow to navigating back to profile activity
        backArrow = view.findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Navigating back to profile activity");
                getActivity().finish();
            }
        });

        checkMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Attempting to save changes");
                saveProfileSettings();
            }
        });
        return view;
    }

    /**
     * Retrieves the data in the widgets
     * and submits it in the databaase
     * and makes sure that the username is unique
     */
    private void saveProfileSettings(){
        final String displayName = mDisplayName.getText().toString();
        final String username = mUserName.getText().toString();
        final String description = mDescription.getText().toString();
        final String website = mWebsite.getText().toString();
        final String email = mEmail.getText().toString();
        final long phoneNumber = Long.parseLong(mPhoneNumber.getText().toString());

        if(!mUserSettings.getUser().getUsername().equals(username)){
            Log.d(TAG, "onDataChange: changed username attempt");
            checkIfUsernameExists(username);
        }
        //case 2  - the user  changed his email
        if(!mUserSettings.getUser().getEmail().equals(email)){
            ConfirmPasswordDialog dialog = new ConfirmPasswordDialog();
            dialog.show(getFragmentManager(),getString(R.string.confirm_password_dialog));
            //The dialog opens and knows when it closes that its target fragment will be EditProfileFragment
            // thus letting us pass variables using the interface to the fragment
            dialog.setTargetFragment(EditProfileFragment.this,1);
            // After the dialog is opened it redirects to onConfirmPassword() method in this fragment
            // where the email changing logic is handled
        }
        /*
         Update the other fields only where they've been changed
         and do not require uniqueness
         */
        if(!mUserSettings.getSettings().getDisplay_name().equals(displayName)){
            // update DisplayName
            mFirebaseHelper.updateDisplayName(displayName);
        }
        if(!mUserSettings.getSettings().getWebsite().equals(website)){
            // update Website
            mFirebaseHelper.updateWebsite(website);
        }
        if(!mUserSettings.getSettings().getDescription().equals(description)){
            // update Description
            mFirebaseHelper.updateDescription(description);
        }
        if(mUserSettings.getUser().getPhone_number() != phoneNumber){
            // update PhoneNumber
            mFirebaseHelper.updatePhoneNumber(phoneNumber);
        }



    }

    /**
     * check if @param already exists in the database
      * @param username
     */
    private void checkIfUsernameExists(final String username) {
        Log.d(TAG, "checkIfUsernameExists: checking if " + username + " already exists in the database");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        Query query = ref
                .child(getString(R.string.dbname_users))
                .orderByChild(getString(R.string.field_username))
                .equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    // add username
                    mFirebaseHelper.updateUsername(username);
                    Toast.makeText(mContext, "Saved username", Toast.LENGTH_SHORT).show();
                }
                for(DataSnapshot singleDataSnapshot : dataSnapshot.getChildren()){
                    Log.d(TAG, "checkIfUsernameExists: FOUND MATCH : " + singleDataSnapshot.getValue(User.class).getUsername());
                    Toast.makeText(mContext, "That username already exists", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        /*
        The query method is the same as .child().child().child()...
        but supposedly faster
         */

    }

    private void setProfileWidgets(UserSettings userSettings){
        Log.d(TAG, "setProfileWidgets: Setting widgets with data retrieved from firebase DB");

        User user = userSettings.getUser();
        UserAccountSettings settings = userSettings.getSettings();

        mUserSettings = userSettings;

        UniversalImageLoader.setImage(settings.getProfile_photo(),mProfilePhoto,null,"");

        mDisplayName.setText(settings.getDisplay_name());
        mUserName.setText(settings.getUsername());
        mWebsite.setText(settings.getWebsite());
        mDescription.setText(settings.getDescription());
        mPhoneNumber.setText(String.valueOf(user.getPhone_number()));
        mEmail.setText(user.getEmail());
        mChangeProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: changing profile photo");
                Intent intent  = new Intent(getActivity(), ShareActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getActivity().startActivity(intent);
                //fixing backstack
                getActivity().finish();
            }
        });

    }

   /* private void setProfileImage(){
        Log.d(TAG, "setProfileImage: Setting profile image");
        String imgURL = "https://support.appsflyer.com/hc/article_attachments/115011109089/android.png"; //random img
        UniversalImageLoader.setImage(imgURL,mProfilePhoto,null,"");
    }*/
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
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        userID = mAuth.getCurrentUser().getUid();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if(user != null){
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged: signed_id: " + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged: signed_out " );
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
