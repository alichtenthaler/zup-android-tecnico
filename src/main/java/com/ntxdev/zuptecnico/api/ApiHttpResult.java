package com.ntxdev.zuptecnico.api;

/**
 * Created by igorlira on 3/3/14.
 */
public class ApiHttpResult<T>
{
    public boolean success;
    public int statusCode;
    public T result;

    public Class<T> getResultClass()
    {
        return (Class<T>)result.getClass();
    }
}
