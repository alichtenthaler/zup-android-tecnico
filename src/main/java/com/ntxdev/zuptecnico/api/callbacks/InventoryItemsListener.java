package com.ntxdev.zuptecnico.api.callbacks;

import com.ntxdev.zuptecnico.entities.InventoryItem;

import java.util.Calendar;

/**
 * Created by igorlira on 3/18/14.
 */
public interface InventoryItemsListener {
    public void onInventoryItemsReceived(InventoryItem[] items, int page, int per_page, int[] inventory_category_ids, String address, String title, Calendar creation_from, Calendar creation_to, Calendar modification_from, Calendar modification_to, Float latitude, Float longitude, int job_id);
    public void onInventoryItemsReceived(InventoryItem[] items, int categoryId, int page, int job_id);
    public void onInventoryItemsReceived(InventoryItem[] items, double latitude, double longitude, double radius, double zoom, int job_id);
}
