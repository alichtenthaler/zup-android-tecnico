package com.ntxdev.zuptecnico.entities.collections;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ntxdev.zuptecnico.entities.User;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserCreationResult {
    public String message;
    public User user;
    public Map<String, String[]> error;
}
