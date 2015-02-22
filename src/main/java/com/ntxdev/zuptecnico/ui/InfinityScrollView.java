package com.ntxdev.zuptecnico.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * Created by igorlira on 3/18/14.
 */
public class InfinityScrollView extends ScrollView {
    public interface OnScrollViewListener {
        void onScrollChanged(InfinityScrollView v, int l, int t, int oldl, int oldt );
    }

    private OnScrollViewListener mOnScrollViewListener;

    public void setOnScrollViewListener(OnScrollViewListener l) {
        this.mOnScrollViewListener = l;
    }

    public InfinityScrollView(Context context)
    {
        super(context);
    }

    public InfinityScrollView(Context context, AttributeSet attributeSet)
    {
        super(context, attributeSet);
    }

    public InfinityScrollView(Context context, AttributeSet attributeSet, int defStyle)
    {
        super(context, attributeSet, defStyle);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        if(mOnScrollViewListener != null)
            mOnScrollViewListener.onScrollChanged(this, l, t, oldl, oldt);

        super.onScrollChanged(l, t, oldl, oldt);
    }
}
