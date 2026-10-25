package io.github.bg7427941.jojosmod.registry;

import io.github.bg7427941.jojosmod.JojosMod;
import io.github.bg7427941.jojosmod.stands.StandDefinition;
import io.github.bg7427941.jojosmod.stands.StandStats;
import io.github.bg7427941.jojosmod.stands.StandCombat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.Identifier;

public final class ModStands {
    public static final StandDefinition TRAINING_STAND = new StandDefinition(
            Identifier.fromNamespaceAndPath(JojosMod.MOD_ID, "training_stand"),
            "entity.jojosmod.training_stand", StandDefinition.Tier.BASIC,
            new StandStats(2, 2, 2, 2, 2, 2));

    // Gameplay ratings for the prototype, rather than a literal conversion of the manga's stats.
    public static final StandDefinition ECHOES_ACT_1 = new StandDefinition(
            Identifier.fromNamespaceAndPath(JojosMod.MOD_ID, "echoes_act_1"),
            "stand.jojosmod.echoes_act_1", StandDefinition.Tier.BASIC,
            new StandStats(1, 1, 2, 1, 2, 3));
    public static final StandDefinition LITTLE_FEET = new StandDefinition(
            Identifier.fromNamespaceAndPath(JojosMod.MOD_ID, "little_feet"),
            "stand.jojosmod.little_feet", StandDefinition.Tier.BASIC,
            new StandStats(1, 2, 1, 2, 2, 2));

    /** Equal chances; the training Stand and future strong Stands are deliberately excluded. */
    public static final List<StandDefinition> ARROW_POOL = List.of(ECHOES_ACT_1, LITTLE_FEET);

    public static final StandDefinition STAR_PLATINUM = new StandDefinition(
            Identifier.fromNamespaceAndPath(JojosMod.MOD_ID, "star_platinum"),
            "stand.jojosmod.star_platinum", StandDefinition.Tier.STRONG,
            new StandStats(5, 5, 3, 5, 5, 5), new StandCombat(6.0F, 3.0, 16, 4.0));

    /** Dedicated Part 3 pool; the original arrow and existing assignments retain their behavior. */
    public static final List<StandDefinition> STARDUST_POOL = List.of(STAR_PLATINUM);

    private static final Map<Identifier, StandDefinition> DEFINITIONS =
            Map.of(TRAINING_STAND.id(), TRAINING_STAND,
                    ECHOES_ACT_1.id(), ECHOES_ACT_1, LITTLE_FEET.id(), LITTLE_FEET,
                    STAR_PLATINUM.id(), STAR_PLATINUM);

    private ModStands() {}

    public static Optional<StandDefinition> find(Identifier id) {
        return Optional.ofNullable(DEFINITIONS.get(id));
    }
}
