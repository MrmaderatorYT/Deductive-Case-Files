package com.ccs.deductive_case_files.screens;

import android.graphics.Canvas;

import com.ccs.deductive_case_files.R;
import com.ccs.deductive_case_files.activity.MainActivity;
import com.ccs.deductive_case_files.game.CaseManager;
import com.ccs.deductive_case_files.game.CaseModel;
import com.ccs.deductive_case_files.game.ClueModel;
import com.ccs.deductive_case_files.game.DeductionGrid;
import com.ccs.deductive_case_files.game.SuspectModel;
import com.ccs.deductive_case_files.graphics.IconRenderer;
import com.ccs.deductive_case_files.ui.CanvasButton;
import com.ccs.deductive_case_files.ui.CanvasCard;
import com.ccs.deductive_case_files.ui.CanvasChip;
import com.ccs.deductive_case_files.ui.CanvasComponent;
import com.ccs.deductive_case_files.ui.CanvasDialog;
import com.ccs.deductive_case_files.ui.CanvasGrid;
import com.ccs.deductive_case_files.ui.CanvasIcon;
import com.ccs.deductive_case_files.ui.CanvasIllustration;
import com.ccs.deductive_case_files.ui.CanvasNavigationBar;
import com.ccs.deductive_case_files.ui.CanvasText;
import com.ccs.deductive_case_files.ui.Screen;
import com.ccs.deductive_case_files.ui.SoundManager;
import com.ccs.deductive_case_files.ui.ThemeManager;

/** A case: the brief (story, suspects, key fact, evidence task) and the investigation matrix. */
public final class CaseScreen extends Screen {
    private final MainActivity a;
    private final CaseModel m;
    private final DeductionGrid grid;
    private final CanvasGrid gridView;
    private boolean busy;

    public CaseScreen(MainActivity a, CaseModel m) {
        super(a.canvas);
        this.a = a;
        this.m = m;
        grid = new DeductionGrid(m.n, m.cats);
        grid.deserialize(a.progress.grid(m.id));
        String[] states = {str(R.string.grid_empty), str(R.string.grid_no), str(R.string.grid_yes)};
        gridView = new CanvasGrid(view, m, grid, states, v -> {
            a.progress.saveGrid(m.id, grid.serialize());
            SoundManager.get().play(SoundManager.MARK);
        });
        a.manager.solve(m, () -> {
        });
    }

    private boolean taskSolved() {
        return !m.hasTask() || a.progress.taskSolved(m.id);
    }

    @Override
    public boolean onBack() {
        a.leaveCase();
        return true;
    }

    @Override
    public float snackbarOffset() {
        return insetBottom + t.dp(88);
    }

    @Override
    protected void build() {
        CanvasButton restart = CanvasButton.icon(IconRenderer.RESTART, s(R.string.restart), this::confirmRestart);
        float top = topBar(s(R.string.case_n, m.number), a::leaveCase, restart);
        String[] tabs = {s(R.string.tab_brief), s(R.string.tab_investigate)};
        CanvasNavigationBar tabBar = new CanvasNavigationBar(view, tabs, null, a.caseTab, this::setTab);
        tabBar.tabs = true;
        tabBar.at(0, top, w, t.dp(48));
        float barH = t.dp(72) + insetBottom;
        body(top + t.dp(48), h - barH);
        float y = top + t.dp(48) + t.dp(16);
        y = a.caseTab == 0 ? buildBrief(y) : buildInvestigate(y);
        scroll.contentHeight = y + t.dp(16) - scroll.b.top;

        chrome.add(tabBar);
        BottomBar bar = new BottomBar();
        bar.at(0, h - barH, w, barH);
        chrome.add(bar);
        float by = h - barH + t.dp(12);
        if (a.caseTab == 0) {
            CanvasButton go = new CanvasButton(CanvasButton.FILLED, s(R.string.to_investigation), IconRenderer.GRID, () -> setTab(1));
            go.at(cl, by, cw, t.dp(48));
            chrome.add(go);
        } else {
            float half = (cw - t.dp(12)) / 2f;
            CanvasButton hint = new CanvasButton(CanvasButton.TONAL, s(R.string.hint), IconRenderer.BULB, this::onHint);
            hint.at(cl, by, half, t.dp(48));
            CanvasButton check = new CanvasButton(CanvasButton.FILLED, s(R.string.check), IconRenderer.CHECK, this::onCheck);
            check.at(cl + half + t.dp(12), by, half, t.dp(48));
            chrome.add(hint);
            chrome.add(check);
        }
    }

    private void setTab(int tab) {
        a.caseTab = tab;
        scroll = null;
        rebuild();
    }

    // ------------------------------------------------------------------ brief

    private float buildBrief(float y) {
        float ih = Math.min(cw * 9f / 16f, t.dp(230));
        CanvasIllustration il = new CanvasIllustration(a.getResources(), m.scene);
        il.at(cl, y, cw, ih);
        scroll.add(il);
        y += ih + t.dp(16);
        y = text(m.title, ThemeManager.HEADLINE, t.onSurface, cl, y, cw) + t.dp(8);

        float x = cl;
        String[] chips = {s(CasesScreen.DIFF[m.difficulty]), s(CasesScreen.KIND[m.kind]), m.n + "×" + m.n};
        for (String label : chips) {
            CanvasChip chip = new CanvasChip(label);
            float cwid = chip.preferredWidth(t);
            if (x + cwid > cl + cw) {
                x = cl;
                y += t.dp(40);
            }
            chip.at(x, y, cwid, t.dp(36));
            scroll.add(chip);
            x += cwid + t.dp(8);
        }
        y += t.dp(48);
        y = text(m.intro, ThemeManager.BODY_L, t.onSurface, cl, y, cw) + t.dp(16);

        // key fact
        CanvasText fl = new CanvasText(t, s(R.string.section_fact), ThemeManager.LABEL_L, t.onTertiaryContainer, cw - t.dp(72));
        CanvasText ft = new CanvasText(t, m.fact, ThemeManager.BODY_L, t.onTertiaryContainer, cw - t.dp(72));
        float fh = fl.height + ft.height + t.dp(36);
        CanvasCard fact = new CanvasCard(CanvasCard.FILLED);
        fact.color = t.tertiaryContainer;
        fact.at(cl, y, cw, fh);
        scroll.add(fact);
        CanvasIcon fi = new CanvasIcon(IconRenderer.SEARCH, t.onTertiaryContainer, t.dp(24));
        fi.at(cl + t.dp(16), y + t.dp(16), t.dp(24), t.dp(24));
        scroll.add(fi);
        fl.place(cl + t.dp(56), y + t.dp(16));
        ft.place(cl + t.dp(56), y + t.dp(20) + fl.height);
        scroll.add(fl);
        scroll.add(ft);
        y += fh + t.dp(20);

        // suspects
        y = text(m.catNames[0], ThemeManager.TITLE_M, t.onSurface, cl, y, cw) + t.dp(8);
        for (SuspectModel sp : m.suspects) {
            CanvasText name = new CanvasText(t, sp.name, ThemeManager.TITLE_S, t.onSurface, cw - t.dp(68));
            CanvasText role = new CanvasText(t, sp.role, ThemeManager.BODY_M, t.onSurfaceVariant, cw - t.dp(68));
            float rh = Math.max(t.dp(56), name.height + role.height + t.dp(16));
            CanvasIcon av = new CanvasIcon(IconRenderer.PERSON, t.onSecondaryContainer, t.dp(20));
            av.background = t.secondaryContainer;
            av.letter = sp.name.isEmpty() ? "?" : sp.name.substring(0, 1);
            av.at(cl, y + (rh - t.dp(40)) / 2f, t.dp(40), t.dp(40));
            scroll.add(av);
            float ty = y + (rh - name.height - role.height) / 2f;
            name.place(cl + t.dp(56), ty);
            role.place(cl + t.dp(56), ty + name.height);
            scroll.add(name);
            scroll.add(role);
            y += rh;
        }
        y += t.dp(12);
        int[] icons = {IconRenderer.PIN, IconRenderer.TAG, IconRenderer.CLOCK};
        for (int c = 1; c < m.cats; c++) {
            CanvasIcon ic = new CanvasIcon(c == m.ordered ? IconRenderer.CLOCK : icons[(c - 1) % 2], t.primary, t.dp(20));
            ic.at(cl + t.dp(10), y, t.dp(20), t.dp(20));
            scroll.add(ic);
            float yy = text(m.catNames[c], ThemeManager.TITLE_S, t.onSurface, cl + t.dp(56), y, cw - t.dp(56));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < m.n; i++) sb.append(i > 0 ? " · " : "").append(m.items[c][i]);
            y = text(sb, ThemeManager.BODY_M, t.onSurfaceVariant, cl + t.dp(56), yy + t.dp(2), cw - t.dp(56)) + t.dp(14);
        }

        if (m.hasTask()) {
            boolean done = a.progress.taskSolved(m.id);
            CanvasText tl = new CanvasText(t, s(R.string.section_task) + " · " + s(CasesScreen.KIND[m.kind]),
                    ThemeManager.LABEL_L, t.primary, cw - t.dp(110));
            CanvasText tt = new CanvasText(t, m.taskTitle, ThemeManager.TITLE_M, t.onSurface, cw - t.dp(110));
            CanvasText ts = new CanvasText(t, s(done ? R.string.task_done : R.string.task_todo), ThemeManager.BODY_M,
                    t.onSurfaceVariant, cw - t.dp(110));
            float th = tl.height + tt.height + ts.height + t.dp(40);
            CanvasCard card = new CanvasCard(CanvasCard.OUTLINED);
            card.at(cl, y + t.dp(6), cw, th);
            card.desc = s(R.string.section_task) + ". " + m.taskTitle + ". " + s(done ? R.string.task_done : R.string.task_todo);
            card.onClick = a::openTask;
            scroll.add(card);
            y += t.dp(6);
            CanvasIcon ic = new CanvasIcon(done ? IconRenderer.UNLOCK : IconRenderer.LOCK,
                    done ? t.onSecondaryContainer : t.onTertiaryContainer, t.dp(22));
            ic.background = done ? t.secondaryContainer : t.tertiaryContainer;
            ic.at(cl + t.dp(16), y + (th - t.dp(40)) / 2f, t.dp(40), t.dp(40));
            scroll.add(ic);
            tl.desc = tt.desc = ts.desc = null;
            float ty = y + t.dp(16);
            tl.place(cl + t.dp(72), ty);
            tt.place(cl + t.dp(72), ty + tl.height + t.dp(4));
            ts.place(cl + t.dp(72), ty + tl.height + tt.height + t.dp(8));
            scroll.add(tl);
            scroll.add(tt);
            scroll.add(ts);
            CanvasIcon chev = new CanvasIcon(IconRenderer.CHEVRON, t.onSurfaceVariant, t.dp(22));
            chev.at(cl + cw - t.dp(36), y + (th - t.dp(22)) / 2f, t.dp(22), t.dp(22));
            scroll.add(chev);
            y += th;
        }
        return y;
    }

    // ------------------------------------------------------------------ investigation

    private float buildInvestigate(float y) {
        y = gridView.layout(t, cl, y, cw) + t.dp(14);
        scroll.add(gridView);
        String help = s(R.string.grid_help) + (gridView.pannable() ? " " + s(R.string.grid_pan) : "");
        y = text(help, ThemeManager.BODY_S, t.onSurfaceVariant, cl, y, cw) + t.dp(18);

        y = text(s(R.string.clues_title), ThemeManager.TITLE_M, t.onSurface, cl, y, cw) + t.dp(4);
        y = text(m.fact, ThemeManager.BODY_M, t.tertiary, cl, y, cw) + t.dp(10);
        final int struck = a.progress.struck(m.id);
        for (int i = 0; i < m.clues.size(); i++) {
            final int idx = i;
            ClueModel clue = m.clues.get(i);
            boolean locked = clue.locked && !taskSolved();
            boolean used = (struck & (1 << i)) != 0;
            String txt = locked ? s(R.string.clue_locked) : clue.text;
            CanvasText ct = new CanvasText(t, txt, ThemeManager.BODY_M, locked ? t.onSurfaceVariant : t.onSurface, cw - t.dp(68));
            float ch = Math.max(t.dp(52), ct.height + t.dp(24));
            CanvasCard card = new CanvasCard(locked ? CanvasCard.OUTLINED : CanvasCard.LOW);
            card.radius = 12;
            card.at(cl, y, cw, ch);
            card.desc = s(R.string.clue_n, i + 1) + ". " + txt;
            card.onClick = locked ? a::openTask : () -> {
                a.progress.setStruck(m.id, struck ^ (1 << idx));
                rebuild();
            };
            scroll.add(card);
            CanvasIcon badge = new CanvasIcon(IconRenderer.LOCK, locked ? t.onTertiaryContainer : t.onSecondaryContainer, t.dp(16));
            badge.background = locked ? t.tertiaryContainer : used ? t.surfaceHighest : t.secondaryContainer;
            if (!locked) badge.letter = used ? "✓" : String.valueOf(i + 1);
            badge.at(cl + t.dp(12), y + (ch - t.dp(30)) / 2f, t.dp(30), t.dp(30));
            scroll.add(badge);
            ct.desc = null;
            ct.alpha = used ? 0.45f : 1f;
            ct.place(cl + t.dp(54), y + (ch - ct.height) / 2f);
            scroll.add(ct);
            y += ch + t.dp(8);
        }
        return y;
    }

    // ------------------------------------------------------------------ actions

    private void dialog(int icon, String title, String message, String positive, Runnable onPositive, String negative) {
        CanvasDialog d = new CanvasDialog(view);
        d.icon = icon;
        d.title = title;
        d.message = message;
        d.positive = positive;
        d.onPositive = onPositive;
        d.negative = negative;
        view.showDialog(d);
    }

    private void confirmRestart() {
        dialog(IconRenderer.RESTART, s(R.string.restart_title), s(R.string.restart_body), s(R.string.restart), () -> {
            a.progress.resetCase(m.id);
            grid.clear();
            gridView.clearFlags();
            rebuild();
        }, s(R.string.cancel));
    }

    private void save() {
        a.progress.saveGrid(m.id, grid.serialize());
    }

    private void onHint() {
        if (busy) return;
        busy = true;
        a.manager.solve(m, () -> {
            if (m.solution == null) {
                busy = false;
                return;
            }
            if (gridView.flagErrors(m.solution) > 0) {
                busy = false;
                a.progress.addHint(m.id);
                view.invalidate();
                dialog(IconRenderer.BULB, s(R.string.hint), s(R.string.hint_errors), s(R.string.close), null, null);
                return;
            }
            final boolean unlocked = taskSolved();
            a.manager.hint(m, grid.copy(), unlocked, step -> {
                busy = false;
                if (view.screen() != this) return;
                if (step == null) {
                    if (!unlocked) {
                        dialog(IconRenderer.LOCK, s(R.string.hint), s(R.string.hint_need_task), s(R.string.task_open), a::openTask, s(R.string.close));
                    } else {
                        dialog(IconRenderer.BULB, s(R.string.hint), s(R.string.hint_none), s(R.string.close), null, null);
                    }
                    return;
                }
                a.progress.addHint(m.id);
                gridView.flagHint(step.a, step.b);
                if (scroll != null) scroll.scrollTo(0);
                CanvasDialog d = new CanvasDialog(view);
                d.anchorBottom = true;
                d.title = s(R.string.hint);
                d.message = StepText.describe(m, step);
                d.negative = s(R.string.close);
                d.positive = s(R.string.hint_apply);
                d.onPositive = () -> {
                    gridView.mark(step.a, step.b, step.v);
                    gridView.clearFlags();
                    save();
                    SoundManager.get().play(SoundManager.MARK);
                    view.contentChanged();
                };
                view.showDialog(d);
            });
        });
    }

    private void onCheck() {
        if (busy) return;
        busy = true;
        a.manager.solve(m, () -> {
            busy = false;
            if (m.solution == null || view.screen() != this) return;
            int errors = gridView.flagErrors(m.solution);
            view.invalidate();
            if (errors > 0) {
                a.progress.addMistake(m.id);
                SoundManager.get().play(SoundManager.BAD);
                view.snack(s(R.string.check_errors, errors));
            } else if (!grid.isComplete(m.solution)) {
                view.snack(s(R.string.check_incomplete));
            } else {
                SoundManager.get().play(SoundManager.OK);
                accuse();
            }
        });
    }

    /** The matrix shows who was where; guilt follows only from the key fact. */
    private void accuse() {
        CanvasDialog d = new CanvasDialog(view);
        d.icon = IconRenderer.SEARCH;
        d.title = s(R.string.accuse_title);
        d.message = s(R.string.accuse_body) + "\n\n" + m.fact;
        d.options = m.items[0];
        d.negative = s(R.string.cancel);
        d.onChoice = i -> {
            if (i == m.culprit(m.solution)) {
                int stars = CaseManager.stars(a.progress.hints(m.id), a.progress.mistakes(m.id));
                a.progress.setSolved(m.id, stars);
                SoundManager.get().play(SoundManager.WIN);
                a.showResult();
            } else {
                a.progress.addMistake(m.id);
                SoundManager.get().play(SoundManager.BAD);
                dialog(IconRenderer.CLOSE, s(R.string.accuse_wrong_title), s(R.string.accuse_wrong_body), s(R.string.close), null, null);
            }
        };
        view.showDialog(d);
    }

    private static final class BottomBar extends CanvasComponent {
        @Override
        public void draw(Canvas c, ThemeManager t) {
            t.fill.setColor(t.surfaceContainer);
            c.drawRect(b, t.fill);
        }
    }
}
