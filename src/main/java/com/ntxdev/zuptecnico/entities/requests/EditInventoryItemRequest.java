package com.ntxdev.zuptecnico.entities.requests;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Created by igorlira on 3/17/14.
 */
public class EditInventoryItemRequest {
    public static class Data
    {
        public int field_id;
        public Object content;
    }

    public Hashtable<String, Object> data;
    public Integer inventory_status_id;
}
