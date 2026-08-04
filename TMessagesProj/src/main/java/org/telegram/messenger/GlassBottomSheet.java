package org.telegram.messenger;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class GlassBottomSheet {

    public static View createGlassCard(Context context, String title, String subtitle, View content, int style) {
        LinearLayout wrapper = new LinearLayout(context);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        int pad = AndroidUtilities.dp(16);
        wrapper.setPadding(pad, AndroidUtilities.dp(12), pad, pad);

        int radius = AndroidUtilities.dp(20);
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadii(new float[]{radius, radius, radius, radius, 0, 0, 0, 0});

        int[] colors = getColors(style);
        bg.setColor(Color.argb(230, Color.red(colors[0]), Color.green(colors[0]), Color.blue(colors[0])));
        bg.setStroke(AndroidUtilities.dp(1), Color.argb(30, 255, 255, 255));
        wrapper.setBackground(bg);

        View handle = new View(context);
        GradientDrawable handleBg = new GradientDrawable();
        handleBg.setShape(GradientDrawable.RECTANGLE);
        handleBg.setCornerRadius(AndroidUtilities.dp(3));
        handleBg.setColor(Color.argb(60, 255, 255, 255));
        FrameLayout handleWrap = new FrameLayout(context);
        int handleW = AndroidUtilities.dp(40);
        int handleH = AndroidUtilities.dp(4);
        FrameLayout.LayoutParams handleLp = new FrameLayout.LayoutParams(handleW, handleH);
        handleLp.gravity = Gravity.CENTER_HORIZONTAL;
        handleLp.topMargin = AndroidUtilities.dp(4);
        handleLp.bottomMargin = AndroidUtilities.dp(12);
        handleWrap.addView(handle, handleLp);
        wrapper.addView(handleWrap);

        if (title != null) {
            TextView titleView = new TextView(context);
            titleView.setText(title);
            titleView.setTextSize(18);
            titleView.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            titleView.setTextColor(Color.WHITE);
            titleView.setGravity(Gravity.CENTER);
            titleView.setPadding(0, 0, 0, subtitle != null ? AndroidUtilities.dp(4) : AndroidUtilities.dp(12));
            wrapper.addView(titleView);
        }

        if (subtitle != null) {
            TextView subView = new TextView(context);
            subView.setText(subtitle);
            subView.setTextSize(13);
            subView.setTextColor(Color.argb(150, 255, 255, 255));
            subView.setGravity(Gravity.CENTER);
            subView.setPadding(0, 0, 0, AndroidUtilities.dp(16));
            wrapper.addView(subView);
        }

        if (content != null) {
            wrapper.addView(content);
        }

        return wrapper;
    }

    public static View createGlassSwitch(Context context, String title, String description, boolean checked, Runnable onToggle, int style) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);

        int radius = AndroidUtilities.dp(14);
        int[] colors = getColors(style);
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(radius);
        bg.setColor(Color.argb(180, Color.red(colors[0]), Color.green(colors[0]), Color.blue(colors[0])));
        bg.setStroke(AndroidUtilities.dp(1), Color.argb(25, 255, 255, 255));
        card.setBackground(bg);
        int pad = AndroidUtilities.dp(14);
        card.setPadding(pad, pad, pad, pad);

        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout textCol = new LinearLayout(context);
        textCol.setOrientation(LinearLayout.VERTICAL);
        textCol.setWeightSum(1);

        TextView titleView = new TextView(context);
        titleView.setText(title);
        titleView.setTextSize(15);
        titleView.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        titleView.setTextColor(Color.WHITE);
        textCol.addView(titleView);

        if (description != null) {
            TextView descView = new TextView(context);
            descView.setText(description);
            descView.setTextSize(12);
            descView.setTextColor(Color.argb(120, 255, 255, 255));
            descView.setPadding(0, AndroidUtilities.dp(4), 0, 0);
            textCol.addView(descView);
        }

        row.addView(textCol, 0, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        android.widget.Switch toggle = new android.widget.Switch(context);
        toggle.setChecked(checked);
        toggle.setOnCheckedChangeListener((btn, isChecked) -> onToggle.run());
        int tint = checked ? 0xFF6B35FF : Color.argb(80, 100, 100, 100);
        toggle.getThumbDrawable().setTint(tint);
        toggle.getTrackDrawable().setTint(Color.argb(40, 255, 255, 255));
        row.addView(toggle);

        card.addView(row);

        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardLp.bottomMargin = AndroidUtilities.dp(8);
        card.setLayoutParams(cardLp);

        return card;
    }

    public static View createSectionHeader(Context context, String text) {
        TextView header = new TextView(context);
        header.setText(text.toUpperCase());
        header.setTextSize(12);
        header.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        header.setTextColor(Color.argb(100, 255, 255, 255));
        header.setPadding(AndroidUtilities.dp(4), AndroidUtilities.dp(16), 0, AndroidUtilities.dp(8));
        return header;
    }

    public static View createGlassButton(Context context, String text, Runnable onClick, int style) {
        TextView button = new TextView(context);
        button.setText(text);
        button.setTextSize(15);
        button.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        button.setTextColor(Color.WHITE);
        button.setGravity(Gravity.CENTER);

        int radius = AndroidUtilities.dp(12);
        int[] colors = getColors(style);
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(radius);
        bg.setColor(Color.argb(220, Color.red(colors[1]), Color.green(colors[1]), Color.blue(colors[1])));
        bg.setStroke(AndroidUtilities.dp(1), Color.argb(30, 255, 255, 255));
        button.setBackground(bg);
        int pad = AndroidUtilities.dp(14);
        button.setPadding(pad * 2, pad, pad * 2, pad);

        if (onClick != null) {
            button.setOnClickListener(v -> onClick.run());
        }

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = AndroidUtilities.dp(12);
        button.setLayoutParams(lp);

        return button;
    }

    private static int[] getColors(int style) {
        switch (style) {
            case GlassEffectHelper.GLASS_DARK:
                return new int[]{0xFF0E0E1A, 0xFF1A1A30};
            case GlassEffectHelper.GLASS_ACCENT:
                return new int[]{0xFF142040, 0xFF1E3060};
            case GlassEffectHelper.GLASS_ORANGE:
                return new int[]{0xFF201510, 0xFF3A2520};
            default:
                return new int[]{0xFF12121E, 0xFF1E1E32};
        }
    }
}
