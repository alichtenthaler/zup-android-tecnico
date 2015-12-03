package com.ntxdev.zuptecnico.tasks;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.ViewGroup;

import com.ntxdev.zuptecnico.R;
import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.entities.ReportCategory;
import com.ntxdev.zuptecnico.ui.ImageLoadedListener;
import com.ntxdev.zuptecnico.ui.WebImageView;

/**
 * Created by igorlira on 7/20/15.
 */
public class ReportCategoryIconDownloader extends AsyncTask<Void, Void, Void> implements ImageLoadedListener {
    WebImageView imageView;
    ReportCategory category;

    Bitmap cachedImage;

    public ReportCategoryIconDownloader(WebImageView imageView, ReportCategory category) {
        this.imageView = imageView;
        this.category = category;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        cachedImage = Zup.getInstance().getReportCategoryService().getReportCategoryIcon(category.id);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (cachedImage != null)
            imageView.setImageBitmap(cachedImage);
        else
            imageView.setImageURL(category.icon.retina.mobile.active, this);
    }

    @Override
    public void onImageLoaded(int resourceId) {
        Zup.getInstance().getReportCategoryService().setReportCategoryIcon(category.id,
                Zup.getInstance().getBitmap(resourceId));
    }
}