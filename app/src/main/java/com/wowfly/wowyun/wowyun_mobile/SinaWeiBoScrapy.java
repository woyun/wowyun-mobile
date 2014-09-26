package com.wowfly.wowyun.wowyun_mobile;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.BaseAdapter;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.apache.http.cookie.Cookie;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Created by user on 9/14/14.
 */
public class SinaWeiBoScrapy extends WebScrapy implements WebScrapy.ScrapyCallback{
    private static final String TAG = "SinaWeiBoScrapy";
    private String form_action;
    private String form_method;
    private String form_vk;
    private String username_field;
    private String password_field;
    private String weibo_uid;
    private String mWeiBoBuf;
    //private String mUSERNAME;
    //private String mPASSWORD;
    private Handler mHandler;
    //private String mProfileImageURL;
    //private String mNICKNAME;

    private AsyncHttpResponseHandler mLoginRespHandler = new AsyncHttpResponseHandler() {
        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            try {
                String str = new String(responseBody, "utf-8");
                Log.i(TAG, " mLoginRespHandler statusCode " + statusCode + " \n" + str);
            } catch (UnsupportedEncodingException e) {

            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Log.i(TAG, " mLoginRespHandler.onFailure " + statusCode);
        }
    };

    public SinaWeiBoScrapy(Context context, String username, String password, Handler handler) {
        loginURI = "https://login.weibo.cn/login/?ns=1&revalid=2";
        setScrapyCallback(this);
        mCookieStore = new WowPersistentCookieStore(context);
        //myCookieStore.
        mHttpClient.setCookieStore(mCookieStore);
        //myCookieStore.getCookies();
        //mHttpClient.setUserAgent("Mozilla/5.0 (Android 4.4.4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2062.44");
        mUSERNAME = username;
        mPASSWORD = password;
        mHandler = handler;
    }

    private void parseLoginPageResp(byte[] resp) {
        ByteArrayInputStream is = new ByteArrayInputStream(resp);
        try {
            XmlPullParserFactory parserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserFactory.newPullParser();
            parser.setInput(is, "utf-8");
            //parser.set
            int type = parser.getEventType();
            while (type != XmlPullParser.END_DOCUMENT) {
                switch (type) {
                    case XmlPullParser.START_DOCUMENT:
                        //Log.i(TAG, "Start Document");
                        break;
                    case XmlPullParser.START_TAG:
                        //Log.i(TAG, "start tag = " + parser.getName());
                        if(parser.getName().equals("form")) {
                            form_action = parser.getAttributeValue(0);
                            form_method = parser.getAttributeValue(1);
                        } else if(parser.getName().equals("input")) {
                            Log.i(TAG, "" + parser.getAttributeValue(0) + " " + parser.getAttributeValue(1));
                            if(parser.getAttributeValue(0).equals("text")) {
                                username_field = parser.getAttributeValue(1);
                            } else if(parser.getAttributeValue(0).equals("password")) {
                                password_field = parser.getAttributeValue(1);
                            } else if(parser.getAttributeValue(0).equals("hidden")) {
                                if(parser.getAttributeValue(1).equals("vk")) {
                                    form_vk = parser.getAttributeValue(2);
                                }
                            }
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

        } catch (XmlPullParserException e) {
            Log.i(TAG, " XmlPullParserException ");
        } catch (IOException e) {
            Log.i(TAG, " IOException ");
        }
    }

    private void parseWeiboPageResp(byte[] resp) {
        boolean tagStatus = false;
        ByteArrayInputStream is = new ByteArrayInputStream(resp);
        try {
            XmlPullParserFactory parserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserFactory.newPullParser();
            parser.setInput(is, "utf-8");
            //parser.set
            int type = parser.getEventType();
            while (type != XmlPullParser.END_DOCUMENT) {
                switch (type) {
                    case XmlPullParser.START_DOCUMENT:
                        //Log.i(TAG, "Start Document");
                        break;
                    case XmlPullParser.START_TAG:
                        Log.i(TAG, "start tag = " + parser.getName());// + " text " + parser.getAttributeValue(0));
                        if(parser.getName().equals("div")) {
                            if(parser.getAttributeValue(0).equals("ut")) {
                                tagStatus = true;
                            } else {
                                tagStatus = false;
                            }
                        } else if(tagStatus && parser.getName().equals("a")) {
                            String href = parser.getAttributeValue(0);
                            if(href.contains("/info")) {
                                Log.i(TAG, " uid info " + href);
                                break;
                            }
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
        } catch (XmlPullParserException e) {
            Log.i(TAG, " XmlPullParserException " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Log.i(TAG, " IOException ");
        }
    }

    private void parseProfile(byte[] resp) {
        try {
            String str = new String(resp, "utf-8");
            int sepE = str.indexOf("alt=\"头像\"");
            int sepS = str.substring(0, sepE).lastIndexOf("<img src=\"");
            mProfileImageURL = str.substring(sepS+10, sepE-2);
            Log.i(TAG, " profileURL " + mProfileImageURL);
            String nickname = str.substring(sepE);
            sepS = nickname.indexOf(">昵称</a>");
            nickname = nickname.substring(sepS);
            Log.i(TAG, " nick name " + nickname);
            sepS = nickname.indexOf(":");
            nickname = nickname.substring(sepS+1);
            Log.i(TAG, " nickname " + nickname);
            sepE = nickname.indexOf("<br");
            mNICKNAME = nickname.substring(0, sepE);
            Log.i(TAG, "nick name " + nickname);
        } catch (UnsupportedEncodingException e) {
        }
    }

    private void parseWeiBoUID(byte[] resp) {
        try {
            String str = new String(resp, "utf-8");
            int sep = str.indexOf("/info\">");
            String s0 = str.substring(0, sep);
            sep = s0.lastIndexOf("href=\"/");
            weibo_uid = s0.substring(sep+7);
            Log.i(TAG, " uid " + weibo_uid);
        } catch (UnsupportedEncodingException e) {

        }
    }

    public void onFailure(int statusCode, Header[] headers, byte[] resp) {
        if(actionMode == ACTION_BIND) {
            Message msg = new Message();
            msg.what = SNSSyncActivity.BIND_FAILURE;
            mHandler.sendMessage(msg);
        }
    }

    public void onSuccess(int statusCode, Header[] headers, byte[] resp) {
        try {
            String str = new String(resp, "utf-8");
            Log.i(TAG, " " + status + " http status " + statusCode + "\n body " + str);
        } catch (UnsupportedEncodingException e) {
        }

        switch (status) {
            case STATUS_INIT:
                parseLoginPageResp(resp);
                Log.i(TAG, " action " + form_action + " method " + form_method);
                Log.i(TAG, " username " + username_field + " password " + password_field + " vk " + form_vk);
                //doLogin();
                if(actionMode == ACTION_BIND) {
                    doLogin();
                }
                break;

            case STATUS_PREPARE_LOGIN:
                break;

            case STATUS_GET_PROFILE:
                parseProfile(resp);
                if(actionMode == ACTION_BIND) {
                    /**end of ACTION_BIND*/
                    Message msg = new Message();
                    msg.what = SNSSyncActivity.BIND_SUCCESS;
                    Bundle data = new Bundle();
                    data.putString("username", mUSERNAME);
                    data.putString("password", mPASSWORD);
                    data.putString("nickname", mNICKNAME);
                    data.putString("snstype", "weibo");
                    msg.setData(data);
                    mHandler.sendMessage(msg);
                }
                break;

            case STATUS_DOLOGIN:
                //parseWeiboPageResp(resp);
                parseWeiBoUID(resp);
                if(actionMode == ACTION_BIND) {
                    getProfile();
                }
                //getWeiBo();
                break;
        }
    }

    public void doLogin() {
        RequestParams params = new RequestParams();
        params.add(username_field, mUSERNAME);
        params.add(password_field, mPASSWORD);
        params.add("remember", "on");
        params.add("backURL", "http://weibo.cn");
        params.add("backTitle", "Mobile Sina");
        params.add("vk", form_vk);
        params.add("submit", "登录");
        params.add("tryCount", "");
        mHttpClient.post("https://login.weibo.cn/login/" + form_action, params, mRespHandler);
        status = STATUS_DOLOGIN;
    }

    public void getProfile() {
        String SUB="", gsid_CTandWM = "";

        Log.i(TAG, " start get weibo profile");
        List<Cookie> cookieList = mCookieStore.getCookies();
        for(Cookie item : cookieList) {
            Log.i(TAG, " " + item.getDomain());
            if(item.getDomain().equals(".weibo.cn")) {
                if(item.getName().equals("SUB")) {
                    SUB = item.getValue();
                } else if(item.getName().equals("gsid_CTandWM")) {
                    gsid_CTandWM = item.getValue();
                }
            }
        }
        RequestParams params = new RequestParams();
        params.add("SUB", SUB);
        params.add("gsid_CTandWM", gsid_CTandWM);

        mHttpClient.get("http://weibo.cn/" + weibo_uid + "/info", mRespHandler);
        status = STATUS_GET_PROFILE;
    }

    public void getWeiBo() {
        String SUB="", gsid_CTandWM = "";

        List<Cookie> cookieList = mCookieStore.getCookies();
        for(Cookie item : cookieList) {
            Log.i(TAG, " " + item.getDomain());
            if(item.getDomain().equals(".weibo.cn")) {
                if(item.getName().equals("SUB")) {
                    SUB = item.getValue();
                } else if(item.getName().equals("gsid_CTandWM")) {
                    gsid_CTandWM = item.getValue();
                }
            }
        }
        RequestParams params = new RequestParams();
        params.add("SUB", SUB);
        params.add("gsid_CTandWM", gsid_CTandWM);

        mHttpClient.get("http://weibo.cn/" + weibo_uid + "/profile", mRespHandler);
        status = STATUS_GET_WEIBO;
    }

    public void doBind() {
        actionMode = WebScrapy.ACTION_BIND;
        super.doInit();
    }

    public int checkUpdateInfo(byte[] resp) {
        //Document doc = Parser.parse(mWeiBoBuf, null);
        return 0;
    }
}
