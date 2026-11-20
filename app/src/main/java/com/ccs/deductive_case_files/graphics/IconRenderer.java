package com.ccs.deductive_case_files.graphics;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

/** Original outline icons drawn with Canvas on a 24-unit grid; no bitmap assets. */
public final class IconRenderer {
    public static final int BACK = 0, HOME = 1, FOLDER = 2, SETTINGS = 3, CHECK = 4, CLOSE = 5,
            BULB = 6, RESTART = 7, LOCK = 8, STAR = 9, STAR_OUTLINE = 10, CHEVRON = 11, SEARCH = 12,
            DOC = 13, PLAY = 14, GLOBE = 15, PALETTE = 16, MOTION = 17, SOUND = 18, DELETE = 19,
            PERSON = 20, PIN = 21, TAG = 22, CLOCK = 23, GRID = 24, UNLOCK = 25;

    private static final Paint P = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Path PATH = new Path();
    private static final RectF R = new RectF();

    private IconRenderer() {
    }

    public static void draw(Canvas c, int icon, float cx, float cy, float size, int color) {
        float s = size / 24f;
        c.save();
        c.translate(cx - size / 2f, cy - size / 2f);
        c.scale(s, s);
        P.setColor(color);
        P.setStyle(Paint.Style.STROKE);
        P.setStrokeWidth(2f);
        P.setStrokeCap(Paint.Cap.ROUND);
        P.setStrokeJoin(Paint.Join.ROUND);
        PATH.rewind();
        switch (icon) {
            case BACK:
                line(c, 20, 12, 5, 12);
                poly(c, false, 11, 6, 5, 12, 11, 18);
                break;
            case HOME:
                poly(c, true, 4, 11, 12, 4, 20, 11, 20, 20, 14, 20, 14, 14, 10, 14, 10, 20, 4, 20);
                break;
            case FOLDER:
                poly(c, true, 3, 6, 9.5f, 6, 11.5f, 8.5f, 21, 8.5f, 21, 19, 3, 19);
                break;
            case SETTINGS:
                c.drawCircle(12, 12, 3.2f, P);
                for (int i = 0; i < 8; i++) {
                    double a = Math.PI / 4 * i;
                    float co = (float) Math.cos(a), si = (float) Math.sin(a);
                    c.drawLine(12 + co * 6.6f, 12 + si * 6.6f, 12 + co * 9.2f, 12 + si * 9.2f, P);
                }
                c.drawCircle(12, 12, 6.6f, P);
                break;
            case CHECK:
                poly(c, false, 5, 12.5f, 10, 17.5f, 19, 7);
                break;
            case CLOSE:
                line(c, 6, 6, 18, 18);
                line(c, 18, 6, 6, 18);
                break;
            case BULB:
                R.set(6, 3, 18, 15);
                c.drawArc(R, 140, 260, false, P);
                poly(c, false, 7.4f, 12.9f, 9.5f, 16.5f, 14.5f, 16.5f, 16.6f, 12.9f);
                line(c, 9.5f, 19, 14.5f, 19);
                line(c, 10.5f, 21.5f, 13.5f, 21.5f);
                break;
            case RESTART:
                R.set(4.5f, 4.5f, 19.5f, 19.5f);
                c.drawArc(R, -50, 300, false, P);
                poly(c, false, 16.8f, 2.5f, 17.2f, 6.9f, 12.8f, 7.2f);
                break;
            case LOCK:
            case UNLOCK:
                R.set(5, 10.5f, 19, 20.5f);
                c.drawRoundRect(R, 2, 2, P);
                R.set(8, 3.5f, 16, 12);
                if (icon == LOCK) c.drawArc(R, 180, 180, false, P);
                else c.drawArc(R, 180, 150, false, P);
                line(c, 8, 7.75f, 8, 10.5f);
                if (icon == LOCK) line(c, 16, 7.75f, 16, 10.5f);
                c.drawCircle(12, 15.5f, 0.8f, P);
                break;
            case STAR:
            case STAR_OUTLINE:
                for (int i = 0; i < 10; i++) {
                    double a = -Math.PI / 2 + Math.PI / 5 * i;
                    float r = (i & 1) == 0 ? 9.2f : 4f;
                    float x = 12 + (float) Math.cos(a) * r, y = 12.6f + (float) Math.sin(a) * r;
                    if (i == 0) PATH.moveTo(x, y);
                    else PATH.lineTo(x, y);
                }
                PATH.close();
                if (icon == STAR) P.setStyle(Paint.Style.FILL_AND_STROKE);
                P.setStrokeWidth(1.6f);
                c.drawPath(PATH, P);
                break;
            case CHEVRON:
                poly(c, false, 9, 6, 15, 12, 9, 18);
                break;
            case SEARCH:
                c.drawCircle(10.5f, 10.5f, 6, P);
                line(c, 15, 15, 20.5f, 20.5f);
                break;
            case DOC:
                poly(c, true, 6, 3, 14, 3, 19, 8, 19, 21, 6, 21);
                poly(c, false, 14, 3, 14, 8, 19, 8);
                line(c, 9, 13, 16, 13);
                line(c, 9, 17, 16, 17);
                break;
            case PLAY:
                poly(c, true, 8, 5, 19, 12, 8, 19);
                break;
            case GLOBE:
                c.drawCircle(12, 12, 9, P);
                R.set(8, 3, 16, 21);
                c.drawOval(R, P);
                line(c, 3, 12, 21, 12);
                break;
            case PALETTE:
                c.drawCircle(12, 12, 9, P);
                R.set(3, 3, 21, 21);
                P.setStyle(Paint.Style.FILL);
                c.drawArc(R, 90, 180, true, P);
                break;
            case MOTION:
                c.drawCircle(15, 12, 5.5f, P);
                line(c, 2.5f, 8, 7, 8);
                line(c, 2.5f, 12, 6, 12);
                line(c, 2.5f, 16, 7, 16);
                break;
            case SOUND:
                poly(c, true, 4, 9.5f, 8, 9.5f, 12.5f, 5.5f, 12.5f, 18.5f, 8, 14.5f, 4, 14.5f);
                R.set(11, 8, 17, 16);
                c.drawArc(R, -60, 120, false, P);
                R.set(9, 5, 20.5f, 19);
                c.drawArc(R, -55, 110, false, P);
                break;
            case DELETE:
                line(c, 4.5f, 6.5f, 19.5f, 6.5f);
                poly(c, false, 9, 6.5f, 9.6f, 4, 14.4f, 4, 15, 6.5f);
                poly(c, false, 6.5f, 6.5f, 7.5f, 20.5f, 16.5f, 20.5f, 17.5f, 6.5f);
                line(c, 10.2f, 10.5f, 10.2f, 16.5f);
                line(c, 13.8f, 10.5f, 13.8f, 16.5f);
                break;
            case PERSON:
                c.drawCircle(12, 8, 4, P);
                R.set(4.5f, 14, 19.5f, 27);
                c.drawArc(R, 180, 180, false, P);
                break;
            case PIN:
                R.set(6, 3, 18, 15);
                c.drawArc(R, 150, 240, false, P);
                poly(c, false, 6.8f, 12, 12, 21, 17.2f, 12);
                c.drawCircle(12, 9, 2, P);
                break;
            case TAG:
                poly(c, true, 3.5f, 3.5f, 12, 3.5f, 20.5f, 12, 12, 20.5f, 3.5f, 12);
                c.drawCircle(8, 8, 1.2f, P);
                break;
            case CLOCK:
                c.drawCircle(12, 12, 9, P);
                poly(c, false, 12, 6.5f, 12, 12, 16, 14.5f);
                break;
            case GRID:
                R.set(4, 4, 20, 20);
                c.drawRoundRect(R, 2, 2, P);
                line(c, 9.3f, 4, 9.3f, 20);
                line(c, 14.6f, 4, 14.6f, 20);
                line(c, 4, 9.3f, 20, 9.3f);
                line(c, 4, 14.6f, 20, 14.6f);
                break;
            default:
                break;
        }
        c.restore();
    }

    private static void line(Canvas c, float x1, float y1, float x2, float y2) {
        c.drawLine(x1, y1, x2, y2, P);
    }

    private static void poly(Canvas c, boolean close, float... p) {
        PATH.rewind();
        PATH.moveTo(p[0], p[1]);
        for (int i = 2; i < p.length; i += 2) PATH.lineTo(p[i], p[i + 1]);
        if (close) PATH.close();
        c.drawPath(PATH, P);
    }
}
