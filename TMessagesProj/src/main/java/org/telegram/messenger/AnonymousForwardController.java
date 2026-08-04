package org.telegram.messenger;

import android.content.Context;
import android.content.SharedPreferences;

public class AnonymousForwardController {

    private static final String PREF_NAME = "anonForwardConfig";
    private static final String KEY_ANON_FORWARD = "anonymousForwardEnabled";

    private static SharedPreferences getPrefs() {
        return ApplicationLoader.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isAnonymousForwardEnabled() {
        return getPrefs().getBoolean(KEY_ANON_FORWARD, false);
    }

    public static void setAnonymousForwardEnabled(boolean enabled) {
        getPrefs().edit().putBoolean(KEY_ANON_FORWARD, enabled).apply();
    }
}
