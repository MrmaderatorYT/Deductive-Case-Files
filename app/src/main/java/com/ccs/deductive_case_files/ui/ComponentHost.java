package com.ccs.deductive_case_files.ui;

import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * A set of components sharing one touch stream: fixed "chrome" (bars, buttons) drawn above an
 * optional scroll container. Screens and dialogs are component hosts.
 */
public class ComponentHost {
    public final List<CanvasComponent> chrome = new ArrayList<>();
    public CanvasScrollContainer scroll;
    protected final GameCanvasView view;

    private CanvasComponent target, panTarget;
    private boolean targetInScroll, dragging, downInScroll;
    private float downX, downY, lastX, lastY;
    private VelocityTracker tracker;
    private final float slop;

    public ComponentHost(GameCanvasView view) {
        this.view = view;
        slop = ViewConfiguration.get(view.getContext()).getScaledTouchSlop();
    }

    public void drawAll(Canvas c, ThemeManager t) {
        if (scroll != null) scroll.draw(c, t);
        for (int i = 0, n = chrome.size(); i < n; i++) chrome.get(i).draw(c, t);
    }

    public void collectA11y(List<A11yNode> out) {
        for (CanvasComponent ch : chrome) ch.collectA11y(out, 0);
        if (scroll != null) scroll.collectA11y(out, 0);
    }

    private CanvasComponent find(float x, float y, boolean needClick) {
        targetInScroll = false;
        for (int i = chrome.size() - 1; i >= 0; i--) {
            CanvasComponent ch = chrome.get(i);
            if (ch.contains(x, y) && (!needClick || ch.clickable())) return ch;
        }
        if (scroll != null && scroll.b.contains(x, y)) {
            float cy = y + scroll.y;
            for (int i = scroll.children.size() - 1; i >= 0; i--) {
                CanvasComponent ch = scroll.children.get(i);
                if (ch.contains(x, cy) && (!needClick || ch.clickable())) {
                    targetInScroll = true;
                    return ch;
                }
            }
        }
        return null;
    }

    public boolean touch(MotionEvent e) {
        float x = e.getX(), y = e.getY();
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (tracker == null) tracker = VelocityTracker.obtain();
                tracker.clear();
                tracker.addMovement(e);
                downX = lastX = x;
                downY = lastY = y;
                dragging = false;
                panTarget = null;
                downInScroll = scroll != null && scroll.b.contains(x, y);
                if (scroll != null) scroll.stop();
                target = find(x, y, true);
                if (target != null) target.press(x, targetInScroll ? y + scroll.y : y, view.anim);
                view.invalidate();
                return true;
            case MotionEvent.ACTION_MOVE:
                if (tracker != null) tracker.addMovement(e);
                if (!dragging && Math.hypot(x - downX, y - downY) > slop) {
                    dragging = true;
                    if (Math.abs(x - downX) > Math.abs(y - downY)) {
                        CanvasComponent under = find(downX, downY, false);
                        if (under != null && under.canPan()) panTarget = under;
                    }
                    if (target != null) {
                        target.release(view.anim);
                        target = null;
                    }
                }
                if (dragging) {
                    if (panTarget != null) panTarget.panBy(x - lastX);
                    else if (downInScroll) scroll.dragBy(y - lastY);
                    view.onScrolled();
                }
                lastX = x;
                lastY = y;
                return true;
            case MotionEvent.ACTION_UP:
                if (target != null) {
                    CanvasComponent tg = target;
                    target = null;
                    tg.release(view.anim);
                    if (tg.tap(x, targetInScroll ? y + scroll.y : y) && !(tg instanceof CanvasGrid)) {
                        SoundManager.get().play(SoundManager.TAP);
                    }
                } else if (dragging && panTarget == null && downInScroll && tracker != null) {
                    tracker.addMovement(e);
                    tracker.computeCurrentVelocity(1000);
                    scroll.fling(tracker.getYVelocity());
                }
                view.invalidate();
                return true;
            case MotionEvent.ACTION_CANCEL:
                if (target != null) target.release(view.anim);
                target = null;
                view.invalidate();
                return true;
            default:
                return true;
        }
    }
}
