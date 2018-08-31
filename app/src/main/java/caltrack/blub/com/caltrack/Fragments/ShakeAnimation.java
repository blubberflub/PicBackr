package caltrack.blub.com.caltrack.Fragments;

import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;

public class ShakeAnimation
{
    public TranslateAnimation shakeError() {
        TranslateAnimation shake = new TranslateAnimation(0, 8, 0, 0);
        shake.setDuration(600);
        shake.setInterpolator(new CycleInterpolator(3));
        return shake;
    }
}
