package com.ntxdev.zuptecnico;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.entities.Group;
import com.ntxdev.zuptecnico.entities.Session;
import com.ntxdev.zuptecnico.entities.User;
import com.ntxdev.zuptecnico.util.ViewUtils;

import io.fabric.sdk.android.Fabric;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class LoginActivity extends AppCompatActivity implements Callback<Session> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }
        setContentView(R.layout.activity_login);

        Zup.getInstance().initStorage(this.getApplicationContext());
        if (Zup.getInstance().hasSessionToken()) {
            Intent intent = new Intent(this.getApplicationContext(), LoadingDataActivity.class);
            this.startActivity(intent);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items_list to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void login(View view) {
        TextView txtLogin = (TextView) findViewById(R.id.txt_login);
        TextView txtSenha = (TextView) findViewById(R.id.txt_senha);

        String username = txtLogin.getText().toString();
        String password = txtSenha.getText().toString();

        Zup.getInstance().getService().authenticate(username, password, this);
        //Zup.getInstance().tryLogin(username, password, this);

        findViewById(R.id.login_button).setVisibility(View.GONE);
        findViewById(R.id.login_progress).setVisibility(View.VISIBLE);
        txtLogin.setEnabled(false);
        txtSenha.setEnabled(false);
    }

    public void onLoginSuccess() {
        TextView txtLogin = (TextView) findViewById(R.id.txt_login);
        TextView txtSenha = (TextView) findViewById(R.id.txt_senha);
        findViewById(R.id.login_button).setVisibility(View.VISIBLE);
        findViewById(R.id.login_progress).setVisibility(View.GONE);
        txtLogin.setEnabled(true);
        txtSenha.setEnabled(true);

        ViewUtils.hideKeyboard(this, txtLogin.getWindowToken());

        Intent intent = new Intent(this.getApplicationContext(), LoadingDataActivity.class);
        this.startActivity(intent);
    }

    public void onLoginError(String errorDescription) {
        TextView txtLogin = (TextView) findViewById(R.id.txt_login);
        TextView txtSenha = (TextView) findViewById(R.id.txt_senha);
        findViewById(R.id.login_button).setVisibility(View.VISIBLE);
        findViewById(R.id.login_progress).setVisibility(View.GONE);
        txtLogin.setEnabled(true);
        txtSenha.setEnabled(true);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.error_title));
        builder.setMessage(errorDescription);
        builder.setCancelable(true);
        builder.setPositiveButton(getString(R.string.lab_ok), null);
        builder.show();
    }

    @Override
    public void success(Session session, Response response) {
        User user = session.user;
        Group[] groups = user.groups;
        if (groups != null) {
            for (int index = 0; index < groups.length; index++) {
                Group group = groups[index];
                if (group.getPermissions().panel_access) {
                    Zup.getInstance().getUserService().addUser(session.user);
                    Zup.getInstance().setSession(session);
                    Zup.getInstance().getStorage().addUser(session.user);
                    this.onLoginSuccess();
                    return;
                }
            }
        }
        this.onLoginError(getString(R.string.error_user_not_allowed_message));
    }

    @Override
    public void failure(RetrofitError retrofitError) {
        Session session = (Session) retrofitError.getBodyAs(Session.class);
        if (session != null) {
            Log.e("Error", "Could not login", retrofitError.getCause());
            this.onLoginError(session.error);
        } else {
            this.onLoginError(getString(R.string.error_network));
        }
    }
}
