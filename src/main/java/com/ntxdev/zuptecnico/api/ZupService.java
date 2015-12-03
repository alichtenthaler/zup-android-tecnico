package com.ntxdev.zuptecnico.api;

import com.ntxdev.zuptecnico.config.InternalConstants;
import com.ntxdev.zuptecnico.entities.ReportNotificationCollection;
import com.ntxdev.zuptecnico.entities.Session;
import com.ntxdev.zuptecnico.entities.collections.InventoryCategoryCollection;
import com.ntxdev.zuptecnico.entities.collections.InventoryCategoryStatusCollection;
import com.ntxdev.zuptecnico.entities.collections.InventoryItemCollection;
import com.ntxdev.zuptecnico.entities.collections.ReportCategoryCollection;
import com.ntxdev.zuptecnico.entities.collections.ReportHistoryItemCollection;
import com.ntxdev.zuptecnico.entities.collections.ReportItemCollection;
import com.ntxdev.zuptecnico.entities.collections.SingleInventoryCategoryCollection;
import com.ntxdev.zuptecnico.entities.collections.SingleInventoryItemCollection;
import com.ntxdev.zuptecnico.entities.collections.SingleReportItemCollection;
import com.ntxdev.zuptecnico.entities.collections.SingleUserCollection;
import com.ntxdev.zuptecnico.entities.collections.UserCollection;
import com.ntxdev.zuptecnico.entities.collections.UserCreationResult;
import com.ntxdev.zuptecnico.entities.requests.CreateArbitraryReportItemRequest;
import com.ntxdev.zuptecnico.entities.requests.CreateReportItemCommentRequest;
import com.ntxdev.zuptecnico.entities.requests.CreateReportItemRequest;
import com.ntxdev.zuptecnico.entities.requests.CreateUserRequest;
import com.ntxdev.zuptecnico.entities.responses.CreateReportItemCommentResponse;
import com.ntxdev.zuptecnico.entities.responses.DeleteInventoryItemResponse;
import com.ntxdev.zuptecnico.entities.responses.EditInventoryItemResponse;
import com.ntxdev.zuptecnico.entities.responses.PositionValidationResponse;
import com.ntxdev.zuptecnico.entities.responses.PublishInventoryItemResponse;
import com.ntxdev.zuptecnico.entities.responses.UpdateCaseStepResponse;

import java.util.Hashtable;
import java.util.Map;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.http.QueryMap;

/**
 * Created by igorlira on 4/6/15.
 */
public interface ZupService {
    @FormUrlEncoded
    @POST("/authenticate")
    public Session authenticate(@Field("email") String email, @Field("password") String password);

    @FormUrlEncoded
    @POST("/authenticate")
    public void authenticate(@Field("email") String email, @Field("password") String password, Callback<Session> callback);

    @GET("/users/{id}")
    SingleUserCollection retrieveUser(@Path("id") int id);

    @GET("/users")
    UserCollection retrieveUsers(@Query("page") int page);

    @GET("/search/users")
    UserCollection searchUsers(@Query("name") String name, @Query("page") int page);

    @POST("/users")
    void createUser(@Body CreateUserRequest body, Callback<UserCreationResult> callback);

    @POST("/users")
    UserCreationResult createUser(@Body CreateUserRequest body);

    @GET("/users/{id}")
    void retrieveUser(@Path("id") int id, Callback<SingleUserCollection> cb);

    @POST("/inventory/categories/{categoryId}/items")
    PublishInventoryItemResponse publishInventoryItem(@Path("categoryId") int categoryId, @Field("data") Hashtable<String, Object> data, @Field("inventory_status_id") int statusId);

    @PUT("/inventory/categories/{categoryId}/items/{item_id}")
    EditInventoryItemResponse editInventoryItem(@Path("categoryId") int categoryId, @Path("item_id") int itemId, @Field("data") Hashtable<String, Object> data, @Field("inventory_status_id") int statusId);

    @DELETE("/inventory/categories/{categoryId}/items/{item_id}")
    DeleteInventoryItemResponse deleteInventoryItem(@Path("categoryId") int categoryId, @Path("item_id") int itemId);

    @GET("/inventory/categories?display_type=full&return_fields=" + InternalConstants.INVENTORY_CATEGORY_LISTING_RETURN_FIELDS)
    InventoryCategoryCollection getInventoryCategories();

    @GET("/inventory/categories?display_type=full&return_fields=" + InternalConstants.INVENTORY_CATEGORY_LISTING_RETURN_FIELDS)
    void getInventoryCategories(Callback<InventoryCategoryCollection> callback);

    @GET("/inventory/categories/{categoryId}")
    SingleInventoryCategoryCollection getInventoryCategory(@Path("categoryId") int id);

    @GET("/inventory/categories/{categoryId}/statuses")
    InventoryCategoryStatusCollection getInventoryCategoryStatuses(@Path("categoryId") int id);

    @GET("/search/inventory/items")
    InventoryItemCollection searchInventoryItems(@Query("inventory_categories_ids") int inventory_category_ids, @Query("inventory_statuses_ids") Integer[] inventory_statuses_ids, @Query("query") String query, @Query("page") int page, @Query("per_page") int per_page);

    @GET("/search/inventory/items")
    InventoryItemCollection searchInventoryItems(@Query("inventory_categories_ids") int inventory_category_ids, @Query("page") int page, @QueryMap Map<String, Object> options);

    @GET("/inventory/items?return_fields=" + InternalConstants.INVENTORY_ITEM_LISTING_RETURN_FIELDS)
    InventoryItemCollection getInventoryItems(@Query("inventory_category_id") int categoryId, @Query("page") int page);

    @GET("/inventory/items?return_fields=" + InternalConstants.INVENTORY_ITEM_LISTING_RETURN_FIELDS)
    InventoryItemCollection getInventoryItems(@Query("inventory_category_id") int categoryId, @Query("page") int page, @Query("sort") String sort, @Query("order") String order);

    @GET("/inventory/items?return_fields=" + InternalConstants.INVENTORY_ITEM_MAP_RETURN_FIELDS)
    InventoryItemCollection getInventoryItems(@Query("clusterize") boolean clusterize, @Query("inventory_categories_ids") int categoryId, @Query("position[latitude]") double latitude, @Query("position[longitude]") double longitude, @Query("position[distance]") double radius, @Query("zoom") int zoom);

    @GET("/inventory/categories/{categoryId}/items/{item_id}?display_type=full&return_fields=" + InternalConstants.INVENTORY_ITEM_RETURN_FIELDS)
    SingleInventoryItemCollection getInventoryItem(@Path("categoryId") int categoryId, @Path("item_id") int itemId);

    @PUT("/cases/{case_id}")
    UpdateCaseStepResponse updateCaseStep(@Path("case_id") int caseId, int stepId, int stepVersion, Hashtable<Integer, Object> fields);

    @GET("/reports/items")
    ReportItemCollection retrieveReportItems(@Query("page") int page);

    @GET("/search/reports/items?return_fields=" + InternalConstants.REPORT_ITEM_LISTING_RETURN_FIELDS)
    void retrieveFilteredReportItems(@Query("page") int page,
                                                    @QueryMap Map<String, Object> options,
                                                    Callback<ReportItemCollection> cb);

    @GET("/reports/items?return_fields=" + InternalConstants.REPORT_ITEM_LISTING_RETURN_FIELDS)
    void retrieveReportItemsListing(@Query("page") int page, Callback<ReportItemCollection> cb);

    @GET("/search/reports/items?return_fields=" + InternalConstants.REPORT_ITEM_LISTING_RETURN_FIELDS)
    void retrieveReportItemsByAddressOrProtocol(@Query("page") int page,
                                                @Query("query") String query,
                                                Callback<ReportItemCollection> cb);

    @GET("/search/reports/items?clusterize=true&return_fields=" + InternalConstants.REPORT_ITEM_MAP_RETURN_FIELDS)
    ReportItemCollection retrieveReportItems(@Query("position[latitude]") float latitude,
                                             @Query("position[longitude]") float longitude,
                                             @Query("position[distance]") float distance,
                                             @Query("limit") int limit,
                                             @Query("zoom") int zoom);

    @GET("/reports/items/{item_id}/history?return_fields=" + InternalConstants.REPORT_ITEM_HISTORY_RETURN_FIELDS)
    void retrieveReportItemHistory(@Path("item_id") int itemId,
                                   Callback<ReportHistoryItemCollection> cb);

    @GET("/reports/items/{item_id}/history?return_fields=" + InternalConstants.REPORT_ITEM_HISTORY_RETURN_FIELDS)
    ReportHistoryItemCollection retrieveReportItemHistory(@Path("item_id") int itemId);

    @GET("/reports/categories/{category_id}/items/{item_id}/notifications/last?return_fields=" + InternalConstants.REPORT_ITEM_NOTIFICATION_RETURN_FIELDS)
    void retrieveLastReportNotification(@Path("item_id") int itemId, @Path("category_id") int categoryId, Callback<ReportNotificationCollection> cb);

    @GET("/reports/categories/{category_id}/items/{item_id}/notifications?return_fields=" + InternalConstants.REPORT_ITEM_NOTIFICATION_RETURN_FIELDS)
    void retrieveReportNotificationCollection(@Path("item_id") int itemId, @Path("category_id") int categoryId, Callback<ReportNotificationCollection> cb);


    @GET("/reports/categories?display_type=full")
    ReportCategoryCollection getReportCategories();

    @GET("/reports/items/{item_id}")
    SingleReportItemCollection retrieveReportItem(@Path("item_id") int itemId);

    @DELETE("/reports/items/{item_id}")
    Response deleteReportItem(@Path("item_id") int itemId);

    @POST("/reports/{item_id}/comments")
    void createReportItemComment(@Path("item_id") int itemId,
                                 @Body CreateReportItemCommentRequest request,
                                 Callback<CreateReportItemCommentResponse> cb);

    @POST("/reports/{item_id}/comments")
    CreateReportItemCommentResponse createReportItemComment(@Path("item_id") int itemId,
                                                            @Body CreateReportItemCommentRequest request);

    @GET("/reports/items/{item_id}")
    void retrieveReportItem(@Path("item_id") int itemId, Callback<SingleReportItemCollection> cb);


    @POST("/reports/items")
    SingleReportItemCollection createReportItem(@Query("category_id") int category_id,
                                                @Query("inventory_item_id") int inventory_item_id,
                                                @Query("description") String description,
                                                @Query("reference") String reference,
                                                @Query("images") String[] images);

    @POST("/reports/items")
    SingleReportItemCollection createReportItem(@Query("latitude") float latitude,
                                                @Query("longitude") float longitude,
                                                @Query("category_id") int category_id,
                                                @Query("description") String description,
                                                @Query("reference") String reference,
                                                @Query("address") String address,
                                                @Query("district") String district,
                                                @Query("city") String city,
                                                @Query("state") String state,
                                                @Query("country") String country,
                                                @Query("images") String[] images);

    @POST("/reports/{categoryId}/items")
    SingleReportItemCollection createReportItem(@Path("categoryId") int categoryId,
                                                @Body CreateReportItemRequest body);

    @POST("/reports/{categoryId}/items")
    void createReportItem(@Path("categoryId") int categoryId,
                          @Body CreateArbitraryReportItemRequest body,
                          Callback<SingleReportItemCollection> cb);

    @POST("/reports/{categoryId}/items")
    SingleReportItemCollection createReportItem(@Path("categoryId") int categoryId,
                                                @Body CreateArbitraryReportItemRequest body);

    @PUT("/reports/{categoryId}/items/{item_id}")
    SingleReportItemCollection updateReportItem(@Path("categoryId") int categoryId,
                                                @Path("item_id") int itemId,
                                                @Body CreateReportItemRequest body);

    @PUT("/reports/{categoryId}/items/{item_id}")
    SingleReportItemCollection updateReportItem(@Path("categoryId") int categoryId,
                                                @Path("item_id") int itemId,
                                                @Body CreateArbitraryReportItemRequest body);

    @POST("/users")
    SingleUserCollection createUser(@Query("email") String email, @Query("password") String password,
                                    @Query("password_confirmation") String password_confirmation,
                                    @Query("name") String name, @Query("phone") String phone,
                                    @Query("document") String document, @Query("address") String address,
                                    @Query("address_additional") String address_additional,
                                    @Query("postal_code") String postal_code,
                                    @Query("district") String district, @Query("city") String city);

    @GET("/utils/city-boundary/validate")
    PositionValidationResponse validatePosition(@Query("latitude") double latitude, @Query("longitude") double longitude);
}
