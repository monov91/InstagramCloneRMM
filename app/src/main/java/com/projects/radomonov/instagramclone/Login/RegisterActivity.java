package com.projects.radomonov.instagramclone.Login;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.projects.radomonov.instagramclone.Models.User;
import com.projects.radomonov.instagramclone.R;
import com.projects.radomonov.instagramclone.Utils.FirebaseHelper;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "Register Activity";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private Context mContext;
    private ProgressBar mProgoressBar;
    private EditText mEmail,mPassword,mUsername;
    private TextView mPleaseWait;
    private String email,username,password;
    private Button btnRegister;
    private FirebaseHelper firebaseHelper;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private String append = "";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Log.d(TAG, "onCreate: started.");

        mContext = RegisterActivity.this;
        initWidgets();
        setupFirebaseAuth();
        firebaseHelper = new FirebaseHelper(mContext);
        setupRegisterButton();
    }

    /*
     Initialize activity widgets
     */
    private void initWidgets(){
        mProgoressBar = findViewById(R.id.progressBar);
        mPleaseWait = findViewById(R.id.loadingPleaseWait);
        mEmail = findViewById(R.id.input_email);
        mPassword = findViewById(R.id.input_password);
        mUsername = findViewById(R.id.input_username);
        btnRegister = findViewById(R.id.btn_register);
        hideProgressBar();
    }

    private void hideProgressBar(){
        mProgoressBar.setVisibility(View.GONE);
        mPleaseWait.setVisibility(View.GONE);
    }
    private void showProgressBar(){
        mProgoressBar.setVisibility(View.VISIBLE);
        mPleaseWait.setVisibility(View.VISIBLE);
    }

    private boolean isStringNull(String str){
        Log.d(TAG, "isStringNull: checking if string is null");
        if(str.equals("") || str.equals(null)){
            return true;
        } else {
            return false;
        }
    }

    private void setupRegisterButton(){
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = mEmail.getText().toString();
                password = mPassword.getText().toString();
                username = mUsername.getText().toString();

                if(checkInputs(email,username,password)){
                    showProgressBar();
                    firebaseHelper.registerNewEmail(email,password,username);
                }
            }
        });

    }

    private boolean checkInputs(String email,String username,String password){
        Log.d(TAG, "checkInputs: checking for null values");
        if(isStringNull(email) || isStringNull(username) || isStringNull(password)){
            Toast.makeText(mContext, "All fields must be filled", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /*
            ----------------------------------------- Firebase ----------------------------------
             */

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

                for(DataSnapshot singleDataSnapshot : dataSnapshot.getChildren()){
                    Log.d(TAG, "checkIfUsernameExists: FOUND MATCH : " + singleDataSnapshot.getValue(User.class).getUsername());
                    Toast.makeText(mContext, "That username already exists", Toast.LENGTH_SHORT).show();
                    append = myRef.push().getKey().substring(3,10);
                    Log.d(TAG, "onDataChange: Username already exists . Appending random string to name : "+ append);
                }
                String mUsername;
                mUsername = username + append;

                //add new user to the DB
                firebaseHelper.addNewUser(email,mUsername,"","","");

                Toast.makeText(mContext, "Signup Successful sending verification email", Toast.LENGTH_SHORT).show();

                mAuth.signOut();
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
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        //FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);
        //mAuth.addAuthStateListener(mAuthStateListener);
        mAuth.addAuthStateListener(mAuthStateListener);
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
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged: signed_id: " + user.getUid());
                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            //check : make sure that username is not already in use
                            /*
                            mitch tabian --> mitch.tabian_2asdasd9a (random str)
                             */
                            checkIfUsernameExists(username);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    finish();
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged: signed_out " );
                }
            }
        };
    }
}
