package com.ccs.deductive_case_files.ui;

import android.graphics.Canvas;
import android.graphics.RectF;
import android.text.TextPaint;
import android.text.TextUtils;

import com.ccs.deductive_case_files.game.CaseModel;
import com.ccs.deductive_case_files.game.ClueModel;
import com.ccs.deductive_case_files.game.DeductionGrid;

import java.util.List;

/**
 * Interactive logic matrix. Blocks are arranged as the classic staircase: the first category
 * runs down the left, the others across the top, with the remaining pairs below.
 * Tap cycles a cell: empty → ✕ → ✓ → empty. Wide matrices pan horizontally.
 */
public final class CanvasGrid extends CanvasComponent {
    public interface Listener {
        void onMarked(byte value);
    }

    private static final byte FLAG_ERROR = 1, FLAG_HINT = 2;

    private final GameCanvasView view;
    private final CaseModel m;
    private final DeductionGrid g;
    private final DeductionGrid flags;
    private final int n, k, blocks;
    private final Listener listener;
    private final String[] stateNames;

    private float cs, strip, hw, hh, cellsLeft, cellsTop, viewW, panX, maxPan;
    private String[][] labels;
    private String[] catLabels;
    private int animA = -1, animB = -1;
    private float animScale = 1f;
    private boolean locked;
    private int onlyA = -1, onlyB = -1;

    public CanvasGrid(GameCanvasView view, CaseModel m, DeductionGrid g, String[] stateNames, Listener l) {
        this.view = view;
        this.m = m;
        this.g = g;
        this.n = m.n;
        this.k = m.cats;
        this.blocks = k - 1;
        this.flags = new DeductionGrid(n, k);
        this.listener = l;
        this.stateNames = stateNames;
        onClick = () -> {
        };
    }

    private int rowCat(int ri) {
        return ri == 0 ? 0 : k - ri;
    }

    private int colCat(int ci) {
        return ci + 1;
    }

    private boolean exists(int ri, int ci) {
        return ci < blocks - ri;
    }

    /** Computes metrics for the available width and positions the grid; returns its bottom. */
    public float layout(ThemeManager t, float x, float y, float availW) {
        TextPaint p = t.text(ThemeManager.LABEL_M, 0);
        strip = t.textHeight(ThemeManager.LABEL_S) + t.dp(8);
        float maxRow = 0, maxCol = 0;
        for (int c = 0; c < k; c++)
            for (int i = 0; i < n; i++) {
                float w = p.measureText(m.items[c][i]);
                if (c == 0 || c >= 2) maxRow = Math.max(maxRow, w);
                if (c >= 1) maxCol = Math.max(maxCol, w);
            }
        hw = Math.min(maxRow + t.dp(14), availW * 0.32f);
        hh = Math.min(maxCol + t.dp(14), t.dp(124));
        float availCells = availW - strip - hw;
        float minCell = Math.max(t.dp(34), t.textHeight(ThemeManager.LABEL_M) + t.dp(10));
        cs = Math.max(minCell, Math.min(t.dp(50), availCells / (blocks * n)));
        float cellsW = blocks * n * cs;
        viewW = Math.min(cellsW, availCells);
        maxPan = cellsW - viewW;
        panX = Math.max(0, Math.min(maxPan, panX));
        float total = strip + hw + viewW;
        float left = x + (availW - total) / 2f;
        cellsLeft = left + strip + hw;
        cellsTop = y + strip + hh;
        b.set(left, y, left + total, cellsTop + blocks * n * cs);

        labels = new String[k][n];
        catLabels = new String[k];
        TextPaint sp = t.text(ThemeManager.LABEL_S, 0);
        for (int c = 0; c < k; c++) {
            catLabels[c] = TextUtils.ellipsize(m.catNames[c].toUpperCase(), sp, n * cs - t.dp(6),
                    TextUtils.TruncateAt.END).toString();
            for (int i = 0; i < n; i++) {
                float lim = (c == 0 ? hw : Math.min(hw, hh)) - t.dp(10);
                if (c == 1) lim = hh - t.dp(10);
                labels[c][i] = TextUtils.ellipsize(m.items[c][i], p, lim, TextUtils.TruncateAt.END).toString();
            }
        }
        return b.bottom;
    }

    /** Guided mode: only the given cell reacts to taps (pass -1 to block every cell). */
    public void restrict(int ea, int eb) {
        locked = ea < 0;
        onlyA = ea;
        onlyB = eb;
    }

    public boolean pannable() {
        return maxPan > 0.5f;
    }

    @Override
    public boolean canPan() {
        return pannable();
    }

    @Override
    public void panBy(float dx) {
        panX = Math.max(0, Math.min(maxPan, panX - dx));
    }

    public void clearFlags() {
        flags.clear();
    }

    /** Marks every cell that contradicts the solution; returns how many there are. */
    public int flagErrors(int[][] sol) {
        flags.clear();
        int e = 0;
        for (int c1 = 0; c1 < k; c1++)
            for (int c2 = c1 + 1; c2 < k; c2++)
                for (int i = 0; i < n; i++)
                    for (int j = 0; j < n; j++) {
                        byte v = g.get(c1, i, c2, j);
                        if (v != DeductionGrid.EMPTY
                                && (v == DeductionGrid.YES) != DeductionGrid.truth(sol, c1, i, c2, j)) {
                            flags.set(c1, i, c2, j, FLAG_ERROR);
                            e++;
                        }
                    }
        return e;
    }

    public void flagHint(int ea, int eb) {
        flags.clear();
        flags.set(ea, eb, FLAG_HINT);
        // bring the hinted cell into view
        int c1 = ClueModel.cat(ea), c2 = ClueModel.cat(eb);
        int colCat = c1 == 0 ? c2 : Math.min(c1, c2);
        int colIdx = colCat == c1 ? ClueModel.idx(ea) : ClueModel.idx(eb);
        float x = ((colCat - 1) * n + colIdx) * cs;
        if (x < panX) panX = x;
        else if (x + cs > panX + viewW) panX = x + cs - viewW;
        panX = Math.max(0, Math.min(maxPan, panX));
    }

    /** Applies a mark programmatically (used by "apply hint"). */
    public void mark(int ea, int eb, byte v) {
        g.set(ea, eb, v);
        if (v == DeductionGrid.YES) autoCross(ClueModel.cat(ea), ClueModel.idx(ea), ClueModel.cat(eb), ClueModel.idx(eb));
        pop(ea, eb);
    }

    private void pop(int ea, int eb) {
        animA = ea;
        animB = eb;
        view.anim.run(0.5f, 1f, 160, v -> animScale = v);
    }

    private void autoCross(int c1, int i, int c2, int j) {
        for (int x = 0; x < n; x++) {
            if (x != j && g.get(c1, i, c2, x) == DeductionGrid.EMPTY) g.set(c1, i, c2, x, DeductionGrid.NO);
            if (x != i && g.get(c1, x, c2, j) == DeductionGrid.EMPTY) g.set(c1, x, c2, j, DeductionGrid.NO);
        }
    }

    @Override
    public boolean tap(float x, float y) {
        if (x < cellsLeft || y < cellsTop || x > cellsLeft + viewW) return false;
        int cx = (int) ((x - cellsLeft + panX) / cs), cy = (int) ((y - cellsTop) / cs);
        int ci = cx / n, ri = cy / n;
        if (ci >= blocks || ri >= blocks || !exists(ri, ci)) return false;
        int c1 = rowCat(ri), i = cy % n, c2 = colCat(ci), j = cx % n;
        if (locked) return false;
        if (onlyA >= 0) {
            int a = ClueModel.ent(c1, i), bb = ClueModel.ent(c2, j);
            if (!((a == onlyA && bb == onlyB) || (a == onlyB && bb == onlyA))) return false;
        }
        byte v = g.get(c1, i, c2, j);
        v = v == DeductionGrid.EMPTY ? DeductionGrid.NO : v == DeductionGrid.NO ? DeductionGrid.YES : DeductionGrid.EMPTY;
        g.set(c1, i, c2, j, v);
        flags.set(c1, i, c2, j, (byte) 0);
        if (v == DeductionGrid.YES) autoCross(c1, i, c2, j);
        pop(ClueModel.ent(c1, i), ClueModel.ent(c2, j));
        listener.onMarked(v);
        return true;
    }

    @Override
    public void draw(Canvas c, ThemeManager t) {
        float line = t.dp(1), bs = n * cs;
        TextPaint lp = t.text(ThemeManager.LABEL_M, t.onSurface);
        float base = t.baseline(ThemeManager.LABEL_M, 0);

        // row headers (fixed while panning)
        for (int ri = 0; ri < blocks; ri++) {
            int cat = rowCat(ri);
            float top = cellsTop + ri * bs;
            t.fill.setColor(t.secondaryContainer);
            t.rect.set(b.left, top + line, b.left + strip, top + bs - line);
            c.drawRoundRect(t.rect, t.dp(3), t.dp(3), t.fill);
            c.save();
            c.rotate(-90, b.left + strip / 2f, top + bs / 2f);
            TextPaint sp = t.text(ThemeManager.LABEL_S, t.onSecondaryContainer);
            c.drawText(catLabels[cat], b.left + strip / 2f - sp.measureText(catLabels[cat]) / 2f,
                    t.baseline(ThemeManager.LABEL_S, top + bs / 2f), sp);
            c.restore();
            t.text(ThemeManager.LABEL_M, t.onSurface);
            for (int i = 0; i < n; i++) {
                String s = labels[cat][i];
                c.drawText(s, cellsLeft - t.dp(6) - lp.measureText(s), top + i * cs + cs / 2f + base, lp);
            }
        }

        c.save();
        c.clipRect(cellsLeft - line, b.top, cellsLeft + viewW + line, b.bottom + line);
        c.translate(cellsLeft - panX, 0);

        // column headers
        for (int ci = 0; ci < blocks; ci++) {
            int cat = colCat(ci);
            float left = ci * bs;
            t.fill.setColor(t.secondaryContainer);
            t.rect.set(left + line, b.top, left + bs - line, b.top + strip);
            c.drawRoundRect(t.rect, t.dp(3), t.dp(3), t.fill);
            TextPaint sp = t.text(ThemeManager.LABEL_S, t.onSecondaryContainer);
            c.drawText(catLabels[cat], left + bs / 2f - sp.measureText(catLabels[cat]) / 2f,
                    t.baseline(ThemeManager.LABEL_S, b.top + strip / 2f), sp);
            t.text(ThemeManager.LABEL_M, t.onSurface);
            for (int j = 0; j < n; j++) {
                float cx = left + j * cs + cs / 2f;
                c.save();
                c.rotate(-90, cx, cellsTop - t.dp(6));
                c.drawText(labels[cat][j], cx, cellsTop - t.dp(6) + base, lp);
                c.restore();
            }
        }

        // blocks
        for (int ri = 0; ri < blocks; ri++) {
            for (int ci = 0; ci < blocks; ci++) {
                if (!exists(ri, ci)) continue;
                int c1 = rowCat(ri), c2 = colCat(ci);
                float left = ci * bs, top = cellsTop + ri * bs;
                t.fill.setColor(t.surfaceLowest);
                c.drawRect(left, top, left + bs, top + bs, t.fill);
                for (int i = 0; i < n; i++)
                    for (int j = 0; j < n; j++)
                        drawCell(c, t, c1, i, c2, j, left + j * cs, top + i * cs);
                t.stroke.setColor(t.outlineVariant);
                t.stroke.setStrokeWidth(line);
                for (int x = 1; x < n; x++) {
                    c.drawLine(left + x * cs, top, left + x * cs, top + bs, t.stroke);
                    c.drawLine(left, top + x * cs, left + bs, top + x * cs, t.stroke);
                }
                t.stroke.setColor(t.outline);
                t.stroke.setStrokeWidth(t.dp(1.5f));
                c.drawRect(left, top, left + bs, top + bs, t.stroke);
            }
        }
        c.restore();

        if (maxPan > 0.5f) {
            float tw = viewW * viewW / (viewW + maxPan), tx = cellsLeft + (viewW - tw) * (panX / maxPan);
            t.fill.setColor(ThemeManager.alpha(t.onSurfaceVariant, 0.4f));
            t.rect.set(tx, b.bottom + t.dp(5), tx + tw, b.bottom + t.dp(8));
            c.drawRoundRect(t.rect, t.dp(2), t.dp(2), t.fill);
        }
    }

    private void drawCell(Canvas c, ThemeManager t, int c1, int i, int c2, int j, float x, float y) {
        byte v = g.get(c1, i, c2, j), f = flags.get(c1, i, c2, j);
        if (v == DeductionGrid.EMPTY && f == 0) return;
        float cx = x + cs / 2f, cy = y + cs / 2f, sc = 1f;
        if (animA >= 0 && animScale < 1f) {
            int a = ClueModel.ent(c1, i), bb = ClueModel.ent(c2, j);
            if ((a == animA && bb == animB) || (a == animB && bb == animA)) sc = animScale;
        }
        if (f == FLAG_ERROR) {
            t.fill.setColor(t.errorContainer);
            c.drawRect(x, y, x + cs, y + cs, t.fill);
        } else if (f == FLAG_HINT) {
            t.fill.setColor(t.tertiaryContainer);
            c.drawRect(x, y, x + cs, y + cs, t.fill);
            t.stroke.setColor(t.tertiary);
            t.stroke.setStrokeWidth(t.dp(2.5f));
            float in = t.dp(2);
            c.drawRect(x + in, y + in, x + cs - in, y + cs - in, t.stroke);
        }
        if (v == DeductionGrid.YES) {
            float r = (cs / 2f - t.dp(3.5f)) * sc;
            t.fill.setColor(f == FLAG_ERROR ? t.error : t.primary);
            t.rect.set(cx - r, cy - r, cx + r, cy + r);
            c.drawRoundRect(t.rect, t.dp(3), t.dp(3), t.fill);
            t.stroke.setColor(f == FLAG_ERROR ? t.onError : t.onPrimary);
            t.stroke.setStrokeWidth(t.dp(2.4f));
            float u = cs * 0.2f * sc;
            c.drawLine(cx - u, cy + u * 0.1f, cx - u * 0.25f, cy + u * 0.8f, t.stroke);
            c.drawLine(cx - u * 0.25f, cy + u * 0.8f, cx + u, cy - u * 0.7f, t.stroke);
        } else if (v == DeductionGrid.NO) {
            t.stroke.setColor(f == FLAG_ERROR ? t.error : ThemeManager.alpha(t.onSurfaceVariant, 0.75f));
            t.stroke.setStrokeWidth(t.dp(2f));
            float u = cs * 0.19f * sc;
            c.drawLine(cx - u, cy - u, cx + u, cy + u, t.stroke);
            c.drawLine(cx + u, cy - u, cx - u, cy + u, t.stroke);
        }
    }

    @Override
    public void collectA11y(List<A11yNode> out, float dy) {
        RectF r = new RectF();
        for (int ri = 0; ri < blocks; ri++)
            for (int ci = 0; ci < blocks; ci++) {
                if (!exists(ri, ci)) continue;
                int c1 = rowCat(ri), c2 = colCat(ci);
                for (int i = 0; i < n; i++)
                    for (int j = 0; j < n; j++) {
                        float x = cellsLeft - panX + (ci * n + j) * cs, y = cellsTop + (ri * n + i) * cs;
                        if (x < cellsLeft - 1 || x + cs > cellsLeft + viewW + 1) continue;
                        r.set(x, y, x + cs, y + cs);
                        String text = m.items[c1][i] + " — " + m.items[c2][j] + ": "
                                + stateNames[g.get(c1, i, c2, j)];
                        out.add(new A11yNode(r, dy, text, true, this, r.centerX(), r.centerY()));
                    }
            }
    }
}
