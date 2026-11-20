package com.ccs.deductive_case_files.ui;

import android.graphics.RectF;

/** Virtual accessibility node describing one interactive or readable Canvas element. */
public final class A11yNode {
    public final RectF r = new RectF();
    public final String text;
    public final boolean clickable;
    public final CanvasComponent target;
    /** Tap point in the target's own coordinate space. */
    public final float tx, ty;
    public boolean checkable, checked;

    public A11yNode(RectF bounds, float dy, String text, boolean clickable, CanvasComponent target,
                    float tx, float ty) {
        r.set(bounds);
        r.offset(0, dy);
        this.text = text;
        this.clickable = clickable;
        this.target = target;
        this.tx = tx;
        this.ty = ty;
    }
}
