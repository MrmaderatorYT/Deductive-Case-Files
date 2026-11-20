package com.ccs.deductive_case_files.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeProvider;

import com.ccs.deductive_case_files.graphics.AnimationManager;

import java.util.ArrayList;
import java.util.List;

/**
 * The single view of every Activity. It hosts the current {@link Screen}, animates screen
 * transitions, shows dialogs and snackbars, and exposes Canvas components to TalkBack through
 * virtual accessibility nodes. It redraws only when something changes.
 */
public final class GameCanvasView extends View {
    public final AnimationManager anim = new AnimationManager(this);
    private final ThemeManager t = ThemeManager.get();

    private Screen screen, previous;
    private float transition = 1f;
    private int direction;
    private CanvasDialog dialog;

    private CanvasText snack;
    private float snackAnim;
    private final Runnable hideSnack = () -> anim.run(snackAnim, 0f, 180, AnimationManager.DECELERATE,
            v -> snackAnim = v, () -> snack = null);

    private int insetL, insetT, insetR, insetB;

    private final List<A11yNode> nodes = new ArrayList<>();
    private boolean nodesDirty = true;
    private int a11yFocus = -1, hovered = -1;
    private final AccessibilityManager a11y;

    public GameCanvasView(Context context) {
        super(context);
        a11y = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        setFocusable(true);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
        setOnApplyWindowInsetsListener((v, insets) -> {
            if (Build.VERSION.SDK_INT >= 30) {
                android.graphics.Insets i = insets.getInsets(
                        WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                insetL = i.left;
                insetT = i.top;
                insetR = i.right;
                insetB = i.bottom;
            } else {
                insetL = insets.getSystemWindowInsetLeft();
                insetT = insets.getSystemWindowInsetTop();
                insetR = insets.getSystemWindowInsetRight();
                insetB = insets.getSystemWindowInsetBottom();
            }
            relayout();
            return insets;
        });
    }

    public Screen screen() {
        return screen;
    }

    /** Switches screens. direction: 1 forward, -1 back, 0 cross-fade. */
    public void setScreen(Screen s, int direction) {
        previous = screen;
        screen = s;
        this.direction = direction;
        dialog = null;
        if (getWidth() > 0) s.layout(getWidth(), getHeight(), insetL, insetT, insetR, insetB);
        if (previous != null) {
            transition = 0f;
            anim.run(0f, 1f, 300, AnimationManager.EMPHASIZED, v -> transition = v, () -> previous = null);
        } else {
            transition = 1f;
        }
        contentChanged();
    }

    public void relayout() {
        if (getWidth() == 0) return;
        if (screen != null) screen.layout(getWidth(), getHeight(), insetL, insetT, insetR, insetB);
        if (dialog != null) dialog.layout(getWidth(), getHeight(), insetT, insetB);
        snack = null;
        contentChanged();
    }

    public void showDialog(CanvasDialog d) {
        dialog = d;
        d.layout(getWidth(), getHeight(), insetT, insetB);
        d.show();
        contentChanged();
    }

    public void dismissDialog() {
        dialog = null;
        contentChanged();
    }

    public boolean hasDialog() {
        return dialog != null;
    }

    public void snack(String message) {
        if (screen == null) return;
        float width = Math.min(getWidth() - insetL - insetR - t.dp(32), t.dp(560)) - t.dp(32);
        snack = new CanvasText(t, message, ThemeManager.BODY_M, t.inverseOnSurface, width,
                Layout.Alignment.ALIGN_NORMAL, 4);
        removeCallbacks(hideSnack);
        anim.run(0f, 1f, 200, v -> snackAnim = v);
        postDelayed(hideSnack, 3800);
        announceForAccessibility(message);
    }

    public boolean onBack() {
        if (dialog != null) {
            if (dialog.cancelable) dismissDialog();
            return true;
        }
        return screen != null && screen.onBack();
    }

    public void contentChanged() {
        nodesDirty = true;
        invalidate();
        if (a11y.isEnabled() && getParent() != null) {
            sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED);
        }
    }

    void onScrolled() {
        nodesDirty = true;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        relayout();
    }

    @Override
    public void computeScroll() {
        CanvasScrollContainer sc = dialog != null ? dialog.scroll : screen != null ? screen.scroll : null;
        if (sc != null && sc.tick()) {
            nodesDirty = true;
            postInvalidateOnAnimation();
        }
    }

    @Override
    protected void onDraw(Canvas c) {
        c.drawColor(t.surface);
        if (screen == null) return;
        int w = getWidth(), h = getHeight();
        if (previous != null && transition < 1f) {
            float shift = t.dp(36) * direction;
            float out = Math.max(0f, 1f - transition * 2.2f);
            if (out > 0f) {
                int s = c.saveLayerAlpha(0, 0, w, h, (int) (255 * out));
                c.translate(-shift * transition, 0);
                previous.drawAll(c, t);
                c.restoreToCount(s);
            }
            float in = Math.max(0f, Math.min(1f, (transition - 0.25f) / 0.75f));
            int s = c.saveLayerAlpha(0, 0, w, h, (int) (255 * in));
            c.translate(shift * (1f - transition), direction == 0 ? t.dp(12) * (1f - transition) : 0);
            screen.drawAll(c, t);
            c.restoreToCount(s);
        } else {
            screen.drawAll(c, t);
        }
        if (snack != null && snackAnim > 0f) drawSnack(c, w, h);
        if (dialog != null) dialog.draw(c, t, w, h);
    }

    private void drawSnack(Canvas c, int w, int h) {
        float pad = t.dp(16), bw = Math.min(w - insetL - insetR - t.dp(32), t.dp(560));
        float bh = Math.max(t.dp(48), snack.height + t.dp(28));
        float left = (w - bw) / 2f;
        float bottom = h - screen.snackbarOffset() + t.dp(20) * (1f - snackAnim);
        int s = c.saveLayerAlpha(left - pad, bottom - bh - pad, left + bw + pad, bottom + pad, (int) (255 * snackAnim));
        t.fill.setColor(t.inverseSurface);
        t.fill.setShadowLayer(t.dp(6), 0, t.dp(2), 0x40000000);
        t.rect.set(left, bottom - bh, left + bw, bottom);
        c.drawRoundRect(t.rect, t.dp(8), t.dp(8), t.fill);
        t.fill.clearShadowLayer();
        snack.place(left + pad, bottom - bh + (bh - snack.height) / 2f);
        snack.draw(c, t);
        c.restoreToCount(s);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (screen == null) return false;
        if (previous != null && e.getActionMasked() == MotionEvent.ACTION_DOWN) return false;
        boolean r = dialog != null ? dialog.touch(e) : screen.touch(e);
        if (e.getActionMasked() == MotionEvent.ACTION_UP) nodesDirty = true;
        return r;
    }

    // ---------------------------------------------------------------- accessibility

    private List<A11yNode> nodes() {
        if (nodesDirty) {
            nodes.clear();
            if (dialog != null) dialog.collectA11y(nodes);
            else if (screen != null) screen.collectA11y(nodes);
            nodesDirty = false;
            if (a11yFocus >= nodes.size()) a11yFocus = -1;
        }
        return nodes;
    }

    private void sendNodeEvent(int id, int type) {
        if (!a11y.isEnabled() || getParent() == null || id < 0 || id >= nodes().size()) return;
        AccessibilityEvent e = AccessibilityEvent.obtain(type);
        e.setPackageName(getContext().getPackageName());
        e.setClassName(View.class.getName());
        e.setSource(this, id);
        e.getText().add(nodes.get(id).text);
        getParent().requestSendAccessibilityEvent(this, e);
    }

    @Override
    protected boolean dispatchHoverEvent(MotionEvent e) {
        if (a11y.isTouchExplorationEnabled()) {
            int id = -1;
            if (e.getActionMasked() != MotionEvent.ACTION_HOVER_EXIT) {
                List<A11yNode> list = nodes();
                for (int i = list.size() - 1; i >= 0; i--) {
                    if (list.get(i).r.contains(e.getX(), e.getY())) {
                        id = i;
                        if (list.get(i).clickable) break;
                    }
                }
            }
            if (id != hovered) {
                sendNodeEvent(hovered, AccessibilityEvent.TYPE_VIEW_HOVER_EXIT);
                hovered = id;
                sendNodeEvent(id, AccessibilityEvent.TYPE_VIEW_HOVER_ENTER);
            }
            return true;
        }
        return super.dispatchHoverEvent(e);
    }

    @Override
    public AccessibilityNodeProvider getAccessibilityNodeProvider() {
        return provider;
    }

    private final AccessibilityNodeProvider provider = new AccessibilityNodeProvider() {
        private final int[] loc = new int[2];
        private final Rect rect = new Rect();

        @Override
        public AccessibilityNodeInfo createAccessibilityNodeInfo(int id) {
            List<A11yNode> list = nodes();
            if (id == View.NO_ID) {
                AccessibilityNodeInfo info = AccessibilityNodeInfo.obtain(GameCanvasView.this);
                onInitializeAccessibilityNodeInfo(info);
                for (int i = 0; i < list.size(); i++) info.addChild(GameCanvasView.this, i);
                return info;
            }
            if (id < 0 || id >= list.size()) return null;
            A11yNode n = list.get(id);
            AccessibilityNodeInfo info = AccessibilityNodeInfo.obtain(GameCanvasView.this, id);
            info.setPackageName(getContext().getPackageName());
            info.setClassName(n.clickable ? "android.widget.Button" : "android.widget.TextView");
            info.setParent(GameCanvasView.this);
            info.setText(n.text);
            info.setContentDescription(n.text);
            n.r.roundOut(rect);
            info.setBoundsInParent(rect);
            getLocationOnScreen(loc);
            rect.offset(loc[0], loc[1]);
            info.setBoundsInScreen(rect);
            info.setVisibleToUser(true);
            info.setEnabled(true);
            info.setFocusable(true);
            if (n.clickable) {
                info.setClickable(true);
                info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLICK);
            }
            if (n.checkable) {
                info.setCheckable(true);
                info.setChecked(n.checked);
            }
            if (a11yFocus == id) {
                info.setAccessibilityFocused(true);
                info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
            } else {
                info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_ACCESSIBILITY_FOCUS);
            }
            return info;
        }

        @Override
        public boolean performAction(int id, int action, Bundle args) {
            if (id == View.NO_ID) return performAccessibilityAction(action, args);
            List<A11yNode> list = nodes();
            if (id < 0 || id >= list.size()) return false;
            switch (action) {
                case AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS:
                    a11yFocus = id;
                    invalidate();
                    sendNodeEvent(id, AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED);
                    return true;
                case AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS:
                    a11yFocus = -1;
                    sendNodeEvent(id, AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED);
                    return true;
                case AccessibilityNodeInfo.ACTION_CLICK: {
                    A11yNode n = list.get(id);
                    if (!n.clickable) return false;
                    n.target.tap(n.tx, n.ty);
                    sendNodeEvent(id, AccessibilityEvent.TYPE_VIEW_CLICKED);
                    contentChanged();
                    return true;
                }
                default:
                    return false;
            }
        }
    };
}
