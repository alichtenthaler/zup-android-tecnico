package com.ntxdev.zuptecnico.ui;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.hardware.display.DisplayManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.internal.widget.ScrollingTabContainerView;
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
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;


import com.ntxdev.zuptecnico.R;
import com.ntxdev.zuptecnico.util.ResizeAnimation;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by igorlira on 2/9/14.
 */
public class SingularTabHost extends FrameLayout implements View.OnClickListener
{
    public static class Tab
    {
        public TabViewController viewController;
        public String identifier;
        public String text;
        public boolean isActive;
    }

    public static class TabViewController
    {
        public ViewGroup tab;

        public TabViewController(ViewGroup tab)
        {
            this.tab = tab;
            init();
        }

        void init()
        {

        }
    }

    private TabHost tabHost;

    private ArrayList<TabViewController> tabs;
    private LinearLayout container;

    public static interface OnTabChangeListener {
        public void onTabChange(SingularTabHost tabHost, String oldIdentifier, String newIdentifier);
    }

    private OnTabChangeListener onTabChangeListener;

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

        LinearLayout layout = new LinearLayout(getContext());

        TabWidget tabWidget = new TabWidget(getContext());
        tabWidget.setId(android.R.id.tabs);
        //tabWidget.setStripEnabled(true);
        tabWidget.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        layout.addView(tabWidget);

        FrameLayout frameLayout2 = new FrameLayout(getContext());
        frameLayout2.setId(android.R.id.tabcontent);
        frameLayout2.setVisibility(GONE);
        layout.addView(frameLayout2);

        View view = new View(getContext());
        view.setId(R.id.txt_login);
        frameLayout2.addView(view);

        this.tabHost = new TabHost(getContext(), null);
        this.tabHost.addView(layout);
        this.addView(this.tabHost);
        this.tabHost.setup();

        this.tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String s) {
                if(SingularTabHost.this.onTabChangeListener != null)
                    SingularTabHost.this.onTabChangeListener.onTabChange(SingularTabHost.this, null, s);
            }
        });

        /*this.tabs = new ArrayList<TabViewController>();

        this.container = new LinearLayout(this.getContext());
        this.addView(container);*/
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        {
            //if(layoutTask != null)
              //  layoutTask.cancel(true);

            //layoutTask = new LayoutTask();
            //layoutTask.execute();

            //adjustWidths();
        }
    }

    public void setOnTabChangeListener(OnTabChangeListener listener)
    {
        this.onTabChangeListener = listener;
    }

    public void removeAllTabs()
    {
        this.tabHost.clearAllTabs();
    }

    public void addTab(String identifier, String label)
    {
        /*LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);

        CustomRipple tabView = new CustomRipple(getContext());
        tabView.setLayoutParams(params);
        this.container.addView(tabView);

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        lp.addRule(RelativeLayout.CENTER_VERTICAL);
        lp.setMargins(30, 0, 30, 0);

        TextView lbl = new TextView(getContext());
        lbl.setText(label);
        lbl.setLayoutParams(lp);
        lbl.setTypeface(Typeface.DEFAULT_BOLD);
        tabView.addView(lbl);

        TabViewController controller = new TabViewController(tabView);
        this.tabs.add(controller);

        adjustWidths();*/

        int hashCode = 9923;
        if(identifier != null)
            hashCode = identifier.hashCode();

        View tabView = new View(getContext());
        tabView.setId(1337 + hashCode);
        ((ViewGroup)this.tabHost.findViewById(android.R.id.tabcontent)).addView(tabView);

        TabHost.TabSpec spec = this.tabHost.newTabSpec(identifier);
        spec.setIndicator(label);
        spec.setContent(tabView.getId());
        this.tabHost.addTab(spec);

    }

    void adjustWidths()
    {
        int totalWidth = 0;
        for(TabViewController tvc : this.tabs)
        {
            totalWidth += tvc.tab.getWidth();
        }

        if(totalWidth < this.getWidth())
        {
            int newWidth = this.getWidth() / this.tabs.size();

            for(TabViewController tvc : this.tabs)
            {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tvc.tab.getLayoutParams();
                params.width = newWidth;

                tvc.tab.setLayoutParams(params);
            }
        }
    }

    private void setActiveTab(Tab sender)
    {


        if(this.onTabChangeListener != null)
        {
            //this.onTabChangeListener.onTabChange(this, oldIdentifier, sender.getIdentifier());
        }
    }

    private Tab getActiveTab()
    {
        return null;
    }

    @Override
    public void onClick(View view) {
        //Tab sender = (Tab)view;
        //setActiveTab(sender);
    }
}
