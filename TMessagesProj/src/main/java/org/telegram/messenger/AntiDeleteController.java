package org.telegram.messenger;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.telegram.tgnet.TLRPC;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AntiDeleteController {

    private static final String PREFS_NAME = "anti_delete_prefs";
    private static final String KEY_ENABLED = "anti_delete_enabled";
    private static final String FILE_NAME = "anti_delete_messages.json";

    private static final Map<Integer, AntiDeleteController> instances = new HashMap<>();
    private final int currentAccount;

    private final SharedPreferences prefs;
    private final Gson gson;
    private final File storageFile;

    private Map<Long, List<DeletedMessageEntry>> deletedMessagesMap;
    private boolean loaded;

    public static AntiDeleteController getInstance(int account) {
        synchronized (instances) {
            AntiDeleteController instance = instances.get(account);
            if (instance == null) {
                instance = new AntiDeleteController(account);
                instances.put(account, instance);
            }
            return instance;
        }
    }

    private AntiDeleteController(int account) {
        this.currentAccount = account;
        this.prefs = ApplicationLoader.applicationContext.getSharedPreferences(PREFS_NAME + "_" + account, Context.MODE_PRIVATE);
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.storageFile = new File(ApplicationLoader.getFilesDirFixed("anti_delete"), FILE_NAME);
        this.deletedMessagesMap = new HashMap<>();
        this.loaded = false;
    }

    public boolean isAntiDeleteEnabled() {
        return prefs.getBoolean(KEY_ENABLED, false);
    }

    public void setAntiDeleteEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply();
    }

    public void saveDeletedMessage(MessageObject message) {
        if (!isAntiDeleteEnabled() || message == null) {
            return;
        }

        loadIfNeeded();

        try {
            DeletedMessageEntry entry = buildEntry(message);
            if (entry == null) {
                return;
            }

            long dialogId = message.getDialogId();
            List<DeletedMessageEntry> list = deletedMessagesMap.get(dialogId);
            if (list == null) {
                list = new ArrayList<>();
                deletedMessagesMap.put(dialogId, list);
            }

            for (DeletedMessageEntry existing : list) {
                if (existing.messageId == entry.messageId) {
                    return;
                }
            }

            list.add(entry);
            saveToFile();
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    public List<DeletedMessageEntry> getDeletedMessages(long dialogId) {
        loadIfNeeded();
        List<DeletedMessageEntry> list = deletedMessagesMap.get(dialogId);
        return list != null ? new ArrayList<>(list) : new ArrayList<>();
    }

    public int getMessageCount(long dialogId) {
        loadIfNeeded();
        List<DeletedMessageEntry> list = deletedMessagesMap.get(dialogId);
        return list != null ? list.size() : 0;
    }

    public void deleteSavedMessage(long dialogId, int messageId) {
        loadIfNeeded();
        List<DeletedMessageEntry> list = deletedMessagesMap.get(dialogId);
        if (list != null) {
            for (int i = list.size() - 1; i >= 0; i--) {
                if (list.get(i).messageId == messageId) {
                    list.remove(i);
                    break;
                }
            }
            if (list.isEmpty()) {
                deletedMessagesMap.remove(dialogId);
            }
            saveToFile();
        }
    }

    private DeletedMessageEntry buildEntry(MessageObject message) {
        TLRPC.Message msg = message.messageOwner;
        if (msg == null) {
            return null;
        }

        DeletedMessageEntry entry = new DeletedMessageEntry();
        entry.messageId = message.getId();
        entry.dialogId = message.getDialogId();
        entry.date = msg.date;
        entry.text = message.messageText != null ? message.messageText.toString() : "";

        long senderId = 0;
        if (msg.from_id != null && msg.from_id.user_id != 0) {
            senderId = msg.from_id.user_id;
        } else if (msg.peer_id != null && msg.peer_id.user_id != 0) {
            senderId = msg.peer_id.user_id;
        }
        entry.senderId = senderId;
        entry.senderName = resolveSenderName(senderId, msg);

        entry.mediaUrls = extractMediaUrls(msg);

        return entry;
    }

    private String resolveSenderName(long userId, TLRPC.Message msg) {
        if (userId == 0) {
            return "Unknown";
        }
        try {
            MessagesController controller = MessagesController.getInstance(currentAccount);
            TLRPC.User user = controller.getUser(userId);
            if (user != null) {
                String name = UserObject.getUserName(user);
                if (name != null && !name.isEmpty()) {
                    return name;
                }
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
        return "Unknown";
    }

    private List<String> extractMediaUrls(TLRPC.Message msg) {
        List<String> urls = new ArrayList<>();
        TLRPC.MessageMedia media = MessageObject.getMedia(msg);
        if (media == null) {
            return urls;
        }

        try {
            if (media.webpage != null && media.webpage.url != null) {
                urls.add(media.webpage.url);
            }
            if (media.document != null) {
                for (int i = 0; i < media.document.attributes.size(); i++) {
                    TLRPC.DocumentAttribute attr = media.document.attributes.get(i);
                    if (attr instanceof TLRPC.TL_documentAttributeVideo) {
                        // Video attribute - get file reference from document
                        if (media.document != null && media.document.size != 0) {
                            urls.add("video://" + media.document.size);
                        }
                    }
                }
            }
            if (media.photo != null) {
                for (int i = 0; i < media.photo.sizes.size(); i++) {
                    TLRPC.PhotoSize size = media.photo.sizes.get(i);
                    if (size instanceof TLRPC.TL_photoSize) {
                        if (size.location != null) {
                            urls.add("photo://" + size.location.dc_id + "/" + size.location.volume_id + "/" + size.location.local_id);
                        }
                    }
                }
            }
        } catch (Exception e) {
            FileLog.e(e);
        }

        return urls;
    }

    private synchronized void loadIfNeeded() {
        if (loaded) {
            return;
        }
        loaded = true;
        loadFromFile();
    }

    private void loadFromFile() {
        if (!storageFile.exists()) {
            deletedMessagesMap = new HashMap<>();
            return;
        }

        FileReader reader = null;
        try {
            reader = new FileReader(storageFile);
            Type type = new TypeToken<Map<Long, List<DeletedMessageEntry>>>() {}.getType();
            Map<Long, List<DeletedMessageEntry>> data = gson.fromJson(reader, type);
            if (data != null) {
                deletedMessagesMap = data;
            } else {
                deletedMessagesMap = new HashMap<>();
            }
        } catch (Exception e) {
            FileLog.e(e);
            deletedMessagesMap = new HashMap<>();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private synchronized void saveToFile() {
        FileWriter writer = null;
        try {
            File parent = storageFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            writer = new FileWriter(storageFile);
            gson.toJson(deletedMessagesMap, writer);
            writer.flush();
        } catch (Exception e) {
            FileLog.e(e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public static void cleanup(int account) {
        synchronized (instances) {
            instances.remove(account);
        }
    }

    public static class DeletedMessageEntry {
        public int messageId;
        public long dialogId;
        public int date;
        public String text;
        public long senderId;
        public String senderName;
        public List<String> mediaUrls;
    }
}
