package com.ccs.deductive_case_files.ui;

import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;

import com.ccs.deductive_case_files.graphics.AnimationManager;

import java.util.List;

/** Base class of every Canvas widget: bounds, state, touch feedback and accessibility. */
public abstract class CanvasComponent {
    public final RectF b = new RectF();
    public boolean enabled = true, pressed;
    /** Content description read by TalkBack; null hides the component from accessibility. */
    public String desc;
    public Runnable onClick;
    private float grow, fade, px, py;

    public CanvasComponent at(float left, float top, float width, float height) {
        b.set(left, top, left + width, top + height);
        return this;
    }

    public abstract void draw(Canvas c, ThemeManager t);

    public boolean clickable() {
        return enabled && onClick != null;
    }

    public boolean contains(float x, float y) {
        return b.contains(x, y);
    }

    public void press(float x, float y, AnimationManager am) {
        pressed = true;
        px = x;
        py = y;
        fade = 1f;
        am.run(0f, 1f, 280, v -> grow = v);
    }

    public void release(AnimationManager am) {
        pressed = false;
        am.run(1f, 0f, 260, v -> fade = v);
    }

    /** Performs the click at the given point; returns true when something happened. */
    public boolean tap(float x, float y) {
        if (onClick == null) return false;
        onClick.run();
        return true;
    }

    /** Horizontal panning support (used by the matrix). */
    public boolean canPan() {
        return false;
    }

    public void panBy(float dx) {
    }

    public void collectA11y(List<A11yNode> out, float dy) {
        if (desc != null) out.add(new A11yNode(b, dy, desc, clickable(), this, b.centerX(), b.centerY()));
    }

    /** Ripple-like state layer clipped to a rounded rectangle. */
    protected void drawState(Canvas c, ThemeManager t, RectF r, float radius, int color) {
        if (fade <= 0f || !AnimationManager.isEnabled()) return;
        Path p = t.path;
        p.rewind();
        p.addRoundRect(r, radius, radius, Path.Direction.CW);
        c.save();
        c.clipPath(p);
        t.fill.setColor(ThemeManager.alpha(color, 0.14f * fade));
        float max = Math.max(r.width(), r.height());
        c.drawCircle(px, py, max * (0.15f + 1.0f * grow), t.fill);
        c.restore();
    }
}
