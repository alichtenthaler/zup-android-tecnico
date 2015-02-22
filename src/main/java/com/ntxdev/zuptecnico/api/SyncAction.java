package com.ntxdev.zuptecnico.api;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.content.LocalBroadcastManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ntxdev.zuptecnico.ZupApplication;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by igorlira on 3/25/14.
 */
public abstract class SyncAction implements Serializable
{
    static final int SYNCACTION_CREATE_ITEM = 0;
    static final int SYNCACTION_EDIT_ITEM = 1;
    static final int SYNCACTION_DELETE_ITEM = 2;

    int id;
    boolean running = false;
    boolean pending = true;
    boolean successful = false;
    Date date;

    public static final String ACTION_SYNC_CHANGED = "zuptecnico.sync_changed";
    public static final String ACTION_SYNC_BEGIN = "zuptecnico.sync_begin";
    public static final String ACTION_SYNC_END = "zuptecnico.sync_end";

    public SyncAction()
    {
        this.date = Calendar.getInstance().getTime();
    }

    public SyncAction(JSONObject info)
    {

    }

    public static SyncAction load(Cursor cursor, ObjectMapper mapper) throws Exception
    {
        // id, date, type, info, pending, running, successful
        int id = cursor.getInt(0);
        long date = cursor.getLong(1);
        int type = cursor.getInt(2);
        String info = cursor.getString(3);
        boolean pending = cursor.getInt(4) == 1;
        boolean running = cursor.getInt(5) == 1;
        boolean successful = cursor.getInt(6) == 1;

        JSONObject info_o = new JSONObject(info);

        SyncAction action;
        if(type == SYNCACTION_CREATE_ITEM)
        {
            action = new PublishInventoryItemSyncAction(info_o, mapper);
        }
        else if(type == SYNCACTION_EDIT_ITEM)
        {
            action = new EditInventoryItemSyncAction(info_o, mapper);
        }
        else if(type == SYNCACTION_DELETE_ITEM)
        {
            action = new DeleteInventoryItemSyncAction(info_o, mapper);
        }
        else
            throw new Exception("Invalid sync action type");

        action.running = running;
        action.pending = pending;
        action.successful = successful;

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date);
        action.date = cal.getTime();

        action.id = id;

        return action;
    }

    public int getId()
    {
        return id;
    }

    public ContentValues save()
    {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            long dt = calendar.getTimeInMillis();

            ContentValues values = new ContentValues();
            values.put("date", dt);
            values.put("type", getType());
            values.put("info", serialize().toString());
            values.put("pending", pending ? 1 : 0);
            values.put("running", running ? 1 : 0);
            values.put("successful", successful ? 1 : 0);

            return values;
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public boolean perform()
    {
        this.pending = false;
        this.running = true;
        this.broadcastChange();

        this.successful = this.onPerform();
        this.running = false;
        this.broadcastChange();

        return this.successful;
    }

    private void broadcastChange()
    {
        Zup.getInstance().updateSyncAction(this);

        Intent intent = new Intent(SyncAction.ACTION_SYNC_CHANGED);
        intent.putExtra("sync_action", this);

        if(ZupApplication.getContext() == null)
            return;

        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(ZupApplication.getContext());
        manager.sendBroadcast(intent);
    }

    public boolean isRunning()
    {
        return running;
    }

    public boolean wasSuccessful()
    {
        return successful;
    }

    public boolean isPending()
    {
        return pending;
    }

    protected abstract boolean onPerform();
    protected abstract JSONObject serialize() throws Exception;

    public abstract String getError();

    public Date getDate()
    {
        return this.date;
    }

    public int getType()
    {
        if(this instanceof PublishInventoryItemSyncAction)
            return SYNCACTION_CREATE_ITEM;
        else if(this instanceof EditInventoryItemSyncAction)
            return SYNCACTION_EDIT_ITEM;
        else if(this instanceof DeleteInventoryItemSyncAction)
            return SYNCACTION_DELETE_ITEM;
        else
            return -1;
    }
}
