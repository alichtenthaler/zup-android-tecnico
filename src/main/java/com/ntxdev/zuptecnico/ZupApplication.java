package com.ntxdev.zuptecnico;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.ntxdev.zuptecnico.api.Zup;

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
        Zup.getInstance().initStorage(context);
    }

    @Override
    public void onTerminate() {
        Log.e("APP", "Application is being closed");

        Zup.getInstance().close();
        super.onTerminate();
    }

    public static Context getContext()
    {
        return context;
    }
}
