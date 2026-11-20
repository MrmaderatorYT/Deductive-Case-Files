package com.ccs.deductive_case_files.data;

import android.content.Context;
import android.content.SharedPreferences;

/** Per-case progress stored in SharedPreferences; survives process death. */
public final class ProgressManager {
    public static final int NEW = 0, STARTED = 1, SOLVED = 2;
    private final SharedPreferences p;

    public ProgressManager(Context c) {
        p = c.getApplicationContext().getSharedPreferences("progress", Context.MODE_PRIVATE);
    }

    public int status(int id) {
        return p.getInt("s" + id, NEW);
    }

    public String grid(int id) {
        return p.getString("g" + id, null);
    }

    public int hints(int id) {
        return p.getInt("h" + id, 0);
    }

    public int mistakes(int id) {
        return p.getInt("m" + id, 0);
    }

    public boolean taskSolved(int id) {
        return p.getBoolean("t" + id, false);
    }

    public int stars(int id) {
        return p.getInt("r" + id, 0);
    }

    /** Bit mask of clues the player crossed off as used. */
    public int struck(int id) {
        return p.getInt("x" + id, 0);
    }

    public int lastCase() {
        return p.getInt("last", 0);
    }

    public void setLastCase(int id) {
        p.edit().putInt("last", id).apply();
    }

    public void saveGrid(int id, String grid) {
        SharedPreferences.Editor e = p.edit().putString("g" + id, grid);
        if (status(id) == NEW) e.putInt("s" + id, STARTED);
        e.apply();
    }

    public void addHint(int id) {
        p.edit().putInt("h" + id, hints(id) + 1).apply();
    }

    public void addMistake(int id) {
        p.edit().putInt("m" + id, mistakes(id) + 1).apply();
    }

    public void setTaskSolved(int id) {
        SharedPreferences.Editor e = p.edit().putBoolean("t" + id, true);
        if (status(id) == NEW) e.putInt("s" + id, STARTED);
        e.apply();
    }

    public void setStruck(int id, int mask) {
        p.edit().putInt("x" + id, mask).apply();
    }

    public void setSolved(int id, int stars) {
        p.edit().putInt("s" + id, SOLVED).putInt("r" + id, Math.max(stars, stars(id))).apply();
    }

    /** Clears one case so it can be replayed (the best star rating is kept). */
    public void resetCase(int id) {
        p.edit().remove("s" + id).remove("g" + id).remove("h" + id).remove("m" + id)
                .remove("t" + id).remove("x" + id).apply();
    }

    public void resetAll() {
        p.edit().clear().apply();
    }
}
