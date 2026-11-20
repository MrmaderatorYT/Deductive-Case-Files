package com.ccs.deductive_case_files.activity;

import android.os.Bundle;

import com.ccs.deductive_case_files.screens.SettingsScreen;

/** Settings: language, theme, animations, sounds and progress reset. */
public final class SettingsActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        canvas.setScreen(new SettingsScreen(this), 0);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }
}
