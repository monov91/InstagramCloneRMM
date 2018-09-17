package com.projects.radomonov.instagramclone.Share;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.projects.radomonov.instagramclone.Profile.AccountSettingsActivity;
import com.projects.radomonov.instagramclone.R;
import com.projects.radomonov.instagramclone.Utils.Permissions;

public class PhotoFragment extends Fragment {
    private static final String TAG = "PhotoFragment";

    // constants
    private static final int PHOTO_FRAGMENT_NUMBER = 1;
    private static final int GALLERY_FRAGMENT_NUMBER = 0;
    private static final int CAMERA_REQUEST_CODE = 5;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo,container,false);

        Button btnLaunchCamera = view.findViewById(R.id.btnLaunchCamera);
        btnLaunchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(((ShareActivity)getActivity()).getCurrentTabNumber() == PHOTO_FRAGMENT_NUMBER){
                    // Check again for camera permission
                    if(((ShareActivity)getActivity()).checkSinglePermission(Permissions.PERMISSIONS[2])){
                        Log.d(TAG, "onClick: starting camera");
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(cameraIntent,CAMERA_REQUEST_CODE);
                    } else {
                        // If somehow we dont have permission for camera restart the share activity
                        // and clear activity stack
                        Intent intent = new Intent (getActivity(),ShareActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                }

            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CAMERA_REQUEST_CODE){
            Log.d(TAG, "onActivityResult: done taking a photo");
            Log.d(TAG, "onActivityResult: navigating to final share screen");

            Bitmap bitmap = (Bitmap) data.getExtras().get("data");

            if (isRootTask()) {
                try {
                    Log.d(TAG, "onActivityResult:  We received a new Bitmap from camera" + bitmap);
                    Intent intent = new Intent(getActivity(), PostActivity.class);
                    intent.putExtra(getString(R.string.selected_bitmap), bitmap);
                    startActivity(intent);
                } catch (NullPointerException e){
                    Log.d(TAG, "onActivityResult: NullPointerException");
                }
            } else {
                try {
                    Log.d(TAG, "onActivityResult:  We received a new Bitmap from camera" + bitmap);
                    Intent intent = new Intent(getActivity(), AccountSettingsActivity.class);
                    intent.putExtra(getString(R.string.selected_bitmap), bitmap);
                    intent.putExtra(getString(R.string.return_to_fragment),getString(R.string.edit_profile_fragment));
                    startActivity(intent);
                    //fixing back stack
                    getActivity().finish();
                } catch (NullPointerException e){
                    Log.d(TAG, "onActivityResult: NullPointerException");
                }
            }

        }
    }

    private boolean isRootTask() {
        if (((ShareActivity) getActivity()).getTask() == 0) {
            return true;
        } else {
            return false;
        }
    }
}
