package com.ntxdev.zuptecnico;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.RotateAnimation;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.api.callbacks.InventoryItemsListener;
import com.ntxdev.zuptecnico.api.callbacks.JobFailedListener;
import com.ntxdev.zuptecnico.entities.InventoryCategoryStatus;
import com.ntxdev.zuptecnico.entities.InventoryItem;
import com.ntxdev.zuptecnico.entities.InventoryItemFilter;
import com.ntxdev.zuptecnico.entities.MapCluster;
import com.ntxdev.zuptecnico.entities.collections.InventoryItemCollection;
import com.ntxdev.zuptecnico.ui.InfinityScrollView;
import com.ntxdev.zuptecnico.ui.UIHelper;
import com.ntxdev.zuptecnico.util.Utilities;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import retrofit.RetrofitError;

/**
 * Created by igorlira on 4/27/14.
 */
public class InventoryItemsAdvancedSearchResultActivity extends AppCompatActivity implements InfinityScrollView.OnScrollViewListener
{
    private boolean selectMode;
    private Intent searchData;
    private int _page;

    private Tasker tasker;

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
            if(getSupportActionBar() != null)
                getSupportActionBar().hide();

            findViewById(R.id.items_select_buttons).setVisibility(View.VISIBLE);
        }

        _page = 1;
        loadPage();

        InfinityScrollView scroll = (InfinityScrollView) findViewById(R.id.items_scroll);
        scroll.setOnScrollViewListener(this);

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
            intent.putExtra("categoryId", searchData.getIntExtra("categoryId", -1));
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
                hideNoItems();
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
        if (bottom < 50 * getResources().getDisplayMetrics().density && (tasker == null)) {
            loadPage();
        }
    }

    private void clear()
    {
        ((ViewGroup)findViewById(R.id.inventory_items_container)).removeAllViews();
    }

    private void loadPage() {
        if(tasker != null)
            tasker.cancel(true);

        tasker = new Tasker(this.searchData);
        tasker.execute();
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
                intent.putExtra("categoryId", (Integer)view.getTag(R.id.tag_category_id));
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

    void showBigLoading()
    {
        findViewById(R.id.activity_items_loading).setVisibility(View.VISIBLE);
    }

    void hideBigLoading()
    {
        findViewById(R.id.activity_items_loading).setVisibility(View.GONE);
    }

    void showLoading()
    {
        findViewById(R.id.activity_items_loading_old).setVisibility(View.VISIBLE);
    }

    void hideLoading()
    {
        findViewById(R.id.activity_items_loading_old).setVisibility(View.INVISIBLE);
    }

    void showNoItems()
    {
        findViewById(R.id.activity_items_noitems).setVisibility(View.VISIBLE);
    }

    void hideNoItems()
    {
        findViewById(R.id.activity_items_noitems).setVisibility(View.GONE);
    }

    class Tasker extends AsyncTask<Void, Void, InventoryItemCollection>
    {
        int categoryId;

        Map<String, Object> options;

        public Tasker(Intent searchData)
        {
            this.options = new HashMap<String, Object>();

            if(searchData.hasExtra("categoryId"))
            {
                this.categoryId = searchData.getIntExtra("categoryId", 0);
            }

            if(searchData.hasExtra("address"))
                this.options.put("address", searchData.getStringExtra("address"));

            if(searchData.hasExtra("name"))
                this.options.put("title", searchData.getStringExtra("name"));

            if(searchData.hasExtra("creation_from"))
                this.options.put("created_at[begin]", Zup.getIsoDate(((Calendar) searchData.getExtras().get("creation_from")).getTime()));

            if(searchData.hasExtra("creation_to"))
                this.options.put("created_at[end]", Zup.getIsoDate(((Calendar)searchData.getExtras().get("creation_to")).getTime()));

            if(searchData.hasExtra("modification_from"))
                this.options.put("updated_at[begin]", Zup.getIsoDate(((Calendar)searchData.getExtras().get("modification_from")).getTime()));

            if(searchData.hasExtra("modification_to"))
                this.options.put("updated_at[end]", Zup.getIsoDate(((Calendar)searchData.getExtras().get("modification_to")).getTime()));

            if(searchData.hasExtra("latitude"))
                this.options.put("latitude", searchData.getStringExtra("latitude"));

            if(searchData.hasExtra("longitude"))
                this.options.put("longitude", searchData.getStringExtra("longitude"));

            Object[] statuses = (Object[])searchData.getExtras().get("statuses");
            if(statuses != null)
                this.options.put("inventory_statuses_ids", Utilities.joinAsInteger(statuses));

            for(int i = 0; i < 100; i++)
            {
                if(!searchData.hasExtra("filter" + i))
                    break;

                InventoryItemFilter filter = (InventoryItemFilter) searchData.getSerializableExtra("filter" + i);
                filter.serialize(this.options);
            }
        }

        @Override
        protected void onPreExecute()
        {
            if(_page == 1)
                showBigLoading();
            else
                showLoading();
        }

        @Override
        protected InventoryItemCollection doInBackground(Void... voids)
        {
            try
            {
                return Zup.getInstance().getService().searchInventoryItems(this.categoryId, _page, this.options);
            }
            catch (RetrofitError error)
            {
                Log.e("Retrofit", "Could not search inventory items", error);
            }

            return null;
        }

        @Override
        protected void onPostExecute(InventoryItemCollection inventoryItemCollection)
        {
            if(inventoryItemCollection == null || inventoryItemCollection.items == null)
            {
                hideNoItems();
                Toast.makeText(InventoryItemsAdvancedSearchResultActivity.this, "Não foi possível buscar itens.", Toast.LENGTH_LONG).show();
            }
            else if(inventoryItemCollection.items.length == 0)
            {
                showNoItems();
            }
            else
            {
                _page++; // Next page that will be loaded
                hideNoItems();
                fillCategoryItems(inventoryItemCollection.items);
            }
            hideLoading();
            hideBigLoading();

            tasker = null;
        }
    }
}
