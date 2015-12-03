package com.ntxdev.zuptecnico.activities.reports;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ntxdev.zuptecnico.R;
import com.ntxdev.zuptecnico.adapters.OfflineReportsAdapter;
import com.ntxdev.zuptecnico.adapters.ReportsAdapter;
import com.ntxdev.zuptecnico.api.DeleteReportItemSyncAction;
import com.ntxdev.zuptecnico.api.EditReportItemSyncAction;
import com.ntxdev.zuptecnico.api.PublishReportItemSyncAction;
import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.entities.ReportItem;
import com.ntxdev.zuptecnico.fragments.reports.FilterReportsFragment;
import com.ntxdev.zuptecnico.fragments.reports.ReportCategorySelectorDialog;
import com.ntxdev.zuptecnico.fragments.reports.ReportsMapFragment;
import com.ntxdev.zuptecnico.ui.UIHelper;

/**
 * Created by igorlira on 7/18/15.
 */
public class ReportsListActivity extends AppCompatActivity implements AdapterView.OnItemClickListener,
        ReportsAdapter.ReportsAdapterListener, ReportsMapFragment.Listener {
    private static final int REQUEST_SHOWREPORT = 1;
    private static final int REQUEST_FILTER = 2;

    ListView mListView;
    ReportsAdapter adapter;
    boolean isOffline;
    ReportsMapFragment mMapFragment;
    private View mMapContainer;
    FilterReportsFragment.FilterOptions optionsQuery;

    private Menu mMenu;
    private boolean mMapMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.activity_reports_list);

        loadUI();


        this.adapter = new ReportsAdapter(this);
        this.adapter.setListener(this);

        mMapContainer = findViewById(R.id.reports_map_container);

        mListView = (ListView) findViewById(R.id.listView);
        mListView.setAdapter(this.adapter);
        mListView.setDividerHeight(0);
        mListView.setOnItemClickListener(this);

        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                reportPublished((ReportItem) intent.getSerializableExtra("report"));
            }
        }, new IntentFilter(PublishReportItemSyncAction.REPORT_PUBLISHED));
        manager.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                reportDeleted(intent.getIntExtra("report_id", -1));
            }
        }, new IntentFilter(DeleteReportItemSyncAction.REPORT_DELETED));
        manager.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                reportEdited((ReportItem) intent.getSerializableExtra("report"));
            }
        }, new IntentFilter(EditReportItemSyncAction.REPORT_EDITED));

        findViewById(R.id.offline_warning).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goOnline();
            }
        });

        boolean canCreate = Zup.getInstance().getAccess().canCreateReportItem();
        findViewById(R.id.report_create_button).setVisibility(canCreate ? View.VISIBLE : View.GONE);
    }

    protected void loadUI() {
        UIHelper.initActivity(this, true);
        UIHelper.setTitle(this, "Relatos");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_items_filter:
                intent = new Intent(this, FilterReportsActivity.class);
                if (optionsQuery != null) {
                    intent.putExtra(FilterReportsActivity.FILTER_OPTIONS, optionsQuery);
                }
                this.startActivityForResult(intent, REQUEST_FILTER);
                return true;
            case R.id.action_search:
                intent = new Intent(this, SearchReportByProtocolActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_map:
                showMap();
                return true;
            case R.id.action_list:
                hideMap();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;

        getMenuInflater().inflate(R.menu.reports_list, menu);
        refreshMenu();

        return true;
    }

    @Override
    public void openReportItem(int id) {
        Intent intent = new Intent(this, ReportItemDetailsActivity.class);
        intent.putExtra("item_id", (int) id);
        startActivityForResult(intent, REQUEST_SHOWREPORT);
    }

    private void refreshMenu() {
        mMenu.findItem(R.id.action_list).setVisible(mMapMode);
        mMenu.findItem(R.id.action_map).setVisible(!mMapMode);
    }

    private void showMap() {
        if (mMapFragment == null) {
            mMapFragment = new ReportsMapFragment();
            mMapFragment.setListener(this);
        }

        mMapContainer.setVisibility(View.VISIBLE);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.reports_map_container, mMapFragment)
                .commit();

        mMapMode = true;
        refreshMenu();
    }

    private void hideMap() {
        if (mMapFragment == null) {
            return;
        }

        mMapContainer.setVisibility(View.GONE);

        getSupportFragmentManager().beginTransaction()
                .remove(mMapFragment)
                .commit();

        mMapMode = false;
        refreshMenu();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Update our listing because we may be offline now
        loadPage();
    }

    private void reportEdited(ReportItem item) {
        // Load list again
        loadPage();
    }

    void reportDeleted(int id) {
        // Load list again
        loadPage();

        Toast.makeText(this, getString(R.string.report_deleted), Toast.LENGTH_SHORT).show();
    }

    void showNoConnectionBar() {
        findViewById(R.id.offline_warning).setVisibility(View.VISIBLE);
    }

    void hideNoConnectionBar() {
        findViewById(R.id.offline_warning).setVisibility(View.GONE);
    }

    void reportPublished(ReportItem item) {
        if (item == null)
            return;

        Toast.makeText(this, getString(R.string.message_report_created), Toast.LENGTH_SHORT).show();
        openReportItem(item.id);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long itemId) {
        if (itemId <= 0)
            return;

        openReportItem((int) itemId);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_FILTER && resultCode == RESULT_OK) {
            optionsQuery = (FilterReportsFragment.FilterOptions) data.getParcelableExtra(FilterReportsActivity.FILTER_OPTIONS);
            if (optionsQuery != null) {
                this.adapter.setFilterOptions(optionsQuery.getQueryMap());
            }
            showLoading();
        } else if (requestCode == REQUEST_SHOWREPORT
                && resultCode == ReportItemDetailsActivity.RESULT_DELETED) {
            // Remove all items and show the loading bar while the report is being deleted
            this.adapter.clear();
            showLoading();

            Toast.makeText(this, getString(R.string.deleting_report), Toast.LENGTH_SHORT).show();
        } else if (requestCode == REQUEST_SHOWREPORT
                && resultCode == ReportItemDetailsActivity.RESULT_CHANGED) {
            this.adapter.reset();

            if (!isOffline) {
                showLoading();
            }
        }
    }

    public void showCreateDialog(View sender) {
        ReportCategorySelectorDialog dialog = new ReportCategorySelectorDialog();
        dialog.show(getSupportFragmentManager(), "category_list");
        dialog.setListener(new ReportCategorySelectorDialog.OnReportCategorySetListener() {
            @Override
            public void onReportCategorySet(int categoryId) {
                createItem(categoryId);
            }
        });
    }

    void createItem(int categoryId) {
        Intent intent = new Intent(this, CreateReportItemActivity.class);
        intent.putExtra("categoryId", categoryId);
        startActivity(intent);
    }

    void showLoading() {
        hideResultsView();
        findViewById(R.id.loading).setVisibility(View.VISIBLE);
    }

    void hideLoading() {
        findViewById(R.id.loading).setVisibility(View.GONE);
    }

    void loadPage() {
        showLoading();
        this.adapter.reset();
    }

    @Override
    public void onReportsLoaded() {
        this.hideLoading();
        if (adapter.getSizeOfRealList() > 0) {
            showResultsView();
        } else {
            hideResultsView();
        }
    }

    @Override
    public void onEmptyResultsLoaded() {
        Toast.makeText(this, getString(R.string.no_results_found), Toast.LENGTH_SHORT).show();
        hideLoading();

    }

    private void showResultsView() {
        findViewById(R.id.layout_result_search).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.results_size_text)).setText(String.valueOf(adapter.getSizeOfRealList()));
    }

    private void hideResultsView() {
        findViewById(R.id.layout_result_search).setVisibility(View.GONE);
    }

    @Override
    public void onNetworkError() {
        this.adapter = new OfflineReportsAdapter(this);
        this.adapter.setListener(this);
        mListView.setAdapter(this.adapter);

        this.isOffline = true;
        showNoConnectionBar();
        hideLoading();

    }

    void goOnline() {
        this.adapter = new ReportsAdapter(this);
        this.adapter.setListener(this);
        mListView.setAdapter(this.adapter);

        this.isOffline = false;
        hideNoConnectionBar();

        loadPage();
    }
}
