package com.projects.radomonov.instagramclone.Utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

public class Heart {
    private static final String TAG = "Heart";

    public ImageView heartWhite;
    public ImageView heartRed;

    private static final DecelerateInterpolator DECELERATOR_INTERPOLATOR = new DecelerateInterpolator();
    private static final AccelerateInterpolator ACCELERATOR_INTERPOLATOR = new AccelerateInterpolator();

    public Heart(ImageView heartWhite, ImageView heartRed) {
        this.heartWhite = heartWhite;
        this.heartRed = heartRed;
    }

    public void toggleLike() {
        Log.d(TAG, "toggleLike: toggling heart");

        AnimatorSet animationSet = new AnimatorSet();
        if (heartRed.getVisibility() == View.VISIBLE) {
            Log.d(TAG, "toggleLike: toggling red heart off");
            heartRed.setScaleX(0.1f);
            heartRed.setScaleY(0.1f);

            ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(heartRed, "scaleY", 1f, 0f);
            scaleDownY.setDuration(300);
            scaleDownY.setInterpolator(ACCELERATOR_INTERPOLATOR);
            ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(heartRed, "scaleX", 1f, 0f);
            scaleDownX.setDuration(300);
            scaleDownX.setInterpolator(ACCELERATOR_INTERPOLATOR);

            heartRed.setVisibility(View.GONE);
            heartWhite.setVisibility(View.VISIBLE);

            animationSet.playTogether(scaleDownY, scaleDownX);

        } else {
            if (heartRed.getVisibility() == View.GONE) {
                Log.d(TAG, "toggleLike: toggling red heart on");
                heartRed.setScaleX(0.1f);
                heartRed.setScaleY(0.1f);

                ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(heartRed, "scaleY", 0.1f, 1f);
                scaleDownY.setDuration(300);
                scaleDownY.setInterpolator(DECELERATOR_INTERPOLATOR);
                ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(heartRed, "scaleX", 0.1f, 1f);
                scaleDownX.setDuration(300);
                scaleDownX.setInterpolator(DECELERATOR_INTERPOLATOR);

                heartRed.setVisibility(View.VISIBLE);
                heartWhite.setVisibility(View.GONE);

                animationSet.playTogether(scaleDownY, scaleDownX);
            }
        }
        animationSet.start();

    }
}
