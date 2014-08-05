package com.ntxdev.zuptecnico.entities.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ntxdev.zuptecnico.entities.InventoryItem;

/**
 * Created by igorlira on 3/17/14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeleteInventoryItemResponse {
    public String error;
}
