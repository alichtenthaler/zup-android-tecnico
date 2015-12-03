package com.ntxdev.zuptecnico.adapters;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.ntxdev.zuptecnico.R;
import com.ntxdev.zuptecnico.activities.reports.ReportsListActivity;
import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.entities.ReportCategory;
import com.ntxdev.zuptecnico.entities.ReportItem;
import com.ntxdev.zuptecnico.entities.collections.ReportItemCollection;
import com.ntxdev.zuptecnico.ui.WebImageView;
import com.ntxdev.zuptecnico.util.Utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Header;
import retrofit.client.Response;

/**
 * Created by igorlira on 7/18/15.
 */
public class ReportsAdapter extends BaseAdapter implements Callback<ReportItemCollection> {
    private SparseArray<ViewGroup> viewCache;
    private boolean moreItemsAvailable;
    private View loadingView;
    private Map<String, Object> filterOptions;
    private int sizeOfRealList;

    private List<ReportItem> items;
    private int pageId; // next page that will be loaded

    private Context context;
    private ReportsAdapterListener listener;
    private String query;

    public void setQuery(String query) {
        if(query.trim().isEmpty()){
            this.query = null;
            clear();
        }else {
            this.query = query;
            reset();
        }

    }

    public String getQuery() {
        return query;
    }

    public int getSizeOfRealList(){
        return sizeOfRealList;
    }

    @Override
    public void success(ReportItemCollection reportItemCollection, Response response) {
        if((query != null && !query.isEmpty()) || (filterOptions != null)) {
            List<Header> headerList = response.getHeaders();
            for (Header header : headerList) {
                if (header.getName().equals("total")) {
                    sizeOfRealList = Integer.parseInt(header.getValue());
                    break;
                }
            }
        }else{
            sizeOfRealList = 0;
        }
        ReportItem[] reportItems = reportItemCollection.reports;
        if (reportItems == null) {
            ReportsAdapter.this.setMoreItemsAvailable(false);
            if (listener != null)
                listener.onNetworkError();
            return;
        }

        if(reportItems.length == 0){
            if(pageId == 1 && (filterOptions != null  || (query != null && !query.isEmpty()))) {
                listener.onEmptyResultsLoaded();
                return;
            }
        }

        for (int i = 0; i < reportItems.length; i++) {
            items.add(reportItems[i]);
        }

        pageId = pageId + 1;

        if (listener != null) {
            listener.onReportsLoaded();
        }

        ReportsAdapter.this.setMoreItemsAvailable(reportItems.length > 0);
        ReportsAdapter.this.notifyDataSetChanged();
    }

    @Override
    public void failure(RetrofitError error) {
        Log.e("RETROFIT", "Could not load reports list", error);
        listener.onNetworkError();
    }

    public interface ReportsAdapterListener {
        void onReportsLoaded();
        void onEmptyResultsLoaded();
        void onNetworkError();
    }

    public void setListener(ReportsAdapterListener listener) {
        this.listener = listener;
    }

    protected ReportsAdapterListener getListener() {
        return this.listener;
    }

    public ReportsAdapter(Context context) {
        this.context = context;

        items = new ArrayList<>();
        viewCache = new SparseArray<>();
        moreItemsAvailable = false;
        pageId = 1;
    }

    public boolean isFiltered() {
        return query != null;
    }

    public void setFilterOptions(HashMap<String, Object> filterOptions) {
        this.filterOptions = filterOptions;
    }

    public Map<String, Object> getFilterOptions(){
        return filterOptions;
    }


    public void load() {
        if (!Utilities.isConnected(getContext())) {
            if (listener != null) {
                listener.onNetworkError();
                return;
            }
        }
        pageId = 1;
        loadMore();
    }

    public void reset() {
        items.clear();
        notifyDataSetInvalidated();
        moreItemsAvailable = false;
        load();
    }

    public void clear() {
        items.clear();
        notifyDataSetInvalidated();
        moreItemsAvailable = false;
    }

    public void setMoreItemsAvailable(boolean value) {
        boolean oldValue = this.moreItemsAvailable;
        this.moreItemsAvailable = value;
        if (oldValue != value)
            this.notifyDataSetChanged();
    }

    @Override
    public ReportItem getItem(int i) {
        if (moreItemsAvailable && i == getCount() - 1)
            return null;

        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        ReportItem item = getItem(i);
        if (item != null)
            return item.id;
        else
            return 0;
    }

    @Override
    public int getCount() {
        int count = items.size();
        if (moreItemsAvailable)
            count++; // the loading item

        return count;
    }

    void loadMore() {
        try {
            if (filterOptions != null) {
                Zup.getInstance().getService().retrieveFilteredReportItems(pageId,
                        filterOptions, this);
            } else if(query != null){
                Zup.getInstance().getService().retrieveReportItemsByAddressOrProtocol(pageId, query, this);
            }else {
                Zup.getInstance().getService().retrieveReportItemsListing(pageId, this);
            }
        } catch (RetrofitError ex) {
            Log.e("Retrofit", "Could not load report items", ex);
        }
    }

    public Context getContext() {
        return context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (moreItemsAvailable && position == getCount() - 1) { // loading
            loadMore();

            if (loadingView != null)
                return loadingView;
            else {
                LayoutInflater inflater = LayoutInflater.from(context);
                return loadingView = inflater.inflate(R.layout.listview_loadingmore, parent, false);
            }
        }

        ReportItem item = getItem(position);
        ViewGroup root;
        if (viewCache.get(position, null) != null) {
            root = viewCache.get(position);
            fillData(root, item);
        } else {
            LayoutInflater inflater = LayoutInflater.from(context);
            root = (ViewGroup) inflater.inflate(R.layout.report_list_item, parent, false);

            viewCache.put(position, root);

            fillData(root, item);
        }

        return root;
    }

    void fillData(ViewGroup root, ReportItem item) {
        TextView txtTitle = (TextView) root.findViewById(R.id.user_name);
        TextView txtAddress = (TextView) root.findViewById(R.id.user_email);
        TextView txtDate = (TextView) root.findViewById(R.id.report_date);
        TextView txtStatus = (TextView) root.findViewById(R.id.report_status);

        ReportCategory category = Zup.getInstance().getReportCategoryService().getReportCategory(item.category_id);
        root.findViewById(R.id.report_saved).setVisibility(
                Zup.getInstance().getReportItemService().hasReportItem(item.id) ? View.VISIBLE : View.GONE);
        if (category != null) {
            ReportCategory.Status status = null;
            if (item.status_id != 0)
                status = category.getStatus(item.status_id);

            txtTitle.setText(category.title);
            if (status != null) {
                txtStatus.setText(status.getTitle());
                txtStatus.setTextColor(status.getUiColor());
            } else {
                txtStatus.setVisibility(View.GONE);
            }
        } else {
            txtStatus.setVisibility(View.GONE);
            txtTitle.setText(context.getString(R.string.error_category_not_found).toUpperCase() + item.category_id + " ####");
        }

        if (item.protocol != null) {
            txtAddress.setText("Protocolo " + item.protocol + " - " + item.address);
        }else {
            txtAddress.setText(item.address);
        }
        txtDate.setText(context.getString(R.string.inclusion_date_title) +" " + Zup.getInstance().formatIsoDate(item.created_at));
        WebImageView imageView = (WebImageView) root.findViewById(R.id.report_category_image);
        if (category != null) {
            category.loadImageInto(imageView);
        }
    }
}
