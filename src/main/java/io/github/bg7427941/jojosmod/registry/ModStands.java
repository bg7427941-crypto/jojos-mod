package io.github.bg7427941.jojosmod.registry;

import io.github.bg7427941.jojosmod.JojosMod;
import io.github.bg7427941.jojosmod.stands.StandDefinition;
import io.github.bg7427941.jojosmod.stands.StandStats;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.Identifier;

public final class ModStands {
    public static final StandDefinition TRAINING_STAND = new StandDefinition(
            Identifier.fromNamespaceAndPath(JojosMod.MOD_ID, "training_stand"),
            "entity.jojosmod.training_stand", StandDefinition.Tier.BASIC,
            new StandStats(2, 2, 2, 2, 2, 2));

    private static final Map<Identifier, StandDefinition> DEFINITIONS =
            Map.of(TRAINING_STAND.id(), TRAINING_STAND);

    private ModStands() {}

    public static Optional<StandDefinition> find(Identifier id) {
        return Optional.ofNullable(DEFINITIONS.get(id));
    }
}
