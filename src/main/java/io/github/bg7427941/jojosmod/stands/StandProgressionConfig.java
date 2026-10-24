package io.github.bg7427941.jojosmod.stands;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Provisional disc requirements; milestone rewards remain a separate design decision. */
public final class StandProgressionConfig {
    public static final ModConfigSpec SPEC;
    private static final ModConfigSpec.IntValue BASIC_REQUIREMENT;
    private static final ModConfigSpec.IntValue STRONG_REQUIREMENT;

    static {
        var builder = new ModConfigSpec.Builder();
        builder.push("willpower");
        BASIC_REQUIREMENT = builder.comment("Minimum willpower to equip a basic Stand disc.")
                .defineInRange("basicStandRequirement", 1, 0, WillpowerData.MAX_VALUE);
        STRONG_REQUIREMENT = builder.comment("Reserved for future strong Stand discs; no strong cores are obtainable yet.")
                .defineInRange("strongStandRequirement", 3, 0, WillpowerData.MAX_VALUE);
        builder.pop();
        SPEC = builder.build();
    }

    private StandProgressionConfig() {}

    public static int requiredWillpower(StandDefinition stand) {
        return stand.tier() == StandDefinition.Tier.STRONG ? STRONG_REQUIREMENT.get() : BASIC_REQUIREMENT.get();
    }
}
