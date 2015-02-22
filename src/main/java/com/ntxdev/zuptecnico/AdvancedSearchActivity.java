package com.ntxdev.zuptecnico;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.entities.InventoryCategoryStatus;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

/**
 * Created by igorlira on 4/27/14.
 */
public class AdvancedSearchActivity extends ActionBarActivity implements DatePickerDialog.OnDateSetListener
{
    public static final int REQUEST_SEARCH = 1;
    public static final int RESULT_SEARCH = 1;

    private int _categoryId;

    public void onCreate(Bundle savedInstance)
    {
        super.onCreate(savedInstance);
        this.setContentView(R.layout.activity_inventory_items_advancedsearch);
        getSupportActionBar().hide();

        Zup.getInstance().initStorage(getApplicationContext());

        Intent intent = getIntent();
        int categoryId = intent.getIntExtra("category_id", -1);
        if(categoryId == -1)
            return;

        _categoryId = categoryId;

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
                Object[] srch_statuses = (Object[])searchData.getExtras().get("statuses");
                ArrayList<Integer> sstatuses = new ArrayList<Integer>();
                for(int i = 0; i < srch_statuses.length; i++)
                {
                    sstatuses.add((Integer)srch_statuses[i]);
                }
                for(int i = 0; i < statusesContainer.getChildCount(); i++)
                {
                    if(!(statusesContainer.getChildAt(i) instanceof CheckBox))
                        continue;

                    CheckBox checkBox = (CheckBox)statusesContainer.getChildAt(i);
                    InventoryCategoryStatus status = (InventoryCategoryStatus)checkBox.getTag();
                    if(sstatuses.contains(status.id))
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

        intent.putExtra("category_id", _categoryId);
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
            if(child.getClass() == objectClass)
                return (T)child;
        }

        return null;
    }
}
