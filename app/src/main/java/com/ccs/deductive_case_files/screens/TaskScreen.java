package com.ccs.deductive_case_files.screens;

import android.graphics.Canvas;

import com.ccs.deductive_case_files.R;
import com.ccs.deductive_case_files.activity.MainActivity;
import com.ccs.deductive_case_files.game.CaseModel;
import com.ccs.deductive_case_files.game.Cipher;
import com.ccs.deductive_case_files.game.ClueModel;
import com.ccs.deductive_case_files.graphics.IconRenderer;
import com.ccs.deductive_case_files.localization.LocaleManager;
import com.ccs.deductive_case_files.ui.CanvasButton;
import com.ccs.deductive_case_files.ui.CanvasCard;
import com.ccs.deductive_case_files.ui.CanvasChip;
import com.ccs.deductive_case_files.ui.CanvasComponent;
import com.ccs.deductive_case_files.ui.CanvasIcon;
import com.ccs.deductive_case_files.ui.CanvasIllustration;
import com.ccs.deductive_case_files.ui.CanvasText;
import com.ccs.deductive_case_files.ui.Screen;
import com.ccs.deductive_case_files.ui.SoundManager;
import com.ccs.deductive_case_files.ui.ThemeManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Special evidence task of a case: alibi analysis, contradiction hunt, cipher, document,
 * visual puzzle (single choice) or putting events in order. Solving it reveals a locked clue.
 */
public final class TaskScreen extends Screen {
    private final MainActivity a;
    private final CaseModel m;
    private int selected = -1;
    /** For ordering tasks: option indices in the order the player tapped them. */
    private final List<Integer> order = new ArrayList<>();
    private final int[] display;

    public TaskScreen(MainActivity a, CaseModel m) {
        super(a.canvas);
        this.a = a;
        this.m = m;
        int n = m.taskOptions.length;
        display = new int[n];
        for (int i = 0; i < n; i++) display[i] = i;
        if (m.taskMode == CaseModel.TASK_ORDER && n > 1) {
            Random r = new Random(m.id * 31L + 7);
            boolean identity = true;
            while (identity) {
                for (int i = n - 1; i > 0; i--) {
                    int j = r.nextInt(i + 1), tmp = display[i];
                    display[i] = display[j];
                    display[j] = tmp;
                }
                for (int i = 0; i < n; i++) if (display[i] != i) identity = false;
            }
        }
    }

    @Override
    public boolean onBack() {
        a.backToCase();
        return true;
    }

    @Override
    public float snackbarOffset() {
        return insetBottom + t.dp(88);
    }

    @Override
    protected void build() {
        final boolean solved = a.progress.taskSolved(m.id);
        final boolean ordering = m.taskMode == CaseModel.TASK_ORDER;
        float top = topBar(s(R.string.section_task), a::backToCase);
        float barH = t.dp(72) + insetBottom;
        body(top, h - barH);
        float y = top + t.dp(8);

        CanvasChip kind = new CanvasChip(s(CasesScreen.KIND[m.kind]));
        kind.tonal = true;
        kind.at(cl, y, kind.preferredWidth(t), t.dp(36));
        scroll.add(kind);
        y += t.dp(44);
        y = text(m.taskTitle, ThemeManager.HEADLINE, t.onSurface, cl, y, cw) + t.dp(12);

        if (m.kind == CaseModel.VISUAL) {
            float ih = Math.min(cw * 9f / 16f, t.dp(260));
            CanvasIllustration il = new CanvasIllustration(a.getResources(), m.taskSpec, m.taskTitle);
            il.at(cl, y, cw, ih);
            scroll.add(il);
            y += ih + t.dp(14);
            y = text(m.taskBody, ThemeManager.BODY_L, t.onSurface, cl, y, cw) + t.dp(16);
        } else {
            // the evidence itself on a "paper" card
            String body = m.taskBody;
            CanvasText code = null, abc = null;
            if (m.kind == CaseModel.CIPHER) {
                String lang = LocaleManager.lang();
                code = new CanvasText(t, Cipher.encode(m.taskPlain, m.taskSpec, lang), ThemeManager.MONO, t.onSurface, cw - t.dp(32));
                abc = new CanvasText(t, spaced(Cipher.alphabet(lang)), ThemeManager.BODY_S, t.onSurfaceVariant, cw - t.dp(32));
            }
            CanvasText bt = new CanvasText(t, body, ThemeManager.BODY_M, t.onSurface, cw - t.dp(32));
            float ph = bt.height + t.dp(32) + (code != null ? code.height + abc.height + t.dp(28) : 0);
            CanvasCard paper = new CanvasCard(CanvasCard.OUTLINED);
            paper.color = t.surfaceLowest;
            paper.radius = 12;
            paper.at(cl, y, cw, ph);
            scroll.add(paper);
            float py = y + t.dp(16);
            if (code != null) {
                code.place(cl + t.dp(16), py);
                scroll.add(code);
                py += code.height + t.dp(12);
            }
            bt.place(cl + t.dp(16), py);
            scroll.add(bt);
            py += bt.height;
            if (abc != null) {
                abc.place(cl + t.dp(16), py + t.dp(12));
                scroll.add(abc);
            }
            y += ph + t.dp(18);
        }

        y = text(m.taskQuestion, ThemeManager.TITLE_M, t.onSurface, cl, y, cw) + t.dp(6);
        if (ordering && !solved) y = text(s(R.string.task_order_help), ThemeManager.BODY_S, t.onSurfaceVariant, cl, y, cw);
        y += t.dp(8);

        for (int d = 0; d < display.length; d++) {
            final int opt = solved && ordering ? d : display[d];
            int pos = ordering ? (solved ? d : order.indexOf(opt)) : -1;
            boolean sel = ordering ? pos >= 0 : (solved ? opt == m.taskAnswer : opt == selected);
            CanvasText ot = new CanvasText(t, m.taskOptions[opt], ThemeManager.BODY_L, t.onSurface, cw - t.dp(76));
            float rh = Math.max(t.dp(56), ot.height + t.dp(24));
            CanvasCard row = new CanvasCard(CanvasCard.OUTLINED);
            row.radius = 12;
            row.selected = sel;
            row.at(cl, y, cw, rh);
            row.desc = (pos >= 0 ? (pos + 1) + ". " : "") + m.taskOptions[opt];
            if (!solved) {
                row.onClick = () -> {
                    if (ordering) {
                        int at = order.indexOf(opt);
                        if (at >= 0) while (order.size() > at) order.remove(order.size() - 1);
                        else order.add(opt);
                    } else selected = opt;
                    rebuild();
                };
            }
            scroll.add(row);
            Marker mk = new Marker(sel, ordering && pos >= 0 ? String.valueOf(pos + 1) : null, ordering);
            mk.at(cl + t.dp(14), y + (rh - t.dp(28)) / 2f, t.dp(28), t.dp(28));
            scroll.add(mk);
            ot.desc = null;
            ot.place(cl + t.dp(56), y + (rh - ot.height) / 2f);
            scroll.add(ot);
            y += rh + t.dp(8);
        }

        if (solved) {
            String clueText = "";
            for (ClueModel c : m.clues) if (c.locked) clueText = c.text;
            CanvasText st = new CanvasText(t, s(R.string.task_correct), ThemeManager.TITLE_M, t.onSecondaryContainer, cw - t.dp(72));
            CanvasText sb = new CanvasText(t, m.taskSolved + "\n\n" + s(R.string.task_new_clue) + " " + clueText,
                    ThemeManager.BODY_M, t.onSecondaryContainer, cw - t.dp(72));
            float sh = st.height + sb.height + t.dp(38);
            CanvasCard ok = new CanvasCard(CanvasCard.FILLED);
            ok.color = t.secondaryContainer;
            ok.at(cl, y + t.dp(8), cw, sh);
            scroll.add(ok);
            CanvasIcon ic = new CanvasIcon(IconRenderer.UNLOCK, t.onSecondaryContainer, t.dp(24));
            ic.at(cl + t.dp(16), y + t.dp(24), t.dp(24), t.dp(24));
            scroll.add(ic);
            st.place(cl + t.dp(56), y + t.dp(24));
            sb.place(cl + t.dp(56), y + t.dp(30) + st.height);
            scroll.add(st);
            scroll.add(sb);
            y += sh + t.dp(8);
        }
        scroll.contentHeight = y + t.dp(16) - top;

        CanvasCard bar = new CanvasCard(CanvasCard.FILLED);
        bar.radius = 0;
        bar.color = t.surfaceContainer;
        bar.at(0, h - barH, w, barH);
        chrome.add(bar);
        float by = h - barH + t.dp(12);
        if (solved) {
            CanvasButton back = new CanvasButton(CanvasButton.FILLED, s(R.string.task_back), IconRenderer.GRID, () -> {
                a.caseTab = 1;
                a.backToCase();
            });
            back.at(cl, by, cw, t.dp(48));
            chrome.add(back);
        } else {
            CanvasButton answer = new CanvasButton(CanvasButton.FILLED, s(R.string.task_answer), IconRenderer.CHECK, this::answer);
            answer.enabled = ordering ? order.size() == display.length : selected >= 0;
            answer.at(cl, by, cw, t.dp(48));
            chrome.add(answer);
        }
    }

    private static String spaced(String abc) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < abc.length(); i++) sb.append(i > 0 ? " " : "").append(abc.charAt(i));
        return sb.toString();
    }

    private void answer() {
        boolean right;
        if (m.taskMode == CaseModel.TASK_ORDER) {
            right = true;
            for (int i = 0; i < order.size(); i++) if (order.get(i) != i) right = false;
        } else {
            right = selected == m.taskAnswer;
        }
        if (right) {
            a.progress.setTaskSolved(m.id);
            SoundManager.get().play(SoundManager.OK);
            rebuild();
            scroll.scrollTo(scroll.max());
        } else {
            a.progress.addMistake(m.id);
            SoundManager.get().play(SoundManager.BAD);
            selected = -1;
            order.clear();
            rebuild();
            view.snack(s(R.string.task_wrong));
        }
    }

    /** Radio dot or order number in front of an option. */
    private static final class Marker extends CanvasComponent {
        private final boolean on, square;
        private final String number;

        Marker(boolean on, String number, boolean square) {
            this.on = on;
            this.number = number;
            this.square = square;
        }

        @Override
        public void draw(Canvas c, ThemeManager t) {
            float r = t.dp(10), cx = b.centerX(), cy = b.centerY();
            if (square) {
                t.rect.set(cx - t.dp(13), cy - t.dp(13), cx + t.dp(13), cy + t.dp(13));
                if (on) {
                    t.fill.setColor(t.primary);
                    c.drawRoundRect(t.rect, t.dp(8), t.dp(8), t.fill);
                    android.text.TextPaint p = t.text(ThemeManager.LABEL_L, t.onPrimary);
                    c.drawText(number, cx - p.measureText(number) / 2f, t.baseline(ThemeManager.LABEL_L, cy), p);
                } else {
                    t.stroke.setColor(t.outline);
                    t.stroke.setStrokeWidth(t.dp(1.5f));
                    c.drawRoundRect(t.rect, t.dp(8), t.dp(8), t.stroke);
                }
                return;
            }
            t.stroke.setColor(on ? t.primary : t.onSurfaceVariant);
            t.stroke.setStrokeWidth(t.dp(2));
            c.drawCircle(cx, cy, r, t.stroke);
            if (on) {
                t.fill.setColor(t.primary);
                c.drawCircle(cx, cy, t.dp(5), t.fill);
            }
        }
    }
}
