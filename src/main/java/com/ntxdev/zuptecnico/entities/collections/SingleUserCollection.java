package com.ntxdev.zuptecnico.entities.collections;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ntxdev.zuptecnico.entities.User;

/**
 * Created by igorlira on 8/8/14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SingleUserCollection
{
    public User user;
}
