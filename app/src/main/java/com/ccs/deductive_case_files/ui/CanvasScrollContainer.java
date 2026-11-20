package com.ccs.deductive_case_files.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.widget.OverScroller;

import java.util.ArrayList;
import java.util.List;

/** Vertically scrolling viewport; children are laid out in content coordinates. */
public final class CanvasScrollContainer extends CanvasComponent {
    public final List<CanvasComponent> children = new ArrayList<>();
    public float y, contentHeight;
    private final OverScroller scroller;

    public CanvasScrollContainer(Context ctx) {
        scroller = new OverScroller(ctx);
    }

    public float max() {
        return Math.max(0f, contentHeight - b.height());
    }

    public void scrollTo(float v) {
        y = Math.max(0f, Math.min(max(), v));
    }

    public void dragBy(float dy) {
        scrollTo(y - dy);
    }

    public void stop() {
        scroller.forceFinished(true);
    }

    public void fling(float velocityY) {
        scroller.fling(0, (int) y, 0, (int) -velocityY, 0, 0, 0, (int) max());
    }

    /** Advances a fling; returns true while it is still running. */
    public boolean tick() {
        if (!scroller.computeScrollOffset()) return false;
        scrollTo(scroller.getCurrY());
        return !scroller.isFinished();
    }

    public void add(CanvasComponent c) {
        children.add(c);
        contentHeight = Math.max(contentHeight, c.b.bottom - b.top);
    }

    /** Offset that converts content coordinates into screen coordinates. */
    public float offset() {
        return -y;
    }

    @Override
    public void draw(Canvas c, ThemeManager t) {
        c.save();
        c.clipRect(b);
        c.translate(0, -y);
        float top = b.top + y, bottom = b.bottom + y;
        for (int i = 0, n = children.size(); i < n; i++) {
            CanvasComponent ch = children.get(i);
            if (ch.b.bottom < top || ch.b.top > bottom) continue;
            ch.draw(c, t);
        }
        c.restore();
        float max = max();
        if (max > 0 && !scroller.isFinished()) {
            float h = b.height(), th = Math.max(t.dp(32), h * h / contentHeight);
            float ty = b.top + (h - th) * (y / max);
            t.fill.setColor(ThemeManager.alpha(t.onSurfaceVariant, 0.35f));
            t.rect.set(b.right - t.dp(5), ty, b.right - t.dp(2), ty + th);
            c.drawRoundRect(t.rect, t.dp(2), t.dp(2), t.fill);
        }
    }

    @Override
    public void collectA11y(List<A11yNode> out, float dy) {
        float top = b.top + y, bottom = b.bottom + y;
        for (CanvasComponent ch : children) {
            if (ch.b.bottom < top || ch.b.top > bottom) continue;
            ch.collectA11y(out, -y);
        }
    }
}
