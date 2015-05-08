package com.ntxdev.zuptecnico.api;

import com.ntxdev.zuptecnico.entities.Session;
import com.ntxdev.zuptecnico.entities.collections.SingleUserCollection;
import com.ntxdev.zuptecnico.entities.responses.TransferCaseStepResponse;
import com.ntxdev.zuptecnico.entities.responses.UpdateCaseStepResponse;

import java.util.Hashtable;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;

/**
 * Created by igorlira on 4/6/15.
 */
public interface ZupService
{
    @FormUrlEncoded
    @POST("/authenticate")
    public Session authenticate(@Field("email") String email, @Field("password") String password);

    @GET("/users/{id}")
    SingleUserCollection retrieveUser(@Path("id") int id);

    @PUT("/cases/{case_id}/case_steps/{step_id}")
    TransferCaseStepResponse transferCaseStep(int caseId, int stepId, int responsible_user_id);

    @PUT("/cases/{case_id}")
    UpdateCaseStepResponse updateCaseStep(@Path("case_id") int caseId, int stepId, int stepVersion, Hashtable<Integer, Object> fields);
}
