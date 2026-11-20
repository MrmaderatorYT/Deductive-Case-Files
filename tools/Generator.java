import com.ccs.deductive_case_files.game.CaseModel;
import com.ccs.deductive_case_files.game.ClueModel;
import com.ccs.deductive_case_files.game.HintSystem;
import com.ccs.deductive_case_files.game.PuzzleSolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Development-time tool (not shipped in the APK). Produces constraint sets that have exactly one
 * solution and are solvable by the in-game deduction engine at the requested level.
 * Usage: java Generator > skeleton.txt   (the 30 hand-written cases were built from this output;
 * ContentGen reuses attempt() for the generated cases)
 */
public final class Generator {
    // id, difficulty, n, cats, ordered, kind, scene, minClues, maxClues, maxLevel
    static final Object[][] SPEC = {
            {1, 0, 3, 3, -1, "MATRIX", "MANOR", 3, 4, 1},
            {2, 0, 3, 3, -1, "MATRIX", "CAFE", 3, 5, 1},
            {3, 0, 3, 3, -1, "ALIBI", "TRAIN", 4, 5, 1},
            {4, 0, 3, 3, -1, "CIPHER", "LIBRARY", 4, 5, 1},
            {5, 0, 3, 3, -1, "VISUAL", "MUSEUM", 4, 5, 1},
            {6, 0, 3, 3, -1, "CONTRADICTION", "THEATER", 4, 5, 1},
            {7, 0, 4, 3, -1, "MATRIX", "HOTEL", 5, 7, 1},
            {8, 0, 4, 3, -1, "DOCUMENT", "OFFICE", 5, 7, 1},
            {9, 0, 4, 3, -1, "SEQUENCE", "GARDEN", 5, 7, 1},
            {10, 0, 4, 3, -1, "ALIBI", "HARBOR", 6, 8, 1},
            {11, 1, 4, 3, -1, "MATRIX", "LAB", 5, 7, 1},
            {12, 1, 4, 3, -1, "CIPHER", "OBSERVATORY", 5, 7, 1},
            {13, 1, 4, 3, -1, "CONTRADICTION", "MANOR", 5, 7, 1},
            {14, 1, 4, 3, -1, "VISUAL", "HOTEL", 5, 7, 1},
            {15, 1, 4, 3, -1, "DOCUMENT", "MUSEUM", 6, 8, 1},
            {16, 1, 4, 4, 3, "SEQUENCE", "TRAIN", 6, 9, 1},
            {17, 1, 4, 4, 3, "ALIBI", "THEATER", 6, 9, 1},
            {18, 1, 4, 4, 3, "MATRIX", "CAFE", 6, 9, 1},
            {19, 1, 4, 4, 3, "CIPHER", "HARBOR", 7, 10, 1},
            {20, 1, 4, 4, 3, "VISUAL", "LIBRARY", 7, 10, 1},
            {21, 2, 5, 3, -1, "MATRIX", "GARDEN", 8, 11, 1},
            {22, 2, 5, 3, -1, "CONTRADICTION", "OFFICE", 8, 11, 1},
            {23, 2, 5, 3, -1, "DOCUMENT", "LAB", 8, 11, 2},
            {24, 2, 5, 3, -1, "SEQUENCE", "OBSERVATORY", 8, 11, 2},
            {25, 2, 5, 3, -1, "ALIBI", "HOTEL", 8, 11, 2},
            {26, 2, 5, 3, -1, "CIPHER", "MANOR", 8, 11, 2},
            {27, 2, 5, 3, -1, "VISUAL", "CAFE", 8, 11, 2},
            {28, 2, 4, 4, 3, "CONTRADICTION", "MUSEUM", 6, 8, 2},
            {29, 2, 5, 4, 3, "DOCUMENT", "HARBOR", 9, 13, 2},
            {30, 2, 5, 4, 3, "MATRIX", "THEATER", 9, 13, 2},
    };

    public static void main(String[] args) {
        StringBuilder notes = new StringBuilder();
        for (Object[] s : SPEC) {
            CaseModel m = null;
            for (long seed = (int) s[0] * 1000L; m == null; seed++) m = attempt(s, new Random(seed));
            int[][] sol = PuzzleSolver.solve(m);
            int[] la = new int[1];
            int level = new HintSystem(m).grade(la);
            System.out.println("[" + m.id + "]");
            System.out.println("d=" + m.difficulty);
            System.out.println("k=" + s[5]);
            System.out.println("s=" + s[6]);
            System.out.println("n=" + m.n);
            System.out.println("c=" + m.cats);
            System.out.println("o=" + m.ordered);
            System.out.println("key=" + ClueModel.name(m.key));
            int lock = -1;
            for (int i = 0; i < m.clues.size(); i++) {
                ClueModel c = m.clues.get(i);
                System.out.println("q=" + c.spec());
                if (lock < 0 && c.parts[0].type == ClueModel.NOT) lock = i;
            }
            if (!"MATRIX".equals(s[5])) {
                System.out.println("lock=" + Math.max(lock, 0));
                System.out.println("t=" + ("SEQUENCE".equals(s[5]) ? "ORDER" : "CHOICE 0"));
                System.out.println("v=");
            }
            System.out.println();
            notes.append("# ").append(m.id).append(" level=").append(level).append(" la=").append(la[0])
                    .append(" culprit=A").append(m.culprit(sol)).append(" sol:");
            for (int g = 0; g < m.n; g++) {
                notes.append(" A").append(g);
                for (int c = 1; c < m.cats; c++) notes.append('-').append((char) ('A' + c)).append(sol[c][g]);
            }
            notes.append('\n');
        }
        System.err.print(notes);
    }

    static CaseModel attempt(Object[] s, Random rnd) {
        int n = (int) s[2], cats = (int) s[3], ordered = (int) s[4], diff = (int) s[1];
        int minClues = (int) s[7], maxClues = (int) s[8], maxLevel = (int) s[9];
        int[][] sol = new int[cats][n];
        for (int c = 0; c < cats; c++) {
            List<Integer> p = new ArrayList<>();
            for (int i = 0; i < n; i++) p.add(i);
            if (c > 0) Collections.shuffle(p, rnd);
            for (int i = 0; i < n; i++) sol[c][i] = p.get(i);
        }
        CaseModel m = new CaseModel();
        m.id = (int) s[0];
        m.difficulty = diff;
        m.n = n;
        m.cats = cats;
        m.ordered = ordered;
        m.key = ClueModel.ent(1, rnd.nextInt(n));

        // weights per type: IS, NOT, EITHER, PAIR, LT, NEXT, DIFF
        int[] w;
        if (diff == 0) w = n == 3 ? new int[]{2, 7, 1, 0, 0, 0, 0} : new int[]{2, 6, 2, 0, 0, 0, 0};
        else if (diff == 1) w = new int[]{1, 4, 3, 2, 4, 3, 1};
        else w = new int[]{1, 3, 3, 3, 4, 3, 2};
        int maxIs = n == 3 ? 1 : (diff == 2 ? 1 : 2);

        List<List<ClueModel>> pool = pool(m, sol, rnd);
        int isCount = 0;
        for (int guard = 0; guard < 200; guard++) {
            if (PuzzleSolver.solve(m) != null && ok(m, maxLevel)) break;
            int total = 0;
            for (int t = 0; t < w.length; t++) if (!pool.get(t).isEmpty()) total += w[t];
            if (total == 0) return null;
            int r = rnd.nextInt(total), type = 0;
            for (int t = 0; t < w.length; t++) {
                if (pool.get(t).isEmpty()) continue;
                if (r < w[t]) {
                    type = t;
                    break;
                }
                r -= w[t];
            }
            ClueModel c = pool.get(type).remove(pool.get(type).size() - 1);
            if (type == ClueModel.IS && ++isCount > maxIs) continue;
            m.clues.add(c);
        }
        if (PuzzleSolver.solve(m) == null || !ok(m, maxLevel)) return null;
        // drop redundant clues
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < m.clues.size(); i++) order.add(i);
        Collections.shuffle(order, rnd);
        List<ClueModel> keep = new ArrayList<>(m.clues);
        for (int idx : order) {
            ClueModel c = m.clues.get(idx);
            keep.remove(c);
            CaseModel t = shallow(m, keep);
            if (PuzzleSolver.solve(t) == null || !ok(t, maxLevel)) keep.add(c);
        }
        m.clues.clear();
        m.clues.addAll(keep);
        Collections.shuffle(m.clues, rnd);
        if (m.clues.size() < minClues || m.clues.size() > maxClues) return null;
        int types = 0, seen = 0;
        for (ClueModel c : m.clues) if ((seen & (1 << c.parts[0].type)) == 0) {
            seen |= 1 << c.parts[0].type;
            types++;
        }
        if (diff > 0 && types < 3) return null;
        if (diff == 2 && maxLevel == 2 && new HintSystem(m).grade(null) != 2) return null;
        boolean hasNot = (seen & (1 << ClueModel.NOT)) != 0;
        return hasNot ? m : null;
    }

    static boolean ok(CaseModel m, int maxLevel) {
        int[] la = new int[1];
        int level = new HintSystem(m).grade(la);
        return level > 0 && level <= maxLevel && la[0] <= 3;
    }

    static CaseModel shallow(CaseModel m, List<ClueModel> clues) {
        CaseModel t = new CaseModel();
        t.n = m.n;
        t.cats = m.cats;
        t.ordered = m.ordered;
        t.key = m.key;
        t.clues.addAll(clues);
        return t;
    }

    static List<List<ClueModel>> pool(CaseModel m, int[][] sol, Random rnd) {
        int n = m.n, k = m.cats;
        List<List<ClueModel>> pool = new ArrayList<>();
        for (int t = 0; t < 7; t++) pool.add(new ArrayList<>());
        int[] ents = new int[n * k], grp = new int[n * k];
        int cnt = 0;
        for (int c = 0; c < k; c++)
            for (int g = 0; g < n; g++) {
                ents[cnt] = ClueModel.ent(c, sol[c][g]);
                grp[cnt++] = g;
            }
        for (int x = 0; x < cnt; x++)
            for (int y = 0; y < cnt; y++) {
                int a = ents[x], b = ents[y], ca = ClueModel.cat(a), cb = ClueModel.cat(b);
                if (ca < cb) {
                    boolean same = grp[x] == grp[y];
                    if (same && a != m.key && b != m.key) add(pool, "IS", a, b);
                    if (!same) add(pool, "NOT", a, b);
                }
                if (m.ordered >= 0 && a != b && ca != m.ordered && cb != m.ordered && grp[x] != grp[y]) {
                    int ta = sol[m.ordered][grp[x]], tb = sol[m.ordered][grp[y]];
                    String T = "" + (char) ('A' + m.ordered);
                    if (ta < tb && tb - ta > 1 && rnd.nextInt(2) == 0)
                        pool.get(ClueModel.LT).add(ClueModel.parse("LT " + ClueModel.name(a) + " " + ClueModel.name(b) + " " + T));
                    if (tb == ta + 1)
                        pool.get(ClueModel.NEXT).add(ClueModel.parse("NEXT " + ClueModel.name(a) + " " + ClueModel.name(b) + " " + T));
                }
                for (int z = 0; z < cnt; z++) {
                    int c = ents[z], cc = ClueModel.cat(c);
                    // EITHER a b c: b truly with a, c not
                    if (ca != cb && ca != cc && cb != 0 && cc != 0 && b != c && grp[x] == grp[y] && grp[x] != grp[z]
                            && rnd.nextInt(3) == 0) {
                        if (rnd.nextBoolean()) add(pool, "EITHER", a, b, c);
                        else add(pool, "EITHER", a, c, b);
                    }
                    // DIFF: three categories, three groups
                    if (ca < cb && cb < cc && grp[x] != grp[y] && grp[y] != grp[z] && grp[x] != grp[z]
                            && rnd.nextInt(4) == 0) add(pool, "DIFF", a, b, c);
                    // PAIR a b | c d : a,b same category, c with a, d with b
                    if (ca == cb && x < y && cc != ca && cc != 0 && grp[z] == grp[x]) {
                        for (int u = 0; u < cnt; u++) {
                            int d = ents[u];
                            if (ClueModel.cat(d) != ca && ClueModel.cat(d) != 0 && grp[u] == grp[y] && rnd.nextInt(3) == 0) {
                                if (rnd.nextBoolean()) add(pool, "PAIR", a, b, c, d);
                                else add(pool, "PAIR", a, b, d, c);
                            }
                        }
                    }
                }
            }
        for (List<ClueModel> l : pool) Collections.shuffle(l, rnd);
        return pool;
    }

    static void add(List<List<ClueModel>> pool, String type, int... e) {
        StringBuilder sb = new StringBuilder(type);
        for (int x : e) sb.append(' ').append(ClueModel.name(x));
        ClueModel c = ClueModel.parse(sb.toString());
        pool.get(c.parts[0].type).add(c);
    }
}
