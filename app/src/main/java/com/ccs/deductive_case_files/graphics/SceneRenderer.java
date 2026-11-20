package com.ccs.deductive_case_files.graphics;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;

import com.ccs.deductive_case_files.R;

/** Draws the Android vector resources exported from the game's SVG artwork. */
public final class SceneRenderer {
    private static final Path CLIP = new Path();
    private static final SparseArray<Drawable> IMAGES = new SparseArray<>();

    private SceneRenderer() {
    }

    public static void draw(Canvas canvas, RectF bounds, float radius, Resources resources, int id, boolean cover) {
        if (bounds.isEmpty() || id == 0) return;
        Drawable image = IMAGES.get(id);
        if (image == null) {
            image = resources.getDrawable(id, null).mutate();
            IMAGES.put(id, image);
        }
        int sourceWidth = image.getIntrinsicWidth(), sourceHeight = image.getIntrinsicHeight();
        float sx = bounds.width() / sourceWidth, sy = bounds.height() / sourceHeight;
        float scale = cover ? Math.max(sx, sy) : Math.min(sx, sy);
        int width = Math.round(sourceWidth * scale), height = Math.round(sourceHeight * scale);
        CLIP.rewind();
        CLIP.addRoundRect(bounds, radius, radius, Path.Direction.CW);
        int saved = canvas.save();
        canvas.clipPath(CLIP);
        if (!cover) canvas.drawColor(0xFFEDE1CC);
        canvas.translate(bounds.centerX() - width / 2f, bounds.centerY() - height / 2f);
        image.setBounds(0, 0, width, height);
        image.draw(canvas);
        canvas.restoreToCount(saved);
    }

    public static int evidenceResource(Resources resources, String spec) {
        String[] specs = resources.getStringArray(R.array.evidence_specs);
        for (int i = 0; i < specs.length; i++) {
            if (!specs[i].equals(spec)) continue;
            TypedArray images = resources.obtainTypedArray(R.array.evidence_images);
            try {
                return images.getResourceId(i, 0);
            } finally {
                images.recycle();
            }
        }
        return 0;
    }

    public static int sceneResource(String scene) {
        if (scene == null) return R.drawable.scene_manor;
        switch (scene) {
            case "HERO": return R.drawable.scene_hero;
            case "CAFE": return R.drawable.scene_cafe;
            case "TRAIN": return R.drawable.scene_train;
            case "LIBRARY": return R.drawable.scene_library;
            case "MUSEUM": return R.drawable.scene_museum;
            case "THEATER": return R.drawable.scene_theater;
            case "HOTEL": return R.drawable.scene_hotel;
            case "OFFICE": return R.drawable.scene_office;
            case "GARDEN": return R.drawable.scene_garden;
            case "HARBOR": return R.drawable.scene_harbor;
            case "LAB": return R.drawable.scene_lab;
            case "OBSERVATORY": return R.drawable.scene_observatory;
            default: return R.drawable.scene_manor;
        }
    }
}
