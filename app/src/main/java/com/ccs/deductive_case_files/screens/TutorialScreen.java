package com.ccs.deductive_case_files.screens;

import com.ccs.deductive_case_files.R;
import com.ccs.deductive_case_files.activity.MainActivity;
import com.ccs.deductive_case_files.game.CaseModel;
import com.ccs.deductive_case_files.game.DeductionGrid;
import com.ccs.deductive_case_files.game.HintSystem;
import com.ccs.deductive_case_files.game.PuzzleSolver;
import com.ccs.deductive_case_files.graphics.IconRenderer;
import com.ccs.deductive_case_files.ui.CanvasButton;
import com.ccs.deductive_case_files.ui.CanvasCard;
import com.ccs.deductive_case_files.ui.CanvasDialog;
import com.ccs.deductive_case_files.ui.CanvasGrid;
import com.ccs.deductive_case_files.ui.CanvasIcon;
import com.ccs.deductive_case_files.ui.CanvasProgressBar;
import com.ccs.deductive_case_files.ui.CanvasText;
import com.ccs.deductive_case_files.ui.Screen;
import com.ccs.deductive_case_files.ui.SoundManager;
import com.ccs.deductive_case_files.ui.ThemeManager;

import java.util.List;

/**
 * Guided tour: a tiny training case solved together with the player. Every deduction of the
 * hint engine becomes one step — the coach explains it and the player makes the mark.
 */
public final class TutorialScreen extends Screen {
    private static final int INTRO1 = 0, INTRO2 = 1, GUIDED = 2, AFTER = 3, ACCUSE = 4, DONE = 5;

    private final MainActivity a;
    private final CaseModel m;
    private final DeductionGrid grid;
    private final CanvasGrid gridView;
    private final List<HintSystem.Step> steps;
    private int phase = INTRO1, index = -1;

    public TutorialScreen(MainActivity a, CaseModel m) {
        super(a.canvas);
        this.a = a;
        this.m = m;
        // the training case is 3×3, so solving it right here costs well under a millisecond
        if (m.solution == null) m.solution = PuzzleSolver.solve(m);
        steps = new HintSystem(m).explain();
        grid = new DeductionGrid(m.n, m.cats);
        String[] states = {str(R.string.grid_empty), str(R.string.grid_no), str(R.string.grid_yes)};
        gridView = new CanvasGrid(view, m, grid, states, v -> onMarked());
        gridView.restrict(-1, -1);
    }

    @Override
    public boolean onBack() {
        a.goHome();
        return true;
    }

    @Override
    public float snackbarOffset() {
        return insetBottom + t.dp(88);
    }

    private HintSystem.Step current() {
        return index >= 0 && index < steps.size() ? steps.get(index) : null;
    }

    /** Moves to the next deduction that is not on the grid yet. */
    private void nextStep() {
        index++;
        while (index < steps.size() && grid.get(steps.get(index).a, steps.get(index).b) == steps.get(index).v) index++;
        HintSystem.Step s = current();
        if (s == null) {
            phase = AFTER;
            gridView.clearFlags();
            gridView.restrict(-1, -1);
        } else {
            gridView.flagHint(s.a, s.b);
            gridView.restrict(s.a, s.b);
        }
        rebuild();
    }

    private void onMarked() {
        HintSystem.Step s = current();
        SoundManager.get().play(SoundManager.MARK);
        if (s != null && grid.get(s.a, s.b) == s.v) {
            nextStep();
        } else if (s != null) {
            gridView.flagHint(s.a, s.b);
            view.invalidate();
        }
    }

    private void advance() {
        switch (phase) {
            case INTRO1:
                phase = INTRO2;
                rebuild();
                break;
            case INTRO2:
                phase = GUIDED;
                nextStep();
                break;
            case GUIDED: {
                HintSystem.Step s = current();
                if (s != null) {
                    gridView.mark(s.a, s.b, s.v);
                    SoundManager.get().play(SoundManager.MARK);
                    nextStep();
                }
                break;
            }
            case AFTER:
                phase = ACCUSE;
                rebuild();
                break;
            case ACCUSE:
                accuse();
                break;
            default:
                int first = a.nextCase(0);
                if (first == 0) a.goCases(1);
                else a.openCase(first);
        }
    }

    private void accuse() {
        CanvasDialog d = new CanvasDialog(view);
        d.icon = IconRenderer.SEARCH;
        d.title = s(R.string.accuse_title);
        d.message = m.fact;
        d.options = m.items[0];
        d.negative = s(R.string.cancel);
        d.onChoice = i -> {
            if (i == m.culprit(m.solution)) {
                SoundManager.get().play(SoundManager.WIN);
                phase = DONE;
                rebuild();
            } else {
                SoundManager.get().play(SoundManager.BAD);
                view.snack(s(R.string.accuse_wrong_body));
            }
        };
        view.showDialog(d);
    }

    @Override
    protected void build() {
        float top = topBar(s(R.string.tour_title), a::goHome);
        float barH = t.dp(72) + insetBottom;
        body(top, h - barH);
        float y = top + t.dp(4);

        int total = steps.size() + 5, done = phase == INTRO1 ? 0 : phase == INTRO2 ? 1
                : phase == GUIDED ? 2 + Math.max(0, index) : phase == AFTER ? total - 3 : phase == ACCUSE ? total - 2 : total;
        CanvasProgressBar bar = new CanvasProgressBar(done / (float) total, false);
        bar.at(cl, y, cw, t.dp(12));
        scroll.add(bar);
        y += t.dp(24);

        y = text(m.title, ThemeManager.TITLE_L, t.onSurface, cl, y, cw) + t.dp(4);
        if (phase == INTRO1) y = text(m.intro, ThemeManager.BODY_M, t.onSurfaceVariant, cl, y, cw) + t.dp(4);
        y += t.dp(8);
        y = gridView.layout(t, cl, y, cw) + t.dp(16);
        scroll.add(gridView);

        // coach card
        HintSystem.Step st = current();
        String msg;
        switch (phase) {
            case INTRO1:
                msg = s(R.string.tour_intro1);
                break;
            case INTRO2:
                msg = s(R.string.tour_intro2);
                break;
            case GUIDED:
                msg = StepText.describe(m, st) + "\n\n"
                        + s(st.v == DeductionGrid.YES ? R.string.tour_tap_yes : R.string.tour_tap_no);
                break;
            case AFTER:
                msg = s(R.string.tour_after);
                break;
            case ACCUSE:
                msg = s(R.string.tour_fact) + "\n\n" + m.fact;
                break;
            default:
                msg = s(R.string.tour_done) + "\n\n" + m.outro;
        }
        CanvasText ct = new CanvasText(t, msg, ThemeManager.BODY_L, t.onTertiaryContainer, cw - t.dp(72));
        float ch = Math.max(t.dp(72), ct.height + t.dp(32));
        CanvasCard card = new CanvasCard(CanvasCard.FILLED);
        card.color = t.tertiaryContainer;
        card.at(cl, y, cw, ch);
        scroll.add(card);
        CanvasIcon ic = new CanvasIcon(phase == DONE ? IconRenderer.STAR : IconRenderer.BULB, t.onTertiaryContainer, t.dp(24));
        ic.at(cl + t.dp(16), y + t.dp(16), t.dp(24), t.dp(24));
        scroll.add(ic);
        ct.place(cl + t.dp(56), y + t.dp(16));
        scroll.add(ct);
        y += ch + t.dp(16);

        // the clues of the training case; the one used by the current step is highlighted
        if (phase != DONE) {
            y = text(s(R.string.clues_title), ThemeManager.TITLE_M, t.onSurface, cl, y, cw) + t.dp(8);
            for (int i = 0; i < m.clues.size(); i++) {
                CanvasText qt = new CanvasText(t, m.clues.get(i).text, ThemeManager.BODY_M, t.onSurface, cw - t.dp(68));
                float qh = Math.max(t.dp(48), qt.height + t.dp(20));
                CanvasCard qc = new CanvasCard(CanvasCard.LOW);
                qc.radius = 12;
                qc.selected = phase == GUIDED && st != null && st.clue == i;
                qc.at(cl, y, cw, qh);
                scroll.add(qc);
                CanvasIcon badge = new CanvasIcon(IconRenderer.DOC, t.onSecondaryContainer, t.dp(16));
                badge.background = qc.selected ? t.primaryContainer : t.secondaryContainer;
                badge.letter = String.valueOf(i + 1);
                badge.at(cl + t.dp(12), y + (qh - t.dp(30)) / 2f, t.dp(30), t.dp(30));
                scroll.add(badge);
                qt.place(cl + t.dp(54), y + (qh - qt.height) / 2f);
                scroll.add(qt);
                y += qh + t.dp(8);
            }
        }
        scroll.contentHeight = y + t.dp(16) - top;

        CanvasCard bottom = new CanvasCard(CanvasCard.FILLED);
        bottom.radius = 0;
        bottom.color = t.surfaceContainer;
        bottom.at(0, h - barH, w, barH);
        chrome.add(bottom);
        int label = phase == GUIDED ? R.string.tour_do_it : phase == ACCUSE ? R.string.tour_accuse
                : phase == DONE ? R.string.tour_first_case : R.string.next;
        CanvasButton next = new CanvasButton(phase == GUIDED ? CanvasButton.TONAL : CanvasButton.FILLED, s(label),
                phase == GUIDED ? IconRenderer.CHECK : phase == ACCUSE ? IconRenderer.SEARCH : IconRenderer.CHEVRON, this::advance);
        next.at(cl, h - barH + t.dp(12), cw, t.dp(48));
        chrome.add(next);
    }
}
