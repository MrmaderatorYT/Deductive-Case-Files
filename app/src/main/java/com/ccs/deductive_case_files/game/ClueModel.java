package com.ccs.deductive_case_files.game;

/**
 * One clue of a case: a localized sentence plus the machine-readable constraints it states.
 * Entities are encoded as (category << 4) | index.
 */
public final class ClueModel {
    /** a and b belong together. */
    public static final int IS = 0;
    /** a and b do not belong together. */
    public static final int NOT = 1;
    /** a goes with exactly one of b, c. */
    public static final int EITHER = 2;
    /** of a and b, one goes with c and the other with d. */
    public static final int PAIR = 3;
    /** in ordered category T, a comes earlier than b. */
    public static final int LT = 4;
    /** in ordered category T, b comes immediately after a. */
    public static final int NEXT = 5;
    /** all listed entities belong to different groups. */
    public static final int DIFF = 6;

    private static final String[] NAMES = {"IS", "NOT", "EITHER", "PAIR", "LT", "NEXT", "DIFF"};

    public static final class Constraint {
        public final int type;
        /** Entity codes; for LT/NEXT the last element is the ordered category index. */
        public final int[] a;

        public Constraint(int type, int[] a) {
            this.type = type;
            this.a = a;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(NAMES[type]);
            for (int i = 0; i < a.length; i++) {
                sb.append(' ');
                if ((type == LT || type == NEXT) && i == a.length - 1) sb.append((char) ('A' + a[i]));
                else sb.append(name(a[i]));
            }
            return sb.toString();
        }
    }

    public final Constraint[] parts;
    public String text = "";
    /** True when the clue is revealed only after the special evidence task is solved. */
    public boolean locked;

    public ClueModel(Constraint[] parts) {
        this.parts = parts;
    }

    public static int ent(int cat, int idx) {
        return (cat << 4) | idx;
    }

    public static int cat(int e) {
        return e >> 4;
    }

    public static int idx(int e) {
        return e & 15;
    }

    public static String name(int e) {
        return "" + (char) ('A' + cat(e)) + idx(e);
    }

    public static int parseEntity(String s) {
        return ent(s.charAt(0) - 'A', s.charAt(1) - '0');
    }

    /** Parses "NOT A0 B1;NOT A0 B2". */
    public static ClueModel parse(String spec) {
        String[] ps = spec.split(";");
        Constraint[] out = new Constraint[ps.length];
        for (int p = 0; p < ps.length; p++) {
            String[] t = ps[p].trim().split(" +");
            int type = -1;
            for (int i = 0; i < NAMES.length; i++) if (NAMES[i].equals(t[0])) type = i;
            if (type < 0) throw new IllegalArgumentException("Bad clue: " + spec);
            int[] a = new int[t.length - 1];
            for (int i = 1; i < t.length; i++) {
                a[i - 1] = t[i].length() == 1 ? t[i].charAt(0) - 'A' : parseEntity(t[i]);
            }
            out[p] = new Constraint(type, a);
        }
        return new ClueModel(out);
    }

    public String spec() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append(';');
            sb.append(parts[i]);
        }
        return sb.toString();
    }
}
