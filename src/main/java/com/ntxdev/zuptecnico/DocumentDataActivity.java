package com.ntxdev.zuptecnico;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.ViewGroup;

import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.ui.UIHelper;

/**
 * Created by igorlira on 2/22/14.
 */
public class DocumentDataActivity extends ActionBarActivity
{
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_documentdata);

        Zup.getInstance().initStorage(getApplicationContext());

        if (savedInstanceState == null) {
            //getSupportFragmentManager().beginTransaction()
            //        .add(R.id.container, new PlaceholderFragment())
            //        .commit();
        }

        if(Build.VERSION.SDK_INT >= 11)
        {
            //this.getActionBar().hide();
            ViewGroup actionBarLayout = (ViewGroup)getLayoutInflater().inflate(R.layout.action_bar, null);

            android.support.v7.app.ActionBar actionBar = this.getSupportActionBar();
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayUseLogoEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(actionBarLayout);
        }

        UIHelper.initActivity(this, false);
    }
}
