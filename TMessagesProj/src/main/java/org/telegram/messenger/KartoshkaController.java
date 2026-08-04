package org.telegram.messenger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class KartoshkaController {

    private static final String BASE_URL = "https://kartoshka.free/v1/";
    private static final String AUTH_TOKEN = "Bearer 114:__CBmSXjvDU1ka_DVaIUFyqYY405Z6pR";
    private static final Gson gson = new GsonBuilder().create();
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static KartoshkaController instance;

    public interface Callback {
        void onResult(List<GiftItem> items, boolean hasMore, String error);
    }

    public static KartoshkaController getInstance() {
        if (instance == null) {
            instance = new KartoshkaController();
        }
        return instance;
    }

    public void fetchGiftHistory(String username, Callback callback) {
        fetchGiftHistory(username, null, callback);
    }

    public void fetchGiftHistory(String username, String cursor, Callback callback) {
        executor.execute(() -> {
            try {
                String urlStr = BASE_URL + "owner/@" + username + "/history";
                if (cursor != null && !cursor.isEmpty()) {
                    urlStr += "?cursor=" + cursor;
                }
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", AUTH_TOKEN);
                conn.setRequestProperty("Accept", "application/json");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);

                int code = conn.getResponseCode();
                BufferedReader reader;
                if (code >= 200 && code < 300) {
                    reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                } else {
                    reader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
                }

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
                conn.disconnect();

                String body = sb.toString();

                if (code < 200 || code >= 300) {
                    postError(callback, "HTTP " + code + ": " + body);
                    return;
                }

                List<GiftItem> items = new ArrayList<>();
                boolean hasMore = false;
                try {
                    org.json.JSONObject json = new org.json.JSONObject(body);
                    org.json.JSONObject result = json.optJSONObject("result");
                    if (result != null) {
                        hasMore = result.optBoolean("hasMore", false);
                        org.json.JSONArray arr = result.optJSONArray("items");
                        if (arr != null) {
                            for (int i = 0; i < arr.length(); i++) {
                                org.json.JSONObject itemObj = arr.getJSONObject(i);
                                GiftItem item = parseGiftItem(itemObj);
                                if (item != null) {
                                    items.add(item);
                                }
                            }
                        }
                    } else {
                        items = gson.fromJson(body, new TypeToken<ArrayList<GiftItem>>() {}.getType());
                    }
                } catch (Exception e) {
                    FileLog.e(e);
                }
                if (items == null) items = new ArrayList<>();

                List<GiftItem> finalItems = items;
                boolean finalHasMore = hasMore;
                AndroidUtilities.runOnUIThread(() -> callback.onResult(finalItems, finalHasMore, null));
            } catch (Exception e) {
                FileLog.e(e);
                postError(callback, e.getMessage());
            }
        });
    }

    private GiftItem parseGiftItem(org.json.JSONObject obj) {
        try {
            GiftItem item = new GiftItem();
            item.id = obj.optString("id", "");
            item.kind = obj.optString("kind", "");
            item.time = obj.optString("time", "");

            org.json.JSONObject giftAction = obj.optJSONObject("giftAction");
            if (giftAction != null) {
                item.direction = giftAction.optString("direction", "");
                item.action = giftAction.optString("action", "");

                org.json.JSONObject fromObj = giftAction.optJSONObject("from");
                if (fromObj != null) {
                    item.fromName = fromObj.optString("name", "");
                    item.fromUsername = fromObj.optString("username", "");
                }

                org.json.JSONObject toObj = giftAction.optJSONObject("to");
                if (toObj != null) {
                    item.toName = toObj.optString("name", "");
                    item.toUsername = toObj.optString("username", "");
                }

                org.json.JSONObject giftObj = giftAction.optJSONObject("gift");
                if (giftObj != null) {
                    item.giftTitle = giftObj.optString("title", "");
                    item.giftSlug = giftObj.optString("slug", "");
                    item.giftNum = giftObj.optInt("num", 0);
                    item.minted = giftObj.optBoolean("minted", false);
                    item.onSale = giftObj.optBoolean("onSale", false);
                    item.availabilityIssued = giftObj.optInt("availabilityIssued", 0);
                    item.availabilityTotal = giftObj.optInt("availabilityTotal", 0);
                    item.monoScore = giftObj.optInt("monoScore", 0);

                    org.json.JSONObject modelObj = giftObj.optJSONObject("model");
                    if (modelObj != null) {
                        item.modelName = modelObj.optString("name", "");
                    }
                    org.json.JSONObject backdropObj = giftObj.optJSONObject("backdrop");
                    if (backdropObj != null) {
                        item.backdropName = backdropObj.optString("name", "");
                    }
                    org.json.JSONObject patternObj = giftObj.optJSONObject("pattern");
                    if (patternObj != null) {
                        item.patternName = patternObj.optString("name", "");
                    }
                }

                org.json.JSONObject profileChange = giftAction.optJSONObject("profileChange");
                if (profileChange != null) {
                    item.profileField = profileChange.optString("field", "");
                    item.profileOldValue = profileChange.optString("oldValue", "");
                    item.profileNewValue = profileChange.optString("newValue", "");
                }
            }
            return item;
        } catch (Exception e) {
            FileLog.e(e);
            return null;
        }
    }

    private void postError(Callback callback, String error) {
        AndroidUtilities.runOnUIThread(() -> callback.onResult(new ArrayList<>(), false, error));
    }

    public static class GiftItem {
        public String id;
        public String kind;
        public String time;
        public String action;
        public String direction;
        public String giftTitle;
        public String giftSlug;
        public int giftNum;
        public boolean minted;
        public boolean onSale;
        public String modelName;
        public String backdropName;
        public String patternName;
        public int rarityPermille;
        public int monoScore;
        public int availabilityIssued;
        public int availabilityTotal;
        public String fromName;
        public String fromUsername;
        public String toName;
        public String toUsername;
        public String profileField;
        public String profileOldValue;
        public String profileNewValue;

        public boolean isNFT() {
            return minted && giftNum > 0;
        }

        public boolean isSent() {
            return "SENT".equalsIgnoreCase(direction);
        }

        public boolean isReceived() {
            return "RECEIVED".equalsIgnoreCase(direction);
        }

        public boolean isGiftKind() {
            return "GIFT".equalsIgnoreCase(kind);
        }

        public boolean isProfileKind() {
            return "PROFILE".equalsIgnoreCase(kind);
        }

        public String getDisplayTitle() {
            if (giftTitle != null && !giftTitle.isEmpty()) return giftTitle;
            if (giftSlug != null && !giftSlug.isEmpty()) return giftSlug;
            return "Gift";
        }
    }
}
