package com.ntxdev.zuptecnico.activities.reports;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.ntxdev.zuptecnico.R;
import com.ntxdev.zuptecnico.ZupApplication;
import com.ntxdev.zuptecnico.adapters.ReportItemCommentsAdapter;
import com.ntxdev.zuptecnico.api.DeleteReportItemSyncAction;
import com.ntxdev.zuptecnico.api.PublishReportCommentSyncAction;
import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.entities.ReportHistoryItem;
import com.ntxdev.zuptecnico.entities.ReportItem;
import com.ntxdev.zuptecnico.entities.ReportNotificationCollection;
import com.ntxdev.zuptecnico.entities.collections.SingleReportItemCollection;
import com.ntxdev.zuptecnico.fragments.reports.ReportItemCommentsFragment;
import com.ntxdev.zuptecnico.fragments.reports.ReportItemGeneralInfoFragment;
import com.ntxdev.zuptecnico.fragments.reports.ReportItemHistoryFragment;
import com.ntxdev.zuptecnico.fragments.reports.ReportItemImagesFragment;
import com.ntxdev.zuptecnico.fragments.reports.ReportItemMapFragment;
import com.ntxdev.zuptecnico.fragments.reports.ReportItemNotificationFragment;
import com.ntxdev.zuptecnico.fragments.reports.ReportItemUserInfoFragment;
import com.ntxdev.zuptecnico.tasks.ReportItemDownloader;
import com.ntxdev.zuptecnico.ui.UIHelper;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by igorlira on 7/18/15.
 */
public class ReportItemDetailsActivity extends AppCompatActivity
        implements Callback<SingleReportItemCollection> {
    public static final int REQUEST_EDIT_REPORT = 1;
    public static final int RESULT_DELETED = 1;
    public static final int RESULT_CHANGED = 2;

    ReportItemGeneralInfoFragment generalInfo;
    ReportItemImagesFragment images;
    ReportItemMapFragment map;
    ReportItemNotificationFragment notificationInfo;
    ReportItemUserInfoFragment userInfo;
    ReportItemCommentsFragment comments;
    ReportItemCommentsFragment internalComments;
    ReportItemHistoryFragment history;
    Bundle bundle;

    BroadcastReceiver createdReceiver;
    private boolean mReportChanged = false;
    Menu menu;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_report_item_details);
        UIHelper.initActivity(this, false);

        if(savedInstanceState != null) {
            bundle = savedInstanceState;
            hideLoading();
            if(getItem() != null)
                itemLoaded();
        }
        else {
            bundle = new Bundle();

            int itemId = getIntent().getIntExtra("item_id", -1);
            if(Zup.getInstance().getReportItemService().hasReportItem(itemId)) {
                ReportItem item = Zup.getInstance().getReportItemService().getReportItem(itemId);
                ReportHistoryItem[] history = Zup.getInstance().getReportItemService()
                        .getReportItemHistory(itemId);
                bundle.putParcelable("item", item);
                bundle.putParcelable("user", item.user);
                if(history != null) {
                    bundle.putParcelableArray("history", history);
                }
                loadNotifications(itemId, item.category_id);
            }
            else {
                loadItem(itemId);
            }
        }

        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(ZupApplication.getContext());
        manager.registerReceiver(createdReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int itemId = intent.getIntExtra("report_id", -1);
                ReportItem.Comment comment = (ReportItem.Comment) intent.getExtras().getParcelable("comment");
                commentCreated(itemId, comment);
            }
        }, new IntentFilter(PublishReportCommentSyncAction.REPORT_COMMENT_CREATED));
    }

    private void loadNotifications(int itemId, int categoryId) {
        Zup.getInstance().getService().retrieveReportNotificationCollection(itemId, categoryId, new Callback<ReportNotificationCollection>() {
            @Override
            public void success(ReportNotificationCollection reportNotification, Response response) {
                if (reportNotification != null && reportNotification.notifications != null) {
                    bundle.putParcelableArray("notifications", reportNotification.notifications);
                }
                itemLoaded();
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("RETROFIT", "Could not load report notifications", error);
                itemLoaded();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(ZupApplication.getContext());
        manager.unregisterReceiver(createdReceiver);
    }

    private void commentCreated(int itemId, ReportItem.Comment comment) {
        if(itemId == getItem().id) {

            // Add the comment to the loaded item
            if(!Zup.getInstance().getReportItemService().hasReportItem(itemId)) {
                ReportItem item = getItem();
                item.addComment(comment);

                bundle.putParcelable("item", item);
            }
            else {
                ReportItem item = Zup.getInstance().getReportItemService().getReportItem(itemId);
                if(item != null) {
                    // Refresh the item in the bundle
                    bundle.putParcelable("item", item);
                }

                item.addComment(comment);
                Zup.getInstance().getReportItemService().addReportItem(item);
            }
            comments.refresh(getItem());
            internalComments.refresh(getItem());
        }
    }

    private void commentCreationFailed(int itemId, int type, String message) {
        Toast.makeText(this, getString(R.string.unable_create_comment), Toast.LENGTH_LONG).show();
    }

    ReportItem getItem() {
        return (ReportItem) bundle.getParcelable("item");
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);

        outState.putAll(bundle);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putAll(bundle);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        this.bundle = savedInstanceState;
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onRestoreInstanceState(savedInstanceState, persistentState);

        this.bundle = savedInstanceState;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.report_item_details, menu);
        this.menu = menu;
        setUpMenu();
        return super.onCreateOptionsMenu(menu);
    }

    private void setUpMenu()
    {
        if(this.menu != null && this.getItem() != null)
        {
            if(Zup.getInstance().getReportItemService().hasReportItem(getItem().id)) {
                menu.findItem(R.id.action_delete_local).setVisible(true);
                menu.findItem(R.id.action_download).setVisible(false);
            }
            else {
                menu.findItem(R.id.action_delete_local).setVisible(false);
                menu.findItem(R.id.action_download).setVisible(true);
            }

            menu.findItem(R.id.action_edit).setVisible(Zup.getInstance().getAccess()
                    .canEditReportItem(getItem().category_id));

            menu.findItem(R.id.action_delete).setVisible(Zup.getInstance().getAccess()
                    .canDeleteReportItem(getItem().category_id));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(this.getItem() == null)
            return false;

        if(item.getItemId() == R.id.action_delete) {
            showConfirmDeleteDialog();
        }
        else if(item.getItemId() == R.id.action_edit) {
            showEditScreen();
        }
        else if(item.getItemId() == R.id.action_download) {
            saveItem();
            setUpMenu();
        }
        else if(item.getItemId() == R.id.action_delete_local) {
            Zup.getInstance().getReportItemService().deleteReportItem(getItem().id);
            setUpMenu();

            setResult(RESULT_CHANGED);
        }
        return super.onOptionsItemSelected(item);
    }

    void showConfirmDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.usure));
        builder.setMessage(getString(R.string.delete_report_confirm_text));
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                confirmDelete();
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    void confirmDelete() {
        DeleteReportItemSyncAction action = new DeleteReportItemSyncAction(getItem().id);
        Zup.getInstance().addSyncAction(action);

        Zup.getInstance().sync();

        setResult(RESULT_DELETED);
        finish();
    }

    void showEditScreen() {
        Intent intent = new Intent(this, CreateReportItemActivity.class);
        intent.putExtra("item", this.getItem());
        startActivityForResult(intent, REQUEST_EDIT_REPORT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_EDIT_REPORT && resultCode == CreateReportItemActivity.RESULT_REPORT_CHANGED) {
            ReportItem item = (ReportItem) data.getExtras().getParcelable("item");
            this.bundle.putParcelable("item", item);
            refreshData();

            mReportChanged = true;
            setResult(RESULT_CHANGED);
        }
    }

    private void refreshData() {
        generalInfo.refresh();
        images.refresh();
        map.refresh();
    }

    void loadItem(int id) {
        if(id == -1) {
            finish();
            return;
        }

        Zup.getInstance().getService().retrieveReportItem(id, this);
        showLoading();
    }

    void saveItem() {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMax(100);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setIndeterminate(false);
        dialog.setProgressNumberFormat(null);
        dialog.setMessage(getString(R.string.message_wait_loading_data));
        dialog.show();

        ReportItemDownloader downloader = new ReportItemDownloader(getApplicationContext(), getItem().id,
                new ReportItemDownloader.Listener() {
                    @Override
                    public void onProgress(float progress) {
                        dialog.setProgress((int)(progress * 100.0f));
                    }

                    @Override
                    public void onFinished() {
                        dialog.dismiss();
                        setUpMenu();
                    }

                    @Override
                    public void onError() {
                        showDownloadError();
                    }
                }
        );
        downloader.execute();
    }

    void showDownloadError() {
        Toast.makeText(this, getString(R.string.error_unable_load_report_item), Toast.LENGTH_LONG).show();
    }

    void showLoading() {
        findViewById(R.id.wait_sync_standard_message).setVisibility(View.VISIBLE);
        findViewById(R.id.report_loading).setVisibility(View.VISIBLE);
    }

    void hideLoading() {
        findViewById(R.id.wait_sync_standard_message).setVisibility(View.GONE);
        findViewById(R.id.report_loading).setVisibility(View.GONE);
    }

    void itemLoaded() {
        hideLoading();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        generalInfo = new ReportItemGeneralInfoFragment();
        generalInfo.setArguments(this.bundle);
        transaction.add(R.id.listView, generalInfo, "general");

        images = new ReportItemImagesFragment();
        images.setArguments(this.bundle);
        transaction.add(R.id.listView, images, "images");

        map = new ReportItemMapFragment();
        map.setArguments(this.bundle);
        transaction.add(R.id.listView, map, "map");

        userInfo = new ReportItemUserInfoFragment();
        userInfo.setArguments(this.bundle);
        transaction.add(R.id.listView, userInfo, "user_info");

        if(bundle.containsKey("notifications")) {
            notificationInfo = new ReportItemNotificationFragment();
            notificationInfo.setArguments(this.bundle);
            transaction.add(R.id.listView, notificationInfo, "notification_info");
        }

        comments = new ReportItemCommentsFragment();
        Bundle commentsBundle = new Bundle(this.bundle);
        commentsBundle.putInt("filter_type", ReportItemCommentsAdapter.FILTER_COMMENTS);
        comments.setArguments(commentsBundle);
        transaction.add(R.id.listView, comments, "comments");



        internalComments = new ReportItemCommentsFragment();
        commentsBundle = new Bundle(this.bundle);
        commentsBundle.putInt("filter_type", ReportItemCommentsAdapter.FILTER_INTERNAL);
        internalComments.setArguments(commentsBundle);
        transaction.add(R.id.listView, internalComments, "comments_internal");

        history = new ReportItemHistoryFragment();
        history.setArguments(this.bundle);
        transaction.add(R.id.listView, history, "history");

        if(getItem().images == null || getItem().images.length == 0)
            transaction.hide(images);

        try {
            transaction.commit();
        }
        catch (Exception ex) {
            // FIXME Sometimes this will hang and crash after the activity is finished
        }

        setUpMenu();
    }

    @Override
    public void success(SingleReportItemCollection singleReportItemCollection, Response response) {
        ReportItem item = singleReportItemCollection.report;
        loadNotifications(item.id, item.category_id);
        bundle.putParcelable("item", item);
        bundle.putParcelable("user", item.user);
    }

    @Override
    public void failure(RetrofitError error) {
        Log.e("RETROFIT", "Could not load report item", error);

        Toast.makeText(this, getString(R.string.error_loading_report_item), Toast.LENGTH_LONG).show();
        finish();
    }
}
