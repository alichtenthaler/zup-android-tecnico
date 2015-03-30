package com.ntxdev.zuptecnico;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.entities.Case;
import com.ntxdev.zuptecnico.entities.Flow;
import com.ntxdev.zuptecnico.entities.collections.SingleFlowCollection;
import com.ntxdev.zuptecnico.ui.UIHelper;

/**
 * Created by igorlira on 7/26/14.
 */
public class CaseDetailsActivity extends ActionBarActivity
{
    private final int REQUEST_STEP_FILL = 1;

    private CaseLoader caseLoader;
    private StepLoader stepLoader;
    private FieldLoader fieldLoader;
    private Case _case;
    private Flow _flow;

    private int _flowId = -1;
    private int _flowVersion = -1;

    private int _caseId;

    private Menu _menu;

    private boolean isNew = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_document_details);

        Zup.getInstance().initStorage(this);
        UIHelper.initActivity(this, false);

        int caseId = getIntent().getIntExtra("case_id", -1);
        if(caseId == -1)
            return;

        isNew = getIntent().getBooleanExtra("is_new", false);
        _flowId = getIntent().getIntExtra("flow_id", -1);
        _flowVersion = getIntent().getIntExtra("flow_version", -1);
        this._case = (Case) getIntent().getSerializableExtra("case");

        if(this._case == null)
        {
            this._case = Zup.getInstance().getCase(caseId);
        }

        if(_flowId != -1)
        {
            findViewById(R.id.document_details_header).setVisibility(View.GONE);
        }

        this._caseId = caseId;

        if(this._case == null)
            loadCase();
        else
            caseDownloaded();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.document_details, menu);

        _menu = menu;
        refreshMenu();

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_items_download && this._case != null && !Zup.getInstance().hasCase(this._caseId))
        {
            Zup.getInstance().addCase(this._case);
            refreshMenu();
        }
        else if(item.getItemId() == R.id.action_items_delete_download &&Zup.getInstance().hasCase(this._caseId))
        {
            Zup.getInstance().removeCase(this._caseId);
            refreshMenu();
        }
        return super.onOptionsItemSelected(item);
    }

    void refreshMenu()
    {
        if(_menu == null)
            return;

        MenuItem download = _menu.findItem(R.id.action_items_download);
        MenuItem downloadDelete = _menu.findItem(R.id.action_items_delete_download);

        if(Zup.getInstance().hasCase(_caseId))
        {
            download.setVisible(false);
            downloadDelete.setVisible(true);
        }
        else
        {
            download.setVisible(true);
            downloadDelete.setVisible(false);
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        refreshMenu();

        Case updatedCase = Zup.getInstance().getCase(_caseId);
        if(updatedCase != null)
            _case = updatedCase;

        if(_case != null && _flow != null)
            fillCaseData(_case, _flow);
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
        Flow childFlow = null;
        if(step.step_type.equals("flow"))
            childFlow = Zup.getInstance().getFlow(step.getChildFlowId(), step.getChildFlowVersion());

        Case.Step stepData = item.getStep(step.id);
        String statusT = "Status desconhecido";
        if(!isNew && (done || (stepData != null && stepData.executed)))
        {
            statusT = "Finalizado";
        }
        else if ((item.current_step != null && item.current_step.step_id == step.id && !item.current_step.executed) ||
                (item.current_step != null && childFlow != null && childFlow.getStep(item.current_step.step_id) != null))
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
        if(step.step_type.equals("flow")) {
            //Flow flow1 = Zup.getInstance().getFlow(step.getChildFlowId(), step.getChildFlowVersion());
            name.setText(step.title);

            /*if(flow1 != null)
                name.setText(step.title + " (" + flow1 + " v" + flow1.last_version + ")");
            else
                name.setText(step.title + " (#" + step.getChildFlowId() + " v" + step.getChildFlowVersion() + " NOT PRESENT)");
                */
        }

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

    void openSubflow(int flowId, int flowVersion, Case thecase, boolean done)
    {
        Intent intent = new Intent(this, CaseDetailsActivity.class);
        intent.putExtra("case_id", thecase.id);
        intent.putExtra("flow_id", flowId);
        intent.putExtra("flow_version", flowVersion);
        intent.putExtra("case", thecase);
        intent.putExtra("is_new", !done);

        this.startActivity(intent);
    }

    void openStep(int stepId, int flowId, int flowVersion, Case thecase)
    {
        Intent intent = new Intent(this, ViewCaseStepFormActivity.class);
        intent.putExtra("step_id", stepId);
        intent.putExtra("flow_id", flowId);
        intent.putExtra("flow_version", flowVersion);
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

    void fillContainer(final Case item, Flow initialFlow)
    {
        ViewGroup container = (ViewGroup)findViewById(R.id.document_details_container);
        View view = getLayoutInflater().inflate(R.layout.inventory_item_section_header, null);

        TextView sectionTitle = (TextView)view.findViewById(R.id.inventory_item_section_title);
        sectionTitle.setText("ETAPAS");

        container.removeAllViews();
        container.addView(view);

        boolean done = true;
        if(isNew)
            done = false;

        for(int i = 0; i < initialFlow.steps.length; i++)
        {
            final Flow.Step step = initialFlow.steps[i];
            if(done && item.next_step_id != null && item.next_step_id == step.id)
                done = false;
            else if(done && step.step_type.equals("flow"))
            {
                Flow childFlow = Zup.getInstance().getFlow(step.getChildFlowId(), step.getChildFlowVersion());
                if(childFlow != null && childFlow.getStep(item.next_step_id) != null)
                    done = false;
            }

            final boolean stepdone = done;

            View stepView = createStepView(item, initialFlow, step, done);
            stepView.setTag(R.id.tag_case, item);
            stepView.setTag(R.id.tag_step_id, step.id);
            stepView.setTag(R.id.tag_flow_id, initialFlow.id);
            //stepView.setTag(R.id.tag_flow_version, item.flow_version);
            stepView.setTag(R.id.tag_flow_version, initialFlow.version_id);
            stepView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(step.step_type.equals("flow"))
                        openSubflow(step.getChildFlowId(), step.getChildFlowVersion(), item, stepdone);
                    else
                        openStep((Integer)view.getTag(R.id.tag_step_id), (Integer)view.getTag(R.id.tag_flow_id), (Integer)view.getTag(R.id.tag_flow_version), (Case)view.getTag(R.id.tag_case));
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

    class StepLoader extends AsyncTask<Flow, Void, SingleFlowCollection>
    {
        Flow flow;
        @Override
        protected SingleFlowCollection doInBackground(Flow... flows)
        {
            this.flow = flows[0];
            return Zup.getInstance().retrieveFlowVersion(flows[0].id, flows[0].version_id);
        }

        @Override
        protected void onPostExecute(SingleFlowCollection stepCollection)
        {
            super.onPostExecute(stepCollection);

            if(stepCollection == null || stepCollection.flow == null)
            {
                // TODO sem conexao?
                return;
            }

            this.flow = stepCollection.flow;
            Zup.getInstance().updateFlow(this.flow.id, this.flow.version_id, this.flow);

            /*for(Flow.Step step : stepCollection.steps)
            {
                Zup.getInstance().addFlowStep(step);
            }

            //this.flow.steps = stepCollection.steps;
            Zup.getInstance().updateFlow(this.flow.id, this.flow.version_id, this.flow);*/

            stepsDownloaded(this.flow);
        }
    }

    void stepsDownloaded(Flow flow)
    {
        boolean fieldsDownloaded = true;

        for(Flow.Step step : flow.steps)
        {
            if(!step.areFieldsDownloaded())
            {
                fieldsDownloaded = false;
                break;
            }
        }

        if(fieldsDownloaded)
            fieldsDownloaded(flow);
        else
            downloadFields(flow);
    }

    void downloadFields(Flow flow)
    {

    }

    void fieldsDownloaded(Flow flow)
    {
        _flow = flow;
        fillCaseData(_case, flow);
    }

    void caseDownloaded()
    {
        Case aCase = _case;

        Flow initialFlow;
        if(_flowId == -1)
            initialFlow = Zup.getInstance().getFlow(aCase.initial_flow_id, aCase.flow_version);
        else
            initialFlow = Zup.getInstance().getFlow(_flowId, _flowVersion);

        if(initialFlow.areStepsDownloaded())
            stepsDownloaded(initialFlow);
            //fillCaseData(aCase, initialFlow);
        else
        {
            downloadSteps(initialFlow);
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
                Toast.makeText(CaseDetailsActivity.this, "Sem conexão", Toast.LENGTH_LONG).show();
                return;
            }

            caseDownloaded();
        }
    }

    class FieldLoader extends AsyncTask<Integer, Void, Flow>
    {
        @Override
        protected Flow doInBackground(Integer... integers)
        {
            return null; //Zup.getInstance().retrieveCase(integers[0]);
        }

        @Override
        protected void onPostExecute(Flow aCase) {
            super.onPostExecute(aCase);

            //_case = aCase;

            if(aCase == null)
            {
                Toast.makeText(CaseDetailsActivity.this, "Sem conexão", Toast.LENGTH_LONG).show();
                return;
            }

            caseDownloaded();
        }
    }
}
