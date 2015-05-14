package com.ntxdev.zuptecnico;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.api.callbacks.InventoryItemsListener;
import com.ntxdev.zuptecnico.api.callbacks.JobFailedListener;
import com.ntxdev.zuptecnico.entities.InventoryCategoryStatus;
import com.ntxdev.zuptecnico.entities.InventoryItem;
import com.ntxdev.zuptecnico.entities.MapCluster;
import com.ntxdev.zuptecnico.ui.InfinityScrollView;
import com.ntxdev.zuptecnico.ui.UIHelper;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by igorlira on 4/27/14.
 */
public class InventoryItemsAdvancedSearchResultActivity extends ActionBarActivity implements InventoryItemsListener, InfinityScrollView.OnScrollViewListener, JobFailedListener
{
    private boolean selectMode;
    private Intent searchData;
    private int _page;
    private int _pageJobId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items);

        Zup.getInstance().initStorage(getApplicationContext());


        UIHelper.initActivity(this, false);
        UIHelper.setTitle(this, "Busca avançada");

        this.searchData = (Intent)getIntent().getExtras().get("search_data");
        this.selectMode = getIntent().getBooleanExtra("select", false);

        if(selectMode)
        {
            getSupportActionBar().hide();
            findViewById(R.id.items_select_buttons).setVisibility(View.VISIBLE);
        }

        _page = 1;
        loadPage();

        InfinityScrollView scroll = (InfinityScrollView) findViewById(R.id.items_scroll);
        scroll.setOnScrollViewListener(this);
    }

    public void selectCancel(View view)
    {
        setResult(RESULT_CANCELED);
        finish();
    }

    public void selectDone(View view)
    {
        Intent intent = new Intent();
        ArrayList<Integer> idsList = new ArrayList<Integer>();

        ViewGroup container = (ViewGroup)findViewById(R.id.inventory_items_container);
        for(int i = 0; i < container.getChildCount(); i++)
        {
            View child = container.getChildAt(i);
            Integer id = (Integer) child.getTag(R.id.tag_item_id);

            if(id == null)
                continue;

            CheckBox checkBox = (CheckBox) child.findViewById(R.id.fragment_inventory_item_check);
            if(checkBox.isChecked()) {
                idsList.add(id);
            }
        }

        int[] res = new int[idsList.size()];
        for(int i = 0; i < res.length; i++)
        {
            res[i] = idsList.get(i);
        }

        intent.putExtra("ids", res);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.items_advanced_search_result, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_items_edit)
        {
            Intent intent = new Intent(this, AdvancedSearchActivity.class);
            intent.putExtra("category_id", searchData.getIntExtra("category_id", -1));
            intent.putExtra("search_data", searchData);
            startActivityForResult(intent, AdvancedSearchActivity.REQUEST_SEARCH);
            overridePendingTransition(R.anim.slide_in_bottom, R.anim.hold);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == AdvancedSearchActivity.REQUEST_SEARCH)
        {
            if(resultCode == AdvancedSearchActivity.RESULT_SEARCH)
            {
                this.searchData = data;
                this.clear();
                _page = 1;
                loadPage();
            }
        }
    }

    @Override
    public void onScrollChanged(InfinityScrollView v, int l, int t, int oldl, int oldt) {
        int height = v.getChildAt(0).getHeight();
        int scrollHeight = v.getHeight();

        int bottom = height - scrollHeight - t;
        if (bottom < 50 * getResources().getDisplayMetrics().density && _pageJobId == 0) {
            loadPage();
        }
    }

    private void clear()
    {
        ((ViewGroup)findViewById(R.id.inventory_items_container)).removeAllViews();
    }

    private void loadPage() {
        int categoryId = searchData.getIntExtra("category_id", 0);
        String address = searchData.getStringExtra("address");
        String title = searchData.getStringExtra("name");
        Calendar creation_from = (Calendar)searchData.getExtras().get("creation_from");
        Calendar creation_to = (Calendar)searchData.getExtras().get("creation_to");
        Calendar modification_from = (Calendar)searchData.getExtras().get("modification_from");
        Calendar modification_to = (Calendar)searchData.getExtras().get("modification_to");
        String raw_latitude = searchData.getStringExtra("latitude");
        String raw_longitude = searchData.getStringExtra("longitude");
        Object[] statuses = (Object[])searchData.getExtras().get("statuses");

        Integer[] sstatuses = null;
        if(statuses != null) {
            sstatuses = new Integer[statuses.length];
            for (int i = 0; i < statuses.length; i++) {
                sstatuses[i] = (Integer) statuses[i];
            }
        }

        Float latitude = null;
        Float longitude = null;

        if(raw_latitude != null && !raw_latitude.equals(""))
        {
            try
            {
                latitude = Float.parseFloat(raw_latitude);
            }
            catch (NumberFormatException ex) { }
        }
        if(raw_longitude != null && !raw_longitude.equals(""))
        {
            try
            {
                longitude = Float.parseFloat(raw_latitude);
            }
            catch (NumberFormatException ex) { }
        }

        _pageJobId = Zup.getInstance().searchInventoryItems(_page, 30, new int[] { categoryId }, sstatuses, address, title, creation_from, creation_to, modification_from, modification_to, latitude, longitude, this, this);

        findViewById(R.id.activity_items_loading).setVisibility(View.VISIBLE);
    }

    private View setUpItemView(InventoryItem item)
    {
        ViewGroup rootView = (ViewGroup)getLayoutInflater().inflate(R.layout.fragment_inventory_item, null);
        rootView.setTag(R.id.tag_item_id, item.id);
        rootView.setTag(R.id.tag_category_id, item.inventory_category_id);
        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InventoryItemsAdvancedSearchResultActivity.this, InventoryItemDetailsActivity.class);
                intent.putExtra("item_id", (Integer)view.getTag(R.id.tag_item_id));
                intent.putExtra("category_id", (Integer)view.getTag(R.id.tag_category_id));
                InventoryItemsAdvancedSearchResultActivity.this.startActivityForResult(intent, 0);
                InventoryItemsAdvancedSearchResultActivity.this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        TextView title = (TextView)rootView.findViewById(R.id.fragment_inventory_item_title);
        TextView description = (TextView)rootView.findViewById(R.id.fragment_inventory_item_desc);
        ImageView downloadIcon = (ImageView)rootView.findViewById(R.id.fragment_inventory_item_download_icon);
        TextView state = (TextView)rootView.findViewById(R.id.fragment_inventory_item_statedesc);
        CheckBox checkBox = (CheckBox)rootView.findViewById(R.id.fragment_inventory_item_check);

        if(selectMode)
        {
            checkBox.setVisibility(View.VISIBLE);
        }

        if(item.inventory_status_id != null)
        {
            InventoryCategoryStatus status = Zup.getInstance().getInventoryCategoryStatus(item.inventory_category_id, item.inventory_status_id);
            if (status != null) {
                state.setText(status.title);
                state.setBackgroundColor(status.getColor());
            }
        }
        else
        {
            state.setVisibility(View.GONE);
        }

        if(item.syncError)
        {
            title.setTextColor(0xffff0000);
        }

        title.setText(Zup.getInstance().getInventoryItemTitle(item));
        description.setText("Incluído em " + Zup.getInstance().formatIsoDate(item.created_at));
        downloadIcon.setVisibility(Zup.getInstance().hasInventoryItem(item.id) ? View.VISIBLE : View.GONE);

        return rootView;
    }

    private void fillCategoryItems(InventoryItem[] items)
    {
        ViewGroup root = (ViewGroup)findViewById(R.id.inventory_items_container);
        for(InventoryItem item : items)
        {
            View view = setUpItemView(item);
            root.addView(view);
        }
    }

    @Override
    public void onInventoryItemsReceived(InventoryItem[] items, int page, int per_page, int[] inventory_category_ids, String address, String title, Calendar creation_from, Calendar creation_to, Calendar modification_from, Calendar modification_to, Float latitude, Float longitude, int job_id) {
        if (job_id == _pageJobId) {
            _page++; // Next page that will be loaded
            _pageJobId = 0;
            fillCategoryItems(items);
            findViewById(R.id.activity_items_loading).setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onInventoryItemsReceived(InventoryItem[] items, int categoryId, int page, int job_id) {

    }

    @Override
    public void onInventoryItemsReceived(InventoryItem[] items, MapCluster[] clusters, double latitude, double longitude, double radius, double zoom, int job_id) {

    }

    @Override
    public void onJobFailed(int job_id) {
        Toast.makeText(this, "Não foi possível buscar itens.", 3).show();
    }
}
