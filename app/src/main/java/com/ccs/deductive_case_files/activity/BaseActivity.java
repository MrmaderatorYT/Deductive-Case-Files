package com.ccs.deductive_case_files.activity;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.window.OnBackInvokedDispatcher;

import com.ccs.deductive_case_files.data.CaseRepository;
import com.ccs.deductive_case_files.data.ProgressManager;
import com.ccs.deductive_case_files.data.SettingsManager;
import com.ccs.deductive_case_files.game.CaseManager;
import com.ccs.deductive_case_files.game.CaseModel;
import com.ccs.deductive_case_files.graphics.AnimationManager;
import com.ccs.deductive_case_files.localization.LocaleManager;
import com.ccs.deductive_case_files.ui.GameCanvasView;
import com.ccs.deductive_case_files.ui.SoundManager;
import com.ccs.deductive_case_files.ui.ThemeManager;

import java.util.List;

/**
 * Common base of all activities: applies theme and language, draws edge-to-edge, owns the
 * Canvas view, routes the back gesture and re-applies settings when they change.
 */
public abstract class BaseActivity extends Activity {
    public GameCanvasView canvas;
    public SettingsManager settings;
    public ProgressManager progress;
    public CaseManager manager;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private String stamp = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = new SettingsManager(this);
        progress = new ProgressManager(this);
        manager = new CaseManager(handler::post);
        applySettings();
        canvas = new GameCanvasView(this);
        setContentView(canvas);
        edgeToEdge();
        if (Build.VERSION.SDK_INT >= 33) {
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                    OnBackInvokedDispatcher.PRIORITY_DEFAULT, this::handleBack);
        }
    }

    /** Reads settings and pushes them into the theme, locale, animation and sound managers. */
    public final void applySettings() {
        stamp = settings.stamp();
        Configuration cfg = getResources().getConfiguration();
        LocaleManager.apply(this, settings.language());
        ThemeManager theme = ThemeManager.get();
        theme.init(getResources().getDisplayMetrics(), cfg.fontScale);
        int mode = settings.theme();
        boolean systemDark = (cfg.uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        boolean dark = mode == SettingsManager.THEME_DARK || (mode == SettingsManager.THEME_SYSTEM && systemDark);
        theme.apply(dark);
        AnimationManager.setEnabled(settings.animations());
        SoundManager.get().setEnabled(settings.sounds());
        Window w = getWindow();
        w.setBackgroundDrawable(new ColorDrawable(theme.surface));
        systemBars(dark);
    }

    @SuppressWarnings("deprecation")
    private void edgeToEdge() {
        Window w = getWindow();
        if (Build.VERSION.SDK_INT >= 30) {
            w.setDecorFitsSystemWindows(false);
        } else {
            w.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }
        if (Build.VERSION.SDK_INT >= 29) {
            w.setNavigationBarContrastEnforced(false);
            w.setStatusBarContrastEnforced(false);
        }
        systemBars(ThemeManager.get().dark);
    }

    /** Dark icons on light surfaces and vice versa. */
    @SuppressWarnings("deprecation")
    private void systemBars(boolean dark) {
        Window w = getWindow();
        if (Build.VERSION.SDK_INT >= 30) {
            WindowInsetsController c = w.getInsetsController();
            if (c == null) return;
            int mask = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                    | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;
            c.setSystemBarsAppearance(dark ? 0 : mask, mask);
        } else {
            View d = w.getDecorView();
            int f = d.getSystemUiVisibility();
            int light = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            d.setSystemUiVisibility(dark ? f & ~light : f | light);
        }
    }

    /** Cases with texts of the active language. */
    public List<CaseModel> cases() {
        return CaseRepository.cases(this, LocaleManager.lang());
    }

    /** Re-applies everything after a setting or configuration change, without losing state. */
    public void refresh() {
        applySettings();
        cases();
        if (canvas != null) canvas.relayout();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!stamp.equals(settings.stamp())) refresh();
        else if (canvas != null) canvas.relayout();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        refresh();
    }

    protected void handleBack() {
        if (!canvas.onBack()) finish();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBackPressed() {
        handleBack();
    }
}
