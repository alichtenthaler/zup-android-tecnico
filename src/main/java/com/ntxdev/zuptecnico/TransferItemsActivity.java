package com.ntxdev.zuptecnico;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import com.ntxdev.zuptecnico.entities.MapCluster;
import com.ntxdev.zuptecnico.entities.collections.InventoryItemCollection;
import com.ntxdev.zuptecnico.entities.collections.SingleInventoryItemCollection;
import com.ntxdev.zuptecnico.ui.InfinityScrollView;

import java.util.ArrayList;
import java.util.Calendar;

import retrofit.RetrofitError;

public class TransferItemsActivity extends AppCompatActivity implements View.OnClickListener, InfinityScrollView.OnScrollViewListener {
    private int _categoryId;
    private Integer _stateId;
    private int _page;
    private PageLoader pageLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_items);

        Zup.getInstance().initStorage(getApplicationContext());

        _categoryId = getIntent().getIntExtra("categoryId", 0);
        _stateId = (Integer)getIntent().getExtras().get("state_id");
        _page = 1;
        loadPage();

        InfinityScrollView scroll = (InfinityScrollView) findViewById(R.id.items_scroll);
        scroll.setOnScrollViewListener(this);
    }

    private void loadPage()
    {
        if(pageLoader != null)
           pageLoader.cancel(true);

        pageLoader = new PageLoader(_page, _stateId);
        pageLoader.execute();
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
        if (bottom < 50 * getResources().getDisplayMetrics().density && pageLoader == null) {
            loadPage();
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

    class PageLoader extends AsyncTask<Void, Void, InventoryItem[]>
    {
        int page;
        Integer statusId;

        public PageLoader(int page, Integer statusId)
        {
            this.page = page;
            this.statusId = statusId;
        }

        @Override
        protected void onPreExecute()
        {
            findViewById(R.id.activity_items_loading).setVisibility(View.VISIBLE);
        }

        @Override
        protected InventoryItem[] doInBackground(Void... voids)
        {
            try
            {
                InventoryItemCollection result = Zup.getInstance().getService().searchInventoryItems(_categoryId, (statusId != null ? new Integer[]{statusId} : null), null, page, 30);
                return result.items;
            }
            catch (RetrofitError error)
            {
                Log.e("Retrofit", "Could not get item listing", error);
                return null;
            }
        }

        @Override
        protected void onPostExecute(InventoryItem[] items)
        {
            if(items != null)
            {
                _page = this.page + 1; // Next page that will be loaded
                fillCategoryItems(items);
            }
            else
            {
                Toast.makeText(TransferItemsActivity.this, "Não foi possível obter a listagem de itens.", Toast.LENGTH_LONG).show();
            }
            findViewById(R.id.activity_items_loading).setVisibility(View.GONE);

            pageLoader = null;
        }
    }

    public class Tasker extends AsyncTask<Void, Integer, Void>
    {
        private int total;
        private ArrayList<Integer[]> itemsToDownload;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            itemsToDownload = new ArrayList<Integer[]>();

            final View overlay = findViewById(R.id.activity_transfer_items_overlay);
            final View popup = findViewById(R.id.activity_transfer_items_popup);
            final AlphaAnimation animation = new AlphaAnimation(0, .5f);
            animation.setDuration(300);
            animation.setFillAfter(true);
            final AlphaAnimation animation2 = new AlphaAnimation(0, 1f);
            animation.setDuration(300);
            animation.setFillAfter(true);

            overlay.setVisibility(View.VISIBLE);
            overlay.startAnimation(animation);
            popup.setVisibility(View.VISIBLE);
            popup.startAnimation(animation2);

            int temp_count = 0;
            ViewGroup container = (ViewGroup)findViewById(R.id.inventory_items_container);

            for(int i = 0; i < container.getChildCount(); i++)
            {
                ViewGroup rootView = (ViewGroup)container.getChildAt(i);
                CheckBox checkBox = (CheckBox)rootView.findViewById(R.id.fragment_inventory_item_check);

                int itemId = (Integer)rootView.getTag(R.id.tag_item_id);
                int categoryId = (Integer)rootView.getTag(R.id.tag_category_id);

                if(checkBox.isChecked()) {
                    temp_count++;
                    itemsToDownload.add(new Integer[] { categoryId, itemId });
                }
            }

            this.total = temp_count;
        }

        @Override
        protected Void doInBackground(Void... voids)
        {
            try
            {
                int temp_count = 0;
                publishProgress(temp_count);

                for (Integer[] data : itemsToDownload) {
                    int categoryId = data[0];
                    int itemId = data[1];

                    transferItem(itemId, categoryId);
                    temp_count++;

                    publishProgress(temp_count);
                }

                finish();
            }
            catch (RetrofitError error)
            {
                Log.e("Retrofit", "Could not transfer item", error);
                publishProgress(-1);
            }

            return null;
        }

        void transferItem(int itemId, int categoryId)
        {
            SingleInventoryItemCollection result = Zup.getInstance().getService().getInventoryItem(categoryId, itemId);
            if(result.item != null)
                Zup.getInstance().addInventoryItem(result.item);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            int downloaded = values[0];

            if(downloaded == -1) // error
            {
                View overlay = findViewById(R.id.activity_transfer_items_overlay);
                View popup = findViewById(R.id.activity_transfer_items_popup);

                overlay.setVisibility(View.GONE);
                popup.setVisibility(View.GONE);

                Toast.makeText(TransferItemsActivity.this, "Não foi possível transferir os itens. Verifique se há conexão com a internet.", Toast.LENGTH_LONG).show();
            }
            else
            {
                TextView progress = (TextView) findViewById(R.id.activity_transfer_items_popup_progress);
                progress.setText(downloaded + " itens transferidos de " + total);
            }
        }
    }
}
