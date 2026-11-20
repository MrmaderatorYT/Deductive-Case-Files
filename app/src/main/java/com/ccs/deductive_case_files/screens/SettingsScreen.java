package com.ccs.deductive_case_files.screens;

import com.ccs.deductive_case_files.R;
import com.ccs.deductive_case_files.activity.BaseActivity;
import com.ccs.deductive_case_files.data.SettingsManager;
import com.ccs.deductive_case_files.graphics.IconRenderer;
import com.ccs.deductive_case_files.localization.LocaleManager;
import com.ccs.deductive_case_files.ui.CanvasButton;
import com.ccs.deductive_case_files.ui.CanvasDialog;
import com.ccs.deductive_case_files.ui.CanvasIcon;
import com.ccs.deductive_case_files.ui.CanvasSegmented;
import com.ccs.deductive_case_files.ui.CanvasSwitch;
import com.ccs.deductive_case_files.ui.CanvasText;
import com.ccs.deductive_case_files.ui.Screen;
import com.ccs.deductive_case_files.ui.ThemeManager;

/** Settings: language, theme, animations, sounds, reset. Changes apply instantly. */
public final class SettingsScreen extends Screen {
    private final BaseActivity a;

    public SettingsScreen(BaseActivity a) {
        super(a.canvas);
        this.a = a;
    }

    @Override
    protected void build() {
        float top = topBar(s(R.string.settings_title), a::finish);
        body(top, h);
        float y = top + t.dp(8);
        SettingsManager st = a.settings;

        y = header(IconRenderer.GLOBE, s(R.string.set_language), y);
        CanvasSegmented lang = new CanvasSegmented(new String[]{"Українська", "English"},
                LocaleManager.EN.equals(st.language()) ? 1 : 0, i -> {
            st.setLanguage(i == 1 ? LocaleManager.EN : LocaleManager.UK);
            a.refresh();
        });
        lang.at(cl, y, cw, t.dp(48));
        scroll.add(lang);
        y += t.dp(48) + t.dp(20);

        y = header(IconRenderer.PALETTE, s(R.string.set_theme), y);
        CanvasSegmented theme = new CanvasSegmented(new String[]{s(R.string.theme_system), s(R.string.theme_light),
                s(R.string.theme_dark)}, st.theme(), i -> {
            st.setTheme(i);
            a.refresh();
        });
        theme.at(cl, y, cw, t.dp(48));
        scroll.add(theme);
        y += t.dp(48) + t.dp(20);

        y = toggle(IconRenderer.MOTION, s(R.string.set_animations), s(R.string.set_animations_sub), st.animations(), on -> {
            st.setAnimations(on);
            a.applySettings();
        }, y);
        y = toggle(IconRenderer.SOUND, s(R.string.set_sounds), s(R.string.set_sounds_sub), st.sounds(), on -> {
            st.setSounds(on);
            a.applySettings();
        }, y);
        y += t.dp(12);

        y = header(IconRenderer.DELETE, s(R.string.set_reset), y);
        y = text(s(R.string.set_reset_sub), ThemeManager.BODY_M, t.onSurfaceVariant, cl, y - t.dp(4), cw) + t.dp(12);
        CanvasButton reset = new CanvasButton(CanvasButton.DANGER, s(R.string.set_reset), IconRenderer.DELETE, this::confirmReset);
        reset.at(cl, y, Math.min(cw, reset.preferredWidth(t)), t.dp(48));
        scroll.add(reset);
        y += t.dp(48) + t.dp(28);
        y = text(s(R.string.about), ThemeManager.BODY_S, t.onSurfaceVariant, cl, y, cw);
        scroll.contentHeight = y + t.dp(24) + insetBottom - top;
    }

    private float header(int icon, String title, float y) {
        CanvasIcon ic = new CanvasIcon(icon, t.primary, t.dp(22));
        ic.at(cl, y + t.dp(6), t.dp(24), t.dp(24));
        scroll.add(ic);
        CanvasText ct = new CanvasText(t, title, ThemeManager.TITLE_M, t.onSurface, cw - t.dp(40));
        ct.place(cl + t.dp(40), y + (t.dp(36) - ct.height) / 2f);
        scroll.add(ct);
        return y + Math.max(t.dp(36), ct.height) + t.dp(10);
    }

    private float toggle(int icon, String title, String sub, boolean on, CanvasSwitch.Listener l, float y) {
        CanvasText tt = new CanvasText(t, title, ThemeManager.TITLE_M, t.onSurface, cw - t.dp(112));
        CanvasText ts = new CanvasText(t, sub, ThemeManager.BODY_M, t.onSurfaceVariant, cw - t.dp(112));
        float rh = Math.max(t.dp(72), tt.height + ts.height + t.dp(24));
        CanvasIcon ic = new CanvasIcon(icon, t.primary, t.dp(22));
        ic.at(cl, y + (rh - t.dp(24)) / 2f, t.dp(24), t.dp(24));
        scroll.add(ic);
        float ty = y + (rh - tt.height - ts.height) / 2f;
        tt.desc = null;
        ts.desc = null;
        tt.place(cl + t.dp(40), ty);
        ts.place(cl + t.dp(40), ty + tt.height);
        scroll.add(tt);
        scroll.add(ts);
        CanvasSwitch sw = new CanvasSwitch(view, on, title + ". " + sub, l);
        sw.at(cl, y, cw, rh);
        scroll.add(sw);
        return y + rh;
    }

    private void confirmReset() {
        CanvasDialog d = new CanvasDialog(view);
        d.icon = IconRenderer.DELETE;
        d.title = s(R.string.reset_title);
        d.message = s(R.string.reset_body);
        d.positive = s(R.string.set_reset);
        d.negative = s(R.string.cancel);
        d.onPositive = () -> {
            a.progress.resetAll();
            view.snack(s(R.string.reset_done));
        };
        view.showDialog(d);
    }
}
