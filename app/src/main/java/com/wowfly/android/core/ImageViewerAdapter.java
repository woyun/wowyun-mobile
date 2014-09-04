package com.wowfly.android.core;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wowfly.wowyun.wowyun_mobile.R;

import org.w3c.dom.Text;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;

import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by user on 8/18/14.
 */
public class ImageViewerAdapter extends PagerAdapter{
    private static final String TAG = "ImageViewerAdapter";
    private Activity _activity;
    private LayoutInflater inflater;
    private MediaInfo mInfo;
    private static Bitmap mBitmap = null;
    private boolean isWebAlbum = false;
    private PhotoViewAttacher mAttacher;

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
            mAttacher.update();
        }
    }

    public ImageViewerAdapter(Activity activity, MediaInfo mi, boolean webalbum) {
        this._activity = activity;
        mInfo = mi;
        isWebAlbum = webalbum;
    }

    public int getCount() {
        if(isWebAlbum)
            return mInfo.getWebAlbumMediaCount();
        else
            return mInfo.getImageCount();//.get._imageList.size();
    }

    public boolean isViewFromObject(View view, Object obj) {
        return view == ((RelativeLayout) obj);
    }

    public Object instantiateItem(ViewGroup container, int position) {
        ImageView imageView;
        TextView tips, filename;
        int count = mInfo.getImageCount();

        if(mBitmap != null) {
            System.gc();
            mBitmap.recycle();
            mBitmap = null;
        }
        if(isWebAlbum) {
            count = mInfo.getWebAlbumMediaCount();
        }
        inflater = (LayoutInflater)_activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewLayout = inflater.inflate(R.layout.layout_imageviewer, container, false);
        imageView = (ImageView) viewLayout.findViewById(R.id.imageholder);

        mAttacher = new PhotoViewAttacher(imageView);
        filename = (TextView) viewLayout.findViewById(R.id.imagefilename);
        if(isWebAlbum)
            filename.setText(mInfo.getWebAlbumMediaName(position));
        else
            filename.setText(mInfo.getImageName(position));

        tips = (TextView) viewLayout.findViewById(R.id.imagetips);
        tips.setText((position+1) + "/" + count);

        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
        opt.inSampleSize = 2;
        Bitmap bitmap = null;

        if(isWebAlbum == false) {
            try {
                bitmap = BitmapFactory.decodeFile(mInfo.getImagePath(position), opt);
                imageView.setImageBitmap(bitmap);
                mAttacher.update();
            } catch (OutOfMemoryError e0) {
                System.gc();
                try {
                    bitmap = BitmapFactory.decodeFile(mInfo.getImagePath(position), opt);
                    imageView.setImageBitmap(bitmap);
                    mAttacher.update();
                } catch (OutOfMemoryError e1) {
                    Log.e(TAG, "Bitmap decode really out of memory");
                }
            }
        } else {
            String url = mInfo.getWebAlbumMediaPath(position);
            String mime = mInfo.getWebAlbumMediaMime(position);
            Log.i(TAG, " start web album viewer " + url + " mime " + mime);
            if(mime.startsWith("image/")) {
                url = "http://101.69.230.238:8081/" + url;
                new DownloadImageTask((ImageView) viewLayout.findViewById(R.id.imageholder)).execute(url);
            }
        }

        ((ViewPager) container).addView(viewLayout);
        return  viewLayout;
    }

    public void destroyItem(ViewGroup container, int position, Object obj) {
        ((ViewPager)container).removeView((RelativeLayout) obj);
    }
}
