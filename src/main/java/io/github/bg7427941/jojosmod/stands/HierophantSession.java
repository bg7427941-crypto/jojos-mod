package io.github.bg7427941.jojosmod.stands;

import java.util.Optional;
import java.util.UUID;

/** Server-only cast identity also invalidates the bounded projectiles on an interruption. */
public record HierophantSession(Optional<UUID> volley, long startedAt, int pulses, boolean casting, long readyAt) {
    public static final HierophantSession EMPTY = new HierophantSession(Optional.empty(), 0, 0, false, 0);

    public HierophantSession finish() { return new HierophantSession(volley, startedAt, pulses, false, readyAt); }
    public HierophantSession cancel() { return new HierophantSession(Optional.empty(), 0, 0, false, readyAt); }
}
