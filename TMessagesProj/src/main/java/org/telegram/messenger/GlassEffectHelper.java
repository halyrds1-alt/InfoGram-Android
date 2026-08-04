package org.telegram.messenger;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RenderEffect;
import android.graphics.RenderNode;
import android.graphics.Shader;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.graphics.Outline;

public class GlassEffectHelper {

    public static final int GLASS_LIGHT = 0;
    public static final int GLASS_DARK = 1;
    public static final int GLASS_ACCENT = 2;
    public static final int GLASS_ORANGE = 3;

    private static int glassRadiusDp = 20;
    private static float glassAlpha = 0.65f;
    private static int blurRadius = 16;

    public static boolean canUseBlur() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S;
    }

    public static void applyGlassBackground(View view, int style) {
        if (view == null || view.getContext() == null) return;
        int radius = AndroidUtilities.dp(glassRadiusDp);

        int[] colors = getGlassColors(style);
        float alpha = glassAlpha;

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadii(new float[]{radius, radius, radius, radius, radius, radius, radius, radius});

        int baseColor = colors[0];
        int r = Color.red(baseColor);
        int g = Color.green(baseColor);
        int b = Color.blue(baseColor);
        bg.setColor(Color.argb((int)(255 * alpha), r, g, b));

        bg.setStroke(AndroidUtilities.dp(1), colors[1]);

        view.setBackground(bg);
        view.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View v, Outline outline) {
                outline.setRoundRect(0, 0, v.getWidth(), v.getHeight(), radius);
            }
        });
        view.setClipToOutline(true);
    }

    public static void applyGlassCard(View view, int style) {
        if (view == null) return;
        int radius = AndroidUtilities.dp(16);
        int[] colors = getGlassColors(style);

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadii(new float[]{radius, radius, radius, radius, radius, radius, radius, radius});
        bg.setColor(Color.argb(180, Color.red(colors[0]), Color.green(colors[0]), Color.blue(colors[0])));
        bg.setStroke(AndroidUtilities.dp(1), colors[1]);

        view.setBackground(bg);
        view.setElevation(AndroidUtilities.dp(4));
        view.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View v, Outline outline) {
                outline.setRoundRect(0, 0, v.getWidth(), v.getHeight(), radius);
            }
        });
        view.setClipToOutline(true);
    }

    public static void applyGlassBlur(View targetView, View overlayView, int style) {
        if (targetView == null || overlayView == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                RenderEffect blurEffect = RenderEffect.createBlurEffect(blurRadius, blurRadius, android.graphics.Shader.TileMode.CLAMP);
                overlayView.setRenderEffect(blurEffect);
            } catch (Exception e) {
                applyGlassBackground(overlayView, style);
            }
        } else {
            applyGlassBackground(overlayView, style);
        }
    }

    public static void applyRoundedCorner(View view, int radiusDp) {
        if (view == null) return;
        int radius = AndroidUtilities.dp(radiusDp);
        view.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View v, Outline outline) {
                outline.setRoundRect(0, 0, v.getWidth(), v.getHeight(), radius);
            }
        });
        view.setClipToOutline(true);
    }

    public static void applyCardStyle(View view, int style) {
        if (view == null) return;
        int radius = AndroidUtilities.dp(14);
        int[] colors = getGlassColors(style);

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadii(new float[]{radius, radius, radius, radius, radius, radius, radius, radius});

        int baseColor = colors[0];
        bg.setColor(Color.argb(200, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor)));
        bg.setStroke(AndroidUtilities.dp(1), Color.argb(40, 255, 255, 255));

        view.setBackground(bg);
        view.setElevation(AndroidUtilities.dp(2));
        view.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View v, Outline outline) {
                outline.setRoundRect(0, 0, v.getWidth(), v.getHeight(), radius);
            }
        });
        view.setClipToOutline(true);
    }

    public static void applyTopBarGlass(View view) {
        if (view == null) return;
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setColor(Color.argb(210, 18, 18, 28));
        bg.setStroke(AndroidUtilities.dp(1), Color.argb(30, 255, 255, 255));
        view.setBackground(bg);
    }

    public static void animateShow(View view) {
        if (view == null) return;
        view.setAlpha(0f);
        view.setTranslationY(AndroidUtilities.dp(20));
        view.animate()
            .alpha(1f)
            .translationY(0)
            .setDuration(350)
            .setInterpolator(new android.view.animation.DecelerateInterpolator(2f))
            .start();
    }

    public static void animateCardIn(View view, int delay) {
        if (view == null) return;
        view.setAlpha(0f);
        view.setScaleX(0.9f);
        view.setScaleY(0.9f);
        view.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setStartDelay(delay)
            .setDuration(400)
            .setInterpolator(new android.view.animation.DecelerateInterpolator(2.5f))
            .start();
    }

    private static int[] getGlassColors(int style) {
        switch (style) {
            case GLASS_DARK:
                return new int[]{0xFF12121C, 0xFF2A2A3E};
            case GLASS_ACCENT:
                return new int[]{0xFF1A2A4A, 0xFF2A4A7A};
            case GLASS_ORANGE:
                return new int[]{0xFF2A1A0A, 0xFF4A2A0A};
            case GLASS_LIGHT:
            default:
                return new int[]{0xFF1E1E2E, 0xFF2E2E42};
        }
    }
}
