package com.ntxdev.zuptecnico.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ntxdev.zuptecnico.entities.InventoryCategory;
import com.ntxdev.zuptecnico.entities.InventoryItem;
import com.ntxdev.zuptecnico.entities.responses.PublishInventoryItemResponse;

import org.json.JSONObject;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by igorlira on 3/25/14.
 */
public class PublishInventoryItemSyncAction extends SyncAction {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Serializer
    {
        public InventoryItem item;
        public String error;

        public Serializer() { }
    }

    public InventoryItem item;
    String error;

    public PublishInventoryItemSyncAction(InventoryItem item)
    {
        this.item = item;
    }

    public PublishInventoryItemSyncAction(JSONObject object, ObjectMapper mapper) throws IOException
    {
        Serializer serializer = mapper.readValue(object.toString(), Serializer.class);

        this.item = serializer.item;
        this.error = serializer.error;
    }

    public boolean onPerform()
    {
        ApiHttpResult<PublishInventoryItemResponse> response = Zup.getInstance().publishInventoryItem(item);

        if(response.statusCode == 200 || response.statusCode == 201)
        {
            error = null;
            return true;
        }
        else if(response.result != null && response.result.error instanceof Map)
        {
            String message = "";
            int j = 0;
            for(Object key : ((Map)response.result.error).keySet())
            {
                if(j > 0)
                    message += "\r\n\r\n";

                InventoryCategory.Section.Field field = Zup.getInstance().getInventoryCategory(item.inventory_category_id).getField(key.toString());

                String fieldName;
                if(field == null)
                    fieldName = key.toString();
                else
                    fieldName = field.label;

                message += fieldName + "\r\n";

                List lst = (List)((Map)response.result.error).get(key.toString());
                int i = 0;
                for(Object msg : lst)
                {
                    if(i > 0)
                        message += "\r\n";

                    message += " - " + msg;
                    i++;
                }

                j++;
            }

            error = message;
            return false;
        }
        else if(response.result != null && response.result.error != null)
        {
            error = response.result.error.toString();
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
        serializer.error = this.getError();

        ObjectMapper mapper = new ObjectMapper();
        String res = mapper.writeValueAsString(serializer);

        return new JSONObject(res);
    }

    @Override
    public String getError() {
        return error;
    }
}
