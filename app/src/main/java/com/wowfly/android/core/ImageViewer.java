package com.wowfly.android.core;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.wowfly.wowyun.wowyun_mobile.R;

import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by user on 8/18/14.
 */
public class ImageViewer extends Activity {
    private ImageViewerAdapter adapter;
    private ViewPager viewPager;
    private MediaInfo mInfo = null;
    private SharedPreferences mPref;
    private boolean isWebAlbum = false;

    protected void onCreate(Bundle saved) {
        WowYunApp _app = (WowYunApp) getApplication();

        super.onCreate(saved);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_fullscreen_view);
        viewPager = (ViewPager) findViewById(R.id.imageviewer);
        viewPager.setPageTransformer(true, new DepthPageTransformer());
        //viewPager.s
        //getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Intent i = getIntent();
        int pos = i.getIntExtra("position", 0);
        if(pos < 0) {
            Log.i("ImageViewer", " activity for web album image viewer");
            pos = -pos-1;
            isWebAlbum = true;
        }

        mPref = getSharedPreferences("wowyun-mobile", Context.MODE_PRIVATE);


        mInfo = _app.getMediaInfo();
        adapter = new ImageViewerAdapter(ImageViewer.this, mInfo, isWebAlbum);
/*        if(isWebAlbum == false) {
            //mInfo = new MediaInfo(this.getContentResolver());
            //mInfo.getImagesInfo(mPref);

            adapter = new ImageViewerAdapter(ImageViewer.this, mInfo);
        } else {

        }*/
        viewPager.setAdapter(adapter);

        viewPager.setCurrentItem(pos);
        //viewPager.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        //viewPager.setAlpha();
    }
}
