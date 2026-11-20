package com.ccs.deductive_case_files.ui;

import android.content.res.Resources;
import android.graphics.Canvas;

import com.ccs.deductive_case_files.graphics.SceneRenderer;

/** Displays a case woodcut or an evidence diagram. */
public final class CanvasIllustration extends CanvasComponent {
    private final Resources resources;
    private final int image;
    private final boolean cover;

    public CanvasIllustration(Resources resources, String scene) {
        this.resources = resources;
        image = SceneRenderer.sceneResource(scene);
        cover = true;
    }

    public CanvasIllustration(Resources resources, String spec, String desc) {
        this.resources = resources;
        image = SceneRenderer.evidenceResource(resources, spec);
        cover = false;
        this.desc = desc;
    }

    @Override
    public void draw(Canvas c, ThemeManager t) {
        SceneRenderer.draw(c, b, t.dp(8), resources, image, cover);
    }
}
