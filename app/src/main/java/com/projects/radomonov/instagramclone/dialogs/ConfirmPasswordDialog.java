package com.projects.radomonov.instagramclone.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.projects.radomonov.instagramclone.R;

public class ConfirmPasswordDialog extends DialogFragment{
    private static final String TAG = "ConfirmPasswordDialog";

    public interface onConfirmPasswordListener{
            void onConfirmPassword(String password);
    }
    onConfirmPasswordListener mOnConfirmPasswordListener;

    private EditText mPassword;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_confirm_password,container,false);
        Log.d(TAG, "onCreateView: started");

        mPassword = view.findViewById(R.id.confirm_password);
        TextView cancel = view.findViewById(R.id.dialogCancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: closing the dialog");
                getDialog().dismiss();
            }
        });
        TextView confirm = view.findViewById(R.id.dialogConfirm);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: getting password and confirming");

                String password = mPassword.getText().toString();
                if(!password.equals("")){
                    // Invokes the overriden method in EditProfileFragment (onConfirmPassword())
                    mOnConfirmPasswordListener.onConfirmPassword(password);
                    getDialog().dismiss();
                } else {
                    Toast.makeText(getActivity(), "Enter your password", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach: attached");
        try{
            //Gets connected to EditProfileFragment
            mOnConfirmPasswordListener = (onConfirmPasswordListener) getTargetFragment();
            Log.d(TAG, "onAttach: tried");
        } catch (ClassCastException e){
            Log.e(TAG, "onAttach: ClassCastException" + e.getMessage());
        }
    }
}
