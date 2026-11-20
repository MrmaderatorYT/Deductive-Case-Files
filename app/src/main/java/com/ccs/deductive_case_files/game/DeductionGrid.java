package com.ccs.deductive_case_files.game;

import java.util.Arrays;

/** Marks of a logic matrix: one n×n block for every pair of categories. */
public final class DeductionGrid {
    public static final byte EMPTY = 0, NO = 1, YES = 2;

    public final int n, cats;
    private final byte[] cells;

    public DeductionGrid(int n, int cats) {
        this.n = n;
        this.cats = cats;
        cells = new byte[cats * cats * n * n];
    }

    private int at(int c1, int i, int c2, int j) {
        if (c1 > c2) return ((c2 * cats + c1) * n + j) * n + i;
        return ((c1 * cats + c2) * n + i) * n + j;
    }

    public byte get(int c1, int i, int c2, int j) {
        return cells[at(c1, i, c2, j)];
    }

    public void set(int c1, int i, int c2, int j, byte v) {
        cells[at(c1, i, c2, j)] = v;
    }

    public byte get(int ea, int eb) {
        return cells[at(ea >> 4, ea & 15, eb >> 4, eb & 15)];
    }

    public void set(int ea, int eb, byte v) {
        cells[at(ea >> 4, ea & 15, eb >> 4, eb & 15)] = v;
    }

    public void clear() {
        Arrays.fill(cells, EMPTY);
    }

    public boolean isEmpty() {
        for (byte b : cells) if (b != EMPTY) return false;
        return true;
    }

    public DeductionGrid copy() {
        DeductionGrid g = new DeductionGrid(n, cats);
        System.arraycopy(cells, 0, g.cells, 0, cells.length);
        return g;
    }

    public void copyFrom(DeductionGrid o) {
        System.arraycopy(o.cells, 0, cells, 0, cells.length);
    }

    /** True value of a cell in the given solution (solution[cat][group] = item index). */
    public static boolean truth(int[][] sol, int c1, int i, int c2, int j) {
        int[] a = sol[c1], b = sol[c2];
        for (int g = 0; g < a.length; g++) if (a[g] == i) return b[g] == j;
        return false;
    }

    /** Number of marks that contradict the solution. */
    public int countErrors(int[][] sol) {
        int e = 0;
        for (int c1 = 0; c1 < cats; c1++)
            for (int c2 = c1 + 1; c2 < cats; c2++)
                for (int i = 0; i < n; i++)
                    for (int j = 0; j < n; j++) {
                        byte v = get(c1, i, c2, j);
                        if (v != EMPTY && (v == YES) != truth(sol, c1, i, c2, j)) e++;
                    }
        return e;
    }

    /** Complete when every suspect has the right ✓ in every other category. */
    public boolean isComplete(int[][] sol) {
        for (int c = 1; c < cats; c++)
            for (int g = 0; g < n; g++)
                if (get(0, g, c, sol[c][g]) != YES) return false;
        return true;
    }

    public String serialize() {
        char[] out = new char[cells.length];
        for (int i = 0; i < out.length; i++) out[i] = (char) ('0' + cells[i]);
        return new String(out);
    }

    public void deserialize(String s) {
        if (s == null || s.length() != cells.length) return;
        for (int i = 0; i < cells.length; i++) {
            int v = s.charAt(i) - '0';
            cells[i] = (byte) (v < 0 || v > 2 ? 0 : v);
        }
    }
}
