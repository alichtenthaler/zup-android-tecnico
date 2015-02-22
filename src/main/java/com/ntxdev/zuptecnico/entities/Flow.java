package com.ntxdev.zuptecnico.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.LinkedHashMap;

/**
 * Created by igorlira on 7/26/14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Flow implements Serializable
{
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Step implements Serializable
    {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Field implements Serializable
        {
            public int id;
            public String title;
            public String field_type;
            public int category_inventory_id;
            public int category_report_id;
            public int origin_field_id;
            public boolean active;
            public int step_id;
            public boolean multiple;
            // filter
            public LinkedHashMap requirements;
            public int order_number;
            public LinkedHashMap values;

            public int last_version;
        }
        public int id;
        public String title;
        public String step_type;

        // child_flow
        @JsonProperty("my_fields")
        public Field[] fields;

        public int order_number;
        public boolean active;

        public int last_version;

        public Step[] list_versions;

        public int child_flow_id;
        public Flow child_flow;

        public int getChildFlowId()
        {
            if(child_flow != null)
                return child_flow.id;
            else
                return child_flow_id;
        }

        public Field getField(int id)
        {
            if(fields == null)
                return null;

            for(int i = 0; i < fields.length; i++)
            {
                if(fields[i].id == id)
                    return fields[i];
            }

            return null;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ResolutionState implements Serializable
    {
        public int id;
        public int flow_id;
        public String title;
        @JsonProperty("default")
        public boolean _default;
        public boolean active;
        public String created_at;
        public String updated_at;
        public Integer last_version;
        public Integer last_version_id;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StepCollection implements Serializable
    {
        public Step[] steps;
    }

    public int id;
    public String title;
    public String description;
    public boolean initial;
    public Integer[] steps_id;
    public Integer created_by_id;
    public Integer updated_by_id;

    @JsonProperty("my_resolution_states")
    public ResolutionState[] resolution_states;
    public String status;
    public Integer last_version;
    public Integer last_version_id;
    public String created_at;
    public String updated_at;

    @JsonProperty("my_steps_flows")
    public Step[] steps;

    public Flow[] list_versions;

    public boolean areStepsDownloaded()
    {
        return steps != null && steps.length > 0;
    }

    public Step getStep(int id)
    {
        if(!areStepsDownloaded())
            return null;

        for(int i = 0; i < steps.length; i++)
        {
            if(steps[i].id == id)
                return steps[i];
        }

        return null;
    }
}
