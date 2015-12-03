package com.ntxdev.zuptecnico;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.entities.Document;
import com.ntxdev.zuptecnico.ui.UIHelper;

/**
 * Created by igorlira on 2/22/14.
 */
public class DocumentDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_details);

        Zup.getInstance().initStorage(getApplicationContext());

        UIHelper.initActivity(this, false);

        ImageView imgStateIcon = (ImageView)findViewById(R.id.document_details_state_icon);
        TextView txtStateDesc = (TextView)findViewById(R.id.document_details_state_desc);
        TextView txtTitle = (TextView)findViewById(R.id.document_details_title);
        TextView txtType = (TextView)findViewById(R.id.document_details_type);
        TextView txtDesc = (TextView)findViewById(R.id.document_details_desc);

        Intent intent = getIntent();
        int documentId = intent.getIntExtra("document_id", -1);
        Document document = Zup.getInstance().getDocument(documentId);


        UIHelper.setTitle(this, "Documento #" + document.getId());
        imgStateIcon.setImageDrawable(getResources().getDrawable(Zup.getInstance().getDocumentStateBigDrawable(document.getState())));
        txtStateDesc.setBackgroundColor(Zup.getInstance().getDocumentStateColor(document.getState()));

        txtTitle.setText("Documento #" + document.getId());
        txtType.setText("Árvore");
        txtDesc.setText("Data de criação: ...");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items_list to the action bar if it is present.
        getMenuInflater().inflate(R.menu.document_details, menu);
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