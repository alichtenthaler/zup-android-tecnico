package com.ntxdev.zuptecnico.api.notifications;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by igorlira on 3/18/14.
 */
public class ZupNotificationCenter {
    private ArrayList<ZupNotificationListener> listeners;

    public ZupNotificationCenter()
    {
        listeners = new ArrayList<ZupNotificationListener>();
    }

    public void subscribe(ZupNotificationListener listener)
    {
        if(!listeners.contains(listener))
            listeners.add(listener);
    }

    public void post(final ZupNotification notification)
    {
        Iterator<ZupNotificationListener> listenerIterator = listeners.iterator();
        while(listenerIterator.hasNext())
        {
            final ZupNotificationListener listener = listenerIterator.next();
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    listener.onNotificationReceived(notification);
                }
            });
        }
    }

    private void runOnMainThread(Runnable runnable)
    {
        new Handler(Looper.getMainLooper()).post(runnable);
    }
}
