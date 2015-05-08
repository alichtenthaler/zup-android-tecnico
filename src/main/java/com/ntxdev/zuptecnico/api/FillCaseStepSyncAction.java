package com.ntxdev.zuptecnico.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ntxdev.zuptecnico.entities.Case;
import com.ntxdev.zuptecnico.entities.InventoryItem;
import com.ntxdev.zuptecnico.entities.responses.EditInventoryItemResponse;
import com.ntxdev.zuptecnico.entities.responses.UpdateCaseStepResponse;
import com.ntxdev.zuptecnico.util.Strings;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Hashtable;

/**
 * Created by igorlira on 3/25/14.
 */
public class FillCaseStepSyncAction extends SyncAction {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Serializer
    {
        public int caseId;
        public int stepId;
        public int stepVersion;
        public Hashtable<Integer, Object> fields;
        public String error;

        public Serializer() { }
    }

    public int caseId;
    public int stepId;
    public int stepVersion;
    public Hashtable<Integer, Object> fields;
    String error;

    public FillCaseStepSyncAction(int caseId, int stepId, int stepVersion, Hashtable<Integer, Object> fields)
    {
        this.caseId = caseId;
        this.stepId = stepId;
        this.stepVersion = stepVersion;
        this.fields = fields;
    }

    public FillCaseStepSyncAction(JSONObject object, ObjectMapper mapper) throws IOException
    {
        Serializer serializer = mapper.readValue(object.toString(), Serializer.class);

        this.caseId = serializer.caseId;
        this.stepId = serializer.stepId;
        this.stepVersion = serializer.stepVersion;
        this.fields = serializer.fields;
        this.error = serializer.error;
    }

    public boolean onPerform() {
        ApiHttpResult<UpdateCaseStepResponse> response = Zup.getInstance().updateCaseStepFull(this.caseId, this.stepId, this.stepVersion, fields);

        if(response.statusCode == 200 || response.statusCode == 201 && response.result._case != null)
        {
            if(!Zup.getInstance().hasCase(response.result._case.id))
                Zup.getInstance().addCase(response.result._case);
            else
                Zup.getInstance().updateCase(response.result._case, true);

            error = null;
            return true;
        }
        else if(response.result != null)
        {
            error = response.result.error;
            return false;
        }
        else
        {
            error = "Sem conexão com a internet.";
            return false;
        }
    }

    @Override
    protected JSONObject serialize() throws Exception {
        Serializer serializer = new Serializer();
        serializer.caseId = caseId;
        serializer.stepId = stepId;
        serializer.stepVersion = stepVersion;
        serializer.fields = fields;
        serializer.error =  getError();

        ObjectMapper mapper = new ObjectMapper();
        String res = mapper.writeValueAsString(serializer);

        return new JSONObject(res);
    }

    @Override
    public String getError() {
        return error;
    }
}
