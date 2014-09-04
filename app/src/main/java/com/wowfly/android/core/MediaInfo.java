package com.wowfly.android.core;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.provider.MediaStore;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.wowfly.wowyun.wowyun_mobile.R;
import com.wowfly.wowyun.wowyun_mobile.WebAlbumActivity;

import org.apache.http.Header;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class MediaInfo {
    public static class ImageInfo {
        int _id;
        String path;
        String name;
    }

    public static class VideoInfo {
        String path;
        String name;
        int _id;
    }

    public static class WebAlbumInfo {
        String mime;
        String thumbpath;
        String imagepath;
    }

    private static final String TAG = "MediaInfo";
    private ContentResolver mCR;
    private ArrayList <VideoInfo> mVideoInfo;
    private ArrayList<ImageInfo> mImageInfo;
    private ArrayList<WebAlbumInfo> mWebAlbumInfo;
    private AsyncHttpClient mHttpClient;

    private Cursor mVideoCursor;
    private Cursor mImageCursor;
    private int mImageNum, mVideoNum;
    private List<Map<String,Object>> mImageBucketList;
    private List<Map<String,Object>> mVideoBucketList;

    private static String[] mVidProj = {
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME
    };

    private static String[] mImgProj = {
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME
    };

    private static String[] mVidBucketProj = {
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME
    };
    private static String[] mImgBucketProj = {
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME
    };

/*    private AsyncHttpResponseHandler mHttpHandler = new AsyncHttpResponseHandler() {
        public void onStart() {
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            //Log.i(TAG, new String(responseBody));
            ByteArrayInputStream is = new ByteArrayInputStream(responseBody);
            if(parseXMLResp(is)) {
                View layout = findViewById(R.id.webalbum_status_layout);
                layout.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
                //Log.i(TAG, " WEBALBUM_LOAD_SUCCESS, do rendering webalbum now");
                ((GridView) listView).setAdapter(new WebAlbumImageAdapter());
            } else {
                TextView status = (TextView) findViewById(R.id.webalbum_status_text);
                status.setText(getResources().getString(R.string.action_fetch_webalbum_failure));
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Log.i(TAG, " statusCode = " + statusCode);
            TextView status = (TextView) findViewById(R.id.webalbum_status_text);
            ProgressBar bar = (ProgressBar) findViewById(R.id.webalbum_status_progressbar);

            if(statusCode == 404) {
                status.setText(getResources().getString(R.string.text_webalbum_404));
            } else {
                status.setText(getResources().getString(R.string.action_fetch_webalbum_failure));
            }
            bar.setVisibility(View.INVISIBLE);
        }
    };*/

    public MediaInfo(ContentResolver cr) {
        mCR = cr;
        mImageInfo = new ArrayList<ImageInfo>();
        mVideoInfo = new ArrayList<VideoInfo>();
        mWebAlbumInfo = new ArrayList<WebAlbumInfo>();
        mImageBucketList = new ArrayList<Map<String, Object>>();
        mVideoBucketList = new ArrayList<Map<String, Object>>();
    }

    public boolean getWebAlbumInfo(InputStream resp) {
        try {
            XmlPullParserFactory parserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserFactory.newPullParser();
            parser.setInput(resp, "utf-8");
            //parser.set
            int type = parser.getEventType();
            while (type != XmlPullParser.END_DOCUMENT) {
                switch (type) {
                    case XmlPullParser.START_DOCUMENT:
                        //Log.i(TAG, "Start Document");
                        break;
                    case XmlPullParser.START_TAG:
                        //Log.i(TAG, "start tag = " + parser.getName());
                        if (parser.getName().equals("item")) {
                            WebAlbumInfo waItem = new WebAlbumInfo();
                            waItem.mime = parser.getAttributeValue(0);
                            waItem.thumbpath = parser.getAttributeValue(1);
                            waItem.imagepath = parser.getAttributeValue(2);
                            mWebAlbumInfo.add(waItem);
                            //Log.i(TAG, " mime= " + waItem.mime + " thumb= " + waItem.thumbpath + " image= " + waItem.imagepath);
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        //Log.i(TAG, "end tag = " + parser.getName());
                        break;
                    case XmlPullParser.TEXT:
                        break;
                }
                parser.next();
                type = parser.getEventType();
            }
            return  true;
        } catch (XmlPullParserException e) {
            Log.i(TAG, " XmlPullParserException ");
            return false;
        } catch (IOException e) {
            Log.i(TAG, " IOException ");
            return false;
        }
    }

    public int getVideoCount() {
        return mVideoNum;
    }

    public int getImageCount() {
        return mImageNum;
    }

    public int getWebAlbumMediaCount() {
        return mWebAlbumInfo.size();
    }

    public String getImageUri(int pos) {
        return "file://" + mImageInfo.get(pos).path;
    }

    public String getImagePath(int pos) {return mImageInfo.get(pos).path; }

    public String getImageName(int pos) {
        return mImageInfo.get(pos).name;
    }

    public String getVideoUri(int pos) {
        return "file://" + mVideoInfo.get(pos).path;
    }
    public String getVideoPath(int pos) {
        return mVideoInfo.get(pos).path;
    }

    public String getWebAlbumMediaUri(int pos) {
        return mWebAlbumInfo.get(pos).imagepath;
    }

    public String getWebAlbumMediaPath(int pos) {
        return mWebAlbumInfo.get(pos).imagepath;
    }

    public String getWebAlbumMediaThumb(int pos) {
        return mWebAlbumInfo.get(pos).thumbpath;
    }

    public String getWebAlbumMediaMime(int pos) {
        return mWebAlbumInfo.get(pos).mime;
    }

    public String getWebAlbumMediaName(int pos) {
        String path = mWebAlbumInfo.get(pos).imagepath;
        int sep = path.lastIndexOf('/');
        String name = path.substring(sep+1);
        return name;
    }

    public int getImageId(int pos) {
        if(mImageInfo.size() > 0) {
            return mImageInfo.get(pos)._id;
        } else {
            return 0;
        }

    }

    public int getVideoId(int pos) {
        if(mVideoInfo.size() > 0) {
            return mVideoInfo.get(pos)._id;
        }else {
            return 0;
        }
    }

    public Bitmap getVideoBitmap(int pos) {
        return null;
    }

    private boolean isBucketInList(String bucket, boolean isImage) {
        int n = 0;
        if(isImage)
            n = mImageBucketList.size();
        else
            n = mVideoBucketList.size();

        for(int idx = 0; idx < n; idx++) {
            if(isImage) {
                Map<String, Object> item = mImageBucketList.get(idx);
                if (item.get(new String("bucket")).toString().equals(bucket)) {
                    return true;
                }
            } else {
                Map<String, Object> item = mVideoBucketList.get(idx);
                if (item.get(new String("bucket")).toString().equals(bucket)) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<Map<String,Object>> getBucketList(boolean isImage, SharedPreferences pref) {
        Cursor c;
        String index;
        Map<String, Object> item;

        if(isImage) {
            mImageBucketList.clear();
        } else {
            mVideoBucketList.clear();
        }

        if(isImage) {
            c = mCR.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, mImgBucketProj, null, null, MediaStore.Images.Media.DEFAULT_SORT_ORDER);
            index = MediaStore.Images.Media.BUCKET_DISPLAY_NAME;
        } else {
            c = mCR.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, mVidBucketProj, null, null, MediaStore.Video.Media.DEFAULT_SORT_ORDER);
            index = MediaStore.Video.Media.BUCKET_DISPLAY_NAME;
        }

        Log.i(TAG, " isImage = " + isImage + " bucket total = " + c.getCount());
        c.moveToFirst();
        int bucketCol = c.getColumnIndexOrThrow(index);
        Set<String> filter;

        if(isImage)
            filter = pref.getStringSet("images.bucket.filter", null);
        else
            filter = pref.getStringSet("video.bucket.filter", null);

        if(filter != null) {
            Log.i(TAG, "isImage "+ isImage + " filter = " + filter.toString());
        }

        do {
            String bucket = c.getString(bucketCol);
            item = new HashMap<String, Object>();
            if(isBucketInList(bucket, isImage) == false) {
                //Log.i(TAG, " add bucket = " + bucket);
                item.put("icon", R.drawable.ic_folder);
                item.put("bucket", bucket);
                if(filter == null) {
                    item.put("checked", true);
                } else {
                    if(filter.contains(bucket)) {
                        item.put("checked", true);
                    } else {
                        item.put("checked", false);
                    }
                }
                if(isImage)
                    mImageBucketList.add(item);
                else
                    mVideoBucketList.add(item);
                Log.i(TAG, "isImage = " + isImage + " add bucketName = " + bucket);
            }
        } while(c.moveToNext());

        c.close();
        return isImage? mImageBucketList : mVideoBucketList;
    }

/*    public ArrayList fetchWebAlbumInfo(String jid, SharedPreferences pref, AsyncHttpResponseHandler httpHandler) {
        mHttpClient.get("http://101.69.230.238:8080/list/"+jid, httpHandler);

        return null;
    }*/

    public ArrayList getVideosInfo(SharedPreferences pref) {
        Cursor c;
        String name, path;
        BitmapFactory.Options opt = new BitmapFactory.Options();

        opt.inDither = false;
        opt.inPreferredConfig = Bitmap.Config.RGB_565;

        Set<String> filter = pref.getStringSet("video.bucket.filter", null);
        if(filter != null) {
            Log.i(TAG, "video.bucket.filter = " + filter.toString());
        }

        if(filter != null) {
            String cond = "";

            if(filter.size() == 0) {
                mVideoInfo.clear();
                mVideoNum = 0;
                return null;
            }

            for(String folder: filter) {
                if(cond.length() > 0)
                    cond = cond + " or " + MediaStore.Video.Media.BUCKET_DISPLAY_NAME + " like '%" + folder + "%' ";
                else
                    cond = MediaStore.Video.Media.BUCKET_DISPLAY_NAME + " like '%" + folder + "%' ";
            }
            Log.i(TAG, "cond = " + cond);
            c = mCR.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, mVidProj, cond, null, MediaStore.Video.Media.DEFAULT_SORT_ORDER);
        } else {
            c = mCR.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, mVidProj, null, null, MediaStore.Video.Media.DEFAULT_SORT_ORDER);
        }

        mVideoInfo.clear();
        int total = c.getCount();
        mVideoNum = total;
        Log.i(TAG, "total video :" + total);
        if(total <= 0) {
            c.close();
            return null;
        }

        c.moveToFirst();

        int idCol = c.getColumnIndex(MediaStore.MediaColumns._ID);
        int dataCol = c.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        int nameCol = c.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME);
        //int bucketCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);


        do {
            VideoInfo vi = new VideoInfo();
            name = c.getString(nameCol);
            path = c.getString(dataCol);
            vi._id = c.getInt(idCol);
            vi.name = name;
            vi.path = path;
            mVideoInfo.add(vi);

            //Log.i(TAG, "id = " + vi._id + " name = " + name + " path = " + path );
        } while(c.moveToNext());

        c.close();
        return null;
    }

    public ArrayList getImagesInfo(SharedPreferences pref) {
        Cursor c;
        String name;
        String date, path;
        int id;

        Set<String> filter = pref.getStringSet("images.bucket.filter", null);
        if(filter != null) {
            Log.i(TAG, "images.bucket.filter = " + filter.toString());
        }

        if(filter != null) {
            String cond = "";
            if(filter.size() == 0) {
                mImageInfo.clear();
                mImageNum = 0;
                return null;
            }

            for(String folder: filter) {
                if(cond.length() > 0)
                    cond = cond + " or " + MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " like '%" + folder + "%' ";
                else
                    cond = MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " like '%" + folder + "%' ";
            }
            Log.i(TAG, "cond = " + cond);
            c = mCR.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, mImgProj, cond, null, MediaStore.Images.Media.DEFAULT_SORT_ORDER);
        } else {
            c = mCR.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, mImgProj, null, null, MediaStore.Images.Media.DEFAULT_SORT_ORDER);
        }

        mImageInfo.clear();
        int total = c.getCount();
        mImageNum = total;
        if(total <= 0) {
            c.close();
            return null;
        }
        Log.i(TAG, "total thumbnail: " + total);
        c.moveToFirst();

        int idColumn = c.getColumnIndexOrThrow(MediaStore.MediaColumns._ID);
        int dataColumn = c.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        int nameColumn = c.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
        int dateColumn = c.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);
        //int bucketColumn = c.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);

        do {
            name = c.getString(nameColumn);
            date = c.getString(dateColumn);
            path = c.getString(dataColumn);
            id = c.getInt(idColumn);
            if(id == 0) continue;

            //Log.i(TAG, "id = " + id + " name = " + name + " path = " + path);
            ImageInfo ii = new ImageInfo();
            ii.name = name;
            ii.path = path;
            ii._id = id;
            mImageInfo.add(ii);

        } while(c.moveToNext());

        c.close();
        return null;
    }
}