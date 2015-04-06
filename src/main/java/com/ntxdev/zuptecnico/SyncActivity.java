package com.ntxdev.zuptecnico;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ntxdev.zuptecnico.api.DeleteInventoryItemSyncAction;
import com.ntxdev.zuptecnico.api.EditInventoryItemSyncAction;
import com.ntxdev.zuptecnico.api.FillCaseStepSyncAction;
import com.ntxdev.zuptecnico.api.PublishInventoryItemSyncAction;
import com.ntxdev.zuptecnico.api.SyncAction;
import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.api.ZupCache;
import com.ntxdev.zuptecnico.entities.Case;
import com.ntxdev.zuptecnico.entities.Flow;
import com.ntxdev.zuptecnico.entities.InventoryCategory;
import com.ntxdev.zuptecnico.ui.UIHelper;

import java.util.Iterator;

/**
 * Created by igorlira on 12/30/14.
 */
public class SyncActivity extends ActionBarActivity
{
    Menu _menu;

    class Receiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            SyncActivity.this.onReceive(context, intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
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

        if(Zup.getInstance().isSyncing())
            hideButton();

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_sync)
        {
            Zup.getInstance().sync();
        }

        return super.onOptionsItemSelected(item);
    }

    public void onReceive(Context context, Intent intent)
    {
        if(intent.getAction().equals(SyncAction.ACTION_SYNC_CHANGED))
        {
            //SyncAction action = (SyncAction) intent.getSerializableExtra("sync_action");
            fillItems();
        }
        else if(intent.getAction().equals(SyncAction.ACTION_SYNC_BEGIN))
        {
            hideButton();
        }
        else if(intent.getAction().equals(SyncAction.ACTION_SYNC_END))
        {
            showButton();
        }
    }

    void showButton()
    {
        MenuItem item = _menu.findItem(R.id.action_sync);
        item.setVisible(true);
    }

    void hideButton()
    {
        MenuItem item = _menu.findItem(R.id.action_sync);
        item.setVisible(false);
    }

    void fillItems()
    {
        ViewGroup container = (ViewGroup) findViewById(R.id.activity_sync_list);
        container.removeAllViews();

        boolean hasAny = false;

        Iterator<SyncAction> actionIterator = Zup.getInstance().getSyncActions();
        while(actionIterator.hasNext())
        {
            SyncAction action = actionIterator.next();
            View view = setupItemView(action);

            container.addView(view);

            hasAny = true;
        }

        findViewById(R.id.activity_sync_none).setVisibility(!hasAny ? View.VISIBLE : View.GONE);
    }

    View setupItemView(final SyncAction action)
    {
        View view = getLayoutInflater().inflate(R.layout.fragment_inventory_item, null);

        TextView textTitle = (TextView) view.findViewById(R.id.fragment_inventory_item_title);
        TextView textDescription = (TextView) view.findViewById(R.id.fragment_inventory_item_desc);
        TextView stateDesc = (TextView) view.findViewById(R.id.fragment_inventory_item_statedesc);

        if(action instanceof PublishInventoryItemSyncAction)
        {
            PublishInventoryItemSyncAction publish = (PublishInventoryItemSyncAction) action;
            InventoryCategory category = Zup.getInstance().getInventoryCategory(publish.item.inventory_category_id);

            textTitle.setText("Criar Item de Inventário");
            textDescription.setText(category.title);
        }
        else if(action instanceof EditInventoryItemSyncAction)
        {
            EditInventoryItemSyncAction edit = (EditInventoryItemSyncAction) action;

            textTitle.setText("Editar Item de Inventário");
            textDescription.setText(Zup.getInstance().getInventoryItemTitle(edit.item));
        }
        else if(action instanceof DeleteInventoryItemSyncAction)
        {
            DeleteInventoryItemSyncAction delete = (DeleteInventoryItemSyncAction) action;
            InventoryCategory category = Zup.getInstance().getInventoryCategory(delete.categoryId);

            textTitle.setText("Remover Item de Inventário");
            textDescription.setText("ID: " + delete.itemId + ", Categoria: " + category.title);
        }
        else if(action instanceof FillCaseStepSyncAction)
        {
            FillCaseStepSyncAction fill = (FillCaseStepSyncAction) action;

            Case kase = Zup.getInstance().getCase(fill.caseId);
            Flow flow = Zup.getInstance().getFlow(kase.initial_flow_id, kase.flow_version);

            textTitle.setText("Preencher Etapa do Caso");
            if(kase != null && flow != null)
                textDescription.setText(flow.title + " #" + kase.id);
            else
                textDescription.setText("Caso #" + fill.caseId + " Etapa #" + fill.stepId);
        }

        int color;
        String text;
        if(action.isPending())
        {
            text = "Pendente";
            color = 0xff666666;
        }
        else if(action.isRunning())
        {
            text = "Em execução";
            color = 0xffffac2d;
        }
        else if(action.wasSuccessful())
        {
            text = "Concluído";
            color = 0xff78c953;
        }
        else
        {
            text = "Erro";
            color = 0xffff6049;
        }


        stateDesc.setText(text);
        stateDesc.setBackgroundColor(color);

        if(action.getError() != null) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showError(action);
                }
            });
            view.setClickable(true);
        }

        return view;
    }

    @Override
    public void onBackPressed() {

    }

    void showError(final SyncAction action)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Erro");
        builder.setMessage("Data: " + Zup.getInstance().getDateFormat().format(action.getDate()) + "\r\n\r\n" + action.getError());
        builder.setPositiveButton("Tentar Novamente", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                tryAgain(action);
            }
        });
        builder.setNeutralButton("Fechar", null);
        builder.setNegativeButton("Cancelar ação", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                cancel(action);
            }
        });
        builder.show();
    }

    void tryAgain(SyncAction action)
    {
        Zup.getInstance().performSyncAction(action);
    }

    void cancel(final SyncAction action)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("AVISO");
        builder.setMessage("Ao cancelar uma sincronização, o item associado a ela será removido do dispositivo e ficará indisponível enquanto não houver conexão com a internet. Deseja cancelar mesmo assim?");
        builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                cancelConfirm(action);
            }
        });
        builder.setNegativeButton("Não", null);
        builder.show();
    }

    void cancelConfirm(SyncAction action)
    {
        if(action instanceof PublishInventoryItemSyncAction)
        {
            PublishInventoryItemSyncAction publishInventoryItemSyncAction = (PublishInventoryItemSyncAction) action;
            Zup.getInstance().removeInventoryItem(publishInventoryItemSyncAction.item.id);
            ZupCache.removeInventoryItem(publishInventoryItemSyncAction.item.id);
        }
        else if(action instanceof EditInventoryItemSyncAction)
        {
            EditInventoryItemSyncAction publishInventoryItemSyncAction = (EditInventoryItemSyncAction) action;
            Zup.getInstance().removeInventoryItem(publishInventoryItemSyncAction.item.id);
            ZupCache.removeInventoryItem(publishInventoryItemSyncAction.item.id);
        }
        Zup.getInstance().removeSyncAction(action.getId());
        fillItems();
    }
}
