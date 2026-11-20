package com.ccs.deductive_case_files.ui;

import android.graphics.Canvas;
import android.text.TextPaint;
import android.text.TextUtils;

import com.ccs.deductive_case_files.graphics.IconRenderer;
import com.ccs.deductive_case_files.localization.LocaleManager;

/** One full-screen state of the Canvas UI. Components are rebuilt whenever state changes. */
public abstract class Screen extends ComponentHost {
    protected final ThemeManager t = ThemeManager.get();
    protected float w, h, insetTop, insetBottom, insetLeft, insetRight;
    /** Horizontal content bounds (centred and width-limited on large screens). */
    protected float cl, cw;

    protected Screen(GameCanvasView view) {
        super(view);
    }

    public final void layout(float w, float h, float il, float it, float ir, float ib) {
        this.w = w;
        this.h = h;
        insetLeft = il;
        insetTop = it;
        insetRight = ir;
        insetBottom = ib;
        float avail = w - il - ir - t.dp(32);
        cw = Math.min(avail, t.dp(640));
        cl = il + t.dp(16) + (avail - cw) / 2f;
        rebuild();
    }

    public final void rebuild() {
        if (w <= 0) return;
        float keep = scroll != null ? scroll.y : 0f;
        chrome.clear();
        scroll = null;
        build();
        if (scroll != null) scroll.scrollTo(keep);
        view.contentChanged();
    }

    protected abstract void build();

    /** Returns true when the screen consumed the back action. */
    public boolean onBack() {
        return false;
    }

    /** Distance from the bottom of the screen at which snackbars should float. */
    public float snackbarOffset() {
        return insetBottom + t.dp(16);
    }

    protected String s(int id, Object... args) {
        return LocaleManager.s(id, args);
    }

    public String str(int id) {
        return LocaleManager.s(id);
    }

    /** Adds a small top app bar; returns its bottom edge. */
    protected float topBar(String title, Runnable back, CanvasButton... actions) {
        float bottom = insetTop + t.dp(64);
        float left = insetLeft + t.dp(4), right = w - insetRight - t.dp(4);
        float titleLeft = insetLeft + t.dp(16);
        Bar bar = new Bar();
        bar.at(0, 0, w, bottom);
        chrome.add(bar);
        if (back != null) {
            CanvasButton bb = CanvasButton.icon(IconRenderer.BACK, s(com.ccs.deductive_case_files.R.string.back), back);
            bb.tint = t.onSurface;
            bb.at(left, insetTop + t.dp(8), t.dp(48), t.dp(48));
            chrome.add(bb);
            titleLeft = left + t.dp(52);
        }
        for (int i = actions.length - 1; i >= 0; i--) {
            right -= t.dp(48);
            actions[i].at(right, insetTop + t.dp(8), t.dp(48), t.dp(48));
            chrome.add(actions[i]);
        }
        TextPaint p = t.text(ThemeManager.TITLE_L, 0);
        bar.title = TextUtils.ellipsize(title, p, right - titleLeft - t.dp(8), TextUtils.TruncateAt.END).toString();
        bar.titleX = titleLeft;
        bar.titleY = insetTop + t.dp(32);
        bar.desc = title;
        return bottom;
    }

    /** Creates the scrolling body between two horizontal edges. */
    protected CanvasScrollContainer body(float top, float bottom) {
        scroll = new CanvasScrollContainer(view.getContext());
        scroll.at(0, top, w, bottom - top);
        return scroll;
    }

    /** Adds wrapped text to the body at (x, y); returns the new y. */
    protected float text(CharSequence text, int style, int color, float x, float y, float width) {
        CanvasText ct = new CanvasText(t, text, style, color, width);
        ct.place(x, y);
        scroll.add(ct);
        return ct.b.bottom;
    }

    private final class Bar extends CanvasComponent {
        String title;
        float titleX, titleY;

        @Override
        public void draw(Canvas c, ThemeManager t) {
            t.fill.setColor(t.surface);
            c.drawRect(b, t.fill);
            c.drawText(title, titleX, t.baseline(ThemeManager.TITLE_L, titleY), t.text(ThemeManager.TITLE_L, t.onSurface));
        }
    }
}
