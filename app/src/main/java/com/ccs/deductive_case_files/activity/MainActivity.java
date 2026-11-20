package com.ccs.deductive_case_files.activity;

import android.content.Intent;
import android.os.Bundle;

import com.ccs.deductive_case_files.data.CaseRepository;
import com.ccs.deductive_case_files.data.ProgressManager;
import com.ccs.deductive_case_files.game.CaseModel;
import com.ccs.deductive_case_files.screens.CaseScreen;
import com.ccs.deductive_case_files.screens.CasesScreen;
import com.ccs.deductive_case_files.screens.HomeScreen;
import com.ccs.deductive_case_files.screens.ResultScreen;
import com.ccs.deductive_case_files.screens.TaskScreen;
import com.ccs.deductive_case_files.screens.TutorialScreen;
import com.ccs.deductive_case_files.R;
import com.ccs.deductive_case_files.graphics.IconRenderer;
import com.ccs.deductive_case_files.localization.LocaleManager;
import com.ccs.deductive_case_files.ui.CanvasDialog;

/**
 * The game itself. Home, case selection, the case, its evidence task and the result are states
 * of one Canvas view rather than separate activities.
 */
public final class MainActivity extends BaseActivity {
    public static final int HOME = 0, CASES = 1, CASE = 2, TASK = 3, RESULT = 4, TUTORIAL = 5;
    private int state = HOME, caseId, origin = HOME;
    /** UI state kept across screen rebuilds and process death. */
    public int caseTab, filter = -1;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        cases();
        if (b != null) {
            state = b.getInt("state", HOME);
            caseId = b.getInt("case", 0);
            origin = b.getInt("origin", HOME);
            caseTab = b.getInt("tab", 0);
            filter = b.getInt("filter", -1);
        }
        if (state >= CASE && state != TUTORIAL && CaseRepository.byId(caseId) == null) state = HOME;
        show(state, 0);
        if (state == HOME && !settings.tourOffered()) offerTour();
    }

    @Override
    protected void onSaveInstanceState(Bundle b) {
        super.onSaveInstanceState(b);
        b.putInt("state", state);
        b.putInt("case", caseId);
        b.putInt("origin", origin);
        b.putInt("tab", caseTab);
        b.putInt("filter", filter);
    }

    private void show(int newState, int direction) {
        state = newState;
        CaseModel m = CaseRepository.byId(caseId);
        switch (newState) {
            case CASES:
                canvas.setScreen(new CasesScreen(this), direction);
                break;
            case CASE:
                canvas.setScreen(new CaseScreen(this, m), direction);
                break;
            case TASK:
                canvas.setScreen(new TaskScreen(this, m), direction);
                break;
            case RESULT:
                canvas.setScreen(new ResultScreen(this, m), direction);
                break;
            case TUTORIAL:
                canvas.setScreen(new TutorialScreen(this, CaseRepository.tutorial()), direction);
                break;
            default:
                canvas.setScreen(new HomeScreen(this), direction);
        }
    }

    public void goHome() {
        show(HOME, state == HOME ? 0 : -1);
    }

    public void goCases(int direction) {
        show(CASES, direction);
    }

    /** Opens a case: solved cases go straight to their result. */
    public void openCase(int id) {
        if (state == HOME || state == CASES) origin = state;
        caseId = id;
        caseTab = 0;
        progress.setLastCase(id);
        show(progress.status(id) == ProgressManager.SOLVED ? RESULT : CASE, 1);
    }

    public void openTask() {
        show(TASK, 1);
    }

    public void backToCase() {
        show(CASE, -1);
    }

    public void showResult() {
        show(RESULT, 1);
    }

    /** Clears the progress of the current case and starts it again. */
    public void replay() {
        progress.resetCase(caseId);
        caseTab = 0;
        show(CASE, 0);
    }

    public void leaveCase() {
        show(origin == CASES ? CASES : HOME, -1);
    }

    /** First case that is not solved yet, preferring untouched ones after {@code afterId}. */
    public int nextCase(int afterId) {
        int fallback = 0;
        boolean passed = afterId == 0;
        for (CaseModel m : cases()) {
            if (m.id == afterId) {
                passed = true;
                continue;
            }
            if (progress.status(m.id) == ProgressManager.SOLVED) continue;
            if (passed) return m.id;
            if (fallback == 0) fallback = m.id;
        }
        return fallback;
    }

    /** Starts the guided tour on the training case. */
    public void openTutorial() {
        settings.setTourOffered();
        show(TUTORIAL, 1);
    }

    /** First-launch invitation to take the tour. */
    private void offerTour() {
        CanvasDialog d = new CanvasDialog(canvas);
        d.icon = IconRenderer.BULB;
        d.cancelable = false;
        d.title = LocaleManager.s(R.string.tour_offer_title);
        d.message = LocaleManager.s(R.string.tour_offer_body);
        d.positive = LocaleManager.s(R.string.tour_start);
        d.negative = LocaleManager.s(R.string.tour_later);
        d.onPositive = this::openTutorial;
        d.onNegative = settings::setTourOffered;
        canvas.showDialog(d);
    }

    public void openSettings() {
        startActivity(new Intent(this, SettingsActivity.class));
        overridePendingTransition(0, 0);
    }
}
