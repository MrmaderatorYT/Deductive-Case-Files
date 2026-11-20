package com.ccs.deductive_case_files.screens;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import com.ccs.deductive_case_files.R;
import com.ccs.deductive_case_files.activity.MainActivity;
import com.ccs.deductive_case_files.data.CaseRepository;
import com.ccs.deductive_case_files.data.ProgressManager;
import com.ccs.deductive_case_files.game.CaseModel;
import com.ccs.deductive_case_files.graphics.IconRenderer;
import com.ccs.deductive_case_files.ui.CanvasButton;
import com.ccs.deductive_case_files.ui.CanvasCard;
import com.ccs.deductive_case_files.ui.CanvasComponent;
import com.ccs.deductive_case_files.ui.CanvasIllustration;
import com.ccs.deductive_case_files.ui.CanvasNavigationBar;
import com.ccs.deductive_case_files.ui.CanvasProgressBar;
import com.ccs.deductive_case_files.ui.CanvasText;
import com.ccs.deductive_case_files.ui.Screen;
import com.ccs.deductive_case_files.ui.ThemeManager;

import java.util.List;

/** Home: title, progress and the main actions. */
public final class HomeScreen extends Screen {
    private static final String WORDMARK = "Deductive";
    private final MainActivity a;

    public HomeScreen(MainActivity a) {
        super(a.canvas);
        this.a = a;
    }

    static CanvasNavigationBar navBar(MainActivity a, Screen s, int selected, float w, float h, float insetBottom) {
        ThemeManager t = ThemeManager.get();
        String[] labels = {s.str(R.string.nav_home), s.str(R.string.nav_cases), s.str(R.string.nav_settings)};
        int[] icons = {IconRenderer.HOME, IconRenderer.FOLDER, IconRenderer.SETTINGS};
        CanvasNavigationBar nav = new CanvasNavigationBar(a.canvas, labels, icons, selected, i -> {
            if (i == 0) a.goHome();
            else if (i == 1) a.goCases(0);
            else a.openSettings();
        });
        nav.bottomInset = insetBottom;
        nav.at(0, h - t.dp(80) - insetBottom, w, t.dp(80) + insetBottom);
        return nav;
    }

    @Override
    public float snackbarOffset() {
        return insetBottom + t.dp(96);
    }

    @Override
    protected void build() {
        CanvasNavigationBar nav = navBar(a, this, 0, w, h, insetBottom);
        body(0, nav.b.top);
        List<CaseModel> cases = a.cases();
        int solved = 0, stars = 0;
        for (CaseModel m : cases) {
            if (a.progress.status(m.id) == ProgressManager.SOLVED) solved++;
            stars += a.progress.stars(m.id);
        }

        float y = masthead(insetTop + t.dp(18));
        float ih = Math.min(cw * 0.54f, t.dp(245));
        CanvasIllustration hero = new CanvasIllustration(a.getResources(), "HERO");
        hero.at(cl, y, cw, ih);
        scroll.add(hero);
        y = text(s(R.string.tagline), ThemeManager.BODY_M, t.onSurfaceVariant, cl, y + ih + t.dp(14), cw);
        y += t.dp(12);

        final int last = a.progress.lastCase();
        boolean canContinue = last > 0 && a.progress.status(last) == ProgressManager.STARTED;
        float bh = Math.max(t.dp(60), t.textHeight(ThemeManager.LABEL_L) + t.dp(26));
        if (canContinue) {
            CaseModel lm = CaseRepository.byId(last);
            y = button(new CanvasButton(CanvasButton.FILLED, s(R.string.home_continue, lm == null ? last : lm.number), IconRenderer.PLAY,
                    () -> a.openCase(last)), y, bh);
        }
        y = button(new CanvasButton(canContinue ? CanvasButton.TONAL : CanvasButton.FILLED, s(R.string.home_new),
                IconRenderer.SEARCH, this::newCase), y, bh);
        y = progress(y + t.dp(12), solved, cases.size(), stars) + t.dp(12);
        y = button(new CanvasButton(CanvasButton.OUTLINED, s(R.string.home_collection), IconRenderer.FOLDER,
                () -> a.goCases(1)), y, bh);
        y = button(new CanvasButton(CanvasButton.OUTLINED, s(R.string.tour_title), IconRenderer.BULB,
                a::openTutorial), y, bh);
        y = button(new CanvasButton(CanvasButton.OUTLINED, s(R.string.home_settings), IconRenderer.SETTINGS,
                a::openSettings), y, bh);
        scroll.contentHeight = y + t.dp(16);
        chrome.add(nav);
    }

    private float masthead(float y) {
        float markSize = t.dp(52);
        float titleWidth = t.text(ThemeManager.DISPLAY, t.onSurface).measureText(WORDMARK);
        float sideBySideOffset = markSize + t.dp(14);
        boolean stacked = cw - sideBySideOffset < titleWidth + t.dp(1);
        float titleX = stacked ? cl : cl + sideBySideOffset;
        float width = stacked ? cw : cw - sideBySideOffset;
        int titleStyle = width < titleWidth + t.dp(1) ? ThemeManager.HEADLINE : ThemeManager.DISPLAY;

        CanvasText wordmark = new CanvasText(t, WORDMARK, titleStyle, t.onSurface, width);
        wordmark.desc = s(R.string.app_name);
        CanvasText subtitle = new CanvasText(t, "CASE FILES", ThemeManager.LABEL_M, t.primary, width);
        subtitle.desc = null;
        float textHeight = wordmark.height + t.dp(2) + subtitle.height;
        float height = stacked ? markSize + t.dp(12) + textHeight : Math.max(markSize, textHeight);
        float titleY = stacked ? y + markSize + t.dp(12) : y + (height - textHeight) / 2f;
        float markY = stacked ? y : y + (height - markSize) / 2f;

        CaseMark mark = new CaseMark(a.getDrawable(R.drawable.ic_case_mark), t.onPrimary);
        mark.at(cl, markY, markSize, markSize);
        scroll.add(mark);
        wordmark.place(titleX, titleY);
        scroll.add(wordmark);
        subtitle.place(titleX, wordmark.b.bottom + t.dp(2));
        scroll.add(subtitle);
        return y + height + t.dp(16);
    }

    private float progress(float y, int solved, int total, int stars) {
        float inset = t.dp(14), width = cw - 2f * inset, x = cl + inset;
        float fraction = total == 0 ? 0f : solved / (float) total;
        String percentage = Math.round(100 * fraction) + "%";
        float percentWidth = (float) Math.ceil(t.text(ThemeManager.MONO, t.primary).measureText(percentage)) + t.dp(2);
        CanvasText percent = new CanvasText(t, percentage, ThemeManager.MONO, t.primary, percentWidth);
        CanvasText count = new CanvasText(t, s(R.string.home_progress, solved, total), ThemeManager.TITLE_M, t.onSurface,
                width - percentWidth - t.dp(10));
        count.place(x, y + inset);
        percent.place(x + width - percentWidth, y + inset);
        CanvasProgressBar bar = new CanvasProgressBar(fraction, false);
        bar.at(x, Math.max(count.b.bottom, percent.b.bottom) + t.dp(8), width, t.dp(6));
        CanvasText starCount = new CanvasText(t, s(R.string.home_stars, stars, total * 3), ThemeManager.BODY_M,
                t.onSurfaceVariant, width);
        float bottom = starCount.place(x, bar.b.bottom + t.dp(10)) + inset;
        CanvasCard card = new CanvasCard(CanvasCard.OUTLINED);
        card.color = t.surfaceLow;
        card.at(cl, y, cw, bottom - y);
        scroll.add(card);
        scroll.add(count);
        scroll.add(percent);
        scroll.add(bar);
        scroll.add(starCount);
        return bottom;
    }

    private static final class CaseMark extends CanvasComponent {
        private final Drawable glyph;

        CaseMark(Drawable glyph, int color) {
            this.glyph = glyph.mutate();
            this.glyph.setTint(color);
        }

        @Override
        public void draw(Canvas c, ThemeManager t) {
            t.fill.setColor(t.primary);
            c.drawRoundRect(b, t.dp(8), t.dp(8), t.fill);
            int extent = Math.round(b.width() * 0.72f);
            c.save();
            c.translate(b.centerX(), b.centerY());
            glyph.setBounds(-extent, -extent, extent, extent);
            glyph.draw(c);
            c.restore();
        }
    }

    private float button(CanvasButton b, float y, float bh) {
        b.at(cl, y, cw, bh);
        scroll.add(b);
        return y + bh;
    }

    private void newCase() {
        int id = 0;
        for (CaseModel m : a.cases()) {
            if (a.progress.status(m.id) == ProgressManager.NEW) {
                id = m.id;
                break;
            }
        }
        if (id == 0) id = a.nextCase(0);
        if (id == 0) view.snack(s(R.string.home_all_done));
        else a.openCase(id);
    }
}
