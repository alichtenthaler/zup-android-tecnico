package com.ntxdev.zuptecnico.storage;

import com.ntxdev.zuptecnico.api.SyncAction;
import com.ntxdev.zuptecnico.entities.Document;
import com.ntxdev.zuptecnico.entities.Flow;
import com.ntxdev.zuptecnico.entities.InventoryCategory;
import com.ntxdev.zuptecnico.entities.InventoryCategoryStatus;
import com.ntxdev.zuptecnico.entities.InventoryItem;
import com.ntxdev.zuptecnico.entities.User;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by igorlira on 5/19/14.
 */
public interface IStorage {
    public void setSession(int userId, String token);

    public int getSessionUserId();

    public String getSessionToken();

    public void addUser(User user);

    public User getUser(int id);

    public Iterator<User> getUsersIterator();

    public void addInventoryCategory(InventoryCategory inventoryCategory);

    public InventoryCategory getInventoryCategory(int id);

    public boolean hasInventoryCategory(int id);

    public void updateInventoryCategoryInfo(int id, InventoryCategory copyFrom);

    public Iterator<InventoryCategory> getInventoryCategoriesIterator();

    public boolean hasInventoryCategoryStatus(int id);

    public InventoryCategoryStatus getInventoryCategoryStatus(int id);

    public void addInventoryCategoryStatus(InventoryCategoryStatus status);

    public void removeInventoryCategoryStatus(int id);

    public void removeInventoryCategory(int id);

    public Iterator<InventoryCategoryStatus> getInventoryCategoryStatusIterator();

    public Iterator<InventoryCategoryStatus> getInventoryCategoryStatusIterator(int category);

    public Iterator<InventoryItem> getInventoryItemsIterator(Integer categoryId, Integer stateId, String searchQuery, int page);

    public int getLocalInventoryItemCount();

    public void addInventoryItem(InventoryItem inventoryItem);

    public void removeInventoryItem(int itemId);

    public InventoryItem getInventoryItem(int id);

    public boolean hasInventoryItem(int id);

    public void updateInventoryItemInfo(int id, InventoryItem copyFrom);

    public Iterator<InventoryItem> getInventoryItemsIterator();

    public Iterator<InventoryItem> getInventoryItemsIteratorByCategory(int categoryId);

    public Iterator<Flow> getFlowIterator();

    public Flow getFlow(int id);

    public boolean hasFlow(int id);

    public void updateFlow(int id, Flow data);

    public void addFlow(Flow flow);

    public void clear();

    public void addSyncAction(SyncAction action);

    public void removeSyncAction(int id);

    public Iterator<SyncAction> getSyncActionIterator();

    public int getSyncActionCount();

    public void resetSyncActions();

    public void updateSyncAction(SyncAction action);
}
