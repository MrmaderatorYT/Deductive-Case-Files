package com.ccs.deductive_case_files.ui;

import android.graphics.Canvas;

/** Material 3 progress indicators: linear bar or circular ring, both determinate. */
public final class CanvasProgressBar extends CanvasComponent {
    public float value;
    public boolean circular;
    public String center;

    public CanvasProgressBar(float value, boolean circular) {
        this.value = value;
        this.circular = circular;
    }

    @Override
    public void draw(Canvas c, ThemeManager t) {
        float v = Math.max(0f, Math.min(1f, value));
        if (circular) {
            float sw = t.dp(6), r = Math.min(b.width(), b.height()) / 2f - sw / 2f;
            t.rect.set(b.centerX() - r, b.centerY() - r, b.centerX() + r, b.centerY() + r);
            t.stroke.setStrokeWidth(sw);
            t.stroke.setColor(t.secondaryContainer);
            c.drawArc(t.rect, 0, 360, false, t.stroke);
            if (v > 0) {
                t.stroke.setColor(t.primary);
                c.drawArc(t.rect, -90, 360 * v, false, t.stroke);
            }
            if (center != null) {
                android.text.TextPaint p = t.text(ThemeManager.TITLE_S, t.onSurface);
                c.drawText(center, b.centerX() - p.measureText(center) / 2f,
                        t.baseline(ThemeManager.TITLE_S, b.centerY()), p);
            }
            return;
        }
        float h = t.dp(6), r = h / 2f, cy = b.centerY(), gap = t.dp(4);
        float split = b.left + b.width() * v;
        if (v < 1f) {
            t.fill.setColor(t.secondaryContainer);
            t.rect.set(v > 0 ? split + gap : b.left, cy - r, b.right, cy + r);
            if (t.rect.width() > 0) c.drawRoundRect(t.rect, r, r, t.fill);
        }
        if (v > 0f) {
            t.fill.setColor(t.primary);
            t.rect.set(b.left, cy - r, Math.max(split, b.left + h), cy + r);
            c.drawRoundRect(t.rect, r, r, t.fill);
        }
    }
}
