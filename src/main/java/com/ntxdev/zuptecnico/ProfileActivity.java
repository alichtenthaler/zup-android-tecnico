package com.ntxdev.zuptecnico;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.entities.User;
import com.ntxdev.zuptecnico.ui.UIHelper;

public class ProfileActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Zup.getInstance().initStorage(getApplicationContext());

        User user = Zup.getInstance().getSessionUser();

        TextView txtName = (TextView)findViewById(R.id.profile_name);
        TextView txtUsername = (TextView)findViewById(R.id.txt_profile_username);
        TextView txtCoordinator = (TextView)findViewById(R.id.txt_profile_coordinator);
        TextView txtEmail = (TextView)findViewById(R.id.txt_profile_email);
        TextView txtGroup = (TextView)findViewById(R.id.txt_profile_group);

        txtName.setText(user.name);
        txtEmail.setText(user.email);

        UIHelper.initActivity(this, true);
        UIHelper.setTitle(this, user.name);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.profile, menu);
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

}
