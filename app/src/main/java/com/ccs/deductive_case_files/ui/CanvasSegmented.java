package com.ccs.deductive_case_files.ui;

import android.graphics.Canvas;
import android.graphics.Path;
import android.text.TextPaint;

import com.ccs.deductive_case_files.graphics.IconRenderer;

import java.util.List;

/** Segmented button for a single choice. */
public final class CanvasSegmented extends CanvasComponent {
    public interface Listener {
        void onSelected(int index);
    }

    private final String[] labels;
    public int selected;
    private final Listener listener;
    private final float[] radii = new float[8];

    public CanvasSegmented(String[] labels, int selected, Listener l) {
        this.labels = labels;
        this.selected = selected;
        this.listener = l;
        onClick = () -> {
        };
    }

    @Override
    public boolean tap(float x, float y) {
        int i = (int) ((x - b.left) / (b.width() / labels.length));
        i = Math.max(0, Math.min(labels.length - 1, i));
        if (i != selected) {
            selected = i;
            listener.onSelected(i);
        }
        return true;
    }

    @Override
    public void draw(Canvas c, ThemeManager t) {
        float h = Math.min(b.height(), t.dp(40) * Math.max(1f, t.fontScale));
        float top = b.centerY() - h / 2f, sw = b.width() / labels.length, r = t.dp(5);
        Path p = t.path;
        for (int i = 0; i < labels.length; i++) {
            float l = b.left + sw * i;
            boolean sel = i == selected;
            if (sel) {
                float rl = i == 0 ? r : 0, rr = i == labels.length - 1 ? r : 0;
                radii[0] = radii[1] = radii[6] = radii[7] = rl;
                radii[2] = radii[3] = radii[4] = radii[5] = rr;
                p.rewind();
                t.rect.set(l, top, l + sw, top + h);
                p.addRoundRect(t.rect, radii, Path.Direction.CW);
                t.fill.setColor(t.primaryContainer);
                c.drawPath(p, t.fill);
            }
            int fg = sel ? t.onPrimaryContainer : t.onSurface;
            TextPaint tp = t.text(ThemeManager.LABEL_L, fg);
            float tw = tp.measureText(labels[i]), iw = sel ? t.dp(22) : 0;
            float x = l + (sw - tw - iw) / 2f;
            if (sel) IconRenderer.draw(c, IconRenderer.CHECK, x + t.dp(8), b.centerY(), t.dp(16), fg);
            c.drawText(labels[i], x + iw, t.baseline(ThemeManager.LABEL_L, b.centerY()), tp);
        }
        t.stroke.setColor(t.outline);
        t.stroke.setStrokeWidth(t.dp(1));
        t.rect.set(b.left + t.dp(0.5f), top + t.dp(0.5f), b.right - t.dp(0.5f), top + h - t.dp(0.5f));
        c.drawRoundRect(t.rect, r, r, t.stroke);
        for (int i = 1; i < labels.length; i++) {
            float x = b.left + sw * i;
            c.drawLine(x, top, x, top + h, t.stroke);
        }
    }

    @Override
    public void collectA11y(List<A11yNode> out, float dy) {
        float sw = b.width() / labels.length;
        for (int i = 0; i < labels.length; i++) {
            android.graphics.RectF r = new android.graphics.RectF(b.left + sw * i, b.top, b.left + sw * (i + 1), b.bottom);
            A11yNode n = new A11yNode(r, dy, labels[i], true, this, r.centerX(), r.centerY());
            n.checkable = true;
            n.checked = i == selected;
            out.add(n);
        }
    }
}
