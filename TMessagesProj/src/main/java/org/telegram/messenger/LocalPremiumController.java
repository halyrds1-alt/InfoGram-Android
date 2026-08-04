package org.telegram.messenger;

import android.content.Context;
import android.content.SharedPreferences;

public class LocalPremiumController {

    private static final String PREF_NAME = "localPremiumConfig";
    private static final String KEY_ENABLED = "localPremiumEnabled";

    private static SharedPreferences getPrefs() {
        return ApplicationLoader.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isLocalPremiumEnabled() {
        return getPrefs().getBoolean(KEY_ENABLED, false);
    }

    public static void setLocalPremiumEnabled(boolean enabled) {
        getPrefs().edit().putBoolean(KEY_ENABLED, enabled).apply();
    }

    public static boolean isPremium() {
        return isLocalPremiumEnabled();
    }

    public static long getMaxUploadSize() {
        return isLocalPremiumEnabled() ? Long.MAX_VALUE : 2000 * 1024 * 1024;
    }

    public static long getMaxDownloadSize() {
        return isLocalPremiumEnabled() ? Long.MAX_VALUE : 2000 * 1024 * 1024;
    }
}
