package com.ntxdev.zuptecnico;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.api.callbacks.InventoryItemPublishedListener;
import com.ntxdev.zuptecnico.api.callbacks.InventoryItemsListener;
import com.ntxdev.zuptecnico.api.callbacks.JobFailedListener;
import com.ntxdev.zuptecnico.api.callbacks.ResourceLoadedListener;
import com.ntxdev.zuptecnico.entities.InventoryCategory;
import com.ntxdev.zuptecnico.entities.InventoryCategoryStatus;
import com.ntxdev.zuptecnico.entities.InventoryItem;
import com.ntxdev.zuptecnico.entities.InventoryItemImage;
import com.ntxdev.zuptecnico.entities.MapCluster;
import com.ntxdev.zuptecnico.ui.InfinityScrollView;
import com.ntxdev.zuptecnico.ui.SingularTabHost;
import com.ntxdev.zuptecnico.ui.UIHelper;
import com.ntxdev.zuptecnico.util.BitmapUtil;
import com.ntxdev.zuptecnico.util.ResizeAnimation;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Iterator;

public class ItemsActivity extends ActionBarActivity implements ResourceLoadedListener, InventoryItemPublishedListener, SearchBarListener, SingularTabHost.OnTabChangeListener, InventoryItemsListener, InfinityScrollView.OnScrollViewListener, GoogleMap.OnCameraChangeListener, JobFailedListener {
    private static final int REQUEST_SEARCH = 1;
    private static final int REQUEST_CREATE_ITEM = 2;

    private Menu _menu;
    private SupportMapFragment _mapFragment;
    private GoogleMap _map;
    private boolean _viewingMap;
    private int googlePlayStatus;
    private Hashtable<Marker, InventoryItem> itemMarkers;
    private Hashtable<Marker, MapCluster> clusterMarkers;
    private LocationClient locationClient;

    private int _categoryId;
    private int _page;
    private int _offlinePage;
    private boolean _isOffline;
    private int _pageJobId;
    private int _mapJobId;
    private Integer _stateId = null;

    private boolean _searchVisible = false;
    private Thread searchTimer;

    //private Intent _advancedSearchQuery = null;
    private String _searchQuery = "";
    private String _sort = "id";
    private String _order = "ASC";

    private InventoryItem expectedPublishedItem;
    private OfflinePageLoader offlinePageLoader;

    private AlertDialog _loadingCategoriesDialog;

    android.support.v7.widget.PopupMenu menu;

    Thread updateCategories = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                while (true) {
                    android.os.Handler handler = new android.os.Handler(ItemsActivity.this.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            updateCategoriesMenu();
                        }
                    });

                    Thread.sleep(10000);
                }
            } catch (InterruptedException ex) { }
        }
    });

    void updateCategoriesMenu()
    {
        if(_loadingCategoriesDialog != null) {
            _loadingCategoriesDialog.dismiss();
            _loadingCategoriesDialog = null;
        }

        menu.getMenu().clear();

        int i = 0;
        Iterator<InventoryCategory> categories = Zup.getInstance().getInventoryCategories();
        while (categories.hasNext()) {
            InventoryCategory category = categories.next();
            menu.getMenu().add(Menu.NONE, category.id, i, category.title);

            if (i == 0 && _categoryId == 0) {
                _categoryId = category.id;
                _page = 1;
                this._offlinePage = 1;
                loadPage();
                UIHelper.setTitle(this, category.title);

                new AsyncTask<Void, Void, Void>()
                {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);

                        refreshTabHost();
                    }
                }.execute();
            }
            i++;
        }

        if(i == 0)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Carregando Categorias de Inventário...");
            builder.setCancelable(false);

            ProgressBar progress = new ProgressBar(this);
            progress.setIndeterminate(true);

            builder.setView(progress);

            _loadingCategoriesDialog = builder.show();
        }
    }

    class ItemInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        private View mWindow;
        private View mContents;

        public ItemInfoWindowAdapter() {
            //mContents = getLayoutInflater().inflate(R.layout.inventory_items_map_info, null);
            mWindow = getLayoutInflater().inflate(R.layout.inventory_items_map_window, null);
        }

        class ImageLoader extends AsyncTask<Object, Void, Object[]>
        {
            @Override
            protected Object[] doInBackground(Object... args)
            {
                String url = (String) args[0];
                Marker marker = (Marker) args[1];

                int resourceId = Zup.getInstance().requestImage(url, false);
                if(!Zup.getInstance().isResourceLoaded(resourceId)) // Error
                    return new Object[] { marker, null };
                else
                {
                    return new Object[] { marker, Zup.getInstance().getResourceBitmap(resourceId) };
                }
            }

            @Override
            protected void onPostExecute(Object[] result)
            {
                Marker marker = (Marker) result[0];
                Bitmap bitmap = (Bitmap) result[1];

                if(bitmap != null) {
                    marker.hideInfoWindow();
                    marker.showInfoWindow();
                }
            }
        }

        void fillInfo(View view, final Marker marker)
        {
            final ImageView imageView = (ImageView) view.findViewById(R.id.inventory_item_map_info_image);

            TextView titleView = (TextView) view.findViewById(R.id.inventory_item_map_info_title);
            titleView.setText(marker.getTitle());

            InventoryItem item = itemMarkers.get(marker);
            InventoryItemImage firstImage = Zup.getInstance().getInventoryItemFirstImage(item);
            if(firstImage != null)
            {
                if(firstImage.content != null && !firstImage.content.isEmpty())
                {
                    byte[] data = Base64.decode(firstImage.content, Base64.NO_WRAP);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

                    imageView.setImageBitmap(bitmap);
                    imageView.setVisibility(View.VISIBLE);
                }
                else if(Zup.getInstance().isResourceLoaded(firstImage.versions.thumb))
                {
                    imageView.setImageBitmap(Zup.getInstance().getBitmap(firstImage.versions.thumb));
                    imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                    imageView.setVisibility(View.VISIBLE);
                }
                else
                {
                    imageView.setImageResource(R.drawable.documento_detalhes_status_icon_sync);
                    imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                    imageView.setVisibility(View.VISIBLE);

                    ImageLoader loader = new ImageLoader();
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
                        loader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, firstImage.versions.thumb, marker);
                    } else {
                        loader.execute(firstImage.versions.thumb, marker);
                    }
                }
            }
            else
            {
                imageView.setVisibility(View.GONE);
            }


            TextView state = (TextView)view.findViewById(R.id.inventory_item_map_info_status);
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
        }

        @Override
        public View getInfoWindow(Marker marker) {
            if(mWindow != null)
                fillInfo(mWindow, marker);
            return mWindow;
        }

        @Override
        public View getInfoContents(final Marker marker) {
            if(mContents != null)
                fillInfo(mContents, marker);
            return mContents;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.itemMarkers = new Hashtable<Marker, InventoryItem>();
        this.clusterMarkers = new Hashtable<Marker, MapCluster>();
        setContentView(R.layout.activity_items);

        Zup.getInstance().initStorage(getApplicationContext());

        googlePlayStatus = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        MapsInitializer.initialize(this);
        _mapFragment = new SupportMapFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.items_mapcontainer, _mapFragment).commit();

        if (savedInstanceState == null) {

        }

        if (Build.VERSION.SDK_INT >= 11) {
        }

        UIHelper.initActivity(this, true);
        this.menu = UIHelper.initMenu(this);

        /*int i = 0;
        Iterator<InventoryCategory> categories = Zup.getInstance().getInventoryCategories();
        while (categories.hasNext()) {
            InventoryCategory category = categories.next();
            menu.getMenu().add(Menu.NONE, category.id, i, category.title);

            if (i == 0) {
                _categoryId = category.id;
                _page = 1;
                this._offlinePage = 1;
                loadPage();
                UIHelper.setTitle(this, category.title);
            }
            i++;
        }*/
        updateCategoriesMenu();


        final ActionBarActivity activity = this;
        menu.setOnMenuItemClickListener(new android.support.v7.widget.PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                _categoryId = menuItem.getItemId();
                if (_viewingMap) {
                    setUpMapIfNeeded();
                    addLocalItemsToMap(null);
                    onCameraChange(_map.getCameraPosition());
                }

                refreshTabHost();

                clear();
                _sort = "id";
                _order = "ASC";
                _page = 1;
                _offlinePage = 1;
                _pageJobId = 0;
                loadPage();
                UIHelper.setTitle(activity, menuItem.getTitle().toString());
                return true;
            }
        });

        ViewGroup actionBar = (ViewGroup)this.getSupportActionBar().getCustomView();

        SingularTabHost tabHost = (SingularTabHost) findViewById(R.id.tabhost_documents);
        tabHost.setOnTabChangeListener(this);

        //refreshTabHost();
        /*tabHost.addTab("all", "Todos estados");
        tabHost.addTab("ok", "Saudável / OK");
        tabHost.addTab("analysis", "Em análise");
        tabHost.addTab("risk", "Em risco")*/

        InfinityScrollView scroll = (InfinityScrollView) findViewById(R.id.items_scroll);
        scroll.setOnScrollViewListener(this);

        Zup.getInstance().setInventoryItemPublishedListener(this);

        this.updateCategories.start();
        showBigLoading();

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

        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //refreshTabHost();
            }
        }, new IntentFilter(Zup.ACTION_STATUSES_RECEIVED));
    }

    @Override
    protected void onStop() {
        super.onStop();
        clear();
        _page = 1;
        _offlinePage = 1;
    }

    @Override
    public void onJobFailed(int job_id) {
        if(job_id == _pageJobId)
            _pageJobId = 0;

        hideLoading();
        hideBigLoading();
        showNoConnectionBar();
        //Toast.makeText(this, "Não foi possível obter a listagem de itens.", 3).show();

        // We were already offline. Load new local items.
        if(_isOffline)
        {
            loadOfflinePage();
        }
        else
        {
            _isOffline = true;
            _page = 1;
            _offlinePage = 1;
            clear();
            loadOfflinePage();
        }
    }

    void loadOfflinePage()
    {
        if(offlinePageLoader != null)
            offlinePageLoader.cancel(true);

        offlinePageLoader = new OfflinePageLoader();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
            offlinePageLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            offlinePageLoader.execute();
        }
    }

    @Override
    public void onInventoryItemPublished(int itemId, InventoryItem item) {
        if(expectedPublishedItem != null)
        {
            Intent intent = new Intent(ItemsActivity.this, InventoryItemDetailsActivity.class);
            intent.putExtra("item_id", itemId);
            intent.putExtra("category_id", item.inventory_category_id);
            ItemsActivity.this.startActivityForResult(intent, 0);
            ItemsActivity.this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

            expectedPublishedItem = null;
        }
    }

    void showTabHost()
    {
        SingularTabHost tabHost = (SingularTabHost) findViewById(R.id.tabhost_documents);
        tabHost.setVisibility(View.VISIBLE);
    }

    void hideTabHost()
    {
        SingularTabHost tabHost = (SingularTabHost) findViewById(R.id.tabhost_documents);
        tabHost.setVisibility(View.GONE);
    }

    void refreshTabHost()
    {
        _stateId = null;

        SingularTabHost tabHost = (SingularTabHost) findViewById(R.id.tabhost_documents);

        tabHost.removeAllTabs();

        Iterator<InventoryCategoryStatus> statusIterator = Zup.getInstance().getInventoryCategoryStatusIterator(_categoryId);
        if(statusIterator.hasNext() && !_viewingMap)
        {
            tabHost.setVisibility(View.VISIBLE);
        }
        else
        {
            tabHost.setVisibility(View.GONE);
        }

        tabHost.addTab(null, "Todos os estados");

        int i = 0;
        while(statusIterator.hasNext())
        {
            InventoryCategoryStatus status = statusIterator.next();
            //if(i == 0)
            //    _stateId = status.id;


            tabHost.addTab(Integer.toString(status.id), status.title);

            i++;
        }

        //tabHost.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSearchTextChanged(String text) {
        _searchQuery = text;
        startSearchTimer();
    }

    private void startSearchTimer()
    {
        if(searchTimer != null)
            searchTimer.interrupt();

        final String oldSearchQuery = _searchQuery;
        searchTimer = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                    if(_searchQuery.equals(oldSearchQuery))
                    {
                        Zup.runOnMainThread(new Runnable() {
                            @Override
                            public void run() {
                                search();
                            }
                        });
                    }
                    searchTimer = null;
                } catch(InterruptedException ex) { }
            }
        });
        searchTimer.start();
    }

    private void search()
    {
        this._offlinePage = 1;
        this._page = 1;
        this.clear();
        this.loadPage();
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

    void showNoConnectionBar()
    {
        View view = findViewById(R.id.bar_no_connection);
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if(params.height == 35)
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

    private void loadPage() {
        // Don't load if there's no category selected
        if(_categoryId == 0)
            return;

        hideNoItems();

        if(_searchQuery.equals(""))
            if(_stateId == null)
                _pageJobId = Zup.getInstance().requestInventoryItems(_categoryId, _page, _sort, _order, this, this);
            else
                _pageJobId = Zup.getInstance().searchInventoryItems(_page, 30, new int[] { _categoryId }, new Integer[] { _stateId }, null, null, null, null, null, null, null, null, this, this);
        else
            _pageJobId = Zup.getInstance().searchInventoryItems(_page, 30, new int[] { _categoryId }, ( _stateId != null ? new Integer[] { _stateId } : null), _searchQuery, this, this);
            //_pageJobId = Zup.getInstance().searchInventoryItems(_page, 30, new int[] { _categoryId }, (_stateId != null ? new Integer[] { _stateId } : null), null, _searchQuery, null, null, null, null, null, null, this, this);

        if(_page == 1)
            showBigLoading();
        else
            showLoading();
    }

    public void onInventoryItemsReceived(InventoryItem[] items, int categoryId, int page, int job_id) {
        if(_isOffline)
        {
            _isOffline = false;
            clear();
        }

        if (job_id == _pageJobId && _page == page && _categoryId == categoryId) {
            hideNoConnectionBar();
            if(items != null && items.length > 0)
                _page++; // Next page that will be loaded
            _pageJobId = 0;
            fillCategoryItems(items);
            hideLoading();
            hideBigLoading();
        }
    }

    public void onInventoryItemsReceived(InventoryItem[] items, int page, int per_page, int[] inventory_category_ids, String address, String title, Calendar creation_from, Calendar creation_to, Calendar modification_from, Calendar modification_to, Float latitude, Float longitude, int job_id)
    {
        if(_isOffline)
        {
            _isOffline = false;
            clear();
        }

        if (job_id == _pageJobId) {
            hideNoConnectionBar();
            if(items != null && items.length > 0)
                _page++; // Next page that will be loaded
            _pageJobId = 0;
            fillCategoryItems(items);
            hideLoading();
            hideBigLoading();
        }
    }

    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();

        Zup.getInstance().setResourceLoadedListener(this);

        clear();
        _page = 1;
        this._offlinePage = 1;
        _pageJobId = 0;
        loadPage();
    }

    void showMapLoading() {
        findViewById(R.id.items_map_progress).setVisibility(View.VISIBLE);
    }

    void hideMapLoading() {
        findViewById(R.id.items_map_progress).setVisibility(View.GONE);
    }

    private void setUpMapIfNeeded() {
        if (_map == null) {
            _map = _mapFragment.getMap();
            if (_map != null) {
                setUpMap();
            }
        }
    }

    private View setUpItemView(InventoryItem item)
    {
        ViewGroup rootView = (ViewGroup)getLayoutInflater().inflate(R.layout.fragment_inventory_item, null);
        rootView.setTag(R.id.tag_item_id, item.id);
        rootView.setTag(R.id.tag_category_id, item.inventory_category_id);
        //rootView.setClickable(true);
        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ItemsActivity.this, InventoryItemDetailsActivity.class);
                intent.putExtra("item_id", (Integer)view.getTag(R.id.tag_item_id));
                intent.putExtra("category_id", (Integer)view.getTag(R.id.tag_category_id));
                ItemsActivity.this.startActivityForResult(intent, 0);
                ItemsActivity.this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        TextView title = (TextView)rootView.findViewById(R.id.fragment_inventory_item_title);
        TextView description = (TextView)rootView.findViewById(R.id.fragment_inventory_item_desc);
        ImageView downloadIcon = (ImageView)rootView.findViewById(R.id.fragment_inventory_item_download_icon);
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
        downloadIcon.setVisibility(Zup.getInstance().hasInventoryItem(item.id) ? View.VISIBLE : View.GONE);

        return rootView;
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition)
    {
        VisibleRegion region = _map.getProjection().getVisibleRegion();
        double distance  = getDistance(region.latLngBounds.northeast, region.latLngBounds.southwest);
        double latitude = cameraPosition.target.latitude;
        double longitude = cameraPosition.target.longitude;
        double zoom = cameraPosition.zoom;

        showMapLoading();
        _mapJobId = Zup.getInstance().requestInventoryItems(_categoryId, latitude, longitude, distance / 2, zoom, this, this);
    }

    double rad(double x) {
        return x * Math.PI / (double)180;
    };

    double getDistance(LatLng p1, LatLng p2) {
        double R = 6378137; // Earth’s mean radius in meter
        double dLat = rad(p2.latitude - p1.latitude);
        double dLong = rad(p2.longitude - p1.longitude);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(rad(p1.latitude)) * Math.cos(rad(p2.longitude)) *
                        Math.sin(dLong / 2) * Math.sin(dLong / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = R * c;
        return d; // returns the distance in meter
    }

    public void onInventoryItemsReceived(InventoryItem[] items, MapCluster[] clusters, double latitude, double longitude, double radius, double zoom, int job_id)
    {
        //itemMarkers.clear();
        //_map.clear();

        if(_isOffline)
        {
            _isOffline = false;
            clear();
        }

        hideNoConnectionBar();

        ArrayList<Marker> markersToRemove = new ArrayList<Marker>();
        markersToRemove.addAll(this.itemMarkers.keySet());

        if(items.length <= 50) {

            for (InventoryItem item : items) {
                if (item.position == null)
                    continue;

                Marker itemMarker = null;
                Marker[] markers = itemMarkers.keySet().toArray(new Marker[0]);
                int index = 0;
                for (Object itemObj : itemMarkers.values().toArray()) {
                    InventoryItem _item = (InventoryItem) itemObj;
                    if (_item.id == item.id) {
                        itemMarker = markers[index];
                        break;
                    }

                    index++;
                }

                if (itemMarker == null) {
                    Bitmap markerBitmap = Zup.getInstance().getInventoryCategoryPinBitmap(item.inventory_category_id);

                    BitmapDescriptor markerIcon = BitmapDescriptorFactory.defaultMarker();
                    if (markerBitmap != null) {
                        markerIcon = BitmapDescriptorFactory.fromBitmap(markerBitmap);
                    }

                    LatLng pos = new LatLng(item.position.latitude, item.position.longitude);

                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(pos);
                    markerOptions.flat(true);
                    markerOptions.title(Zup.getInstance().getInventoryItemTitle(item));
                    markerOptions.icon(markerIcon);

                    itemMarkers.put(_map.addMarker(markerOptions), item);
                } else {
                    if (itemMarker.getPosition().longitude != item.position.longitude || itemMarker.getPosition().latitude != item.position.latitude) {
                        itemMarker.setPosition(new LatLng(item.position.latitude, item.position.longitude));
                    }

                    markersToRemove.remove(itemMarker);
                }
            }

            this.findViewById(R.id.items_map_toomany).setVisibility(View.GONE);
        }
        else
        {
            this.findViewById(R.id.items_map_toomany).setVisibility(View.VISIBLE);
        }

        for(Marker marker : clusterMarkers.keySet())
        {
            marker.remove();
        }
        this.clusterMarkers.clear();

        if(clusters != null)
        {
            for(MapCluster cluster : clusters)
            {
                Bitmap markerBitmap = BitmapUtil.getMapClusterBitmap(cluster, getResources().getDisplayMetrics());
                BitmapDescriptor markerIcon = BitmapDescriptorFactory.fromBitmap(markerBitmap);

                LatLng pos = new LatLng(cluster.position[0], cluster.position[1]);

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(pos);
                markerOptions.flat(true);
                markerOptions.title("(cluster)");
                markerOptions.icon(markerIcon);

                clusterMarkers.put(_map.addMarker(markerOptions), cluster);
            }
        }

        for(Marker marker : markersToRemove)
        {
            marker.remove();
            this.itemMarkers.remove(marker);
        }

        hideMapLoading();
    }

    private void setUpMap() {
        if (googlePlayStatus == ConnectionResult.SUCCESS) {
            itemMarkers.clear();
            _map.clear();
            _map.setInfoWindowAdapter(new ItemInfoWindowAdapter());
            _map.setOnCameraChangeListener(this);

            if (locationClient == null) {
                locationClient = new LocationClient(this, new GooglePlayServicesClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        locationConnected();
                    }

                    @Override
                    public void onDisconnected() {

                    }
                }, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {

                    }
                });

                locationClient.connect();
            }

            //int count = 0;
            LatLngBounds.Builder builder = LatLngBounds.builder();
            this.addLocalItemsToMap(builder);

            _map.setMyLocationEnabled(true);

            LatLng pos = new LatLng(-23.548338, -46.634845);
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(pos, 10);
            _map.moveCamera(update);

            _map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    if(marker.getTitle().equals("(cluster)"))
                        return true;

                    marker.showInfoWindow();
                    return true;
                }
            });

            final Activity activity = this;
            _map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    InventoryItem item = itemMarkers.get(marker);

                    Intent intent = new Intent(activity, InventoryItemDetailsActivity.class);
                    intent.putExtra("item_id", item.id);
                    intent.putExtra("category_id", item.inventory_category_id);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            });
        }
    }

    private void addLocalItemsToMap(LatLngBounds.Builder builder)
    {
        _map.clear();

        Iterator<InventoryItem> items = Zup.getInstance().getInventoryItemsByCategory(_categoryId);
        while (items.hasNext()) {
            InventoryItem item = items.next();
            if (item.position == null)
                continue;

            InventoryCategory category = Zup.getInstance().getInventoryCategory(item.inventory_category_id);
            BitmapDescriptor markerIcon = BitmapDescriptorFactory.defaultMarker();
            int resourceId = Zup.getInstance().getInventoryCategoryPinResourceId(category.id);
            if (resourceId != 0 && Zup.getInstance().isResourceLoaded(resourceId)) {
                markerIcon = BitmapDescriptorFactory.fromBitmap(Zup.getInstance().getBitmap(resourceId));
            }

            LatLng pos = new LatLng(item.position.latitude, item.position.longitude);
            if(builder != null)
                builder.include(pos);

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(pos);
            markerOptions.flat(true);
            markerOptions.title(Zup.getInstance().getInventoryItemTitle(item));
            markerOptions.icon(markerIcon);

            itemMarkers.put(_map.addMarker(markerOptions), item);
        }
    }

    @Override
    public void onResourceLoaded(String url, int resourceId) {
        for(Marker marker : this.itemMarkers.keySet())
        {
            InventoryItem item = this.itemMarkers.get(marker);
            if(item == null)
                return;

            InventoryCategory category = Zup.getInstance().getInventoryCategory(item.inventory_category_id);
            if(category == null || category.pin == null || category.pin._default == null || category.pin._default.mobile == null)
                return;

            if(category.pin._default.mobile.equals(url))
            {
                try {
                    marker.setIcon(BitmapDescriptorFactory.fromBitmap(Zup.getInstance().getBitmap(resourceId)));
                } catch (IllegalArgumentException ex) {
                    // http://stackoverflow.com/questions/21523202/released-unknown-bitmap-reference-setting-marker-in-android
                }
            }
        }
    }

    private void locationConnected() {
        locationClient.requestLocationUpdates(LocationRequest.create(), new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Location myLocation = location;
                if (myLocation != null) {
                    _map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), 15));
                }
            }
        });
    }

    private void clear()
    {
        //FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        //for(InventoryItemFragment fragment : _itemsFragments)
        //{
            //transaction.hide(fragment);
        //    transaction.remove(fragment);
        //}
        //transaction.commit();

        ((ViewGroup)findViewById(R.id.inventory_items_container)).removeAllViews();
    }

    private void fillCategoryItems(InventoryItem[] items)
    {
        ViewGroup root = (ViewGroup)findViewById(R.id.inventory_items_container);
        int i = 0;
        for(InventoryItem item : items)
        {
            View view = setUpItemView(item);
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

        if(_page == 1 && items.length == 0)
        {
            showNoItems();
        }
        else
        {
            hideNoItems();
        }
    }

    void showNoItems()
    {
        findViewById(R.id.activity_items_noitems).setVisibility(View.VISIBLE);
    }

    void hideNoItems()
    {
        findViewById(R.id.activity_items_noitems).setVisibility(View.GONE);
    }

    public void onTabChange(SingularTabHost tabHost, String oldIdentifier, String newIdentifier)
    {
        if(newIdentifier != null)
            this._stateId = Integer.parseInt(newIdentifier);
        else
            this._stateId = null;

        _page = 1;
        _offlinePage = 1;
        this.clear();
        loadPage();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items_list to the action bar if it is present.
        getMenuInflater().inflate(R.menu.items_list, menu);
        _menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        } else if(id == R.id.action_search) {
            _searchVisible = true;
            UIHelper.showSearchBar(this, this, _menu);
        } else if(id == R.id.action_items_map) {
            _menu.findItem(R.id.action_items_map).setVisible(false);
            _menu.findItem(R.id.action_items_list).setVisible(true);
            _menu.findItem(R.id.action_order_creation).setVisible(false);
            _menu.findItem(R.id.action_order_modification).setVisible(false);
            _menu.findItem(R.id.action_order_name).setVisible(false);
            _menu.findItem(R.id.action_search).setVisible(false);

            findViewById(R.id.items_scroll).setVisibility(View.GONE);
            findViewById(R.id.items_mapcontainer_container).setVisibility(View.VISIBLE);

            _viewingMap = true;
            refreshTabHost();

            return true;
        } else if(id == R.id.action_items_list) {
            _menu.findItem(R.id.action_items_map).setVisible(true);
            _menu.findItem(R.id.action_items_list).setVisible(false);
            _menu.findItem(R.id.action_order_creation).setVisible(true);
            _menu.findItem(R.id.action_order_modification).setVisible(true);
            _menu.findItem(R.id.action_order_name).setVisible(true);
            _menu.findItem(R.id.action_search).setVisible(true);

            findViewById(R.id.items_scroll).setVisibility(View.VISIBLE);
            findViewById(R.id.items_mapcontainer_container).setVisibility(View.GONE);

            _viewingMap = false;
            refreshTabHost();

            return true;
        } else if(id == R.id.action_items_add) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Novo item de inventário");

            final ArrayList<InventoryCategory> categories = new ArrayList<InventoryCategory>();
            Iterator<InventoryCategory> categoryIterator = Zup.getInstance().getInventoryCategories();
            while(categoryIterator.hasNext())
            {
                InventoryCategory category = categoryIterator.next();
                categories.add(category);
            }

            String[] items = new String[categories.size()];
            for(int i = 0; i < categories.size(); i++)
            {
                items[i] = categories.get(i).title;
            }

            final Activity activity = this;
            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    InventoryCategory category = categories.get(i);
                    dialogInterface.dismiss();

                    Intent intent = new Intent(activity, CreateInventoryItemActivity.class);
                    intent.putExtra("create", true);
                    intent.putExtra("category_id", category.id);
                    startActivityForResult(intent, REQUEST_CREATE_ITEM);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            });

            builder.show();
        } else if(id == R.id.action_items_viewdownloaded) {
            Intent intent = new Intent(this, DownloadedItemsActivity.class);
            this.startActivity(intent);
        } else if(id == R.id.action_items_transfer) {
            Intent intent = new Intent(this, TransferItemsActivity.class);
            intent.putExtra("category_id", _categoryId);
            intent.putExtra("state_id", _stateId);
            this.startActivityForResult(intent, 0);
        } else if(id == R.id.action_order_name) {
            if(this._sort.equals("id"))
            {
                if(this._order.equals("ASC"))
                    this._order = "DESC";
                else
                    this._order = "ASC";
            }
            else
                this._order = "ASC";

            this._sort = "id";
            this._page = 1;
            this._offlinePage = 1;
            this.clear();
            this.loadPage();
        } else if(id == R.id.action_order_creation) {
            if(this._sort.equals("created_at"))
            {
                if(this._order.equals("ASC"))
                    this._order = "DESC";
                else
                    this._order = "ASC";
            }
            else
                this._order = "ASC";

            this._sort = "created_at";
            this._page = 1;
            this._offlinePage = 1;
            this.clear();
            this.loadPage();
        } else if(id == R.id.action_order_modification) {
            if(this._sort.equals("updated_at"))
            {
                if(this._order.equals("ASC"))
                    this._order = "DESC";
                else
                    this._order = "ASC";
            }
            else
                this._order = "ASC";

            this._sort = "updated_at";
            this._page = 1;
            this._offlinePage = 1;
            this.clear();
            this.loadPage();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_SEARCH)
        {
            if(resultCode == AdvancedSearchActivity.RESULT_SEARCH)
            {
                Intent intent = new Intent(this, InventoryItemsAdvancedSearchResultActivity.class);
                intent.putExtra("search_data", data);
                this.startActivity(intent);
            }
        }
        else if(requestCode == REQUEST_CREATE_ITEM && resultCode == CreateInventoryItemActivity.RESULT_ITEM_SAVED)
        {
            this.expectedPublishedItem = Zup.getInstance().getInventoryItem(data.getIntExtra("item_id", 0));
            this.expectedPublishedItem.publishToken = Zup.getInstance().generateJobId();

            new InventoryCategory.Section();
        }
        else {
            //if(resultCode == 2) // reset signal
            {
                if (_viewingMap) {
                    setUpMap();
                    onCameraChange(_map.getCameraPosition());
                }

                clear();
                _page = 1;
                this._offlinePage = 1;
                _pageJobId = 0;
                loadPage();
            }
            //this.fillCategoryItems();
        }
    }

    class OfflinePageLoader extends AsyncTask<Void, Void, InventoryItem[]>
    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            showLoading();
        }

        @Override
        protected InventoryItem[] doInBackground(Void... voids) {
            Iterator<InventoryItem> items = Zup.getInstance().getInventoryItemsIterator(_categoryId, _stateId, _searchQuery, _offlinePage);

            ArrayList<InventoryItem> result = new ArrayList<InventoryItem>();
            while(items.hasNext())
            {
                result.add(items.next());
            }

            InventoryItem[] itemsArray = result.toArray(new InventoryItem[0]);

            return itemsArray;
        }

        @Override
        protected void onPostExecute(InventoryItem[] inventoryItems) {
            super.onPostExecute(inventoryItems);

            hideLoading();
            hideBigLoading();
            fillCategoryItems(inventoryItems);

            if(inventoryItems.length > 0)
                _offlinePage++;
        }
    }
}
