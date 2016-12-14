package com.autocounting.autocounting.utils;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

/**
 * Runs animations asynchronously.
 */
public class AnimationRunner extends Thread implements Runnable{

    private final Context context;
    private final View view;
    private final int animationId;
    private final int duration;

    public AnimationRunner(Context context, View view, int animationId, int duration){
        this.context = context;
        this.view = view;
        this.animationId = animationId;
        this.duration = duration;
    }

    @Override
    public void run() {
        Animation animation = AnimationUtils.loadAnimation(context, animationId);
        animation.setDuration(duration);
        view.startAnimation(animation);
    }
}
