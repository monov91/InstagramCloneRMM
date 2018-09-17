package com.projects.radomonov.instagramclone.Search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.projects.radomonov.instagramclone.Models.User;
import com.projects.radomonov.instagramclone.Profile.ProfileActivity;
import com.projects.radomonov.instagramclone.R;
import com.projects.radomonov.instagramclone.Utils.BottomNavigatinoViewHelper;
import com.projects.radomonov.instagramclone.Utils.UserListAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchActivity extends AppCompatActivity {
    private static final String TAG = "SearchActivity";
    private Context mContext = SearchActivity.this;
    private static final int ACTIVITY_NUM = 1;

    //widgets

    private EditText mSearchParam;
    private ListView mListView;

    //vars
    List<User> mUserList;
    private UserListAdapter mAdapter;
    private String mCurrentUsername;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Log.d(TAG, "onCreate: Started");

        mSearchParam = findViewById (R.id.search);
        mListView = findViewById(R.id.listView);
        getCurrentUsername();
        hideSoftKeyboard();
        setUpBottomNavigationView();
        initTextListener();
    }

    private void getCurrentUsername(){
        DatabaseReference reference =FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_users))
                .orderByChild(getString(R.string.field_user_id))
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleDaatasnapshot : dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found my username" + singleDaatasnapshot.getValue(User.class).getUsername().toString() );
                    mCurrentUsername = singleDaatasnapshot.getValue(User.class).getUsername().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void searchForMatch(String keyword){
        Log.d(TAG, "searchForMatch: Searching for a match " + keyword);
        mUserList.clear();
        if(keyword.length() == 0){

        } else {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference
                    .child(getString(R.string.dbname_users))
                    .orderByChild(getString(R.string.field_username))
                    .equalTo(keyword);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot singlesnapshot : dataSnapshot.getChildren()){
                        Log.d(TAG, "onDataChange: found user " + singlesnapshot.getValue(User.class).getUsername().toString());
                        if(!singlesnapshot.getValue(User.class).getUsername().toString().equals(mCurrentUsername)){
                            mUserList.add(singlesnapshot.getValue(User.class));
                        }
                        //update the users ListView
                        updateUsersList();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }


    private void updateUsersList(){
        Log.d(TAG, "updateUsersList: updating users list ");
        mAdapter = new UserListAdapter(SearchActivity.this,R.layout.layout_user_listitem,mUserList);

        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "onItemClick: selected user " + mUserList.get(i).toString());

                //navigate to profileActivity
                Intent intent = new Intent(SearchActivity.this, ProfileActivity.class);
                intent.putExtra(getString(R.string.calling_activity),getString(R.string.search_activity));
                intent.putExtra(getString(R.string.intent_user),mUserList.get(i));
                startActivity(intent);
            }
        });
    }

    private void initTextListener(){
        Log.d(TAG, "initTextListener: initializing");

        mUserList = new ArrayList<>();
        mSearchParam.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String text = mSearchParam.getText().toString().toLowerCase(Locale.getDefault());
                searchForMatch(text);
            }
        });
    }
    private void setUpBottomNavigationView(){
        BottomNavigationViewEx bottomNavigationViewEx  = findViewById(R.id.bottomNavViewBar);
        BottomNavigatinoViewHelper.setUpBottomNavigationView(bottomNavigationViewEx);
        BottomNavigatinoViewHelper.enableNavigation(mContext,bottomNavigationViewEx,this);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem item = menu.getItem(ACTIVITY_NUM);
        item.setChecked(true);
    }

    private void hideSoftKeyboard() {
        if(getCurrentFocus() != null){
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);
        }

    }
}
