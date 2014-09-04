package com.wowfly.wowyun.wowyun_mobile;

import android.util.Log;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.wowfly.android.core.ViewHolder;
import com.wowfly.android.core.WowYunApp;
import com.wowfly.android.core.XMPPService;

import org.apache.http.Header;

/**
 * Created by user on 8/21/14.
 */
public class UploadHttpResponseHandler extends AsyncHttpResponseHandler {
    private static final String TAG = "UploadHttpResponseHandler";
    private int mPosition, mChildCount;
    private GridView mView;
    private ProgressBar mSavedProgressBar;
    private WowYunApp mApp;
    private String mPeerJid;
    //private ListAdapter mAdapter;
    private BaseAdapter mAdapter;
    private boolean mIsImage = true;
    private String mFilename;

    UploadHttpResponseHandler(WowYunApp _app, String to_jid, boolean isImage, String filename, GridView view, int position) {
        mPosition = position;
        mView = view;
        mAdapter = (BaseAdapter) view.getAdapter();
        mChildCount = mView.getChildCount();
        mApp = _app;
        mPeerJid = to_jid;
        mIsImage = isImage;
        mFilename = filename;
        Log.i(TAG, "childCount = " + mView.getChildCount());
    }

    public void onStart() {
        WowYunApp.UploadDataItem item = (WowYunApp.UploadDataItem) mAdapter.getItem(mPosition);
        item.progress = 0;
        item.uploading = true;
        //mView.deferNotifyDataSetChanged();
        Log.i(TAG, "mPos = " + mPosition + " onStart event");
        mAdapter.notifyDataSetChanged();
    }
    @Override
    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
        WowYunApp.UploadDataItem item = (WowYunApp.UploadDataItem) mAdapter.getItem(mPosition);
        XMPPService _xmpp = mApp.getXMPP();
        if(_xmpp != null) {

            //int sep = item.path.lastIndexOf('/');
            //String name = item.path.substring(sep+1);
            Log.i(TAG, "send xmpp message to " + mPeerJid + " " + mFilename + " isImage = " + mIsImage);
            if(mIsImage)
                _xmpp.doSend(mPeerJid, "image/"+mFilename);
            else
                _xmpp.doSend(mPeerJid, "video/"+mFilename);
        }
        item.progress = 100;
        item.uploading = false;
        Log.i(TAG, "mPos = " + mPosition + " onSuccess event");
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
        WowYunApp.UploadDataItem item = (WowYunApp.UploadDataItem) mAdapter.getItem(mPosition);
        item.progress = 0;
        item.uploading = false;
        Log.i(TAG, "mPos = " + mPosition + " onFailure event");
        mAdapter.notifyDataSetChanged();
    }

    public void onProgress(int pos, int len) {
        WowYunApp.UploadDataItem item = (WowYunApp.UploadDataItem) mAdapter.getItem(mPosition);
        Double p0 = new Double(pos);
        Double p1 = new Double(len);

        p0 = (p0/p1)*100;
        //p0.intValue();

        item.progress = p0.intValue();
        item.uploading = true;
        Log.i(TAG, "mPosition = " + mPosition + " upload pos = " + pos + " len = " + len + " progress = " + item.progress);
        mAdapter.notifyDataSetChanged();
    }
}