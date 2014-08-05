package com.ntxdev.zuptecnico.api.callbacks;

import com.ntxdev.zuptecnico.entities.InventoryItem;

/**
 * Created by igorlira on 3/18/14.
 */
public interface InventoryItemListener {
    public void onInventoryItemReceived(InventoryItem item, int categoryId, int page, int job_id);
}
