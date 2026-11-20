package com.ccs.deductive_case_files.game;

/** A suspect (an item of the first category) with a short localized role. */
public final class SuspectModel {
    public final int index;
    public final String name;
    public final String role;

    public SuspectModel(int index, String name, String role) {
        this.index = index;
        this.name = name;
        this.role = role;
    }
}
