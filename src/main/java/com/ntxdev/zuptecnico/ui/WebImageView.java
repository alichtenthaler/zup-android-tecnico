package com.ntxdev.zuptecnico.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.ntxdev.zuptecnico.api.Zup;

/**
 * Created by igorlira on 7/18/15.
 */
public class WebImageView extends ImageView implements ImageLoadedListener {
    private int resourceId = -1;
    private ImageLoadedListener callback;

    public WebImageView(Context context) {
        super(context);
    }

    public WebImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WebImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public WebImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public int setImageURL(String url, ImageLoadedListener callback) {
        this.callback = callback;
        return this.resourceId = Zup.getInstance().requestImage(url, true, this);
    }

    @Override
    public void onImageLoaded(int resourceId) {
        if(resourceId == this.resourceId) {
            this.setImageBitmap(Zup.getInstance().getBitmap(resourceId));

            if(this.callback != null)
                this.callback.onImageLoaded(this.resourceId);
        }
    }
}
