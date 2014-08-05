package com.ntxdev.zuptecnico.entities.collections;

import com.ntxdev.zuptecnico.entities.InventoryItem;

/**
 * Created by igorlira on 3/3/14.
 */
public class InventoryItemCollection
{
    public int request_inventory_category_id;
    public InventoryItem[] items;
    public String etag;
    public String error;
}
