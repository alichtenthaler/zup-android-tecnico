package com.ntxdev.zuptecnico.api.callbacks;

/**
 * Created by igorlira on 3/16/14.
 */
public interface LoginListener {
    public void onLoginSuccess();
    public void onLoginError(int errorCode, String errorDescription);
}
