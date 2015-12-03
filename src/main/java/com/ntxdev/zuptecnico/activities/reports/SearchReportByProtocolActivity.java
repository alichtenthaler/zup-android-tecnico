package com.ntxdev.zuptecnico.activities.reports;

import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import com.ntxdev.zuptecnico.R;
import com.ntxdev.zuptecnico.ui.UIHelper;

public class SearchReportByProtocolActivity extends ReportsListActivity {
    private String query;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_reports_list, menu);

        final SearchView search = (SearchView) menu.findItem(R.id.action_search).getActionView();
        search.setQueryHint(getString(R.string.search_by_protocol_hint));
        search.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                finish();
                return true;
            }
        });
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                updateQuery(query);
                return true;
            }
        });
        search.setIconifiedByDefault(false);
        search.requestFocus();
        return true;
    }

    @Override
    void loadPage() {
        updateQuery();
    }

    void updateQuery(String query){
        this.query = query;
        updateQuery();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateQuery();
    }

    void updateQuery(){
        if (query != null && !query.trim().isEmpty()) {
            adapter.setQuery(query);
            showLoading();
        } else {
            adapter.clear();
            hideLoading();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return true;
    }

    @Override
    protected void loadUI() {
        UIHelper.initActivity(this, false);
        UIHelper.setTitle(this, "Relatos");
    }
}
