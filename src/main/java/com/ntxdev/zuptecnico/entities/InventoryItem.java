package com.ntxdev.zuptecnico.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by igorlira on 3/3/14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class InventoryItem {
    public static final int LOCAL_MASK = 0x80000000;

    public InventoryItem()
    {
        this.data = new ArrayList<Data>();
    }

    public int id;
    public String title;
    public Coordinates position;
    public Integer inventory_category_id;
    public Integer inventory_status_id;
    public ArrayList<Data> data;
    public String created_at;
    public String address;

    @JsonIgnore
    public boolean isLocal;
    @JsonIgnore
    public boolean syncError;
    @JsonIgnoreProperties
    public int publishToken;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Coordinates
    {
        public float latitude;
        public float longitude;
    }

    //@JsonIgnoreProperties(ignoreUnknown = true)
    public static class Data
    {
        public int id;
        public InventoryCategory.Section.Field field;
        //public int inventory_field_id;
        public Object content;

        // ------------- //
        @JsonIgnore
        private int _fieldId;

        public int getFieldId()
        {
            if(field != null)
                return field.id;
            else
                return _fieldId;
        }

        public void setFieldId(int id)
        {
            _fieldId = id;
        }
    }

    public void updateInfo(InventoryItem copyFrom)
    {
        this.id = copyFrom.id;
        if(copyFrom.title != null)
            this.title = copyFrom.title;
        if(copyFrom.position != null)
            this.position = copyFrom.position;
        if(copyFrom.inventory_category_id != null)
            this.inventory_category_id = copyFrom.inventory_category_id;
        if(copyFrom.data != null)
            this.data = copyFrom.data;
        if(copyFrom.created_at != null)
            this.created_at = copyFrom.created_at;
    }

    public Object getFieldValue(int id)
    {
        for(int i = 0; i < this.data.size(); i++)
        {
            if(this.data.get(i).getFieldId() == id)
            {
                return this.data.get(i).content;
            }
        }

        return null;
    }

    public void setFieldValue(int id, Object value)
    {
        Data found = null;
        for(int i = 0; i < this.data.size(); i++)
        {
            if(this.data.get(i).getFieldId() == id)
            {
                found = this.data.get(i);
            }
        }

        if(found == null)
        {
            found = new Data();

            //found.inventory_field_id = id;
            found.setFieldId(id);
            data.add(found);
        }

        found.content = value;
    }
}
