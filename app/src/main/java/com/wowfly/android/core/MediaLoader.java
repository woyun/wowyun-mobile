package com.wowfly.android.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.decode.BaseImageDecoder;
import com.nostra13.universalimageloader.core.decode.ImageDecoder;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.wowfly.wowyun.wowyun_mobile.R;

import org.jivesoftware.smack.Connection;

import java.io.File;

/**
 * Created by user on 8/15/14.
 */
public class MediaLoader {
    private static final String TAG = "MediaLoader";
    private MediaInfo mInfo = null;
    private File mCacheDir;
    private ImageLoaderConfiguration mConfig;
    private Context mContext;
    private DisplayImageOptions mOption;
    private ImageLoader imgLoader = ImageLoader.getInstance();

    public MediaLoader(Context context) {
        mContext = context;
//        mCacheDir = StorageUtils.getCacheDirectory(context);
        mInfo = new MediaInfo(context.getContentResolver());
    }

    public void setup(){

        ImageDecoder smartUriDecoder = new SmartUriDecoder(mContext.getContentResolver(), new BaseImageDecoder(false));

        mConfig = new ImageLoaderConfiguration.Builder(mContext)
                .denyCacheImageMultipleSizesInMemory()
                .taskExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                .imageDecoder(smartUriDecoder)
                .threadPoolSize(5)
                .taskExecutorForCachedImages(AsyncTask.THREAD_POOL_EXECUTOR)
                .threadPriority(5)
                .tasksProcessingOrder(QueueProcessingType.FIFO)
                .memoryCache(new WeakMemoryCache())
                .memoryCacheSize(2 * 1024 * 1024)
                .memoryCacheSizePercentage(32)
                .imageDownloader(new BaseImageDownloader(mContext))
                .defaultDisplayImageOptions(DisplayImageOptions.createSimple())
                .build();

        //mConfig = ImageLoaderConfiguration.createDefault(mContext);


        mOption = new DisplayImageOptions.Builder()
                //.showImageOnLoading(R.drawable.ic_stub)
                .cacheInMemory(true)
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new SimpleBitmapDisplayer())
                .build();
        imgLoader.init(mConfig);
    }

    public void reset() {
        imgLoader.clearMemoryCache();
        imgLoader.clearDiskCache();
    }

    public MediaInfo getMediaInfo() {
        return mInfo;
    }

    public void displayImage(int pos, ImageView view, SimpleImageLoadingListener listener) {
        imgLoader.displayImage("image/"+mInfo.getImageId(pos), view, mOption, listener);
    }

    public void displayVideo(int pos, ImageView view, SimpleImageLoadingListener listener) {
        //Log.i(TAG, "Video Uri = " + mInfo.getVideoUri(pos));
        imgLoader.displayImage("video/"+mInfo.getVideoId(pos),view, mOption, listener);
    }
}
