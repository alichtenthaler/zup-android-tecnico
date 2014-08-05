package com.ntxdev.zuptecnico;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.TextView;

import com.ntxdev.zuptecnico.R;
import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.entities.Case;
import com.ntxdev.zuptecnico.entities.Flow;
import com.ntxdev.zuptecnico.ui.UIHelper;

/**
 * Created by igorlira on 7/26/14.
 */
public class CaseDetailsActivity extends ActionBarActivity
{
    private CaseLoader caseLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_document_details);

        Zup.getInstance().initStorage(this);
        UIHelper.initActivity(this, false);

        int caseId = getIntent().getIntExtra("case_id", -1);
        if(caseId == -1)
            return;

        caseLoader = new CaseLoader();
        caseLoader.execute(caseId);
    }

    void fillCaseData(Case item)
    {
        Flow initialFlow = Zup.getInstance().getFlow(item.initial_flow_id);

        UIHelper.setTitle(this, "Caso " + item.id);
        TextView title = (TextView)findViewById(R.id.document_details_title);
        TextView flow = (TextView)findViewById(R.id.document_details_type);
        TextView description = (TextView)findViewById(R.id.document_details_desc);

        title.setText("Caso " + item.id);
        flow.setText(initialFlow.title);
        if(item.created_at != null)
            description.setText("Data de criação: " + Zup.getInstance().formatIsoDate(item.created_at) + (item.updated_at != null ? "\nÚltima modificação: " + Zup.getInstance().formatIsoDate(item.updated_at) : ""));
        else
            description.setVisibility(View.INVISIBLE);
    }

    class CaseLoader extends AsyncTask<Integer, Void, Case>
    {
        @Override
        protected Case doInBackground(Integer... integers)
        {
            return Zup.getInstance().retrieveCase(integers[0]);
        }

        @Override
        protected void onPostExecute(Case aCase) {
            super.onPostExecute(aCase);

            fillCaseData(aCase);
        }
    }
}
