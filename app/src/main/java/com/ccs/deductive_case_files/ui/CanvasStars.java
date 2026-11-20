package com.ccs.deductive_case_files.ui;

import android.graphics.Canvas;

import com.ccs.deductive_case_files.graphics.IconRenderer;

/** Row of three rating stars. */
public final class CanvasStars extends CanvasComponent {
    private final int count;
    private final float size;

    public CanvasStars(int count, float size, String desc) {
        this.count = count;
        this.size = size;
        this.desc = desc;
    }

    @Override
    public void draw(Canvas c, ThemeManager t) {
        float step = b.width() / 3f;
        for (int i = 0; i < 3; i++) {
            boolean on = i < count;
            IconRenderer.draw(c, on ? IconRenderer.STAR : IconRenderer.STAR_OUTLINE,
                    b.left + step * i + step / 2f, b.centerY(), size, on ? t.tertiary : t.outlineVariant);
        }
    }
}
