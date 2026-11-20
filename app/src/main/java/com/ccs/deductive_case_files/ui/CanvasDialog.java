package com.ccs.deductive_case_files.ui;

import android.graphics.Canvas;
import android.graphics.RectF;
import android.text.Layout;
import android.view.MotionEvent;

import com.ccs.deductive_case_files.graphics.AnimationManager;

/** Material 3 basic dialog: optional icon, title, message, a single-choice list and buttons. */
public final class CanvasDialog extends ComponentHost {
    public interface Choice {
        void onChosen(int index);
    }

    public String title, message, positive, negative;
    public int icon = -1;
    public String[] options;
    public Runnable onPositive, onNegative;
    public Choice onChoice;
    public boolean cancelable = true;
    /** Docks the dialog above the bottom edge so the content behind it stays visible. */
    public boolean anchorBottom;

    private final RectF card = new RectF();
    private float appear;

    public CanvasDialog(GameCanvasView view) {
        super(view);
    }

    public void show() {
        view.anim.run(0f, 1f, 220, AnimationManager.EMPHASIZED, v -> appear = v, null);
    }

    public void layout(float w, float h, float insetTop, float insetBottom) {
        ThemeManager t = ThemeManager.get();
        chrome.clear();
        float pad = t.dp(24), cw = Math.min(w - t.dp(48), t.dp(420)), inner = cw - pad * 2;
        float left = (w - cw) / 2f, x = left + pad;

        CanvasIcon ic = icon >= 0 ? new CanvasIcon(icon, t.secondary, t.dp(26)) : null;
        Layout.Alignment al = ic != null ? Layout.Alignment.ALIGN_CENTER : Layout.Alignment.ALIGN_NORMAL;
        CanvasText tt = new CanvasText(t, title, ThemeManager.HEADLINE, t.onSurface, inner, al, 4);

        scroll = new CanvasScrollContainer(view.getContext());
        float cy = 0;
        java.util.List<CanvasComponent> body = new java.util.ArrayList<>();
        if (message != null && !message.isEmpty()) {
            CanvasText mt = new CanvasText(t, message, ThemeManager.BODY_M, t.onSurfaceVariant, inner);
            mt.place(x, cy);
            cy += mt.height + t.dp(8);
            body.add(mt);
        }
        if (options != null) {
            for (int i = 0; i < options.length; i++) {
                final int idx = i;
                CanvasText ot = new CanvasText(t, options[i], ThemeManager.BODY_L, t.onSurface, inner - t.dp(32));
                float rh = Math.max(t.dp(52), ot.height + t.dp(20));
                CanvasCard row = new CanvasCard(CanvasCard.OUTLINED);
                row.color = t.surfaceHigh;
                row.radius = 12;
                row.at(x, cy, inner, rh);
                row.desc = options[i];
                row.onClick = () -> {
                    view.dismissDialog();
                    if (onChoice != null) onChoice.onChosen(idx);
                };
                ot.desc = null;
                ot.place(x + t.dp(16), cy + (rh - ot.height) / 2f);
                body.add(row);
                body.add(ot);
                cy += rh + t.dp(8);
            }
        }
        float buttonsH = positive != null || negative != null ? t.dp(48) : 0;
        float headH = (ic != null ? t.dp(40) : 0) + tt.height + t.dp(16);
        float maxBody = h - insetTop - insetBottom - t.dp(48) - pad * 2 - headH - buttonsH - t.dp(16);
        float bodyH = Math.min(cy, Math.max(t.dp(80), maxBody));
        float total = pad + headH + bodyH + (buttonsH > 0 ? t.dp(16) + buttonsH : 0) + pad - (buttonsH > 0 ? t.dp(8) : 0);
        float top = anchorBottom ? h - insetBottom - t.dp(16) - total
                : insetTop + (h - insetTop - insetBottom - total) / 2f;
        card.set(left, top, left + cw, top + total);

        float y = top + pad;
        if (ic != null) {
            ic.at(left + cw / 2f - t.dp(13), y, t.dp(26), t.dp(26));
            chrome.add(ic);
            y += t.dp(40);
        }
        tt.place(x, y);
        chrome.add(tt);
        y += tt.height + t.dp(16);
        scroll.at(left, y, cw, bodyH);
        for (CanvasComponent c : body) {
            c.b.offset(0, y);
            scroll.add(c);
        }
        scroll.contentHeight = cy;
        y += bodyH + t.dp(16);
        float bx = left + cw - pad;
        if (positive != null) {
            CanvasButton pb = new CanvasButton(CanvasButton.TEXT, positive, -1, () -> {
                view.dismissDialog();
                if (onPositive != null) onPositive.run();
            });
            float bw = pb.preferredWidth(t) - t.dp(24);
            pb.at(bx - bw, y, bw, t.dp(48));
            chrome.add(pb);
            bx -= bw + t.dp(8);
        }
        if (negative != null) {
            CanvasButton nb = new CanvasButton(CanvasButton.TEXT, negative, -1, () -> {
                view.dismissDialog();
                if (onNegative != null) onNegative.run();
            });
            float bw = nb.preferredWidth(t) - t.dp(24);
            nb.at(bx - bw, y, bw, t.dp(48));
            chrome.add(nb);
        }
    }

    public void draw(Canvas c, ThemeManager t, float w, float h) {
        t.fill.setColor(ThemeManager.alpha(t.scrim, (anchorBottom ? 0.2f : 0.45f) * appear));
        c.drawRect(0, 0, w, h, t.fill);
        int save = -1;
        if (appear < 1f) {
            save = c.saveLayerAlpha(card.left, card.top - t.dp(24), card.right, card.bottom + t.dp(24),
                    (int) (255 * appear));
            float s = 0.92f + 0.08f * appear;
            c.scale(s, s, card.centerX(), card.centerY());
        }
        t.fill.setColor(t.surfaceHigh);
        if (anchorBottom) t.fill.setShadowLayer(t.dp(10), 0, t.dp(3), 0x50000000);
        c.drawRoundRect(card, t.dp(28), t.dp(28), t.fill);
        t.fill.clearShadowLayer();
        drawAll(c, t);
        if (save >= 0) c.restoreToCount(save);
    }

    @Override
    public boolean touch(MotionEvent e) {
        if (e.getActionMasked() == MotionEvent.ACTION_DOWN && !card.contains(e.getX(), e.getY())) {
            if (cancelable) view.dismissDialog();
            return true;
        }
        return super.touch(e);
    }
}
