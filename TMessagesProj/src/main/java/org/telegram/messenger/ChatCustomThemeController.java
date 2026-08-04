package org.telegram.messenger;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;

public class ChatCustomThemeController {

    private static final String PREF_NAME = "chatThemesConfig";
    private static final int DEFAULT_BG = -1;
    private static final int DEFAULT_TEXT = -1;
    private static final int DEFAULT_ACCENT = -1;

    public static final String[] PRESET_NAMES = {"Default", "Dark Blue", "Green", "Purple", "Red", "Orange"};
    public static final int[][] PRESET_COLORS = {
        {0xFF000000, 0xFFFFFFFF, 0xFF3390EC},
        {0xFF1B2836, 0xFFCCCCCC, 0xFF5288C1},
        {0xFF1A332A, 0xFFC8E6C9, 0xFF4CAF50},
        {0xFF2A1B3D, 0xFFD1C4E9, 0xFF7C4DFF},
        {0xFF3B1A1A, 0xFFFFCDD2, 0xFFE53935},
        {0xFF3B2F1A, 0xFFFFE0B2, 0xFFFF9800}
    };

    private static ChatCustomThemeController instance;

    private final SharedPreferences prefs;

    private ChatCustomThemeController() {
        prefs = ApplicationLoader.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized ChatCustomThemeController getInstance() {
        if (instance == null) {
            instance = new ChatCustomThemeController();
        }
        return instance;
    }

    public int[] getThemeColors(long dialogId) {
        int bg = prefs.getInt("bg_" + dialogId, DEFAULT_BG);
        int text = prefs.getInt("text_" + dialogId, DEFAULT_TEXT);
        int accent = prefs.getInt("accent_" + dialogId, DEFAULT_ACCENT);
        return new int[]{bg, text, accent};
    }

    public boolean hasCustomTheme(long dialogId) {
        return prefs.contains("bg_" + dialogId);
    }

    public void setThemeColors(long dialogId, int bgColor, int textColor, int accentColor) {
        prefs.edit()
            .putInt("bg_" + dialogId, bgColor)
            .putInt("text_" + dialogId, textColor)
            .putInt("accent_" + dialogId, accentColor)
            .apply();
    }

    public void clearTheme(long dialogId) {
        prefs.edit()
            .remove("bg_" + dialogId)
            .remove("text_" + dialogId)
            .remove("accent_" + dialogId)
            .apply();
    }

    public Map<Long, int[]> getAllThemes() {
        Map<Long, int[]> map = new HashMap<>();
        Map<String, ?> all = prefs.getAll();
        for (Map.Entry<String, ?> entry : all.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("bg_")) {
                try {
                    long dialogId = Long.parseLong(key.substring("bg_".length()));
                    int[] colors = getThemeColors(dialogId);
                    map.put(dialogId, colors);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return map;
    }

    public static int[] getPresetColors(int presetIndex) {
        if (presetIndex >= 0 && presetIndex < PRESET_COLORS.length) {
            return PRESET_COLORS[presetIndex].clone();
        }
        return null;
    }
}
