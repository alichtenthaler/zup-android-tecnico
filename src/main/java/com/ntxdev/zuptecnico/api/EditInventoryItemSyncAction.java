package com.ntxdev.zuptecnico.api;

import android.util.JsonReader;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ntxdev.zuptecnico.entities.InventoryItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;

/**
 * Created by igorlira on 3/25/14.
 */
public class EditInventoryItemSyncAction extends SyncAction {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Serializer
    {
        public InventoryItem item;

        public Serializer() { }
    }

    public InventoryItem item;

    public EditInventoryItemSyncAction(InventoryItem item)
    {
        this.item = item;
    }

    public EditInventoryItemSyncAction(JSONObject object, ObjectMapper mapper) throws IOException
    {
        Serializer serializer = mapper.readValue(object.toString(), Serializer.class);

        this.item = serializer.item;
    }

    public boolean onPerform() {
        return Zup.getInstance().editInventoryItem(item);
    }

    @Override
    protected JSONObject serialize() throws Exception {
        Serializer serializer = new Serializer();
        serializer.item = this.item;

        ObjectMapper mapper = new ObjectMapper();
        String res = mapper.writeValueAsString(serializer);

        return new JSONObject(res);
    }
}
