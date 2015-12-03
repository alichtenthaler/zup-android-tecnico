package com.ntxdev.zuptecnico.api;

import android.content.Intent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ntxdev.zuptecnico.entities.ReportItem;
import com.ntxdev.zuptecnico.entities.requests.CreateReportItemCommentRequest;
import com.ntxdev.zuptecnico.entities.responses.CreateReportItemCommentResponse;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.Random;

import retrofit.RetrofitError;

/**
 * Created by igorlira on 7/13/15.
 */
public class PublishReportCommentSyncAction extends SyncAction {
    public static final String REPORT_COMMENT_CREATED = "report_comment_created";

    public static class Serializer {
        public int temporaryId;
        public int itemId;
        public int type;
        public String message;

        public String error;
    }

    String error;

    private int temporaryId;
    public int itemId;
    public int type;
    public String message;

    public PublishReportCommentSyncAction(int itemId, int type, String message) {
        this.temporaryId = new Random(System.currentTimeMillis()).nextInt();

        this.itemId = itemId;
        this.type = type;
        this.message = message;
    }

    public PublishReportCommentSyncAction(JSONObject object, ObjectMapper mapper) throws IOException
    {
        Serializer serializer = mapper.readValue(object.toString(), Serializer.class);

        this.itemId = serializer.itemId;
        this.type = serializer.type;
        this.message = serializer.message;
        this.error = serializer.error;
        this.temporaryId = serializer.temporaryId;
    }

    @Override
    protected boolean onPerform()
    {
        try {
            CreateReportItemCommentRequest request = new CreateReportItemCommentRequest();
            request.message = message;
            request.visibility = type;

            CreateReportItemCommentResponse response = Zup.getInstance().getService()
                    .createReportItemComment(itemId, request);

            this.removeFakeComment();
            this.addCommentToItem(response.comment);

            Intent intent = new Intent();
            intent.putExtra("report_id", itemId);
            intent.putExtra("comment", response.comment);

            this.broadcastAction(REPORT_COMMENT_CREATED, intent);
            return true;
        }
        catch (RetrofitError ex) {
            ReportItem.Comment comment = createFakeComment();
            if(comment != null) {
                Intent intent = new Intent();
                intent.putExtra("report_id", itemId);
                intent.putExtra("comment", comment);

                this.broadcastAction(REPORT_COMMENT_CREATED, intent);
            }

            this.error = ex.getMessage();
            return false;
        }
    }

    private void removeFakeComment() {
        ReportItem item = Zup.getInstance().getReportItemService().getReportItem(itemId);
        if(item != null) {
            item.removeComment(temporaryId);
            Zup.getInstance().getReportItemService().addReportItem(item);
        }
    }

    private void addCommentToItem(ReportItem.Comment comment) {
        if(Zup.getInstance().getSessionUser() == null)
            return;

        ReportItem item = Zup.getInstance().getReportItemService().getReportItem(this.itemId);
        if(item == null)
            return;

        item.addComment(comment);
        Zup.getInstance().getReportItemService().addReportItem(item);
    }

    private ReportItem.Comment createFakeComment() {
        if(Zup.getInstance().getSessionUser() == null)
            return null;

        ReportItem.Comment newComment = new ReportItem.Comment();
        newComment.id = temporaryId;
        newComment.author = Zup.getInstance().getSessionUser();
        newComment.created_at = Zup.getIsoDate(Calendar.getInstance().getTime());
        newComment.visibility = this.type;
        newComment.message = this.message;
        newComment.isFake = true;

        addCommentToItem(newComment);

        return newComment;
    }

    @Override
    protected JSONObject serialize() throws Exception
    {
        Serializer serializer = new Serializer();
        serializer.itemId = itemId;
        serializer.type = type;
        serializer.message = message;
        serializer.error = error;
        serializer.temporaryId = temporaryId;

        String res = Zup.getInstance().getObjectMapper().writeValueAsString(serializer);

        return new JSONObject(res);
    }

    @Override
    public String getError()
    {
        return this.error;
    }
}
