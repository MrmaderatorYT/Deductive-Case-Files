package com.ccs.deductive_case_files.ui;

import android.graphics.Canvas;
import android.graphics.RectF;
import android.text.TextPaint;

import com.ccs.deductive_case_files.graphics.IconRenderer;

import java.util.List;

/** Bottom navigation bar or primary tab row (when {@code tabs} is true). */
public final class CanvasNavigationBar extends CanvasComponent {
    public interface Listener {
        void onSelected(int index);
    }

    private final String[] labels;
    private final int[] icons;
    public int selected;
    private float indicator;
    public float bottomInset;
    public boolean tabs;
    private final Listener listener;
    private final GameCanvasView view;

    public CanvasNavigationBar(GameCanvasView view, String[] labels, int[] icons, int selected, Listener l) {
        this.view = view;
        this.labels = labels;
        this.icons = icons;
        this.selected = selected;
        this.indicator = selected;
        this.listener = l;
        onClick = () -> {
        };
    }

    @Override
    public boolean tap(float x, float y) {
        int i = (int) ((x - b.left) / (b.width() / labels.length));
        i = Math.max(0, Math.min(labels.length - 1, i));
        if (i != selected) {
            if (tabs) {
                selected = i;
                view.anim.run(indicator, i, 220, v -> indicator = v);
            }
            listener.onSelected(i);
        }
        return true;
    }

    @Override
    public void draw(Canvas c, ThemeManager t) {
        float iw = b.width() / labels.length;
        if (tabs) {
            t.fill.setColor(t.surface);
            c.drawRect(b, t.fill);
            t.fill.setColor(t.outlineVariant);
            c.drawRect(b.left, b.bottom - t.dp(1), b.right, b.bottom, t.fill);
            for (int i = 0; i < labels.length; i++) {
                boolean sel = i == selected;
                TextPaint p = t.text(ThemeManager.TITLE_S, sel ? t.primary : t.onSurfaceVariant);
                float tw = p.measureText(labels[i]);
                c.drawText(labels[i], b.left + iw * i + (iw - tw) / 2f, t.baseline(ThemeManager.TITLE_S, b.centerY()), p);
            }
            float w = iw - t.dp(24), x = b.left + iw * indicator + t.dp(12);
            t.fill.setColor(t.primary);
            c.drawRect(x, b.bottom - t.dp(2), x + w, b.bottom, t.fill);
            return;
        }
        t.fill.setColor(t.surfaceContainer);
        c.drawRect(b, t.fill);
        t.fill.setColor(t.outlineVariant);
        c.drawRect(b.left, b.top, b.right, b.top + t.dp(1), t.fill);
        float top = b.top + t.dp(12);
        for (int i = 0; i < labels.length; i++) {
            float cx = b.left + iw * i + iw / 2f;
            boolean sel = i == selected;
            if (sel) {
                t.fill.setColor(t.primaryContainer);
                t.rect.set(cx - t.dp(24), top, cx + t.dp(24), top + t.dp(32));
                c.drawRoundRect(t.rect, t.dp(5), t.dp(5), t.fill);
            }
            IconRenderer.draw(c, icons[i], cx, top + t.dp(16), t.dp(22), sel ? t.onPrimaryContainer : t.onSurfaceVariant);
            TextPaint p = t.text(ThemeManager.LABEL_M, sel ? t.primary : t.onSurfaceVariant);
            c.drawText(labels[i], cx - p.measureText(labels[i]) / 2f, t.baseline(ThemeManager.LABEL_M, top + t.dp(46)), p);
        }
    }

    @Override
    public void collectA11y(List<A11yNode> out, float dy) {
        float iw = b.width() / labels.length;
        for (int i = 0; i < labels.length; i++) {
            RectF r = new RectF(b.left + iw * i, b.top, b.left + iw * (i + 1), b.bottom - bottomInset);
            A11yNode n = new A11yNode(r, dy, labels[i], true, this, r.centerX(), r.centerY());
            n.checkable = true;
            n.checked = i == selected;
            out.add(n);
        }
    }
}
