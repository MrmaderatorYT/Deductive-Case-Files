package com.ccs.deductive_case_files.game;

import java.util.ArrayList;
import java.util.List;

/** A detective case: language-neutral structure plus the texts of the active language. */
public final class CaseModel {
    public static final int EASY = 0, MEDIUM = 1, HARD = 2;
    /** Puzzle kinds. Every case has a matrix; non-MATRIX kinds add a special evidence task. */
    public static final int MATRIX = 0, ALIBI = 1, CONTRADICTION = 2, CIPHER = 3, DOCUMENT = 4,
            VISUAL = 5, SEQUENCE = 6;
    public static final String[] KINDS = {"MATRIX", "ALIBI", "CONTRADICTION", "CIPHER", "DOCUMENT",
            "VISUAL", "SEQUENCE"};
    public static final int TASK_NONE = 0, TASK_CHOICE = 1, TASK_ORDER = 2;

    // ---- structure ----
    public int id;
    /** Position in the collection (1-based); ids stay stable so saved progress survives reordering. */
    public int number;
    public int difficulty;
    public int kind;
    public String scene = "MANOR";
    public int n = 3, cats = 3;
    /** Index of the ordered category (times) or -1. */
    public int ordered = -1;
    /** Entity the crime is tied to; the culprit is the suspect linked with it. */
    public int key;
    public final List<ClueModel> clues = new ArrayList<>();
    public int taskMode = TASK_NONE;
    public int taskAnswer;
    /** Visual or cipher specification, e.g. "CLOCK:10:25" or "CAESAR:3". */
    public String taskSpec = "";

    // ---- localized texts ----
    public String title = "", intro = "", fact = "", outro = "";
    public String[] catNames = new String[0];
    public String[][] items = new String[0][];
    public SuspectModel[] suspects = new SuspectModel[0];
    public String taskTitle = "", taskBody = "", taskQuestion = "", taskSolved = "", taskPlain = "";
    public String[] taskOptions = new String[0];

    /** Unique solution, solution[cat][group] = item index; filled in by the solver. */
    public volatile int[][] solution;

    public boolean hasTask() {
        return taskMode != TASK_NONE;
    }

    public String itemName(int entity) {
        return items[ClueModel.cat(entity)][ClueModel.idx(entity)];
    }

    /** Suspect index linked with the key entity in the given solution. */
    public int culprit(int[][] sol) {
        int c = ClueModel.cat(key), i = ClueModel.idx(key);
        for (int g = 0; g < n; g++) if (sol[c][g] == i) return g;
        return -1;
    }
}
