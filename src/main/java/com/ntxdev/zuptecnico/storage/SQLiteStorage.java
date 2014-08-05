package com.ntxdev.zuptecnico.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ntxdev.zuptecnico.api.Storage;
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
public class SQLiteStorage implements IStorage {
    ZupOpenHelper db;

    public SQLiteStorage(Context context)
    {
        db = new ZupOpenHelper(context);
    }

    public void setSession(int userId, String token)
    {
        db.setSession(userId, token);
    }

    public int getSessionUserId()
    {
        return db.getSessionUserId();
    }

    public String getSessionToken()
    {
        return db.getSessionToken();
    }

    @Override
    public void addUser(User user)
    {
        db.addUser(user);
    }

    @Override
    public User getUser(int id) {
        return db.getUser(id);
    }

    @Override
    public Iterator<User> getUsersIterator() {
        return db.getUsersIterator();
    }

    @Override
    public void addInventoryCategory(InventoryCategory inventoryCategory)
    {
        db.addInventoryCategory(inventoryCategory);
    }

    @Override
    public InventoryCategory getInventoryCategory(int id) {
        return db.getInventoryCategory(id);
    }

    @Override
    public boolean hasInventoryCategory(int id) {
        return db.hasInventoryCategory(id);
    }

    @Override
    public void updateInventoryCategoryInfo(int id, InventoryCategory copyFrom) {

    }

    @Override
    public Iterator<InventoryCategory> getInventoryCategoriesIterator() {
        return db.getInventoryCategoriesIterator();
    }

    @Override
    public boolean hasInventoryCategoryStatus(int id) {
        return db.hasInventoryCategoryStatus(id);
    }

    @Override
    public InventoryCategoryStatus getInventoryCategoryStatus(int id) {
        return db.getInventoryCategoryStatus(id);
    }

    @Override
    public void addInventoryCategoryStatus(InventoryCategoryStatus status)
    {
        db.addInventoryCategoryStatus(status);
    }

    @Override
    public void removeInventoryCategoryStatus(int id) {

    }

    @Override
    public Iterator<InventoryCategoryStatus> getInventoryCategoryStatusIterator() {
        return db.getInventoryItemCategoryStatusIterator();
    }

    @Override
    public Iterator<InventoryCategoryStatus> getInventoryCategoryStatusIterator(int category) {
        return db.getInventoryItemCategoryStatusIterator(category);
    }

    @Override
    public int getLocalInventoryItemCount() {
        return 0;
    }

    @Override
    public void addInventoryItem(InventoryItem inventoryItem) {
        db.addInventoryItem(inventoryItem);
    }

    @Override
    public void removeInventoryItem(int itemId) {
        db.removeInventoryItem(itemId);
    }

    @Override
    public InventoryItem getInventoryItem(int id) {
        return db.getInventoryItem(id);
    }

    @Override
    public boolean hasInventoryItem(int id) {
        return db.hasInventoryItem(id);
    }

    @Override
    public void updateInventoryItemInfo(int id, InventoryItem copyFrom) {
        db.updateInventoryItemInfo(id, copyFrom);
    }

    @Override
    public Iterator<InventoryItem> getInventoryItemsIterator() {
        return db.getInventoryItemsIterator();
    }

    public Iterator<InventoryItem> getInventoryItemsIterator(Integer categoryId, Integer stateId, String searchQuery, int page)
    {
        return db.getInventoryItemsIterator(categoryId, stateId, searchQuery, page);
    }

    @Override
    public Iterator<InventoryItem> getInventoryItemsIteratorByCategory(int categoryId) {
        return db.getInventoryItemsIteratorByCategory(categoryId);
    }

    public Iterator<Flow> getFlowIterator()
    {
        return db.getFlowIterator();
    }

    public Flow getFlow(int id)
    {
        return db.getFlow(id);
    }

    public boolean hasFlow(int id)
    {
        return db.hasFlow(id);
    }

    public void updateFlow(int id, Flow data)
    {
        db.updateFlow(id, data);
    }

    public void addFlow(Flow flow)
    {
        db.addFlow(flow);
    }
}