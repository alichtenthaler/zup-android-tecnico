package com.ntxdev.zuptecnico.entities.requests;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * Created by igorlira on 7/23/15.
 */
public class CreateArbitraryReportItemRequest {
    private double latitude;
    private double longitude;
    @JsonProperty("category_id")
    private int categoryId;
    private String description;
    private String reference;
    private String address;
    private String district;
    private String city;
    private String state;
    private String country;
    private String[] images;
    private Integer userId;

    @JsonGetter("latitude")
    public double getLatitude() {
        return latitude;
    }

    @JsonSetter("latitude")
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    @JsonGetter("longitude")
    public double getLongitude() {
        return longitude;
    }

    @JsonSetter("longitude")
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @JsonGetter("categoryId")
    public int getCategoryId() {
        return categoryId;
    }

    @JsonSetter("categoryId")
    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
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

    @JsonGetter("address")
    public String getAddress() {
        return address;
    }

    @JsonSetter("address")
    public void setAddress(String address) {
        this.address = address;
    }

    @JsonGetter("district")
    public String getDistrict() {
        return district;
    }

    @JsonSetter("district")
    public void setDistrict(String district) {
        this.district = district;
    }

    @JsonGetter("city")
    public String getCity() {
        return city;
    }

    @JsonSetter("city")
    public void setCity(String city) {
        this.city = city;
    }

    @JsonGetter("state")
    public String getState() {
        return state;
    }

    @JsonSetter("state")
    public void setState(String state) {
        this.state = state;
    }

    @JsonGetter("country")
    public String getCountry() {
        return country;
    }

    @JsonSetter("country")
    public void setCountry(String country) {
        this.country = country;
    }

    @JsonGetter("images")
    public String[] getImages() {
        return images;
    }

    @JsonSetter("images")
    public void setImages(String[] images) {
        this.images = images;
    }

    @JsonGetter("user_id")
    public Integer getUserId() {
        return userId;
    }

    @JsonSetter("user_id")
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
}
