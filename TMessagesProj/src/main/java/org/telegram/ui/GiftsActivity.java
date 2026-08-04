package org.telegram.ui;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.KartoshkaController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

import java.util.ArrayList;
import java.util.List;

public class GiftsActivity extends BaseFragment {

    private EditText searchField;
    private LinearLayout listView;
    private ProgressBar progressBar;
    private TextView errorText;
    private TextView emptyText;
    private FrameLayout contentView;
    private String initialUsername;
    private ArrayList<KartoshkaController.GiftItem> giftItems = new ArrayList<>();
    private boolean loading;
    private boolean hasMore;
    private String currentCursor;
    private int currentAccount;

    public GiftsActivity() {
        this(null);
    }

    public GiftsActivity(String username) {
        this.initialUsername = username;
        this.currentAccount = UserConfig.selectedAccount;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle("Gifts");
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        contentView = new FrameLayout(context);

        LinearLayout mainLayout = new LinearLayout(context);
        mainLayout.setOrientation(LinearLayout.VERTICAL);

        FrameLayout searchContainer = new FrameLayout(context);
        searchContainer.setPadding(AndroidUtilities.dp(12), AndroidUtilities.dp(8), AndroidUtilities.dp(12), AndroidUtilities.dp(8));
        searchContainer.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefault));

        searchField = new EditText(context);
        searchField.setHint("Search @username");
        searchField.setSingleLine(true);
        searchField.setTextSize(16);
        searchField.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(20), Theme.getColor(Theme.key_windowBackgroundGray)));
        searchField.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(8), AndroidUtilities.dp(16), AndroidUtilities.dp(8));
        searchField.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
        searchField.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText3));
        searchField.setOnEditorActionListener((v, actionId, event) -> {
            String username = searchField.getText().toString().trim().replace("@", "");
            if (!username.isEmpty()) {
                loadGifts(username);
            }
            return true;
        });
        searchContainer.addView(searchField, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        mainLayout.addView(searchContainer, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        progressBar = new ProgressBar(context);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.GONE);

        errorText = new TextView(context);
        errorText.setTextSize(14);
        errorText.setGravity(Gravity.CENTER);
        errorText.setPadding(AndroidUtilities.dp(32), AndroidUtilities.dp(32), AndroidUtilities.dp(32), AndroidUtilities.dp(32));
        errorText.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
        errorText.setVisibility(View.GONE);

        emptyText = new TextView(context);
        emptyText.setTextSize(14);
        emptyText.setGravity(Gravity.CENTER);
        emptyText.setPadding(AndroidUtilities.dp(32), AndroidUtilities.dp(32), AndroidUtilities.dp(32), AndroidUtilities.dp(32));
        emptyText.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
        emptyText.setText("No gifts found");
        emptyText.setVisibility(View.GONE);

        listView = new LinearLayout(context);
        listView.setOrientation(LinearLayout.VERTICAL);

        FrameLayout listContainer = new FrameLayout(context);
        listContainer.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        ScrollView scrollView = new ScrollView(context);
        scrollView.setFillViewport(true);
        scrollView.addView(listContainer, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        contentView.addView(scrollView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        contentView.addView(progressBar, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));
        contentView.addView(errorText, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        contentView.addView(emptyText, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        mainLayout.addView(contentView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        if (!TextUtils.isEmpty(initialUsername)) {
            searchField.setText(initialUsername);
            loadGifts(initialUsername);
        }

        return mainLayout;
    }

    private void loadGifts(String username) {
        if (loading) return;
        loading = true;
        giftItems.clear();
        listView.removeAllViews();
        progressBar.setVisibility(View.VISIBLE);
        errorText.setVisibility(View.GONE);
        emptyText.setVisibility(View.GONE);

        KartoshkaController.getInstance().fetchGiftHistory(username, (items, hasMore, error) -> {
            loading = false;
            progressBar.setVisibility(View.GONE);

            if (error != null) {
                errorText.setText("Error: " + error);
                errorText.setVisibility(View.VISIBLE);
                return;
            }

            if (items == null || items.isEmpty()) {
                emptyText.setVisibility(View.VISIBLE);
                return;
            }

            giftItems.addAll(items);
            GiftsActivity.this.hasMore = hasMore;
            populateList();
        });
    }

    private void populateList() {
        listView.removeAllViews();
        Context context = getContext();
        if (context == null) return;

        for (int i = 0; i < giftItems.size(); i++) {
            KartoshkaController.GiftItem item = giftItems.get(i);
            View itemView = createItemView(context, item, i == giftItems.size() - 1);
            listView.addView(itemView);
        }

        if (hasMore) {
            TextView loadMore = new TextView(context);
            loadMore.setText("Load More");
            loadMore.setTextSize(14);
            loadMore.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
            loadMore.setGravity(Gravity.CENTER);
            loadMore.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(12), AndroidUtilities.dp(16), AndroidUtilities.dp(12));
            loadMore.setOnClickListener(v -> {
                if (!loading && !TextUtils.isEmpty(initialUsername)) {
                    KartoshkaController.getInstance().fetchGiftHistory(initialUsername, currentCursor, (items, hasMore1, error) -> {
                        if (error == null && items != null) {
                            giftItems.addAll(items);
                            hasMore = hasMore1;
                            populateList();
                        }
                    });
                }
            });
            listView.addView(loadMore);
        }
    }

    private View createItemView(Context context, KartoshkaController.GiftItem item, boolean isLast) {
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(12), AndroidUtilities.dp(16), AndroidUtilities.dp(12));
        container.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

        TextView titleView = new TextView(context);
        titleView.setTextSize(16);
        titleView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        titleView.setTypeface(AndroidUtilities.bold());
        String title = !TextUtils.isEmpty(item.giftTitle) ? item.giftTitle : (item.giftSlug != null ? item.giftSlug : "Gift");
        titleView.setText(title);
        container.addView(titleView);

        if (!TextUtils.isEmpty(item.modelName) || !TextUtils.isEmpty(item.backdropName) || !TextUtils.isEmpty(item.patternName)) {
            TextView detailsView = new TextView(context);
            detailsView.setTextSize(13);
            detailsView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
            StringBuilder sb = new StringBuilder();
            if (!TextUtils.isEmpty(item.modelName)) sb.append("Model: ").append(item.modelName);
            if (!TextUtils.isEmpty(item.backdropName)) {
                if (sb.length() > 0) sb.append(" | ");
                sb.append("Backdrop: ").append(item.backdropName);
            }
            if (!TextUtils.isEmpty(item.patternName)) {
                if (sb.length() > 0) sb.append(" | ");
                sb.append("Pattern: ").append(item.patternName);
            }
            detailsView.setText(sb.toString());
            LinearLayout.LayoutParams params = LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT);
            params.topMargin = AndroidUtilities.dp(4);
            container.addView(detailsView, params);
        }

        LinearLayout infoRow = new LinearLayout(context);
        infoRow.setOrientation(LinearLayout.HORIZONTAL);

        if (item.rarityPermille > 0) {
            TextView rarityView = new TextView(context);
            rarityView.setTextSize(12);
            rarityView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText3));
            rarityView.setText(String.format("Rarity: %.1f%%", item.rarityPermille / 10.0));
            infoRow.addView(rarityView);
        }

        if (item.monoScore > 0) {
            TextView scoreView = new TextView(context);
            scoreView.setTextSize(12);
            scoreView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText3));
            scoreView.setText(" | Score: " + item.monoScore);
            infoRow.addView(scoreView);
        }

        if (infoRow.getChildCount() > 0) {
            LinearLayout.LayoutParams params = LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT);
            params.topMargin = AndroidUtilities.dp(4);
            container.addView(infoRow, params);
        }

        if (item.availabilityIssued > 0 || item.availabilityTotal > 0) {
            TextView availView = new TextView(context);
            availView.setTextSize(12);
            availView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText3));
            availView.setText(item.availabilityIssued + "/" + item.availabilityTotal + " available");
            LinearLayout.LayoutParams params = LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT);
            params.topMargin = AndroidUtilities.dp(2);
            container.addView(availView, params);
        }

        String directionText = "";
        if (!TextUtils.isEmpty(item.direction)) {
            if ("SENT".equalsIgnoreCase(item.direction)) {
                directionText = "Sent";
            } else if ("RECEIVED".equalsIgnoreCase(item.direction)) {
                directionText = "Received";
            }
        }

        if (!TextUtils.isEmpty(item.fromName) || !TextUtils.isEmpty(item.toName)) {
            TextView fromToView = new TextView(context);
            fromToView.setTextSize(12);
            fromToView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText3));
            StringBuilder sb = new StringBuilder();
            if (!TextUtils.isEmpty(directionText)) sb.append(directionText).append(" ");
            if (!TextUtils.isEmpty(item.fromName)) sb.append("from ").append(item.fromName);
            if (!TextUtils.isEmpty(item.toName)) {
                if (sb.length() > 0) sb.append(" → ");
                sb.append("to ").append(item.toName);
            }
            fromToView.setText(sb.toString());
            LinearLayout.LayoutParams params = LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT);
            params.topMargin = AndroidUtilities.dp(2);
            container.addView(fromToView, params);
        }

        if (!TextUtils.isEmpty(item.time)) {
            TextView dateView = new TextView(context);
            dateView.setTextSize(12);
            dateView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText3));
            dateView.setText(item.time);
            LinearLayout.LayoutParams params = LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT);
            params.topMargin = AndroidUtilities.dp(2);
            container.addView(dateView, params);
        }

        if (!isLast) {
            View divider = new View(context);
            divider.setBackgroundColor(Theme.getColor(Theme.key_divider));
            LinearLayout.LayoutParams dividerParams = LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 1);
            dividerParams.topMargin = AndroidUtilities.dp(8);
            container.addView(divider, dividerParams);
        }

        return container;
    }
}
