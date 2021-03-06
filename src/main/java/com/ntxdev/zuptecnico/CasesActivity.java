package com.ntxdev.zuptecnico;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.Interpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.entities.Case;
import com.ntxdev.zuptecnico.entities.Flow;
import com.ntxdev.zuptecnico.entities.collections.CaseCollection;
import com.ntxdev.zuptecnico.entities.collections.FlowCollection;
import com.ntxdev.zuptecnico.ui.InfinityScrollView;
import com.ntxdev.zuptecnico.ui.SingularTabHost;
import com.ntxdev.zuptecnico.ui.UIHelper;
import com.ntxdev.zuptecnico.util.ResizeAnimation;

import java.util.Iterator;

/**
 * Created by igorlira on 7/25/14.
 */
public class CasesActivity extends AppCompatActivity implements SingularTabHost.OnTabChangeListener, InfinityScrollView.OnScrollViewListener
{
    private int _flowId;
    private int _page = 1;
    private String _status = null;
    private int _offlinePage = 1;
    private int _pageJobId;
    private String _sort = "id";

    private PageLoader pageLoader;
    private FlowLoader flowLoader;

    private boolean isWaitingForFlows;
    private Case[] casesWaiting;

    private Case[] casesShown;

    android.support.v7.widget.PopupMenu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.activity_items);
        Zup.getInstance().initStorage(getApplicationContext());
        UIHelper.initActivity(this, true);

        menu = UIHelper.initMenu(this);

        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                selectFlow(menuItem);
                return true;
            }
        });

        SingularTabHost tabHost = (SingularTabHost) findViewById(R.id.tabhost_documents);
        tabHost.setOnTabChangeListener(this);

        InfinityScrollView scroll = (InfinityScrollView) findViewById(R.id.items_scroll);
        scroll.setOnScrollViewListener(this);

        refreshMenu();
        selectFlow(-1, "Todos os casos");

        flowLoader = new FlowLoader();
        flowLoader.execute();

        // Spinner animation
        View image = findViewById(R.id.activity_items_loading_image);
        image.measure(0, 0);

        RotateAnimation animation = new RotateAnimation(360, 0, image.getMeasuredWidth() / 2, image.getMeasuredHeight() / 2);
        animation.setRepeatCount(Animation.INFINITE);
        animation.setDuration(2000);
        animation.setInterpolator(new Interpolator() {
            @Override
            public float getInterpolation(float v) {
                return v;
            }
        });
        findViewById(R.id.activity_items_loading_image).startAnimation(animation);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.documents, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_items_viewdownloaded)
        {
            Intent intent = new Intent(this, DownloadedCasesActivity.class);
            this.startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.clear();

        if(this.pageLoader != null)
            this.pageLoader.cancel(true);

        if(this.flowLoader != null)
            this.flowLoader.cancel(true);

        casesWaiting = null;
        casesShown = null;
        this.flowLoader = null;
        this.pageLoader = null;

        this._page = 1;
        this._status = null;
    }

    @Override
    protected void onResume() {
        super.onResume();

        selectFlow(-1, "Todos os casos");
    }

    class FlowLoader extends AsyncTask<Void, Void, Boolean>
    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            showBigLoading();
            UIHelper.showProgress(CasesActivity.this);
        }

        @Override
        protected Boolean doInBackground(Void... voids)
        {
            FlowCollection flowCollection = Zup.getInstance().retrieveFlows();
            if(flowCollection == null || flowCollection.flows == null)
                return false;

            for(Flow flow : flowCollection.flows)
            {
                //if(!flow.areStepsDownloaded())
                /*if(false)
                {
                    Flow.StepCollection steps = Zup.getInstance().retrieveFlowSteps(flow.id);
                    for (Flow.Step step : steps.steps) {
                        Zup.getInstance().addFlowStep(step);
                    }
                    flow.steps = steps.steps;
                }*/

                Log.d("FLOW", "Processing flow #" + flow.id);
                if(flow.version_id == null && flow.list_versions != null) // Unpublished, let's take other versions
                {
                    Log.d("FLOW", "Flow #" + flow.id + " last version is unpublished. Processing previous versions");
                    for(Flow version : flow.list_versions)
                    {
                        if(version.version_id == null)
                            continue;

                        Log.d("FLOW", "Saving Flow #" + version.id + " v" + version.version_id);

                        if(Zup.getInstance().hasFlow(version.id, version.version_id))
                        {
                            Zup.getInstance().updateFlow(version.id, version.version_id, version);
                        }
                        else
                        {
                            Zup.getInstance().addFlow(version);
                        }
                    }
                }
                else if(flow.version_id != null)
                {
                    Log.d("FLOW", "Saving Flow #" + flow.id + " v" + flow.version_id);

                    if(Zup.getInstance().hasFlow(flow.id, flow.version_id))
                    {
                        Zup.getInstance().updateFlow(flow.id, flow.version_id, flow);
                    }
                    else
                    {
                        Zup.getInstance().addFlow(flow);
                    }
                }
                /*if(Zup.getInstance().hasFlow(flow.id, flow.version_id))
                {
                    Zup.getInstance().updateFlow(flow.id, flow.version_id, flow);
                }
                else
                {
                    Zup.getInstance().addFlow(flow);
                }*/
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean updated) {
            super.onPostExecute(updated);

            UIHelper.hideProgress(CasesActivity.this);

            if(updated)
            {
                refreshMenu();
                refreshTabHost();

                hideNoConnectionBar();

                if(isWaitingForFlows)
                {
                    isWaitingForFlows = false;

                    if(casesWaiting != null)
                        fillCases(casesWaiting);

                    casesWaiting = null;
                }
            }
            else
            {
                showNoConnectionBar();
            }
        }
    }

    @Override
    public void onBackPressed() {

    }

    void refreshMenu()
    {
        Iterator<Flow> flows;
        try
        {
            flows = Zup.getInstance().getFlows();
        }
        catch (Exception ex)
        {
            isWaitingForFlows = true;
            Log.e("CASES", "Waiting for flows", ex);
            return;
        }

        menu.getMenu().clear();
        menu.getMenu().add(Menu.NONE, -1, 0, "Todos os casos");

        int i = 0;
        while(flows.hasNext())
        {
            Flow flow = flows.next();
            menu.getMenu().add(Menu.NONE, flow.id, i, flow.title);

            i++;
        }
    }

    void selectFlow(int id, String title)
    {
        _flowId = id;
        refreshTabHost();

        clear();
        _sort = "id";
        _page = 1;
        _offlinePage = 1;
        _pageJobId = 0;
        loadPage();
        UIHelper.setTitle(CasesActivity.this, title);
    }

    void selectFlow(MenuItem menuItem)
    {
        selectFlow(menuItem.getItemId(), menuItem.getTitle().toString());
    }

    void loadPage()
    {
        if(this.pageLoader != null)
            this.pageLoader.cancel(true);

        this.pageLoader = new PageLoader();
        this.pageLoader.execute(_flowId, _page);
    }

    class PageLoader extends AsyncTask<Integer, Void, CaseCollection>
    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if(_page == 1)
                showBigLoading();
            else
                showLoading();
        }

        @Override
        protected CaseCollection doInBackground(Integer... integers) {
            int flow = integers[0];
            int page = integers[1];

            if(flow == -1)
                return Zup.getInstance().retrieveCases(page);
            else
                return Zup.getInstance().retrieveCases(flow, page);
        }

        @Override
        protected void onPostExecute(CaseCollection caseCollection) {
            super.onPostExecute(caseCollection);

            hideLoading();
            hideBigLoading();
            if(caseCollection == null || caseCollection.cases == null)
            {
                showNoConnectionBar();
            }
            else
            {
                hideNoConnectionBar();

                if(caseCollection.cases.length > 0)
                    _page++; // Next page that will be loaded

                _pageJobId = 0;
                fillCases(caseCollection.cases);
            }

            pageLoader = null;
        }
    }

    private View setUpCaseView(Case item)
    {
        Flow _flow;
        try
        {
            _flow = Zup.getInstance().getFlow(item.initial_flow_id, item.flow_version);
        }
        catch(Exception ex)
        {
            return null;
        }

        ViewGroup rootView = (ViewGroup)getLayoutInflater().inflate(R.layout.fragment_documents, null);
        rootView.setTag(R.id.tag_item_id, item.id);
        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CasesActivity.this, CaseDetailsActivity.class);
                intent.putExtra("case_id", (Integer)view.getTag(R.id.tag_item_id));
                CasesActivity.this.startActivityForResult(intent, 0);
                CasesActivity.this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        TextView title = (TextView)rootView.findViewById(R.id.fragment_document_title);
        TextView flow = (TextView)rootView.findViewById(R.id.fragment_document_type);
        TextView description = (TextView)rootView.findViewById(R.id.fragment_document_desc);
        TextView state = (TextView)rootView.findViewById(R.id.fragment_document_statedesc);
        ImageView stateicon = (ImageView)rootView.findViewById(R.id.fragment_document_stateicon);
        View downloadicon = rootView.findViewById(R.id.fragment_document_downloadicon);

        if(Zup.getInstance().hasCase(item.id))
        {
            downloadicon.setVisibility(View.VISIBLE);
        }

        stateicon.setImageDrawable(getResources().getDrawable(Zup.getInstance().getCaseStatusDrawable(item.getStatus())));
        state.setBackgroundColor(Zup.getInstance().getCaseStatusColor(item.getStatus()));
        state.setText(Zup.getInstance().getCaseStatusString(item.getStatus()));

        title.setText("Caso " + item.id);
        flow.setText(_flow.title);// + " v" + item.flow_version);
        description.setText("Data de criação: " + Zup.getInstance().formatIsoDate(item.created_at) + (item.updated_at != null ? " / Última modificação: " + Zup.getInstance().formatIsoDate(item.updated_at): ""));

        return rootView;
    }

    void clear()
    {
        ViewGroup root = (ViewGroup)findViewById(R.id.inventory_items_container);
        root.removeAllViews();
    }

    private void fillCases(Case[] cases)
    {
        casesShown = cases;

        ViewGroup root = (ViewGroup)findViewById(R.id.inventory_items_container);
        for(Case item : cases)
        {
            if(Zup.getInstance().hasCase(item.id))
                Zup.getInstance().updateCase(item, true);

            int i = 0;
            if(_status == null || _status.equals(item.getStatus()))
            {
                View view = setUpCaseView(item);
                if (view == null) // Couldnt find some flow
                {
                    isWaitingForFlows = true;
                    casesWaiting = cases;
                    return;
                }
                root.addView(view);

                TranslateAnimation animation = new TranslateAnimation(root.getWidth() + ((float)root.getWidth() * 0.2f * (float)i), 0, 0, 0);
                animation.setDuration(250);

                AlphaAnimation animation1 = new AlphaAnimation(0, 1);
                animation1.setDuration(250);

                AnimationSet set = new AnimationSet(true);
                set.addAnimation(animation);
                set.addAnimation(animation1);
                view.startAnimation(set);

                i++;
            }
        }
    }

    void showLoading()
    {
        findViewById(R.id.activity_items_loading_old).setVisibility(View.VISIBLE);
    }

    void hideLoading()
    {
        findViewById(R.id.activity_items_loading_old).setVisibility(View.INVISIBLE);
    }

    void showBigLoading()
    {
        findViewById(R.id.activity_items_loading).setVisibility(View.VISIBLE);
    }

    void hideBigLoading()
    {
        findViewById(R.id.activity_items_loading).setVisibility(View.GONE);
    }

    void showNoConnectionBar()
    {
        View view = findViewById(R.id.bar_no_connection);
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if(params.height >= 35)
            return;

        ResizeAnimation animation = new ResizeAnimation(view, 0, 35);
        animation.setDuration(350);

        view.startAnimation(animation);
    }

    void hideNoConnectionBar()
    {
        View view = findViewById(R.id.bar_no_connection);
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if(params.height == 0)
            return;

        ResizeAnimation animation = new ResizeAnimation(view, 35, 0);
        animation.setDuration(350);

        view.startAnimation(animation);
    }

    void refreshTabHost()
    {
        /*Flow flow = null;
        if(_flowId != -1)
            flow = Zup.getInstance().getFlow(_flowId);*/

        SingularTabHost tabHost = (SingularTabHost) findViewById(R.id.tabhost_documents);

        tabHost.removeAllTabs();
        tabHost.addTab("all", "Todos estados");
        tabHost.addTab("pending", "Pendentes");
        tabHost.addTab("active", "Em andamento");
        tabHost.addTab("finished", "Concluídos");
        tabHost.setVisibility(View.VISIBLE);

        /*if(flow != null)
        {
            for (int i = 0; i < flow.resolution_states.length; i++) {
                Flow.ResolutionState resolutionState = flow.resolution_states[i];
                tabHost.addTab(Integer.toString(resolutionState.id), resolutionState.title);
            }

            if (flow.resolution_states.length > 0) {
                tabHost.setVisibility(View.VISIBLE);
            } else {
                tabHost.setVisibility(View.GONE);
            }
        }
        else
        {
            tabHost.setVisibility(View.GONE);
        }*/
    }

    @Override
    public void onScrollChanged(InfinityScrollView v, int l, int t, int oldl, int oldt)
    {
        int height = v.getChildAt(0).getHeight();
        int scrollHeight = v.getHeight();

        int bottom = height - scrollHeight - t;
        if (bottom < 50 * getResources().getDisplayMetrics().density && pageLoader == null) {
            loadPage();
        }
    }

    @Override
    public void onTabChange(SingularTabHost tabHost, String oldIdentifier, String newIdentifier) {
        clear();

        if(newIdentifier.equals("all"))
            _status = null;
        else
            _status = newIdentifier;

        if(casesShown != null)
            fillCases(casesShown);
    }
}
