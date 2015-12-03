package com.ntxdev.zuptecnico.api;

import java.io.IOException;

import retrofit.client.ApacheClient;
import retrofit.client.Client;
import retrofit.client.Request;
import retrofit.client.Response;

/**
 * Created by igorlira on 6/12/15.
 */
public class ZupApiClient extends ApacheClient
{
    @Override
    public Response execute(Request request) throws IOException {
        return super.execute(request);
    }
}
