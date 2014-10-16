package com.wowfly.wowyun.wowyun_mobile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.decode.BaseImageDecoder;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.wowfly.android.core.GridItem;
import com.wowfly.android.core.ImageViewer;
import com.wowfly.android.core.MediaInfo;
import com.wowfly.android.core.WowYunApp;

import org.apache.http.Header;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by user on 9/1/14.
 */
public class WebAlbumActivity extends Activity  {
    private static final String TAG = "WebAlbumActivity";
    private AsyncHttpClient mHttpClient;
    private GridView listView;
    private ArrayList<WebAlbumInfo> waInfo;

    private ImageLoaderConfiguration mConfig;
    private Context mContext;
    private DisplayImageOptions mOption;
    private ImageLoader imgLoader = ImageLoader.getInstance();
    private Handler mHandler;
    private MediaInfo mInfo;

    public static class WebAlbumInfo {
        String mime;
        String thumbpath;
        String imagepath;
    }

    public static class WebAlbumViewHolder {
        ImageView imageView;
    }

    public WebAlbumActivity() {
        mHttpClient = new AsyncHttpClient();
        waInfo = new ArrayList<WebAlbumInfo>();
    }

/*    private boolean parseXMLResp(InputStream resp) {
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
                            waInfo.add(waItem);
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
    }*/

    private AsyncHttpResponseHandler mHttpHandler = new AsyncHttpResponseHandler() {
        public void onStart() {
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            Log.i(TAG, new String(responseBody));
            ByteArrayInputStream is = new ByteArrayInputStream(responseBody);
            if(mInfo.getWebAlbumInfo(is)) {
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
    };


    protected void onCreate(Bundle saved) {
        super.onCreate(saved);

        WowYunApp _app = (WowYunApp) getApplication();
        mInfo = _app.getMediaInfo();
        mContext = getBaseContext();
        setContentView(R.layout.activity_webalbum);
        listView = (GridView) findViewById(R.id.webalbum_gridview);
        listView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(mContext, ImageViewer.class);
                intent.putExtra("position", -i-1);
                startActivity(intent);
            }
        });
        //listView.setMultiChoiceModeListener(this);
        final TextView status = (TextView) findViewById(R.id.webalbum_status_text);
        status.setText(getResources().getString(R.string.action_fetch_webalbum));

        Intent intent = getIntent();
        String jid = intent.getStringExtra("jid");
        Log.i(TAG, " webalbum jid = " + jid);
        int sep = jid.indexOf('@');
        String _jid = jid.substring(0, sep);
        this.setTitle(_jid + "的云端相册");

        mConfig = new ImageLoaderConfiguration.Builder(mContext)
                .taskExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                .threadPoolSize(3)
                .tasksProcessingOrder(QueueProcessingType.FIFO)
                .memoryCache(new WeakMemoryCache())
                .imageDownloader(new BaseImageDownloader(mContext))
                .imageDecoder(new BaseImageDecoder(true))
                .memoryCacheSize(2 * 1024 * 1024)
                .memoryCacheSizePercentage(32)
                .defaultDisplayImageOptions(DisplayImageOptions.createSimple())
                .build();

        mOption = new DisplayImageOptions.Builder()
                //.showImageOnLoading(R.drawable.ic_stub)
                .cacheInMemory(true)
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new SimpleBitmapDisplayer())
                .build();

        imgLoader.init(mConfig);


/*        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case WowYunApp.WEBALBUM_LOAD_SUCCESS:
                        View layout = findViewById(R.id.webalbum_status_layout);
                        layout.setVisibility(View.GONE);
                        listView.setVisibility(View.VISIBLE);
                        Log.i(TAG, " WEBALBUM_LOAD_SUCCESS, do rendering webalbum now");
                        ((GridView) listView).setAdapter(new WebAlbumImageAdapter());
                        break;
                    case WowYunApp.WEBALBUM_LOAD_FAILURE:
                        status.setText(getResour

ces().getString(R.string.action_fetch_webalbum_failure));
                        break;
                }
            }
        };*/
        Log.i(TAG, " start get http resource for jid = " + _jid);
        mHttpClient.get("http://101.69.230.238:8080/list/"+_jid, mHttpHandler);
/*        new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                parseXML(responseBody);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });*/



/*        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    URL mediainfo = new URL("http://101.69.230.238:8080/list/" + _jid);
                    XmlPullParserFactory parserFactory = XmlPullParserFactory.newInstance();
                    XmlPullParser parser = parserFactory.newPullParser();
                    parser.setInput(mediainfo.openStream(), "utf-8");
                    //parser.set
                    int type = parser.getEventType();

                    while(type != XmlPullParser.END_DOCUMENT) {
                        switch (type) {
                            case XmlPullParser.START_DOCUMENT:
                                Log.i(TAG, "Start Document");
                                break;
                            case XmlPullParser.START_TAG:
                                Log.i(TAG, "start tag = " + parser.getName());
                                if(parser.getName().equals("item")) {
                                    WebAlbumInfo waItem = new WebAlbumInfo();
                                    waItem.mime = parser.getAttributeValue(0);
                                    waItem.thumbpath = parser.getAttributeValue(1);
                                    waItem.imagepath = parser.getAttributeValue(2);
                                    waInfo.add(waItem);
                                    Log.i(TAG, " mime= " + waItem.mime + " thumb= " + waItem.thumbpath + " image= " + waItem.imagepath);
                                }
                                break;
                            case XmlPullParser.END_TAG:
                                Log.i(TAG, "end tag = " + parser.getName());
                                break;
                            case XmlPullParser.TEXT:
                                break;
                        }
                        parser.next();
                        type = parser.getEventType();
                    }
                    Message msg = new Message();
                    msg.what = WowYunApp.WEBALBUM_LOAD_SUCCESS;
                    WebAlbumActivity.this.mHandler.sendMessage(msg);
                } catch (MalformedURLException e) {
                    Log.i(TAG, " MalformedURLException ");
                    Message msg = new Message();
                    msg.what = WowYunApp.WEBALBUM_LOAD_FAILURE;
                    WebAlbumActivity.this.mHandler.sendMessage(msg);
                } catch (XmlPullParserException e) {
                    Log.i(TAG, " XmlPullParserException ");
                    Message msg = new Message();
                    msg.what = WowYunApp.WEBALBUM_LOAD_FAILURE;
                    WebAlbumActivity.this.mHandler.sendMessage(msg);
                } catch (IOException e) {
                    Log.i(TAG, " IOException ");

                }
            }
        };
        new Thread(runnable).start();*/
    }


    public class WebAlbumImageAdapter extends BaseAdapter {
        public int getCount() {
            return mInfo.getWebAlbumMediaCount();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int pos, View view, ViewGroup parent) {
            GridItem convertView;
            WebAlbumViewHolder holder;

            convertView = (GridItem) view;
            if(convertView == null) {
                convertView = new GridItem(getApplicationContext());
                convertView.setLayout(R.layout.item_grid_webalbum);

                holder = new WebAlbumViewHolder();
                holder.imageView = (ImageView) convertView.findViewById(R.id.imagethumb);
                convertView.setTag(holder);
            } else {
                holder = (WebAlbumViewHolder) convertView.getTag();
            }

            String _path = "http://101.69.230.238:8081/"+mInfo.getWebAlbumMediaThumb(pos);
            Log.i(TAG, " display Image " + _path);
            imgLoader.displayImage(_path, holder.imageView, mOption, new SimpleImageLoadingListener() {
                public void onLoadingStarted(String uri, View view) {
                }

                public void onLoadingFailed(String uri, View view, FailReason ret) {
                }

                public void onLoadingComplete(String uri, View view, Bitmap loaded) {

                }
            });
            return convertView;
        }
    }
}
