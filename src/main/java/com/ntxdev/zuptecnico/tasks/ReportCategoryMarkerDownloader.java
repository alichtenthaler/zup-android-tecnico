package com.ntxdev.zuptecnico.tasks;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.entities.ReportCategory;
import com.ntxdev.zuptecnico.ui.ImageLoadedListener;
import com.ntxdev.zuptecnico.ui.WebImageView;

/**
 * Created by igorlira on 7/20/15.
 */
public class ReportCategoryMarkerDownloader extends AsyncTask<Void, Void, Void> implements ImageLoadedListener {
    WebImageView imageView;
    ReportCategory category;

    Bitmap cachedImage;

    public ReportCategoryMarkerDownloader(WebImageView imageView, ReportCategory category) {
        this.imageView = imageView;
        this.category = category;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        cachedImage = Zup.getInstance().getReportCategoryService().getReportCategoryMarker(category.id);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (cachedImage != null)
            imageView.setImageBitmap(cachedImage);
        else
            imageView.setImageURL(category.getMarkerURL(), this);
    }

    @Override
    public void onImageLoaded(int resourceId) {
        Zup.getInstance().getReportCategoryService().setReportCategoryMarker(category.id,
                Zup.getInstance().getBitmap(resourceId));
    }
}