package com.ntxdev.zuptecnico.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ntxdev.zuptecnico.R;
import com.ntxdev.zuptecnico.entities.InventoryCategory;

/**
 * Created by igorlira on 3/15/15.
 */
public class InventoryItemFilterViewController
{
    public enum FilterType
    {
        Equals,
        GreaterThan,
        LessThan,
        Different,
        Like,
        Between,
        Includes,

    }

    private FilterType type;
    private Activity activity;
    private ViewGroup viewGroup;
    private InventoryCategory.Section.Field field;
    private boolean isFilterEnabled;

    public InventoryItemFilterViewController(ViewGroup viewGroup, InventoryCategory.Section.Field field, Activity activity)
    {
        this.activity = activity;
        this.field = field;
        this.viewGroup = viewGroup;
        this.removeFilter();

        this.init();
        this.setMultipleValues(false);
    }

    public void setValues(Object firstV, Object secondV)
    {
        EditText first = (EditText) findViewById(R.id.inventory_item_filter_first);
        EditText second = (EditText) findViewById(R.id.inventory_item_filter_second);

        first.setTag(firstV);
        second.setTag(secondV);

        if(firstV == null)
            firstV = "";

        if(secondV == null)
            secondV = "";

        first.setText(firstV.toString());
        second.setText(secondV.toString());
    }

    public Object[] getValues()
    {
        EditText first = (EditText) findViewById(R.id.inventory_item_filter_first);
        EditText second = (EditText) findViewById(R.id.inventory_item_filter_second);

        Object firstV, secondV;

        if(isDecimal())
        {
            if(first.getText().length() > 0)
                firstV = Double.parseDouble(first.getText().toString());
            else
                firstV = null;

            if(second.getText().length() > 0)
                secondV = Double.parseDouble(second.getText().toString());
            else
                secondV = null;
        }
        else if(isNumeric())
        {
            if(first.getText().length() > 0)
                firstV = Integer.parseInt(first.getText().toString());
            else
                firstV = null;

            if(second.getText().length() > 0)
                secondV = Integer.parseInt(second.getText().toString());
            else
                secondV = null;
        }
        else
        {
            firstV = first.getText().toString();
            secondV = second.getText().toString();
        }

        return new Object[] { firstV, secondV };
    }

    public void setEnabled(boolean enabled)
    {
        this.isFilterEnabled = enabled;

        if(enabled)
            this.addFilter();
        else
            this.removeFilter();
    }

    public boolean isEnabled()
    {
        return this.isFilterEnabled;
    }

    void init()
    {
        this.type = FilterType.Equals;

        TextView textView = (TextView) findViewById(R.id.inventory_item_text_name);
        textView.setText(this.field.label.toUpperCase());

        findViewById(R.id.inventory_item_filter_add_button).setClickable(true);
        findViewById(R.id.inventory_item_filter_add_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addFilter();
            }
        });

        findViewById(R.id.inventory_item_filter_remove).setClickable(true);
        findViewById(R.id.inventory_item_filter_remove).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeFilter();
            }
        });

        findViewById(R.id.inventory_item_filter_type_button).setClickable(true);
        findViewById(R.id.inventory_item_filter_type_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createTypeDialog();
            }
        });

        this.setExtra();
        this.setNumeric(isNumeric(), isDecimal());
    }

    void setExtra()
    {
        TextView textView = (TextView) findViewById(R.id.inventory_item_filter_extra);

        String pkgName = this.getClass().getPackage().getName();
        int resId = activity.getResources().getIdentifier("inventory_item_extra_" + field.kind, "string", pkgName);
        if(resId != 0)
        {
            textView.setVisibility(View.VISIBLE);
            textView.setText(activity.getResources().getText(resId));
        }
        else
            textView.setVisibility(View.GONE);
    }

    String getTypeName(FilterType type)
    {
        switch (type)
        {
            case Equals:
                return "Igual a";

            case Different:
                return "Diferente de";

            case Between:
                return "Entre";

            case GreaterThan:
                return "Maior que";

            case LessThan:
                return "Menor que";

            case Like:
                return "Parecido com";

            case Includes:
                return "Inclui";
        }

        return null;
    }

    boolean isNumeric()
    {
        if(field.kind.equals("integer") || field.kind.equals("decimal") || field.kind.equals("meters")
                || field.kind.equals("centimeters") || field.kind.equals("kilometers")
                || field.kind.equals("years") || field.kind.equals("months")
                || field.kind.equals("days") || field.kind.equals("hours")
                || field.kind.equals("seconds") || field.kind.equals("angle"))
            return true;

        return false;
    }

    boolean isDecimal()
    {
        if(field.kind.equals("decimal"))
            return true;

        return false;
    }

    FilterType[] getAvailableTypes()
    {
        if(field.kind.equals("text"))
        {
            return new FilterType[] { FilterType.Equals, FilterType.Different };
        }
        else if(field.kind.equals("integer") || field.kind.equals("decimal") || field.kind.equals("meters")
                || field.kind.equals("centimeters") || field.kind.equals("kilometers")
                || field.kind.equals("years") || field.kind.equals("months")
                || field.kind.equals("days") || field.kind.equals("hours")
                || field.kind.equals("seconds") || field.kind.equals("angle"))
        {
            return new FilterType[]
                    { FilterType.Equals, FilterType.Different, FilterType.GreaterThan,
                    FilterType.LessThan, FilterType.Between };
        }

        return new FilterType[] { FilterType.Equals, FilterType.Different };
    }

    public InventoryCategory.Section.Field getField()
    {
        return field;
    }

    public FilterType getType()
    {
        return type;
    }

    public void setType(FilterType type)
    {
        this.type = type;

        TextView tv = (TextView) findViewById(R.id.inventory_item_filter_type_button);
        tv.setText(getTypeName(type));

        switch (type)
        {
            case Between:
                setMultipleValues(true);
                break;

            default:
                setMultipleValues(false);
                break;
        }
    }

    void createTypeDialog()
    {
        FilterType[] types = getAvailableTypes();

        AlertDialog.Builder builder = new AlertDialog.Builder(this.viewGroup.getContext());
        builder.setTitle("Escolher tipo de filtro");

        View dialogView = this.activity.getLayoutInflater().inflate(R.layout.dialog_select_items, null);
        EditText input = (EditText) dialogView.findViewById(R.id.dialog_select_items_search);

        input.setVisibility(View.GONE);

        builder.setView(dialogView);
        final AlertDialog dialog = builder.show();

        ViewGroup container = (ViewGroup) dialogView.findViewById(R.id.dialog_select_items_container);

        for(FilterType type : types)
        {
            View separator = new View(this.viewGroup.getContext());
            separator.setBackgroundColor(0xffcccccc);
            separator.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));


            TextView itemView = new TextView(this.viewGroup.getContext());
            itemView.setClickable(true);
            itemView.setText(getTypeName(type));
            itemView.setBackgroundResource(R.drawable.sidebar_cell);
            itemView.setPadding(20, 20, 20, 20);
            itemView.setTag(type);

            container.addView(separator);
            container.addView(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setType((FilterType)view.getTag());
                    dialog.dismiss();
                }
            });
        }
    }

    void setNumeric(boolean numeric, boolean decimal)
    {
        EditText first = (EditText) findViewById(R.id.inventory_item_filter_first);
        EditText second = (EditText) findViewById(R.id.inventory_item_filter_second);

        int type = 0;
        if(numeric)
            type |= InputType.TYPE_CLASS_NUMBER;
        else
            type |= InputType.TYPE_CLASS_TEXT;

        if(decimal)
            type |= InputType.TYPE_NUMBER_FLAG_DECIMAL;

        first.setInputType(type);
        second.setInputType(type);
    }

    void setMultipleValues(boolean multipleValues)
    {
        EditText first = (EditText) findViewById(R.id.inventory_item_filter_first);
        EditText second = (EditText) findViewById(R.id.inventory_item_filter_second);
        TextView between = (TextView) findViewById(R.id.inventory_item_filter_between);

        if(multipleValues)
        {
            first.setVisibility(View.VISIBLE);
            second.setVisibility(View.VISIBLE);
            between.setVisibility(View.VISIBLE);

            first.setLayoutParams(new LinearLayout.LayoutParams(100, ViewGroup.LayoutParams.WRAP_CONTENT));
            second.setLayoutParams(new LinearLayout.LayoutParams(100, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        else
        {
            first.setVisibility(View.VISIBLE);
            second.setVisibility(View.GONE);
            between.setVisibility(View.GONE);

            first.setLayoutParams(new LinearLayout.LayoutParams(230, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
    }

    void addFilter()
    {
        isFilterEnabled = true;
        findViewById(R.id.inventory_item_filter_add_button).setVisibility(View.GONE);
        findViewById(R.id.inventory_item_filter_fields).setVisibility(View.VISIBLE);
    }

    void removeFilter()
    {
        isFilterEnabled = false;
        findViewById(R.id.inventory_item_filter_add_button).setVisibility(View.VISIBLE);
        findViewById(R.id.inventory_item_filter_fields).setVisibility(View.GONE);
    }

    View findViewById(int id)
    {
        return viewGroup.findViewById(id);
    }
}
