package com.ntxdev.zuptecnico.ui;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.hardware.display.DisplayManager;
import android.os.AsyncTask;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;


import com.ntxdev.zuptecnico.R;
import com.ntxdev.zuptecnico.util.ResizeAnimation;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by igorlira on 2/9/14.
 */
public class SingularTabHost extends FrameLayout implements View.OnClickListener {
    private static class Tab extends CustomRipple {
        private boolean active;
        private String identifier;
        private String label;

        public Tab(Context context) {
            super(context);
            init();
            //setBackgroundDrawable(getResources().getDrawable(R.drawable.tab_item));
        }

        public Tab(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
            //setBackgroundDrawable(getResources().getDrawable(R.drawable.tab_item));
        }

        public Tab(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            init();
            //setBackgroundDrawable(getResources().getDrawable(R.drawable.tab_item));
        }

        public void setIdentifier(String identifier)
        {
            this.identifier = identifier;
        }

        public void setLabel(String label)
        {
            this.label = label;
        }

        public String getIdentifier()
        {
            return this.identifier;
        }

        public String getLabel()
        {
            return this.label;
        }

        public void setActive(boolean value)
        {
            this.active = value;
            this.invalidate();
        }

        public boolean isActive()
        {
            return this.active;
        }

        @Override
        protected void onDraw(android.graphics.Canvas canvas) {
            Paint paint = new Paint();
            //paint.setColor(getResources().g);
            //canvas.drawRect(new Rect(0, 0, 100, 100), paint);

            if(active)
            {
                //paint.setColor(getResources().getColor(R.color.zupblue));
                //canvas.drawRect(new Rect(0, canvas.getHeight() - 5, canvas.getWidth(), canvas.getHeight()), paint);
            }

            paint.setColor(getResources().getColor(R.color.tabborder));
            canvas.drawRect(new Rect(canvas.getWidth() - 1, 10, canvas.getWidth(), canvas.getHeight() - 10), paint);

            Paint textPaint = new Paint();
            textPaint.setColor(getResources().getColor(R.color.tabtext));
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setTextSize(12 * getResources().getDisplayMetrics().density);
            textPaint.setTypeface(Typeface.DEFAULT_BOLD);

            int xPos = (canvas.getWidth() / 2);
            int yPos = (int) ((canvas.getHeight() / 2) - ((textPaint.descent() + textPaint.ascent()) / 2)) ;

            canvas.drawText(this.label.toUpperCase(), xPos, yPos, textPaint);
        }

        @Override
        public void setLayoutParams(ViewGroup.LayoutParams params) {
            super.setLayoutParams(params);
        }

        void init()
        {
            setRippleColor(this.getResources().getColor(R.color.tab_pressed));
            PAINT_ALPHA = 255;
            rippleType = 2;
        }
    }

    private static class ActiveTabIndicator extends View {
        int toX;
        int newWidth;

        public ActiveTabIndicator(Context context) {
            super(context);
        }

        public ActiveTabIndicator(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public ActiveTabIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @Override
        public void setLayoutParams(ViewGroup.LayoutParams params) {
            super.setLayoutParams(params);
        }

        public void beginMoveTo(int toX, int newWidth)
        {
            ResizeAnimation animation = new ResizeAnimation(this, this.getWidth(), 5, newWidth, 5);
            animation.setDuration(250);

            this.toX = toX;
            this.newWidth = newWidth;

            float fromX = this.getLeft();
            final int fromWidth = this.getWidth();
            float y = 0;

            TranslateAnimation animation1 = new TranslateAnimation(TranslateAnimation.ABSOLUTE, fromX, TranslateAnimation.ABSOLUTE, toX, TranslateAnimation.ABSOLUTE, y, TranslateAnimation.ABSOLUTE, y);
            animation1.setDuration(250);
            animation1.setFillAfter(false);

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) this.getLayoutParams();
            params.setMargins(0, 0, 0, 0);
            params.width = fromWidth;

            this.setLayoutParams(params);

            AnimationSet set = new AnimationSet(true);
            set.addAnimation(animation);
            set.addAnimation(animation1);
            set.setDuration(250);
            this.startAnimation(set);

            set.setFillAfter(false);
        }

        @Override
        protected void onAnimationEnd() {
            super.onAnimationEnd();

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) this.getLayoutParams();
            params.setMargins(toX, 0, 0, 0);
            params.width = newWidth;

            this.setLayoutParams(params);
        }
    }

    public static interface OnTabChangeListener {
        public void onTabChange(SingularTabHost tabHost, String oldIdentifier, String newIdentifier);
    }

    private ActiveTabIndicator activeIndicatorView;
    private HorizontalScrollView scrollView;
    private LinearLayout container;
    private ArrayList<Tab> tabs;
    private OnTabChangeListener onTabChangeListener;
    private OnLayoutChangeListener onLayoutChangeListener;

    private LayoutTask layoutTask;

    public SingularTabHost(Context context) {
        super(context);

        init();
    }

    public SingularTabHost(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SingularTabHost(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        if(!this.isInEditMode())
        {
            this.setBackgroundColor(getResources().getColor(R.color.tabhostbg));
        }

        this.scrollView = new HorizontalScrollView(this.getContext());
        this.scrollView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        this.scrollView.setHorizontalScrollBarEnabled(false);
        this.addView(scrollView);

        RelativeLayout mainContainer = new RelativeLayout(this.getContext());
        mainContainer.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        this.scrollView.addView(mainContainer);

        this.container = new LinearLayout(this.getContext());
        this.container.setOrientation(LinearLayout.HORIZONTAL);
        this.container.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mainContainer.addView(this.container);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 5);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

        this.activeIndicatorView = new ActiveTabIndicator(this.getContext());
        this.activeIndicatorView.setLayoutParams(params);
        this.activeIndicatorView.setBackgroundColor(getResources().getColor(R.color.zupblue));
        mainContainer.addView(this.activeIndicatorView);

        this.tabs = new ArrayList<Tab>();
        if(Build.VERSION.SDK_INT >= 11)
        {
            onLayoutChangeListener = new OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                    for(int j = 0; j < tabs.size(); j++)
                    {
                        updateTabWidth(tabs.get(j));
                    }
                    SingularTabHost.this.removeOnLayoutChangeListener(onLayoutChangeListener);
                }
            };
            this.addOnLayoutChangeListener(onLayoutChangeListener);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        {
            //if(layoutTask != null)
              //  layoutTask.cancel(true);

            //layoutTask = new LayoutTask();
            //layoutTask.execute();
        }
    }

    class LayoutTask extends AsyncTask<Void, Void, Boolean>
    {
        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                Thread.sleep(500);
                return true;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean a) {
            super.onPostExecute(a);

            if(a)
            {
                updateTabWidths();
            }
                //u
        }
    }

    public void updateTabWidths()
    {
        for(int j = 0; j < tabs.size(); j++)
        {
            updateTabWidth(tabs.get(j));
        }
    }

    void updateTabWidth(Tab tab)
    {
        int defaultWidth = getWidth() / tabs.size();

        Paint textPaint = new Paint();
        textPaint.setTextSize(15 * getResources().getDisplayMetrics().density);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        float textWidth = textPaint.measureText(tab.label);
        Rect bounds = new Rect();
        textPaint.getTextBounds(tab.label, 0, tab.label.length(), bounds);

        textWidth = bounds.width();
        int desiredMinWidth = (int)textWidth + (int)(20 * getResources().getDisplayMetrics().density);

        if(desiredMinWidth > defaultWidth)
        {
            defaultWidth = desiredMinWidth;
        }

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tab.getLayoutParams();
        params.width = defaultWidth;
        params.height = 44;
        params.leftMargin = 0;
        params.topMargin = 0;

        tab.setLayoutParams(params);
        if(tab == getActiveTab()) {
            if(activeIndicatorView.getAnimation() != null)
                activeIndicatorView.getAnimation().cancel();

            RelativeLayout.LayoutParams rparams = (RelativeLayout.LayoutParams) activeIndicatorView.getLayoutParams();
            rparams.width = defaultWidth - 1;
            activeIndicatorView.setLayoutParams(rparams);
        }
    }

    public void setOnTabChangeListener(OnTabChangeListener listener)
    {
        this.onTabChangeListener = listener;
    }

    public void removeAllTabs()
    {
        List<Tab> viewsToRemove = new ArrayList<Tab>();
        for(int i = 0; i < container.getChildCount(); i++)
        {
            if(!(container.getChildAt(i) instanceof Tab))
                continue;

            viewsToRemove.add((Tab)container.getChildAt(i));
        }

        for(Tab tab : viewsToRemove)
        {
            //tab.setVisibility(GONE);
            container.removeView(tab);
        }

        this.tabs.clear();
        //this.container.setWeightSum(0);
    }

    public void addTab(String identifier, String label)
    {
        Tab tab = new Tab(this.getContext());
        tab.setIdentifier(identifier);
        tab.setLabel(label);
        tab.setClickable(true);
        tab.setOnClickListener(this);

        this.container.addView(tab);
        tab.setLayoutParams(new LinearLayout.LayoutParams(200, 44));
        //this.container.setWeightSum(tabs.size() + 1);

        this.tabs.add(tab);
        if(this.tabs.size() == 1)
        {
            setActiveTab(tab);
        }

        /*for(Tab _tab : tabs) {
            updateTabWidth(_tab);
        }*/

        /*if(layoutTask != null)
            layoutTask.cancel(true);

        layoutTask = new LayoutTask();
        layoutTask.execute();*/
    }

    private void setActiveTab(Tab sender)
    {
        final Tab activetab = getActiveTab();
        String oldIdentifier = null;
        if(activetab != null)
        {
            oldIdentifier = activetab.getIdentifier();
        }

        for(int i = 0; i < this.tabs.size(); i++)
        {
            if(this.tabs.get(i) == sender)
            {
                sender.setActive(true);
            }
            else
            {
                this.tabs.get(i).setActive(false);
            }
        }

        if(sender != null) {
            final int fromWidth = this.activeIndicatorView.getWidth();
            final int toWidth = sender.getWidth() - 1;

            // Layout was calculated?
            if(toWidth > 0) {
                this.activeIndicatorView.beginMoveTo((int)sender.getX(), toWidth);
            }
        }

        if(this.onTabChangeListener != null)
        {
            this.onTabChangeListener.onTabChange(this, oldIdentifier, sender.getIdentifier());
        }
    }

    private Tab getActiveTab()
    {
        for(int i = 0; i < this.tabs.size(); i++)
        {
            if(this.tabs.get(i).isActive())
            {
                return this.tabs.get(i);
            }
        }

        return null;
    }

    @Override
    public void onClick(View view) {
        Tab sender = (Tab)view;
        setActiveTab(sender);
    }
}
