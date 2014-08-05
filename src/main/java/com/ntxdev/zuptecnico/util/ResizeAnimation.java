package com.ntxdev.zuptecnico.util;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by igorlira on 5/25/14.
 */
public class ResizeAnimation extends Animation {
    private View mView;
    private float mToHeight;
    private float mFromHeight;

    private float mToWidth;
    private float mFromWidth;

    boolean hasWidth;
    boolean hasHeigth;

    public ResizeAnimation(View v, float fromHeight, float toHeight) {
        mToHeight = toHeight;
        mFromHeight = fromHeight;
        mView = v;
        setDuration(300);

        hasWidth = false;
        hasHeigth = true;
    }

    public ResizeAnimation(View v, float fromWidth, float fromHeight, float toWidth, float toHeight) {
        mToHeight = toHeight;
        mToWidth = toWidth;
        mFromHeight = fromHeight;
        mFromWidth = fromWidth;
        mView = v;
        setDuration(300);

        hasWidth = true;
        hasHeigth = true;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        float height =
                (mToHeight - mFromHeight) * interpolatedTime + mFromHeight;
        float width = (mToWidth - mFromWidth) * interpolatedTime + mFromWidth;
        ViewGroup.LayoutParams p = mView.getLayoutParams();
        if(hasHeigth)
            p.height = (int) height;
        if(hasWidth)
            p.width = (int) width;
        mView.requestLayout();
    }
}