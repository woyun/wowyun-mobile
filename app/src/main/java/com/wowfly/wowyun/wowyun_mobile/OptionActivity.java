package com.wowfly.wowyun.wowyun_mobile;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by user on 9/18/14.
 */
public class OptionActivity extends Activity {
    public void onCreate(Bundle saved) {
        super.onCreate(saved);
        setTitle("设置");
        getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefsFragment()).commit();
    }

    public static class PrefsFragment extends PreferenceFragment {
        public void onCreate(Bundle saved) {
            super.onCreate(saved);
            addPreferencesFromResource(R.xml.preferences);
        }
    }
}
