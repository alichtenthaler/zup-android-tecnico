package com.ntxdev.zuptecnico;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
    private final int REQUEST_STEP_FILL = 1;

    private CaseLoader caseLoader;
    private StepLoader stepLoader;
    private Case _case;

    private int _caseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_document_details);

        Zup.getInstance().initStorage(this);
        UIHelper.initActivity(this, false);

        int caseId = getIntent().getIntExtra("case_id", -1);
        if(caseId == -1)
            return;

        this._caseId = caseId;

        loadCase();
    }

    void loadCase()
    {
        showLoadingScreen();

        caseLoader = new CaseLoader();
        caseLoader.execute(_caseId);
    }

    void hideLoadingScreen()
    {
        findViewById(R.id.document_details_loading).setVisibility(View.GONE);
    }

    void showLoadingScreen()
    {
        findViewById(R.id.document_details_loading).setVisibility(View.VISIBLE);
    }

    View createStepView(Case item, Flow flow, Flow.Step step, boolean done)
    {
        Case.Step stepData = item.getStep(step.id);
        String statusT = "Status desconhecido";
        if(done || (stepData != null && stepData.executed))
        {
            statusT = "Finalizado";
        }
        else if (item.current_step != null && item.current_step.step_id == step.id && !item.current_step.executed)
        {
            statusT = "Em andamento";
        }
        else// if(item.next_step_id == step.id)
        {
            statusT = "Não iniciado";
        }

        ViewGroup root = (ViewGroup)getLayoutInflater().inflate(R.layout.case_step_item, null);

        TextView name = (TextView) root.findViewById(R.id.case_step_item_name);
        TextView owner = (TextView) root.findViewById(R.id.case_step_item_owner);
        TextView status = (TextView) root.findViewById(R.id.case_step_item_status);

        name.setText(step.title);
        if(stepData != null && stepData.hasResponsibleUser())
            Zup.getInstance().showUsernameInto(owner, "Condutor: ", stepData.responsible_user_id);
        else
            owner.setText("Sem condutor");
        status.setText(statusT);

        return root;
    }

    @Override
    public void startActivity(Intent intent)
    {
        super.startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    void openStep(int stepId, int flowId, Case thecase)
    {
        Intent intent = new Intent(this, ViewCaseStepFormActivity.class);
        intent.putExtra("step_id", stepId);
        intent.putExtra("flow_id", flowId);
        intent.putExtra("case", thecase);

        this.startActivityForResult(intent, REQUEST_STEP_FILL);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_STEP_FILL && resultCode == ViewCaseStepFormActivity.RESULT_STEP_EDITED)
        {
            loadCase();
        }
    }

    void fillContainer(Case item, Flow initialFlow)
    {
        ViewGroup container = (ViewGroup)findViewById(R.id.document_details_container);
        View view = getLayoutInflater().inflate(R.layout.inventory_item_section_header, null);

        TextView sectionTitle = (TextView)view.findViewById(R.id.inventory_item_section_title);
        sectionTitle.setText("ETAPAS");

        container.removeAllViews();
        container.addView(view);

        boolean done = true;
        for(int i = 0; i < initialFlow.steps.length; i++)
        {
            Flow.Step step = initialFlow.steps[i];
            if(item.next_step_id != null && item.next_step_id == step.id)
                done = false;

            View stepView = createStepView(item, initialFlow, step, done);
            stepView.setTag(R.id.tag_case, item);
            stepView.setTag(R.id.tag_step_id, step.id);
            stepView.setTag(R.id.tag_flow_id, initialFlow.id);
            stepView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openStep((Integer)view.getTag(R.id.tag_step_id), (Integer)view.getTag(R.id.tag_flow_id), (Case)view.getTag(R.id.tag_case));
                }
            });

            if(!done)
                stepView.setBackgroundDrawable(getResources().getDrawable(R.drawable.case_item_cell_new));

            container.addView(stepView);
        }
    }

    void fillCaseData(Case item, Flow initialFlow)
    {
        UIHelper.setTitle(this, "Caso " + item.id);
        TextView title = (TextView)findViewById(R.id.document_details_title);
        TextView flow = (TextView)findViewById(R.id.document_details_type);
        TextView description = (TextView)findViewById(R.id.document_details_desc);
        TextView state = (TextView)findViewById(R.id.document_details_state_desc);
        ImageView stateicon = (ImageView)findViewById(R.id.document_details_state_icon);

        stateicon.setImageDrawable(getResources().getDrawable(Zup.getInstance().getCaseStatusBigDrawable(item.status)));
        state.setBackgroundColor(Zup.getInstance().getCaseStatusColor(item.status));
        state.setText(Zup.getInstance().getCaseStatusString(item.status));

        title.setText("Caso " + item.id);
        flow.setText(initialFlow.title);
        if(item.created_at != null)
            description.setText("Data de criação: " + Zup.getInstance().formatIsoDate(item.created_at) + (item.updated_at != null ? "\nÚltima modificação: " + Zup.getInstance().formatIsoDate(item.updated_at) : ""));
        else
            description.setVisibility(View.INVISIBLE);

        fillContainer(item, initialFlow);

        hideLoadingScreen();
    }

    void downloadSteps(Flow flow)
    {
        stepLoader = new StepLoader();
        stepLoader.execute(flow);
    }

    class StepLoader extends AsyncTask<Flow, Void, Flow.StepCollection>
    {
        Flow flow;
        @Override
        protected Flow.StepCollection doInBackground(Flow... flows)
        {
            this.flow = flows[0];
            return Zup.getInstance().retrieveFlowSteps(flows[0].id);
        }

        @Override
        protected void onPostExecute(Flow.StepCollection stepCollection)
        {
            super.onPostExecute(stepCollection);

            if(stepCollection == null || stepCollection.steps == null)
            {
                // TODO sem conexao?
                return;
            }

            this.flow.steps = stepCollection.steps;
            Zup.getInstance().updateFlow(this.flow.id, this.flow);

            fillCaseData(_case, this.flow);
        }
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

            _case = aCase;

            if(aCase == null)
            {
                Toast.makeText(CaseDetailsActivity.this, "Sem conexão", 3);
                return;
            }

            Flow initialFlow = Zup.getInstance().getFlow(aCase.initial_flow_id);
            if(initialFlow.areStepsDownloaded())
                fillCaseData(aCase, initialFlow);
            else
            {
                downloadSteps(initialFlow);
            }
        }
    }
}
