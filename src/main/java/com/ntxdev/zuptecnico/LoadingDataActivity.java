package com.ntxdev.zuptecnico;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.entities.InventoryCategory;
import com.ntxdev.zuptecnico.entities.collections.InventoryCategoryCollection;
import com.ntxdev.zuptecnico.entities.collections.InventoryCategoryStatusCollection;
import com.ntxdev.zuptecnico.entities.collections.ReportCategoryCollection;
import com.ntxdev.zuptecnico.entities.collections.SingleUserCollection;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by igorlira on 5/27/15.
 */
public class LoadingDataActivity extends AppCompatActivity {
    private int loadCategoriesJobId;
    private ArrayList<Integer> loadPinsJobIds;
    private ArrayList<Integer> loadStatusesJobIds;
    private Tasker tasker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        Zup.getInstance().initStorage(this);

        begin();
    }

    void begin() {
        tasker = new Tasker();
        tasker.execute();
    }

    void everythingLoaded() {
        Zup.getInstance().getStorage().setHasFullLoad();

        Intent intent = new Intent(this, ItemsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {

    }

    public void onJobFailed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);

        if (Zup.getInstance().getStorage().hasFullLoad()) {
            builder.setMessage(getString(R.string.error_session_outdated));
            builder.setPositiveButton(getString(R.string.continue_text), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    everythingLoaded();
                }
            });
        } else {
            builder.setMessage(getString(R.string.error_no_internet_loading));
            builder.setPositiveButton(getString(R.string.try_again), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    begin();
                }
            });
        }
        builder.show();
    }

    class Tasker extends AsyncTask<Void, String, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                this.loadCategories();
                this.loadStatuses();
                this.loadReportCategories();
                this.loadUser();

                return true;
            } catch (Exception ex) {
                Log.e("Loading data", ex.getMessage(), ex);
                return false;
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            TextView tv = (TextView) findViewById(R.id.loading_status);
            tv.setText(values[0]);
        }

        void loadCategories() {
            this.publishProgress(getString(R.string.loading_inventory_categories));
            InventoryCategoryCollection categories = Zup.getInstance().getService().getInventoryCategories();

            Zup.getInstance().inventoryItemCategoriesReceived(categories);
        }

        void loadStatuses() {
            this.publishProgress(getString(R.string.loading_inventory_status));

            Iterator<InventoryCategory> categoryIterator = Zup.getInstance().getInventoryCategories();
            while (categoryIterator.hasNext()) {
                InventoryCategory category = categoryIterator.next();

                InventoryCategoryStatusCollection statuses = Zup.getInstance().getService().getInventoryCategoryStatuses(category.id);
                Zup.getInstance().inventoryCategoryStatusesReceived(statuses);
            }
        }

        void loadReportCategories() {
            this.publishProgress(getString(R.string.loading_report_categories));

            ReportCategoryCollection categories = Zup.getInstance().getService().getReportCategories();
            Zup.getInstance().getReportCategoryService().setReportCategories(categories.categories);
        }

        void loadUser() {
            this.publishProgress(getString(R.string.loading_user_details));

            SingleUserCollection userCollection = Zup.getInstance().getService().retrieveUser(Zup.getInstance().getSessionUserId());
            Zup.getInstance().getUserService().addUser(userCollection.user);
            Zup.getInstance().refreshAccess();
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            if (aBoolean)
                everythingLoaded();
            else
                onJobFailed();
        }
    }
}
