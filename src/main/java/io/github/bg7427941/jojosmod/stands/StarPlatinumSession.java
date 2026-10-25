package io.github.bg7427941.jojosmod.stands;

/** Temporary server state; the regular Stand session still owns the manifestation and strike cooldown. */
public record StarPlatinumSession(Action action, long startedAt, int pulses, long barrageReadyAt, long guardReadyAt,
        long fingerReadyAt) {
    public enum Action { NONE, STRIKE, BARRAGE, GUARD, STAR_FINGER }
    public static final StarPlatinumSession EMPTY = new StarPlatinumSession(Action.NONE, 0, 0, 0, 0, 0);

    public StarPlatinumSession(Action action, long startedAt, int pulses, long barrageReadyAt, long guardReadyAt) {
        this(action, startedAt, pulses, barrageReadyAt, guardReadyAt, 0);
    }

    public StarPlatinumSession finish() {
        return new StarPlatinumSession(Action.NONE, 0, 0, barrageReadyAt, guardReadyAt, fingerReadyAt);
    }
}
