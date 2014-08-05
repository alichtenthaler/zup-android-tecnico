package com.ntxdev.zuptecnico.api;

import com.ntxdev.zuptecnico.entities.InventoryItem;

/**
 * Created by igorlira on 3/25/14.
 */
public class DeleteInventoryItemSyncAction implements SyncAction {
    public int categoryId;
    public int itemId;

    public DeleteInventoryItemSyncAction(int categoryId, int id)
    {
        this.categoryId = categoryId;
        this.itemId = id;
    }

    public boolean perform()
    {
        boolean result = Zup.getInstance().deleteInventoryItem(categoryId, itemId);
        if(result)
            Zup.getInstance().removeInventoryItem(itemId);

        return result;
    }
}
