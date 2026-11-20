package com.ccs.deductive_case_files.game;

import com.ccs.deductive_case_files.game.ClueModel.Constraint;

import java.util.ArrayList;
import java.util.List;

/**
 * Human-style deduction engine. It derives the next provable mark from the clues and the marks
 * already on the grid, which powers hints, the written explanation and the difficulty grading.
 */
public final class HintSystem {
    public static final int R_CLUE = 0, R_TAKEN = 1, R_ONLY = 2, R_LINK = 3, R_NOCOMMON = 4,
            R_LOGIC = 5, R_CONTRA = 6;

    /** One deduction: cell (a, b) gets value v because of {@code rule}. */
    public static final class Step {
        public final int a, b, rule, clue, via;
        public final byte v;

        Step(int a, int b, byte v, int rule, int clue, int via) {
            this.a = a;
            this.b = b;
            this.v = v;
            this.rule = rule;
            this.clue = clue;
            this.via = via;
        }
    }

    private static final byte EMPTY = DeductionGrid.EMPTY, NO = DeductionGrid.NO, YES = DeductionGrid.YES;

    private final CaseModel m;
    private final int n, k;
    private DeductionGrid g;
    private boolean apply, conflict, changed, useLocked;
    private Step step;

    public HintSystem(CaseModel m) {
        this.m = m;
        n = m.n;
        k = m.cats;
    }

    /**
     * Finds the next deduction for the grid without modifying it.
     *
     * @param useLocked whether clues still hidden behind the evidence task may be used
     */
    public Step next(DeductionGrid grid, boolean lookahead, boolean useLocked) {
        this.useLocked = useLocked;
        g = grid;
        apply = false;
        conflict = false;
        step = null;
        scan();
        if (step != null || conflict || !lookahead) return step;
        return lookahead(grid);
    }

    /** Full chain of deductions from an empty grid. */
    public List<Step> explain() {
        List<Step> out = new ArrayList<>();
        DeductionGrid grid = new DeductionGrid(n, k);
        for (int guard = 0; guard < 2000; guard++) {
            Step s = next(grid, true, true);
            if (s == null) break;
            grid.set(s.a, s.b, s.v);
            out.add(s);
        }
        return out;
    }

    /** 0 = engine cannot finish, 1 = plain propagation, 2 = needs "what if" reasoning. */
    public int grade(int[] lookaheadCount) {
        DeductionGrid grid = new DeductionGrid(n, k);
        int level = 1, la = 0;
        for (int guard = 0; guard < 2000; guard++) {
            Step s = next(grid, true, true);
            if (s == null) break;
            if (s.rule == R_CONTRA) {
                level = 2;
                la++;
            }
            grid.set(s.a, s.b, s.v);
        }
        if (lookaheadCount != null) lookaheadCount[0] = la;
        for (int c1 = 0; c1 < k; c1++)
            for (int c2 = c1 + 1; c2 < k; c2++)
                for (int i = 0; i < n; i++)
                    for (int j = 0; j < n; j++)
                        if (grid.get(c1, i, c2, j) == EMPTY) return 0;
        return level;
    }

    private Step lookahead(DeductionGrid base) {
        for (int pass = 0; pass < 2; pass++) {
            byte assume = pass == 0 ? YES : NO;
            for (int c2 = 1; c2 < k; c2++)
                for (int c1 = 0; c1 < c2; c1++)
                    for (int i = 0; i < n; i++)
                        for (int j = 0; j < n; j++) {
                            if (base.get(c1, i, c2, j) != EMPTY) continue;
                            DeductionGrid t = base.copy();
                            t.set(c1, i, c2, j, assume);
                            if (contradicts(t)) {
                                return new Step(ClueModel.ent(c1, i), ClueModel.ent(c2, j),
                                        assume == YES ? NO : YES, R_CONTRA, -1, -1);
                            }
                        }
        }
        return null;
    }

    private boolean contradicts(DeductionGrid t) {
        g = t;
        apply = true;
        conflict = false;
        do {
            changed = false;
            scan();
        } while (changed && !conflict);
        apply = false;
        return conflict;
    }

    /** Returns true when scanning must stop (a step was found or a conflict detected). */
    private boolean emit(int a, int b, byte v, int rule, int clue, int via) {
        byte cur = g.get(a, b);
        if (cur == v) return false;
        if (cur != EMPTY) {
            conflict = true;
            return true;
        }
        if (apply) {
            g.set(a, b, v);
            changed = true;
            return false;
        }
        step = new Step(a, b, v, rule, clue, via);
        return true;
    }

    private void scan() {
        if (direct() || taken() || only() || link() || noCommon()) return;
        complex();
    }

    private boolean direct() {
        for (int ci = 0; ci < m.clues.size(); ci++) {
            ClueModel c = m.clues.get(ci);
            if (c.locked && !useLocked) continue;
            for (Constraint p : c.parts) {
                int[] a = p.a;
                if (p.type == ClueModel.IS) {
                    if (emit(a[0], a[1], YES, R_CLUE, ci, -1)) return true;
                } else if (p.type == ClueModel.NOT) {
                    if (emit(a[0], a[1], NO, R_CLUE, ci, -1)) return true;
                } else if (p.type == ClueModel.DIFF) {
                    for (int i = 0; i < a.length; i++)
                        for (int j = i + 1; j < a.length; j++)
                            if (ClueModel.cat(a[i]) != ClueModel.cat(a[j])
                                    && emit(a[i], a[j], NO, R_CLUE, ci, -1)) return true;
                }
            }
        }
        return false;
    }

    private boolean taken() {
        for (int c1 = 0; c1 < k; c1++)
            for (int c2 = c1 + 1; c2 < k; c2++)
                for (int i = 0; i < n; i++)
                    for (int j = 0; j < n; j++) {
                        if (g.get(c1, i, c2, j) != YES) continue;
                        int a = ClueModel.ent(c1, i), b = ClueModel.ent(c2, j);
                        for (int x = 0; x < n; x++) {
                            if (x != j && emit(a, ClueModel.ent(c2, x), NO, R_TAKEN, -1, b)) return true;
                            if (x != i && emit(ClueModel.ent(c1, x), b, NO, R_TAKEN, -1, a)) return true;
                        }
                    }
        return false;
    }

    private boolean only() {
        for (int c1 = 0; c1 < k; c1++)
            for (int i = 0; i < n; i++)
                for (int c2 = 0; c2 < k; c2++) {
                    if (c2 == c1) continue;
                    int yes = 0, open = 0, last = -1;
                    for (int j = 0; j < n; j++) {
                        byte v = g.get(c1, i, c2, j);
                        if (v == YES) yes++;
                        else if (v == EMPTY) {
                            open++;
                            last = j;
                        }
                    }
                    if (yes > 1 || (yes == 0 && open == 0)) {
                        conflict = true;
                        return true;
                    }
                    if (yes == 0 && open == 1
                            && emit(ClueModel.ent(c1, i), ClueModel.ent(c2, last), YES, R_ONLY, -1, c2))
                        return true;
                }
        return false;
    }

    private boolean link() {
        for (int c1 = 0; c1 < k; c1++)
            for (int c2 = c1 + 1; c2 < k; c2++)
                for (int i = 0; i < n; i++)
                    for (int j = 0; j < n; j++) {
                        if (g.get(c1, i, c2, j) != YES) continue;
                        int a = ClueModel.ent(c1, i), b = ClueModel.ent(c2, j);
                        for (int c3 = 0; c3 < k; c3++) {
                            if (c3 == c1 || c3 == c2) continue;
                            for (int x = 0; x < n; x++) {
                                int z = ClueModel.ent(c3, x);
                                byte va = g.get(a, z), vb = g.get(b, z);
                                if (va != EMPTY && vb == EMPTY) {
                                    if (emit(b, z, va, R_LINK, -1, a)) return true;
                                } else if (vb != EMPTY && va == EMPTY) {
                                    if (emit(a, z, vb, R_LINK, -1, b)) return true;
                                } else if (va != vb) {
                                    conflict = true;
                                    return true;
                                }
                            }
                        }
                    }
        return false;
    }

    private boolean noCommon() {
        for (int c1 = 0; c1 < k; c1++)
            for (int i = 0; i < n; i++)
                for (int cy = 0; cy < k; cy++) {
                    if (cy == c1) continue;
                    for (int cz = 0; cz < k; cz++) {
                        if (cz == c1 || cz == cy) continue;
                        for (int z = 0; z < n; z++) {
                            if (g.get(c1, i, cz, z) != EMPTY) continue;
                            boolean any = false;
                            for (int y = 0; y < n && !any; y++)
                                any = g.get(c1, i, cy, y) != NO && g.get(cy, y, cz, z) != NO;
                            if (!any && emit(ClueModel.ent(c1, i), ClueModel.ent(cz, z), NO,
                                    R_NOCOMMON, -1, cy)) return true;
                        }
                    }
                }
        return false;
    }

    private boolean complex() {
        for (int ci = 0; ci < m.clues.size(); ci++) {
            ClueModel c = m.clues.get(ci);
            if (c.locked && !useLocked) continue;
            for (Constraint p : c.parts) {
                boolean stop;
                switch (p.type) {
                    case ClueModel.EITHER:
                        stop = either(p.a, ci);
                        break;
                    case ClueModel.PAIR:
                        stop = pair(p.a, ci);
                        break;
                    case ClueModel.LT:
                        stop = less(p.a, ci);
                        break;
                    case ClueModel.NEXT:
                        stop = nextTo(p.a, ci);
                        break;
                    default:
                        stop = false;
                }
                if (stop) return true;
            }
        }
        return false;
    }

    private boolean lg(int a, int b, byte v, int ci) {
        return emit(a, b, v, R_LOGIC, ci, -1);
    }

    private boolean either(int[] e, int ci) {
        int a = e[0], b = e[1], c = e[2];
        boolean same = ClueModel.cat(b) == ClueModel.cat(c);
        if (same) {
            int cat = ClueModel.cat(b);
            for (int x = 0; x < n; x++) {
                int d = ClueModel.ent(cat, x);
                if (d != b && d != c && lg(a, d, NO, ci)) return true;
            }
        } else if (lg(b, c, NO, ci)) return true;
        byte vb = g.get(a, b), vc = g.get(a, c);
        if (vb == NO && vc == NO) {
            conflict = true;
            return true;
        }
        if (vb == NO && lg(a, c, YES, ci)) return true;
        if (vc == NO && lg(a, b, YES, ci)) return true;
        if (vb == YES && lg(a, c, NO, ci)) return true;
        return vc == YES && lg(a, b, NO, ci);
    }

    private boolean pair(int[] e, int ci) {
        int a = e[0], b = e[1], c = e[2], d = e[3];
        int ca = ClueModel.cat(a), cc = ClueModel.cat(c), cd = ClueModel.cat(d);
        if (cc != cd && lg(c, d, NO, ci)) return true;
        for (int x = 0; x < n; x++) {
            int o = ClueModel.ent(ca, x);
            if (o != a && o != b && (lg(o, c, NO, ci) || lg(o, d, NO, ci))) return true;
            if (cc == cd) {
                int f = ClueModel.ent(cc, x);
                if (f != c && f != d && (lg(a, f, NO, ci) || lg(b, f, NO, ci))) return true;
            }
        }
        boolean first = g.get(a, c) != NO && g.get(b, d) != NO
                && g.get(a, d) != YES && g.get(b, c) != YES;
        boolean second = g.get(a, d) != NO && g.get(b, c) != NO
                && g.get(a, c) != YES && g.get(b, d) != YES;
        if (!first && !second) {
            conflict = true;
            return true;
        }
        if (first && !second) return lg(a, c, YES, ci) || lg(b, d, YES, ci);
        if (second && !first) return lg(a, d, YES, ci) || lg(b, c, YES, ci);
        return false;
    }

    private boolean less(int[] e, int ci) {
        int a = e[0], b = e[1], t = e[2];
        if (ClueModel.cat(a) != ClueModel.cat(b) && lg(a, b, NO, ci)) return true;
        int minA = -1, maxB = -1;
        for (int x = 0; x < n; x++) {
            if (minA < 0 && g.get(a, ClueModel.ent(t, x)) != NO) minA = x;
            if (g.get(b, ClueModel.ent(t, x)) != NO) maxB = x;
        }
        if (minA < 0 || maxB < 0) {
            conflict = true;
            return true;
        }
        for (int x = 0; x < n; x++) {
            if (x >= maxB && lg(a, ClueModel.ent(t, x), NO, ci)) return true;
            if (x <= minA && lg(b, ClueModel.ent(t, x), NO, ci)) return true;
        }
        return false;
    }

    private boolean nextTo(int[] e, int ci) {
        int a = e[0], b = e[1], t = e[2];
        if (ClueModel.cat(a) != ClueModel.cat(b) && lg(a, b, NO, ci)) return true;
        for (int x = 0; x < n; x++) {
            if ((x == n - 1 || g.get(b, ClueModel.ent(t, x + 1)) == NO)
                    && lg(a, ClueModel.ent(t, x), NO, ci)) return true;
            if ((x == 0 || g.get(a, ClueModel.ent(t, x - 1)) == NO)
                    && lg(b, ClueModel.ent(t, x), NO, ci)) return true;
        }
        return false;
    }
}
