package com.ntxdev.zuptecnico.api;

import com.ntxdev.zuptecnico.entities.Document;
import com.ntxdev.zuptecnico.entities.InventoryCategory;
import com.ntxdev.zuptecnico.entities.InventoryCategoryStatus;
import com.ntxdev.zuptecnico.entities.InventoryItem;
import com.ntxdev.zuptecnico.entities.User;
import com.ntxdev.zuptecnico.entities.collections.InventoryCategoryStatusCollection;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

/**
 * Created by igorlira on 2/9/14.
 */
public class Storage {
    private ArrayList<InventoryCategory> inventoryCategories;
    private ArrayList<InventoryItem> inventoryItems;
    private ArrayList<Document> documents;
    private ArrayList<User> users;
    private ArrayList<InventoryCategoryStatus> inventoryCategoryStatuses;

    public Storage() {
        this.documents = new ArrayList<Document>();
        this.inventoryCategories = new ArrayList<InventoryCategory>();
        this.inventoryItems = new ArrayList<InventoryItem>();
        this.users = new ArrayList<User>();
        this.inventoryCategoryStatuses = new ArrayList<InventoryCategoryStatus>();

        this.documents.add(new Document(1, Calendar.getInstance().getTime(), Calendar.getInstance().getTime(), Document.State.WaitingForSync));
        this.documents.add(new Document(2, Calendar.getInstance().getTime(), Calendar.getInstance().getTime(), Document.State.Pending));
        this.documents.add(new Document(3, Calendar.getInstance().getTime(), Calendar.getInstance().getTime(), Document.State.Running));
        this.documents.add(new Document(4, Calendar.getInstance().getTime(), Calendar.getInstance().getTime(), Document.State.Finished));
    }

    public void addUser(User user) {
        this.users.add(user);
    }

    public User getUser(int id) {
        Iterator<User> items = getUsersIterator();
        while (items.hasNext()) {
            User user = items.next();
            if (user.id == id) {
                return user;
            }
        }

        return null;
    }

    public Iterator<User> getUsersIterator() {
        return this.users.iterator();
    }

    public void addInventoryCategory(InventoryCategory inventoryCategory) {
        this.inventoryCategories.add(inventoryCategory);
    }

    public InventoryCategory getInventoryCategory(int id) {
        Iterator<InventoryCategory> items = getInventoryCategoriesIterator();
        while (items.hasNext()) {
            InventoryCategory category = items.next();
            if (category.id == id) {
                return category;
            }
        }

        return null;
    }

    public boolean hasInventoryCategory(int id) {
        return getInventoryCategory(id) != null;
    }

    public void updateInventoryCategoryInfo(int id, InventoryCategory copyFrom) {
        if (!hasInventoryCategory(id))
            return;

        getInventoryCategory(id).updateInfo(copyFrom);
    }

    public Iterator<InventoryCategory> getInventoryCategoriesIterator() {
        return inventoryCategories.iterator();
    }

    public boolean hasInventoryCategoryStatus(int id)
    {
        Iterator<InventoryCategoryStatus> statuses = getInventoryCategoryStatusIterator();
        while(statuses.hasNext())
        {
            InventoryCategoryStatus status = statuses.next();
            if(status.id == id)
                return true;
        }

        return false;
    }

    public InventoryCategoryStatus getInventoryCategoryStatus(int id)
    {
        Iterator<InventoryCategoryStatus> statuses = getInventoryCategoryStatusIterator();
        while(statuses.hasNext())
        {
            InventoryCategoryStatus status = statuses.next();
            if(status.id == id)
                return status;
        }

        return null;
    }

    public void addInventoryCategoryStatus(InventoryCategoryStatus status)
    {
        if(hasInventoryCategoryStatus(status.id))
            return;

        this.inventoryCategoryStatuses.add(status);
    }

    public void removeInventoryCategoryStatus(int id)
    {
        InventoryCategoryStatus status = getInventoryCategoryStatus(id);
        if(status == null)
            return;

        this.inventoryCategoryStatuses.remove(status);
    }

    public Iterator<InventoryCategoryStatus> getInventoryCategoryStatusIterator()
    {
        return this.inventoryCategoryStatuses.iterator();
    }

    public Iterator<InventoryCategoryStatus> getInventoryCategoryStatusIterator(int category)
    {
        ArrayList<InventoryCategoryStatus> statuses = new ArrayList<InventoryCategoryStatus>();
        Iterator<InventoryCategoryStatus> statusIterator = getInventoryCategoryStatusIterator();
        while(statusIterator.hasNext())
        {
            InventoryCategoryStatus status = statusIterator.next();
            if(status.inventory_category_id == category)
                statuses.add(status);
        }

        return statuses.iterator();
    }

    public int getLocalInventoryItemCount()
    {
        int count = 0;
        Iterator<InventoryItem> items = getInventoryItemsIterator();
        while(items.hasNext())
        {
            InventoryItem item = items.next();
            if(item.isLocal)
                count++;
        }

        return count;
    }

    public void addInventoryItem(InventoryItem inventoryItem)
    {
        this.inventoryItems.add(inventoryItem);
    }

    public void removeInventoryItem(int itemId)
    {
        InventoryItem item = getInventoryItem(itemId);
        if(item != null)
            this.inventoryItems.remove(item);
    }

    public InventoryItem getInventoryItem(int id)
    {
        Iterator<InventoryItem> items = getInventoryItemsIterator();
        while(items.hasNext())
        {
            InventoryItem item = items.next();
            if(item.id == id)
            {
                return item;
            }
        }

        return null;
    }

    public boolean hasInventoryItem(int id)
    {
        return getInventoryItem(id) != null;
    }

    public void updateInventoryItemInfo(int id, InventoryItem copyFrom)
    {
        if(!hasInventoryItem(id))
            return;

        getInventoryItem(id).updateInfo(copyFrom);
    }

    public Iterator<InventoryItem> getInventoryItemsIterator()
    {
        return inventoryItems.iterator();
    }

    public Iterator<InventoryItem> getInventoryItemsIteratorByCategory(int categoryId)
    {
        ArrayList<InventoryItem> result = new ArrayList<InventoryItem>();
        for(InventoryItem item : this.inventoryItems)
        {
            if(item.inventory_category_id == categoryId)
            {
                result.add(item);
            }
        }

        return result.iterator();
    }

    public Iterator<Document> getDocumentsIterator()
    {
        return documents.iterator();
    }

    public Iterator<Document> getDocumentsIterator(Document.State state)
    {
        ArrayList<Document> result = new ArrayList<Document>();
        for(Document doc : documents)
        {
            if(doc.getState() == state)
            {
                result.add(doc);
            }
        }

        return result.iterator();
    }

    public Document getDocument(int id)
    {
        for(Document doc : documents)
        {
            if(doc.getId() == id)
            {
                return doc;
            }
        }

        return null;
    }
}
