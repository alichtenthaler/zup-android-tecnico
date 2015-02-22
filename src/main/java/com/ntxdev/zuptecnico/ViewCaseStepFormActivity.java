package com.ntxdev.zuptecnico;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.entities.Case;
import com.ntxdev.zuptecnico.entities.Flow;
import com.ntxdev.zuptecnico.ui.UIHelper;

import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

/**
 * Created by igorlira on 7/30/14.
 */
public class ViewCaseStepFormActivity extends ActionBarActivity
{
    boolean editMode;
    Case theCase;
    Flow flow;
    Menu menu;
    Flow.Step step;
    int stepVersion;

    View inventoryItemSelectView;
    int inventoryItemSelectCategoryId;

    private static final int REQUEST_INVENTORY_ITEM_SEARCH = 1;
    private static final int REQUEST_INVENTORY_ITEM_SEARCH_SELECT = 2;

    public static final int RESULT_STEP_EDITED = 1;
    boolean edited = false;

    public static class DatePickerFragment extends android.support.v4.app.DialogFragment
    {
        int day;
        int month;
        int year;

        boolean cancelled;

        DatePickerDialog.OnDateSetListener onDateSetListener;

        public DatePickerFragment init(int day, int month, int year, DatePickerDialog.OnDateSetListener onDateSetListener)
        {
            this.day = day;
            this.month = month - 1;
            this.year = year;
            this.onDateSetListener = onDateSetListener;

            return this;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {
            return new DatePickerDialog(this.getActivity(), new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int i, int i2, int i3) {
                    day = i3;
                    month = i2;
                    year = i;
                }
            }, year, month, day);
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            cancelled = true;
            super.onCancel(dialog);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            super.onDismiss(dialog);

            if(!cancelled)
            {
                this.onDateSetListener.onDateSet(null, year, month + 1, day);
            }
        }
    }

    boolean canEdit()
    {
        Case.Step stepData = theCase.getStep(step.id);
        //return (theCase.current_step != null && theCase.current_step.step_id == step.id) && (stepData == null || !stepData.hasResponsibleUser() || (stepData.hasResponsibleUser() && stepData.responsible_user_id == Zup.getInstance().getSessionUserId()));
        return (theCase.next_step_id != null && theCase.next_step_id == step.id) && (stepData == null || !stepData.hasResponsibleUser() || (stepData.hasResponsibleUser() && stepData.responsible_user_id == Zup.getInstance().getSessionUserId()));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_case_step_form);

        hideEditBar();

        Zup.getInstance().initStorage(this);
        UIHelper.initActivity(this, false);

        theCase = (Case) getIntent().getSerializableExtra("case");
        int flowId = getIntent().getIntExtra("flow_id", -1);
        int flowVersion = getIntent().getIntExtra("flow_version", -1);
        int stepId = getIntent().getIntExtra("step_id", -1);

        if(theCase == null || flowId == -1 || stepId == -1 || flowVersion == -1)
        {
            finish();
            return;
        }

        flow = Zup.getInstance().getFlow(flowId, flowVersion);
        if(theCase.getStep(stepId) != null)
        {
            step = theCase.getStep(stepId).my_step;
            stepVersion = theCase.getStep(stepId).step_version;
        }
        else
        {
            step = flow.getStep(stepId);
            stepVersion = step.last_version;
        }

        fillData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.case_step_form, menu);

        this.menu = menu;
        if(!canEdit())
            menu.findItem(R.id.action_edit).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    void enterEditMode()
    {
        showEditBar();
        editMode = true;
        fillData();
    }

    void confirmResponsibility()
    {
        ChangeResponsibleUserTask task = new ChangeResponsibleUserTask();
        task.execute();
    }

    class ChangeResponsibleUserTask extends AsyncTask<Void, Void, Boolean>
    {
        AlertDialog dialog;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            AlertDialog.Builder builder = new AlertDialog.Builder(ViewCaseStepFormActivity.this);
            builder.setMessage("Aguarde enquanto o condutor da etapa é alterado...");
            builder.setCancelable(false);
            dialog = builder.show();
        }

        @Override
        protected Boolean doInBackground(Void... voids)
        {
            Case _case = Zup.getInstance().updateCaseStep(theCase.id, step.id, stepVersion, null);

            if(_case != null)
                theCase = _case;
            // TODO update case

            return _case != null;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean)
        {
            super.onPostExecute(aBoolean);

            dialog.dismiss();

            if(aBoolean)
            {
                edited = true;
                enterEditMode();
            }
            else
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(ViewCaseStepFormActivity.this);
                builder.setTitle("Erro");
                builder.setMessage("Não foi possível alterar o condutor da etapa. Verifique se o dispositivo está com internet.");
                builder.setPositiveButton("OK", null);
                builder.show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_edit)
        {
            Case.Step stepData = theCase.getStep(step.id);
            if(stepData == null || !stepData.hasResponsibleUser())
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Para editar essa etapa, é necessário se tornar o condutor dela. Deseja continuar?");
                builder.setCancelable(false);
                builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        confirmResponsibility();
                    }
                });
                builder.setNegativeButton("Cancelar", null);
                builder.show();
            }
            else
            {
                enterEditMode();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    void fillData()
    {
        UIHelper.setTitle(this, step.title);

        ViewGroup container = (ViewGroup) findViewById(R.id.case_step_form_container);
        container.removeAllViews();

        if(editMode)
            container.addView(createEditableResponsibleUserView());
        else
            container.addView(createNonEditableResponsibleUserView());
        for(int i = 0; i < step.fields.length; i++)
        {
            Flow.Step.Field field = step.fields[i];

            View view = createFieldView(field, editMode);
            container.addView(view);
        }
    }

    View createFieldView(Flow.Step.Field field, boolean editable)
    {
        if(editable)
            return createEditableFieldView(field);
        else
            return createNonEditableFieldView(field);
    }

    View createEditableFieldView(final Flow.Step.Field field)
    {
        Case.Step stepData = theCase.getStep(field.step_id);
        View view = null;
        Object datavalue = null;

        if(stepData != null && stepData.hasDataField(field.id))
            datavalue = stepData.getDataField(field.id);

        if(field.field_type.equals("date"))
        {
            view = getLayoutInflater().inflate(R.layout.inventory_item_item_date_edit, null);
            view.setTag(R.id.tag_field_id, field.id);
            TextView title = (TextView) view.findViewById(R.id.inventory_item_date_name);
            TextView txtDay = (TextView) view.findViewById(R.id.inventory_item_date_day);
            TextView txtMonth = (TextView) view.findViewById(R.id.inventory_item_date_month);
            TextView txtYear = (TextView) view.findViewById(R.id.inventory_item_date_year);

            title.setText(field.title.toUpperCase());
            if(datavalue != null)
            {
                String date = (String) datavalue;
                Date dt = Zup.getInstance().getIsoDate(date);
                Calendar cal = Calendar.getInstance();
                cal.setTime(dt);

                txtDay.setText(Integer.toString(cal.get(Calendar.DAY_OF_MONTH)));
                txtMonth.setText(Integer.toString(cal.get(Calendar.MONTH)));
                txtYear.setText(Integer.toString(cal.get(Calendar.YEAR)));
            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showDatePicker(field, view);
                }
            });
        }
        else if(isIntegerField(field) || isDecimalField(field))
        {
            ViewGroup fieldView = (ViewGroup) getLayoutInflater().inflate(R.layout.inventory_item_item_text_edit, null);
            fieldView.setTag(R.id.tag_field_id, field.id);

            TextView fieldTitle = (TextView) fieldView.findViewById(R.id.inventory_item_text_name);
            EditText fieldValue = (EditText) fieldView.findViewById(R.id.inventory_item_text_value);
            TextView fieldExtra = (TextView) fieldView.findViewById(R.id.inventory_item_text_extra);

            int inputType = InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED;
            if(isDecimalField(field))
            {
                inputType |= InputType.TYPE_NUMBER_FLAG_DECIMAL;
            }

            fieldTitle.setText(field.title.toUpperCase());
            fieldValue.setLayoutParams(new LinearLayout.LayoutParams(100, ViewGroup.LayoutParams.WRAP_CONTENT));
            fieldValue.setInputType(inputType);

            String pkgName = this.getClass().getPackage().getName();
            int resId = getResources().getIdentifier("case_step_extra_" + field.field_type, "string", pkgName);
            if(resId != 0)
            {
                fieldExtra.setVisibility(View.VISIBLE);
                fieldExtra.setText(getResources().getText(resId));
            }

            if(datavalue != null)
            {
                fieldValue.setText(datavalue.toString());
            }

            view = fieldView;
        }
        else if(field.field_type.equals("select"))
        {
            view = getLayoutInflater().inflate(R.layout.inventory_item_item_select_edit, null);
            view.setTag(R.id.tag_field_id, field.id);
            TextView title = (TextView) view.findViewById(R.id.inventory_item_text_name);
            TextView value = (TextView) view.findViewById(R.id.inventory_item_text_value);

            title.setText(field.title.toUpperCase());

            if(datavalue == null)
            {
                value.setText("Selecione...");
            }
            else
            {
                Object val = field.values.get(datavalue);
                if(val != null)
                    value.setText(val.toString());
                else
                    value.setText("-");
            }

            view.setClickable(true);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showSelectDialog(view);
                }
            });
        }
        else if(field.field_type.equals("category_inventory"))
        {
            view = getLayoutInflater().inflate(R.layout.case_item_inventory_item_edit, null);
            final View fieldView = view;
            view.setTag(R.id.tag_field_id, field.id);
            TextView title = (TextView) view.findViewById(R.id.inventory_item_text_name);
            Button buttonAdd = (Button) view.findViewById(R.id.button_add_item);
            ViewGroup container = (ViewGroup) view.findViewById(R.id.inventory_items_container);

            title.setText(field.title.toUpperCase());

            buttonAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View bview) {
                    showInventoryItemSearch(field.category_inventory_id, fieldView);
                }
            });
        }
        else //(field.field_type.equals("text"))
        {
            view = getLayoutInflater().inflate(R.layout.inventory_item_item_text_edit, null);
            view.setTag(R.id.tag_field_id, field.id);
            TextView title = (TextView) view.findViewById(R.id.inventory_item_text_name);
            TextView value = (TextView) view.findViewById(R.id.inventory_item_text_value);

            if(datavalue != null)
                value.setText(datavalue.toString());

            title.setText(field.title.toUpperCase());
        }

        return view;
    }

    void showInventoryItemSearch(int categoryId, View view)
    {
        ViewGroup container = (ViewGroup) view.findViewById(R.id.inventory_items_container);

        Intent intent = new Intent(this, AdvancedSearchActivity.class);
        intent.putExtra("category_id", categoryId);
        this.startActivityForResult(intent, REQUEST_INVENTORY_ITEM_SEARCH);

        inventoryItemSelectView = view;
        inventoryItemSelectCategoryId = categoryId;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_INVENTORY_ITEM_SEARCH && resultCode == AdvancedSearchActivity.RESULT_SEARCH)
        {
            Intent intent = new Intent(this, InventoryItemsAdvancedSearchResultActivity.class);
            intent.putExtra("search_data", data);
            intent.putExtra("select", true);
            this.startActivityForResult(intent, REQUEST_INVENTORY_ITEM_SEARCH_SELECT);
        }
        else if(requestCode == REQUEST_INVENTORY_ITEM_SEARCH_SELECT && resultCode == InventoryItemsAdvancedSearchResultActivity.RESULT_OK)
        {
            int[] ids = data.getIntArrayExtra("ids");
            ViewGroup container = (ViewGroup) inventoryItemSelectView.findViewById(R.id.case_items_container);

            for(int i = 0; i < ids.length; i++)
            {
                View view = createInventoryItemView(inventoryItemSelectCategoryId, ids[i]);
                container.addView(view);
            }

            inventoryItemSelectView = null;
        }
    }

    View createInventoryItemView(int categoryId, int itemId)
    {
        View view = getLayoutInflater().inflate(R.layout.case_inventory_item_item, null);
        view.setTag(R.id.tag_item_id, itemId);

        TextView name = (TextView) view.findViewById(R.id.case_inventory_item_item_name);
        View remove = view.findViewById(R.id.case_inventory_item_item_remove);

        Zup.getInstance().showInventoryItemTitleInto(name, "", categoryId, itemId);

        return view;
    }

    void showSelectDialog(final View view)
    {
        int fieldId = (Integer) view.getTag(R.id.tag_field_id);
        Flow.Step.Field field = step.getField(fieldId);

        final String[] options = new String[field.values.size()];
        final String[] keys = new String[field.values.size()];
        int i = 0;
        for(Object key : field.values.keySet())
        {
            keys[i] = key.toString();
            options[i++] = field.values.get(key).toString();
        }

        final TextView value = (TextView) view.findViewById(R.id.inventory_item_text_value);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(field.title);

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                view.setTag(R.id.tag_button_value, keys[i]);
                value.setText(options[i]);
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    void showDatePicker(Flow.Step.Field field, View dateField)
    {
        final TextView txtDay = (TextView) dateField.findViewById(R.id.inventory_item_date_day);
        final TextView txtMonth = (TextView) dateField.findViewById(R.id.inventory_item_date_month);
        final TextView txtYear = (TextView) dateField.findViewById(R.id.inventory_item_date_year);

        Calendar calendar = Calendar.getInstance();
        if(txtDay.length() > 0 && txtMonth.length() > 0 && txtYear.length() > 0 && Integer.parseInt(txtDay.getText().toString()) > 0 && Integer.parseInt(txtMonth.getText().toString()) > 0 && Integer.parseInt(txtYear.getText().toString()) > 0)
        {
            calendar.clear();
            calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(txtDay.getText().toString()));
            calendar.set(Calendar.MONTH, Integer.parseInt(txtMonth.getText().toString()));
            calendar.set(Calendar.YEAR, Integer.parseInt(txtYear.getText().toString()));
        }

        DatePickerFragment fragment = new DatePickerFragment().init(calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                txtDay.setText(Integer.toString(day));
                txtMonth.setText(Integer.toString(month));
                txtYear.setText(Integer.toString(year));
            }
        });
        fragment.show(getSupportFragmentManager(), "datepicker");
    }

    void showChooseResponsibleUserDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Escolher condutor");
        builder.show();
    }

    View createNonEditableFieldView(Flow.Step.Field field)
    {
        Case.Step stepData = theCase.getStep(field.step_id);

        View view = getLayoutInflater().inflate(R.layout.inventory_item_item_text, null);
        TextView title = (TextView) view.findViewById(R.id.inventory_item_text_name);
        TextView value = (TextView) view.findViewById(R.id.inventory_item_text_value);

        title.setText(field.title.toUpperCase() + " (" + field.field_type + ")");
        if(stepData != null && stepData.hasDataField(field.id))
            value.setText(stepData.getDataField(field.id).toString());
        else
            value.setText("");

        return view;
    }

    View createNonEditableResponsibleUserView()
    {
        View view = getLayoutInflater().inflate(R.layout.inventory_item_item_text, null);
        TextView title = (TextView) view.findViewById(R.id.inventory_item_text_name);
        TextView value = (TextView) view.findViewById(R.id.inventory_item_text_value);

        title.setText("CONDUTOR");
        Case.Step stepData = theCase.getStep(step.id);
        if(stepData != null && stepData.hasResponsibleUser())
        {
            Zup.getInstance().showUsernameInto(value, "", stepData.responsible_user_id);
        }
        else
        {
            value.setText("Sem condutor");
        }

        return view;
    }

    View createEditableResponsibleUserView()
    {
        View view = getLayoutInflater().inflate(R.layout.inventory_item_item_select_edit, null);
        TextView title = (TextView) view.findViewById(R.id.inventory_item_text_name);
        TextView value = (TextView) view.findViewById(R.id.inventory_item_text_value);

        title.setText("CONDUTOR");

        Case.Step stepData = theCase.getStep(step.id);
        if(stepData != null && stepData.hasResponsibleUser())
        {
            Zup.getInstance().showUsernameInto(value, "", stepData.responsible_user_id);
        }
        else
        {
            value.setText("Sem condutor");
        }

        view.setClickable(true);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChooseResponsibleUserDialog();
            }
        });

        return view;
    }

    class PublishTask extends AsyncTask<Void, Void, Case>
    {
        Hashtable<Integer, Object> fields;
        AlertDialog dialog;

        public PublishTask(Hashtable<Integer, Object> fields)
        {
            this.fields = fields;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            AlertDialog.Builder builder = new AlertDialog.Builder(ViewCaseStepFormActivity.this);
            builder.setCancelable(false);
            builder.setMessage("Aguarde enquanto os dados são enviados...");
            dialog = builder.show();
        }

        @Override
        protected Case doInBackground(Void... voids)
        {
            return Zup.getInstance().updateCaseStep(theCase.id, step.id, stepVersion, fields);
        }

        @Override
        protected void onPostExecute(Case aCase)
        {
            super.onPostExecute(aCase);

            dialog.dismiss();
            if(aCase != null)
            {
                // TODO update case
                edited = true;
                theCase = aCase;
                leaveEditMode();
            }
            else
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(ViewCaseStepFormActivity.this);
                builder.setPositiveButton("OK", null);
                builder.setMessage("Não foi possível atualizar o caso. Verifique se o dispositivo está conectado à internet.");
                builder.show();
            }
        }
    }

    void leaveEditMode()
    {
        hideEditBar();
        editMode = false;
        fillData();
    }

    public void finishEditing(View view)
    {
        ViewGroup container = (ViewGroup) findViewById(R.id.case_step_form_container);

        Hashtable<Integer, Object> fields = new Hashtable<Integer, Object>();
        for(int i = 0; i < container.getChildCount(); i++)
        {
            View row = container.getChildAt(i);
            Integer fieldId = (Integer)row.getTag(R.id.tag_field_id);
            if(fieldId == null)
                continue;

            Object value = serializeField(row);

            if(value != null)
                fields.put(fieldId, value);
        }

        PublishTask task = new PublishTask(fields);
        task.execute();
    }

    boolean isIntegerField(Flow.Step.Field field)
    {
        return field.field_type.equals("integer") || field.field_type.equals("year") || field.field_type.equals("month") || field.field_type.equals("day") || field.field_type.equals("hour") || field.field_type.equals("minute") || field.field_type.equals("second") || field.field_type.equals("angle");
    }

    boolean isDecimalField(Flow.Step.Field field)
    {
        return field.field_type.equals("meter") || field.field_type.equals("centimeter") || field.field_type.equals("kilometer") || field.field_type.equals("decimal");
    }

    Object serializeField(View view)
    {
        Integer fieldId = (Integer) view.getTag(R.id.tag_field_id);
        Flow.Step.Field field = step.getField(fieldId);

        if(field.field_type.equals("date"))
        {
            TextView txtDay = (TextView) view.findViewById(R.id.inventory_item_date_day);
            TextView txtMonth = (TextView) view.findViewById(R.id.inventory_item_date_month);
            TextView txtYear = (TextView) view.findViewById(R.id.inventory_item_date_year);

            try
            {
                int day = Integer.parseInt(txtDay.getText().toString());
                int month = Integer.parseInt(txtMonth.getText().toString());
                int year = Integer.parseInt(txtYear.getText().toString());

                Calendar cal = Calendar.getInstance();
                cal.clear();
                cal.set(year, month, day);

                return Zup.getIsoDate(cal.getTime());
            }
            catch (NumberFormatException ex)
            {
                return null;
            }
        }
        else if(field.field_type.equals("select"))
        {
            return view.getTag(R.id.tag_button_value);
        }
        else if(field.field_type.equals("category_inventory"))
        {
            ViewGroup container = (ViewGroup) view.findViewById(R.id.case_items_container);
            int[] ids = new int[container.getChildCount()];

            for(int i = 0; i < container.getChildCount(); i++)
            {
                View child = container.getChildAt(i);
                ids[i] = (Integer) child.getTag(R.id.tag_item_id);
            }

            return ids;
        }
        else if(isDecimalField(field) || isIntegerField(field))
        {
            TextView value = (TextView) view.findViewById(R.id.inventory_item_text_value);

            if(value.length() < 1)
                return null;
            else
                return Float.parseFloat(value.getText().toString());
        }
        else
        {
            TextView value = (TextView) view.findViewById(R.id.inventory_item_text_value);

            if(value.length() < 1)
                return null;
            else
                return value.getText().toString();
        }
    }

    void showEditBar()
    {
        getSupportActionBar().hide();

        View bar = findViewById(R.id.case_step_form_editbar);
        bar.setVisibility(View.VISIBLE);
    }

    void hideEditBar()
    {
        getSupportActionBar().show();
        //findViewById(R.id.case_step_form_editbar).setVisibility(View.GONE);

        final View bar = findViewById(R.id.case_step_form_editbar);
        TranslateAnimation animation = new TranslateAnimation(0, 0, 0, -bar.getHeight());
        animation.setDuration(200);

        bar.startAnimation(animation);

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                bar.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }
}
