package com.ntxdev.zuptecnico.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ntxdev.zuptecnico.R;
import com.ntxdev.zuptecnico.entities.ReportNotificationCollection.ReportNotificationItem;
import com.ntxdev.zuptecnico.util.Utilities;

/**
 * Created by Renan on 04/09/2015.
 */
public class ReportItemNotificationsAdapter extends BaseAdapter {
    Context context;
    ReportNotificationItem[] items;
    SparseArray<View> viewCache;

    public ReportItemNotificationsAdapter(Context context, ReportNotificationItem[] items) {
        this.context = context;
        this.viewCache = new SparseArray<>();
        this.items = items;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    @Override
    public int getCount() {
        return items.length;
    }

    @Override
    public ReportNotificationItem getItem(int i) {
        return items[i];
    }

    @Override
    public long getItemId(int i) {
        return getItem(i).id;
    }

    @Override
    public View getView(int i, View v, ViewGroup viewGroup) {
        ReportNotificationItem comment = getItem(i);

        if (viewCache.get(comment.id) != null)
            return viewCache.get(comment.id);
        else {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.report_details_notificationinfo_item, viewGroup, false);
            fillData(view, comment);

            return view;
        }
    }

    void fillData(View root, final ReportNotificationItem notification) {
        if (notification == null)
            return;

        TextView txtName = (TextView) root.findViewById(R.id.notification_name);
        TextView txtDaysToDeadline = (TextView) root.findViewById(R.id.notification_days_to_deadline);
        TextView txtCreatedAt = (TextView) root.findViewById(R.id.notification_created_at);
        TextView txtDeadlineInDays = (TextView) root.findViewById(R.id.notification_deadline_in_days);

        txtName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNotification(notification);
            }
        });

        txtCreatedAt.setText(context.getString(R.string.notification_created_at_title) + " " +
                Utilities.formatIsoDateAndTime(notification.createdAt));
        txtDeadlineInDays.setText(context.getString(R.string.notification_deadline_in_days) + " " +
                notification.deadlineInDays + " " + context.getString(R.string.inventory_item_extra_days));

        txtName.setText(notification.notificationType.title);
        String daysToDeadLineFormatted = context.getString(R.string.notification_days_to_deadline) + " ";
        if (notification.daysToDeadline < 0) {
            daysToDeadLineFormatted += context.getString(R.string.overdue_notification_label_text) + " " +
                    notification.daysToDeadline + " " + context.getString(R.string.inventory_item_extra_days) +
                    " " + context.getString(R.string.ago);
        } else {
            daysToDeadLineFormatted += notification.daysToDeadline + " " + context.getString(R.string.inventory_item_extra_days);
        }
        txtDaysToDeadline.setText(Html.fromHtml(daysToDeadLineFormatted));
    }

    void showNotification(ReportNotificationItem item){
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        WebView wv = new WebView(context);
        wv.loadData(item.content, "text/html; charset=UTF-8", null);
        wv.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);

                return true;
            }
        });

        alert.setView(wv);
        alert.setNegativeButton(context.getString(R.string.close_title), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        alert.show();
    }
}