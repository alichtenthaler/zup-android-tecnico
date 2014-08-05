package com.ntxdev.zuptecnico.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by igorlira on 3/16/14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    public int id;
    public String name;
    public String email;
    public String phone;
    public String document;
    public String address;
    public String address_additional;
    public String postal_code;
    public String district;
    public String created_at;
    public String updated_at;
}
