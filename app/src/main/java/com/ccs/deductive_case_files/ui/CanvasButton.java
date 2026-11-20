package com.ccs.deductive_case_files.ui;

import android.graphics.Canvas;
import android.text.TextPaint;

import com.ccs.deductive_case_files.graphics.IconRenderer;

/** Filled, tonal, outlined, text and icon controls. */
public final class CanvasButton extends CanvasComponent {
    public static final int FILLED = 0, TONAL = 1, OUTLINED = 2, TEXT = 3, ICON = 4, DANGER = 5;
    public final int style;
    public String label;
    public int icon = -1;
    /** Icon tint override for icon buttons (0 = default). */
    public int tint;

    public CanvasButton(int style, String label, int icon, Runnable onClick) {
        this.style = style;
        this.label = label;
        this.icon = icon;
        this.onClick = onClick;
        this.desc = label;
    }

    public static CanvasButton icon(int icon, String desc, Runnable onClick) {
        CanvasButton b = new CanvasButton(ICON, null, icon, onClick);
        b.desc = desc;
        return b;
    }

    public float preferredWidth(ThemeManager t) {
        if (style == ICON) return t.dp(48);
        float w = t.text(ThemeManager.LABEL_L, 0).measureText(label) + t.dp(48);
        if (icon >= 0) w += t.dp(22);
        return Math.max(w, t.dp(72));
    }

    @Override
    public void draw(Canvas c, ThemeManager t) {
        if (style == ICON) {
            float half = t.dp(20);
            t.rect.set(b.centerX() - half, b.centerY() - half, b.centerX() + half, b.centerY() + half);
            int col = tint != 0 ? tint : t.onSurfaceVariant;
            drawState(c, t, t.rect, t.dp(6), col);
            IconRenderer.draw(c, icon, b.centerX(), b.centerY(), t.dp(24),
                    enabled ? col : ThemeManager.alpha(t.onSurface, 0.38f));
            return;
        }
        float h = Math.min(b.height(), Math.max(Math.min(b.height() - t.dp(8), t.dp(56)),
                Math.max(t.dp(40), t.textHeight(ThemeManager.LABEL_L) + t.dp(18))));
        float r = t.dp(6);
        t.rect.set(b.left, b.centerY() - h / 2f, b.right, b.centerY() + h / 2f);
        int bg = 0, fg;
        switch (style) {
            case FILLED:
                bg = t.primary;
                fg = t.onPrimary;
                break;
            case TONAL:
                bg = t.secondaryContainer;
                fg = t.onSecondaryContainer;
                break;
            case DANGER:
                fg = t.error;
                break;
            default:
                fg = t.primary;
        }
        if (!enabled) {
            if (bg != 0) bg = ThemeManager.alpha(t.onSurface, 0.12f);
            fg = ThemeManager.alpha(t.onSurface, 0.38f);
        }
        if (bg != 0) {
            t.fill.setColor(bg);
            c.drawRoundRect(t.rect, r, r, t.fill);
        }
        if (style == OUTLINED || style == DANGER) {
            t.stroke.setColor(enabled ? (style == DANGER ? t.error : t.outline) : ThemeManager.alpha(t.onSurface, 0.12f));
            t.stroke.setStrokeWidth(t.dp(1));
            float in = t.dp(0.5f);
            t.rect.inset(in, in);
            c.drawRoundRect(t.rect, r, r, t.stroke);
            t.rect.inset(-in, -in);
        }
        drawState(c, t, t.rect, r, fg);
        TextPaint p = t.text(ThemeManager.LABEL_L, fg);
        float tw = p.measureText(label), iw = icon >= 0 ? t.dp(26) : 0;
        float x = b.centerX() - (tw + iw) / 2f;
        if (icon >= 0) IconRenderer.draw(c, icon, x + t.dp(9), b.centerY(), t.dp(18), fg);
        c.drawText(label, x + iw, t.baseline(ThemeManager.LABEL_L, b.centerY()), p);
    }
}
