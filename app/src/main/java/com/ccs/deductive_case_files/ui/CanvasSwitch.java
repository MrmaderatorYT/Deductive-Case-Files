package com.ccs.deductive_case_files.ui;

import android.graphics.Canvas;

import java.util.List;

/** Material 3 switch with an animated thumb. */
public final class CanvasSwitch extends CanvasComponent {
    public interface Listener {
        void onChanged(boolean on);
    }

    public boolean on;
    private float pos;
    private final GameCanvasView view;

    public CanvasSwitch(GameCanvasView view, boolean on, String desc, Listener l) {
        this.view = view;
        this.on = on;
        this.pos = on ? 1f : 0f;
        this.desc = desc;
        onClick = () -> {
            this.on = !this.on;
            view.anim.run(pos, this.on ? 1f : 0f, 180, v -> pos = v);
            l.onChanged(this.on);
        };
    }

    @Override
    public void draw(Canvas c, ThemeManager t) {
        float w = t.dp(52), h = t.dp(32), l = b.right - w, top = b.centerY() - h / 2f;
        t.rect.set(l, top, l + w, top + h);
        t.fill.setColor(ThemeManager.blend(t.surfaceHighest, t.primary, pos));
        c.drawRoundRect(t.rect, h / 2f, h / 2f, t.fill);
        if (pos < 1f) {
            t.stroke.setStrokeWidth(t.dp(2));
            t.stroke.setColor(ThemeManager.alpha(t.outline, 1f - pos));
            t.rect.inset(t.dp(1), t.dp(1));
            c.drawRoundRect(t.rect, h / 2f, h / 2f, t.stroke);
        }
        float r = t.dp(8 + 4 * pos) + (pressed ? t.dp(2) : 0);
        float cx = l + h / 2f + (w - h) * pos;
        t.fill.setColor(ThemeManager.blend(t.outline, t.onPrimary, pos));
        c.drawCircle(cx, b.centerY(), r, t.fill);
    }

    @Override
    public void collectA11y(List<A11yNode> out, float dy) {
        A11yNode n = new A11yNode(b, dy, desc, true, this, b.centerX(), b.centerY());
        n.checkable = true;
        n.checked = on;
        out.add(n);
    }
}
