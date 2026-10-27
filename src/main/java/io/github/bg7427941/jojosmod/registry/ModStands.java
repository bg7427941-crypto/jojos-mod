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

    public static final StandDefinition HIEROPHANT_GREEN = new StandDefinition(
            Identifier.fromNamespaceAndPath(JojosMod.MOD_ID, "hierophant_green"),
            "stand.jojosmod.hierophant_green", StandDefinition.Tier.STRONG,
            new StandStats(3, 4, 5, 4, 3, 2), new StandCombat(1.0F, 3.0, 100, 4.0));

    public static final StandDefinition SILVER_CHARIOT = new StandDefinition(
            Identifier.fromNamespaceAndPath(JojosMod.MOD_ID, "silver_chariot"), "stand.jojosmod.silver_chariot", StandDefinition.Tier.STRONG,
            new StandStats(3, 5, 3, 4, 4, 3), new StandCombat(4.0F, 3.5, 14, 4.0));

    public static final StandDefinition MAGICIANS_RED = new StandDefinition(
            Identifier.fromNamespaceAndPath(JojosMod.MOD_ID, "magicians_red"), "stand.jojosmod.magicians_red", StandDefinition.Tier.STRONG,
            new StandStats(4, 4, 3, 4, 3, 2), new StandCombat(6.0F, 3, 50, 4.0));

    public static final StandDefinition HERMIT_PURPLE = new StandDefinition(
            Identifier.fromNamespaceAndPath(JojosMod.MOD_ID, "hermit_purple"), "stand.jojosmod.hermit_purple", StandDefinition.Tier.STRONG,
            new StandStats(2, 3, 2, 5, 2, 1), new StandCombat(3.0F, 3, 24, 4.0));

    public static final StandDefinition THE_FOOL = new StandDefinition(
            Identifier.fromNamespaceAndPath(JojosMod.MOD_ID, "the_fool"), "stand.jojosmod.the_fool", StandDefinition.Tier.STRONG,
            new StandStats(4, 4, 2, 3, 1, 3), new StandCombat(3.0F, 3, 50, 4.0));

    public static final StandDefinition THE_WORLD = new StandDefinition(
            Identifier.fromNamespaceAndPath(JojosMod.MOD_ID, "the_world"), "stand.jojosmod.the_world", StandDefinition.Tier.STRONG,
            new StandStats(5, 5, 3, 5, 4, 4), new StandCombat(6.0F, 3, 20, 4.0));

    /** Equal Crusader rolls; The World remains outside the arrow pool.
     * Equal Part 3 rolls; existing assignments and the original arrow retain their behavior. */
    public static final List<StandDefinition> STARDUST_POOL = List.of(STAR_PLATINUM, HIEROPHANT_GREEN, SILVER_CHARIOT, MAGICIANS_RED, HERMIT_PURPLE, THE_FOOL);

    private static final Map<Identifier, StandDefinition> DEFINITIONS =
            Map.of(TRAINING_STAND.id(), TRAINING_STAND,
                    ECHOES_ACT_1.id(), ECHOES_ACT_1, LITTLE_FEET.id(), LITTLE_FEET,
                    STAR_PLATINUM.id(), STAR_PLATINUM, HIEROPHANT_GREEN.id(), HIEROPHANT_GREEN,
                    SILVER_CHARIOT.id(), SILVER_CHARIOT,
                    MAGICIANS_RED.id(), MAGICIANS_RED,
                    HERMIT_PURPLE.id(), HERMIT_PURPLE,
                    THE_FOOL.id(), THE_FOOL,
                    THE_WORLD.id(), THE_WORLD);

    private ModStands() {}

    public static Optional<StandDefinition> find(Identifier id) {
        return Optional.ofNullable(DEFINITIONS.get(id));
    }
}
