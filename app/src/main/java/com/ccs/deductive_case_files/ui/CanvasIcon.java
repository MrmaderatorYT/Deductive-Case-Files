package com.ccs.deductive_case_files.ui;

import android.graphics.Canvas;

import com.ccs.deductive_case_files.graphics.IconRenderer;

/** Static icon, optionally on a round tonal container or with a short text instead of a glyph. */
public final class CanvasIcon extends CanvasComponent {
    public int icon, color, background;
    public float size;
    public String letter;

    public CanvasIcon(int icon, int color, float size) {
        this.icon = icon;
        this.color = color;
        this.size = size;
    }

    @Override
    public void draw(Canvas c, ThemeManager t) {
        if (background != 0) {
            t.fill.setColor(background);
            c.drawCircle(b.centerX(), b.centerY(), Math.min(b.width(), b.height()) / 2f, t.fill);
        }
        if (letter != null) {
            android.text.TextPaint p = t.text(ThemeManager.TITLE_M, color);
            float w = p.measureText(letter);
            c.drawText(letter, b.centerX() - w / 2f, t.baseline(ThemeManager.TITLE_M, b.centerY()), p);
        } else {
            IconRenderer.draw(c, icon, b.centerX(), b.centerY(), size, color);
        }
    }
}
