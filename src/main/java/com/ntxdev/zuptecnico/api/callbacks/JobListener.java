package com.ntxdev.zuptecnico.api.callbacks;

/**
 * Created by igorlira on 5/27/15.
 */
public interface JobListener
{
    public void onJobSuccess(int jobId);
    public void onJobFailed(int jobId);
}
