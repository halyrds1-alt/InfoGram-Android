package org.telegram.messenger;

import android.content.Context;
import android.content.SharedPreferences;

public class AntiDeleteController {

    private static final String PREF_NAME = "antiDeleteConfig";

    public static boolean isEnabled() {
        return ApplicationLoader.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean("enabled", false);
    }

    public static void setEnabled(boolean enabled) {
        ApplicationLoader.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putBoolean("enabled", enabled).apply();
    }
}
