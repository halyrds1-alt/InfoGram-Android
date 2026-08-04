package org.telegram.messenger;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.RenderEffect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

public class LiquidGlassView extends FrameLayout {

    private Paint glassPaint;
    private Paint borderPaint;
    private Paint highlightPaint;
    private RectF rect;
    private float cornerRadius;
    private int glassColor;
    private int borderColor;
    private boolean showHighlight = true;

    public LiquidGlassView(Context context) {
        super(context);
        init();
    }

    public LiquidGlassView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LiquidGlassView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        cornerRadius = AndroidUtilities.dp(20);

        glassPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glassPaint.setColor(Color.argb(170, 18, 18, 28));
        glassPaint.setStyle(Paint.Style.FILL);

        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setColor(Color.argb(35, 255, 255, 255));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(AndroidUtilities.dp(1));

        highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightPaint.setStyle(Paint.Style.FILL);

        rect = new RectF();

        setLayerType(LAYER_TYPE_HARDWARE, null);
        setHasTransientState(true);
    }

    public void setGlassColor(int color) {
        this.glassColor = color;
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        glassPaint.setColor(Color.argb(170, r, g, b));
        invalidate();
    }

    public void setCornerRadius(float radiusDp) {
        this.cornerRadius = AndroidUtilities.dp((int) radiusDp);
        invalidate();
    }

    public void setShowHighlight(boolean show) {
        this.showHighlight = show;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        rect.set(0, 0, getWidth(), getHeight());
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, glassPaint);
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, borderPaint);

        if (showHighlight && getWidth() > 0 && getHeight() > 0) {
            highlightPaint.setShader(new LinearGradient(
                0, 0, getWidth() * 0.3f, getHeight() * 0.4f,
                Color.argb(25, 255, 255, 255),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP
            ));
            RectF highlightRect = new RectF(
                AndroidUtilities.dp(2), AndroidUtilities.dp(2),
                getWidth() - AndroidUtilities.dp(2),
                getHeight() * 0.5f
            );
            canvas.drawRoundRect(highlightRect, cornerRadius, cornerRadius, highlightPaint);
        }
        super.onDraw(canvas);
    }

    public static LiquidGlassView create(Context context, int style) {
        LiquidGlassView view = new LiquidGlassView(context);
        int[] colors;
        switch (style) {
            case GlassEffectHelper.GLASS_DARK:
                colors = new int[]{0xFF0E0E1A, 0xFF1A1A2E};
                break;
            case GlassEffectHelper.GLASS_ACCENT:
                colors = new int[]{0xFF142040, 0xFF1E3060};
                break;
            case GlassEffectHelper.GLASS_ORANGE:
                colors = new int[]{0xFF201510, 0xFF302018};
                break;
            default:
                colors = new int[]{0xFF12121E, 0xFF1E1E30};
                break;
        }
        view.setGlassColor(colors[0]);
        view.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(12), AndroidUtilities.dp(16), AndroidUtilities.dp(12));
        return view;
    }
}
