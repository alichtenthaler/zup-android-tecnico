package com.ntxdev.zuptecnico.util;

import android.content.Context;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ntxdev.zuptecnico.R;
import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.entities.InventoryCategory;
import com.ntxdev.zuptecnico.entities.InventoryCategoryStatus;
import com.ntxdev.zuptecnico.entities.InventoryItem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import br.com.rezende.mascaras.Mask;

public class FieldUtils
{
    public static ArrayList<View> createRadiosForCategoryStatuses(ViewGroup parent, Context context, LayoutInflater layoutInflater, InventoryCategory category, boolean createMode, InventoryItem item)
    {
        ArrayList<View> result = new ArrayList<View>();

        Iterator<InventoryCategoryStatus> statuses = Zup.getInstance().getInventoryCategoryStatusIterator(category.id);
        if(statuses.hasNext())
        {
            ViewGroup sectionHeader = (ViewGroup) layoutInflater.inflate(R.layout.inventory_item_section_header, parent, false);

            TextView sectionTitle = (TextView) sectionHeader.findViewById(R.id.inventory_item_section_title);
            sectionTitle.setText("ESTADO");
            //container.addView(sectionHeader);
            result.add(sectionHeader);

            RadioGroup statusesContainer = new RadioGroup(context);
            statusesContainer.setOrientation(LinearLayout.VERTICAL);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(30, 10, 20, 5);
            statusesContainer.setLayoutParams(params);

            while(statuses.hasNext())
            {
                InventoryCategoryStatus status = statuses.next();

                RadioButton checkBox = new RadioButton(context);
                checkBox.setText(status.title);
                checkBox.setTag(status);
                if(Utilities.isTablet(context))
                    checkBox.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
                else
                    checkBox.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                statusesContainer.addView(checkBox);

                if(!createMode && item.inventory_status_id != null && item.inventory_status_id == status.id)
                    statusesContainer.check(checkBox.getId());
            }

            statusesContainer.setTag(R.id.inventory_item_create_is_status_field, true);
            //container.addView(statusesContainer);
            result.add(statusesContainer);
        }

        return result;
    }

    public static View createSectionHeader(ViewGroup parent, LayoutInflater inflater, InventoryCategory.Section section)
    {
        ViewGroup sectionHeader = (ViewGroup) inflater.inflate(R.layout.inventory_item_section_header, parent, false);

        TextView sectionTitle = (TextView) sectionHeader.findViewById(R.id.inventory_item_section_title);
        sectionTitle.setText(section.title.toUpperCase());

        return sectionHeader;
    }

    public static View createRadiosForField(ViewGroup parent, ObjectMapper mapper, String label, Context context, LayoutInflater inflater, InventoryCategory.Section.Field field, boolean createMode, InventoryItem item)
    {
        ViewGroup fieldView = (ViewGroup) inflater.inflate(R.layout.inventory_item_item_radio_edit, parent, false);
        fieldView.setTag(R.id.inventory_item_create_fieldid, field.id);

        TextView fieldTitle = (TextView) fieldView.findViewById(R.id.inventory_item_text_name);
        fieldTitle.setText(label);

        ViewGroup radiocontainer = (ViewGroup)fieldView.findViewById(R.id.inventory_item_radio_container);
        if(field.field_options != null)
        {
            RadioGroup group = new RadioGroup(context);
            group.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            for(int x = 0; x < field.field_options.length; x++)
            {
                InventoryCategory.Section.Field.Option option = field.field_options[x];

                RadioButton button = new RadioButton(context);
                button.setText(option.value);
                button.setTag(R.id.tag_button_value, option.id);
                if(Utilities.isTablet(context))
                    button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
                else
                    button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);

                button.setMinimumHeight(60);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                //params.setMargins(15, 0, 0, 0);
                button.setLayoutParams(params);

                group.addView(button);

                Integer[] selected = null;
                if(!createMode)
                {
                    Object raw = item.getFieldValue(field.id);
                    if(raw instanceof List<?>) {
                        selected = mapper.convertValue(raw, Integer[].class);
                    } else if(raw != null && raw instanceof Number) {
                        selected = new Integer[] { mapper.convertValue(raw, Integer.class) };
                    }
                }

                if(!createMode && selected != null && Utilities.arrayContains(selected, option.id))
                    button.setChecked(true);
            }
            radiocontainer.addView(group);
        }
        return fieldView;
    }

    public static View createCheckboxesForField(ViewGroup parent, ObjectMapper mapper, String label, Context context, LayoutInflater inflater, InventoryCategory.Section.Field field, boolean createMode, InventoryItem item)
    {
        ViewGroup fieldView = (ViewGroup) inflater.inflate(R.layout.inventory_item_item_radio_edit, parent, false);
        fieldView.setTag(R.id.inventory_item_create_fieldid, field.id);

        TextView fieldTitle = (TextView) fieldView.findViewById(R.id.inventory_item_text_name);
        fieldTitle.setText(label);

        ViewGroup radiocontainer = (ViewGroup)fieldView.findViewById(R.id.inventory_item_radio_container);
        if(field.field_options != null)
        {
            for(int x = 0; x < field.field_options.length; x++)
            {
                InventoryCategory.Section.Field.Option option = field.field_options[x];

                CheckBox button = new CheckBox(context);//(RadioButton)radioElement.findViewById(R.id.inventory_item_item_radio_radio);
                button.setText(option.value);
                button.setTag(R.id.tag_button_value, option.id);
                if(Utilities.isTablet(context))
                    button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
                else
                    button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);

                button.setMinimumHeight(60);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                //params.setMargins(15, 0, 0, 0);
                button.setLayoutParams(params);

                Integer[] selected = null;
                if(!createMode)
                {
                    Object raw = item.getFieldValue(field.id);
                    if(raw instanceof List<?>) {
                        selected = mapper.convertValue(raw, Integer[].class);
                    } else if(raw != null && raw instanceof Number) {
                        selected = new Integer[] { mapper.convertValue(raw, Integer.class) };
                    }
                }

                radiocontainer.addView(button);

                if(!createMode && selected != null && Utilities.arrayContains(selected, option.id))
                    button.setChecked(true);
            }
        }
        return fieldView;
    }

    public static View createImagesField(ViewGroup parent, String label, InventoryCategory.Section.Field field, LayoutInflater inflater, View.OnClickListener addButtonListener)
    {
        ViewGroup fieldView = (ViewGroup) inflater.inflate(R.layout.inventory_item_item_images_edit, parent, false);
        fieldView.setTag(R.id.inventory_item_create_fieldid, field.id);

        TextView fieldTitle = (TextView) fieldView.findViewById(R.id.inventory_item_text_name);
        fieldTitle.setText(label);

        Button addButton = (Button) fieldView.findViewById(R.id.inventory_item_images_button);
        addButton.setOnClickListener(addButtonListener);

        return fieldView;
    }

    public static View createNumberField(ViewGroup parent, Context context, String label, InventoryCategory.Section.Field field, LayoutInflater inflater, boolean createMode, InventoryItem item)
    {
        ViewGroup fieldView = (ViewGroup) inflater.inflate(R.layout.inventory_item_item_text_edit, parent, false);
        fieldView.setTag(R.id.inventory_item_create_fieldid, field.id);

        TextView fieldTitle = (TextView) fieldView.findViewById(R.id.inventory_item_text_name);
        final EditText fieldValue = (EditText) fieldView.findViewById(R.id.inventory_item_text_value);
        TextView fieldExtra = (TextView) fieldView.findViewById(R.id.inventory_item_text_extra);

        fieldView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fieldValue.requestFocus();
            }
        });

        int flags = InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED;
        if(field.kind.equals("decimal") || field.kind.equals("meters") || field.kind.equals("centimeters") || field.kind.equals("kilometers") || field.kind.equals("angle"))
        {
            flags |= InputType.TYPE_NUMBER_FLAG_DECIMAL;
        }

        fieldTitle.setText(label);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) fieldValue.getLayoutParams();
        params.width = 100;
        fieldValue.setLayoutParams(params);
        //fieldValue.setLayoutParams(new LinearLayout.LayoutParams(100, ViewGroup.LayoutParams.WRAP_CONTENT));
        fieldValue.setInputType(flags);

        String pkgName = context.getClass().getPackage().getName();
        int resId = context.getResources().getIdentifier("inventory_item_extra_" + field.kind, "string", pkgName);
        if(resId != 0)
        {
            fieldExtra.setVisibility(View.VISIBLE);
            fieldExtra.setText(context.getResources().getText(resId));
        }

        if(!createMode && item.getFieldValue(field.id) != null)
            fieldValue.setText(item.getFieldValue(field.id).toString());

        return fieldView;
    }

    public static View createSelectField(ViewGroup parent, ObjectMapper mapper, String label, InventoryCategory.Section.Field field, LayoutInflater inflater, boolean createMode, InventoryItem item, final View.OnClickListener listener)
    {
        final ViewGroup fieldView = (ViewGroup) inflater.inflate(R.layout.inventory_item_item_select_edit, parent, false);
        fieldView.setTag(R.id.inventory_item_create_fieldid, field.id);

        TextView fieldTitle = (TextView) fieldView.findViewById(R.id.inventory_item_text_name);
        final TextView fieldValue = (TextView) fieldView.findViewById(R.id.inventory_item_text_value);

        fieldTitle.setText(label);

        if(!createMode && item.getFieldValue(field.id) != null) {
            ArrayList selected = mapper.convertValue(item.getFieldValue(field.id), ArrayList.class);
            if(selected.size() > 0)
            {
                Integer id = mapper.convertValue(selected.get(0), Integer.class);
                InventoryCategory.Section.Field.Option option = field.getOption(id);

                if(option != null) {
                    fieldValue.setText(option.value);
                    fieldValue.setTag(id);
                } else {
                    fieldValue.setText("Valor inválido");
                }
            }
            else
            {
                fieldValue.setText("Escolha uma opção...");
            }
            //fieldValue.setText(item.getFieldValue(field.id).toString());
            //fieldValue.setTag(item.getFieldValue(field.id).toString());
        }
        else
            fieldValue.setText("Escolha uma opção...");

        fieldValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listener != null)
                    listener.onClick(fieldView);
            }
        });
        fieldView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listener != null)
                    listener.onClick(fieldView);
            }
        });

        return fieldView;
    }

    public static View createDateField(ViewGroup parent, String label, InventoryCategory.Section.Field field, LayoutInflater inflater, boolean createMode, InventoryItem item, final View.OnClickListener listener)
    {
        final ViewGroup fieldView = (ViewGroup) inflater.inflate(R.layout.inventory_item_item_select_edit, parent, false);
        fieldView.setTag(R.id.inventory_item_create_fieldid, field.id);

        TextView fieldTitle = (TextView) fieldView.findViewById(R.id.inventory_item_text_name);
        TextView fieldValue = (TextView) fieldView.findViewById(R.id.inventory_item_text_value);

        fieldTitle.setText(label);

        if(!createMode && item.getFieldValue(field.id) != null) {
            fieldValue.setText(item.getFieldValue(field.id).toString());
            fieldValue.setTag(item.getFieldValue(field.id).toString());
        }
        else
            fieldValue.setText("Escolha uma data...");

        fieldValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listener != null)
                    listener.onClick(fieldView);
            }
        });
        fieldView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listener != null)
                    listener.onClick(fieldView);
            }
        });

        return fieldView;
    }

    public static View createTimeField(ViewGroup parent, String label, InventoryCategory.Section.Field field, LayoutInflater inflater, boolean createMode, InventoryItem item, final View.OnClickListener listener)
    {
        final ViewGroup fieldView = (ViewGroup) inflater.inflate(R.layout.inventory_item_item_select_edit, parent, false);
        fieldView.setTag(R.id.inventory_item_create_fieldid, field.id);

        TextView fieldTitle = (TextView) fieldView.findViewById(R.id.inventory_item_text_name);
        final TextView fieldValue = (TextView) fieldView.findViewById(R.id.inventory_item_text_value);

        fieldTitle.setText(label);

        if(!createMode && item.getFieldValue(field.id) != null) {
            fieldValue.setText(item.getFieldValue(field.id).toString());
            fieldValue.setTag(item.getFieldValue(field.id).toString());
        }
        else
            fieldValue.setText("Escolha um tempo...");

        fieldValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listener != null)
                    listener.onClick(fieldView);
            }
        });
        fieldView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listener != null)
                    listener.onClick(fieldView);
            }
        });

        return fieldView;
    }

    public static View createCPForCNPJField(ViewGroup parent, String label, InventoryCategory.Section.Field field, LayoutInflater inflater, boolean createMode, InventoryItem item)
    {
        ViewGroup fieldView = (ViewGroup) inflater.inflate(R.layout.inventory_item_item_text_edit, parent, false);
        fieldView.setTag(R.id.inventory_item_create_fieldid, field.id);

        TextView fieldTitle = (TextView) fieldView.findViewById(R.id.inventory_item_text_name);
        final TextView fieldValue = (TextView) fieldView.findViewById(R.id.inventory_item_text_value);

        fieldValue.setInputType(InputType.TYPE_CLASS_NUMBER);
        if(field.kind.equals("cpf"))
            fieldValue.addTextChangedListener(Mask.insert("###.###.###-##", (EditText) fieldValue));
        else if(field.kind.equals("cnpj"))
            fieldValue.addTextChangedListener(Mask.insert("##.###.###/####-##", (EditText)fieldValue));

        fieldTitle.setText(label);
        if(!createMode && item.getFieldValue(field.id) != null)
            fieldValue.setText(item.getFieldValue(field.id).toString());

        fieldView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fieldValue.requestFocus();
            }
        });

        return fieldView;
    }

    public static View createURLorEmailField(ViewGroup parent, String label, InventoryCategory.Section.Field field, LayoutInflater inflater, boolean createMode, InventoryItem item)
    {
        ViewGroup fieldView = (ViewGroup) inflater.inflate(R.layout.inventory_item_item_text_edit, parent, false);
        fieldView.setTag(R.id.inventory_item_create_fieldid, field.id);

        TextView fieldTitle = (TextView) fieldView.findViewById(R.id.inventory_item_text_name);
        final TextView fieldValue = (TextView) fieldView.findViewById(R.id.inventory_item_text_value);

        fieldTitle.setText(label);
        if(!createMode && item.getFieldValue(field.id) != null)
            fieldValue.setText(item.getFieldValue(field.id).toString());

        fieldView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fieldValue.requestFocus();
            }
        });

        return fieldView;
    }

    public static View createTextField(ViewGroup parent, Context context, String label, InventoryCategory.Section.Field field, LayoutInflater inflater, boolean createMode, InventoryItem item)
    {
        ViewGroup fieldView = (ViewGroup) inflater.inflate(R.layout.inventory_item_item_text_edit, parent, false);
        fieldView.setTag(R.id.inventory_item_create_fieldid, field.id);

        TextView fieldTitle = (TextView) fieldView.findViewById(R.id.inventory_item_text_name);
        final TextView fieldValue = (TextView) fieldView.findViewById(R.id.inventory_item_text_value);

        if(field.kind != null && !field.kind.equals("text") && !field.kind.equals("textarea")) {
            label += " (Unknown field kind: " + field.kind + ")";
            fieldValue.setEnabled(false);
        }

        if(field.kind != null && field.kind.equals("textarea")) {
            EditText editText = (EditText) fieldValue;
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            editText.setLines(3);
            editText.setGravity(Gravity.TOP | Gravity.START);
            editText.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 100));
        }

        fieldTitle.setText(label);
        if(!createMode && item.getFieldValue(field.id) != null)
            fieldValue.setText(item.getFieldValue(field.id).toString());

        fieldView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fieldValue.requestFocus();
            }
        });

        return fieldView;
    }
}
