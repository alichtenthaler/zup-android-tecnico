package com.ntxdev.zuptecnico.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.DisplayMetrics;

import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.entities.InventoryCategory;
import com.ntxdev.zuptecnico.entities.MapCluster;

/**
 * Created by igorlira on 5/8/15.
 */
public class BitmapUtil
{
    public static Bitmap getMapClusterBitmap(MapCluster cluster, DisplayMetrics metrics)
    {
        String color = Zup.getInstance().getInventoryCateGoryColor(cluster.category_id);
        //InventoryCategory category = Zup.getInstance().getInventoryCategory(cluster.category_id);

        String s = Integer.toString(cluster.count);
        Rect bounds = new Rect();

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(16 * metrics.density);
        paint.setColor(0xffffffff);
        paint.getTextBounds(s, 0, s.length(), bounds);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextAlign(Paint.Align.CENTER);

        Paint borderPaint = new Paint();
        borderPaint.setAntiAlias(true);
        borderPaint.setColor(0xffffffff);

        Paint fillPaint = new Paint();
        fillPaint.setAntiAlias(true);
        fillPaint.setColor(0xff2ab4dc);
        if(color != null)
        {
            fillPaint.setColor(Color.parseColor(color));
        }

        int border = (int)(5 * metrics.density);
        int padding = (int)(10 * metrics.density);
        int size = Math.max(bounds.width(), bounds.height())  + border * 2 + padding * 2;

        Bitmap result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);

        canvas.drawCircle(size/2, size/2, size / 2, borderPaint);
        canvas.drawCircle(size/2, size/2, (size - border*2)/2, fillPaint);

        int yPos = (int) ((size / 2) - ((paint.descent() + paint.ascent()) / 2)) ;
        canvas.drawText(s, size / 2, yPos, paint);

        return result;
    }
}
