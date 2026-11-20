package com.ccs.deductive_case_files.game;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Runs solver work off the UI thread and delivers results back through the supplied
 * main-thread executor.
 */
public final class CaseManager {
    public interface Callback<T> {
        void onResult(T value);
    }

    private static final ExecutorService WORKER = Executors.newSingleThreadExecutor();
    private final Executor main;

    public CaseManager(Executor main) {
        this.main = main;
    }

    /** Derives the unique solution of the case (cached on the model). */
    public void solve(final CaseModel m, final Runnable done) {
        if (m.solution != null) {
            done.run();
            return;
        }
        WORKER.execute(() -> {
            if (m.solution == null) m.solution = PuzzleSolver.solve(m);
            main.execute(done);
        });
    }

    /** Finds the next logical step for a snapshot of the player's grid. */
    public void hint(final CaseModel m, final DeductionGrid snapshot, final boolean useLocked,
                     final Callback<HintSystem.Step> cb) {
        WORKER.execute(() -> {
            final HintSystem.Step s = new HintSystem(m).next(snapshot, true, useLocked);
            main.execute(() -> cb.onResult(s));
        });
    }

    /** Builds the full deduction chain shown on the result screen. */
    public void explain(final CaseModel m, final Callback<List<HintSystem.Step>> cb) {
        WORKER.execute(() -> {
            final List<HintSystem.Step> steps = new HintSystem(m).explain();
            main.execute(() -> cb.onResult(steps));
        });
    }

    public static int stars(int hints, int mistakes) {
        int penalty = hints + mistakes;
        return penalty == 0 ? 3 : penalty <= 3 ? 2 : 1;
    }
}
