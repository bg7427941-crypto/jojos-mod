package io.github.bg7427941.jojosmod.stands;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Server-only session state. Summoned entities and cooldowns are not saved or copied on death. */
public record StandSession(Optional<UUID> activeEntity, long nextToggleTick, long nextAttackTick) {
    public static final StandSession EMPTY = new StandSession(Optional.empty(), 0, 0);

    public StandSession {
        Objects.requireNonNull(activeEntity, "activeEntity");
    }
}
