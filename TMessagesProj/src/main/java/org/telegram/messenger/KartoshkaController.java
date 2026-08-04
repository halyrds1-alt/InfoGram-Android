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

                Type listType = new TypeToken<ArrayList<GiftItem>>() {}.getType();
                List<GiftItem> items = gson.fromJson(body, listType);
                if (items == null) {
                    items = new ArrayList<>();
                }

                boolean hasMore = false;
                try {
                    org.json.JSONObject json = new org.json.JSONObject(body);
                    hasMore = json.optBoolean("hasMore", false);
                } catch (Exception ignored) {
                }

                if (items.isEmpty()) {
                    try {
                        org.json.JSONObject json = new org.json.JSONObject(body);
                        if (json.has("items")) {
                            org.json.JSONArray arr = json.getJSONArray("items");
                            items = gson.fromJson(arr.toString(), listType);
                        }
                        hasMore = json.optBoolean("hasMore", false);
                    } catch (Exception ignored) {
                    }
                }

                List<GiftItem> finalItems = items;
                boolean finalHasMore = hasMore;
                AndroidUtilities.runOnUIThread(() -> callback.onResult(finalItems, finalHasMore, null));
            } catch (Exception e) {
                FileLog.e(e);
                postError(callback, e.getMessage());
            }
        });
    }

    private void postError(Callback callback, String error) {
        AndroidUtilities.runOnUIThread(() -> callback.onResult(new ArrayList<>(), false, error));
    }

    public static class GiftItem {
        public String id;
        public String kind;
        public String time;
        public String giftTitle;
        public String giftSlug;
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
        public String direction;
    }
}
