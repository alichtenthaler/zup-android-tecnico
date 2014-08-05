package com.ntxdev.zuptecnico.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by igorlira on 7/26/14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Case
{
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Step
    {
        public int id;
        public String title;
        public String step_type;

        // child_flow
        // fields

        public int order_number;
        public boolean active;
        public String created_at;
        public String updated_at;
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


}
