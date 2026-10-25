package io.github.bg7427941.jojosmod.stands;

import java.util.Objects;
import net.minecraft.resources.Identifier;

/** Code-registered definition; data-pack loading can be added without changing player IDs. */
public record StandDefinition(Identifier id, String translationKey, Tier tier, StandStats stats, StandCombat combat) {
    public enum Tier { BASIC, STRONG }

    public StandDefinition(Identifier id, String translationKey, Tier tier, StandStats stats) {
        this(id, translationKey, tier, stats, StandCombat.legacy(stats));
    }

    public StandDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(translationKey, "translationKey");
        Objects.requireNonNull(tier, "tier");
        Objects.requireNonNull(stats, "stats");
        Objects.requireNonNull(combat, "combat");
    }
}
