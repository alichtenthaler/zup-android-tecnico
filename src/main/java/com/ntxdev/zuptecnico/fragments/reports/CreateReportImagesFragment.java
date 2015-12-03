package com.ntxdev.zuptecnico.fragments.reports;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.ntxdev.zuptecnico.R;
import com.ntxdev.zuptecnico.ui.WebImageView;
import com.ntxdev.zuptecnico.util.FileUtils;
import com.ntxdev.zuptecnico.util.Utilities;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

import eu.janmuller.android.simplecropimage.CropImage;

/**
 * Created by igorlira on 7/20/15.
 */
public class CreateReportImagesFragment extends Fragment {
    private static final int CAMERA_RETURN = 1406;
    private static final int CROP_RETURN = 1407;
    private static final int GALLERY_RETURN = 1408;

    ArrayList<Image> images;
    ViewGroup imagesListLayout;
    View imageLayout;
    private String tempImagePath;

    class Image {
        public File file;
        public String url;
        public View view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null){
            tempImagePath = savedInstanceState.getString("image_path");
        }
        images = new ArrayList<>();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(tempImagePath != null){
            outState.putString("image_path", tempImagePath);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_report_images, container, false);
        init(view);
        return view;
    }

    public int getAddableCount() {
        int count = 0;
        for (int i = 0; i < images.size(); i++) {
            if (images.get(i).file != null) {
                count++;
            }
        }

        return count;
    }

    public int getCount() {
        return images.size();
    }

    public File getItem(int i) {
        return images.get(i).file;
    }

    public String getItemBase64(int i) {
        if (getItem(i) == null) // It's a pre-added image
            return null;

        return Utilities.encodeBase64(getItem(i).getPath());
    }

    void init(View view) {
        View scroll = view.findViewById(R.id.report_images_scroll);
        scroll.setVisibility(images.size() > 0 ? View.VISIBLE : View.GONE);

        imagesListLayout = (ViewGroup) view.findViewById(R.id.report_images_container);

        View addButton = view.findViewById(R.id.report_addimage);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickImage();
            }
        });
    }

    private void inflateImageLayout() {
        View scroll = getView().findViewById(R.id.report_images_scroll);
        scroll.setVisibility(View.VISIBLE);

        imageLayout = LayoutInflater.from(getActivity()).inflate(R.layout.report_item_create_image, imagesListLayout, false);
        ((ProgressBar) imageLayout.findViewById(R.id.progressBar6)).setVisibility(View.VISIBLE);
        imagesListLayout.addView(imageLayout);
    }

    private void dismissImageLayout() {
        if (imageLayout != null) {
            imagesListLayout.removeView(imageLayout);
            imageLayout = null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        inflateImageLayout();
        if (resultCode != Activity.RESULT_OK) {
            dismissImageLayout();
            return;
        }

        switch (requestCode) {
            case CROP_RETURN:
                String path = data.getStringExtra(CropImage.IMAGE_PATH);
                if (path == null) {
                    dismissImageLayout();
                    return;
                }
                final File file = new File(path);
                addImage(file);
                break;
            case GALLERY_RETURN:
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                if (selectedImage == null) {
                    return;
                }
                Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                if (cursor == null) {
                    return;
                }
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();
                Uri tempImage = Uri.fromFile(new File(picturePath));
                tempImagePath = tempImage.getPath();
            case CAMERA_RETURN:
                Intent intent = new Intent(getActivity(), CropImage.class);
                intent.putExtra(CropImage.IMAGE_PATH, tempImagePath);
                intent.putExtra(CropImage.SCALE, true);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(FileUtils.getTempImagesFolder(), System.currentTimeMillis() + ".jpg")));
                intent.putExtra(CropImage.ASPECT_X, 1);
                intent.putExtra(CropImage.ASPECT_Y, 1);
                intent.putExtra(CropImage.OUTPUT_X, 800);
                intent.putExtra(CropImage.OUTPUT_Y, 800);
                tempImagePath = null;

                startActivityForResult(intent, CROP_RETURN);
                break;
        }
    }

    void pickImage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Adicionar imagem");
        builder.setItems(new String[]{"Da galeria", "Da câmera"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, GALLERY_RETURN);
                } else {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    Uri tempImage = Uri.fromFile(new File(FileUtils.getTempImagesFolder(), "tmp_image_" + String.valueOf(System.currentTimeMillis()) + ".jpg"));
                    tempImagePath = tempImage.getPath();
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, tempImage);
                    intent.putExtra("return-data", true);
                    startActivityForResult(intent, CAMERA_RETURN);
                }
            }
        });
        builder.show();
    }

    public void addImage(File file) {
        Image img = new Image();
        img.file = file;

        addImage(img);
    }

    public void addImage(String url) {
        Image img = new Image();
        img.url = url;

        addImage(img);
    }

    void addImage(Image img) {
        boolean wasNull = false;
        if(imageLayout == null){
            imageLayout = LayoutInflater.from(getActivity()).inflate(R.layout.report_item_create_image, imagesListLayout, false);
            wasNull = true;
        }
        WebImageView imageView = (WebImageView) imageLayout.findViewById(R.id.report_image);
        View removeButton = imageLayout.findViewById(R.id.report_image_remove);
        removeButton.setClickable(true);
        removeButton.setTag(img); // index
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeImage((Image) view.getTag());
            }
        });

        ((ProgressBar) imageLayout.findViewById(R.id.progressBar6)).setVisibility(View.GONE);

        if (img.file != null) {
            imageView.setImageURI(Uri.fromFile(img.file));
        } else {
            Picasso.with(getActivity().getApplicationContext()).load(img.url).into(imageView);
            removeButton.setVisibility(View.GONE);
        }
        img.view = imageLayout;
        this.images.add(img);
        if(wasNull){
            imagesListLayout.addView(imageLayout);
        }
    }

    void removeImage(Image image) {
        this.images.remove(image);

        View view = image.view;

        imagesListLayout.removeView(view);

        View scroll = getView().findViewById(R.id.report_images_scroll);
        scroll.setVisibility(images.size() > 0 ? View.VISIBLE : View.GONE);
    }
}
