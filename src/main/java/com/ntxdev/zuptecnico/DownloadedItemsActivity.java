package com.ntxdev.zuptecnico;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.entities.InventoryCategory;
import com.ntxdev.zuptecnico.entities.InventoryCategoryStatus;
import com.ntxdev.zuptecnico.entities.InventoryItem;
import com.ntxdev.zuptecnico.ui.InfinityScrollView;
import com.ntxdev.zuptecnico.ui.SingularTabHost;
import com.ntxdev.zuptecnico.ui.UIHelper;

import java.util.Iterator;

public class DownloadedItemsActivity extends ActionBarActivity implements View.OnClickListener {
    private Menu _menu;
    private int _categoryId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items);

        Zup.getInstance().initStorage(getApplicationContext());

        UIHelper.initActivity(this, false);
        android.support.v7.widget.PopupMenu menu = UIHelper.initMenu(this);

        int i = 0;
        Iterator<InventoryCategory> categories = Zup.getInstance().getInventoryCategories();
        while (categories.hasNext()) {
            InventoryCategory category = categories.next();
            menu.getMenu().add(Menu.NONE, category.id, i, category.title);

            if (i == 0) {
                _categoryId = category.id;
                fillItems();
                UIHelper.setTitle(this, category.title);
            }
            i++;
        }


        final ActionBarActivity activity = this;
        menu.setOnMenuItemClickListener(new android.support.v7.widget.PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                _categoryId = menuItem.getItemId();
                fillItems();
                UIHelper.setTitle(activity, menuItem.getTitle().toString());
                return true;
            }
        });

        SingularTabHost tabHost = (SingularTabHost) findViewById(R.id.tabhost_documents);
        //tabHost.setOnTabChangeListener(this);

        tabHost.addTab("all", "Todos estados");
        tabHost.addTab("ok", "Saudável / OK");
        tabHost.addTab("analysis", "Em análise");
        tabHost.addTab("risk", "Em risco");

        InfinityScrollView scroll = (InfinityScrollView) findViewById(R.id.items_scroll);
    }

    private void fillItems()
    {
        Iterator<InventoryItem> itemIterator = Zup.getInstance().getInventoryItemsByCategory(_categoryId);
        ViewGroup container = (ViewGroup)findViewById(R.id.inventory_items_container);
        container.removeAllViews();

        boolean hasAny = false;
        while(itemIterator.hasNext())
        {
            hasAny = true;

            InventoryItem item = itemIterator.next();
            View itemView = setUpItemView(item);

            container.addView(itemView);
        }

        findViewById(R.id.activity_items_noitems).setVisibility(hasAny ? View.GONE : View.VISIBLE);
    }

    private View setUpItemView(InventoryItem item)
    {
        ViewGroup rootView = (ViewGroup)getLayoutInflater().inflate(R.layout.fragment_inventory_item, null);
        rootView.setTag(R.id.tag_item_id, item.id);
        rootView.setTag(R.id.tag_category_id, item.inventory_category_id);
        rootView.setOnClickListener(this);

        TextView title = (TextView)rootView.findViewById(R.id.fragment_inventory_item_title);
        TextView description = (TextView)rootView.findViewById(R.id.fragment_inventory_item_desc);
        ImageView downloadIcon = (ImageView)rootView.findViewById(R.id.fragment_inventory_item_download_icon);

        if(item.syncError)
        {
            title.setTextColor(0xffff0000);
        }

        title.setText(Zup.getInstance().getInventoryItemTitle(item));
        description.setText("Incluído em " + Zup.getInstance().formatIsoDate(item.created_at));
        downloadIcon.setVisibility(View.GONE);

        TextView state = (TextView)rootView.findViewById(R.id.fragment_inventory_item_statedesc);
        if(item.inventory_status_id != null)
        {
            InventoryCategoryStatus status = Zup.getInstance().getInventoryCategoryStatus(item.inventory_category_id, item.inventory_status_id);
            if (status != null) {
                state.setText(status.title);
                state.setBackgroundColor(status.getColor());
                state.setVisibility(View.VISIBLE);
            }
            else {
                state.setVisibility(View.GONE);
            }
        }
        else
        {
            state.setVisibility(View.GONE);
        }

        return rootView;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        fillItems();
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

        Intent intent = new Intent(this, InventoryItemDetailsActivity.class);
        intent.putExtra("item_id", itemId);
        intent.putExtra("category_id", categoryId);
        this.startActivityForResult(intent, 0);
    }
}
