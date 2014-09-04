package com.wowfly.android.core;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.nostra13.universalimageloader.core.decode.BaseImageDecoder;
import com.nostra13.universalimageloader.core.decode.ImageDecoder;
import com.nostra13.universalimageloader.core.decode.ImageDecodingInfo;

import java.io.IOException;

/**
 * Created by user on 8/19/14.
 */
public class SmartUriDecoder implements ImageDecoder {
    private static final String TAG = "SmartUriDecoder";
    private final ContentResolver mResolver;
    private final BaseImageDecoder mImageUriDecoder;

    public SmartUriDecoder(ContentResolver resolver, BaseImageDecoder imageDecoder) {
        if(imageDecoder == null) {
            throw  new NullPointerException("Image decoder is null");
        }

        mResolver = resolver;
        mImageUriDecoder = imageDecoder;
    }

    public Bitmap decode(ImageDecodingInfo info) throws IOException {
        if(TextUtils.isEmpty(info.getImageKey())) {
            return null;
        }

        String uri = info.getImageKey().replaceFirst("_\\d+x\\d+$", "");
        //Log.i(TAG, "ImageKey = " + info.getImageKey() + "uri = " + uri);

        if(uri.startsWith("http://")) {
            return mImageUriDecoder.decode(info);
        }

        if(isVideoUri(uri)) {
            return makeVideoThumbnail(uri.substring(6));
        } else {
            return makeImageThumbnail(uri.substring(6));
        }
        //mImageUriDecoder.decode();
    }

    private Bitmap makeImageThumbnail(String imagepath) {
        if(imagepath == null) {
            return null;
        }
        BitmapFactory.Options opt = new BitmapFactory.Options();

        opt.inDither = false;
        opt.inPreferredConfig = Bitmap.Config.RGB_565;

        //Log.i(TAG, "make Image " + imagepath);
        return MediaStore.Images.Thumbnails.getThumbnail(mResolver, Integer.parseInt(imagepath), MediaStore.Images.Thumbnails.MINI_KIND, opt);
    }

    private Bitmap makeVideoThumbnail(String videopath) {
        if(videopath == null) {
            return null;
        }

        BitmapFactory.Options opt = new BitmapFactory.Options();

        opt.inDither = false;
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        //Log.i(TAG, "make Image " + videopath);
        return MediaStore.Video.Thumbnails.getThumbnail(mResolver, Integer.parseInt(videopath), MediaStore.Images.Thumbnails.MINI_KIND, opt);
    }

    private boolean isVideoUri(String uri) {
        //Log.i(TAG, "isVideoUri = " + uri);

        return uri.startsWith("video/");
    }

    private String getVideoFilePath(Uri uri) {
        String col = MediaStore.Video.VideoColumns.DATA;
        Cursor c = mResolver.query(uri, new String[] { col }, null, null, null);

        try {
            int dataidx = c.getColumnIndex(col);
            if(dataidx != -1 && c.moveToFirst())
                return c.getString(dataidx);
        }
        finally {
            c.close();
        }
        return null;
    }
}
