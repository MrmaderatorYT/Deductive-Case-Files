import com.ccs.deductive_case_files.game.CaseModel;
import com.ccs.deductive_case_files.game.CaseParser;
import com.ccs.deductive_case_files.game.Cipher;
import com.ccs.deductive_case_files.game.ClueModel;
import com.ccs.deductive_case_files.game.HintSystem;
import com.ccs.deductive_case_files.game.PuzzleSolver;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Development-time check of the shipped content (not part of the APK).
 * Proves every case has exactly one solution, is solvable by the hint engine, and that the
 * texts of both languages are complete and consistent with the structure.
 * Usage: java Verifier app/src/main/assets
 */
public final class Verifier {
    static int errors;

    public static void main(String[] args) throws Exception {
        String dir = args[0];
        for (String lang : new String[]{"uk", "en"}) {
            List<CaseModel> cases;
            try (BufferedReader r = Files.newBufferedReader(Paths.get(dir, "cases.dat"), StandardCharsets.UTF_8)) {
                cases = CaseParser.parseStructure(r);
            }
            try (BufferedReader r = Files.newBufferedReader(Paths.get(dir, "cases_" + lang + ".txt"), StandardCharsets.UTF_8)) {
                CaseParser.applyTexts(cases, r);
            }
            if (cases.size() != 121) fail(0, "expected 120 cases + tutorial, got " + cases.size());
            java.util.Set<String> titles = new java.util.HashSet<>();
            int[] perDiff = new int[3];
            for (CaseModel m : cases) {
                if (m.id > 0) perDiff[m.difficulty]++;
                if (!titles.add(m.title)) fail(m.id, "duplicate title " + m.title);
                int n = PuzzleSolver.count(m, 3);
                if (n != 1) fail(m.id, "solutions: " + n);
                int[][] sol = PuzzleSolver.solve(m);
                int[] la = new int[1];
                int level = new HintSystem(m).grade(la);
                if (level == 0) fail(m.id, "hint engine cannot finish");
                if (m.difficulty < 2 && level != 1) fail(m.id, "needs what-if reasoning but is not hard");
                if (m.title.isEmpty() || m.intro.isEmpty() || m.fact.isEmpty() || m.outro.isEmpty()) fail(m.id, "missing text");
                for (int c = 0; c < m.cats; c++) {
                    if (m.catNames[c].isEmpty()) fail(m.id, "category name " + c);
                    for (String it : m.items[c]) if (it.equals("?") || it.isEmpty()) fail(m.id, "item in category " + c);
                }
                for (int i = 0; i < m.n; i++) if (m.suspects[i].role.isEmpty()) fail(m.id, "suspect role " + i);
                int locked = 0;
                for (int i = 0; i < m.clues.size(); i++) {
                    if (m.clues.get(i).text.isEmpty()) fail(m.id, "clue text " + i);
                    if (m.clues.get(i).locked) locked++;
                }
                if (m.hasTask() != (m.kind != CaseModel.MATRIX)) fail(m.id, "task/kind mismatch");
                if (m.hasTask()) {
                    if (locked != 1) fail(m.id, "locked clues: " + locked);
                    if (m.taskTitle.isEmpty() || m.taskBody.isEmpty() || m.taskQuestion.isEmpty() || m.taskSolved.isEmpty())
                        fail(m.id, "task text");
                    if (m.taskOptions.length < 3) fail(m.id, "task options");
                    if (m.taskMode == CaseModel.TASK_CHOICE && m.taskAnswer >= m.taskOptions.length) fail(m.id, "answer index");
                    if ((m.taskMode == CaseModel.TASK_ORDER) != (m.kind == CaseModel.SEQUENCE)) fail(m.id, "order mode");
                    if (m.kind == CaseModel.CIPHER) {
                        if (!m.taskPlain.equals(m.taskOptions[m.taskAnswer])) fail(m.id, "cipher plain != answer option");
                        String enc = Cipher.encode(m.taskPlain, m.taskSpec, lang);
                        if (enc.equals(m.taskPlain)) fail(m.id, "cipher does not change the text");
                        int letters = 0;
                        for (char ch : m.taskPlain.toCharArray()) if (Cipher.alphabet(lang).indexOf(ch) >= 0) letters++;
                        if (letters < m.taskPlain.length() * 0.7) fail(m.id, "cipher text is mostly non-letters");
                    }
                    if (m.kind == CaseModel.VISUAL && m.taskSpec.isEmpty()) fail(m.id, "visual spec");
                }
                String who = sol == null ? "?" : m.suspects[m.culprit(sol)].name;
                if (args.length > 1) System.out.println(lang + " #" + m.id + " level=" + level + " what-if=" + la[0] + " clues=" + m.clues.size()
                        + " culprit=" + who + " | outro names culprit: " + m.outro.contains(who.substring(0, Math.min(4, who.length()))));
                if (sol != null && !m.outro.contains(who.substring(0, Math.min(4, who.length())))) fail(m.id, "outro does not name " + who);
            }
            if (perDiff[0] != 40 || perDiff[1] != 40 || perDiff[2] != 40) fail(0, "difficulty split");
        }
        System.out.println(errors == 0 ? "ALL CASES VERIFIED" : "ERRORS: " + errors);
        if (errors > 0) System.exit(1);
    }

    static void fail(int id, String msg) {
        errors++;
        System.out.println("!! case " + id + ": " + msg);
    }
}
