package com.ntxdev.zuptecnico;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.api.callbacks.JobListener;
import com.ntxdev.zuptecnico.entities.InventoryCategory;
import com.ntxdev.zuptecnico.ui.UIHelper;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by igorlira on 5/27/15.
 */
public class LoadingDataActivity extends ActionBarActivity implements JobListener
{
    private int loadCategoriesJobId;
    private ArrayList<Integer> loadPinsJobIds;
    private ArrayList<Integer> loadStatusesJobIds;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        getActionBar().hide();

        Zup.getInstance().initStorage(this);

        ImageView image = (ImageView) findViewById(R.id.loading_icon);
        RotateAnimation animation = new RotateAnimation(360, 0, 73 * getResources().getDisplayMetrics().density / 2, 100 * getResources().getDisplayMetrics().density / 2);
        animation.setRepeatCount(Animation.INFINITE);
        animation.setDuration(2000);
        animation.setInterpolator(new Interpolator() {
            @Override
            public float getInterpolation(float v) {
                return v;
            }
        });
        image.startAnimation(animation);

        loadInventoryCategories();
    }

    void loadInventoryCategories()
    {
        setStatus("Carregando categorias de Inventário");
        this.loadCategoriesJobId = Zup.getInstance().refreshInventoryItemCategories(this);
    }

    void loadPins()
    {
        setStatus("Carregando marcadores do mapa");
        Iterator<InventoryCategory> categoryIterator = Zup.getInstance().getInventoryCategories();
        this.loadPinsJobIds = new ArrayList<Integer>();

        while (categoryIterator.hasNext())
        {
            InventoryCategory category = categoryIterator.next();

            int jobId = Zup.getInstance().requestInventoryCategoryPin(category.id, this);
            if(jobId != -1)
                this.loadPinsJobIds.add(jobId);
        }

        if(this.loadPinsJobIds.size() == 0)
            this.allPinsLoaded();
    }

    void loadStatuses()
    {
        setStatus("Carregando estados de inventário");
        Iterator<InventoryCategory> categoryIterator = Zup.getInstance().getInventoryCategories();
        this.loadStatusesJobIds = new ArrayList<Integer>();

        while (categoryIterator.hasNext())
        {
            InventoryCategory category = categoryIterator.next();

            int jobId = Zup.getInstance().requestInventoryCategoryStatuses(category.id, this);
            if(jobId != -1)
                this.loadStatusesJobIds.add(jobId);
        }

        if(this.loadStatusesJobIds.size() == 0)
            this.allStatusesLoaded();
    }

    void setStatus(String status)
    {
        TextView tv = (TextView) findViewById(R.id.loading_status);
        tv.setText(status);
    }

    void inventoryCategoriesLoaded()
    {
        loadStatuses();
        //loadPins();
    }

    void allPinsLoaded()
    {
        loadStatuses();
    }

    void pinLoaded()
    {

    }

    void allStatusesLoaded()
    {
        everythingLoaded();
    }

    void statusLoaded()
    {

    }

    void everythingLoaded()
    {
        Zup.getInstance().getStorage().setHasFullLoad();

        Intent intent = new Intent(this, ItemsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void onJobSuccess(int jobId)
    {
        if(jobId == loadCategoriesJobId)
            inventoryCategoriesLoaded();
        else if(loadPinsJobIds != null && loadPinsJobIds.contains(jobId))
        {
            pinLoaded();
            loadPinsJobIds.remove((Integer)jobId);

            if(loadPinsJobIds.size() == 0)
                allPinsLoaded();
        }
        else if(loadStatusesJobIds != null && loadStatusesJobIds.contains(jobId))
        {
            statusLoaded();
            loadStatusesJobIds.remove((Integer)jobId);

            if(loadStatusesJobIds.size() == 0)
                allStatusesLoaded();
        }
    }

    @Override
    public void onJobFailed(int jobId)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);

        if(Zup.getInstance().getStorage().hasFullLoad())
        {
            builder.setMessage("Não foi possível carregar as informações do servidor. Os dados desta sessão poderão estar desatualizados em relação ao servidor.");
            builder.setPositiveButton("Continuar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    everythingLoaded();
                }
            });
        }
        else
        {
            builder.setMessage("Não foi possível carregar as informações do servidor e as informações ainda não foram sincronizadas. Conecte-se à internet para tentar novamente.");
            builder.setPositiveButton("Tentar novamente", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    loadInventoryCategories();
                }
            });
        }
        builder.show();
    }
}