package com.ntxdev.zuptecnico;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.entities.Case;
import com.ntxdev.zuptecnico.entities.Flow;
import com.ntxdev.zuptecnico.entities.InventoryCategory;
import com.ntxdev.zuptecnico.entities.InventoryCategoryStatus;
import com.ntxdev.zuptecnico.entities.InventoryItem;
import com.ntxdev.zuptecnico.ui.InfinityScrollView;
import com.ntxdev.zuptecnico.ui.SingularTabHost;
import com.ntxdev.zuptecnico.ui.UIHelper;
import com.ntxdev.zuptecnico.util.ResizeAnimation;

import java.util.ArrayList;
import java.util.Iterator;

public class DownloadedCasesActivity extends ActionBarActivity implements SingularTabHost.OnTabChangeListener, InfinityScrollView.OnScrollViewListener
{
    private int _flowId;
    private int _page = 1;
    private String _status = null;

    private Case[] casesWaiting;

    private Case[] casesShown;

    android.support.v7.widget.PopupMenu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items);

        this.setContentView(R.layout.activity_items);
        Zup.getInstance().initStorage(getApplicationContext());
        UIHelper.initActivity(this, false);

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
    }

    protected void onResume() {
        super.onResume();

        selectFlow(-1, "Todos os casos");
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

    void clear()
    {
        ((ViewGroup)findViewById(R.id.inventory_items_container)).removeAllViews();
    }

    void selectFlow(int id, String title)
    {
        _flowId = id;
        refreshTabHost();

        clear();
        _page = 1;
        loadPage();
        UIHelper.setTitle(this, title);
    }

    void refreshTabHost() {

        SingularTabHost tabHost = (SingularTabHost) findViewById(R.id.tabhost_documents);

        tabHost.removeAllTabs();
        tabHost.addTab("all", "Todos estados");
        tabHost.addTab("pending", "Pendentes");
        tabHost.addTab("active", "Em andamento");
        tabHost.addTab("finished", "Concluídos");
        tabHost.setVisibility(View.VISIBLE);
    }

    void selectFlow(MenuItem menuItem)
    {
        selectFlow(menuItem.getItemId(), menuItem.getTitle().toString());
    }

    void loadPage()
    {
        Iterator<Case> iterator;
        if(_flowId == -1)
            iterator = Zup.getInstance().getCasesIterator();
        else
            iterator = Zup.getInstance().getCasesIterator(_flowId);
        ArrayList<Case> cases = new ArrayList<Case>();

        while(iterator.hasNext())
        {
            cases.add(iterator.next());
        }

        fillCases(cases.toArray(new Case[0]));

        _page++;
    }

    void showLoading()
    {
        findViewById(R.id.activity_items_loading).setVisibility(View.VISIBLE);
    }

    void hideLoading()
    {
        findViewById(R.id.activity_items_loading).setVisibility(View.INVISIBLE);
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

    @Override
    public void onScrollChanged(InfinityScrollView v, int l, int t, int oldl, int oldt) {

    }

    @Override
    public void onTabChange(SingularTabHost tabHost, String oldIdentifier, String newIdentifier) {
        if(newIdentifier.equals("all"))
            _status = null;
        else
            _status = newIdentifier;

        if(casesShown != null)
            fillCases(casesShown);
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
                Intent intent = new Intent(DownloadedCasesActivity.this, CaseDetailsActivity.class);
                intent.putExtra("case_id", (Integer)view.getTag(R.id.tag_item_id));
                DownloadedCasesActivity.this.startActivityForResult(intent, 0);
                DownloadedCasesActivity.this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
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

    private void fillCases(Case[] cases)
    {
        casesShown = cases;

        ViewGroup root = (ViewGroup)findViewById(R.id.inventory_items_container);
        root.removeAllViews();
        for(Case item : cases)
        {
            //if(Zup.getInstance().hasCase(item.id))
            //    Zup.getInstance().updateCase(item);

            if(_status == null || _status.equals(item.getStatus()))
            {
                View view = setUpCaseView(item);
                if (view == null) // Couldnt find some flow
                {
                    casesWaiting = cases;
                    return;
                }
                root.addView(view);
            }
        }
    }
}
