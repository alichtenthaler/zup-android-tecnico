package com.ntxdev.zuptecnico;

import android.app.Application;
import android.content.Context;

/**
 * Created by igorlira on 12/30/14.
 */
public class ZupApplication extends Application
{
    private static Context context;

    @Override
    public void onCreate()
    {
        super.onCreate();

        context = getApplicationContext();
    }

    public static Context getContext()
    {
        return context;
    }
}
