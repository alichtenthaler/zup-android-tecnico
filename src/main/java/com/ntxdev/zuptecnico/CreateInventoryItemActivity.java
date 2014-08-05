package com.ntxdev.zuptecnico;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.kbeanie.imagechooser.api.ChooserType;
import com.kbeanie.imagechooser.api.ChosenImage;
import com.kbeanie.imagechooser.api.ImageChooserListener;
import com.kbeanie.imagechooser.api.ImageChooserManager;
import com.ntxdev.zuptecnico.api.EditInventoryItemSyncAction;
import com.ntxdev.zuptecnico.api.PublishInventoryItemSyncAction;
import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.api.ZupCache;
import com.ntxdev.zuptecnico.entities.InventoryCategory;
import com.ntxdev.zuptecnico.entities.InventoryCategoryStatus;
import com.ntxdev.zuptecnico.entities.InventoryItem;
import com.ntxdev.zuptecnico.entities.InventoryItemImage;
import com.ntxdev.zuptecnico.ui.UIHelper;
import com.ntxdev.zuptecnico.util.GPSUtils;
import com.ntxdev.zuptecnico.util.IOUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Created by igorlira on 3/9/14.
 */
public class CreateInventoryItemActivity extends ActionBarActivity implements ImageChooserListener {
    private static final int PICK_LOCATION = 1;
    private static final int PICK_IMAGE = 2;

    public static final int REQUEST_CREATE_ITEM = 1;

    public static final int RESULT_ITEM_SAVED = 1;

    boolean createMode;
    InventoryCategory category;
    InventoryItem item;

    int _pickImageFieldId;

    Thread addressLoaderWorker;

    private ImageChooserManager imageChooserManager;
    public String filePath;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_item_create);

        Zup.getInstance().initStorage(getApplicationContext());

        getSupportActionBar().hide();
        UIHelper.initActivity(this, false);
        Intent intent = getIntent();

        createMode = intent.getBooleanExtra("create", true);
        if (createMode) {
            int categoryId = intent.getIntExtra("category_id", 0);
            category = Zup.getInstance().getInventoryCategory(categoryId);
        }
        else {
            int categoryId = intent.getIntExtra("category_id", 0);
            int itemId = intent.getIntExtra("item_id", 0);

            category = Zup.getInstance().getInventoryCategory(categoryId);
            if(Zup.getInstance().hasInventoryItem(itemId))
                item = Zup.getInstance().getInventoryItem(itemId);
            else
                item = ZupCache.getInventoryItem(itemId);
        }

        if (category != null) {
            buildPage();
        }
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

            if(((field.required != null && field.required) || (section.required != null && section.required)) && (value == null)) {
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
            ArrayList<String> result = new ArrayList<String>();

            ViewGroup radioContainer = (ViewGroup) childContainer.findViewById(R.id.inventory_item_radio_container);
            for (int i = 0; i < radioContainer.getChildCount(); i++) {
                if (!(radioContainer.getChildAt(i) instanceof CheckBox))
                    continue;

                CheckBox checkBox = (CheckBox) radioContainer.getChildAt(i);
                if (checkBox.isChecked()) {
                    result.add((String) checkBox.getTag(R.id.tag_button_value));
                }
            }

            if (result.size() > 0)
                value = result;
        }
        else if(field.kind.equals("integer") || field.kind.equals("meters") || field.kind.equals("centimeters") || field.kind.equals("kilometers") || field.kind.equals("years") || field.kind.equals("months") || field.kind.equals("days") || field.kind.equals("hours") || field.kind.equals("seconds") || field.kind.equals("angle"))
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
        else if(field.kind == null || field.kind.equals("text"))
        {
            TextView txtValue = (TextView) childContainer.findViewById(R.id.inventory_item_text_value);
            if(txtValue.getText().length() > 0)
                value = txtValue.getText().toString();
        }

        return value;
    }

    private void buildPage()
    {
        ViewGroup container = (ViewGroup)findViewById(R.id.inventory_item_create_container);
        container.removeAllViews();

        if(category.sections != null) {
            Iterator<InventoryCategoryStatus> statuses = Zup.getInstance().getInventoryCategoryStatusIterator(category.id);
            if(statuses.hasNext())
            {
                ViewGroup sectionHeader = (ViewGroup) getLayoutInflater().inflate(R.layout.inventory_item_section_header, container, false);

                TextView sectionTitle = (TextView) sectionHeader.findViewById(R.id.inventory_item_section_title);
                sectionTitle.setText("ESTADO");
                container.addView(sectionHeader);

                RadioGroup statusesContainer = new RadioGroup(this);
                statusesContainer.setOrientation(LinearLayout.VERTICAL);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(20, 10, 20, 5);
                statusesContainer.setLayoutParams(params);

                while(statuses.hasNext())
                {
                    InventoryCategoryStatus status = statuses.next();

                    RadioButton checkBox = new RadioButton(this);
                    checkBox.setText(status.title);
                    checkBox.setTag(status);
                    statusesContainer.addView(checkBox);

                    if(!createMode && item.inventory_status_id != null && item.inventory_status_id == status.id)
                        statusesContainer.check(checkBox.getId());
                }

                statusesContainer.setTag(R.id.inventory_item_create_is_status_field, true);
                container.addView(statusesContainer);
            }
            for (int i = 0; i < category.sections.length; i++) {
                InventoryCategory.Section section = category.sections[i];
                ViewGroup sectionHeader = (ViewGroup) getLayoutInflater().inflate(R.layout.inventory_item_section_header, container, false);

                TextView sectionTitle = (TextView) sectionHeader.findViewById(R.id.inventory_item_section_title);
                sectionTitle.setText(section.title.toUpperCase());

                container.addView(sectionHeader);

                if(section.isLocationSection())
                {
                    ViewGroup fieldView = (ViewGroup) getLayoutInflater().inflate(R.layout.inventory_item_create_field_button, container, false);

                    Button btn = (Button)fieldView.findViewById(R.id.inventory_item_create_field_button_button);
                    btn.setText("Localizar no mapa");

                    btn.setClickable(true);
                    final Activity activity = this;
                    btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(activity, PickMapLocationActivity.class);
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

                            startActivityForResult(intent, PICK_LOCATION);
                        }
                    });

                    container.addView(fieldView);
                }

                for (int j = 0; j < section.fields.length; j++) {
                    final InventoryCategory.Section.Field field = section.fields[j];
                    String label;
                    if(field.label != null) {
                        label = field.label.toUpperCase();
                    } else {
                        label = field.title.toUpperCase();
                    }

                    if(field.kind.equals("radio"))
                    {
                        ViewGroup fieldView = (ViewGroup) getLayoutInflater().inflate(R.layout.inventory_item_item_radio_edit, container, false);
                        fieldView.setTag(R.id.inventory_item_create_fieldid, field.id);

                        TextView fieldTitle = (TextView) fieldView.findViewById(R.id.inventory_item_text_name);
                        fieldTitle.setText(label);

                        ViewGroup radiocontainer = (ViewGroup)fieldView.findViewById(R.id.inventory_item_radio_container);
                        if(field.available_values != null)
                        {
                            RadioGroup group = new RadioGroup(this);
                            group.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            for(int x = 0; x < field.available_values.length; x++)
                            {
                                //ViewGroup radioElement = (ViewGroup)getLayoutInflater().inflate(R.layout.inventory_item_item_radio_element, radiocontainer, false);

                                RadioButton button = new RadioButton(this);//(RadioButton)radioElement.findViewById(R.id.inventory_item_item_radio_radio);
                                button.setText(field.available_values[x]);
                                button.setTag(R.id.tag_button_value, field.available_values[x]);

                                if(!createMode && item.getFieldValue(field.id) != null && item.getFieldValue(field.id).equals(field.available_values[x]))
                                    button.setChecked(true);

                                group.addView(button);
                            }
                            radiocontainer.addView(group);
                        }
                        container.addView(fieldView);
                    }
                    else if(field.kind.equals("checkbox"))
                    {
                        ViewGroup fieldView = (ViewGroup) getLayoutInflater().inflate(R.layout.inventory_item_item_radio_edit, container, false);
                        fieldView.setTag(R.id.inventory_item_create_fieldid, field.id);

                        TextView fieldTitle = (TextView) fieldView.findViewById(R.id.inventory_item_text_name);
                        fieldTitle.setText(label);

                        ViewGroup radiocontainer = (ViewGroup)fieldView.findViewById(R.id.inventory_item_radio_container);
                        if(field.available_values != null)
                        {
                            for(int x = 0; x < field.available_values.length; x++)
                            {
                                CheckBox button = new CheckBox(this);//(RadioButton)radioElement.findViewById(R.id.inventory_item_item_radio_radio);
                                button.setText(field.available_values[x]);
                                button.setTag(R.id.tag_button_value, field.available_values[x]);

                                if(!createMode && item.getFieldValue(field.id) != null && item.getFieldValue(field.id).equals(field.available_values[x]))
                                    button.setChecked(true);

                                radiocontainer.addView(button);
                            }
                        }
                        container.addView(fieldView);
                    }
                    else if(field.kind.equals("images"))
                    {
                        ViewGroup fieldView = (ViewGroup) getLayoutInflater().inflate(R.layout.inventory_item_item_images_edit, container, false);
                        fieldView.setTag(R.id.inventory_item_create_fieldid, field.id);

                        TextView fieldTitle = (TextView) fieldView.findViewById(R.id.inventory_item_text_name);
                        fieldTitle.setText(label);

                        Button addButton = (Button) fieldView.findViewById(R.id.inventory_item_images_button);
                        addButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                view.setFocusable(true);
                                view.requestFocus();
                                _pickImageFieldId = field.id;
                                pickImage();
                            }
                        });

                        container.addView(fieldView);
                    }
                    else if(field.kind.equals("integer") || field.kind.equals("meters") || field.kind.equals("centimeters") || field.kind.equals("kilometers") || field.kind.equals("years") || field.kind.equals("months") || field.kind.equals("days") || field.kind.equals("hours") || field.kind.equals("seconds") || field.kind.equals("angle"))
                    {
                        ViewGroup fieldView = (ViewGroup) getLayoutInflater().inflate(R.layout.inventory_item_item_text_edit, container, false);
                        fieldView.setTag(R.id.inventory_item_create_fieldid, field.id);

                        TextView fieldTitle = (TextView) fieldView.findViewById(R.id.inventory_item_text_name);
                        EditText fieldValue = (EditText) fieldView.findViewById(R.id.inventory_item_text_value);
                        TextView fieldExtra = (TextView) fieldView.findViewById(R.id.inventory_item_text_extra);

                        fieldTitle.setText(label);
                        fieldValue.setLayoutParams(new LinearLayout.LayoutParams(100, ViewGroup.LayoutParams.WRAP_CONTENT));
                        fieldValue.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);

                        String pkgName = this.getClass().getPackage().getName();
                        int resId = getResources().getIdentifier("inventory_item_extra_" + field.kind, "string", pkgName);
                        if(resId != 0)
                        {
                            fieldExtra.setVisibility(View.VISIBLE);
                            fieldExtra.setText(getResources().getText(resId));
                        }

                        container.addView(fieldView);
                    }
                    else {
                        ViewGroup fieldView = (ViewGroup) getLayoutInflater().inflate(R.layout.inventory_item_item_text_edit, container, false);
                        fieldView.setTag(R.id.inventory_item_create_fieldid, field.id);

                        TextView fieldTitle = (TextView) fieldView.findViewById(R.id.inventory_item_text_name);
                        TextView fieldValue = (TextView) fieldView.findViewById(R.id.inventory_item_text_value);

                        if(field.kind != null && !field.kind.equals("text")) {
                            label += " (Unknown field kind: " + field.kind + ")";
                            fieldValue.setEnabled(false);
                        }

                        fieldTitle.setText(label);
                        if(!createMode && item.getFieldValue(field.id) != null)
                            fieldValue.setText(item.getFieldValue(field.id).toString());

                        container.addView(fieldView);
                    }
                    //else if(field.kind != null) { // unknown type
                    //    Log.e("FIELD RENDERING" , "Unknown field type: " + field.kind);
                    //}
                }
            }
        }
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

         /*ScrollView scrollView = (ScrollView)findViewById(R.id.inventory_item_create_scroll);
        _scrollY = scrollView.getScrollY();

        Intent pickIntent = new Intent();
        pickIntent.setType("image/*");
        pickIntent.setAction(Intent.ACTION_GET_CONTENT);

        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(hasImageCaptureBug())
            takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File("/sdcard/tmp")));
        else
            takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        String pickTitle = "Select or take a new Picture"; // Or get from strings.xml
        Intent chooserIntent = Intent.createChooser(pickIntent, pickTitle);
        chooserIntent.putExtra
                (
                        Intent.EXTRA_INITIAL_INTENTS,
                        new Intent[] { takePhotoIntent }
                );

        startActivityForResult(chooserIntent, PICK_IMAGE);*/

        /*Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(intent, "Selecionar imagem"), PICK_IMAGE);*/
    }

    @Override
    protected void onResume() {
        super.onResume();

        ScrollView scrollView = (ScrollView)findViewById(R.id.inventory_item_create_scroll);
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
        final String address = addressData.getThoroughfare() + ", " + addressData.getFeatureName();
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
                    Toast.makeText(CreateInventoryItemActivity.this, "Não foi possível obter o endereço do local.", 3).show();
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

                    /*if(addressLoaderWorker != null)
                        addressLoaderWorker.interrupt();

                    addressLoaderWorker = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            loadAddress(latitude, longitude);
                        }
                    });
                    addressLoaderWorker.start();*/
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
        Toast.makeText(this, "Houve um erro ao selecionar a imagem: " + s, 5);
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
