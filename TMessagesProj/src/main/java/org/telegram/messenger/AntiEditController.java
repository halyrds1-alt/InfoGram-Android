package org.telegram.messenger;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AntiEditController {

    private static final String PREFS_NAME = "antiEditHistory";
    private static final String KEY_ENABLED = "antiEditEnabled";
    private static final String KEY_DATA = "editHistoryData";

    private static final Map<Integer, AntiEditController> instances = new HashMap<>();
    private final int currentAccount;

    private final SharedPreferences prefs;
    private final Gson gson;

    private Map<String, List<EditHistoryEntry>> editHistoryMap;
    private boolean loaded;

    public static AntiEditController getInstance(int account) {
        synchronized (instances) {
            AntiEditController instance = instances.get(account);
            if (instance == null) {
                instance = new AntiEditController(account);
                instances.put(account, instance);
            }
            return instance;
        }
    }

    private AntiEditController(int account) {
        this.currentAccount = account;
        this.prefs = ApplicationLoader.applicationContext.getSharedPreferences(PREFS_NAME + "_" + account, Context.MODE_PRIVATE);
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.editHistoryMap = new HashMap<>();
        this.loaded = false;
    }

    public boolean isAntiEditEnabled() {
        return prefs.getBoolean(KEY_ENABLED, false);
    }

    public void setAntiEditEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply();
    }

    public void saveOriginalMessage(long dialogId, int messageId, String text, long date, String senderName) {
        if (!isAntiEditEnabled() || text == null) {
            return;
        }

        loadIfNeeded();

        String key = dialogId + "_" + messageId;
        List<EditHistoryEntry> list = editHistoryMap.get(key);
        if (list == null) {
            list = new ArrayList<>();
            editHistoryMap.put(key, list);
        }

        for (EditHistoryEntry existing : list) {
            if (existing.date == date && text.equals(existing.text)) {
                return;
            }
        }

        EditHistoryEntry entry = new EditHistoryEntry();
        entry.text = text;
        entry.date = date;
        entry.senderName = senderName != null ? senderName : "Unknown";
        list.add(entry);

        saveToPrefs();
    }

    public List<EditHistoryEntry> getEditHistory(long dialogId, int messageId) {
        loadIfNeeded();
        String key = dialogId + "_" + messageId;
        List<EditHistoryEntry> list = editHistoryMap.get(key);
        return list != null ? new ArrayList<>(list) : new ArrayList<>();
    }

    public void clearAll() {
        editHistoryMap.clear();
        prefs.edit().remove(KEY_DATA).apply();
    }

    private synchronized void loadIfNeeded() {
        if (loaded) {
            return;
        }
        loaded = true;
        loadFromPrefs();
    }

    private void loadFromPrefs() {
        String json = prefs.getString(KEY_DATA, null);
        if (json == null) {
            editHistoryMap = new HashMap<>();
            return;
        }
        try {
            Type type = new TypeToken<Map<String, List<EditHistoryEntry>>>() {}.getType();
            Map<String, List<EditHistoryEntry>> data = gson.fromJson(json, type);
            if (data != null) {
                editHistoryMap = data;
            } else {
                editHistoryMap = new HashMap<>();
            }
        } catch (Exception e) {
            FileLog.e(e);
            editHistoryMap = new HashMap<>();
        }
    }

    private synchronized void saveToPrefs() {
        try {
            String json = gson.toJson(editHistoryMap);
            prefs.edit().putString(KEY_DATA, json).apply();
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    public static void cleanup(int account) {
        synchronized (instances) {
            instances.remove(account);
        }
    }

    public static class EditHistoryEntry {
        public String text;
        public long date;
        public String senderName;
    }
}
