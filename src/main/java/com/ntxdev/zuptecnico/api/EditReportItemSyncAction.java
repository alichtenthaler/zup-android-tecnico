package com.ntxdev.zuptecnico.api;

import android.content.Intent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ntxdev.zuptecnico.entities.Position;
import com.ntxdev.zuptecnico.entities.ReportItem;
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
public class EditReportItemSyncAction extends SyncAction {
    public static final String REPORT_EDITED = "report_edited";
    public static final String NOT_FOUND_ERROR = "404 Not Found";

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Serializer {
        public boolean isArbitraryPosition;

        public int id;
        public Double latitude;
        public Double longitude;
        public int category_id;
        public Integer inventory_item_id;
        public String description;
        public String reference;
        public String address;
        public String district;
        public String city;
        public String state;
        public String country;
        public String[] images;
        public int currentCategoryId;

        public User user;

        public String error;

        public Serializer() {
        }
    }

    public int id;
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

    private int currentCategoryId;

    public EditReportItemSyncAction(int id, double latitude, double longitude, int categoryId,
                                    String description, String reference, String address,
                                    String district, String city, String state, String country,
                                    String[] images, User user, int currentCategoryId) {
        init(id, latitude, longitude, categoryId, description, reference, address, district, city,
                state, country, images, user, currentCategoryId);
    }

    public EditReportItemSyncAction(int id, int inventoryItemId, int categoryId,
                                    String description, String reference, String[] images,
                                    User user, int currentCategoryId) {
        init(id, inventoryItemId, categoryId, description, reference, images, user, currentCategoryId);
    }

    public EditReportItemSyncAction(JSONObject object, ObjectMapper mapper) throws IOException {
        Serializer serializer = mapper.readValue(object.toString(), Serializer.class);

        if (serializer.isArbitraryPosition) {
            init(serializer.id, serializer.latitude, serializer.longitude, serializer.category_id,
                    serializer.description, serializer.reference, serializer.address,
                    serializer.district, serializer.city, serializer.state, serializer.country,
                    serializer.images, serializer.user, serializer.currentCategoryId);
        } else {
            init(serializer.id, serializer.inventory_item_id, serializer.category_id,
                    serializer.description, serializer.reference, serializer.images, serializer.user,
                    serializer.currentCategoryId);
        }
        this.error = serializer.error;
    }

    void init(int id, double latitude, double longitude, int category_id, String description,
              String reference, String address, String district, String city, String state,
              String country, String[] images, User user, int currentCategoryId) {
        this.isArbitraryPosition = true;
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.categoryId = category_id;
        this.description = description;
        this.reference = reference;
        this.address = address;
        this.district = district;
        this.city = city;
        this.state = state;
        this.country = country;
        this.images = images;
        this.user = user;
        this.currentCategoryId = currentCategoryId;
    }

    void init(int id, int inventory_item_id, int category_id, String description,
              String reference, String[] images, User user, int currentCategoryId) {
        this.id = id;
        this.isArbitraryPosition = false;
        this.inventoryItemId = inventory_item_id;
        this.categoryId = category_id;
        this.description = description;
        this.reference = reference;
        this.images = images;
        this.user = user;
        this.currentCategoryId = currentCategoryId;
    }

    @Override
    protected boolean onPerform() {
        try {
            if (this.user != null && this.user.id == User.NEEDS_TO_BE_CREATED) {
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

            if (isArbitraryPosition) {
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
                if (this.user != null)
                    request.setUserId(this.user.id);

                SingleReportItemCollection result = Zup.getInstance().getService()
                        .updateReportItem(this.currentCategoryId, this.id, request);
                updateLocalReportItem(result.report);
                Intent intent = new Intent();
                intent.putExtra("report", result.report);
                broadcastAction(REPORT_EDITED, intent);


                return true;
            } else {
                CreateReportItemRequest request = new CreateReportItemRequest();
                request.setInventoryItemId(this.inventoryItemId);
                request.setCategoryId(this.categoryId);
                request.setDescription(this.description);
                request.setReference(this.reference);
                request.setImages(this.images);
                if (this.user != null)
                    request.setUserId(this.user.id);

                SingleReportItemCollection result = Zup.getInstance().getService()
                        .updateReportItem(this.currentCategoryId, this.id, request);

                updateLocalReportItem(result.report);
                Intent intent = new Intent();
                intent.putExtra("report", result.report);
                broadcastAction(REPORT_EDITED, intent);
                return true;
            }
        } catch (RetrofitError ex) {
            this.error = ex.getMessage();
            return false;
        }
    }

    private void updateLocalReportItem(ReportItem item) {
        Zup.getInstance().getReportItemService().deleteReportItem(this.id);
        Zup.getInstance().getReportItemService().addReportItem(item);
    }

    @Override
    protected JSONObject serialize() throws Exception {
        Serializer serializer = new Serializer();
        serializer.isArbitraryPosition = this.isArbitraryPosition;
        serializer.latitude = this.latitude;
        serializer.longitude = this.longitude;
        serializer.inventory_item_id = this.inventoryItemId;
        serializer.category_id = this.categoryId;
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
        serializer.id = this.id;
        serializer.currentCategoryId = this.currentCategoryId;

        ObjectMapper mapper = new ObjectMapper();
        String res = mapper.writeValueAsString(serializer);

        return new JSONObject(res);
    }

    @Override
    public String getError() {
        return this.error;
    }
}
