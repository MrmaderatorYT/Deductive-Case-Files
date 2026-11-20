package com.ccs.deductive_case_files.game;

import com.ccs.deductive_case_files.game.ClueModel.Constraint;

import java.util.ArrayList;
import java.util.List;

/**
 * Exhaustive backtracking solver. Used to prove that a case has exactly one solution and to
 * derive that solution (it is never stored in the data files).
 */
public final class PuzzleSolver {
    private final int n, cats;
    private final Constraint[] cons;
    private final int[] maxCat;
    private final int[][] sol, inv;
    private final boolean[][] used;
    private int limit, found;
    private int[][] first;

    private PuzzleSolver(CaseModel m) {
        n = m.n;
        cats = m.cats;
        List<Constraint> all = new ArrayList<>();
        for (ClueModel c : m.clues) for (Constraint p : c.parts) all.add(p);
        cons = all.toArray(new Constraint[0]);
        maxCat = new int[cons.length];
        for (int i = 0; i < cons.length; i++) {
            Constraint c = cons[i];
            int mx = 0;
            boolean ord = c.type == ClueModel.LT || c.type == ClueModel.NEXT;
            for (int k = 0; k < c.a.length; k++) {
                int cat = ord && k == c.a.length - 1 ? c.a[k] : ClueModel.cat(c.a[k]);
                mx = Math.max(mx, cat);
            }
            maxCat[i] = mx;
        }
        sol = new int[cats][n];
        inv = new int[cats][n];
        used = new boolean[cats][n];
        for (int g = 0; g < n; g++) {
            sol[0][g] = g;
            inv[0][g] = g;
        }
    }

    /** Counts solutions up to {@code limit}. */
    public static int count(CaseModel m, int limit) {
        PuzzleSolver s = new PuzzleSolver(m);
        s.limit = limit;
        s.search(1, 0);
        return s.found;
    }

    /** Returns the solution if it is unique, otherwise null. */
    public static int[][] solve(CaseModel m) {
        PuzzleSolver s = new PuzzleSolver(m);
        s.limit = 2;
        if (!s.check(0)) return null;
        s.search(1, 0);
        return s.found == 1 ? s.first : null;
    }

    private void search(int cat, int g) {
        if (found >= limit) return;
        if (cat == cats) {
            if (found == 0) {
                first = new int[cats][];
                for (int c = 0; c < cats; c++) first[c] = sol[c].clone();
            }
            found++;
            return;
        }
        if (g == n) {
            if (check(cat)) search(cat + 1, 0);
            return;
        }
        for (int i = 0; i < n; i++) {
            if (used[cat][i]) continue;
            used[cat][i] = true;
            sol[cat][g] = i;
            inv[cat][i] = g;
            search(cat, g + 1);
            used[cat][i] = false;
        }
    }

    private int grp(int e) {
        return inv[e >> 4][e & 15];
    }

    /** Checks the constraints that become decidable once category {@code cat} is assigned. */
    private boolean check(int cat) {
        for (int i = 0; i < cons.length; i++) {
            if (maxCat[i] != cat) continue;
            if (!holds(cons[i])) return false;
        }
        return true;
    }

    private boolean holds(Constraint c) {
        int[] a = c.a;
        switch (c.type) {
            case ClueModel.IS:
                return grp(a[0]) == grp(a[1]);
            case ClueModel.NOT:
                return grp(a[0]) != grp(a[1]);
            case ClueModel.EITHER:
                return (grp(a[0]) == grp(a[1])) ^ (grp(a[0]) == grp(a[2]));
            case ClueModel.PAIR: {
                int x = grp(a[0]), y = grp(a[1]), z = grp(a[2]), w = grp(a[3]);
                return (x == z && y == w) || (x == w && y == z);
            }
            case ClueModel.LT:
                return sol[a[2]][grp(a[0])] < sol[a[2]][grp(a[1])];
            case ClueModel.NEXT:
                return sol[a[2]][grp(a[1])] == sol[a[2]][grp(a[0])] + 1;
            case ClueModel.DIFF:
                for (int i = 0; i < a.length; i++)
                    for (int j = i + 1; j < a.length; j++)
                        if (grp(a[i]) == grp(a[j])) return false;
                return true;
            default:
                return true;
        }
    }
}
