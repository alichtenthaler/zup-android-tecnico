package com.ntxdev.zuptecnico.fragments.reports;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.ntxdev.zuptecnico.FullScreenImageActivity;
import com.ntxdev.zuptecnico.R;
import com.ntxdev.zuptecnico.entities.InventoryItemImage;
import com.ntxdev.zuptecnico.entities.ReportItem;
import com.ntxdev.zuptecnico.ui.WebImageView;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by igorlira on 7/18/15.
 */
public class ReportItemImagesFragment extends Fragment implements View.OnClickListener {
    ReportItem getItem() {
        return (ReportItem) getArguments().getParcelable("item");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_report_details_images, container, false);
        fillData(root);

        return root;
    }

    public void refresh() {
        fillData((ViewGroup) getView());
    }

    void fillData(ViewGroup root) {
        ReportItem item = getItem();

        if(item == null || item.images == null)
            return;

        ViewGroup container = (ViewGroup) root.findViewById(R.id.imageContainer);
        container.removeAllViews();

        for(int i = 0; i < item.images.length; i++) {
            WebImageView imageView = new WebImageView(root.getContext());

            if(item.images[i].content == null) {
                Picasso.with(getActivity().getApplicationContext()).load(item.images[i].thumb)
                        .networkPolicy(NetworkPolicy.OFFLINE).into(imageView);
                imageView.setOnClickListener(this);
            }
            else {
                imageView.setImageBitmap(item.images[i].getBitmap());
            }

            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setTag(item.images[i]);

            container.addView(imageView);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(150, 150);
            params.setMargins(0, 0, 5, 0);
            imageView.setLayoutParams(params);
        }
    }

    InventoryItemImage toInventoryItemImage(ReportItem.Image image) {
        InventoryItemImage img = new InventoryItemImage();
        img.versions = new InventoryItemImage.Versions();
        img.versions.high = image.high;
        img.versions.low = image.low;
        img.versions.thumb = image.thumb;
        img.url = image.original;

        return img;
    }

    InventoryItemImage[] toInventoryItemImageArray() {
        ArrayList<InventoryItemImage> result = new ArrayList<>();

        for(int i = 0; i < getItem().images.length; i++) {
            ReportItem.Image image = getItem().images[i];

            InventoryItemImage img = toInventoryItemImage(image);
            result.add(img);
        }

        InventoryItemImage[] resultArray = new InventoryItemImage[result.size()];
        result.toArray(resultArray);

        return resultArray;
    }

    @Override
    public void onClick(View view) {
        ReportItem.Image image = (ReportItem.Image) view.getTag();

        // TODO refatorar essa classe pra suportar qualquer tipo de imagem
        InventoryItemImage[] images = toInventoryItemImageArray();
        InventoryItemImage img = toInventoryItemImage(image);

        Intent intent = new Intent(getActivity(), FullScreenImageActivity.class);
        intent.putExtra("images", images);
        intent.putExtra("image", (Parcelable) img);
        startActivity(intent);
    }
}
