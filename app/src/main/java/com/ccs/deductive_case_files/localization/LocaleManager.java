package com.ccs.deductive_case_files.localization;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;

/**
 * In-app language switching without restarting: strings are read from a Resources object
 * configured for the chosen locale. Ukrainian is the fallback for unsupported system languages.
 */
public final class LocaleManager {
    public static final String UK = "uk", EN = "en";
    private static Resources res;
    private static String lang = UK;

    private LocaleManager() {
    }

    /** Language used on first launch: the system one if supported, otherwise Ukrainian. */
    public static String systemDefault() {
        String l = Locale.getDefault().getLanguage();
        return EN.equals(l) ? EN : UK;
    }

    /** (Re)creates localized resources; call after a language or configuration change. */
    public static void apply(Context context, String language) {
        lang = EN.equals(language) ? EN : UK;
        Configuration c = new Configuration(context.getResources().getConfiguration());
        c.setLocale(new Locale(lang));
        res = context.createConfigurationContext(c).getResources();
    }

    public static String lang() {
        return lang;
    }

    public static String s(int id, Object... args) {
        return args.length == 0 ? res.getString(id) : res.getString(id, args);
    }
}
