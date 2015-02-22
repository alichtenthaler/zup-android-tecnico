package com.ntxdev.zuptecnico.api;

import com.ntxdev.zuptecnico.entities.InventoryItem;

import java.util.ArrayList;

/**
 * Created by igorlira on 3/23/14.
 */
public class ZupCache {
    private static ArrayList<InventoryItem> inventoryItems = new ArrayList<InventoryItem>();

    public static boolean hasInventoryItem(int id)
    {
        for(int i = 0; i < inventoryItems.size(); i++)
        {
            if(inventoryItems.get(i).id == id)
                return true;
        }

        return false;
    }

    public static InventoryItem getInventoryItem(int id)
    {
        for(int i = 0; i < inventoryItems.size(); i++)
        {
            if(inventoryItems.get(i).id == id)
                return inventoryItems.get(i);
        }

        return null;
    }

    public static void addInventoryItem(InventoryItem item)
    {
        if(hasInventoryItem(item.id))
            return;

        inventoryItems.add(item);
    }

    public static void removeInventoryItem(int id)
    {
        if(!hasInventoryItem(id))
            return;

        InventoryItem item = getInventoryItem(id);
        inventoryItems.remove(item);
    }
}
