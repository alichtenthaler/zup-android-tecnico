package com.ntxdev.zuptecnico.api;

import android.graphics.BitmapFactory;

import com.ntxdev.zuptecnico.entities.Flow;
import com.ntxdev.zuptecnico.entities.InventoryCategory;
import com.ntxdev.zuptecnico.entities.InventoryItem;
import com.ntxdev.zuptecnico.entities.Session;
import com.ntxdev.zuptecnico.entities.collections.CaseCollection;
import com.ntxdev.zuptecnico.entities.collections.FlowCollection;
import com.ntxdev.zuptecnico.entities.collections.InventoryCategoryCollection;
import com.ntxdev.zuptecnico.entities.collections.InventoryCategoryStatusCollection;
import com.ntxdev.zuptecnico.entities.collections.InventoryItemCollection;
import com.ntxdev.zuptecnico.entities.collections.SingleCaseCollection;
import com.ntxdev.zuptecnico.entities.collections.SingleFlowCollection;
import com.ntxdev.zuptecnico.entities.collections.SingleInventoryCategoryCollection;
import com.ntxdev.zuptecnico.entities.collections.SingleInventoryItemCollection;
import com.ntxdev.zuptecnico.entities.collections.SingleUserCollection;
import com.ntxdev.zuptecnico.entities.requests.EditInventoryItemRequest;
import com.ntxdev.zuptecnico.entities.requests.PublishInventoryItemRequest;
import com.ntxdev.zuptecnico.entities.requests.TransferCaseStepRequest;
import com.ntxdev.zuptecnico.entities.requests.UpdateCaseStepRequest;
import com.ntxdev.zuptecnico.entities.responses.DeleteInventoryItemResponse;
import com.ntxdev.zuptecnico.entities.responses.EditInventoryItemResponse;
import com.ntxdev.zuptecnico.entities.responses.PublishInventoryItemResponse;
import com.ntxdev.zuptecnico.entities.responses.TransferCaseStepResponse;
import com.ntxdev.zuptecnico.entities.responses.UpdateCaseStepResponse;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Hashtable;

/**
 * Created by igorlira on 3/3/14.
 */
public class ZupClient
{
    private ApiHttpClient httpClient;
    private String sessionToken;

    public ZupClient()
    {
        httpClient = new ApiHttpClient();
    }

    public void setSessionToken(String sessionToken)
    {
        this.sessionToken = sessionToken;
    }

    public ApiHttpResult<Session> authenticate(String email, String password)
    {
        Hashtable<String, String> postData = new Hashtable<String, String>();
        postData.put("email", email);
        postData.put("password", password);

        ApiHttpResult<Session> result = httpClient.post("authenticate.json", postData, Session.class);
        return result;
    }

    public ApiHttpResult<SingleUserCollection> retrieveUser(int id)
    {
        return httpClient.get("users/" + id + ".json" + (sessionToken != null ? "?token=" + sessionToken : ""), SingleUserCollection.class);
    }

    public ApiHttpResult<TransferCaseStepResponse> transferCaseStep(int caseId, int stepId, int responsible_user_id)
    {
        TransferCaseStepRequest request = new TransferCaseStepRequest();
        request.responsible_user_id = responsible_user_id;
        return httpClient.put("cases/" + caseId + "/case_steps/" + stepId + ".json" + (sessionToken != null ? "?token=" + sessionToken : ""), request, TransferCaseStepResponse.class);
    }

    public ApiHttpResult<UpdateCaseStepResponse> updateCaseStep(int caseId, int stepId, int stepVersion, Hashtable<Integer, Object> fields)
    {
        UpdateCaseStepRequest request = new UpdateCaseStepRequest();
        request.step_id = stepId;
        request.step_version = stepVersion;
        if(fields != null)
        {
            request.fields = new UpdateCaseStepRequest.FieldValue[fields.size()];
            int i = 0;
            for(Integer key : fields.keySet())
            {
                Object value = fields.get(key);

                UpdateCaseStepRequest.FieldValue val = new UpdateCaseStepRequest.FieldValue();
                val.id = key;
                val.value = value;

                request.fields[i++] = val;
            }
        }
        else
            request.fields = new UpdateCaseStepRequest.FieldValue[0];

        return httpClient.put("cases/" + caseId + ".json" + (sessionToken != null ? "?token=" + sessionToken : ""), request, UpdateCaseStepResponse.class);
    }

    public ApiHttpResult<PublishInventoryItemResponse> publishInventoryItem(InventoryItem item)
    {
        PublishInventoryItemRequest request = new PublishInventoryItemRequest();
        request.data = new Hashtable<String, Object>();
        for(InventoryItem.Data data : item.data)
        {
            /*PublishInventoryItemRequest.Data reqdata = new PublishInventoryItemRequest.Data();
            reqdata.field_id = data.getFieldId();
            reqdata.content = data.content;*/
            if(data.content != null)
                request.data.put(Integer.toString(data.getFieldId()), data.content);
        }
        request.inventory_status_id = item.inventory_status_id;
        ApiHttpResult<PublishInventoryItemResponse> result = httpClient.post("inventory/categories/" + item.inventory_category_id + "/items.json" + (sessionToken != null ? "?token=" + sessionToken : ""), request, PublishInventoryItemResponse.class);

        return result;
    }

    public ApiHttpResult<EditInventoryItemResponse> editInventoryItem(InventoryItem item)
    {
        EditInventoryItemRequest request = new EditInventoryItemRequest();
        request.data = new Hashtable<String, Object>();
        for(InventoryItem.Data data : item.data)
        {
            if(data.content != null)
                request.data.put(Integer.toString(data.getFieldId()), data.content);
        }
        request.inventory_status_id = item.inventory_status_id;
        ApiHttpResult<EditInventoryItemResponse> result = httpClient.put("inventory/categories/" + item.inventory_category_id + "/items/" + item.id + ".json" + (sessionToken != null ? "?token=" + sessionToken : ""), request, EditInventoryItemResponse.class);

        return result;
    }

    public ApiHttpResult<DeleteInventoryItemResponse> deleteInventoryItem(int categoryId, int itemId)
    {
        ApiHttpResult<DeleteInventoryItemResponse> result = httpClient.delete("inventory/categories/" + categoryId + "/items/" + itemId + ".json" + (sessionToken != null ? "?token=" + sessionToken : ""), DeleteInventoryItemResponse.class);
        return result;
    }

    public ApiHttpResult<InventoryCategoryCollection> retrieveInventoryCategories()
    {
        ApiHttpResult<InventoryCategoryCollection> result = httpClient.get("inventory/categories.json?display_type=full" + (sessionToken != null ? "&token=" + sessionToken : ""), InventoryCategoryCollection.class);
        return result;
    }

    public ApiHttpResult<SingleInventoryCategoryCollection> retrieveInventoryCategoryInfo(int categoryId)
    {
        ApiHttpResult<SingleInventoryCategoryCollection> result = httpClient.get("inventory/categories/" + categoryId + ".json" + (sessionToken != null ? "?token=" + sessionToken : ""), SingleInventoryCategoryCollection.class);
        return result;
    }

    public ApiHttpResult<InventoryCategoryStatusCollection> retrieveInventoryCategoryStatuses(int categoryId)
    {
        ApiHttpResult<InventoryCategoryStatusCollection> result = httpClient.get("inventory/categories/" + categoryId + "/statuses.json" + (sessionToken != null ? "?token=" + sessionToken : ""), InventoryCategoryStatusCollection.class);
        return result;
    }

    public ApiHttpResult<InventoryCategory> retrieveInventoryCategoryForm(int categoryId)
    {
        ApiHttpResult<InventoryCategory> result = httpClient.get("inventory/categories/" + categoryId + "/form.json" + (sessionToken != null ? "?token=" + sessionToken : ""), InventoryCategory.class);
        return result;
    }

    public ApiHttpResult<InventoryItemCollection> retrieveInventoryItems()
    {
        ApiHttpResult<InventoryItemCollection> result = httpClient.get("inventory/items.json" + (sessionToken != null ? "?token=" + sessionToken : ""), InventoryItemCollection.class);
        return result;
    }

    public ApiHttpResult<InventoryItemCollection> searchInventoryItems(int page, int per_page, int[] inventory_category_ids, Integer[] inventory_statuses_ids, String address, String title, Calendar creation_from, Calendar creation_to, Calendar modification_from, Calendar modification_to, Float latitude, Float longitude)
    {
        String inventory_category_ids_joined = "";
        for(int i = 0; i < inventory_category_ids.length; i++)
        {
            inventory_category_ids_joined += inventory_category_ids[i];
            if(i + 1 < inventory_category_ids.length)
                inventory_category_ids_joined += ",";
        }
        String inventory_statuses_ids_joined = "";
        if(inventory_statuses_ids != null)
        {
            for (int i = 0; i < inventory_statuses_ids.length; i++) {
                inventory_statuses_ids_joined += inventory_statuses_ids[i];
                if (i + 1 < inventory_statuses_ids.length)
                    inventory_statuses_ids_joined += ",";
            }
        }

        String created_at = null;
        if(creation_from != null)
        {
            created_at = "created_at[begin]=" + Zup.getIsoDate(creation_from.getTime());
        }
        if(creation_to != null)
        {
            created_at = (created_at != null ? created_at + "&" : "") + "created_at[end]=" + Zup.getIsoDate(creation_to.getTime());
        }

        String updated_at = null;
        if(modification_from != null)
        {
            updated_at = "updated_at[begin]=" + Zup.getIsoDate(modification_from.getTime());
        }
        if(modification_to != null)
        {
            updated_at = (updated_at != null ? updated_at + "&" : "") + "updated_at[end]=" + Zup.getIsoDate(modification_to.getTime());
        }

        String position = null;
        if(latitude != null)
        {
            position = "position[latitude]=" + latitude;
        }
        if(longitude != null)
        {
            position = (position != null ? position + "&" : "") + "position[longitude]=" + longitude;
        }

        try
        {
            if(address != null)
                address = URLEncoder.encode(address, "utf-8");
            if(title != null)
                title = URLEncoder.encode(title, "utf-8");
        }
        catch (UnsupportedEncodingException ex) { }

        ApiHttpResult<InventoryItemCollection> result = httpClient.get("search/inventory/items.json?page=" + page + "&per_page=" + per_page + "&inventory_categories_ids=" + inventory_category_ids_joined + (inventory_statuses_ids != null ? "&inventory_statuses_ids=" + inventory_statuses_ids_joined : "") + (address != null ? "&address=" + address : "") + (title != null ? "&title=" + title : "") + (created_at != null ? "&" + created_at + "" : "") + (updated_at != null ? "&" + updated_at + "" : "") + (position != null ? "&" + position + "" : "") + (sessionToken != null ? "&token=" + sessionToken : ""), InventoryItemCollection.class);
        return result;
    }

    public ApiHttpResult<InventoryItemCollection> retrieveInventoryItems(int categoryId, int page)
    {
        ApiHttpResult<InventoryItemCollection> result = httpClient.get("inventory/items.json?display_type=basic&inventory_category_id=" + categoryId + "&page=" + page + (sessionToken != null ? "&token=" + sessionToken : ""), InventoryItemCollection.class);
        return result;
    }

    public ApiHttpResult<InventoryItemCollection> retrieveInventoryItems(int categoryId, int page, String sort, String order)
    {
        ApiHttpResult<InventoryItemCollection> result = httpClient.get("inventory/items.json?display_type=basic&inventory_category_id=" + categoryId + "&page=" + page + "&sort=" + sort + "&order=" + order + (sessionToken != null ? "&token=" + sessionToken : ""), InventoryItemCollection.class);
        return result;
    }

    public ApiHttpResult<InventoryItemCollection> retrieveInventoryItems(int categoryId, double latitude, double longitude, double radius, double zoom)
    {
        ApiHttpResult<InventoryItemCollection> result = httpClient.get("inventory/items.json?display_type=basic&inventory_category_id=" + categoryId + "&position[latitude]=" + latitude + "&position[longitude]=" + longitude + "&position[distance]=" + radius + "&zoom=" + zoom + (sessionToken != null ? "&limit=100&token=" + sessionToken : ""), InventoryItemCollection.class);
        return result;
    }

    public ApiHttpResult<SingleInventoryItemCollection> retrieveInventoryItemInfo(int categoryId, int itemId)
    {
        ApiHttpResult<SingleInventoryItemCollection> result = httpClient.get("inventory/categories/" + categoryId + "/items/" + itemId + ".json" + (sessionToken != null ? "?token=" + sessionToken : ""), SingleInventoryItemCollection.class);
        return result;
    }

    public ApiHttpResult<CaseCollection> retrieveCases(int page)
    {
        ApiHttpResult<CaseCollection> result = httpClient.get("cases.json?page=" + page + (sessionToken != null ? "&token=" + sessionToken : ""), CaseCollection.class);
        return result;
    }

    public ApiHttpResult<CaseCollection> retrieveCases(int initial, int page)
    {
        ApiHttpResult<CaseCollection> result = httpClient.get("cases.json?initial_flow_id=" + initial + "&page=" + page + (sessionToken != null ? "&token=" + sessionToken : ""), CaseCollection.class);
        return result;
    }

    public ApiHttpResult<SingleCaseCollection> retrieveCase(int id)
    {
        ApiHttpResult<SingleCaseCollection> result = httpClient.get("cases/" + id + ".json?display_type=full" + (sessionToken != null ? "&token=" + sessionToken : ""), SingleCaseCollection.class);
        return result;
    }

    public ApiHttpResult<FlowCollection> retrieveFlows()
    {
        ApiHttpResult<FlowCollection> result = httpClient.get("flows.json?" + (sessionToken != null ? "&token=" + sessionToken : ""), FlowCollection.class);
        return result;
    }

    public ApiHttpResult<Flow.StepCollection> retrieveFlowSteps(int flowId)
    {
        ApiHttpResult<Flow.StepCollection> result = httpClient.get("flows/" + flowId + "/steps.json?display_type=full" + (sessionToken != null ? "&token=" + sessionToken : ""), Flow.StepCollection.class);
        return result;
    }

    public ApiHttpResult<SingleFlowCollection> retrieveFlowVersion(int flowId, int version)
    {
        ApiHttpResult<SingleFlowCollection> result = httpClient.get("flows/" + flowId + "?version=" + version + "&display_type=full" + (sessionToken != null ? "&token=" + sessionToken : ""), SingleFlowCollection.class);
        return result;
    }

    public void loadResource(String url, Zup.BitmapResource resource)
    {
        InputStream stream = httpClient.get(url);
        if(stream != null)
        {
            resource.bitmap = BitmapFactory.decodeStream(stream);
            resource.loaded = true;
        }
    }
}
