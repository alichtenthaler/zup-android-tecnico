package com.ntxdev.zuptecnico.api.callbacks;

import com.ntxdev.zuptecnico.entities.InventoryItem;

/**
 * Created by igorlira on 5/11/14.
 */
public interface InventoryItemPublishedListener {
    public void onInventoryItemPublished(int itemId, InventoryItem item);
}
