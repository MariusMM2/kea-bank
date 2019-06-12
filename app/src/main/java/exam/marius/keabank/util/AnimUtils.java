package exam.marius.keabank.util;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.support.annotation.AnimRes;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import exam.marius.keabank.R;

/**
 * Utility class for providing different animations
 */
public class AnimUtils {

    // Rotates an image button to a given direction, specified by XML
    public static void rotate(ImageButton imageButton, @AnimRes int direction) {
        Animation rotateAnim = android.view.animation.AnimationUtils.loadAnimation(imageButton.getContext(), direction);
        rotateAnim.setFillAfter(true);

        imageButton.startAnimation(rotateAnim);
    }

    // Used in swapping the new rating button with the submit rating button
    public static void swapButtons(ImageView view1, ImageView view2, boolean stage) {
        int duration = view1.getResources().getInteger(android.R.integer.config_mediumAnimTime);

        ObjectAnimator fadeOut = fade(view1, duration, !stage);

        ObjectAnimator lower = ObjectAnimator.ofFloat(view1, "elevation", stage ?
                view1.getResources().getDimension(R.dimen.fab_elevation_low) :
                view1.getResources().getDimension(R.dimen.fab_elevation_high));
        lower.setDuration(duration);
        lower.setInterpolator(new LinearInterpolator());

        ObjectAnimator fadeIn = fade(view2, duration, stage);

        ObjectAnimator elevate = ObjectAnimator.ofFloat(view2, "elevation", stage ?
                view1.getResources().getDimension(R.dimen.fab_elevation_high) :
                view1.getResources().getDimension(R.dimen.fab_elevation_low));
        elevate.setDuration(duration);
        elevate.setInterpolator(new LinearInterpolator());

        ObjectAnimator rotate1 = ObjectAnimator.ofFloat(view1, View.ROTATION, stage ? 720 : 0);
        rotate1.setDuration(duration);
        rotate1.setInterpolator(new AccelerateDecelerateInterpolator());

        ObjectAnimator rotate2 = ObjectAnimator.ofFloat(view2, View.ROTATION, stage ? 720 : 0);
        rotate2.setDuration(duration);
        rotate2.setInterpolator(new AccelerateDecelerateInterpolator());

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(rotate1).with(fadeOut).with(lower)
                .with(rotate2).with(fadeIn).with(elevate)
        ;
        animatorSet.start();
    }

    // Used to fade in or out an item
    public static ObjectAnimator fade(View target, int duration, boolean show) {
        ObjectAnimator fade = ObjectAnimator.ofFloat(target, View.ALPHA, show ? 1f : 0f);
        fade.setDuration(duration);
        fade.setInterpolator(new LinearInterpolator());
        return fade;
    }
}
