package com.ccs.deductive_case_files.graphics;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;

/**
 * Starts short value animations that redraw the host view only while they run.
 * When animations are disabled in settings the end value is applied immediately.
 */
public final class AnimationManager {
    public interface Setter {
        void set(float value);
    }

    /** Material "emphasized decelerate" easing. */
    public static final Interpolator EMPHASIZED = new PathInterpolator(0.05f, 0.7f, 0.1f, 1f);
    public static final Interpolator DECELERATE = new DecelerateInterpolator(1.6f);

    private static boolean enabled = true;
    private final View host;

    public AnimationManager(View host) {
        this.host = host;
    }

    public static void setEnabled(boolean on) {
        enabled = on;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public ValueAnimator run(float from, float to, long ms, Setter setter) {
        return run(from, to, ms, DECELERATE, setter, null);
    }

    public ValueAnimator run(float from, float to, long ms, Interpolator ip, final Setter setter,
                             final Runnable end) {
        if (!enabled || ms <= 0 || !ValueAnimator.areAnimatorsEnabled()) {
            setter.set(to);
            host.invalidate();
            if (end != null) end.run();
            return null;
        }
        ValueAnimator a = ValueAnimator.ofFloat(from, to);
        a.setDuration(ms);
        a.setInterpolator(ip);
        a.addUpdateListener(v -> {
            setter.set((Float) v.getAnimatedValue());
            host.invalidate();
        });
        if (end != null) {
            a.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    end.run();
                }
            });
        }
        a.start();
        return a;
    }
}
