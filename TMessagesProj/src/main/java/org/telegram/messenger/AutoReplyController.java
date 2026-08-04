package org.telegram.messenger;

import android.content.Context;
import android.content.SharedPreferences;

public class AutoReplyController {

    private static final String PREF_NAME = "autoReplyConfig";
    private static final String KEY_ENABLED = "enabled";
    private static final String KEY_TEXT = "replyText";

    private static SharedPreferences getPrefs() {
        return ApplicationLoader.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isEnabled() {
        return getPrefs().getBoolean(KEY_ENABLED, false);
    }

    public static void setEnabled(boolean enabled) {
        getPrefs().edit().putBoolean(KEY_ENABLED, enabled).apply();
    }

    public static String getReplyText() {
        return getPrefs().getString(KEY_TEXT, "Я сейчас занят, отвечу позже");
    }

    public static void setReplyText(String text) {
        getPrefs().edit().putString(KEY_TEXT, text).apply();
    }
}
