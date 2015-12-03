package com.ntxdev.zuptecnico;

import android.support.v7.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.entities.User;
import com.ntxdev.zuptecnico.ui.UIHelper;

public class ProfileActivity extends AppCompatActivity {

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
    public void onBackPressed() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.profile, menu);
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

    public void logout(View view)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Tem certeza?");
        builder.setMessage("Tem certeza de que deseja sair? Todos os dados salvos no dispositivo serão apagados.");
        builder.setCancelable(true);
        builder.setPositiveButton("Sair", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                confirmlogout();
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    void confirmlogout()
    {
        Zup.getInstance().clearStorage();

        Intent intent = new Intent(this, LoginActivity.class);
        this.startActivity(intent);
    }

}
