package org.telegram.messenger;

import android.content.Context;
import android.content.SharedPreferences;

public class GhostModeController {

    private static final String PREF_NAME = "ghostConfig";
    private static final String KEY_GHOST_MODE = "ghostModeEnabled";
    private static final String KEY_HIDE_READ = "hideReadReceipts";
    private static final String KEY_HIDE_TYPING = "hideTypingStatus";
    private static final String KEY_ANTI_DELETE = "antiDelete";
    private static final String KEY_ANTI_EDIT = "antiEdit";

    private static SharedPreferences getPrefs() {
        return ApplicationLoader.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isEnabled() {
        return getPrefs().getBoolean(KEY_GHOST_MODE, false);
    }

    public static void setEnabled(boolean enabled) {
        getPrefs().edit().putBoolean(KEY_GHOST_MODE, enabled).apply();
    }

    public static boolean isHideReadReceipts() {
        return getPrefs().getBoolean(KEY_HIDE_READ, false);
    }

    public static void setHideReadReceipts(boolean hide) {
        getPrefs().edit().putBoolean(KEY_HIDE_READ, hide).apply();
    }

    public static boolean isHideTypingStatus() {
        return getPrefs().getBoolean(KEY_HIDE_TYPING, false);
    }

    public static void setHideTypingStatus(boolean hide) {
        getPrefs().edit().putBoolean(KEY_HIDE_TYPING, hide).apply();
    }

    public static boolean isAntiDelete() {
        return getPrefs().getBoolean(KEY_ANTI_DELETE, false);
    }

    public static void setAntiDelete(boolean enabled) {
        getPrefs().edit().putBoolean(KEY_ANTI_DELETE, enabled).apply();
    }

    public static boolean isAntiEdit() {
        return getPrefs().getBoolean(KEY_ANTI_EDIT, false);
    }

    public static void setAntiEdit(boolean enabled) {
        getPrefs().edit().putBoolean(KEY_ANTI_EDIT, enabled).apply();
    }

    /**
     * When ghost mode is active, suppress sending read receipts (readHistory).
     */
    public static boolean shouldSendReadPacket() {
        return !isEnabled();
    }

    /**
     * When ghost mode is active, suppress sending online status (account.updateStatus).
     */
    public static boolean shouldSendOnlinePacket() {
        return !isEnabled();
    }

    /**
     * When ghost mode is active, suppress sending upload progress.
     */
    public static boolean shouldSendUploadProgress() {
        return !isEnabled();
    }
}
