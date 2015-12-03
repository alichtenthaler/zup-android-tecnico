package com.ntxdev.zuptecnico.util;

import android.content.Context;
import android.os.Environment;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import okio.BufferedSink;
import okio.Okio;

public class FileUtils {

    public static boolean imageExists(Context context, String filename) {
        File imagesFolder = getImagesFolder(context);
        if (!imagesFolder.exists()) {
            imagesFolder.mkdirs();
        }

        return new File(imagesFolder, filename).exists();
    }

    public static boolean imageExists(Context context, String subfolder, String filename) {
        File imagesFolder = getImagesFolder(context, subfolder);
        if (!imagesFolder.exists()) {
            imagesFolder.mkdirs();
        }

        return new File(imagesFolder, filename).exists();
    }

    public static File getImagesFolder(Context context) {
        return new File(context.getFilesDir() + File.separator + "images" + File.separator + "images");
    }

    public static File getImagesFolder(Context context, String subfolder) {
        return new File(context.getFilesDir() + File.separator + "images" + File.separator + "images" + File.separator + subfolder);
    }

    public static File getTempImagesFolder() {
        File imagesFolder = new File(Environment.getExternalStorageDirectory() + File.separator + "ZUP" + File.separator + "temp");
        if (!imagesFolder.exists()) {
            imagesFolder.mkdirs();
        }

        return imagesFolder;
    }

}

