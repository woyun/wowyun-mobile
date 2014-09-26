package com.wowfly.wowyun.wowyun_mobile;

import android.content.Context;
import android.os.Handler;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;

/**
 * Created by user on 9/14/14.
 */
abstract public class WebScrapy {
    interface ScrapyCallback {
        void onSuccess(int statusCode, Header[] headers, byte[] resp);
        void onFailure(int statusCode, Header[] headers, byte[] resp);
    }

    protected String loginURI;
    protected AsyncHttpClient mHttpClient = new AsyncHttpClient();
    private ScrapyCallback mCallback;
    protected int status;
    protected Handler mHandler;
    protected WowPersistentCookieStore mCookieStore;
    protected int actionMode;

    final static int STATUS_INIT = 0x1000;
    final static int STATUS_PREPARE_LOGIN = 0x1001;
    final static int STATUS_DOLOGIN = 0x1002;
    final static int STATUS_LOGIN_SUCCESS = 0x1003;
    final static int STATUS_GET_PROFILE=0x1005;
    final static int STATUS_GET_WEIBO=0x1006;

    final static int ACTION_BIND = 0x2000;

    protected String mUSERNAME;
    protected String mPASSWORD;
    protected String mProfileImageURL;
    protected String mNICKNAME;

    protected AsyncHttpResponseHandler mRespHandler = new AsyncHttpResponseHandler() {
        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            if(mCallback != null)
                mCallback.onSuccess(statusCode, headers, responseBody);
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            if(mCallback != null)
                mCallback.onFailure(statusCode, headers, responseBody);
        }
    };

    public WebScrapy() {
        status = STATUS_INIT;
    }

    public void setScrapyCallback(ScrapyCallback callback) {
        mCallback = callback;
    }

    protected void doInit() {
        mHttpClient.setUserAgent("Mozilla/5.0 (Linux; Android 4.4.4; Nexus 5 Build/KTU84M) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.138 Mobile Safari/537.36");
        mHttpClient.get(loginURI, mRespHandler);
    }
/*    public void doLogin() {
        //mHttpClient.get(loginURI, mRespHandler);
    }*/

    //abstract void doInit();
    //abstract void doPrepareLogin();
    //abstract void doLogin();
    //abstract void
}
