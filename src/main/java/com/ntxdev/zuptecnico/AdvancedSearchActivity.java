package com.ntxdev.zuptecnico;

import android.support.v7.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.entities.InventoryCategory;
import com.ntxdev.zuptecnico.entities.InventoryCategoryStatus;
import com.ntxdev.zuptecnico.entities.InventoryItemFilter;
import com.ntxdev.zuptecnico.ui.InventoryItemFilterViewController;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Iterator;

public class AdvancedSearchActivity extends AppCompatActivity
        implements DatePickerDialog.OnDateSetListener
{
    public static final int REQUEST_SEARCH = 1;
    public static final int RESULT_SEARCH = 1;

    private int categoryId;

    private ArrayList<InventoryItemFilterViewController> filters;

    public void onCreate(Bundle savedInstance)
    {
        super.onCreate(savedInstance);
        this.setContentView(R.layout.activity_inventory_items_advancedsearch);

        Zup.getInstance().initStorage(getApplicationContext());

        Intent intent = getIntent();
        int categoryId = intent.getIntExtra("categoryId", -1);
        if(categoryId == -1)
            return;

        this.categoryId = categoryId;

        filters = new ArrayList<InventoryItemFilterViewController>();

        ViewGroup statusesContainer = (ViewGroup)findViewById(R.id.advancedsearch_status_container);
        Iterator<InventoryCategoryStatus> statuses = Zup.getInstance().getInventoryCategoryStatusIterator(categoryId);
        while(statuses.hasNext())
        {
            InventoryCategoryStatus status = statuses.next();

            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(status.title);
            checkBox.setTag(status);
            statusesContainer.addView(checkBox);
        }

        View.OnClickListener pickDateOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickDate((ViewGroup)view, (Calendar)view.getTag());
            }
        };
        findViewById(R.id.creation_date_from_field).setOnClickListener(pickDateOnClickListener);
        findViewById(R.id.creation_date_to_field).setOnClickListener(pickDateOnClickListener);
        findViewById(R.id.modification_date_from_field).setOnClickListener(pickDateOnClickListener);
        findViewById(R.id.modification_date_to_field).setOnClickListener(pickDateOnClickListener);

        loadPreviousData();

        buildPage();
    }

    void loadPreviousData()
    {
        ViewGroup statusesContainer = (ViewGroup)findViewById(R.id.advancedsearch_status_container);
        if(getIntent().hasExtra("search_data"))
        {
            Intent searchData = (Intent)getIntent().getExtras().get("search_data");
            if(searchData.hasExtra("name"))
                ((EditText)findViewById(R.id.advancedsearch_name)).setText(searchData.getStringExtra("name"));
            if(searchData.hasExtra("creation_from"))
            {
                ViewGroup container = (ViewGroup)findViewById(R.id.creation_date_from_field);
                Calendar date = (Calendar)searchData.getExtras().get("creation_from");
                container.setTag(date);
                findFirstChildOfType(container, TextView.class).setText(Zup.getInstance().getDateFormat().format(date.getTime()));
            }
            if(searchData.hasExtra("creation_to"))
            {
                ViewGroup container = (ViewGroup)findViewById(R.id.creation_date_to_field);
                Calendar date = (Calendar)searchData.getExtras().get("creation_to");
                container.setTag(date);
                findFirstChildOfType(container, TextView.class).setText(Zup.getInstance().getDateFormat().format(date.getTime()));
            }
            if(searchData.hasExtra("modification_from"))
            {
                ViewGroup container = (ViewGroup)findViewById(R.id.modification_date_from_field);
                Calendar date = (Calendar)searchData.getExtras().get("modification_from");
                container.setTag(date);
                findFirstChildOfType(container, TextView.class).setText(Zup.getInstance().getDateFormat().format(date.getTime()));
            }
            if(searchData.hasExtra("modification_to"))
            {
                ViewGroup container = (ViewGroup)findViewById(R.id.modification_date_to_field);
                Calendar date = (Calendar)searchData.getExtras().get("modification_to");
                container.setTag(date);
                findFirstChildOfType(container, TextView.class).setText(Zup.getInstance().getDateFormat().format(date.getTime()));
            }
            if(searchData.hasExtra("statuses"))
            {
                Object[] search_statuses = (Object[])searchData.getExtras().get("statuses");
                ArrayList<Integer> statuses = new ArrayList<Integer>();
                for(Object statusId : search_statuses)
                {
                    statuses.add((Integer)statusId);
                }
                for(int i = 0; i < statusesContainer.getChildCount(); i++)
                {
                    if(!(statusesContainer.getChildAt(i) instanceof CheckBox))
                        continue;

                    CheckBox checkBox = (CheckBox)statusesContainer.getChildAt(i);
                    InventoryCategoryStatus status = (InventoryCategoryStatus)checkBox.getTag();
                    if(statuses.contains(status.id))
                        checkBox.setChecked(true);
                }
            }
            if(searchData.hasExtra("latitude"))
                ((EditText)findViewById(R.id.advancedsearch_latitude)).setText(searchData.getStringExtra("latitude"));
            if(searchData.hasExtra("longitude"))
                ((EditText)findViewById(R.id.advancedsearch_longitude)).setText(searchData.getStringExtra("longitude"));
            if(searchData.hasExtra("address"))
                ((EditText)findViewById(R.id.advancedsearch_address)).setText(searchData.getStringExtra("address"));
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
            filters.clear();

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
        }
    }

    void buildPage()
    {
        ViewGroup container = (ViewGroup)findViewById(R.id.items_search_container);

        PageBuilder builder = new PageBuilder(container);
        builder.execute();
    }

    private View[] buildPageB()
    {
        //ViewGroup container = (ViewGroup)findViewById(R.id.items_search_container);
        InventoryCategory category = Zup.getInstance().getInventoryCategory(this.categoryId);

        ArrayList<View> result = new ArrayList<View>();

        //container.removeAllViews();
        //filters.clear();

        if(category.sections != null)
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

            for(InventoryCategory.Section section : category.sections)
            {
                for(InventoryCategory.Section.Field field : section.fields)
                {
                    ViewGroup vg = (ViewGroup) getLayoutInflater().inflate(R.layout.inventory_item_item_filter, null);
                    //container.addView(vg);
                    result.add(vg);

                    InventoryItemFilterViewController vc = new InventoryItemFilterViewController(vg, field, this);
                    filters.add(vc);

                    if(getIntent().hasExtra("search_data"))
                    {
                        Intent searchData = (Intent) getIntent().getExtras().get("search_data");
                        if (searchData.hasExtra("filter_" + field.id + "_type"))
                        {
                            Object first = searchData.getExtras().get("filter_" + field.id + "_first");
                            Object second = searchData.getExtras().get("filter_" + field.id + "_second");

                            vc.setType((InventoryItemFilterViewController.FilterType) searchData.getExtras().get("filter_" + field.id + "_type"));
                            vc.setValues(first, second);
                            vc.setEnabled(true);
                        }
                        else
                        {
                            vc.setEnabled(false);
                        }
                    }
                }
            }
        }

        View[] resultarr = new View[result.size()];
        result.toArray(resultarr);

        return resultarr;
    }

    private void pickDate(ViewGroup fieldContainer, Calendar date)
    {
        if(date == null)
            date = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(this, this, date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH));
        dialog.getDatePicker().setTag(R.id.tag_item_id, fieldContainer.getId());

        dialog.show();
    }

    public void cancel(View view)
    {
        finish();
        overridePendingTransition(R.anim.hold, R.anim.slide_out_bottom);
    }

    public void search(View view)
    {
        Calendar creation_from = (Calendar)findViewById(R.id.creation_date_from_field).getTag();
        Calendar creation_to = (Calendar)findViewById(R.id.creation_date_to_field).getTag();

        Calendar modification_from = (Calendar)findViewById(R.id.modification_date_from_field).getTag();
        Calendar modification_to = (Calendar)findViewById(R.id.modification_date_to_field).getTag();

        if(creation_from != null && creation_to != null && creation_from.getTime().compareTo(creation_to.getTime()) > 0) // from > to
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Erro");
            builder.setMessage("O campo da data de criação 'a partir de' deve ser menor que o campo 'até'");
            builder.show();
            return;
        }
        else if(modification_from != null && modification_to != null && modification_from.getTime().compareTo(modification_to.getTime()) > 0)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Erro");
            builder.setMessage("O campo da data de modificação 'a partir de' deve ser menor que o campo 'até'");
            builder.show();
            return;
        }

        ArrayList<Integer> raw_statuses = new ArrayList<Integer>();

        ViewGroup statusesContainer = (ViewGroup)findViewById(R.id.advancedsearch_status_container);
        for(int i = 0; i < statusesContainer.getChildCount(); i++)
        {
            if(!(statusesContainer.getChildAt(i) instanceof CheckBox))
                continue;

            CheckBox checkBox = (CheckBox)statusesContainer.getChildAt(i);
            if(checkBox.isChecked())
            {
                InventoryCategoryStatus status = (InventoryCategoryStatus)checkBox.getTag();
                if(status != null)
                    raw_statuses.add(status.id);
            }
        }

        String raw_name = ((EditText)findViewById(R.id.advancedsearch_name)).getText().toString();
        String raw_latitude = ((EditText)findViewById(R.id.advancedsearch_latitude)).getText().toString();
        String raw_longitude = ((EditText)findViewById(R.id.advancedsearch_longitude)).getText().toString();
        String raw_address = ((EditText)findViewById(R.id.advancedsearch_address)).getText().toString();

        Intent intent = new Intent();
        if(!raw_name.equals(""))
            intent.putExtra("name", raw_name);
        if(creation_from != null)
            intent.putExtra("creation_from", creation_from);
        if(creation_to != null)
            intent.putExtra("creation_to", creation_to);
        if(modification_from != null)
            intent.putExtra("modification_from", modification_from);
        if(modification_to != null)
            intent.putExtra("modification_to", modification_to);
        if(!raw_latitude.equals(""))
            intent.putExtra("latitude", raw_latitude);
        if(!raw_longitude.equals(""))
            intent.putExtra("longitude", raw_longitude);
        if(!raw_address.equals(""))
            intent.putExtra("address", raw_address);
        if(raw_statuses.size() > 0)
            intent.putExtra("statuses", (Integer[])raw_statuses.toArray(new Integer[0]));

        int i = 0;
        for(InventoryItemFilterViewController filter : filters)
        {
            if(filter.isEnabled())
            {
                Object[] values = filter.getValues();

                InventoryItemFilter f = new InventoryItemFilter();
                f.fieldId = filter.getField().id;
                f.type = filter.getTypeString();
                f.value1 = (Serializable) values[0];
                f.value2 = (Serializable) values[1];
                f.isArray = filter.isArray();

                intent.putExtra("filter" + (i++), f);

                intent.putExtra("filter_" + filter.getField().id + "_type", filter.getType());
                intent.putExtra("filter_" + filter.getField().id + "_first", (Serializable) values[0]);
                intent.putExtra("filter_" + filter.getField().id + "_second", (Serializable) values[1]);
            }
        }

        intent.putExtra("categoryId", this.categoryId);
        this.setResult(RESULT_SEARCH, intent);
        finish();
        overridePendingTransition(R.anim.hold, R.anim.slide_out_bottom);
    }

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i2, int i3)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, i);
        calendar.set(Calendar.MONTH, i2);
        calendar.set(Calendar.DAY_OF_MONTH, i3);

        ViewGroup fieldContainer = (ViewGroup)findViewById((Integer)datePicker.getTag(R.id.tag_item_id));
        fieldContainer.setTag(calendar);

        TextView textView = findFirstChildOfType(fieldContainer, TextView.class);
        textView.setText(Zup.getInstance().getDateFormat().format(calendar.getTime()));
    }

    private <T> T findFirstChildOfType(ViewGroup container, Class<T> objectClass)
    {
        for(int i = 0; i < container.getChildCount(); i++)
        {
            View child = container.getChildAt(i);
            if(objectClass.isInstance(child))
                return (T)child;
        }

        return null;
    }
}
