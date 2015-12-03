package com.ntxdev.zuptecnico.activities.reports;

import android.support.v7.app.AlertDialog;
import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ntxdev.zuptecnico.R;
import com.ntxdev.zuptecnico.adapters.ReportCategoriesAdapter;
import com.ntxdev.zuptecnico.api.EditReportItemSyncAction;
import com.ntxdev.zuptecnico.api.PublishReportItemSyncAction;
import com.ntxdev.zuptecnico.api.SyncAction;
import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.entities.ReportCategory;
import com.ntxdev.zuptecnico.entities.ReportItem;
import com.ntxdev.zuptecnico.entities.User;
import com.ntxdev.zuptecnico.fragments.CreateUserDialog;
import com.ntxdev.zuptecnico.fragments.PickLocationDialog;
import com.ntxdev.zuptecnico.fragments.UserPickerDialog;
import com.ntxdev.zuptecnico.fragments.reports.CreateReportImagesFragment;
import com.ntxdev.zuptecnico.fragments.reports.ReportCategorySelectorDialog;
import com.ntxdev.zuptecnico.util.GPSUtils;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by igorlira on 7/20/15.
 */
public class CreateReportItemActivity extends AppCompatActivity implements PickLocationDialog.OnLocationSetListener {
    public static final int RESULT_REPORT_CHANGED = 1;

    private int categoryId;
    private User user;
    private Address address;
    private String reference;
    private ReportItem item;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_create_report_item);

        int categoryId = getIntent().getIntExtra("categoryId", -1);
        this.item = (ReportItem) getIntent().getExtras().get("item");
        if(categoryId == -1 && this.item == null) {
            finish();
            return;
        }

        if(this.item != null) {
            setCategoryId(this.item.category_id);
            setLocation(this.item.position.latitude, this.item.position.longitude,
                    parseAddressFromItem(), this.item.reference);
            assignUser(this.item.user);
            setImages(this.item.images);
            setDescription(this.item.description);

            findViewById(R.id.create_report_user_remove).setVisibility(View.GONE);
        }
        else {
            setCategoryId(categoryId);
        }
    }

    Address parseAddressFromItem() {
        Address address = new Address(Locale.getDefault());
        address.setThoroughfare(this.item.address);
        address.setFeatureName(this.item.number);
        address.setSubLocality(this.item.district);
        address.setSubAdminArea(this.item.city);
        address.setAdminArea(this.item.state);
        address.setCountryName(this.item.country);
        address.setPostalCode(this.item.postalCode);

        return address;
    }

    boolean isEdit() {
        return this.item != null;
    }

    void setCategoryId(int id) {
        this.categoryId = id;

        TextView txtCategory = (TextView) findViewById(R.id.category_title);
        ReportCategory category = Zup.getInstance().getReportCategoryService().getReportCategory(id);

        txtCategory.setText(category.title);
    }

    public void chooseCategory(View sender) {
        showCreateDialog();
    }

    public void chooseLocation(View sender) {
        Bundle args = new Bundle();
        args.putInt("categoryId", this.categoryId);
        args.putParcelable("address", this.address);
        args.putString("reference", this.reference);

        PickLocationDialog dialog = new PickLocationDialog();
        dialog.setArguments(args);
        dialog.setOnLocationSetListener(this);
        dialog.show(getSupportFragmentManager(), "location_picker_dialog");
    }

    public void showCreateDialog() {
        ReportCategorySelectorDialog dialog = new ReportCategorySelectorDialog();
        dialog.show(getSupportFragmentManager(), "category_list");
        dialog.setListener(new ReportCategorySelectorDialog.OnReportCategorySetListener() {
            @Override
            public void onReportCategorySet(int categoryId) {
                setCategoryId(categoryId);
            }
        });
    }

    @Override
    public void onLocationSet(double latitude, double longitude, Address address, String reference) {
        setLocation(latitude, longitude, address, reference);
    }

    void setLocation(double latitude, double longitude, Address address, String reference) {
        TextView txtAddress = (TextView) findViewById(R.id.full_address);

        if(address != null) {
            txtAddress.setText(GPSUtils.formatAddress(address));

            address.setLatitude(latitude);
            address.setLongitude(longitude);
        }
        else {
            txtAddress.setText(null);
        }

        this.reference = reference;
        this.address = address;
    }

    public void assignToMe(View sender) {
        assignUser(Zup.getInstance().getSessionUser());
    }

    public void selectUser(View sender) {
        UserPickerDialog dialog = new UserPickerDialog();
        dialog.show(getSupportFragmentManager(), "user_picker");
        dialog.setListener(new UserPickerDialog.OnUserPickedListener() {
            @Override
            public void onUserPicked(User user) {
                assignUser(user);
            }
        });
    }

    public void createUser(View sender) {
        CreateUserDialog dialog = new CreateUserDialog();
        dialog.show(getSupportFragmentManager(), "user_creator");
        dialog.setListener(new CreateUserDialog.OnUserCreatedListener() {
            @Override
            public void onUserCreated(User user) {
                assignUser(user);
            }
        });
    }

    public void removeUser(View sender) {
        assignUser(null);
    }

    void assignUser(User user) {
        this.user = user;
        TextView txtUserName = (TextView) findViewById(R.id.create_report_user_name);
        View btnRemove = findViewById(R.id.create_report_user_remove);

        if (user != null) {
            hideUserButtons();

            txtUserName.setVisibility(View.VISIBLE);
            btnRemove.setVisibility(View.VISIBLE);
            txtUserName.setText(user.name);
        }
        else {
            showUserButtons();
            txtUserName.setVisibility(View.GONE);
            btnRemove.setVisibility(View.GONE);
        }
    }

    void hideUserButtons() {
        findViewById(R.id.create_report_button_assign_me).setVisibility(View.GONE);
        findViewById(R.id.create_report_button_select_user).setVisibility(View.GONE);
        findViewById(R.id.create_report_button_create_user).setVisibility(View.GONE);
    }

    void showUserButtons() {
        findViewById(R.id.create_report_button_assign_me).setVisibility(View.VISIBLE);
        findViewById(R.id.create_report_button_select_user).setVisibility(View.VISIBLE);
        findViewById(R.id.create_report_button_create_user).setVisibility(View.VISIBLE);
    }

    String getDescription() {
        return ((TextView) findViewById(R.id.report_description)).getText().toString();
    }

    void setDescription(String text) {
        ((TextView) findViewById(R.id.report_description)).setText(text);
    }

    void setImages(ReportItem.Image[] images) {
        CreateReportImagesFragment imagesFragment = (CreateReportImagesFragment)
                getSupportFragmentManager().findFragmentById(R.id.images);

        for(int i = 0; i < images.length; i++) {
            imagesFragment.addImage(images[i].original);
        }
    }

    public void complete(View sender) {
        if(address == null) {
            new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.report_creation_failed_address))
                    .show();

            return;
        }

        CreateReportImagesFragment imagesFragment = (CreateReportImagesFragment)
                getSupportFragmentManager().findFragmentById(R.id.images);

        String[] imgs = new String[imagesFragment.getAddableCount()];
        for(int i = 0, j = 0; i < imagesFragment.getCount(); i++) {
            String base64 = imagesFragment.getItemBase64(i);
            if(base64 != null) {
                imgs[j++] = base64;
            }
        }

        SyncAction action;
        if(isEdit()) {
            String newAddress = address.getThoroughfare()
                    + (address.getFeatureName() != null ? ", " + address.getFeatureName() : "");

            action = new EditReportItemSyncAction(
                    item.id,
                    address.getLatitude(),
                    address.getLongitude(),
                    categoryId,
                    getDescription(),
                    reference,
                    newAddress,
                    address.getSubLocality(),
                    address.getSubAdminArea(),
                    address.getAdminArea(),
                    address.getCountryName(),
                    imgs,
                    user,
                    item.category_id
            );

            item.position.latitude = (float) address.getLatitude();
            item.position.longitude = (float) address.getLongitude();
            item.category_id = categoryId;
            item.description = getDescription();
            item.reference = reference;
            item.address = newAddress;
            item.district = address.getSubLocality();
            item.city = address.getSubAdminArea();
            item.state = address.getAdminArea();
            item.country = address.getCountryName();

            ArrayList<ReportItem.Image> newImages = new ArrayList<>();
            for(int i = 0; i < item.images.length; i++) {
                newImages.add(item.images[i]);
            }

            for(int i = 0; i < imagesFragment.getCount(); i++) {
                String base64 = imagesFragment.getItemBase64(i);
                if(base64 != null) {
                    newImages.add(new ReportItem.Image(base64));
                }
            }

            ReportItem.Image[] newArray = new ReportItem.Image[newImages.size()];
            newImages.toArray(newArray);

            item.images = newArray;
        }
        else {
            action = new PublishReportItemSyncAction(
                    address.getLatitude(),
                    address.getLongitude(),
                    categoryId,
                    getDescription(),
                    reference,
                    address.getThoroughfare() + (address.getFeatureName() != null ? ", " + address.getFeatureName() : ""),
                    address.getSubLocality(),
                    address.getSubAdminArea(),
                    address.getAdminArea(),
                    address.getCountryName(),
                    imgs,
                    user
            );
        }

        Zup.getInstance().addSyncAction(action);
        Zup.getInstance().sync();

        if(isEdit()) {
            Intent intent = new Intent();
            intent.putExtra("item", item);

            setResult(RESULT_REPORT_CHANGED, intent);
        }else{
            Toast.makeText(this, getString(R.string.creating_report), Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}
