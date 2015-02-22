package com.ntxdev.zuptecnico.entities.requests;

/**
 * Created by igorlira on 8/8/14.
 */
public class UpdateCaseStepRequest
{
    public static class FieldValue
    {
        public int id;
        public Object value;
    }

    public int step_id;
    public int step_version;
    public FieldValue[] fields;
}
