package com.ntxdev.zuptecnico.entities.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by igorlira on 5/25/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PositionValidationResponse
{
    public boolean inside_boundaries;
}
