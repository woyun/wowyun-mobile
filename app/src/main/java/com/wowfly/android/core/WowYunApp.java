package com.wowfly.android.core;

import android.app.Application;
import android.util.Log;

/**
 * Created by user on 8/23/14.
 */
public class WowYunApp extends Application {
    private XMPPService mXMPP = null;
    private static final String TAG = "WowYunAppData";
    private MediaInfo mInfo;

    public static final int WEBALBUM_LOAD_SUCCESS =7001;
    public static final int WEBALBUM_LOAD_FAILURE =7002;

    public static final int LOGIN_SUCCESS=8001;
    public static final int LOGIN_FAILURE=8002;
    public static final int LOGIN_PREPARE=8003;

    public static final int REGISTER_SUCCESS=9001;
    public static final int REGISTER_FAILURE=9002;

    public static final int BUDDY_ADD_SUCCESS=9100;
    public static final int BUDDY_ADD_FAILURE=9101;
    public static final int BUDDY_LIST_UPDATE=9102;


    public static class UploadDataItem {
        public String path;
        public boolean uploading;
        public int progress;
    }

    public XMPPService getXMPP() {
        Log.i(TAG, "mXMPP = " + mXMPP);
        return mXMPP;
    }

    public MediaInfo getMediaInfo() {
        return mInfo;
    }

    public void setMediaInfo(MediaInfo info) {
        mInfo = info;
    }

    public void setXMPP(XMPPService xmpp) {
        Log.i(TAG, "new XMPP instance isn't accept");
    }

    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Application onCreate event");
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "new XMPPService instance");
                mXMPP = new XMPPService();
                Log.i(TAG, "new XMPPService instance end, mXMPP = " + mXMPP);
            }
        };
        new Thread(runnable).start();
    }
}
