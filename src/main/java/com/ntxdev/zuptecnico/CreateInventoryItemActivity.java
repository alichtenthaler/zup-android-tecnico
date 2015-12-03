package com.ntxdev.zuptecnico;

import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationClient;
import com.kbeanie.imagechooser.api.ChooserType;
import com.kbeanie.imagechooser.api.ChosenImage;
import com.kbeanie.imagechooser.api.ImageChooserListener;
import com.kbeanie.imagechooser.api.ImageChooserManager;
import com.ntxdev.zuptecnico.api.EditInventoryItemSyncAction;
import com.ntxdev.zuptecnico.api.PublishInventoryItemSyncAction;
import com.ntxdev.zuptecnico.api.SyncAction;
import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.api.ZupCache;
import com.ntxdev.zuptecnico.entities.InventoryCategory;
import com.ntxdev.zuptecnico.entities.InventoryCategoryStatus;
import com.ntxdev.zuptecnico.entities.InventoryItem;
import com.ntxdev.zuptecnico.entities.InventoryItemImage;
import com.ntxdev.zuptecnico.ui.UIHelper;
import com.ntxdev.zuptecnico.util.FieldUtils;
import com.ntxdev.zuptecnico.util.GPSUtils;
import com.ntxdev.zuptecnico.util.IOUtil;
import com.ntxdev.zuptecnico.util.ResizeAnimation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import br.com.rezende.mascaras.Mask;

/**
 * Created by igorlira on 3/9/14.
 */
public class CreateInventoryItemActivity extends AppCompatActivity implements ImageChooserListener {

    public class Receiver extends BroadcastReceiver
    {
        public Receiver()
        {

        }

        @Override
        public void onReceive(Context context, Intent intent)
        {
            CreateInventoryItemActivity.this.onReceive(context, intent);
        }
    }

    public void onReceive(Context context, Intent intent)
    {
        boolean noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
        if(noConnectivity)
            finish();
    }

    class SelectDialogSearchTask extends AsyncTask<String, Void, View[]>
    {
        InventoryCategory.Section.Field field;
        View fieldView;
        View dialogView;
        AlertDialog dialog;

        public SelectDialogSearchTask(InventoryCategory.Section.Field field, View fieldView, View dialogView, AlertDialog dialog)
        {
            this.field = field;
            this.fieldView = fieldView;
            this.dialogView = dialogView;
            this.dialog = dialog;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            ViewGroup container = (ViewGroup) dialogView.findViewById(R.id.dialog_select_items_container);
            container.removeAllViews();

            TextView itemView = new TextView(CreateInventoryItemActivity.this);
            itemView.setClickable(true);
            itemView.setText("Carregando...");
            itemView.setBackgroundResource(R.drawable.sidebar_cell);
            itemView.setPadding(20, 20, 20, 20);

            container.addView(itemView);
        }

        @Override
        protected View[] doInBackground(String... strings)
        {
            String filter = strings[0];

            ArrayList<View> result = new ArrayList<View>();

            if(field.field_options != null) {
                for (int i = 0; i < field.field_options.length; i++) {
                    InventoryCategory.Section.Field.Option option = field.field_options[i];

                    if(filter != null && filter.length() > 0 && !option.value.toLowerCase().contains(filter.toLowerCase()))
                        continue;

                    View separator = new View(CreateInventoryItemActivity.this);
                    separator.setBackgroundColor(0xffcccccc);
                    separator.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));

                    //container.addView(separator);

                    TextView itemView = new TextView(CreateInventoryItemActivity.this);
                    itemView.setClickable(true);
                    itemView.setText(option.value);
                    itemView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                    itemView.setBackgroundResource(R.drawable.sidebar_cell);
                    itemView.setPadding(20, 20, 20, 20);
                    itemView.setTag(option.id);

                    //container.addView(itemView);

                    itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();

                            TextView tv = (TextView)view;

                            TextView fieldValue = (TextView) fieldView.findViewById(R.id.inventory_item_text_value);
                            fieldValue.setText(tv.getText());
                            fieldValue.setTag(tv.getTag());
                        }
                    });

                    result.add(separator);
                    result.add(itemView);
                }
            }

            return result.toArray(new View[0]);
        }

        @Override
        protected void onPostExecute(View[] result)
        {
            super.onPostExecute(result);

            ViewGroup container = (ViewGroup) dialogView.findViewById(R.id.dialog_select_items_container);
            container.removeAllViews();

            for (int i = 0; i < result.length; i++) {
                container.addView(result[i]);
            }
        }
    }

    private static final int PICK_LOCATION = 1;
    private static final int PICK_IMAGE = 2;

    public static final int REQUEST_CREATE_ITEM = 1;

    public static final int RESULT_ITEM_SAVED = 1;

    private static final String URL_PATTERN = "^(https?:\\/\\/)?([\\da-zA-Z0-9\\.-]+)\\.([a-zA-Z0-9\\.]{2,6})([\\/\\w \\.-]*)*\\/?$";
    private static final String EMAIL_PATTERN = "[a-z0-9A-Z!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9A-Z!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9A-Z](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\\.)+[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?";

    private ArrayList<ViewGroup> locationFields;
    private LocationClient locationClient;
    boolean createMode;
    InventoryCategory category;
    InventoryItem item;

    int _pickImageFieldId;

    Thread addressLoaderWorker;

    private ImageChooserManager imageChooserManager;
    public String filePath;

    private SelectDialogSearchTask searchTask = null;

    private boolean isFakeCreate = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_item_create);

        Zup.getInstance().initStorage(getApplicationContext());

        UIHelper.initActivity(this, false);
        Intent intent = getIntent();

        this.locationFields = new ArrayList<ViewGroup>();
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.registerReceiver(new Receiver(), new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));

        isFakeCreate = intent.getBooleanExtra("fake_create", false);

        createMode = intent.getBooleanExtra("create", true);
        if (createMode) {
            int categoryId = intent.getIntExtra("categoryId", 0);
            category = Zup.getInstance().getInventoryCategory(categoryId);
        }
        else {
            int categoryId = intent.getIntExtra("categoryId", 0);
            int itemId = intent.getIntExtra("item_id", 0);

            category = Zup.getInstance().getInventoryCategory(categoryId);
            if(Zup.getInstance().hasInventoryItem(itemId))
                item = Zup.getInstance().getInventoryItem(itemId);
            else
                item = ZupCache.getInventoryItem(itemId);
        }

        locationClient = new LocationClient(this, new GooglePlayServicesClient.ConnectionCallbacks() {
            @Override
            public void onConnected(Bundle bundle) {
                locationAvailable();
            }

            @Override
            public void onDisconnected() {
                locationUnavailable();
            }
        }, new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(ConnectionResult connectionResult) {
                locationUnavailable();
            }
        });
        locationClient.connect();

        if (category != null) {
            buildPage();
        }
    }


    void showNoConnectionBar()
    {
        View view = findViewById(R.id.bar_no_connection);
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if(params.height == 35)
            return;

        ResizeAnimation animation = new ResizeAnimation(view, 0, 35);
        animation.setDuration(350);

        view.startAnimation(animation);

        for(ViewGroup vg : locationFields)
        {
            vg.findViewById(R.id.inventory_item_create_field_button_button).setEnabled(false);
        }
    }

    void hideNoConnectionBar()
    {
        View view = findViewById(R.id.bar_no_connection);
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if(params.height == 0)
            return;

        ResizeAnimation animation = new ResizeAnimation(view, 35, 0);
        animation.setDuration(350);

        view.startAnimation(animation);

        for(ViewGroup vg : locationFields)
        {
            vg.findViewById(R.id.inventory_item_create_field_button_button).setEnabled(true);
        }
    }

    boolean validateField(InventoryCategory.Section.Field field, ViewGroup fieldView)
    {
        Object value = getFieldValue(fieldView);
        if(value == null)
            return true;

        boolean validationResult;

        if(field.kind.equals("cpf"))
        {
            validationResult = value.toString().length() == "000.000.000-00".length();
        }
        else if(field.kind.equals("cnpj"))
        {
            validationResult = value.toString().length() == "00.000.000/0000-00".length();
        }
        else if(field.kind.equals("url"))
        {
            validationResult = value.toString().matches(URL_PATTERN);
        }
        else if(field.kind.equals("email"))
        {
            validationResult = value.toString().matches(EMAIL_PATTERN);
        }
        else
            validationResult = true;

        boolean minimumResult;
        boolean maximumResult;

        if(field.kind.equals("integer") || field.kind.equals("years") || field.kind.equals("months") || field.kind.equals("days") || field.kind.equals("hours") || field.kind.equals("seconds"))
        {
            int val = (Integer) value;
            if(field.minimum != null)
                minimumResult = val >= field.minimum;
            else
                minimumResult = true;

            if(field.maximum != null)
                maximumResult = val <= field.maximum;
            else
                maximumResult = true;
        }
        else if(field.kind.equals("decimal") || field.kind.equals("meters") || field.kind.equals("centimeters") || field.kind.equals("kilometers") || field.kind.equals("angle"))
        {
            float val = (Float) value;
            if(field.minimum != null)
                minimumResult = val >= field.minimum;
            else
                minimumResult = true;

            if(field.maximum != null)
                maximumResult = val <= field.maximum;
            else
                maximumResult = true;
        }
        else
        {
            String val = value.toString();

            if(field.minimum != null)
                minimumResult = val.length() >= field.minimum;
            else
                minimumResult = true;

            if(field.maximum != null)
                maximumResult = val.length() <= field.maximum;
            else
                maximumResult = true;
        }

        boolean rangeResult = minimumResult && maximumResult;
        return validationResult && rangeResult;
    }

    public void finishEditing(View view)
    {
        boolean validationFailed = false;
        View firstFieldError = null;

        ViewGroup container = (ViewGroup)findViewById(R.id.inventory_item_create_container);
        for(int i = 0; i < container.getChildCount(); i++)
        {
            View child = container.getChildAt(i);
            if(child.getTag(R.id.inventory_item_create_is_status_field) != null && category.require_item_status)
            {
                if(getStatusFieldValue() == null)
                {
                    setStatusFieldButtonsColor((RadioGroup) child, 0xffff0000);
                    validationFailed = true;

                    if(firstFieldError == null)
                        firstFieldError = child;
                }
                else
                {
                    setStatusFieldButtonsColor((RadioGroup) child, 0xff000000);
                }
            }

            if(child.getTag(R.id.inventory_item_create_fieldid) == null)
                continue;

            ViewGroup childContainer = (ViewGroup)child;

            int fieldId = (Integer)child.getTag(R.id.inventory_item_create_fieldid);
            InventoryCategory.Section.Field field = category.getField(fieldId);
            InventoryCategory.Section section = category.getFieldSection(fieldId);

            TextView fieldTitle = (TextView) childContainer.findViewById(R.id.inventory_item_text_name);

            Object value = getFieldValue(childContainer);

            if((((field.required != null && field.required) || (section.required != null && section.required)) && (value == null)) || (value != null && !validateField(field, childContainer))) {
                fieldTitle.setTextColor(0xffff0000);
                validationFailed = true;

                if(firstFieldError == null)
                    firstFieldError = child;
            }
            else {
                fieldTitle.setTextColor(0xff666666);
            }
        }

        if(validationFailed)
        {
            ScrollView scroll = (ScrollView)findViewById(R.id.inventory_item_create_scroll);
            scroll.smoothScrollTo(0, firstFieldError.getTop());
            return;
        }

        InventoryItem item;
        if(createMode)
        {
            item = Zup.getInstance().createInventoryItem();
            item.inventory_category_id = category.id;
        }
        else
        {
            item = this.item;
        }

        for(int i = 0; i < container.getChildCount(); i++)
        {
            View child = container.getChildAt(i);
            if(child.getTag(R.id.inventory_item_create_fieldid) == null)
                continue;

            ViewGroup childContainer = (ViewGroup)child;
            int fieldId = (Integer)childContainer.getTag(R.id.inventory_item_create_fieldid);

            Object value = getFieldValue(childContainer);
            if(value != null)
                item.setFieldValue(fieldId, value);
        }

        item.inventory_status_id = getStatusFieldValue();
        item.position = new InventoryItem.Coordinates();
        item.position.latitude = Float.parseFloat(item.getFieldValue(category.getField("latitude").id).toString());
        item.position.longitude = Float.parseFloat(item.getFieldValue(category.getField("longitude").id).toString());

        if(!Zup.getInstance().hasInventoryItem(item.id))
            Zup.getInstance().addInventoryItem(item);

        if(isFakeCreate)
        {
            Zup.getInstance().removeSyncActionsRelatedToInventoryItem(this.item.id);

            if((this.item.id & InventoryItem.LOCAL_MASK) != 0)
                createMode = true;
        }

        Zup.getInstance().updateInventoryItemInfo(item.id, item);
        //Zup.getInstance().sync();
        if(createMode)
            Zup.getInstance().addSyncAction(new PublishInventoryItemSyncAction(item));
        else
        {
            Zup.getInstance().addSyncAction(new EditInventoryItemSyncAction(item));
        }

        Zup.getInstance().sync();
        Intent intent = new Intent();
        intent.putExtra("item_id", item.id);
        setResult(RESULT_ITEM_SAVED, intent); // reset signal
        finish();
    }

    private void setStatusFieldButtonsColor(RadioGroup group, int color)
    {
        for(int i = 0; i < group.getChildCount(); i++)
        {
            View child = group.getChildAt(i);
            if (!(child instanceof RadioButton))
                continue;

            RadioButton button = (RadioButton) group.getChildAt(i);
            button.setTextColor(color);
        }
    }

    private Integer getStatusFieldValue()
    {
        ViewGroup container = (ViewGroup)findViewById(R.id.inventory_item_create_container);
        for(int i = 0; i < container.getChildCount(); i++)
        {
            View child = container.getChildAt(i);
            if (child.getTag(R.id.inventory_item_create_is_status_field) == null)
                continue;

            RadioGroup group = (RadioGroup) child;
            try
            {
                RadioButton button = (RadioButton) group.findViewById(group.getCheckedRadioButtonId());
                if(button.getTag() instanceof InventoryCategoryStatus)
                    return ((InventoryCategoryStatus) button.getTag()).id;
            }
            catch (Exception ex)
            {
                return null;
            }
        }

        return null;
    }

    private Object getFieldValue(ViewGroup childContainer)
    {
        int fieldId = (Integer)childContainer.getTag(R.id.inventory_item_create_fieldid);
        InventoryCategory.Section.Field field = category.getField(fieldId);

        Object value = null;
        if(field.kind.equals("radio"))
        {
            ViewGroup radioContainer = (ViewGroup)childContainer.findViewById(R.id.inventory_item_radio_container);
            RadioGroup radioGroup = (RadioGroup)radioContainer.getChildAt(0);
            RadioButton selectedButton;
            try
            {
                selectedButton = (RadioButton)radioGroup.findViewById(radioGroup.getCheckedRadioButtonId());
            }
            catch (NullPointerException ex)
            {
                selectedButton = null;
            }

            if(selectedButton != null)
            {
                value = selectedButton.getTag(R.id.tag_button_value);
            }
        }
        else if(field.kind.equals("checkbox")) {
            ArrayList<Integer> result = new ArrayList<Integer>();

            ViewGroup radioContainer = (ViewGroup) childContainer.findViewById(R.id.inventory_item_radio_container);
            for (int i = 0; i < radioContainer.getChildCount(); i++) {
                if (!(radioContainer.getChildAt(i) instanceof CheckBox))
                    continue;

                CheckBox checkBox = (CheckBox) radioContainer.getChildAt(i);
                if (checkBox.isChecked()) {
                    result.add((Integer) checkBox.getTag(R.id.tag_button_value));
                }
            }

            if (result.size() > 0)
                value = result;
        }
        else if(field.kind.equals("integer") || field.kind.equals("years") || field.kind.equals("months") || field.kind.equals("days") || field.kind.equals("hours") || field.kind.equals("seconds"))
        {
            TextView txtValue = (TextView) childContainer.findViewById(R.id.inventory_item_text_value);
            try
            {
                value = Integer.parseInt(txtValue.getText().toString());
            }
            catch (NumberFormatException ex)
            {
                value = null;
            }
        }
        else if(field.kind.equals("decimal") || field.kind.equals("meters") || field.kind.equals("centimeters") || field.kind.equals("kilometers") || field.kind.equals("angle"))
        {
            TextView txtValue = (TextView) childContainer.findViewById(R.id.inventory_item_text_value);
            try
            {
                value = Float.parseFloat(txtValue.getText().toString());
            }
            catch (NumberFormatException ex)
            {
                value = null;
            }
        }
        else if(field.kind.equals("images"))
        {
            ArrayList<InventoryItemImage> result = new ArrayList<InventoryItemImage>();
            ViewGroup container = (ViewGroup) childContainer.findViewById(R.id.inventory_item_images_container);
            for(int i = 0; i < container.getChildCount(); i++)
            {
                if(!(container.getChildAt(i) instanceof ImageView))
                    continue;

                try {
                    InventoryItemImage imageData = new InventoryItemImage();

                    ImageView imageView = (ImageView) container.getChildAt(i);
                    String path = (String) imageView.getTag();
                    byte[] buffer = IOUtil.readFile(path);

                    imageData.content = Base64.encodeToString(buffer, Base64.NO_WRAP);
                    result.add(imageData);
                }
                catch (IOException ex) { }
            }

            value = result;
        }
        else if(field.kind.equals("date") || field.kind.equals("time"))
        {
            TextView txtValue = (TextView) childContainer.findViewById(R.id.inventory_item_text_value);

            String tvalue = (String) txtValue.getTag();
            if(tvalue != null && tvalue.length() > 0)
                value = tvalue;
        }
        else if(field.kind.equals("select"))
        {
            TextView txtValue = (TextView) childContainer.findViewById(R.id.inventory_item_text_value);

            Integer tvalue = (Integer) txtValue.getTag();
            if(tvalue != null)
            {
                value = new int[] { tvalue };
            }
        }
        else if(field.kind == null || field.kind.equals("text") || field.kind.equals("cpf") || field.kind.equals("cnpj") || field.kind.equals("url") || field.kind.equals("email") || field.kind.equals("textarea"))
        {
            TextView txtValue = (TextView) childContainer.findViewById(R.id.inventory_item_text_value);
            if(txtValue.getText().length() > 0)
                value = txtValue.getText().toString();
        }

        return value;
    }

    void fillPos()
    {
        ViewGroup latitudeContainer = getFieldView("latitude");
        ViewGroup longitudeContainer = getFieldView("longitude");
        TextView latitudeText = (TextView)latitudeContainer.findViewById(R.id.inventory_item_text_value);
        TextView longitudeText = (TextView)longitudeContainer.findViewById(R.id.inventory_item_text_value);

        Location lastLocation = locationClient.getLastLocation();
        if(lastLocation == null)
            return;

        double latitude = lastLocation.getLatitude();
        double longitude = lastLocation.getLongitude();

        latitudeText.setText(Double.toString(latitude));
        longitudeText.setText(Double.toString(longitude));
    }

    void locationAvailable()
    {
        for(ViewGroup vg : this.locationFields)
        {
            Button btn = (Button) vg.findViewById(R.id.inventory_item_fill_pos_button);

            btn.setEnabled(true);
        }
    }

    void locationUnavailable()
    {
        for(ViewGroup vg : this.locationFields)
        {
            Button btn = (Button) vg.findViewById(R.id.inventory_item_fill_pos_button);

            btn.setEnabled(false);
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
            return buildPageB(container);
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
        ViewGroup container = (ViewGroup)findViewById(R.id.inventory_item_create_container);

        PageBuilder builder = new PageBuilder(container);
        builder.execute();
    }

    void showMap()
    {
        Intent intent = new Intent(this, PickMapLocationActivity.class);
        ViewGroup latitudeContainer = getFieldView("latitude");
        ViewGroup longitudeContainer = getFieldView("longitude");
        TextView latitudeText = (TextView)latitudeContainer.findViewById(R.id.inventory_item_text_value);
        TextView longitudeText = (TextView)longitudeContainer.findViewById(R.id.inventory_item_text_value);

        double latitude = 0, longitude = 0;
        boolean positionValid = false;
        try
        {
            latitude = Double.parseDouble(latitudeText.getText().toString());
            longitude = Double.parseDouble(longitudeText.getText().toString());
            positionValid = true;
        }
        catch (Exception ex)
        {
            // should we do anything?
        }

        if(positionValid)
        {
            intent.putExtra("position_latitude", latitude);
            intent.putExtra("position_longitude", longitude);
        }

        intent.putExtra("inventory_category_id", category.id);

        startActivityForResult(intent, PICK_LOCATION);
    }

    private View[] buildPageB(ViewGroup parent)
    {
        //ViewGroup container = (ViewGroup)findViewById(R.id.inventory_item_create_container);
        //container.removeAllViews();
        locationFields.clear();

        ArrayList<View> result = new ArrayList<View>();
        ObjectMapper mapper = new ObjectMapper();

        if(category.sections != null) {
            result.addAll(FieldUtils.createRadiosForCategoryStatuses(parent, this, getLayoutInflater(), category, createMode, item));
            category.sortSections();

            for (int i = 0; i < category.sections.length; i++) {
                InventoryCategory.Section section = category.sections[i];
                result.add(FieldUtils.createSectionHeader(parent, getLayoutInflater(), section));

                if(section.isLocationSection())
                {
                    ViewGroup fieldView = (ViewGroup) getLayoutInflater().inflate(R.layout.inventory_item_create_field_button, null, false);

                    locationFields.add(fieldView);

                    Button btn = (Button)fieldView.findViewById(R.id.inventory_item_create_field_button_button);
                    btn.setText("Localizar no mapa");

                    Button fillPos = (Button) fieldView.findViewById(R.id.inventory_item_fill_pos_button);
                    fillPos.setClickable(true);
                    fillPos.setEnabled(locationClient != null && locationClient.isConnected());
                    fillPos.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            fillPos();
                        }
                    });

                    btn.setClickable(true);
                    btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            showMap();
                        }
                    });

                    result.add(fieldView);
                }

                section.sortFields();
                for (int j = 0; j < section.fields.length; j++)
                {
                    final InventoryCategory.Section.Field field = section.fields[j];
                    String label;
                    if(field.label != null) {
                        label = field.label.toUpperCase();
                    } else {
                        label = field.title.toUpperCase();
                    }

                    if(field.kind.equals("radio"))
                    {
                        result.add(FieldUtils.createRadiosForField(parent, mapper, label, this, getLayoutInflater(), field, createMode, item));
                    }
                    else if(field.kind.equals("checkbox"))
                    {
                        result.add(FieldUtils.createCheckboxesForField(parent, mapper, label, this, getLayoutInflater(), field, createMode, item));
                    }
                    else if(field.kind.equals("images"))
                    {
                        result.add(FieldUtils.createImagesField(parent, label, field, getLayoutInflater(), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                view.setFocusable(true);
                                view.requestFocus();
                                _pickImageFieldId = field.id;
                                pickImage();
                            }
                        }));
                    }
                    else if(field.kind.equals("decimal") || field.kind.equals("integer") || field.kind.equals("meters") || field.kind.equals("centimeters") || field.kind.equals("kilometers") || field.kind.equals("years") || field.kind.equals("months") || field.kind.equals("days") || field.kind.equals("hours") || field.kind.equals("seconds") || field.kind.equals("angle"))
                    {
                        result.add(FieldUtils.createNumberField(parent, this, label, field, getLayoutInflater(), createMode, item));
                    }
                    else if(field.kind.equals("select"))
                    {
                        result.add(FieldUtils.createSelectField(parent, mapper, label, field, getLayoutInflater(), createMode, item, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                createSelectDialog(field, view);
                            }
                        }));
                    }
                    else if(field.kind.equals("date"))
                    {
                        result.add(FieldUtils.createDateField(parent, label, field, getLayoutInflater(), createMode, item, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                createDatePickerDialog(field, view);
                            }
                        }));
                    }
                    else if(field.kind.equals("time"))
                    {
                        result.add(FieldUtils.createTimeField(parent, label, field, getLayoutInflater(), createMode, item, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                createTimePickerDialog(field, view);
                            }
                        }));
                    }
                    else if(field.kind.equals("cpf") || field.kind.equals("cnpj"))
                    {
                        result.add(FieldUtils.createCPForCNPJField(parent, label, field, getLayoutInflater(), createMode, item));
                    }
                    else if(field.kind.equals("url") || field.kind.equals("email"))
                    {
                        result.add(FieldUtils.createURLorEmailField(parent, label, field, getLayoutInflater(), createMode, item));
                    }
                    else
                    {
                        result.add(FieldUtils.createTextField(parent, this, label, field, getLayoutInflater(), createMode, item));
                        //container.addView(fieldView);
                    }
                    //else if(field.kind != null) { // unknown type
                    //    Log.e("FIELD RENDERING" , "Unknown field type: " + field.kind);
                    //}
                }
            }
        }

        View[] resultArr = new View[result.size()];
        result.toArray(resultArr);

        return resultArr;
    }

    private void createDatePickerDialog(final InventoryCategory.Section.Field field, final View fieldView)
    {
        String label;
        if(field.label != null) {
            label = field.label;
        } else {
            label = field.title;
        }

        Calendar cal = Calendar.getInstance();

        TextView fieldValue = (TextView) fieldView.findViewById(R.id.inventory_item_text_value);
        final String oldstring = fieldValue.getText().toString();
        final String oldvalue = (String) fieldValue.getTag();
        if(oldvalue != null)
        {
            try {
                String[] chunks = oldvalue.split("/");

                cal.set(Integer.parseInt(chunks[2]), Integer.parseInt(chunks[1]) - 1, Integer.parseInt(chunks[0]));
            } catch (Exception ex) { }
        }

        final DatePickerDialog dialog;
        dialog = new DatePickerDialog(CreateInventoryItemActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month++;

                String daystr = Integer.toString(day);
                if(daystr.length() < 2)
                    daystr = "0" + daystr;

                String monthstr = Integer.toString(month);
                if(monthstr.length() < 2)
                    monthstr = "0" + monthstr;

                String text = daystr + "/" + monthstr + "/" + year;

                TextView fieldValue = (TextView) fieldView.findViewById(R.id.inventory_item_text_value);
                fieldValue.setText(text);
                fieldValue.setTag(text);

            }
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                TextView fieldValue = (TextView) fieldView.findViewById(R.id.inventory_item_text_value);
                fieldValue.setText(oldstring);
                fieldValue.setTag(oldvalue);
            }
        });

        dialog.setTitle(label);

        dialog.setCancelable(true);
        dialog.show();
    }

    private void createTimePickerDialog(final InventoryCategory.Section.Field field, final View fieldView)
    {
        String label;
        if(field.label != null) {
            label = field.label;
        } else {
            label = field.title;
        }

        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);

        TextView fieldValue = (TextView) fieldView.findViewById(R.id.inventory_item_text_value);
        final String oldstring = fieldValue.getText().toString();
        final String oldvalue = (String) fieldValue.getTag();
        if(oldvalue != null)
        {
            try {
                String[] chunks = oldvalue.split(":");

                hour = Integer.parseInt(chunks[0]);
                minute = Integer.parseInt(chunks[1]);
            } catch (Exception ex) { }
        }

        final TimePickerDialog dialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                String hourstr = Integer.toString(hour);
                if(hourstr.length() < 2)
                    hourstr = "0" + hourstr;

                String minutestr = Integer.toString(minute);
                if(minutestr.length() < 2)
                    minutestr = "0" + minutestr;

                String text = hourstr + ":" + minutestr;

                TextView fieldValue = (TextView) fieldView.findViewById(R.id.inventory_item_text_value);
                fieldValue.setText(text);
                fieldValue.setTag(text);
            }
        }, hour, minute, true);

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                TextView fieldValue = (TextView) fieldView.findViewById(R.id.inventory_item_text_value);
                fieldValue.setText(oldstring);
                fieldValue.setTag(oldvalue);
            }
        });

        dialog.setTitle(label);

        dialog.setCancelable(true);
        dialog.show();
    }

    private void createSelectDialog(final InventoryCategory.Section.Field field, final View fieldView)
    {
        String label;
        if(field.label != null) {
            label = field.label;
        } else {
            label = field.title;
        }

        final View view = getLayoutInflater().inflate(R.layout.dialog_select_items, null);
        final EditText input = (EditText) view.findViewById(R.id.dialog_select_items_search);

        final AlertDialog.Builder builder = new AlertDialog.Builder(CreateInventoryItemActivity.this);
        builder.setTitle(label);
        builder.setView(view);

        builder.setCancelable(true);
        builder.setNegativeButton("Cancelar", null);

        final AlertDialog dialog = builder.show();

        this.refreshSelectDialog(null, field, fieldView, view, dialog);

        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                refreshSelectDialog(editable.toString(), field, fieldView, view, dialog);
            }
        });
    }

    private void refreshSelectDialog(String filter, InventoryCategory.Section.Field field, final View fieldView, View dialogView, final AlertDialog dialog)
    {
        if(this.searchTask != null)
            this.searchTask.cancel(true);

        this.searchTask = new SelectDialogSearchTask(field, fieldView, dialogView, dialog);
        this.searchTask.execute(filter);
    }

    private void pickImage()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Adicionar imagem");
        builder.setItems(new String[] { "Da galeria", "Da câmera" }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                int chooserType;

                if(i == 0)
                    chooserType = ChooserType.REQUEST_PICK_PICTURE;
                else
                    chooserType = ChooserType.REQUEST_CAPTURE_PICTURE;

                imageChooserManager = new ImageChooserManager(CreateInventoryItemActivity.this, chooserType, "myfolder");
                imageChooserManager.setImageChooserListener(CreateInventoryItemActivity.this);
                try {
                    filePath = imageChooserManager.choose();
                }
                catch (Exception ex)
                {
                    Log.e("ZUP", "Pick image error: " + ex.getMessage(), ex);
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        ScrollView scrollView = (ScrollView)findViewById(R.id.inventory_item_create_scroll);

        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo(); //.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        boolean isConnected = activeNetInfo != null && activeNetInfo.isConnectedOrConnecting();
        if (isConnected)
            hideNoConnectionBar();
        else
            showNoConnectionBar();
    }

    private ViewGroup getFieldView(int id)
    {
        ViewGroup container = (ViewGroup)findViewById(R.id.inventory_item_create_container);
        for(int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child.getTag(R.id.inventory_item_create_fieldid) == null)
                continue;

            ViewGroup childContainer = (ViewGroup) child;

            int fieldId = (Integer) child.getTag(R.id.inventory_item_create_fieldid);
            if(fieldId == id)
                return childContainer;
        }

        return null;
    }

    private ViewGroup getFieldView(String fieldName)
    {
        ViewGroup container = (ViewGroup)findViewById(R.id.inventory_item_create_container);
        for(int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child.getTag(R.id.inventory_item_create_fieldid) == null)
                continue;

            ViewGroup childContainer = (ViewGroup) child;

            int fieldId = (Integer) child.getTag(R.id.inventory_item_create_fieldid);
            InventoryCategory.Section.Field field = category.getField(fieldId);
            if(field.title != null && field.title.equals(fieldName))
                return childContainer;
        }

        return null;
    }

    private ViewGroup getFieldViewByType(String fieldType)
    {
        ViewGroup container = (ViewGroup)findViewById(R.id.inventory_item_create_container);
        for(int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child.getTag(R.id.inventory_item_create_fieldid) == null)
                continue;

            ViewGroup childContainer = (ViewGroup) child;

            int fieldId = (Integer) child.getTag(R.id.inventory_item_create_fieldid);
            InventoryCategory.Section.Field field = category.getField(fieldId);
            if(field.kind.equals(fieldType))
                return childContainer;
        }

        return null;
    }

    void fillAddress(Address addressData)
    {
        if(addressData == null)
            addressData = new Address(Locale.ENGLISH);

        String addressTmp = "";
        if(addressData.getThoroughfare() != null)
            addressTmp += addressData.getThoroughfare() + ", ";
        if(addressData.getFeatureName() != null)
            addressTmp += addressData.getFeatureName();

        final String address = addressTmp;
        final String city = addressData.getSubAdminArea();
        final String state = addressData.getAdminArea();
        final String postalCode = addressData.getPostalCode();
        final String district = addressData.getSubLocality();

        View addressView = getFieldView("address");
        TextView addressText = (TextView)addressView.findViewById(R.id.inventory_item_text_value);
        View postalCodeView = getFieldView("postal_code");
        TextView postalCodeText = (TextView)postalCodeView.findViewById(R.id.inventory_item_text_value);
        View districtView = getFieldView("district");
        TextView districtText = (TextView)districtView.findViewById(R.id.inventory_item_text_value);
        View cityView = getFieldView("city");
        TextView cityText = (TextView)cityView.findViewById(R.id.inventory_item_text_value);
        View stateView = getFieldView("state");
        TextView stateText = (TextView)stateView.findViewById(R.id.inventory_item_text_value);

        addressText.setText(address);
        postalCodeText.setText(postalCode);
        districtText.setText(district);
        cityText.setText(city);
        stateText.setText(state);
    }

    void loadAddress(double latitude, double longitude)
    {
        try {
            //Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            //List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);
            List<Address> addressList = GPSUtils.getFromLocation(this, latitude, longitude);
            if (addressList != null && addressList.size() > 0) {
                final Address addressData = addressList.get(0);

                // ESTADO = admin area
                // NUMERO = feature name
                // RUA = thorough fare
                // NUMERO = subthoroughfare
                // BAIRRO = sub locality
                // CIDADE = sub admin area
                // CEP = postal code

                final String address = addressData.getThoroughfare() + ", " + addressData.getFeatureName();
                final String city = addressData.getSubAdminArea();
                final String state = addressData.getAdminArea();
                final String postalCode = addressData.getPostalCode();
                final String district = addressData.getSubLocality();

                Zup.runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        fillAddress(addressData);
                    }
                });
            }
        }
        catch (Exception ex)
        {
            Zup.runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(CreateInventoryItemActivity.this, "Não foi possível obter o endereço do local.", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    /* http://stackoverflow.com/questions/1910608/android-action-image-capture-intent */
    public boolean hasImageCaptureBug() {

        // list of known devices that have the bug
        ArrayList<String> devices = new ArrayList<String>();
        devices.add("android-devphone1/dream_devphone/dream");
        devices.add("generic/sdk/generic");
        devices.add("vodafone/vfpioneer/sapphire");
        devices.add("tmobile/kila/dream");
        devices.add("verizon/voles/sholes");
        devices.add("google_ion/google_ion/sapphire");
        devices.add("generic/vbox86p/vbox86p");

        String val = android.os.Build.BRAND + "/" + android.os.Build.PRODUCT + "/"
                + android.os.Build.DEVICE;

        return devices.contains(val);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_LOCATION) {
            ViewGroup latitudeContainer = getFieldView("latitude");
            ViewGroup longitudeContainer = getFieldView("longitude");

            if (resultCode == 1 && latitudeContainer != null && longitudeContainer != null) {
                final double latitude = data.getDoubleExtra("result_latitude", 0);
                final double longitude = data.getDoubleExtra("result_longitude", 0);
                Address address = (Address) data.getExtras().get("result_address");

                if (latitude != 0 || longitude != 0) {
                    TextView latitudeText = (TextView) latitudeContainer.findViewById(R.id.inventory_item_text_value);
                    TextView longitudeText = (TextView) longitudeContainer.findViewById(R.id.inventory_item_text_value);

                    latitudeText.setText(Double.toString(latitude));
                    longitudeText.setText(Double.toString(longitude));

                    fillAddress(address);
                }
            }
        }
        else if(requestCode == PICK_IMAGE)
        {
            Uri _uri = null;
            if (data != null)
            {
                _uri = data.getData();
            }
            else if (hasImageCaptureBug()) {
                File fi = new File("/sdcard/tmp");
                try {
                    _uri = Uri.parse(android.provider.MediaStore.Images.Media.insertImage(getContentResolver(), fi.getAbsolutePath(), null, null));
                    if (!fi.delete()) {
                        Log.i("logMarker", "Failed to delete " + fi);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }

            if(_uri == null)
                return;

            //User had pick an image.
            Cursor cursor = getContentResolver().query(_uri, new String[] { android.provider.MediaStore.Images.ImageColumns.DATA }, null, null, null);
            cursor.moveToFirst();

            final String imageFilePath = cursor.getString(0);
            Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath);

            ViewGroup fieldView = getFieldViewByType("images");
            ViewGroup container = (ViewGroup) fieldView.findViewById(R.id.inventory_item_images_container);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(150, 150);
            layoutParams.setMargins(0, 0, 15, 0);

            final ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(layoutParams);
            imageView.setBackgroundColor(0xffcccccc);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setImageBitmap(bitmap);
            imageView.setTag(imageFilePath);

            container.addView(imageView);

            cursor.close();
        }
        else if (resultCode == RESULT_OK && (requestCode == ChooserType.REQUEST_PICK_PICTURE || requestCode == ChooserType.REQUEST_CAPTURE_PICTURE))
        {
            imageChooserManager.submit(requestCode, data);
        }
    }

    void imageChosen_ui(ChosenImage chosenImage)
    {
        String imageFilePath = chosenImage.getFilePathOriginal();
        Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath);

        ViewGroup fieldView = getFieldView(_pickImageFieldId);//getFieldViewByType("images");
        ViewGroup container = (ViewGroup) fieldView.findViewById(R.id.inventory_item_images_container);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(150, 150);
        layoutParams.setMargins(0, 0, 15, 0);

        ImageView imageView = new ImageView(this);
        imageView.setLayoutParams(layoutParams);
        imageView.setBackgroundColor(0xffcccccc);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setImageBitmap(bitmap);
        imageView.setTag(imageFilePath);

        container.addView(imageView);
    }

    void error_ui(String s)
    {
        Toast.makeText(this, "Houve um erro ao selecionar a imagem: " + s, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onImageChosen(final ChosenImage chosenImage) {
        Zup.runOnMainThread(new Runnable() {
            @Override
            public void run() {
                imageChosen_ui(chosenImage);
            }
        });
    }

    @Override
    public void onError(final String s) {
        Zup.runOnMainThread(new Runnable() {
            @Override
            public void run() {
                error_ui(s);
            }
        });
    }
}
