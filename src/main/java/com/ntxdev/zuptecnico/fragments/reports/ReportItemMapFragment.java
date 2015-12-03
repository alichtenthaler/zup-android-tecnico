package com.ntxdev.zuptecnico.fragments.reports;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ntxdev.zuptecnico.R;
import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.entities.ReportCategory;
import com.ntxdev.zuptecnico.entities.ReportItem;
import com.ntxdev.zuptecnico.ui.ImageLoadedListener;

/**
 * Created by igorlira on 7/18/15.
 */
public class ReportItemMapFragment extends Fragment implements ImageLoadedListener {
    SupportMapFragment mapFragment;
    Marker marker;
    int markerResourceId;

    ReportItem getItem() {
        return (ReportItem) getArguments().getParcelable("item");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_report_details_map, container, false);
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment);

        mapFragment.getMap().getUiSettings().setMyLocationButtonEnabled(false);
        mapFragment.getMap().getUiSettings().setAllGesturesEnabled(false);
        mapFragment.getMap().getUiSettings().setZoomControlsEnabled(false);

        refresh();

        return root;
    }

    public void refresh() {
        mapFragment.getMap().clear();

        CameraPosition position = new CameraPosition.Builder()
                .target(new LatLng(getItem().position.latitude, getItem().position.longitude))
                .zoom(15)
                .build();

        mapFragment.getMap().moveCamera(CameraUpdateFactory.newCameraPosition(position));

        setupMarker();
    }

    void setupMarker() {
        ReportCategory category = Zup.getInstance().getReportCategoryService()
                .getReportCategory(getItem().category_id);
        Bitmap marker = Zup.getInstance().getReportCategoryService()
                .getReportCategoryMarker(getItem().category_id);

        BitmapDescriptor descriptor;
        if(marker != null)
            descriptor = BitmapDescriptorFactory.fromBitmap(marker);
        else {
            descriptor = BitmapDescriptorFactory.defaultMarker();
            this.markerResourceId = Zup.getInstance().requestImage(category.getMarkerURL(), true, this);
        }

        MarkerOptions options = new MarkerOptions();
        options.icon(descriptor);
        options.position(new LatLng(getItem().position.latitude, getItem().position.longitude));
        options.draggable(false);

        this.marker = this.mapFragment.getMap().addMarker(options);
    }

    @Override
    public void onImageLoaded(int resourceId) {
        if(resourceId == this.markerResourceId) {
            Bitmap bitmap = Zup.getInstance().getBitmap(resourceId);
            BitmapDescriptor descriptor = BitmapDescriptorFactory.fromBitmap(bitmap);

            if(bitmap != null) {
                marker.setIcon(descriptor);

                Zup.getInstance().getReportCategoryService()
                        .setReportCategoryMarker(getItem().category_id, bitmap);
            }
            else {
                // TODO remove invalid bitmap
            }
        }
    }
}
