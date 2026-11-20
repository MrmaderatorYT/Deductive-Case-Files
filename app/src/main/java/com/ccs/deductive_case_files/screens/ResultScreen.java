package com.ccs.deductive_case_files.screens;

import android.text.Layout;

import com.ccs.deductive_case_files.R;
import com.ccs.deductive_case_files.activity.MainActivity;
import com.ccs.deductive_case_files.game.CaseModel;
import com.ccs.deductive_case_files.game.DeductionGrid;
import com.ccs.deductive_case_files.game.HintSystem;
import com.ccs.deductive_case_files.graphics.IconRenderer;
import com.ccs.deductive_case_files.ui.CanvasButton;
import com.ccs.deductive_case_files.ui.CanvasCard;
import com.ccs.deductive_case_files.ui.CanvasIcon;
import com.ccs.deductive_case_files.ui.CanvasIllustration;
import com.ccs.deductive_case_files.ui.CanvasStars;
import com.ccs.deductive_case_files.ui.CanvasText;
import com.ccs.deductive_case_files.ui.Screen;
import com.ccs.deductive_case_files.ui.ThemeManager;

import java.util.List;

/** Case result: verdict, the full solution, the chain of deductions and the epilogue. */
public final class ResultScreen extends Screen {
    private final MainActivity a;
    private final CaseModel m;
    private List<HintSystem.Step> steps;

    public ResultScreen(MainActivity a, CaseModel m) {
        super(a.canvas);
        this.a = a;
        this.m = m;
        a.manager.solve(m, () -> a.manager.explain(m, result -> {
            steps = result;
            if (view.screen() == this) rebuild();
        }));
    }

    @Override
    public boolean onBack() {
        a.leaveCase();
        return true;
    }

    @Override
    protected void build() {
        float top = topBar(s(R.string.case_n, m.number), a::leaveCase,
                CanvasButton.icon(IconRenderer.RESTART, s(R.string.result_replay), a::replay));
        final int next = a.nextCase(m.id);
        float barH = t.dp(72) + insetBottom;
        body(top, h - barH);
        float y = top + t.dp(8);

        float ih = Math.min(cw * 0.42f, t.dp(170));
        CanvasIllustration il = new CanvasIllustration(a.getResources(), m.scene);
        il.at(cl, y, cw, ih);
        scroll.add(il);
        y += ih + t.dp(18);
        int stars = a.progress.stars(m.id);
        CanvasStars st = new CanvasStars(stars, t.dp(34), s(R.string.result_stars, stars));
        st.at(cl + cw / 2f - t.dp(66), y, t.dp(132), t.dp(40));
        scroll.add(st);
        y += t.dp(48);
        CanvasText title = new CanvasText(t, s(R.string.result_title), ThemeManager.HEADLINE, t.onSurface, cw,
                Layout.Alignment.ALIGN_CENTER, 3);
        y = place(title, cl, y) + t.dp(4);
        CanvasText sub = new CanvasText(t, m.title, ThemeManager.BODY_L, t.onSurfaceVariant, cw, Layout.Alignment.ALIGN_CENTER, 3);
        y = place(sub, cl, y) + t.dp(4);
        CanvasText stat = new CanvasText(t, s(R.string.result_stats, a.progress.hints(m.id), a.progress.mistakes(m.id)),
                ThemeManager.BODY_S, t.onSurfaceVariant, cw, Layout.Alignment.ALIGN_CENTER, 2);
        y = place(stat, cl, y) + t.dp(20);

        int[][] sol = m.solution;
        if (sol != null) {
            int culprit = m.culprit(sol);
            // verdict
            CanvasText vl = new CanvasText(t, s(R.string.result_culprit), ThemeManager.LABEL_L, t.onTertiaryContainer, cw - t.dp(88));
            CanvasText vn = new CanvasText(t, m.suspects[culprit].name, ThemeManager.TITLE_L, t.onTertiaryContainer, cw - t.dp(88));
            CanvasText vf = new CanvasText(t, m.fact + " " + s(R.string.result_fact_note), ThemeManager.BODY_M, t.onTertiaryContainer, cw - t.dp(32));
            float vh = vl.height + vn.height + vf.height + t.dp(52);
            CanvasCard card = new CanvasCard(CanvasCard.FILLED);
            card.color = t.tertiaryContainer;
            card.at(cl, y, cw, vh);
            scroll.add(card);
            CanvasIcon av = new CanvasIcon(IconRenderer.PERSON, t.tertiaryContainer, t.dp(22));
            av.background = t.onTertiaryContainer;
            av.at(cl + t.dp(16), y + t.dp(16), t.dp(44), t.dp(44));
            scroll.add(av);
            vl.place(cl + t.dp(72), y + t.dp(16));
            vn.place(cl + t.dp(72), y + t.dp(18) + vl.height);
            vf.place(cl + t.dp(16), y + t.dp(34) + vl.height + vn.height);
            scroll.add(vl);
            scroll.add(vn);
            scroll.add(vf);
            y += vh + t.dp(20);

            // solution table
            y = text(s(R.string.result_solution), ThemeManager.TITLE_M, t.onSurface, cl, y, cw) + t.dp(8);
            for (int g = 0; g < m.n; g++) {
                StringBuilder sb = new StringBuilder();
                for (int c = 1; c < m.cats; c++) sb.append(c > 1 ? " · " : "").append(m.items[c][sol[c][g]]);
                CanvasText nm = new CanvasText(t, m.suspects[g].name, ThemeManager.TITLE_S, t.onSurface, cw - t.dp(32));
                CanvasText it = new CanvasText(t, sb, ThemeManager.BODY_M, t.onSurfaceVariant, cw - t.dp(32));
                float rh = nm.height + it.height + t.dp(22);
                CanvasCard row = new CanvasCard(g == culprit ? CanvasCard.OUTLINED : CanvasCard.LOW);
                row.radius = 12;
                row.selected = g == culprit;
                row.at(cl, y, cw, rh);
                scroll.add(row);
                nm.place(cl + t.dp(16), y + t.dp(10));
                it.place(cl + t.dp(16), y + t.dp(12) + nm.height);
                scroll.add(nm);
                scroll.add(it);
                y += rh + t.dp(6);
            }
            y += t.dp(14);
        }

        // explanation
        y = text(s(R.string.result_explanation), ThemeManager.TITLE_M, t.onSurface, cl, y, cw) + t.dp(8);
        if (steps == null) {
            y = text("…", ThemeManager.BODY_M, t.onSurfaceVariant, cl, y, cw);
        } else {
            int num = 0;
            for (HintSystem.Step s : steps) {
                boolean key = s.v == DeductionGrid.YES || s.rule == HintSystem.R_CLUE
                        || s.rule == HintSystem.R_LOGIC || s.rule == HintSystem.R_CONTRA;
                if (!key) continue;
                num++;
                CanvasText n = new CanvasText(t, num + ".", ThemeManager.LABEL_L, t.primary, t.dp(34));
                n.desc = null;
                n.place(cl, y + t.dp(1));
                scroll.add(n);
                y = text(StepText.describe(m, s), ThemeManager.BODY_M, s.v == DeductionGrid.YES ? t.onSurface : t.onSurfaceVariant,
                        cl + t.dp(36), y, cw - t.dp(36)) + t.dp(8);
            }
            if (sol != null) {
                y = text(s(R.string.result_final, m.itemName(m.key), m.suspects[m.culprit(sol)].name), ThemeManager.BODY_L,
                        t.onSurface, cl, y + t.dp(6), cw);
            }
        }
        y += t.dp(18);
        y = text(s(R.string.result_epilogue), ThemeManager.TITLE_M, t.onSurface, cl, y, cw) + t.dp(8);
        y = text(m.outro, ThemeManager.BODY_L, t.onSurface, cl, y, cw);
        scroll.contentHeight = y + t.dp(20) - top;

        CanvasCard bar = new CanvasCard(CanvasCard.FILLED);
        bar.radius = 0;
        bar.color = t.surfaceContainer;
        bar.at(0, h - barH, w, barH);
        chrome.add(bar);
        float by = h - barH + t.dp(12), half = (cw - t.dp(12)) / 2f;
        CanvasButton list = new CanvasButton(CanvasButton.OUTLINED, s(R.string.result_collection), -1, () -> a.goCases(-1));
        if (next > 0) {
            list.at(cl, by, half, t.dp(48));
            CanvasButton nx = new CanvasButton(CanvasButton.FILLED, s(R.string.result_next), IconRenderer.CHEVRON, () -> a.openCase(next));
            nx.at(cl + half + t.dp(12), by, half, t.dp(48));
            chrome.add(nx);
        } else {
            list.at(cl, by, cw, t.dp(48));
        }
        chrome.add(list);
    }

    private float place(CanvasText ct, float x, float y) {
        ct.place(x, y);
        scroll.add(ct);
        return ct.b.bottom;
    }
}
