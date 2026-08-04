package org.telegram.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
    private int selectedFilter = 0; // 0 = NFT, 1 = Regular

    public GiftsActivity() {}

    public GiftsActivity(String username) {
        this.initialUsername = username;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle("Gifts");
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) finishFragment();
            }
        });

        contentView = new LinearLayout(context);
        contentView.setOrientation(LinearLayout.VERTICAL);

        // Search bar
        FrameLayout searchContainer = new FrameLayout(context);
        searchContainer.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(8), AndroidUtilities.dp(16), AndroidUtilities.dp(8));
        searchField = new EditText(context);
        searchField.setHint("@username");
        searchField.setTextSize(16);
        searchField.setSingleLine(true);
        searchField.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        searchField.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                doSearch();
                return true;
            }
            return false;
        });
        searchContainer.addView(searchField, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 44));
        contentView.addView(searchContainer);

        // Filter tabs (NFT / Regular)
        LinearLayout filterRow = new LinearLayout(context);
        filterRow.setOrientation(LinearLayout.HORIZONTAL);
        filterRow.setGravity(Gravity.CENTER);
        filterRow.setPadding(AndroidUtilities.dp(16), 0, AndroidUtilities.dp(16), AndroidUtilities.dp(8));

        TextView nftTab = createTab("NFT", 0);
        TextView regularTab = createTab("Обычные", 1);
        filterRow.addView(nftTab, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1));
        filterRow.addView(regularTab, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1));
        contentView.addView(filterRow);

        // Progress
        progressBar = new ProgressBar(context);
        progressBar.setVisibility(View.GONE);
        contentView.addView(progressBar, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));

        // Error
        errorText = new TextView(context);
        errorText.setTextSize(14);
        errorText.setTextColor(Color.RED);
        errorText.setGravity(Gravity.CENTER);
        errorText.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(16));
        errorText.setVisibility(View.GONE);
        contentView.addView(errorText, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        // Empty
        emptyText = new TextView(context);
        emptyText.setTextSize(16);
        emptyText.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
        emptyText.setGravity(Gravity.CENTER);
        emptyText.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(80), AndroidUtilities.dp(16), AndroidUtilities.dp(16));
        emptyText.setText("Введите @username и нажмите поиск");
        contentView.addView(emptyText, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        // List
        scrollView = new ScrollView(context);
        listContainer = new LinearLayout(context);
        listContainer.setOrientation(LinearLayout.VERTICAL);
        listContainer.setPadding(AndroidUtilities.dp(16), 0, AndroidUtilities.dp(16), AndroidUtilities.dp(16));
        scrollView.addView(listContainer, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        scrollView.setVisibility(View.GONE);
        contentView.addView(scrollView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        FrameLayout container = new FrameLayout(context);
        container.addView(contentView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        return container;
    }

    private TextView createTab(String text, int index) {
        TextView tab = new TextView(getContext());
        tab.setText(text);
        tab.setTextSize(14);
        tab.setTypeface(Typeface.DEFAULT_BOLD);
        tab.setPadding(AndroidUtilities.dp(20), AndroidUtilities.dp(8), AndroidUtilities.dp(20), AndroidUtilities.dp(8));
        tab.setGravity(Gravity.CENTER);
        updateTabStyle(tab, index == selectedFilter);
        tab.setOnClickListener(v -> {
            selectedFilter = index;
            doSearch();
        });
        return tab;
    }

    private void updateTabStyle(TextView tab, boolean selected) {
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(AndroidUtilities.dp(20));
        if (selected) {
            bg.setColor(0xFF3D9E51);
            tab.setTextColor(Color.WHITE);
        } else {
            bg.setColor(0xFF2B2B2B);
            tab.setTextColor(0xFF8E8E93);
        }
        tab.setBackground(bg);
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
                emptyText.setText("Нет подарков для @" + username);
                emptyText.setVisibility(View.VISIBLE);
                return;
            }
            listContainer.removeAllViews();
            int count = 0;
            for (KartoshkaController.GiftItem item : items) {
                boolean isNft = item.minted;
                boolean showNft = selectedFilter == 0;
                if (showNft && !isNft) continue;
                if (!showNft && isNft) continue;

                count++;
                View card = createGiftCard(item);
                listContainer.addView(card);
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) card.getLayoutParams();
                lp.bottomMargin = AndroidUtilities.dp(8);
                card.setLayoutParams(lp);
            }
            if (count == 0) {
                emptyText.setText(selectedFilter == 0 ? "Нет NFT подарков" : "Нет обычных подарков");
                emptyText.setVisibility(View.VISIBLE);
            }
            scrollView.setVisibility(View.VISIBLE);
        });
    }

    private View createGiftCard(KartoshkaController.GiftItem item) {
        Context context = getContext();
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setPadding(AndroidUtilities.dp(12), AndroidUtilities.dp(12), AndroidUtilities.dp(12), AndroidUtilities.dp(12));
        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setCornerRadius(AndroidUtilities.dp(12));
        cardBg.setColor(0xFF1E1E2E);
        card.setBackground(cardBg);

        // Gift icon circle
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
            FrameLayout.LayoutParams badgeLp = new FrameLayout.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
            badgeLp.gravity = Gravity.TOP | Gravity.END;
            iconFrame.addView(nftBadge, badgeLp);
        }
        card.addView(iconFrame, LayoutHelper.createLinear(56, 56));

        // Info column
        LinearLayout info = new LinearLayout(context);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setPadding(AndroidUtilities.dp(12), 0, 0, 0);

        // Direction
        TextView directionView = new TextView(context);
        boolean isSent = "SENT".equals(item.direction);
        directionView.setText(isSent ? "ПОДАРОК ОТПРАВЛЕН" : "ПОДАРОК ПОЛУЧЕН");
        directionView.setTextSize(11);
        directionView.setTypeface(Typeface.DEFAULT_BOLD);
        directionView.setTextColor(isSent ? 0xFFE74C3C : 0xFF3D9E51);
        info.addView(directionView);

        // Title
        TextView titleView = new TextView(context);
        titleView.setText(title);
        titleView.setTextSize(16);
        titleView.setTypeface(Typeface.DEFAULT_BOLD);
        titleView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        info.addView(titleView);

        // From / To
        TextView fromToView = new TextView(context);
        StringBuilder sb = new StringBuilder();
        String fromName = item.fromName != null ? item.fromName : (item.fromUsername != null ? "@" + item.fromUsername : "?");
        String toName = item.toName != null ? item.toName : (item.toUsername != null ? "@" + item.toUsername : "?");
        sb.append("ОТ: ").append(fromName).append("\nКОМУ: ").append(toName);
        fromToView.setText(sb.toString());
        fromToView.setTextSize(12);
        fromToView.setTextColor(0xFF8E8E93);
        fromToView.setLineSpacing(AndroidUtilities.dp(2), 1);
        info.addView(fromToView);

        // Date + badges row
        LinearLayout bottomRow = new LinearLayout(context);
        bottomRow.setOrientation(LinearLayout.HORIZONTAL);
        bottomRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView dateView = new TextView(context);
        dateView.setText(formatDate(item.time));
        dateView.setTextSize(11);
        dateView.setTextColor(0xFF6E6E73);
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
            monoView.setTextColor(0xFF8E8E93);
            bottomRow.addView(monoView);
            LinearLayout.LayoutParams mlp = (LinearLayout.LayoutParams) monoView.getLayoutParams();
            mlp.leftMargin = AndroidUtilities.dp(6);
        }

        info.addView(bottomRow);
        card.addView(info, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1));

        return card;
    }

    private String formatDate(String isoTime) {
        if (isoTime == null) return "";
        try {
            String datePart = isoTime.substring(0, 10);
            String timePart = isoTime.substring(11, 16);
            String[] months = {"янв.", "февр.", "мар.", "апр.", "мая", "июн.", "июл.", "авг.", "сент.", "окт.", "ноя.", "дек."};
            int month = Integer.parseInt(datePart.substring(5, 7)) - 1;
            int day = Integer.parseInt(datePart.substring(8, 10));
            return day + " " + months[month] + ", " + timePart.substring(0, 5);
        } catch (Exception e) {
            return isoTime;
        }
    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        if (initialUsername != null && !initialUsername.isEmpty()) {
            searchField.setText(initialUsername);
            AndroidUtilities.runOnUIThread(this::doSearch, 300);
        }
        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
    }
}
