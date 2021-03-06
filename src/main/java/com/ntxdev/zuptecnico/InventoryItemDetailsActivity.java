package com.ntxdev.zuptecnico;

import android.support.v7.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ntxdev.zuptecnico.api.ApiHttpClient;
import com.ntxdev.zuptecnico.api.DeleteInventoryItemSyncAction;
import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.api.ZupCache;
import com.ntxdev.zuptecnico.entities.InventoryCategory;
import com.ntxdev.zuptecnico.entities.InventoryCategoryStatus;
import com.ntxdev.zuptecnico.entities.InventoryItem;
import com.ntxdev.zuptecnico.entities.InventoryItemImage;
import com.ntxdev.zuptecnico.entities.collections.SingleInventoryItemCollection;
import com.ntxdev.zuptecnico.ui.UIHelper;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import retrofit.RetrofitError;

/**
 * Created by igorlira on 3/3/14.
 */
public class InventoryItemDetailsActivity extends AppCompatActivity
{
    private InventoryItem item;
    private InventoryCategory category;
    private SupportMapFragment mapFragment;
    private GoogleMap map;
    private int googlePlayStatus;
    private int jobId;
    private Menu menu;
    private boolean isFakeCreate = false;

    private Tasker itemLoader;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_item_details);

        Zup.getInstance().initStorage(getApplicationContext());

        UIHelper.initActivity(this, false);

        googlePlayStatus = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        MapsInitializer.initialize(this);

        int itemId = getIntent().getIntExtra("item_id", 0);
        int categoryId = getIntent().getIntExtra("categoryId", 0);
        if(Zup.getInstance().hasInventoryItem(itemId))
            item = Zup.getInstance().getInventoryItem(itemId);
        else if(ZupCache.hasInventoryItem(itemId))
            item = ZupCache.getInventoryItem(itemId);
        else
            requestItemInfo(itemId, categoryId);

        this.isFakeCreate = getIntent().getBooleanExtra("fake_create", false);

        mapFragment = new SupportMapFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.items_mapcontainer, mapFragment).commit();

        if(item != null)
        {
            fillItemInfo();
        }
    }

    private void setUpMenu()
    {
        if(this.menu != null && this.item != null)
        {
            if(this.item.isLocal)
            {
                menu.findItem(R.id.action_items_delete_download).setVisible(false);
                menu.findItem(R.id.action_items_download).setVisible(false);
            }
            else if(Zup.getInstance().hasInventoryItem(this.item.id))
            {
                menu.findItem(R.id.action_items_delete_download).setVisible(true);
                menu.findItem(R.id.action_items_download).setVisible(false);
            }
            else
            {
                menu.findItem(R.id.action_items_delete_download).setVisible(false);
                menu.findItem(R.id.action_items_download).setVisible(true);
            }

            menu.findItem(R.id.action_items_edit).setVisible(Zup.getInstance().getAccess()
                    .canEditInventoryItem(item.inventory_category_id));

            menu.findItem(R.id.action_items_discard).setVisible(Zup.getInstance().getAccess()
                    .canDeleteInventoryItem(item.inventory_category_id));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.items_details, menu);
        if(this.menu == null && menu != null)
            this.menu = menu;

        setUpMenu();
        return true;
    }

    void edit()
    {
        if(!isFakeCreate && Zup.getInstance().hasSyncActionRelatedToInventoryItem(this.item.id))
        {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setTitle("Ops!");
            dialogBuilder.setMessage("Existe uma sincronização pendente para este item. É necessário concluí-la antes de modificá-lo.");
            dialogBuilder.setCancelable(true);
            dialogBuilder.setNegativeButton("Fechar", null);
            dialogBuilder.show();

            return;
        }

        Intent intent = new Intent(this, CreateInventoryItemActivity.class);
        intent.putExtra("create", false);
        intent.putExtra("categoryId", category.id);
        intent.putExtra("item_id", this.item.id);
        intent.putExtra("fake_create", this.isFakeCreate);
        startActivityForResult(intent, 0);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(this.item == null)
            return true;

        if(item.getItemId() == R.id.action_items_discard)
        {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setMessage("Tem certeza de que deseja excluir " + Zup.getInstance().getInventoryItemTitle(this.item) + "? Essa ação é irreversível.");
            dialogBuilder.setPositiveButton("Excluir", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    deleteItem();
                }
            });
            dialogBuilder.setNegativeButton("Cancelar", null);
            dialogBuilder.show();
        }
        else if(item.getItemId() == R.id.action_items_edit)
        {
            edit();
        }
        else if(item.getItemId() == R.id.action_items_download)
        {
            Zup.getInstance().addInventoryItem(this.item);
            this.menu.findItem(R.id.action_items_delete_download).setVisible(true);
            this.menu.findItem(R.id.action_items_download).setVisible(false);
        }
        else if(item.getItemId() == R.id.action_items_delete_download)
        {
            Zup.getInstance().removeInventoryItem(this.item.id);
            this.menu.findItem(R.id.action_items_delete_download).setVisible(false);
            this.menu.findItem(R.id.action_items_download).setVisible(true);
        }

        return super.onOptionsItemSelected(item);
    }

    private void deleteItem()
    {
        Zup.getInstance().addSyncAction(new DeleteInventoryItemSyncAction(item.inventory_category_id, item.id));
        Zup.getInstance().sync();
        finishWithResetSignal();
        //overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    void requestItemInfo(int itemId, int categoryId)
    {
        if(itemLoader != null)
            itemLoader.cancel(true);

        itemLoader = new Tasker(categoryId, itemId);
        itemLoader.execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == 1)
        {
            item = Zup.getInstance().getInventoryItem(item.id); // Refresh that data
            fillItemInfo();
            setUpMap();
        }
    }

    private void finishWithResetSignal()
    {
        this.setResult(2);
        finish();
    }

    void showLoading()
    {
        findViewById(R.id.inventory_item_details_loading).setVisibility(View.VISIBLE);
    }

    void hideLoading()
    {
        findViewById(R.id.inventory_item_details_loading).setVisibility(View.GONE);
    }

    void fillItemInfo()
    {
        setUpMenu();

        category = Zup.getInstance().getInventoryCategory(item.inventory_category_id);

        hideLoading();

        UIHelper.setTitle(this, Zup.getInstance().getInventoryItemTitle(item));

        TextView titleView = (TextView)findViewById(R.id.inventory_item_details_title);
        TextView createdView = (TextView)findViewById(R.id.inventory_item_details_added);
        TextView modifiedView = (TextView)findViewById(R.id.inventory_item_details_modified);
        TextView statusView = (TextView)findViewById(R.id.inventory_item_details_state_desc);
        final ImageView firstImageView = (ImageView)findViewById(R.id.inventory_item_details_state_icon);

        InventoryItemImage firstImage = Zup.getInstance().getInventoryItemFirstImage(item);
        firstImageView.setVisibility(View.GONE);

        if(firstImage != null)
        {
            if(firstImage.content != null)
            {
                byte[] data = Base64.decode(firstImage.content, Base64.NO_WRAP);
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

                firstImageView.setImageBitmap(bitmap);
            }
            else if(Zup.getInstance().isResourceLoaded(firstImage.versions.thumb))
            {
                firstImageView.setImageBitmap(Zup.getInstance().getBitmap(firstImage.versions.thumb));
                firstImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                firstImageView.setVisibility(View.VISIBLE);
            }
            else {
                firstImageView.setImageResource(R.drawable.documento_detalhes_status_icon_sync);
                firstImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                firstImageView.setVisibility(View.VISIBLE);

                ImageLoader loader = new ImageLoader();
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
                    loader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, firstImage.versions.thumb, firstImageView);
                } else {
                    loader.execute(firstImage.versions.thumb, firstImageView);
                }
            }

            //imageView.setVisibility(View.VISIBLE);
        }

        if(item.inventory_status_id != null)
        {
            InventoryCategoryStatus status = Zup.getInstance().getInventoryCategoryStatus(item.inventory_category_id, item.inventory_status_id);
            if(status != null)
            {
                statusView.setBackgroundColor(status.getColor());
                statusView.setText(status.title);

                statusView.setVisibility(View.VISIBLE);
            }
            else
            {
                statusView.setVisibility(View.GONE);
            }
        }
        else
        {
            statusView.setVisibility(View.GONE);
        }

        titleView.setText(Zup.getInstance().getInventoryItemTitle(item));
        createdView.setText("Incluído: " + Zup.getInstance().formatIsoDate(item.created_at));
        modifiedView.setText("Modificado:" + Zup.getInstance().formatIsoDate(item.updated_at));

        buildPage();
    }

    private void setUpMapIfNeeded()
    {
        if(map == null)
        {
            map = mapFragment.getMap();
            if(map != null)
            {
                setUpMap();
            }
        }
    }

    private void setUpMap()
    {
        if(googlePlayStatus == ConnectionResult.SUCCESS && item.position != null)
        {
            BitmapDescriptor markerIcon = BitmapDescriptorFactory.defaultMarker();
            int resourceId = Zup.getInstance().getInventoryCategoryPinResourceId(category.id);
            if(resourceId != 0 && Zup.getInstance().isResourceLoaded(resourceId))
            {
                markerIcon = BitmapDescriptorFactory.fromBitmap(Zup.getInstance().getBitmap(resourceId));
            }

            map.clear();
            map.getUiSettings().setAllGesturesEnabled(false);

            LatLng pos = new LatLng(item.position.latitude, item.position.longitude);
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(pos, 15);
            map.moveCamera(update);

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(pos);
            markerOptions.icon(markerIcon);
            map.addMarker(markerOptions);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        //if(item != null)
        //    setUpMapIfNeeded();
    }

    void buildPage()
    {
        ViewGroup container = (ViewGroup)findViewById(R.id.inventory_item_details_container);

        PageBuilder builder = new PageBuilder(container);
        builder.execute();
    }

    void sortSections()
    {
        Arrays.sort(category.sections, new Comparator<InventoryCategory.Section>() {
            @Override
            public int compare(InventoryCategory.Section section, InventoryCategory.Section section2) {
                int pos1 = 0;
                int pos2 = 0;

                if (section.position != null) {
                    pos1 = section.position;
                }
                if (section2.position != null) {
                    pos2 = section2.position;
                }

                if (section.position == null)
                    pos1 = pos2;

                if (section2.position == null)
                    pos2 = pos1;

                if (pos1 < pos2)
                    return -1;
                else if (pos1 == pos2)
                    return 0;
                else
                    return 1;
            }
        });
    }

    void sortSectionFields(InventoryCategory.Section section)
    {
        Arrays.sort(section.fields, new Comparator<InventoryCategory.Section.Field>() {
            @Override
            public int compare(InventoryCategory.Section.Field section, InventoryCategory.Section.Field section2) {
                int pos1 = 0;
                int pos2 = 0;

                if(section.position != null)
                {
                    pos1 = section.position;
                }
                if(section2.position != null)
                {
                    pos2 = section2.position;
                }

                if(section.position == null)
                    pos1 = pos2;

                if(section2.position == null)
                    pos2 = pos1;

                if(pos1 < pos2)
                    return -1;
                else if(pos1 == pos2)
                    return 0;
                else
                    return 1;
            }
        });
    }

    private View[] buildPageB()
    {
        ArrayList<View> result = new ArrayList<View>();

        ObjectMapper mapper = new ObjectMapper();

        if(category.sections != null) {
            sortSections();

            for (int i = 0; i < category.sections.length; i++) {
                InventoryCategory.Section section = category.sections[i];

                ViewGroup sectionHeader = (ViewGroup) getLayoutInflater().inflate(R.layout.inventory_item_section_header, null, false);

                TextView sectionTitle = (TextView) sectionHeader.findViewById(R.id.inventory_item_section_title);
                sectionTitle.setText(section.title.toUpperCase());

                result.add(sectionHeader);

                sortSectionFields(section);

                for (int j = 0; j < section.fields.length; j++) {
                    InventoryCategory.Section.Field field = section.fields[j];

                    if(field.kind != null && field.kind.equals("images") && this.item.getFieldValue(field.id) != null)
                    {
                        HorizontalScrollView scroll = new HorizontalScrollView(this);
                        scroll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                        LinearLayout fieldView = new LinearLayout(this);
                        fieldView.setPadding(20, 20, 20, 20);
                        fieldView.setOrientation(LinearLayout.HORIZONTAL);
                        fieldView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                        final ArrayList images = (ArrayList)this.item.getFieldValue(field.id);
                        for(int im = 0; im < images.size(); im++)
                        {
                            final InventoryItemImage image;
                            if(images.get(im) instanceof InventoryItemImage)
                                image = (InventoryItemImage)images.get(im);
                            else {
                                Object map = images.get(im);
                                image = mapper.convertValue(map, InventoryItemImage.class);
                            }

                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(150, 150);
                            layoutParams.setMargins(0, 0, 15, 0);

                            final ImageView imageView = new ImageView(this);
                            imageView.setLayoutParams(layoutParams);
                            imageView.setBackgroundColor(0xffcccccc);
                            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                            imageView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Parcelable[] imgs = new Parcelable[images.size()];
                                    for(int x = 0; x < images.size(); x++)
                                    {
                                        InventoryItemImage img;
                                        if(images.get(x) instanceof InventoryItemImage)
                                            img = (InventoryItemImage)images.get(x);
                                        else {
                                            Object map = images.get(x);
                                            ObjectMapper mapper = new ObjectMapper();
                                            img = mapper.convertValue(map, InventoryItemImage.class);
                                        }

                                        imgs[x] = img;
                                    }

                                    Intent intent = new Intent(InventoryItemDetailsActivity.this, FullScreenImageActivity.class);
                                    intent.putExtra("image", (Parcelable)image);
                                    intent.putExtra("images", imgs);
                                    startActivity(intent);
                                }
                            });

                            if(image.content != null)
                            {
                                byte[] data = Base64.decode(image.content, Base64.NO_WRAP);
                                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

                                imageView.setImageBitmap(bitmap);
                            }
                            else if(Zup.getInstance().isResourceLoaded(image.versions.thumb))
                            {
                                imageView.setImageBitmap(Zup.getInstance().getBitmap(image.versions.thumb));
                            }
                            else
                            {
                                ImageLoader loader = new ImageLoader();
                                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
                                    loader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, image.versions.thumb, imageView);
                                } else {
                                    loader.execute(image.versions.thumb, imageView);
                                }
                            }

                            fieldView.addView(imageView);
                        }
                        scroll.addView(fieldView);

                        result.add(scroll);
                    }
                    else if(field.kind.equals("checkbox") || field.kind.equals("radio") || field.kind.equals("select"))
                    {
                        ViewGroup fieldView = (ViewGroup) getLayoutInflater().inflate(R.layout.inventory_item_item_text, null, false);
                        TextView fieldTitle = (TextView) fieldView.findViewById(R.id.inventory_item_text_name);
                        TextView fieldValue = (TextView) fieldView.findViewById(R.id.inventory_item_text_value);

                        fieldTitle.setText(field.label != null ? field.label.toUpperCase() : field.title.toUpperCase());

                        Object valueObj = item.getFieldValue(field.id);
                        ArrayList value;
                        if(valueObj instanceof ArrayList)
                            value = (ArrayList)item.getFieldValue(field.id);
                        else
                        {
                            value = new ArrayList();
                            value.add(valueObj);
                        }

                        String strvalue = "";
                        if(value != null)
                        {
                            for(int x = 0; x < value.size(); x++)
                            {
                                Object valObj = value.get(x);
                                Integer val = mapper.convertValue(valObj, Integer.class);
                                if(val == null)
                                    continue;

                                InventoryCategory.Section.Field.Option option = field.getOption(val);
                                String val_str = "";

                                if(option != null)
                                    val_str = option.value;

                                if(x > 0)
                                    strvalue += "\n";

                                strvalue += val_str;
                            }

                            fieldValue.setText(strvalue);
                        }
                        else
                            fieldValue.setText("");

                        result.add(fieldView);
                    }
                    else
                    {
                        ViewGroup fieldView = (ViewGroup) getLayoutInflater().inflate(R.layout.inventory_item_item_text, null, false);
                        TextView fieldTitle = (TextView) fieldView.findViewById(R.id.inventory_item_text_name);
                        TextView fieldValue = (TextView) fieldView.findViewById(R.id.inventory_item_text_value);

                        fieldTitle.setText(field.label != null ? field.label.toUpperCase() : field.title.toUpperCase());
                        if (item.getFieldValue(field.id) != null)
                            fieldValue.setText(item.getFieldValue(field.id).toString());
                        else
                            fieldValue.setText("");

                        result.add(fieldView);
                    }
                }
            }
        }

        View[] resultarr = new View[result.size()];
        result.toArray(resultarr);

        return resultarr;
    }

    class Tasker extends AsyncTask<Void, Void, InventoryItem>
    {
        int catId;
        int itemId;

        public Tasker(int categoryId, int itemId)
        {
            this.catId = categoryId;
            this.itemId = itemId;
        }

        @Override
        protected void onPreExecute()
        {
            showLoading();
        }

        @Override
        protected InventoryItem doInBackground(Void... voids)
        {
            try
            {
                SingleInventoryItemCollection result = Zup.getInstance().getService().getInventoryItem(catId, itemId);
                if (result == null)
                    return null;

                return result.item;
            }
            catch (RetrofitError error)
            {
                Log.e("Retrofit", "Failed to load inventory item", error);
                return null;
            }
        }

        @Override
        protected void onPostExecute(InventoryItem inventoryItem)
        {
            if(inventoryItem == null)
            {
                Toast.makeText(InventoryItemDetailsActivity.this, "Não foi possível obter detalhes sobre o item.", Toast.LENGTH_LONG).show();
            }
            else
            {
                ZupCache.addInventoryItem(inventoryItem);
                InventoryItemDetailsActivity.this.item = inventoryItem;
                fillItemInfo();
            }
        }
    }

    class ImageLoader extends AsyncTask<Object, Void, Object[]>
    {
        @Override
        protected Object[] doInBackground(Object... objects) {
            String url;

            try
            {
                URL urlObj = new URL(new URL(ApiHttpClient.mBasePath), (String) objects[0]);
                url = urlObj.toString(); //(String) objects[0];
            }
            catch (MalformedURLException ex)
            {
                url = (String) objects[0];
            }
            ImageView imageView = (ImageView) objects[1];

            int resourceId = Zup.getInstance().requestImage(url, false);
            if(!Zup.getInstance().isResourceLoaded(resourceId))
            {
                return new Object[] { imageView, null };
            }
            else
            {
                return new Object[] { imageView, Zup.getInstance().getResourceBitmap(resourceId) };
            }
        }

        @Override
        protected void onPostExecute(Object[] objects) {
            ImageView imageView = (ImageView) objects[0];
            Bitmap bitmap = (Bitmap) objects[1];

            if(bitmap == null)
            {
                imageView.setVisibility(View.INVISIBLE);
            }
            else
            {
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    class PageBuilder extends AsyncTask<Void, Void, View[]>
    {
        ViewGroup container;

        public PageBuilder(ViewGroup container)
        {
            this.container = container;
        }

        @Override
        protected void onPreExecute() {
            container.removeAllViews();

            ProgressBar bar = new ProgressBar(container.getContext());
            bar.setIndeterminate(true);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER_HORIZONTAL;
            params.topMargin = 100;
            params.bottomMargin = 100;

            bar.setLayoutParams(params);
            container.addView(bar);
        }

        @Override
        protected View[] doInBackground(Void... voids) {
            return buildPageB();
        }

        @Override
        protected void onPostExecute(View[] views) {
            container.removeAllViews();
            for(View v : views)
            {
                container.addView(v);
            }

            setUpMapIfNeeded();
        }
    }
}
