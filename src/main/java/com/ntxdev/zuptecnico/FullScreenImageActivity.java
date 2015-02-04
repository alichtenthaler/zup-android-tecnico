package com.ntxdev.zuptecnico;

import android.app.FragmentManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.entities.InventoryItem;
import com.ntxdev.zuptecnico.entities.InventoryItemImage;

import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by igorlira on 5/7/14.
 */
public class FullScreenImageActivity extends ActionBarActivity implements Runnable, ViewPager.OnPageChangeListener {
    int job_id;
    Thread worker;
    //ImageView imgDisplay;
    //PhotoViewAttacher attacher;

    ImageFragment[] fragments;

    public static class ImageFragment extends Fragment
    {
        InventoryItemImage image;
        PhotoViewAttacher attacher;
        boolean loadOnCreate = false;
        boolean isLoading = false;

        public ImageFragment setImage(InventoryItemImage image)
        {
            this.image = image;
            return this;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_fullscreen_image, container, false);
            ImageView imgDisplay = (ImageView) view.findViewById(R.id.imgDisplay);

            return view;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            if(loadOnCreate)
                load(true);
        }

        class ImageLoader extends AsyncTask<Object, Void, Object[]>
        {
            @Override
            protected Object[] doInBackground(Object... objects) {
                String url = (String) objects[0];
                ImageView imageView = (ImageView) objects[1];

                int resourceId = Zup.getInstance().requestImage(url, false);
                if(!Zup.getInstance().isResourceLoaded(resourceId))
                {
                    return new Object[] { imageView, null };
                }
                else
                {
                    return new Object[] { imageView, Zup.getInstance().getResourceBitmap(resourceId) };
                }
            }

            @Override
            protected void onPostExecute(Object[] objects) {
                ImageView imageView = (ImageView) objects[0];
                Bitmap bitmap = (Bitmap) objects[1];

                if(bitmap == null)
                {
                    imageView.setVisibility(View.INVISIBLE);
                }
                else
                {
                    imageView.setVisibility(View.VISIBLE);
                    imageView.setImageBitmap(bitmap);
                    attacher = new PhotoViewAttacher(imageView);
                }
            }
        }

        public void load(boolean bypassLoadCheck)
        {
            if(!bypassLoadCheck && isLoading)
                return;

            isLoading = true;
            if(getView() == null)
            {
                loadOnCreate = true;
                return;
            }
            ImageView imgDisplay = (ImageView) getView().findViewById(R.id.imgDisplay);

            if(image.content != null)
            {
                //webView.loadUrl("data:image/jpeg;base64," + image.content);
                byte[] imagedata = Base64.decode(image.content, Base64.NO_WRAP);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imagedata, 0, imagedata.length);

                imgDisplay.setImageBitmap(bitmap);
                imgDisplay.setVisibility(View.VISIBLE);
                attacher = new PhotoViewAttacher(imgDisplay);
            }
            else if(Zup.getInstance().isResourceLoaded(image.versions.high))
            {
                imgDisplay.setImageBitmap(Zup.getInstance().getBitmap(image.versions.high));
                imgDisplay.setVisibility(View.VISIBLE);
                attacher = new PhotoViewAttacher(imgDisplay);
            }
            else
            {
                ImageLoader loader = new ImageLoader();
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
                    loader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, image.versions.high, imgDisplay);
                } else {
                    loader.execute(image.versions.high, imgDisplay);
                }
            }
        }
    }

    class ImagePagerAdapter extends FragmentStatePagerAdapter
    {
        public ImagePagerAdapter(android.support.v4.app.FragmentManager fragmentManager)
        {
            super(fragmentManager);
        }

        @Override
        public int getCount() {
            return fragments.length;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments[position];
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        fragments[position].load(false);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_fullscreen_view);

        Zup.getInstance().initStorage(getApplicationContext());

        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        ImagePagerAdapter adapter = new ImagePagerAdapter(getSupportFragmentManager());

        Parcelable[] images = getIntent().getParcelableArrayExtra("images");
        InventoryItemImage image = getIntent().getParcelableExtra("image");

        int index = -1;
        fragments = new ImageFragment[images.length];
        for(int i = 0; i < images.length; i++)
        {
            InventoryItemImage img = (InventoryItemImage) images[i];

            if(image.equals((InventoryItemImage)images[i]))
                index = i;

            fragments[i] = new ImageFragment().setImage(img);
        }

        pager.setAdapter(adapter);
        pager.setOnPageChangeListener(this);
        pager.setCurrentItem(index, false);

        if(index >= 0 && index < fragments.length)
            fragments[index].load(false);

        this.getSupportActionBar().hide();

        /*View frame = findViewById(R.id.frame);
        InventoryItemImage image = (InventoryItemImage)getIntent().getSerializableExtra("image");
        imgDisplay = (ImageView) findViewById(R.id.imgDisplay);

        if(image.content != null)
        {
            //webView.loadUrl("data:image/jpeg;base64," + image.content);
            byte[] imagedata = Base64.decode(image.content, Base64.NO_WRAP);
            Bitmap bitmap = BitmapFactory.decodeByteArray(imagedata, 0, imagedata.length);

            imgDisplay.setImageBitmap(bitmap);
            imgDisplay.setVisibility(View.VISIBLE);
            attacher = new PhotoViewAttacher(imgDisplay);
        }
        else if(Zup.getInstance().isResourceLoaded(image.versions.high))
        {
            imgDisplay.setImageBitmap(Zup.getInstance().getBitmap(image.versions.high));
            imgDisplay.setVisibility(View.VISIBLE);
            attacher = new PhotoViewAttacher(imgDisplay);
        }
        else
        {
            job_id = Zup.getInstance().requestImage(image.versions.high);
            worker = new Thread(this);
            worker.start();
        }*/
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN)
            showHideActionBar();

        return true;
    }

    void showHideActionBar()
    {
        final View container = findViewById(R.id.back_container);
        AlphaAnimation animation;
        if(container.getVisibility() == View.VISIBLE)
        {
            animation = new AlphaAnimation(1, 0);
            animation.setDuration(350);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    container.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }
        else
        {
            animation = new AlphaAnimation(1, 0);
            animation.setDuration(10);
            container.startAnimation(animation);

            container.setVisibility(View.VISIBLE);
            animation = new AlphaAnimation(0, 1);
            animation.setDuration(350);
        }

        container.startAnimation(animation);
    }

    public void back(View view)
    {
        finish();
    }

    void imageLoaded()
    {

    }

    @Override
    public void run() {
        /*while(!Zup.getInstance().isResourceLoaded(job_id))
        {
            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException ex) { break; }
        }

        if(Zup.getInstance().isResourceLoaded(job_id))
        {
            Zup.runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    imgDisplay.setImageBitmap(Zup.getInstance().getResourceBitmap(job_id));
                    imgDisplay.setVisibility(View.VISIBLE);
                    attacher = new PhotoViewAttacher(imgDisplay);
                }
            });
        }*/
    }
}
