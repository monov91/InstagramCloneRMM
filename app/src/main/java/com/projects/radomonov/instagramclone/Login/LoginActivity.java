package com.projects.radomonov.instagramclone.Login;

import android.content.Context;
import android.content.Intent;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.projects.radomonov.instagramclone.Home.MainActivity;
import com.projects.radomonov.instagramclone.R;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    //firebase authentication
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private Context mContext;
    private ProgressBar mProgoressBar;
    private EditText mEmail,mPassword;
    private TextView mPleaseWait;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.d(TAG, "onCreate: started.");


        mProgoressBar = findViewById(R.id.progressBar);
        mPleaseWait = findViewById(R.id.pleaseWait);
        mEmail = findViewById(R.id.input_email);
        mPassword = findViewById(R.id.input_password);
        mContext = LoginActivity.this;

        hideProgressBar();

        setupFirebaseAuth();
        loginButtonSetup();
        registerButtonSetup();

    }

    private boolean isStringNull(String str){
        Log.d(TAG, "isStringNull: checking if string is null");
        if(str.trim().equals("") || str.equals(null)){
            return true;
        } else {
            return false;
        }
    }

    private void showProgressBar(){
        mProgoressBar.setVisibility(View.VISIBLE);
        mPleaseWait.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar(){
        mProgoressBar.setVisibility(View.GONE);
        mPleaseWait.setVisibility(View.GONE);
    }

    private void loginButtonSetup(){
        Button btnLogin = findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();


                if((isStringNull(email)) || isStringNull(password)){
                    Toast.makeText(mContext, "Fill out email and password", Toast.LENGTH_SHORT).show();
                } else {showProgressBar();

                    mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d(TAG, "onComplete: " + task.isSuccessful());
                            FirebaseUser user = mAuth.getCurrentUser();
                            /*
                                If sign in fails , display a message to the user.If sign in succeeds ,
                                the authstate listener will be notified and logic to handle
                                the signed in user can he handled in the listener
                                 */
                            if(task.isSuccessful()){
                                try{
                                    Log.d(TAG, "onComplete: user - " + user.getUid());
                                    if(user.isEmailVerified()){
                                        Log.d(TAG, "onComplete email verified: success . Navigate to Home Activity");
                                        Intent intent = new Intent(mContext,MainActivity.class);
                                        startActivity(intent);
                                    } else {
                                        Toast.makeText(mContext, "Email is not verified . Resending confirmation email", Toast.LENGTH_SHORT).show();
                                        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {

                                                } else {
                                                    Log.d(TAG, "onComplete: Couldn't send verification email");
                                                }
                                            }
                                        });
                                        mAuth.signOut();
                                    }
                                } catch(NullPointerException e){
                                    Log.d(TAG, "onClick: NullPointerException: " + e.getMessage());
                                }

                            } else {
                                Log.d(TAG, "onComplete: login with email failed");
                                Toast.makeText(mContext,getString(R.string.auth_failed), Toast.LENGTH_SHORT).show();
                            }
                            hideProgressBar();
                        }
                    });
                }

            }
        });

        /*
        If the user is logged in go to home activity
         */

        if(mAuth.getCurrentUser() != null){
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void registerButtonSetup(){
        TextView linkSignUp = findViewById(R.id.btn_signUp);
        linkSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Navigating to register activity");
                Intent intent = new Intent (LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    /*
        ----------------------------------------- Firebase ----------------------------------
         */
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
    }




}
