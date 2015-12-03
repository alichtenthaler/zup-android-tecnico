package com.ntxdev.zuptecnico.api;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonBooleanFormatVisitor;

import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;

import retrofit.RetrofitError;
import retrofit.http.Query;

/**
 * Created by igorlira on 7/13/15.
 */
public class CreateUserSyncAction extends SyncAction
{
    public static class Serializer
    {
        public String email;
        public String password;
        public String password_confirmation;
        public String name;
        public String phone;
        public String document;
        public String address;
        public String address_additional;
        public String postal_code;
        public String district;
        public String city;

        public String error;
    }

    String error;

    String email;
    String password;
    String password_confirmation;
    String name;
    String phone;
    String document;
    String address;
    String address_additional;
    String postal_code;
    String district;
    String city;

    public CreateUserSyncAction(String email, String password, String password_confirmation,
                                String name, String phone, String document, String address,
                                String address_additional, String postal_code, String district,
                                String city)
    {
        this.email = email;
        this.password = password;
        this.password_confirmation = password_confirmation;
        this.name = name;
        this.phone = phone;
        this.document = document;
        this.address = address;
        this.address_additional = address_additional;
        this.postal_code = postal_code;
        this.district = district;
        this.city = city;
    }

    public CreateUserSyncAction(JSONObject object, ObjectMapper mapper) throws IOException
    {
        Serializer serializer = mapper.readValue(object.toString(), Serializer.class);

        this.email = serializer.email;
        this.password = serializer.password;
        this.password_confirmation = serializer.password_confirmation;
        this.name = serializer.name;
        this.phone = serializer.phone;
        this.document = serializer.document;
        this.address = serializer.address;
        this.address_additional = serializer.address_additional;
        this.postal_code = serializer.postal_code;
        this.district = serializer.district;
        this.city = serializer.city;
        this.error = serializer.error;
    }

    @Override
    protected boolean onPerform()
    {
        try
        {
            // Need to merge retrofit branch
            return false;
        }
        catch (RetrofitError ex)
        {
            this.error = ex.getMessage();
            return false;
        }
    }

    @Override
    protected JSONObject serialize() throws Exception
    {
        Serializer serializer = new Serializer();
        serializer.email = this.email;
        serializer.password = password;
        serializer.password_confirmation = password_confirmation;
        serializer.name = name;
        serializer.phone = phone;
        serializer.document = document;
        serializer.address = address;
        serializer.address_additional = address_additional;
        serializer.postal_code = postal_code;
        serializer.district = district;
        serializer.city = city;

        ObjectMapper mapper = new ObjectMapper();
        String res = mapper.writeValueAsString(serializer);

        return new JSONObject(res);
    }

    @Override
    public String getError()
    {
        return this.error;
    }
}
