package com.ntxdev.zuptecnico.api;

import android.content.Intent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ntxdev.zuptecnico.R;
import com.ntxdev.zuptecnico.ZupApplication;
import com.ntxdev.zuptecnico.entities.responses.DeleteInventoryItemResponse;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit.RetrofitError;

/**
 * Created by igorlira on 3/25/14.
 */
public class DeleteReportItemSyncAction extends SyncAction {
    public static final String REPORT_DELETED = "report_deleted";

    public int itemId;
    public String error;

    public DeleteReportItemSyncAction(JSONObject object, ObjectMapper mapper) throws JSONException
    {
        this.itemId = object.getInt("item_id");

        if(object.has("error"))
            this.error = object.getString("error");
    }

    public DeleteReportItemSyncAction(int id) {
        this.itemId = id;
    }

    public boolean onPerform() {
        try {
            Zup.getInstance().getService().deleteReportItem(this.itemId);
            Zup.getInstance().getReportItemService().deleteReportItem(this.itemId);

            Intent intent = new Intent();
            intent.putExtra("report_id", itemId);
            broadcastAction(REPORT_DELETED, intent);

            return true;
        }
        catch (RetrofitError error) {
            if(error.getKind() == RetrofitError.Kind.NETWORK) {
                this.error = ZupApplication.getContext().getString(R.string.error_network);
            } else {
                this.error = error.getLocalizedMessage();
            }
            return false;
        }
    }

    @Override
    public String getError() {
        return error;
    }

    @Override
    protected JSONObject serialize() throws JSONException
    {
        JSONObject result = new JSONObject();
        result.put("item_id", itemId);
        result.put("error", error);

        return result;
    }
}
