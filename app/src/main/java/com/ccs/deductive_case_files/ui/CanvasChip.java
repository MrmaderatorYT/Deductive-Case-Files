package com.ccs.deductive_case_files.ui;

import android.graphics.Canvas;
import android.text.TextPaint;

import com.ccs.deductive_case_files.graphics.IconRenderer;

/** Small assist/filter chip. */
public final class CanvasChip extends CanvasComponent {
    private final String label;
    public int icon = -1;
    public boolean selected, tonal;

    public CanvasChip(String label) {
        this.label = label;
        this.desc = label;
    }

    public float preferredWidth(ThemeManager t) {
        return t.text(ThemeManager.LABEL_L, 0).measureText(label) + t.dp(icon >= 0 || selected ? 46 : 28);
    }

    @Override
    public void draw(Canvas c, ThemeManager t) {
        float h = Math.min(b.height(), Math.max(t.dp(32), t.textHeight(ThemeManager.LABEL_L) + t.dp(12)));
        t.rect.set(b.left, b.centerY() - h / 2f, b.right, b.centerY() + h / 2f);
        float r = t.dp(4);
        int fg = selected ? t.onPrimaryContainer : tonal ? t.onSecondaryContainer : t.onSurfaceVariant;
        if (selected || tonal) {
            t.fill.setColor(selected ? t.primaryContainer : t.secondaryContainer);
            c.drawRoundRect(t.rect, r, r, t.fill);
        } else {
            t.stroke.setColor(t.outlineVariant);
            t.stroke.setStrokeWidth(t.dp(1));
            c.drawRoundRect(t.rect, r, r, t.stroke);
        }
        drawState(c, t, t.rect, r, fg);
        float x = b.left + t.dp(14);
        int ic = selected ? IconRenderer.CHECK : icon;
        if (ic >= 0) {
            IconRenderer.draw(c, ic, b.left + t.dp(17), b.centerY(), t.dp(16), fg);
            x = b.left + t.dp(32);
        }
        TextPaint p = t.text(ThemeManager.LABEL_L, fg);
        c.drawText(label, x, t.baseline(ThemeManager.LABEL_L, b.centerY()), p);
    }
}
