package com.ntxdev.zuptecnico.entities;

import java.io.Serializable;

/**
 * Created by igorlira on 5/25/15.
 */
public class InventoryItemFilter implements Serializable
{
    public int fieldId;
    public String type;
    public Serializable value1;
    public Serializable value2;
    public boolean isArray;
}
