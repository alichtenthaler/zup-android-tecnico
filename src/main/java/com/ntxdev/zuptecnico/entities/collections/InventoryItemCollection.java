package com.ntxdev.zuptecnico.entities.collections;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ntxdev.zuptecnico.entities.InventoryItem;
import com.ntxdev.zuptecnico.entities.MapCluster;

/**
 * Created by igorlira on 3/3/14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class InventoryItemCollection
{
    public int request_inventory_category_id;
    public InventoryItem[] items;
    public MapCluster[] clusters;
    public String etag;
    public String error;
}
