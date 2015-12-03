package com.ntxdev.zuptecnico;

import android.support.v7.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ntxdev.zuptecnico.api.DeleteInventoryItemSyncAction;
import com.ntxdev.zuptecnico.api.DeleteReportItemSyncAction;
import com.ntxdev.zuptecnico.api.EditInventoryItemSyncAction;
import com.ntxdev.zuptecnico.api.EditReportItemSyncAction;
import com.ntxdev.zuptecnico.api.FillCaseStepSyncAction;
import com.ntxdev.zuptecnico.api.PublishInventoryItemSyncAction;
import com.ntxdev.zuptecnico.api.PublishReportCommentSyncAction;
import com.ntxdev.zuptecnico.api.PublishReportItemSyncAction;
import com.ntxdev.zuptecnico.api.SyncAction;
import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.api.ZupCache;
import com.ntxdev.zuptecnico.config.Constants;
import com.ntxdev.zuptecnico.entities.Case;
import com.ntxdev.zuptecnico.entities.Flow;
import com.ntxdev.zuptecnico.entities.InventoryCategory;
import com.ntxdev.zuptecnico.entities.ReportCategory;
import com.ntxdev.zuptecnico.entities.ReportItem;
import com.ntxdev.zuptecnico.entities.collections.SingleReportItemCollection;
import com.ntxdev.zuptecnico.entities.requests.CreateArbitraryReportItemRequest;
import com.ntxdev.zuptecnico.entities.requests.CreateReportItemRequest;
import com.ntxdev.zuptecnico.ui.UIHelper;
import com.ntxdev.zuptecnico.util.Utilities;

import java.util.Iterator;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by igorlira on 12/30/14.
 */
public class SyncActivity extends AppCompatActivity {
    Menu _menu;

    class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            SyncActivity.this.onReceive(context, intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_sync);

        UIHelper.initActivity(this, true);

        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);


        manager.registerReceiver(new Receiver(), new IntentFilter(SyncAction.ACTION_SYNC_BEGIN));
        manager.registerReceiver(new Receiver(), new IntentFilter(SyncAction.ACTION_SYNC_END));
        manager.registerReceiver(new Receiver(), new IntentFilter(SyncAction.ACTION_SYNC_CHANGED));

        this.fillItems();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sync, menu);

        this._menu = menu;

        if (Zup.getInstance().isSyncing())
            hideButton();

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_sync) {
            if (Utilities.isConnected(this)) {
                Zup.getInstance().sync();
            } else {
                Toast.makeText(this, getString(R.string.error_no_internet_toast), Toast.LENGTH_SHORT).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(SyncAction.ACTION_SYNC_CHANGED)) {
            //SyncAction action = (SyncAction) intent.getSerializableExtra("sync_action");
            fillItems();
        } else if (intent.getAction().equals(SyncAction.ACTION_SYNC_BEGIN)) {
            hideButton();
        } else if (intent.getAction().equals(SyncAction.ACTION_SYNC_END)) {
            showButton();
        }
    }

    void showButton() {
        MenuItem item = _menu.findItem(R.id.action_sync);
        item.setVisible(true);
    }

    void hideButton() {
        MenuItem item = _menu.findItem(R.id.action_sync);
        item.setVisible(false);
    }

    void fillItems() {
        ViewGroup container = (ViewGroup) findViewById(R.id.activity_sync_list);
        container.removeAllViews();

        boolean hasAny = false;

        Iterator<SyncAction> actionIterator = Zup.getInstance().getSyncActions();
        while (actionIterator.hasNext()) {
            SyncAction action = actionIterator.next();
            View view = setupItemView(action);

            container.addView(view);

            hasAny = true;
        }

        findViewById(R.id.activity_sync_none).setVisibility(!hasAny ? View.VISIBLE : View.GONE);
    }

    View setupItemView(final SyncAction action) {
        View view = getLayoutInflater().inflate(R.layout.fragment_inventory_item, null);

        TextView textTitle = (TextView) view.findViewById(R.id.fragment_inventory_item_title);
        TextView textDescription = (TextView) view.findViewById(R.id.fragment_inventory_item_desc);
        TextView stateDesc = (TextView) view.findViewById(R.id.fragment_inventory_item_statedesc);

        if (action instanceof PublishInventoryItemSyncAction) {
            PublishInventoryItemSyncAction publish = (PublishInventoryItemSyncAction) action;
            InventoryCategory category = Zup.getInstance().getInventoryCategory(publish.item.inventory_category_id);

            textTitle.setText(getString(R.string.create_inventory_item_title));
            textDescription.setText(category.title);
        } else if (action instanceof EditInventoryItemSyncAction) {
            EditInventoryItemSyncAction edit = (EditInventoryItemSyncAction) action;

            textTitle.setText(getString(R.string.edit_inventory_item_title));
            textDescription.setText(Zup.getInstance().getInventoryItemTitle(edit.item));
        } else if (action instanceof DeleteInventoryItemSyncAction) {
            DeleteInventoryItemSyncAction delete = (DeleteInventoryItemSyncAction) action;
            InventoryCategory category = Zup.getInstance().getInventoryCategory(delete.categoryId);

            textTitle.setText(getString(R.string.delete_inventory_item_title));
            textDescription.setText(getString(R.string.id_title).toUpperCase() + ": "+  delete.itemId + ", " + getString(R.string.category_title) +": " + category.title);
        } else if (action instanceof FillCaseStepSyncAction) {
            FillCaseStepSyncAction fill = (FillCaseStepSyncAction) action;

            Case kase = Zup.getInstance().getCase(fill.caseId);
            Flow flow = Zup.getInstance().getFlow(kase.initial_flow_id, kase.flow_version);

            textTitle.setText(getString(R.string.fill_case_step));
            if (kase != null && flow != null)
                textDescription.setText(flow.title + " #" + kase.id);
            else
                textDescription.setText(getString(R.string.case_title) + " #" + fill.caseId + " " + getString(R.string.step_title) + " #" + fill.stepId);
        } else if (action instanceof PublishReportItemSyncAction) {
            PublishReportItemSyncAction publish = (PublishReportItemSyncAction) action;

            ReportCategory category = Zup.getInstance().getReportCategoryService()
                    .getReportCategory(publish.categoryId);

            textTitle.setText(getString(R.string.activity_title_create_report_item));
            if (category != null)
                textDescription.setText(category.title);
            else
                textDescription.setText(getString(R.string.invalid_category));
        } else if (action instanceof DeleteReportItemSyncAction) {
            DeleteReportItemSyncAction delete = (DeleteReportItemSyncAction) action;

            textTitle.setText(getString(R.string.delete_report_title));
            textDescription.setText(String.valueOf(delete.itemId));
        } else if (action instanceof EditReportItemSyncAction) {
            EditReportItemSyncAction edit = (EditReportItemSyncAction) action;

            ReportCategory category = Zup.getInstance().getReportCategoryService()
                    .getReportCategory(edit.categoryId);

            textTitle.setText(getString(R.string.edit_report_title));
            if (category != null)
                textDescription.setText(category.title);
            else
                textDescription.setText(getString(R.string.invalid_category));
        } else if (action instanceof PublishReportCommentSyncAction) {
            PublishReportCommentSyncAction publish = (PublishReportCommentSyncAction) action;

            textTitle.setText(getString(R.string.create_comment_report_title));
            textDescription.setText(publish.message);
        }

        int color;
        String text;
        if (action.isPending()) {
            text = getString(R.string.pending_title);
            color = getResources().getColor(R.color.pending_action_color);
        } else if (action.isRunning()) {
            text = getString(R.string.in_execution);
            color = getResources().getColor(R.color.running_action_color);
        } else if (action.wasSuccessful()) {
            text = getString(R.string.done);
            color = getResources().getColor(R.color.completed_action_color);
        } else {
            text = getString(R.string.error_title);
            color = getResources().getColor(R.color.error_action_color);
        }


        stateDesc.setText(text);
        stateDesc.setBackgroundColor(color);

        if (action.getError() != null) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showError(action);
                }
            });
            view.setClickable(true);
            if (action instanceof EditReportItemSyncAction && action.getError().equals(EditReportItemSyncAction.NOT_FOUND_ERROR)) {
                askAboutNotFoundReport((EditReportItemSyncAction) action);
            }
        }


        return view;
    }

    private void askAboutNotFoundReport(final EditReportItemSyncAction action) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.error_title));
        builder.setMessage(getString(R.string.not_found_report_message_header) + action.id + getString(R.string.not_found_report_footer));
        builder.setPositiveButton(getString(R.string.save_new_report), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ReportItem item = Zup.getInstance().getReportItemService().getReportItem(action.id);
                SyncAction newReportAction = new PublishReportItemSyncAction(
                        Constants.DEFAULT_LAT,
                        Constants.DEFAULT_LON,
                        item.category_id,
                        item.description,
                        item.reference,
                        item.address,
                        item.district,
                        item.city,
                        item.state,
                        item.country,
                        action.images,
                        Zup.getInstance().getUserService().getUser(Zup.getInstance().getSessionUserId())
                );
                Zup.getInstance().addSyncAction(newReportAction);
                Zup.getInstance().performSyncAction(newReportAction);
                cancelConfirm(action);
            }
        });
        builder.setNegativeButton(getString(R.string.undo_changes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cancelConfirm(action);
            }
        });
        builder.create().show();
    }

    @Override
    public void onBackPressed() {

    }

    void showItem(final SyncAction action) {
        int itemId, categoryId;

        if (action instanceof PublishInventoryItemSyncAction) {
            itemId = ((PublishInventoryItemSyncAction) action).item.id;
            categoryId = ((PublishInventoryItemSyncAction) action).item.inventory_category_id;
        } else if (action instanceof EditInventoryItemSyncAction) {
            itemId = ((EditInventoryItemSyncAction) action).item.id;
            categoryId = ((EditInventoryItemSyncAction) action).item.inventory_category_id;
        } else
            return;

        Intent intent = new Intent(this, InventoryItemDetailsActivity.class);
        intent.putExtra("item_id", itemId);
        intent.putExtra("categoryId", categoryId);
        intent.putExtra("fake_create", true);
        this.startActivityForResult(intent, 0);
        this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    void showErrorMessage(final SyncAction action) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.error_title));
        builder.setMessage(getString(R.string.date_title) + ": " + Zup.getInstance().getDateFormat().format(action.getDate()) + "\r\n\r\n" + action.getError());
        builder.create().show();
    }

    void showError(final SyncAction action) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.error_title));

        final boolean isItems = action instanceof PublishInventoryItemSyncAction || action instanceof EditInventoryItemSyncAction;

        String[] defaultItems = new String[]{getString(R.string.see_message), getString(R.string.try_again), getString(R.string.cancel_action), getString(R.string.close_title)};
        if (isItems)
            defaultItems = new String[]{getString(R.string.see_message), getString(R.string.try_again), getString(R.string.cancel_action), getString(R.string.view_item_title), getString(R.string.close_title)};

        builder.setItems(defaultItems, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0)
                    showErrorMessage(action);
                else if (i == 1)
                    tryAgain(action);
                else if (i == 2)
                    cancel(action);
                else if (i == 3) {
                    if (isItems)
                        showItem(action);
                }
            }
        });

        builder.create().show();
    }

    void tryAgain(SyncAction action) {
        if (Utilities.isConnected(this)) {
            Zup.getInstance().performSyncAction(action);
        } else {
            Toast.makeText(this, getString(R.string.error_no_internet_toast), Toast.LENGTH_SHORT).show();
        }
    }

    void cancel(final SyncAction action) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.warning).toUpperCase());
        builder.setMessage(getString(R.string.cancel_sync_warning_message));
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                cancelConfirm(action);
            }
        });
        builder.setNegativeButton(getString(R.string.no), null);
        builder.show();
    }

    void cancelConfirm(SyncAction action) {
        if (action instanceof PublishInventoryItemSyncAction) {
            PublishInventoryItemSyncAction publishInventoryItemSyncAction = (PublishInventoryItemSyncAction) action;
            Zup.getInstance().removeInventoryItem(publishInventoryItemSyncAction.item.id);
            ZupCache.removeInventoryItem(publishInventoryItemSyncAction.item.id);
        } else if (action instanceof EditInventoryItemSyncAction) {
            EditInventoryItemSyncAction publishInventoryItemSyncAction = (EditInventoryItemSyncAction) action;
            Zup.getInstance().removeInventoryItem(publishInventoryItemSyncAction.item.id);
            ZupCache.removeInventoryItem(publishInventoryItemSyncAction.item.id);
        } else if (action instanceof EditReportItemSyncAction) {
            Zup.getInstance().getReportItemService().deleteReportItem(action.getId());
        }
        Zup.getInstance().removeSyncAction(action.getId());
        fillItems();
    }
}
