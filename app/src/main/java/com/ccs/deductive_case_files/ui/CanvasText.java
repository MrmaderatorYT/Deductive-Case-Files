package com.ccs.deductive_case_files.ui;

import android.graphics.Canvas;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextUtils;

/** Wrapped text block laid out once with StaticLayout. */
public final class CanvasText extends CanvasComponent {
    private final StaticLayout layout;
    private final int style;
    public int color;
    public float alpha = 1f;
    public final float height, width;

    public CanvasText(ThemeManager t, CharSequence text, int style, int color, float width) {
        this(t, text, style, color, width, Layout.Alignment.ALIGN_NORMAL, Integer.MAX_VALUE);
    }

    public CanvasText(ThemeManager t, CharSequence text, int style, int color, float width,
                      Layout.Alignment align, int maxLines) {
        this.style = style;
        this.color = color;
        int w = Math.max(1, (int) width);
        layout = StaticLayout.Builder.obtain(text, 0, text.length(), t.text(style, color), w)
                .setAlignment(align)
                .setLineSpacing(0f, style == ThemeManager.DISPLAY || style == ThemeManager.HEADLINE ? 1.1f : 1.25f)
                .setIncludePad(false)
                .setMaxLines(maxLines)
                .setEllipsize(TextUtils.TruncateAt.END)
                .build();
        height = layout.getHeight();
        float mw = 0;
        for (int i = 0; i < layout.getLineCount(); i++) mw = Math.max(mw, layout.getLineWidth(i));
        this.width = mw;
        desc = text.toString();
    }

    /** Places the text; returns the bottom edge for flow layouts. */
    public float place(float x, float y) {
        b.set(x, y, x + layout.getWidth(), y + height);
        return b.bottom;
    }

    @Override
    public void draw(Canvas c, ThemeManager t) {
        c.save();
        c.translate(b.left, b.top);
        t.text(style, ThemeManager.alpha(color, alpha));
        layout.draw(c);
        c.restore();
    }
}
