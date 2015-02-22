package com.ntxdev.zuptecnico.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ntxdev.zuptecnico.entities.InventoryItem;
import com.ntxdev.zuptecnico.entities.responses.EditInventoryItemResponse;

import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by igorlira on 3/25/14.
 */
public class EditInventoryItemSyncAction extends SyncAction {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Serializer
    {
        public InventoryItem item;
        public String error;

        public Serializer() { }
    }

    public InventoryItem item;
    String error;

    public EditInventoryItemSyncAction(InventoryItem item)
    {
        this.item = item;
    }

    public EditInventoryItemSyncAction(JSONObject object, ObjectMapper mapper) throws IOException
    {
        Serializer serializer = mapper.readValue(object.toString(), Serializer.class);

        this.item = serializer.item;
        this.error = serializer.error;
    }

    public boolean onPerform() {
        ApiHttpResult<EditInventoryItemResponse> response = Zup.getInstance().editInventoryItem(item);
        if(response.statusCode == 200 || response.statusCode == 201)
        {
            error = null;
            return true;
        }
        else if(response.result != null)
        {
            error = response.result.error;
            return false;
        }
        else
        {
            error = "Sem conexão com a internet.";
            return false;
        }
    }

    @Override
    protected JSONObject serialize() throws Exception {
        Serializer serializer = new Serializer();
        serializer.item = this.item;
        serializer.error =  getError();

        ObjectMapper mapper = new ObjectMapper();
        String res = mapper.writeValueAsString(serializer);

        return new JSONObject(res);
    }

    @Override
    public String getError() {
        return error;
    }
}
