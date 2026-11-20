package com.ccs.deductive_case_files.ui;

import android.graphics.Canvas;

/** Ruled card surface, with optional selection and colour override. */
public final class CanvasCard extends CanvasComponent {
    public static final int FILLED = 0, OUTLINED = 1, LOW = 2;
    public int type;
    /** Overrides the container colour when non-zero. */
    public int color;
    public boolean selected;
    public float radius = 8f;

    public CanvasCard(int type) {
        this.type = type;
    }

    @Override
    public void draw(Canvas c, ThemeManager t) {
        float r = t.dp(radius);
        int bg = color != 0 ? color : type == FILLED ? t.surfaceHigh : type == LOW ? t.surfaceLow : t.surface;
        if (selected) bg = t.primaryContainer;
        t.fill.setColor(bg);
        c.drawRoundRect(b, r, r, t.fill);
        t.stroke.setColor(selected ? t.primary : t.outlineVariant);
        float line = t.dp(selected ? 1.5f : 1f);
        t.stroke.setStrokeWidth(line);
        float in = line / 2f, edgeRadius = Math.max(0, r - in);
        t.rect.set(b);
        t.rect.inset(in, in);
        c.drawRoundRect(t.rect, edgeRadius, edgeRadius, t.stroke);
        drawState(c, t, b, r, t.onSurface);
    }
}
