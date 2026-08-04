package org.telegram.messenger;

import android.content.Context;
import android.content.SharedPreferences;

public class FakeLastSeenController {

    private static final String PREF_NAME = "fakeLastSeenConfig";
    private static final String KEY_ENABLED = "fake_last_seen_enabled";
    private static final String KEY_FAKE_TIME = "fake_last_seen_time";

    public static boolean isEnabled() {
        SharedPreferences prefs = ApplicationLoader.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_ENABLED, false);
    }

    public static void setEnabled(boolean enabled) {
        SharedPreferences prefs = ApplicationLoader.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply();
    }

    public static long getFakeTime() {
        SharedPreferences prefs = ApplicationLoader.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getLong(KEY_FAKE_TIME, 0);
    }

    public static void setFakeTime(long timestampMillis) {
        SharedPreferences prefs = ApplicationLoader.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putLong(KEY_FAKE_TIME, timestampMillis).apply();
    }

    public static boolean shouldIntercept() {
        return isEnabled();
    }
}
