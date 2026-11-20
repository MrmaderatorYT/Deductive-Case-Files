package com.ccs.deductive_case_files.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.ccs.deductive_case_files.localization.LocaleManager;

/** Persistent user settings. */
public final class SettingsManager {
    public static final int THEME_SYSTEM = 0, THEME_LIGHT = 1, THEME_DARK = 2;
    private final SharedPreferences p;

    public SettingsManager(Context c) {
        p = c.getApplicationContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
    }

    public String language() {
        return p.getString("lang", LocaleManager.systemDefault());
    }

    public void setLanguage(String lang) {
        p.edit().putString("lang", lang).apply();
    }

    public int theme() {
        return p.getInt("theme", THEME_SYSTEM);
    }

    public void setTheme(int theme) {
        p.edit().putInt("theme", theme).apply();
    }

    public boolean animations() {
        return p.getBoolean("anim", true);
    }

    public void setAnimations(boolean on) {
        p.edit().putBoolean("anim", on).apply();
    }

    public boolean sounds() {
        return p.getBoolean("sound", true);
    }

    public void setSounds(boolean on) {
        p.edit().putBoolean("sound", on).apply();
    }

    /** Whether the first-launch invitation to the guided tour has been answered. */
    public boolean tourOffered() {
        return p.getBoolean("tour", false);
    }

    public void setTourOffered() {
        p.edit().putBoolean("tour", true).apply();
    }

    /** Changes whenever a setting that affects rendering changes. */
    public String stamp() {
        return language() + theme() + animations() + sounds();
    }
}
