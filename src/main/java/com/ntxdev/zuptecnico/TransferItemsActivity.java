package com.ntxdev.zuptecnico;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ntxdev.zuptecnico.api.ApiHttpResult;
import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.api.callbacks.InventoryItemsListener;
import com.ntxdev.zuptecnico.api.callbacks.JobFailedListener;
import com.ntxdev.zuptecnico.entities.InventoryCategoryStatus;
import com.ntxdev.zuptecnico.entities.InventoryItem;
import com.ntxdev.zuptecnico.entities.collections.SingleInventoryItemCollection;
import com.ntxdev.zuptecnico.ui.InfinityScrollView;

import java.util.Calendar;

public class TransferItemsActivity extends ActionBarActivity implements View.OnClickListener, InfinityScrollView.OnScrollViewListener, InventoryItemsListener, JobFailedListener {
    private int _categoryId;
    private Integer _stateId;
    private int _page;
    private int _pageJobId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_items);

        Zup.getInstance().initStorage(getApplicationContext());

        _categoryId = getIntent().getIntExtra("category_id", 0);
        _stateId = (Integer)getIntent().getExtras().get("state_id");
        _page = 1;
        loadPage();

        this.getSupportActionBar().hide();
        InfinityScrollView scroll = (InfinityScrollView) findViewById(R.id.items_scroll);
        scroll.setOnScrollViewListener(this);
    }

    private void loadPage()
    {
        if(_stateId != null)
            _pageJobId = Zup.getInstance().searchInventoryItems(_page, 30, new int[] { _categoryId }, new Integer[] { _stateId }, null, null, null, null, null, null, null, null, this, this);
        else
            _pageJobId = Zup.getInstance().requestInventoryItems(_categoryId, _page, this, this);

        findViewById(R.id.activity_items_loading).setVisibility(View.VISIBLE);
    }

    private View setUpItemView(InventoryItem item)
    {
        if(Zup.getInstance().hasInventoryItem(item.id))
            return null;

        ViewGroup rootView = (ViewGroup)getLayoutInflater().inflate(R.layout.fragment_inventory_item_transfer, null);
        rootView.setTag(R.id.tag_item_id, item.id);
        rootView.setTag(R.id.tag_category_id, item.inventory_category_id);
        rootView.setOnClickListener(this);

        TextView title = (TextView)rootView.findViewById(R.id.fragment_inventory_item_title);
        TextView description = (TextView)rootView.findViewById(R.id.fragment_inventory_item_desc);
        ImageView downloadIcon = (ImageView)rootView.findViewById(R.id.fragment_inventory_item_download_icon);
        CheckBox checkBox = (CheckBox)rootView.findViewById(R.id.fragment_inventory_item_check);
        TextView state = (TextView)rootView.findViewById(R.id.fragment_inventory_item_statedesc);

        if(item.inventory_status_id != null)
        {
            InventoryCategoryStatus status = Zup.getInstance().getInventoryCategoryStatus(item.inventory_category_id, item.inventory_status_id);
            if (status != null) {
                state.setText(status.title);
                state.setBackgroundColor(status.getColor());
            }
            else
            {
                state.setVisibility(View.GONE);
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
        downloadIcon.setVisibility(View.GONE);
        checkBox.setChecked(Zup.getInstance().hasInventoryItem(item.id));

        return rootView;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    public void onClick(View view) {
        int itemId = (Integer)view.getTag(R.id.tag_item_id);
        int categoryId = (Integer)view.getTag(R.id.tag_category_id);

        CheckBox checkBox = (CheckBox)view.findViewById(R.id.fragment_inventory_item_check);
        checkBox.setChecked(!checkBox.isChecked());
    }

    @Override
    public void onScrollChanged(InfinityScrollView v, int l, int t, int oldl, int oldt)
    {
        int height = v.getChildAt(0).getHeight();
        int scrollHeight = v.getHeight();

        int bottom = height - scrollHeight - t;
        if (bottom < 50 * getResources().getDisplayMetrics().density && _pageJobId == 0) {
            loadPage();
        }
    }

    @Override
    public void onInventoryItemsReceived(InventoryItem[] items, int page, int per_page, int[] inventory_category_ids, String address, String title, Calendar creation_from, Calendar creation_to, Calendar modification_from, Calendar modification_to, Float latitude, Float longitude, int job_id) {
        if (job_id == _pageJobId && _page == page) {
            _page++; // Next page that will be loaded
            _pageJobId = 0;
            fillCategoryItems(items);
            findViewById(R.id.activity_items_loading).setVisibility(View.GONE);
        }
    }

    @Override
    public void onInventoryItemsReceived(InventoryItem[] items, int categoryId, int page, int job_id) {
        if (job_id == _pageJobId && _page == page && _categoryId == categoryId) {
            _page++; // Next page that will be loaded
            _pageJobId = 0;
            fillCategoryItems(items);
            findViewById(R.id.activity_items_loading).setVisibility(View.GONE);
        }
    }

    private void fillCategoryItems(InventoryItem[] items)
    {
        ViewGroup container = (ViewGroup)findViewById(R.id.inventory_items_container);
        for(InventoryItem item : items)
        {
            View itemView = setUpItemView(item);
            if(itemView != null)
                container.addView(itemView);
        }
    }

    public void cancel(View view)
    {
        finish();
    }

    public void transfer(View view)
    {
        new Tasker().execute();
    }

    private void transferItem(int itemId, int categoryId)
    {
        ApiHttpResult<SingleInventoryItemCollection> result = Zup.getInstance().retrieveInventoryItemInfo(categoryId, itemId);
        if(result.statusCode == 200 || result.statusCode == 201)
            Zup.getInstance().addInventoryItem(result.result.item);
    }

    @Override
    public void onJobFailed(int job_id) {
        Toast.makeText(this, "Não foi possível obter a listagem de itens.", 3).show();
    }

    public class Tasker extends AsyncTask<Void, Void, Void>
    {

        @Override
        protected Void doInBackground(Void... voids) {
            final TextView progress = (TextView)findViewById(R.id.activity_transfer_items_popup_progress);
            final View overlay = findViewById(R.id.activity_transfer_items_overlay);
            final View popup = findViewById(R.id.activity_transfer_items_popup);
            final AlphaAnimation animation = new AlphaAnimation(0, .5f);
            animation.setDuration(300);
            animation.setFillAfter(true);
            final AlphaAnimation animation2 = new AlphaAnimation(0, 1f);
            animation.setDuration(300);
            animation.setFillAfter(true);

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                                         @Override
                                                         public void run() {
                                                             overlay.setVisibility(View.VISIBLE);
                                                             overlay.startAnimation(animation);
                                                             popup.setVisibility(View.VISIBLE);
                                                             popup.startAnimation(animation2);
                                                         }
                                                     });

            int temp_count = 0;
            ViewGroup container = (ViewGroup)findViewById(R.id.inventory_items_container);

            for(int i = 0; i < container.getChildCount(); i++)
            {
                ViewGroup rootView = (ViewGroup)container.getChildAt(i);
                CheckBox checkBox = (CheckBox)rootView.findViewById(R.id.fragment_inventory_item_check);

                if(checkBox.isChecked())
                    temp_count++;
            }

            final int total = temp_count;
            temp_count = 0;
            for(int i = 0; i < container.getChildCount(); i++)
            {
                ViewGroup rootView = (ViewGroup)container.getChildAt(i);
                CheckBox checkBox = (CheckBox)rootView.findViewById(R.id.fragment_inventory_item_check);

                int itemId = (Integer)rootView.getTag(R.id.tag_item_id);
                int categoryId = (Integer)rootView.getTag(R.id.tag_category_id);

                if(checkBox.isChecked()) {
                    transferItem(itemId, categoryId);
                    temp_count++;
                }

                final int downloaded = temp_count;

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        progress.setText(downloaded + " itens transferidos de " + total);
                    }
                });
            }

            finish();

            return null;
        }
    }

    @Override
    public void onInventoryItemsReceived(InventoryItem[] items, double latitude, double longitude, double radius, double zoom, int job_id) {

    }
}
