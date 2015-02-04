package com.ntxdev.zuptecnico.util;

import android.os.AsyncTask;
import android.widget.TextView;

import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.entities.User;

/**
 * Created by igorlira on 8/8/14.
 */
public class UserLoaderTask extends AsyncTask<Integer, Void, User>
{
    TextView textView;
    String prefix;

    int userId;

    public UserLoaderTask(TextView textView, String prefix)
    {
        this.textView = textView;
        this.prefix = prefix;
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();
        textView.setText(prefix + "Carregando...");
    }

    @Override
    protected User doInBackground(Integer... integers)
    {
        this.userId = integers[0];
        return Zup.getInstance().retrieveUserInfo(integers[0]);
    }

    @Override
    protected void onPostExecute(User user)
    {
        super.onPostExecute(user);
        if(user != null)
        {
            textView.setText(prefix + user.name);
        }
        else
        {
            textView.setText(prefix + "#" + userId);
        }
    }
}
