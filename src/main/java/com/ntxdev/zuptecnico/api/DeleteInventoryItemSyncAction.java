package com.ntxdev.zuptecnico.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ntxdev.zuptecnico.entities.InventoryItem;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by igorlira on 3/25/14.
 */
public class DeleteInventoryItemSyncAction extends SyncAction {
    public int categoryId;
    public int itemId;

    public DeleteInventoryItemSyncAction(JSONObject object, ObjectMapper mapper) throws JSONException
    {
        this.categoryId = object.getInt("category_id");
        this.itemId = object.getInt("item_id");
    }

    public DeleteInventoryItemSyncAction(int categoryId, int id) {
        this.categoryId = categoryId;
        this.itemId = id;
    }

    public boolean onPerform() {

        boolean result = Zup.getInstance().deleteInventoryItem(categoryId, itemId);
        if (result)
            Zup.getInstance().removeInventoryItem(itemId);

        return result;
    }

    @Override
    protected JSONObject serialize() throws JSONException
    {
        JSONObject result = new JSONObject();
        result.put("category_id", categoryId);
        result.put("item_id", itemId);

        return result;
    }
}
