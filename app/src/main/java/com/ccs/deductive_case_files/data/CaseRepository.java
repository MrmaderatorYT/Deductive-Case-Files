package com.ccs.deductive_case_files.data;

import android.content.Context;

import com.ccs.deductive_case_files.game.CaseModel;
import com.ccs.deductive_case_files.game.CaseParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Loads the offline case files from assets and swaps their texts when the language changes. */
public final class CaseRepository {
    private static List<CaseModel> cases;
    private static CaseModel tutorial;
    private static String loadedLang;
    private static List<CaseModel> all;

    private CaseRepository() {
    }

    public static synchronized List<CaseModel> cases(Context c, String lang) {
        try {
            if (cases == null) all = null;
            if (cases == null) {
                try (BufferedReader r = open(c, "cases.dat")) {
                    all = CaseParser.parseStructure(r);
                }
                List<CaseModel> list = new ArrayList<>();
                for (CaseModel m : all) {
                    if (m.id == 0) tutorial = m;
                    else list.add(m);
                }
                // easy cases first, then medium, then hard; ids keep the order inside a level
                Collections.sort(list, (a, b) -> a.difficulty != b.difficulty ? a.difficulty - b.difficulty : a.id - b.id);
                for (int i = 0; i < list.size(); i++) list.get(i).number = i + 1;
                cases = list;
                loadedLang = null;
            }
            if (!lang.equals(loadedLang)) {
                try (BufferedReader r = open(c, "cases_" + lang + ".txt")) {
                    CaseParser.applyTexts(all, r);
                }
                loadedLang = lang;
            }
        } catch (IOException e) {
            if (cases == null) cases = new ArrayList<>();
        }
        return cases;
    }

    /** The small guided case of the tour; not part of the collection. */
    public static CaseModel tutorial() {
        return tutorial;
    }

    public static CaseModel byId(int id) {
        if (cases != null) for (CaseModel m : cases) if (m.id == id) return m;
        return null;
    }

    private static BufferedReader open(Context c, String name) throws IOException {
        return new BufferedReader(new InputStreamReader(c.getAssets().open(name), StandardCharsets.UTF_8));
    }
}
