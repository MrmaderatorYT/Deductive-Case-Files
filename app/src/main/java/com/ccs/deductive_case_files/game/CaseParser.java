package com.ccs.deductive_case_files.game;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads the case data files. The format is a list of "[id]" sections with key=value lines;
 * it has no Android dependencies so the same code verifies the content on a desktop JVM.
 */
public final class CaseParser {
    private CaseParser() {
    }

    /** Parses the language-neutral structure file. */
    public static List<CaseModel> parseStructure(BufferedReader r) throws IOException {
        List<CaseModel> out = new ArrayList<>();
        CaseModel m = null;
        int lock = -1;
        String line;
        while ((line = r.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.charAt(0) == '#') continue;
            if (line.charAt(0) == '[') {
                if (m != null) finish(m, lock);
                m = new CaseModel();
                m.id = Integer.parseInt(line.substring(1, line.length() - 1));
                lock = -1;
                out.add(m);
                continue;
            }
            int eq = line.indexOf('=');
            if (eq < 0 || m == null) continue;
            String key = line.substring(0, eq), v = line.substring(eq + 1);
            switch (key) {
                case "d":
                    m.difficulty = Integer.parseInt(v);
                    break;
                case "k":
                    for (int i = 0; i < CaseModel.KINDS.length; i++)
                        if (CaseModel.KINDS[i].equals(v)) m.kind = i;
                    break;
                case "s":
                    m.scene = v;
                    break;
                case "n":
                    m.n = Integer.parseInt(v);
                    break;
                case "c":
                    m.cats = Integer.parseInt(v);
                    break;
                case "o":
                    m.ordered = Integer.parseInt(v);
                    break;
                case "key":
                    m.key = ClueModel.parseEntity(v);
                    break;
                case "q":
                    m.clues.add(ClueModel.parse(v));
                    break;
                case "lock":
                    lock = Integer.parseInt(v);
                    break;
                case "t": {
                    String[] t = v.split(" ");
                    m.taskMode = "ORDER".equals(t[0]) ? CaseModel.TASK_ORDER : CaseModel.TASK_CHOICE;
                    if (t.length > 1) m.taskAnswer = Integer.parseInt(t[1]);
                    break;
                }
                case "v":
                    m.taskSpec = v;
                    break;
                default:
                    break;
            }
        }
        if (m != null) finish(m, lock);
        return out;
    }

    private static void finish(CaseModel m, int lock) {
        if (lock >= 0 && lock < m.clues.size() && m.hasTask()) m.clues.get(lock).locked = true;
    }

    /** Applies one language's texts to the already parsed cases. */
    public static void applyTexts(List<CaseModel> cases, BufferedReader r) throws IOException {
        Map<Integer, Map<String, String>> all = new HashMap<>();
        Map<String, String> cur = null;
        String line;
        while ((line = r.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.charAt(0) == '#') continue;
            if (line.charAt(0) == '[') {
                cur = new HashMap<>();
                all.put(Integer.parseInt(line.substring(1, line.length() - 1)), cur);
                continue;
            }
            int eq = line.indexOf('=');
            if (eq < 0 || cur == null) continue;
            cur.put(line.substring(0, eq), line.substring(eq + 1).replace("\\n", "\n"));
        }
        for (CaseModel m : cases) {
            Map<String, String> t = all.get(m.id);
            if (t == null) continue;
            m.title = get(t, "title");
            m.intro = get(t, "intro");
            m.fact = get(t, "fact");
            m.outro = get(t, "outro");
            m.catNames = new String[m.cats];
            m.items = new String[m.cats][m.n];
            m.suspects = new SuspectModel[m.n];
            for (int c = 0; c < m.cats; c++) {
                String[] p = get(t, String.valueOf((char) ('A' + c))).split("\\|");
                m.catNames[c] = p[0];
                for (int i = 0; i < m.n; i++) {
                    String item = i + 1 < p.length ? p[i + 1] : "?";
                    String role = "";
                    int colon = item.indexOf(':');
                    if (c == 0 && colon > 0) {
                        role = item.substring(colon + 1);
                        item = item.substring(0, colon);
                    }
                    m.items[c][i] = item;
                    if (c == 0) m.suspects[i] = new SuspectModel(i, item, role);
                }
            }
            for (int i = 0; i < m.clues.size(); i++) m.clues.get(i).text = get(t, "q" + i);
            m.taskTitle = get(t, "tt");
            m.taskBody = get(t, "tb");
            m.taskQuestion = get(t, "tq");
            m.taskSolved = get(t, "tk");
            m.taskPlain = get(t, "tp");
            String o = get(t, "to");
            m.taskOptions = o.isEmpty() ? new String[0] : o.split("\\|");
        }
    }

    private static String get(Map<String, String> t, String k) {
        String v = t.get(k);
        return v == null ? "" : v;
    }
}
