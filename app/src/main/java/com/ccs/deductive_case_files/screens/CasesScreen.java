package com.ccs.deductive_case_files.screens;

import com.ccs.deductive_case_files.R;
import com.ccs.deductive_case_files.activity.MainActivity;
import com.ccs.deductive_case_files.data.ProgressManager;
import com.ccs.deductive_case_files.game.CaseModel;
import com.ccs.deductive_case_files.graphics.IconRenderer;
import com.ccs.deductive_case_files.ui.CanvasCard;
import com.ccs.deductive_case_files.ui.CanvasChip;
import com.ccs.deductive_case_files.ui.CanvasIcon;
import com.ccs.deductive_case_files.ui.CanvasIllustration;
import com.ccs.deductive_case_files.ui.CanvasNavigationBar;
import com.ccs.deductive_case_files.ui.CanvasProgressBar;
import com.ccs.deductive_case_files.ui.CanvasStars;
import com.ccs.deductive_case_files.ui.CanvasText;
import com.ccs.deductive_case_files.ui.Screen;
import com.ccs.deductive_case_files.ui.ThemeManager;

import android.text.Layout;

import java.util.List;

/** Case collection: difficulty filter, overall progress and a card per case. */
public final class CasesScreen extends Screen {
    private final MainActivity a;

    public CasesScreen(MainActivity a) {
        super(a.canvas);
        this.a = a;
    }

    static int[] DIFF = {R.string.diff_easy, R.string.diff_medium, R.string.diff_hard};
    static int[] KIND = {R.string.kind_matrix, R.string.kind_alibi, R.string.kind_contradiction,
            R.string.kind_cipher, R.string.kind_document, R.string.kind_visual, R.string.kind_sequence};

    @Override
    public float snackbarOffset() {
        return insetBottom + t.dp(96);
    }

    @Override
    public boolean onBack() {
        a.goHome();
        return true;
    }

    @Override
    protected void build() {
        float top = topBar(s(R.string.cases_title), null);
        CanvasNavigationBar nav = HomeScreen.navBar(a, this, 1, w, h, insetBottom);
        body(top, nav.b.top);
        List<CaseModel> cases = a.cases();

        float y = top + t.dp(4), x = cl;
        String[] chips = {s(R.string.filter_all), s(DIFF[0]), s(DIFF[1]), s(DIFF[2])};
        for (int i = 0; i < chips.length; i++) {
            final int f = i - 1;
            CanvasChip chip = new CanvasChip(chips[i]);
            chip.selected = a.filter == f;
            float cwid = chip.preferredWidth(t);
            if (x + cwid > cl + cw) {
                x = cl;
                y += t.dp(48);
            }
            chip.at(x, y, cwid, t.dp(48));
            chip.onClick = () -> {
                a.filter = f;
                scroll = null;
                rebuild();
            };
            scroll.add(chip);
            x += cwid + t.dp(8);
        }
        y += t.dp(56);

        int total = 0, solved = 0;
        for (CaseModel m : cases) {
            if (a.filter >= 0 && m.difficulty != a.filter) continue;
            total++;
            if (a.progress.status(m.id) == ProgressManager.SOLVED) solved++;
        }
        y = text(s(R.string.home_progress, solved, total), ThemeManager.LABEL_L, t.onSurfaceVariant, cl, y, cw);
        CanvasProgressBar bar = new CanvasProgressBar(total == 0 ? 0 : solved / (float) total, false);
        bar.at(cl, y + t.dp(6), cw, t.dp(12));
        scroll.add(bar);
        y += t.dp(30);

        int lastDiff = -1;
        for (final CaseModel m : cases) {
            if (a.filter >= 0 && m.difficulty != a.filter) continue;
            if (m.difficulty != lastDiff) {
                lastDiff = m.difficulty;
                y = text(s(DIFF[m.difficulty]), ThemeManager.TITLE_S, t.primary, cl, y + t.dp(8), cw) + t.dp(10);
            }
            int st = a.progress.status(m.id);
            float th = t.dp(64), textW = cw - th - t.dp(40) - t.dp(76);
            CanvasText title = new CanvasText(t, m.number + ". " + m.title, ThemeManager.TITLE_M, t.onSurface, textW,
                    Layout.Alignment.ALIGN_NORMAL, 2);
            String sub = s(KIND[m.kind]) + " · " + m.n + "×" + m.n + (m.cats > 3 ? " · " + s(R.string.cats_n, m.cats) : "");
            CanvasText subtitle = new CanvasText(t, sub, ThemeManager.BODY_S, t.onSurfaceVariant, textW,
                    Layout.Alignment.ALIGN_NORMAL, 2);
            float ch = Math.max(t.dp(88), title.height + subtitle.height + t.dp(30));
            CanvasCard card = new CanvasCard(st == ProgressManager.SOLVED ? CanvasCard.LOW : CanvasCard.FILLED);
            card.at(cl, y, cw, ch);
            String status = s(st == ProgressManager.SOLVED ? R.string.status_solved
                    : st == ProgressManager.STARTED ? R.string.status_started : R.string.status_new);
            card.desc = m.number + ". " + m.title + ". " + sub + ". " + status;
            card.onClick = () -> a.openCase(m.id);
            scroll.add(card);
            CanvasIllustration thumb = new CanvasIllustration(a.getResources(), m.scene);
            thumb.at(cl + t.dp(12), y + (ch - th) / 2f, th, th);
            scroll.add(thumb);
            float tx = cl + th + t.dp(24), ty = y + (ch - title.height - subtitle.height - t.dp(4)) / 2f;
            title.desc = null;
            subtitle.desc = null;
            title.place(tx, ty);
            subtitle.place(tx, ty + title.height + t.dp(4));
            scroll.add(title);
            scroll.add(subtitle);
            float rx = cl + cw - t.dp(80);
            if (st == ProgressManager.SOLVED) {
                CanvasStars stars = new CanvasStars(a.progress.stars(m.id), t.dp(18), null);
                stars.at(rx, y, t.dp(68), ch);
                scroll.add(stars);
            } else {
                CanvasIcon ic = new CanvasIcon(st == ProgressManager.STARTED ? IconRenderer.PLAY : IconRenderer.CHEVRON,
                        st == ProgressManager.STARTED ? t.tertiary : t.onSurfaceVariant, t.dp(22));
                if (st == ProgressManager.STARTED) ic.background = t.tertiaryContainer;
                ic.at(cl + cw - t.dp(52), y + (ch - t.dp(36)) / 2f, t.dp(36), t.dp(36));
                scroll.add(ic);
            }
            y += ch + t.dp(8);
        }
        scroll.contentHeight = y + t.dp(16) - top;
        chrome.add(nav);
    }
}
