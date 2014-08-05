package com.ntxdev.zuptecnico;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.api.callbacks.LoginListener;

public class LoginActivity extends ActionBarActivity implements LoginListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().hide();
        Zup.getInstance().initStorage(this.getApplicationContext());
        if(Zup.getInstance().hasSessionToken())
        {
            Intent intent = new Intent(this.getApplicationContext(), CasesActivity.class);
            this.startActivity(intent);
        }
        //Zup.getInstance().initLocationClient(this);

        if (savedInstanceState == null) {

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

    public void login(View view)
    {
        TextView txtLogin = (TextView)findViewById(R.id.txt_login);
        TextView txtSenha = (TextView)findViewById(R.id.txt_senha);

        String username = txtLogin.getText().toString();
        String password = txtSenha.getText().toString();

        Zup.getInstance().tryLogin(username, password, this);

        findViewById(R.id.login_button).setVisibility(View.GONE);
        findViewById(R.id.login_progress).setVisibility(View.VISIBLE);
        txtLogin.setEnabled(false);
        txtSenha.setEnabled(false);

        //Intent intent = new Intent(this.getApplicationContext(), DocumentsActivity.class);
        //this.startActivity(intent);
    }

    public void onLoginSuccess()
    {
        Zup.getInstance().refreshInventoryItemCategories();
        //Zup.getInstance().refreshInventoryItems();

        TextView txtLogin = (TextView)findViewById(R.id.txt_login);
        TextView txtSenha = (TextView)findViewById(R.id.txt_senha);
        findViewById(R.id.login_button).setVisibility(View.VISIBLE);
        findViewById(R.id.login_progress).setVisibility(View.GONE);
        txtLogin.setEnabled(true);
        txtSenha.setEnabled(true);

        Intent intent = new Intent(this.getApplicationContext(), CasesActivity.class);
        this.startActivity(intent);
    }

    public void onLoginError(int errorCode, String errorDescription)
    {
        TextView txtLogin = (TextView)findViewById(R.id.txt_login);
        TextView txtSenha = (TextView)findViewById(R.id.txt_senha);
        findViewById(R.id.login_button).setVisibility(View.VISIBLE);
        findViewById(R.id.login_progress).setVisibility(View.GONE);
        txtLogin.setEnabled(true);
        txtSenha.setEnabled(true);
    }

}