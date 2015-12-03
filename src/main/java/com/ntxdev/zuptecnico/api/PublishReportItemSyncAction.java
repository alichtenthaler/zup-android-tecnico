package com.ntxdev.zuptecnico.api;

import android.content.Intent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ntxdev.zuptecnico.entities.User;
import com.ntxdev.zuptecnico.entities.collections.SingleReportItemCollection;
import com.ntxdev.zuptecnico.entities.requests.CreateArbitraryReportItemRequest;
import com.ntxdev.zuptecnico.entities.requests.CreateReportItemRequest;
import com.ntxdev.zuptecnico.entities.requests.CreateUserRequest;

import org.json.JSONObject;

import java.io.IOException;

import retrofit.RetrofitError;

/**
 * Created by igorlira on 7/13/15.
 */
public class PublishReportItemSyncAction extends SyncAction
{
    public static final String REPORT_PUBLISHED = "report_published";

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Serializer
    {
        public boolean isArbitraryPosition;

        public Double latitude;
        public Double longitude;
        public int categoryId;
        public Integer inventoryItemId;
        public String description;
        public String reference;
        public String address;
        public String district;
        public String city;
        public String state;
        public String country;
        public String[] images;

        public User user;

        public String error;

        public Serializer() { }
    }

    public boolean isArbitraryPosition;
    public Double latitude;
    public Double longitude;
    public int categoryId;
    public Integer inventoryItemId;
    public String description;
    public String reference;
    public String address;
    public String district;
    public String city;
    public String state;
    public String country;
    public String[] images;
    public User user;
    String error;

    public PublishReportItemSyncAction(double latitude, double longitude, int categoryId,
                                       String description, String reference, String address,
                                       String district, String city, String state, String country,
                                       String[] images, User user)
    {
        init(latitude, longitude, categoryId, description, reference, address, district, city,
                state, country, images, user);
    }

    public PublishReportItemSyncAction(int inventoryItemId, int categoryId, String description,
                                       String reference, String[] images, User user)
    {
        init(inventoryItemId, categoryId, description, reference, images, user);
    }

    public PublishReportItemSyncAction(JSONObject object, ObjectMapper mapper) throws IOException
    {
        Serializer serializer = mapper.readValue(object.toString(), Serializer.class);

        if(serializer.isArbitraryPosition)
        {
            init(serializer.latitude, serializer.longitude, serializer.categoryId, serializer.description,
                 serializer.reference, serializer.address, serializer.district, serializer.city,
                 serializer.state, serializer.country, serializer.images, serializer.user);
        }
        else
        {
            init(serializer.inventoryItemId, serializer.categoryId, serializer.description,
                 serializer.reference, serializer.images, serializer.user);
        }
        this.error = serializer.error;
    }

    void init(double latitude, double longitude, int categoryId, String description,
              String reference, String address, String district, String city, String state,
              String country, String[] images, User user)
    {
        this.isArbitraryPosition = true;
        this.latitude = latitude;
        this.longitude = longitude;
        this.categoryId = categoryId;
        this.description = description;
        this.reference = reference;
        this.address = address;
        this.district = district;
        this.city = city;
        this.state = state;
        this.country = country;
        this.images = images;
        this.user = user;
    }

    void init(int inventoryItemId, int categoryId, String description,
               String reference, String[] images, User user)
    {
        this.isArbitraryPosition = false;
        this.inventoryItemId = inventoryItemId;
        this.categoryId = categoryId;
        this.description = description;
        this.reference = reference;
        this.images = images;
        this.user = user;
    }

    @Override
    protected boolean onPerform()
    {
        try
        {
            if(this.user != null && this.user.id == User.NEEDS_TO_BE_CREATED) {
                CreateUserRequest request = new CreateUserRequest();
                request.setGeneratePassword(true);
                request.setEmail(this.user.email);
                request.setName(this.user.name);
                request.setAddress(this.user.address);
                request.setAddressAdditional(this.user.address_additional);
                request.setDistrict(this.user.district);
                request.setCity(this.user.city);
                request.setPostalCode(this.user.postal_code);
                request.setPhone(this.user.phone);
                request.setDocument(this.user.document);

                this.user = Zup.getInstance().getService().createUser(request).user;
            }

            if(isArbitraryPosition) {
                CreateArbitraryReportItemRequest request = new CreateArbitraryReportItemRequest();
                request.setLatitude(this.latitude);
                request.setLongitude(this.longitude);
                request.setCategoryId(this.categoryId);
                request.setDescription(this.description);
                request.setReference(this.reference);
                request.setAddress(this.address);
                request.setDistrict(this.district);
                request.setCity(this.city);
                request.setState(this.state);
                request.setCountry(this.country);
                request.setImages(this.images);
                if(this.user != null)
                    request.setUserId(this.user.id);

                SingleReportItemCollection result = Zup.getInstance().getService()
                        .createReportItem(this.categoryId, request);

                Intent intent = new Intent();
                intent.putExtra("report", result.report);
                broadcastAction(REPORT_PUBLISHED, intent);
                return true;
            } else {
                CreateReportItemRequest request = new CreateReportItemRequest();
                request.setInventoryItemId(this.inventoryItemId);
                request.setCategoryId(this.categoryId);
                request.setDescription(this.description);
                request.setReference(this.reference);
                request.setImages(this.images);
                if(this.user != null)
                    request.setUserId(this.user.id);

                SingleReportItemCollection result = Zup.getInstance().getService()
                        .createReportItem(this.categoryId, request);

                Intent intent = new Intent();
                intent.putExtra("report", result.report);
                broadcastAction(REPORT_PUBLISHED, intent);
                return true;
            }
        }
        catch (RetrofitError ex)
        {
            this.error = ex.getMessage();
            return false;
        }
    }

    @Override
    protected JSONObject serialize() throws Exception {
        Serializer serializer = new Serializer();
        serializer.isArbitraryPosition = this.isArbitraryPosition;
        serializer.latitude = this.latitude;
        serializer.longitude = this.longitude;
        serializer.inventoryItemId = this.inventoryItemId;
        serializer.categoryId = this.categoryId;
        serializer.description = this.description;
        serializer.reference = this.reference;
        serializer.address = this.address;
        serializer.district = this.district;
        serializer.city = this.city;
        serializer.state = this.state;
        serializer.country = this.country;
        serializer.images = this.images;
        serializer.error = this.error;
        serializer.user = this.user;

        ObjectMapper mapper = new ObjectMapper();
        String res = mapper.writeValueAsString(serializer);

        return new JSONObject(res);
    }

    @Override
    public String getError() {
        return this.error;
    }
}
