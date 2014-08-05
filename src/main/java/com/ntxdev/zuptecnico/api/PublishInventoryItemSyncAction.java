package com.ntxdev.zuptecnico.api;

import com.ntxdev.zuptecnico.entities.InventoryItem;

/**
 * Created by igorlira on 3/25/14.
 */
public class PublishInventoryItemSyncAction implements SyncAction {
    public InventoryItem item;

    public PublishInventoryItemSyncAction(InventoryItem item)
    {
        this.item = item;
    }

    public boolean perform()
    {
        return Zup.getInstance().publishInventoryItem(item);
    }
}
