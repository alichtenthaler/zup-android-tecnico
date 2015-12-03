package com.ntxdev.zuptecnico.entities.requests;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * Created by igorlira on 7/23/15.
 */
public class CreateReportItemRequest {
    private int categoryId;
    private int inventoryItemId;
    private String description;
    private String reference;
    private String[] images;
    private int userId;

    @JsonGetter("category_id")
    public int getCategoryId() {
        return categoryId;
    }

    @JsonSetter("category_id")
    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    @JsonGetter("inventory_item_id")
    public int getInventoryItemId() {
        return inventoryItemId;
    }

    @JsonSetter("inventory_item_id")
    public void setInventoryItemId(int inventoryItemId) {
        this.inventoryItemId = inventoryItemId;
    }

    @JsonGetter("description")
    public String getDescription() {
        return description;
    }

    @JsonSetter("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @JsonGetter("reference")
    public String getReference() {
        return reference;
    }

    @JsonSetter("reference")
    public void setReference(String reference) {
        this.reference = reference;
    }

    @JsonGetter("image")
    public String[] getImages() {
        return images;
    }

    @JsonSetter("images")
    public void setImages(String[] images) {
        this.images = images;
    }

    @JsonGetter("user_id")
    public int getUserId() {
        return userId;
    }

    @JsonSetter("user_id")
    public void setUserId(int userId) {
        this.userId = userId;
    }
}
