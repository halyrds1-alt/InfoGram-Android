package org.telegram.messenger;

import android.content.Context;
import android.content.SharedPreferences;

public class AntiForwardController {

    private static final String PREF_NAME = "antiForwardConfig";
    private static final String KEY_ENABLED = "enabled";

    private static SharedPreferences getPrefs() {
        return ApplicationLoader.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isEnabled() {
        return getPrefs().getBoolean(KEY_ENABLED, false);
    }

    public static void setEnabled(boolean enabled) {
        getPrefs().edit().putBoolean(KEY_ENABLED, enabled).apply();
    }
}
