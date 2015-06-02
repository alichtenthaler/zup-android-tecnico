package com.ntxdev.zuptecnico.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ntxdev.zuptecnico.entities.responses.DeleteInventoryItemResponse;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by igorlira on 3/25/14.
 */
public class DeleteInventoryItemSyncAction extends SyncAction {
    public int categoryId;
    public int itemId;
    public String error;

    public DeleteInventoryItemSyncAction(JSONObject object, ObjectMapper mapper) throws JSONException
    {
        this.categoryId = object.getInt("category_id");
        this.itemId = object.getInt("item_id");
        this.inventory_item_id = this.itemId;

        if(object.has("error"))
            this.error = object.getString("error");
    }

    public DeleteInventoryItemSyncAction(int categoryId, int id) {
        this.categoryId = categoryId;
        this.itemId = id;
        this.inventory_item_id = id;
    }

    public boolean onPerform() {

        ApiHttpResult<DeleteInventoryItemResponse> result = Zup.getInstance().deleteInventoryItem(categoryId, itemId);
        if (result.statusCode == 200 || result.statusCode == 201)
        {
            error = null;
            Zup.getInstance().removeInventoryItem(itemId);
            return true;
        }
        else if (result.result != null)
        {
            error = result.result.error;
            return false;
        }
        else
        {
            error = "Sem conexão com a internet.";
            return false;
        }
    }

    @Override
    public String getError() {
        return error;
    }

    @Override
    protected JSONObject serialize() throws JSONException
    {
        JSONObject result = new JSONObject();
        result.put("category_id", categoryId);
        result.put("item_id", itemId);
        result.put("error", error);

        return result;
    }
}
