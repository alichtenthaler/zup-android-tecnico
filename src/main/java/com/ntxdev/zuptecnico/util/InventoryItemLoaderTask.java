package com.ntxdev.zuptecnico.util;

import android.os.AsyncTask;
import android.widget.TextView;

import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.entities.InventoryItem;

/**
 * Created by igorlira on 8/8/14.
 */
public class InventoryItemLoaderTask extends AsyncTask<Integer, Void, InventoryItem>
{
    TextView textView;
    String prefix;

    int itemId;
    int categoryId;

    public InventoryItemLoaderTask(TextView textView, String prefix)
    {
        this.textView = textView;
        this.prefix = prefix;
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();
        textView.setText(prefix + "Carregando...");
    }

    @Override
    protected InventoryItem doInBackground(Integer... integers)
    {
        this.categoryId = integers[0];
        this.itemId = integers[1];
        return Zup.getInstance().retrieveSingleInventoryItemInfo(categoryId, itemId);
    }

    @Override
    protected void onPostExecute(InventoryItem item)
    {
        super.onPostExecute(item);
        if(item != null)
        {
            textView.setText(prefix + item.title);
        }
        else
        {
            textView.setText(prefix + "#" + item.id);
        }
    }
}
