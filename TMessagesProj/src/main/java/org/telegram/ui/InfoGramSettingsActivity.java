package org.telegram.ui;

import static org.telegram.messenger.LocaleController.getString;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.AntiDeleteController;
import org.telegram.messenger.AntiEditController;
import org.telegram.messenger.AntiForwardController;
import org.telegram.messenger.AnonymousForwardController;
import org.telegram.messenger.AutoReplyController;
import org.telegram.messenger.FakeLastSeenController;
import org.telegram.messenger.GlassEffectHelper;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

public class InfoGramSettingsActivity extends BaseFragment {

    private FrameLayout contentView;

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(false);
        actionBar.setVisibility(View.GONE);

        contentView = new FrameLayout(context);
        contentView.setBackgroundColor(0xFF0A0A14);

        FrameLayout scrollWrap = new FrameLayout(context);
        contentView.addView(scrollWrap, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        ScrollView scrollView = new ScrollView(context);
        scrollView.setFillViewport(true);
        scrollWrap.addView(scrollView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(50), AndroidUtilities.dp(16), AndroidUtilities.dp(24));
        scrollView.addView(root, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        TextView headerTitle = new TextView(context);
        headerTitle.setText("InfoGram");
        headerTitle.setTextSize(32);
        headerTitle.setTypeface(android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD));
        headerTitle.setTextColor(Color.WHITE);
        headerTitle.setPadding(AndroidUtilities.dp(4), 0, 0, AndroidUtilities.dp(4));
        root.addView(headerTitle);

        TextView headerSub = new TextView(context);
        headerSub.setText("Features & Privacy");
        headerSub.setTextSize(14);
        headerSub.setTextColor(Color.argb(120, 255, 255, 255));
        headerSub.setPadding(AndroidUtilities.dp(4), 0, 0, AndroidUtilities.dp(20));
        root.addView(headerSub);

        root.addView(createSectionHeader(context, "PRIVACY"));

        root.addView(createGlassSwitch(context, "Fake Last Seen",
                "Show a fake online status to others",
                FakeLastSeenController.isEnabled(), () -> {
                    FakeLastSeenController.setEnabled(!FakeLastSeenController.isEnabled());
                    if (FakeLastSeenController.isEnabled() && FakeLastSeenController.getFakeTime() == 0) {
                        FakeLastSeenController.setFakeTime(System.currentTimeMillis() - 3600000);
                    }
                }, GlassEffectHelper.GLASS_DARK, 0));

        root.addView(createGlassSwitch(context, "Anti-Delete",
                "Others can't delete their messages in your chats",
                AntiDeleteController.isEnabled(), () -> {
                    AntiDeleteController.setEnabled(!AntiDeleteController.isEnabled());
                }, GlassEffectHelper.GLASS_DARK, 1));

        root.addView(createGlassSwitch(context, "Anti-Edit",
                "See original messages even after editing",
                AntiEditController.isEnabled(), () -> {
                    AntiEditController.setEnabled(!AntiEditController.isEnabled());
                }, GlassEffectHelper.GLASS_DARK, 2));

        root.addView(createSectionHeader(context, "MESSAGING"));

        root.addView(createGlassSwitch(context, "Auto-Reply",
                "Automatically reply to incoming messages",
                AutoReplyController.isEnabled(), () -> {
                    if (!AutoReplyController.isEnabled()) {
                        showAutoReplyDialog();
                    } else {
                        AutoReplyController.setEnabled(false);
                    }
                }, GlassEffectHelper.GLASS_ACCENT, 3));

        root.addView(createGlassSwitch(context, "Anti-Forward",
                "Hide your name when others forward your messages",
                AntiForwardController.isEnabled(), () -> {
                    AntiForwardController.setEnabled(!AntiForwardController.isEnabled());
                }, GlassEffectHelper.GLASS_ACCENT, 4));

        root.addView(createGlassSwitch(context, "Anonymous Forward",
                "Forward messages without showing author",
                AnonymousForwardController.isEnabled(), () -> {
                    AnonymousForwardController.setEnabled(!AnonymousForwardController.isEnabled());
                }, GlassEffectHelper.GLASS_ACCENT, 5));

        root.addView(createSectionHeader(context, "INFO"));

        root.addView(createInfoCard(context, "About InfoGram",
                "Custom Telegram client with advanced privacy features, gift tracking, and more.\n\nVersion 3.3"));

        return fragmentView = contentView;
    }

    private View createSectionHeader(Context context, String text) {
        TextView header = new TextView(context);
        header.setText(text);
        header.setTextSize(11);
        header.setTypeface(android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD));
        header.setTextColor(Color.argb(90, 255, 255, 255));
        header.setLetterSpacing(0.15f);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = AndroidUtilities.dp(24);
        lp.bottomMargin = AndroidUtilities.dp(10);
        lp.leftMargin = AndroidUtilities.dp(4);
        header.setLayoutParams(lp);
        return header;
    }

    private View createGlassSwitch(Context context, String title, String description, boolean checked, Runnable onToggle, int style, int index) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);

        int radius = AndroidUtilities.dp(16);
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(radius);
        int baseAlpha = checked ? 200 : 160;
        bg.setColor(Color.argb(baseAlpha, 16, 16, 26));
        bg.setStroke(AndroidUtilities.dp(1), Color.argb(checked ? 40 : 15, 107, 53, 255));
        card.setBackground(bg);

        int pad = AndroidUtilities.dp(16);
        card.setPadding(pad, pad, pad, pad);

        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout textCol = new LinearLayout(context);
        textCol.setOrientation(LinearLayout.VERTICAL);

        TextView titleView = new TextView(context);
        titleView.setText(title);
        titleView.setTextSize(16);
        titleView.setTypeface(android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD));
        titleView.setTextColor(Color.WHITE);
        textCol.addView(titleView);

        TextView descView = new TextView(context);
        descView.setText(description);
        descView.setTextSize(12);
        descView.setTextColor(Color.argb(100, 255, 255, 255));
        descView.setPadding(0, AndroidUtilities.dp(4), 0, 0);
        textCol.addView(descView);

        row.addView(textCol, 0, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        android.widget.Switch toggle = new android.widget.Switch(context);
        toggle.setChecked(checked);
        toggle.setOnCheckedChangeListener((btn, isChecked) -> onToggle.run());
        toggle.getTrackDrawable().setTint(Color.argb(checked ? 180 : 40, 107, 53, 255));
        row.addView(toggle);

        card.addView(row);

        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardLp.bottomMargin = AndroidUtilities.dp(10);
        card.setLayoutParams(cardLp);

        GlassEffectHelper.animateCardIn(card, index * 60);

        return card;
    }

    private View createInfoCard(Context context, String title, String text) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);

        int radius = AndroidUtilities.dp(16);
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(radius);
        bg.setColor(Color.argb(140, 14, 14, 22));
        bg.setStroke(AndroidUtilities.dp(1), Color.argb(15, 255, 255, 255));
        card.setBackground(bg);
        int pad = AndroidUtilities.dp(16);
        card.setPadding(pad, pad, pad, pad);

        TextView titleView = new TextView(context);
        titleView.setText(title);
        titleView.setTextSize(15);
        titleView.setTypeface(android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD));
        titleView.setTextColor(Color.WHITE);
        card.addView(titleView);

        TextView textView = new TextView(context);
        textView.setText(text);
        textView.setTextSize(13);
        textView.setTextColor(Color.argb(100, 255, 255, 255));
        textView.setPadding(0, AndroidUtilities.dp(8), 0, 0);
        card.addView(textView);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = AndroidUtilities.dp(12);
        card.setLayoutParams(lp);

        return card;
    }

    private void showAutoReplyDialog() {
        if (getParentActivity() == null) return;
        android.widget.EditText editText = new android.widget.EditText(getParentActivity());
        editText.setText(AutoReplyController.getReplyText());
        editText.setHint("Enter reply text...");
        editText.setSingleLine(false);
        editText.setMinLines(3);
        editText.setPadding(AndroidUtilities.dp(20), AndroidUtilities.dp(10), AndroidUtilities.dp(20), AndroidUtilities.dp(10));
        editText.setTextColor(Color.WHITE);
        editText.setHintTextColor(Color.argb(100, 255, 255, 255));

        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity(), resourceProvider);
        builder.setTitle(getString(R.string.AutoReply));
        builder.setView(editText);
        builder.setPositiveButton("OK", (dialog, which) -> {
            String text = editText.getText().toString().trim();
            if (!text.isEmpty()) {
                AutoReplyController.setReplyText(text);
                AutoReplyController.setEnabled(true);
            }
        });
        builder.setNegativeButton("Cancel", null);
        showDialog(builder.create());
    }

    @Override
    public boolean onBackPressed(boolean invoked) {
        finishFragment();
        return true;
    }
}
