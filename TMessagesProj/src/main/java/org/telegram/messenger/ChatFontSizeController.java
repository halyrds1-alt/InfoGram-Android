package org.telegram.messenger;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;

public class ChatFontSizeController {

    private static final String PREF_NAME = "chatFontSizeConfig";
    private static final int DEFAULT_FONT_SIZE = -1;
    public static final int[] AVAILABLE_SIZES = {12, 14, 16, 18, 20, 22, 24};

    private static ChatFontSizeController instance;

    private final SharedPreferences prefs;

    private ChatFontSizeController() {
        prefs = ApplicationLoader.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized ChatFontSizeController getInstance() {
        if (instance == null) {
            instance = new ChatFontSizeController();
        }
        return instance;
    }

    public int getFontSize(long dialogId) {
        return prefs.getInt("font_size_" + dialogId, DEFAULT_FONT_SIZE);
    }

    public void setFontSize(long dialogId, int sizeSp) {
        boolean found = false;
        for (int size : AVAILABLE_SIZES) {
            if (size == sizeSp) {
                found = true;
                break;
            }
        }
        if (!found) {
            return;
        }
        prefs.edit().putInt("font_size_" + dialogId, sizeSp).apply();
    }

    public Map<Long, Integer> getAllSizes() {
        Map<Long, Integer> map = new HashMap<>();
        Map<String, ?> all = prefs.getAll();
        for (Map.Entry<String, ?> entry : all.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("font_size_")) {
                try {
                    long dialogId = Long.parseLong(key.substring("font_size_".length()));
                    int size = (int) entry.getValue();
                    map.put(dialogId, size);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return map;
    }

    public void clearFontSize(long dialogId) {
        prefs.edit().remove("font_size_" + dialogId).apply();
    }
}
