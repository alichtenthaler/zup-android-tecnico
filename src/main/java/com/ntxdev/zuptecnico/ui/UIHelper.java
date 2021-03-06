package com.ntxdev.zuptecnico.ui;

import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ntxdev.zuptecnico.AdvancedSearchActivity;
import com.ntxdev.zuptecnico.CasesActivity;
import com.ntxdev.zuptecnico.ItemsActivity;
import com.ntxdev.zuptecnico.ProfileActivity;
import com.ntxdev.zuptecnico.R;
import com.ntxdev.zuptecnico.SearchBarListener;
import com.ntxdev.zuptecnico.SyncActivity;
import com.ntxdev.zuptecnico.activities.reports.ReportsListActivity;
import com.ntxdev.zuptecnico.api.SyncAction;
import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.entities.InventoryCategory;
import com.ntxdev.zuptecnico.entities.User;
import com.ntxdev.zuptecnico.util.ViewUtils;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by igorlira on 2/21/14.
 */
public class UIHelper
{
    static class Receiver extends BroadcastReceiver
    {
        AppCompatActivity activity;

        public Receiver(AppCompatActivity activity)
        {
            this.activity = activity;
        }

        @Override
        public void onReceive(Context context, Intent intent)
        {
            if(intent.getAction().equals(SyncAction.ACTION_SYNC_BEGIN))
                UIHelper.showSyncProcess(activity);
            else if(intent.getAction().equals(SyncAction.ACTION_SYNC_END))
                UIHelper.hideSyncProcess(activity);
        }
    }

    public static void initActivity(final AppCompatActivity activity, boolean isRoot)
    {
        initActionBar(activity);
        initSidebar(activity);

        if(activity.getSupportActionBar() != null) {
            ViewGroup actionBar = (ViewGroup) activity.getSupportActionBar().getCustomView();
            ImageView drawer = (ImageView) actionBar.findViewById(R.id.sidebar_drawer);

            if (isRoot) {
                drawer.setClickable(true);
                drawer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        toggleSidebar(activity);
                    }
                });
                drawer.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_menu_white_24dp));
            } else {
                drawer.setClickable(true);
                drawer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        back(activity);
                    }
                });
                drawer.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp));
            }

            LocalBroadcastManager manager = LocalBroadcastManager.getInstance(activity);

            manager.registerReceiver(new Receiver(activity), new IntentFilter(SyncAction.ACTION_SYNC_BEGIN));
            manager.registerReceiver(new Receiver(activity), new IntentFilter(SyncAction.ACTION_SYNC_END));
        }
    }

    private static void showSyncProcess(AppCompatActivity activity)
    {
        activity.findViewById(R.id.actionbar_sync_progress).setVisibility(View.VISIBLE);
    }

    private static void hideSyncProcess(AppCompatActivity activity)
    {
        activity.findViewById(R.id.actionbar_sync_progress).setVisibility(View.GONE);
    }

    private static void initActionBar(AppCompatActivity activity)
    {
        if(activity.getSupportActionBar() == null)
            return;

        ViewGroup actionBarLayout = (ViewGroup)activity.getLayoutInflater().inflate(R.layout.action_bar, null);

        android.support.v7.app.ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(actionBarLayout, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

    }

    public static void showSearchBar(final AppCompatActivity activity, final SearchBarListener listener, final Menu menu)
    {
        activity.findViewById(R.id.actionbar_search).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.actionbar_title).setVisibility(View.GONE);
        for(int i = 0; i < menu.size(); i++){
            menu.getItem(i).setVisible(false);
        }

        ViewGroup actionBar = (ViewGroup)activity.getSupportActionBar().getCustomView();
        final EditText searchText = (EditText)actionBar.findViewById(R.id.actionbar_search_text);
        /*searchText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                listener.onSearchTextChanged(searchText.getText().toString());
                return false;
            }
        });*/
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                listener.onSearchTextChanged(editable.toString());
            }
        });

        View advancedButton = actionBar.findViewById(R.id.actionbar_search_advanced);
        advancedButton.setClickable(true);
        advancedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAdvancedSearchDialog(activity);
            }
        });

        ImageView helpIcon = (ImageView)actionBar.findViewById(R.id.actionbar_search_help);
        helpIcon.setClickable(true);
        helpIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSearchHelp(activity);
            }
        });
        ImageView drawer = (ImageView)actionBar.findViewById(R.id.sidebar_drawer);
        drawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideSearchBar(activity, listener, menu);
                ViewUtils.hideKeyboard(activity, searchText);
            }
        });
        drawer.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp));
    }

    private static void showSearchHelp(Activity activity)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Ajuda");
        builder.setCancelable(true);
        builder.setMessage("Busque pelo nome, logradouro, bairro, CEP ou pela latitude/longitude\n(exemplo: 10,000000/20,000000)");
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    private static void openAdvancedSearchDialog(final Activity activity)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Buscar pela categoria");

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

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                InventoryCategory category = categories.get(i);
                dialogInterface.dismiss();

                Intent intent = new Intent(activity, AdvancedSearchActivity.class);
                intent.putExtra("categoryId", category.id);
                activity.startActivityForResult(intent, AdvancedSearchActivity.REQUEST_SEARCH);
                activity.overridePendingTransition(R.anim.slide_in_bottom, R.anim.hold);
                //activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        builder.show();
    }

    public static void showProgress(AppCompatActivity activity)
    {
        if(activity.getSupportActionBar() != null)
            activity.getSupportActionBar().getCustomView().findViewById(R.id.actionbar_progress).setVisibility(View.VISIBLE);
    }

    public static void hideProgress(AppCompatActivity activity)
    {
        if(activity.getSupportActionBar() != null)
            activity.getSupportActionBar().getCustomView().findViewById(R.id.actionbar_progress).setVisibility(View.GONE);
    }

    public static void hideSearchBar(final AppCompatActivity activity, final SearchBarListener listener, final Menu menu)
    {
        activity.findViewById(R.id.actionbar_search).setVisibility(View.GONE);
        activity.findViewById(R.id.actionbar_title).setVisibility(View.VISIBLE);
        for(int i = 0; i < menu.size(); i++){
            menu.getItem(i).setVisible(true);

            if(menu.getItem(i).getItemId() == R.id.action_items_list)
                menu.getItem(i).setVisible(false);
        }

        ViewGroup actionBar = (ViewGroup)activity.getSupportActionBar().getCustomView();
        EditText searchText = (EditText)actionBar.findViewById(R.id.actionbar_search_text);
        searchText.setText("");
        listener.onSearchTextChanged("");

        ImageView drawer = (ImageView)actionBar.findViewById(R.id.sidebar_drawer);
        drawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleSidebar(activity);
            }
        });
        drawer.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_menu_white_24dp));
    }

    public static void setTitle(final AppCompatActivity activity, String title)
    {
        ViewGroup actionBar = (ViewGroup)activity.getSupportActionBar().getCustomView();
        TextView textTitle = (TextView)actionBar.findViewById(R.id.actionbar_title);
        textTitle.setText(title);
    }

    public static android.support.v7.widget.PopupMenu initMenu(AppCompatActivity activity)
    {
        ViewGroup actionBar = (ViewGroup)activity.getSupportActionBar().getCustomView();
        View drawer = actionBar.findViewById(R.id.sidebar_drawer);
        View arrow = actionBar.findViewById(R.id.actionbar_title_arrow);
        TextView textTitle = (TextView)actionBar.findViewById(R.id.actionbar_title);

        final android.support.v7.widget.PopupMenu menu = new android.support.v7.widget.PopupMenu(activity, drawer);
        textTitle.setClickable(true);
        textTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.show();
            }
        });
        arrow.setVisibility(View.VISIBLE);

        return menu;
    }

    private static void back(Activity activity)
    {
        activity.finish();
        activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private static void initSidebar(final Activity activity)
    {
        ViewGroup root = (ViewGroup)activity.findViewById(R.id.container);

        ViewGroup sidebar = (ViewGroup)activity.getLayoutInflater().inflate(R.layout.sidebar, null);
        sidebar.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        root.addView(sidebar);

        sidebar.setVisibility(View.GONE);

        Animation animation = new AlphaAnimation(1, .5f);
        animation.setDuration(1);
        animation.setFillAfter(true);

        View overlay = sidebar.findViewById(R.id.sidebar_overlay);
        //overlay.startAnimation(animation);

        TextView txtName = (TextView)sidebar.findViewById(R.id.sidebar_label_name);
        User user = Zup.getInstance().getSessionUser();
        if(user != null) {
            txtName.setText(user.name);
        }

        View cellProfile = sidebar.findViewById(R.id.sidebar_cell_profile);
        View cellReports = sidebar.findViewById(R.id.sidebar_cell_reports);
        View cellDocuments = sidebar.findViewById(R.id.sidebar_cell_documents);
        View cellItems = sidebar.findViewById(R.id.sidebar_cell_items);
        View cellNotifications = sidebar.findViewById(R.id.sidebar_cell_notifications);
        View cellSync = sidebar.findViewById(R.id.sidebar_cell_sync);

        if(activity instanceof ProfileActivity)
        {
            TextView labelName = (TextView)cellProfile.findViewById(R.id.sidebar_label_name);
            TextView labelGroup = (TextView)cellProfile.findViewById(R.id.sidebar_label_group);

            cellProfile.setBackgroundColor(activity.getResources().getColor(R.color.sidebar_selected));
            labelName.setTextColor(0xffffffff);
            labelGroup.setTextColor(0xffffffff);
        }
        else if(activity instanceof CasesActivity)
        {
            TextView labelDocuments = (TextView)cellDocuments.findViewById(R.id.sidebar_label_documents);
            ImageView iconDocuments = (ImageView)cellDocuments.findViewById(R.id.sidebar_icon_documents);

            cellDocuments.setBackgroundColor(activity.getResources().getColor(R.color.sidebar_selected));
            labelDocuments.setTextColor(0xffffffff);
            iconDocuments.setImageDrawable(activity.getResources().getDrawable(R.drawable.sidebar_icon_documentos_branco));
        }
        else if(activity instanceof ItemsActivity)
        {
            TextView labelItems = (TextView)cellItems.findViewById(R.id.sidebar_label_items);
            ImageView iconItems = (ImageView)cellItems.findViewById(R.id.sidebar_icon_items);

            cellItems.setBackgroundColor(activity.getResources().getColor(R.color.sidebar_selected));
            labelItems.setTextColor(0xffffffff);
            iconItems.setImageDrawable(activity.getResources().getDrawable(R.drawable.sidebar_icon_inventario_branco));
        }
        else if(activity instanceof SyncActivity)
        {
            TextView labelSync = (TextView)cellSync.findViewById(R.id.sidebar_label_sync);
            ImageView iconSync = (ImageView)cellSync.findViewById(R.id.sidebar_icon_sync);

            cellSync.setBackgroundColor(activity.getResources().getColor(R.color.sidebar_selected));
            labelSync.setTextColor(0xffffffff);
            iconSync.setImageDrawable(activity.getResources().getDrawable(R.drawable.sidebar_icon_inventario_branco));
        }
        else if(activity instanceof ReportsListActivity)
        {
            TextView labelSync = (TextView)cellReports.findViewById(R.id.sidebar_label_reports);
            ImageView iconSync = (ImageView)cellReports.findViewById(R.id.sidebar_icon_reports);

            cellReports.setBackgroundColor(activity.getResources().getColor(R.color.sidebar_selected));
            labelSync.setTextColor(0xffffffff);
            iconSync.setImageDrawable(activity.getResources().getDrawable(R.drawable.sidebar_icon_documentos_branco));
        }

        cellReports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(activity instanceof ReportsListActivity) {
                    toggleSidebar(activity);
                } else {
                    activity.startActivity(new Intent(activity, ReportsListActivity.class));
                }
            }
        });

        cellProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(activity instanceof ProfileActivity)
                {
                    toggleSidebar(activity);
                }
                else
                {
                    activity.startActivity(new Intent(activity, ProfileActivity.class));
                    //activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            }
        });

        cellDocuments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(activity instanceof CasesActivity)
                {
                    toggleSidebar(activity);
                }
                else
                {
                    activity.startActivity(new Intent(activity, CasesActivity.class));
                    //activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            }
        });

        cellItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(activity instanceof ItemsActivity)
                {
                    toggleSidebar(activity);
                }
                else
                {
                    activity.startActivity(new Intent(activity, ItemsActivity.class));
                    //activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            }
        });

        cellSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(activity instanceof SyncActivity)
                {
                    toggleSidebar(activity);
                }
                else
                {
                    activity.startActivity(new Intent(activity, SyncActivity.class));
                    //activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            }
        });
    }

    private static void toggleSidebar(Activity activity)
    {
        final ViewGroup sidebar = (ViewGroup)activity.findViewById(R.id.sidebar_root);
        View sidebarScroll = sidebar.findViewById(R.id.sidebar_scroll);
        View sidebarOverlay = sidebar.findViewById(R.id.sidebar_overlay);

        if(sidebar.getVisibility() == View.VISIBLE)
        {
            AlphaAnimation alphaAnimation = new AlphaAnimation(.5f, 0);
            alphaAnimation.setDuration(250);
            sidebarOverlay.startAnimation(alphaAnimation);

            TranslateAnimation animation = new TranslateAnimation(0, -sidebarScroll.getWidth(), 0, 0);
            animation.setDuration(250);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    sidebar.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            sidebarScroll.startAnimation(animation);
        }
        else
        {
            TextView textSync = (TextView) sidebar.findViewById(R.id.sidebar_sync_count);
            textSync.setText(Integer.toString(Zup.getInstance().getSyncActionCount()));
            sidebar.setVisibility(View.VISIBLE);

            AlphaAnimation alphaAnimation = new AlphaAnimation(0, .5f);
            alphaAnimation.setDuration(250);
            alphaAnimation.setFillAfter(true);
            sidebarOverlay.startAnimation(alphaAnimation);

            int width = sidebarScroll.getWidth();
            if(width == 0)
            {
                width = (int)((float)300 * activity.getResources().getDisplayMetrics().density);
            }

            TranslateAnimation animation = new TranslateAnimation(-width, 0, 0, 0);
            animation.setDuration(250);
            sidebarScroll.startAnimation(animation);
        }
    }

    public static void killApp(boolean killSafely) {
        if (killSafely) {
            /*
             * Notify the system to finalize and collect all objects of the app
             * on exit so that the virtual machine running the app can be killed
             * by the system without causing issues. NOTE: If this is set to
             * true then the virtual machine will not be killed until all of its
             * threads have closed.
             */
            System.runFinalizersOnExit(true);

            /*
             * Force the system to close the app down completely instead of
             * retaining it in the background. The virtual machine that runs the
             * app will be killed. The app will be completely created as a new
             * app in a new virtual machine running in a new process if the user
             * starts the app again.
             */
            System.exit(0);
        } else {
            /*
             * Alternatively the process that runs the virtual machine could be
             * abruptly killed. This is the quickest way to remove the app from
             * the device but it could cause problems since resources will not
             * be finalized first. For example, all threads running under the
             * process will be abruptly killed when the process is abruptly
             * killed. If one of those threads was making multiple related
             * changes to the database, then it may have committed some of those
             * changes but not all of those changes when it was abruptly killed.
             */
            android.os.Process.killProcess(android.os.Process.myPid());
        }

    }
}
