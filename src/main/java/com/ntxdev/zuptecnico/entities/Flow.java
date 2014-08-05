package com.ntxdev.zuptecnico.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by igorlira on 7/26/14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Flow
{
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ResolutionState
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

    public int id;
    public String title;
    public String description;
    public boolean initial;
    public Integer[] steps_id;
    public Integer created_by_id;
    public Integer updated_by_id;
    public ResolutionState[] resolution_states;
    public String status;
    public Integer last_version;
    public Integer last_version_id;
    public String created_at;
    public String updated_at;

}
