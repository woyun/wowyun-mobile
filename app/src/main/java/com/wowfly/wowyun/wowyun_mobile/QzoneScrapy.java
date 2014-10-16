package com.wowfly.wowyun.wowyun_mobile;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.apache.http.Header;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Created by user on 9/18/14.
 */
public class QzoneScrapy extends WebScrapy implements WebScrapy.ScrapyCallback {
    private static final String TAG = "QzoneScrapy";
    private QzoneProto qzoneProto;

    public QzoneScrapy(Context context, String username, String password, Handler handler) {
        loginURI = "http://ui.ptlogin2.qzone.com/cgi-bin/login?style=9&appid=549000929&pt_ttype=1&s_url=http://m.qzone.com/infocenter?g_f=";
        setScrapyCallback(this);
        mCookieStore = new WowPersistentCookieStore(context);
        //myCookieStore.
        mHttpClient.setCookieStore(mCookieStore);
        //myCookieStore.getCookies();
        //mHttpClient.setUserAgent("Mozilla/5.0 (Android 4.4.4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2062.44");
        mUSERNAME = username;
        mPASSWORD = password;
        qzoneProto = new QzoneProto(mUSERNAME, mPASSWORD);
        mHandler = handler;
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
    }

    public void doBind(String vc) {
        final String _vcode = vc;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                String info;
                if(_vcode == null) {
                    info = qzoneProto.login("");
                } else {
                    info = qzoneProto.login(_vcode);
                }
                //String info = qzoneProto.login();
                int sep = info.indexOf("登录成功");
                //Log.i(TAG, " sep " + sep + " sub "+ info.substring(sep));
                if(sep > 0) {
                    String nickname = info.substring(sep+8);
                    //Log.i(TAG, " nickname.01 " + nickname);
                    int s0 = nickname.indexOf("'");
                    int s1 = nickname.lastIndexOf("'");
                    mNICKNAME = nickname.substring(s0+1, s1);
                    //Log.i(TAG, " nickname.02 " + mNICKNAME + " s0 " + s0 + " s1 " + s1);
                    Message msg = new Message();
                    msg.what = SNSSyncActivity.BIND_SUCCESS;
                    Bundle data = new Bundle();
                    data.putString("username", mUSERNAME);
                    data.putString("password", mPASSWORD);
                    data.putString("nickname", mNICKNAME);
                    data.putString("snstype", "qzone");
                    Log.i(TAG, " send qzone bind message " + mNICKNAME + " " + mUSERNAME);
                    msg.setData(data);
                    mHandler.sendMessage(msg);
                } else if(info.equals("1")) {
                    Message msg = new Message();
                    msg.what = SNSSyncActivity.LOGIN_VC;
                    mHandler.sendMessage(msg);
                }

                Log.i(TAG, " qzone login " + info);
                info = qzoneProto.getQzoneProfile();
                BufferedWriter writer = null;
                try {
                    writer = new BufferedWriter(new FileWriter("/sdcard/qzone-01.txt"));
                    writer.write(info);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    if (writer != null) {
                        writer.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //Log.i(TAG, " qzone getprofile " + info);
            }
        };
        new Thread(runnable).start();
    }
}
