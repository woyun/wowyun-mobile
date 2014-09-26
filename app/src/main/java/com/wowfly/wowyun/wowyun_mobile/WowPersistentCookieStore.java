package com.wowfly.wowyun.wowyun_mobile;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.PersistentCookieStore;

import org.apache.http.cookie.Cookie;

import java.util.List;

/**
 * Created by user on 9/15/14.
 */
public class WowPersistentCookieStore extends PersistentCookieStore {
    private static final String TAG = "WowPersistentCookieStore";

    public WowPersistentCookieStore(Context context) {
        super(context);
    }

    public void addCookie(Cookie cookie) {
        Log.i(TAG, " addCookie " + cookie.toString());

        super.addCookie(cookie);
    }

    public List<Cookie> getCookies() {
        List<Cookie> cookieList;
        cookieList = super.getCookies();

        return cookieList;
    }
}
