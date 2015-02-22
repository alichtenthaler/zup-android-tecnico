package com.ntxdev.zuptecnico_homologacao.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ntxdev.zuptecnico_homologacao.entities.InventoryCategory;
import com.ntxdev.zuptecnico_homologacao.entities.InventoryItem;
import com.ntxdev.zuptecnico_homologacao.entities.responses.PublishInventoryItemResponse;

import org.json.JSONObject;

import java.io.IOException;

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
        else if(response.result != null)
        {
            String message = "";
            int j = 0;
            for(String key : response.result.error.keySet())
            {
                if(j > 0)
                    message += "\r\n\r\n";

                InventoryCategory.Section.Field field = Zup.getInstance().getInventoryCategory(item.inventory_category_id).getField(key);

                String fieldName;
                if(field == null)
                    fieldName = key;
                else
                    fieldName = field.label;

                message += fieldName + "\r\n";
                int i = 0;
                for(String msg : response.result.error.get(key))
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
