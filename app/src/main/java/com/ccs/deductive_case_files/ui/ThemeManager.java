package com.ccs.deductive_case_files.ui;

import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.DisplayMetrics;

/** Paper and ink palettes, editorial typography and drawing tools shared by the Canvas UI. */
public final class ThemeManager {
    public static ThemeManager get() {
        return INSTANCE;
    }

    // Type scale
    public static final int DISPLAY = 0, HEADLINE = 1, TITLE_L = 2, TITLE_M = 3, TITLE_S = 4,
            BODY_L = 5, BODY_M = 6, BODY_S = 7, LABEL_L = 8, LABEL_M = 9, LABEL_S = 10, MONO = 11;
    private static final float[] SIZES = {36, 26, 22, 16, 14, 16, 14, 12, 14, 12, 11, 15};
    private static final ThemeManager INSTANCE = new ThemeManager();

    public boolean dark;
    public int primary, onPrimary, primaryContainer, onPrimaryContainer;
    public int secondary, onSecondary, secondaryContainer, onSecondaryContainer;
    public int tertiary, onTertiary, tertiaryContainer, onTertiaryContainer;
    public int error, onError, errorContainer, onErrorContainer;
    public int surface, onSurface, surfaceVariant, onSurfaceVariant, outline, outlineVariant;
    public int surfaceLowest, surfaceLow, surfaceContainer, surfaceHigh, surfaceHighest;
    public int inverseSurface, inverseOnSurface, inversePrimary, scrim;

    public float density = 1f, fontScale = 1f;
    private final TextPaint[] text = new TextPaint[SIZES.length];

    /** Shared scratch objects; only used on the UI thread inside draw calls. */
    public final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    public final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    public final Path path = new Path();
    public final RectF rect = new RectF();

    private ThemeManager() {
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setStrokeCap(Paint.Cap.ROUND);
        stroke.setStrokeJoin(Paint.Join.ROUND);
        Typeface sans = Typeface.create("sans-serif", Typeface.NORMAL);
        Typeface medium = Typeface.create("sans-serif-medium", Typeface.NORMAL);
        Typeface serif = Typeface.create(Typeface.SERIF, Typeface.NORMAL);
        for (int i = 0; i < text.length; i++) {
            TextPaint p = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            if (i == DISPLAY || i == HEADLINE || i == TITLE_L) p.setTypeface(serif);
            else if (i == TITLE_M || i == TITLE_S || i >= LABEL_L && i <= LABEL_S) p.setTypeface(medium);
            else if (i == MONO) p.setTypeface(Typeface.MONOSPACE);
            else p.setTypeface(sans);
            if (i >= LABEL_L && i <= LABEL_S) p.setLetterSpacing(0.02f);
            text[i] = p;
        }
        apply(false);
    }

    public void init(DisplayMetrics dm, float fontScale) {
        density = dm.density;
        this.fontScale = fontScale;
        for (int i = 0; i < text.length; i++) text[i].setTextSize(SIZES[i] * density * fontScale);
    }

    public float dp(float v) {
        return v * density;
    }

    public TextPaint text(int style, int color) {
        TextPaint p = text[style];
        p.setColor(color);
        return p;
    }

    public float textHeight(int style) {
        Paint.FontMetrics fm = text[style].getFontMetrics();
        return fm.descent - fm.ascent;
    }

    /** Baseline that vertically centres a single line of the style around cy. */
    public float baseline(int style, float cy) {
        Paint.FontMetrics fm = text[style].getFontMetrics();
        return cy - (fm.ascent + fm.descent) / 2f;
    }

    public static int alpha(int color, float a) {
        int al = Math.round(((color >>> 24) & 0xFF) * Math.max(0f, Math.min(1f, a)));
        return (al << 24) | (color & 0xFFFFFF);
    }

    public static int blend(int c1, int c2, float t) {
        float u = 1f - t;
        int a = Math.round(((c1 >>> 24) & 0xFF) * u + ((c2 >>> 24) & 0xFF) * t);
        int r = Math.round(((c1 >> 16) & 0xFF) * u + ((c2 >> 16) & 0xFF) * t);
        int g = Math.round(((c1 >> 8) & 0xFF) * u + ((c2 >> 8) & 0xFF) * t);
        int b = Math.round((c1 & 0xFF) * u + (c2 & 0xFF) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public void apply(boolean dark) {
        this.dark = dark;
        if (!dark) {
            primary = 0xFF7B302C;
            onPrimary = 0xFFFFF5E6;
            primaryContainer = 0xFFEBD5CA;
            onPrimaryContainer = 0xFF512722;
            secondary = 0xFF475363;
            onSecondary = 0xFFFFF7EB;
            secondaryContainer = 0xFFE2D9CD;
            onSecondaryContainer = 0xFF38424F;
            tertiary = 0xFF755016;
            onTertiary = 0xFFFFF5E6;
            tertiaryContainer = 0xFFF0DFBD;
            onTertiaryContainer = 0xFF4A3319;
            error = 0xFFA3251C;
            onError = 0xFFFFF5EE;
            errorContainer = 0xFFF8DAD1;
            onErrorContainer = 0xFF601411;
            surface = 0xFFF2EADD;
            onSurface = 0xFF191E26;
            surfaceVariant = 0xFFE8DDD0;
            onSurfaceVariant = 0xFF514D47;
            outline = 0xFF817464;
            outlineVariant = 0xFFC9BBA7;
            surfaceLowest = 0xFFFFFAF0;
            surfaceLow = 0xFFF8F1E7;
            surfaceContainer = 0xFFEBE0D0;
            surfaceHigh = 0xFFF7EFE3;
            surfaceHighest = 0xFFE4D8C8;
            inverseSurface = 0xFF222832;
            inverseOnSurface = 0xFFF2EADD;
            inversePrimary = 0xFFE2BA79;
        } else {
            primary = 0xFFE2BA79;
            onPrimary = 0xFF241B14;
            primaryContainer = 0xFF4E3829;
            onPrimaryContainer = 0xFFF3D7AA;
            secondary = 0xFFB8C3D1;
            onSecondary = 0xFF253140;
            secondaryContainer = 0xFF303B49;
            onSecondaryContainer = 0xFFDFE6ED;
            tertiary = 0xFFDBAA73;
            onTertiary = 0xFF332111;
            tertiaryContainer = 0xFF4D3524;
            onTertiaryContainer = 0xFFF1D2A9;
            error = 0xFFF6A89B;
            onError = 0xFF431810;
            errorContainer = 0xFF562924;
            onErrorContainer = 0xFFFFCEC4;
            surface = 0xFF14181F;
            onSurface = 0xFFF1EBE0;
            surfaceVariant = 0xFF333C48;
            onSurfaceVariant = 0xFFC7BDAE;
            outline = 0xFF8F8A86;
            outlineVariant = 0xFF48515D;
            surfaceLowest = 0xFF11151C;
            surfaceLow = 0xFF1B2029;
            surfaceContainer = 0xFF202630;
            surfaceHigh = 0xFF242B35;
            surfaceHighest = 0xFF2E3743;
            inverseSurface = 0xFFF2EADD;
            inverseOnSurface = 0xFF191E26;
            inversePrimary = 0xFF7B302C;
        }
        scrim = 0xFF000000;
    }
}
