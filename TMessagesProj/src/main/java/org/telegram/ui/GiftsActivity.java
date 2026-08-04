package org.telegram.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.GlassEffectHelper;
import org.telegram.messenger.KartoshkaController;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

import java.util.ArrayList;
import java.util.List;

public class GiftsActivity extends BaseFragment {

    private LinearLayout contentView;
    private EditText searchField;
    private LinearLayout listContainer;
    private ScrollView scrollView;
    private ProgressBar progressBar;
    private TextView errorText;
    private TextView emptyText;
    private String initialUsername;
    private int selectedFilter = 0;

    public GiftsActivity() {}

    public GiftsActivity(String username) {
        this.initialUsername = username;
    }

    @Override
    public View createView(Context context) {
        actionBar.setVisibility(View.GONE);

        contentView = new LinearLayout(context);
        contentView.setOrientation(LinearLayout.VERTICAL);
        contentView.setBackgroundColor(0xFF0A0A14);
        contentView.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(50), AndroidUtilities.dp(16), AndroidUtilities.dp(16));

        TextView headerTitle = new TextView(context);
        headerTitle.setText("Gifts History");
        headerTitle.setTextSize(28);
        headerTitle.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        headerTitle.setTextColor(Color.WHITE);
        headerTitle.setPadding(AndroidUtilities.dp(4), 0, 0, AndroidUtilities.dp(4));
        contentView.addView(headerTitle);

        TextView headerSub = new TextView(context);
        headerSub.setText("Track NFT and regular gifts via Kartoshka API");
        headerSub.setTextSize(13);
        headerSub.setTextColor(Color.argb(100, 255, 255, 255));
        headerSub.setPadding(AndroidUtilities.dp(4), 0, 0, AndroidUtilities.dp(16));
        contentView.addView(headerSub);

        int radius = AndroidUtilities.dp(14);
        GradientDrawable searchBg = new GradientDrawable();
        searchBg.setShape(GradientDrawable.RECTANGLE);
        searchBg.setCornerRadius(radius);
        searchBg.setColor(Color.argb(160, 16, 16, 26));
        searchBg.setStroke(AndroidUtilities.dp(1), Color.argb(20, 255, 255, 255));

        FrameLayout searchWrap = new FrameLayout(context);
        searchWrap.setBackground(searchBg);
        searchWrap.setPadding(AndroidUtilities.dp(14), AndroidUtilities.dp(4), AndroidUtilities.dp(14), AndroidUtilities.dp(4));

        searchField = new EditText(context);
        searchField.setHint("@username");
        searchField.setTextSize(15);
        searchField.setSingleLine(true);
        searchField.setTextColor(Color.WHITE);
        searchField.setHintTextColor(Color.argb(100, 255, 255, 255));
        searchField.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        searchField.setBackgroundColor(Color.TRANSPARENT);
        searchField.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                doSearch();
                return true;
            }
            return false;
        });
        searchWrap.addView(searchField, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 40));
        contentView.addView(searchWrap, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        LinearLayout filterRow = new LinearLayout(context);
        filterRow.setOrientation(LinearLayout.HORIZONTAL);
        filterRow.setGravity(Gravity.CENTER);
        filterRow.setPadding(0, AndroidUtilities.dp(14), 0, AndroidUtilities.dp(14));

        TextView nftTab = createTab("NFT", 0);
        TextView regularTab = createTab("Regular", 1);
        filterRow.addView(nftTab, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1));
        filterRow.addView(regularTab, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1));
        contentView.addView(filterRow);

        progressBar = new ProgressBar(context);
        progressBar.setVisibility(View.GONE);
        contentView.addView(progressBar, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));

        errorText = new TextView(context);
        errorText.setTextSize(14);
        errorText.setTextColor(0xFFFF6B6B);
        errorText.setGravity(Gravity.CENTER);
        errorText.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(40), AndroidUtilities.dp(16), AndroidUtilities.dp(16));
        errorText.setVisibility(View.GONE);
        contentView.addView(errorText, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        emptyText = new TextView(context);
        emptyText.setTextSize(15);
        emptyText.setTextColor(Color.argb(80, 255, 255, 255));
        emptyText.setGravity(Gravity.CENTER);
        emptyText.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(60), AndroidUtilities.dp(16), AndroidUtilities.dp(16));
        emptyText.setText("Enter @username and tap search");
        contentView.addView(emptyText, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        scrollView = new ScrollView(context);
        listContainer = new LinearLayout(context);
        listContainer.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(listContainer, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        scrollView.setVisibility(View.GONE);
        contentView.addView(scrollView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        FrameLayout container = new FrameLayout(context);
        container.addView(contentView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        if (initialUsername != null && !initialUsername.isEmpty()) {
            searchField.setText(initialUsername);
            AndroidUtilities.runOnUIThread(this::doSearch, 300);
        }

        return container;
    }

    private TextView createTab(String text, int index) {
        TextView tab = new TextView(getContext());
        tab.setText(text);
        tab.setTextSize(13);
        tab.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        tab.setTextColor(Color.WHITE);
        tab.setPadding(AndroidUtilities.dp(24), AndroidUtilities.dp(10), AndroidUtilities.dp(24), AndroidUtilities.dp(10));
        tab.setGravity(Gravity.CENTER);

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(AndroidUtilities.dp(20));
        if (index == selectedFilter) {
            bg.setColor(0xFF6B35FF);
        } else {
            bg.setColor(Color.argb(40, 255, 255, 255));
        }
        bg.setStroke(AndroidUtilities.dp(1), Color.argb(index == selectedFilter ? 60 : 15, 107, 53, 255));
        tab.setBackground(bg);

        tab.setOnClickListener(v -> {
            selectedFilter = index;
            updateTabStyles();
            doSearch();
        });
        return tab;
    }

    private void updateTabStyles() {
        if (contentView == null) return;
        ViewGroup filterRow = (ViewGroup) contentView.getChildAt(3);
        if (filterRow == null) return;
        for (int i = 0; i < filterRow.getChildCount(); i++) {
            TextView tab = (TextView) filterRow.getChildAt(i);
            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(AndroidUtilities.dp(20));
            if (i == selectedFilter) {
                bg.setColor(0xFF6B35FF);
            } else {
                bg.setColor(Color.argb(40, 255, 255, 255));
            }
            bg.setStroke(AndroidUtilities.dp(1), Color.argb(i == selectedFilter ? 60 : 15, 107, 53, 255));
            tab.setBackground(bg);
        }
    }

    private void doSearch() {
        String username = searchField.getText().toString().trim().replace("@", "");
        if (username.isEmpty()) return;

        progressBar.setVisibility(View.VISIBLE);
        listContainer.removeAllViews();
        errorText.setVisibility(View.GONE);
        emptyText.setVisibility(View.GONE);

        KartoshkaController.getInstance().fetchGiftHistory(username, (items, hasMore, error) -> {
            progressBar.setVisibility(View.GONE);
            if (error != null) {
                errorText.setText("Error: " + error);
                errorText.setVisibility(View.VISIBLE);
                return;
            }
            if (items.isEmpty()) {
                emptyText.setText("No gifts for @" + username);
                emptyText.setVisibility(View.VISIBLE);
                return;
            }
            listContainer.removeAllViews();
            int count = 0;
            for (KartoshkaController.GiftItem item : items) {
                boolean isNft = item.minted;
                if (selectedFilter == 0 && !isNft) continue;
                if (selectedFilter == 1 && isNft) continue;
                count++;
                View card = createGiftCard(item, count);
                listContainer.addView(card);
            }
            if (count == 0) {
                emptyText.setText(selectedFilter == 0 ? "No NFT gifts" : "No regular gifts");
                emptyText.setVisibility(View.VISIBLE);
            }
            scrollView.setVisibility(View.VISIBLE);
        });
    }

    private View createGiftCard(KartoshkaController.GiftItem item, int index) {
        Context context = getContext();
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setPadding(AndroidUtilities.dp(14), AndroidUtilities.dp(14), AndroidUtilities.dp(14), AndroidUtilities.dp(14));

        int radius = AndroidUtilities.dp(16);
        boolean isSent = "SENT".equals(item.direction);
        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setShape(GradientDrawable.RECTANGLE);
        cardBg.setCornerRadius(radius);
        cardBg.setColor(Color.argb(180, 14, 14, 22));
        cardBg.setStroke(AndroidUtilities.dp(1), Color.argb(isSent ? 30 : 40,
                isSent ? 231 : 61, isSent ? 76 : 158, isSent ? 60 : 81));
        card.setBackground(cardBg);

        FrameLayout iconFrame = new FrameLayout(context);
        TextView iconLetter = new TextView(context);
        String title = item.giftTitle != null ? item.giftTitle : "Gift";
        iconLetter.setText(String.valueOf(title.charAt(0)));
        iconLetter.setTextSize(20);
        iconLetter.setTypeface(Typeface.DEFAULT_BOLD);
        iconLetter.setGravity(Gravity.CENTER);
        iconLetter.setTextColor(Color.WHITE);
        GradientDrawable iconBg = new GradientDrawable();
        iconBg.setShape(GradientDrawable.OVAL);
        iconBg.setColor(0xFF6B35FF);
        iconLetter.setBackground(iconBg);
        iconFrame.addView(iconLetter, LayoutHelper.createFrame(48, 48));

        if (item.minted && item.giftNum > 0) {
            TextView nftBadge = new TextView(context);
            nftBadge.setText("#" + item.giftNum);
            nftBadge.setTextSize(9);
            nftBadge.setTextColor(Color.WHITE);
            nftBadge.setTypeface(Typeface.DEFAULT_BOLD);
            GradientDrawable badgeBg = new GradientDrawable();
            badgeBg.setCornerRadius(AndroidUtilities.dp(8));
            badgeBg.setColor(0xFFE74C3C);
            nftBadge.setBackground(badgeBg);
            nftBadge.setPadding(AndroidUtilities.dp(4), AndroidUtilities.dp(2), AndroidUtilities.dp(4), AndroidUtilities.dp(2));
            nftBadge.setGravity(Gravity.CENTER);
            FrameLayout.LayoutParams badgeLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            badgeLp.gravity = Gravity.TOP | Gravity.END;
            iconFrame.addView(nftBadge, badgeLp);
        }
        card.addView(iconFrame, LayoutHelper.createLinear(56, 56));

        LinearLayout info = new LinearLayout(context);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setPadding(AndroidUtilities.dp(14), 0, 0, 0);

        TextView directionView = new TextView(context);
        directionView.setText(isSent ? "SENT" : "RECEIVED");
        directionView.setTextSize(10);
        directionView.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        directionView.setTextColor(isSent ? 0xFFFF6B6B : 0xFF3D9E51);
        directionView.setLetterSpacing(0.1f);
        info.addView(directionView);

        TextView titleView = new TextView(context);
        titleView.setText(title);
        titleView.setTextSize(16);
        titleView.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        titleView.setTextColor(Color.WHITE);
        titleView.setPadding(0, AndroidUtilities.dp(3), 0, 0);
        info.addView(titleView);

        TextView fromToView = new TextView(context);
        String fromName = item.fromName != null ? item.fromName : (item.fromUsername != null ? "@" + item.fromUsername : "?");
        String toName = item.toName != null ? item.toName : (item.toUsername != null ? "@" + item.toUsername : "?");
        fromToView.setText("From: " + fromName + "\nTo: " + toName);
        fromToView.setTextSize(12);
        fromToView.setTextColor(Color.argb(100, 255, 255, 255));
        fromToView.setLineSpacing(AndroidUtilities.dp(2), 1);
        fromToView.setPadding(0, AndroidUtilities.dp(3), 0, 0);
        info.addView(fromToView);

        LinearLayout bottomRow = new LinearLayout(context);
        bottomRow.setOrientation(LinearLayout.HORIZONTAL);
        bottomRow.setGravity(Gravity.CENTER_VERTICAL);
        bottomRow.setPadding(0, AndroidUtilities.dp(6), 0, 0);

        TextView dateView = new TextView(context);
        dateView.setText(formatDate(item.time));
        dateView.setTextSize(11);
        dateView.setTextColor(Color.argb(70, 255, 255, 255));
        bottomRow.addView(dateView);

        if (item.rarityPermille > 0) {
            TextView rarityBadge = new TextView(context);
            rarityBadge.setText(String.format(" %.1f%% ", item.rarityPermille / 10.0));
            rarityBadge.setTextSize(10);
            rarityBadge.setTextColor(Color.WHITE);
            rarityBadge.setTypeface(Typeface.DEFAULT_BOLD);
            GradientDrawable rarityBg = new GradientDrawable();
            rarityBg.setCornerRadius(AndroidUtilities.dp(8));
            rarityBg.setColor(0xFFFF6B35);
            rarityBadge.setBackground(rarityBg);
            rarityBadge.setPadding(AndroidUtilities.dp(4), AndroidUtilities.dp(1), AndroidUtilities.dp(4), AndroidUtilities.dp(1));
            bottomRow.addView(rarityBadge);
            LinearLayout.LayoutParams rlp = (LinearLayout.LayoutParams) rarityBadge.getLayoutParams();
            rlp.leftMargin = AndroidUtilities.dp(6);
        }

        if (item.monoScore > 0) {
            TextView monoView = new TextView(context);
            monoView.setText("Mono: " + item.monoScore);
            monoView.setTextSize(10);
            monoView.setTextColor(Color.argb(80, 255, 255, 255));
            bottomRow.addView(monoView);
            LinearLayout.LayoutParams mlp = (LinearLayout.LayoutParams) monoView.getLayoutParams();
            mlp.leftMargin = AndroidUtilities.dp(6);
        }

        info.addView(bottomRow);
        card.addView(info, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1));

        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardLp.bottomMargin = AndroidUtilities.dp(10);
        card.setLayoutParams(cardLp);

        GlassEffectHelper.animateCardIn(card, index * 50);

        return card;
    }

    private String formatDate(String isoTime) {
        if (isoTime == null) return "";
        try {
            String datePart = isoTime.substring(0, 10);
            String timePart = isoTime.substring(11, 16);
            String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
            int month = Integer.parseInt(datePart.substring(5, 7)) - 1;
            int day = Integer.parseInt(datePart.substring(8, 10));
            return day + " " + months[month] + ", " + timePart;
        } catch (Exception e) {
            return isoTime;
        }
    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
    }

    @Override
    public boolean onBackPressed(boolean invoked) {
        finishFragment();
        return true;
    }
}
