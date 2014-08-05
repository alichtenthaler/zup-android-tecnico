package com.ntxdev.zuptecnico.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.ntxdev.zuptecnico.R;

import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;


/**
 * Created by igorlira on 2/9/14.
 */
public class SingularTabHost extends FrameLayout implements View.OnClickListener {
    private static class Tab extends View {
        private boolean active;
        private String identifier;
        private String label;

        public Tab(Context context) {
            super(context);
            setBackgroundDrawable(getResources().getDrawable(R.drawable.tab_item));
        }

        public Tab(Context context, AttributeSet attrs) {
            super(context, attrs);
            setBackgroundDrawable(getResources().getDrawable(R.drawable.tab_item));
        }

        public Tab(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            setBackgroundDrawable(getResources().getDrawable(R.drawable.tab_item));
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
                paint.setColor(getResources().getColor(R.color.zupblue));
                canvas.drawRect(new Rect(0, canvas.getHeight() - 5, canvas.getWidth(), canvas.getHeight()), paint);
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
    }

    public static interface OnTabChangeListener {
        public void onTabChange(SingularTabHost tabHost, String oldIdentifier, String newIdentifier);
    }

    private HorizontalScrollView scrollView;
    private LinearLayout container;
    private ArrayList<Tab> tabs;
    private OnTabChangeListener onTabChangeListener;
    private OnLayoutChangeListener onLayoutChangeListener;

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

        this.container = new LinearLayout(this.getContext());
        this.container.setOrientation(LinearLayout.HORIZONTAL);
        this.container.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        this.scrollView.addView(this.container);

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

        tab.setLayoutParams(new LinearLayout.LayoutParams(defaultWidth, /*44*/ViewGroup.LayoutParams.MATCH_PARENT));
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
        tab.setLayoutParams(new LinearLayout.LayoutParams(100, 44));
        tab.setClickable(true);
        tab.setOnClickListener(this);

        this.container.addView(tab);
        //this.container.setWeightSum(tabs.size() + 1);

        this.tabs.add(tab);
        if(this.tabs.size() == 1)
        {
            setActiveTab(tab);
        }

        for(Tab _tab : tabs) {
            updateTabWidth(_tab);
        }
    }

    private void setActiveTab(Tab sender)
    {
        Tab activetab = getActiveTab();
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
