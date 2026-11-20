package com.ccs.deductive_case_files.screens;

import com.ccs.deductive_case_files.R;
import com.ccs.deductive_case_files.game.CaseModel;
import com.ccs.deductive_case_files.game.ClueModel;
import com.ccs.deductive_case_files.game.DeductionGrid;
import com.ccs.deductive_case_files.game.HintSystem;
import com.ccs.deductive_case_files.localization.LocaleManager;

/** Turns a deduction step into a localized sentence for hints and explanations. */
final class StepText {
    private StepText() {
    }

    static String pair(CaseModel m, int a, int b, byte v) {
        return m.itemName(a) + " → " + m.itemName(b) + (v == DeductionGrid.YES ? " ✓" : " ✕");
    }

    static String describe(CaseModel m, HintSystem.Step s) {
        String res = pair(m, s.a, s.b, s.v);
        switch (s.rule) {
            case HintSystem.R_CLUE:
                return LocaleManager.s(R.string.rule_clue, s.clue + 1, res);
            case HintSystem.R_LOGIC:
                return LocaleManager.s(R.string.rule_logic, s.clue + 1, res);
            case HintSystem.R_TAKEN: {
                int partner = ClueModel.cat(s.a) != ClueModel.cat(s.via) ? s.a : s.b;
                return LocaleManager.s(R.string.rule_taken, m.itemName(partner), m.itemName(s.via), res);
            }
            case HintSystem.R_ONLY:
                return LocaleManager.s(R.string.rule_only, m.itemName(s.a), m.catNames[s.via], res);
            case HintSystem.R_LINK:
                return LocaleManager.s(R.string.rule_link, m.itemName(s.a), m.itemName(s.via), res);
            case HintSystem.R_NOCOMMON:
                return LocaleManager.s(R.string.rule_nocommon, m.itemName(s.a), m.catNames[s.via], m.itemName(s.b), res);
            default:
                return LocaleManager.s(R.string.rule_contra, res);
        }
    }
}
