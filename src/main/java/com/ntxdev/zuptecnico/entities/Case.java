package com.ntxdev.zuptecnico.entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Created by igorlira on 7/26/14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Case implements Serializable
{
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Step implements Serializable
    {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class DataField implements Serializable
        {
            public int id;
            public Object value;

            public Flow.Step.Field field;
            @JsonIgnore
            public int fieldId;

            public int getFieldId()
            {
                if(field != null)
                    return field.id;
                else
                    return fieldId;
            }
        }

        public int id;
        public int case_id;
        public int step_id;
        public int step_version;
        public Flow.Step my_step;
        public DataField[] case_step_data_fields;
        // trigger_ids
        public Integer responsible_user_id;
        public boolean executed;

        public boolean hasResponsibleUser()
        {
            return responsible_user_id != null && responsible_user_id > 0;
        }

        public boolean hasDataField(int fieldId)
        {
            if(case_step_data_fields == null)
                return false;

            for(int i = 0; i < case_step_data_fields.length; i++)
            {
                if(case_step_data_fields[i].getFieldId() == fieldId)
                    return true;
            }

            return false;
        }

        public Object getDataField(int fieldId)
        {
            if(case_step_data_fields == null)
                return null;

            for(int i = 0; i < case_step_data_fields.length; i++)
            {
                if(case_step_data_fields[i].getFieldId() == fieldId)
                    return case_step_data_fields[i].value;
            }

            return null;
        }
    }

    public int id;
    public Integer created_by_id;
    public Integer updated_by_id;
    public String created_at;
    public String updated_at;
    public int initial_flow_id;
    public int flow_version;
    public int total_steps;
    public Integer[] disabled_steps;
    public Integer original_case_id;
    public Integer[] children_case_ids;
    public Integer[] case_step_ids;
    public Integer next_step_id;
    public Integer responsible_user_id;
    public Integer responsible_group_id;
    public String status; // active, pending, transfer, inactive e finished

    public Step current_step;
    public Step[] case_steps;

    public Step getStep(int stepId)
    {
        if(case_steps == null)
            return null;

        for(int i = 0; i < case_steps.length; i++)
        {
            if(case_steps[i].step_id == stepId)
                return case_steps[i];
        }

        return null;
    }
}
