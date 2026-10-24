package io.github.bg7427941.jojosmod.stands;

/** Explicit gameplay values, independent of the reference ratings printed for a Stand. */
public record StandCombat(float damage, double reach, int cooldownTicks, double tetherDistance) {
    public StandCombat {
        if (!Float.isFinite(damage) || damage <= 0 || !Double.isFinite(reach) || reach <= 0
                || cooldownTicks < 1 || !Double.isFinite(tetherDistance) || tetherDistance < reach) {
            throw new IllegalArgumentException("Invalid Stand combat profile");
        }
    }

    public static StandCombat legacy(StandStats stats) {
        return new StandCombat(stats.attackDamage(), stats.attackReach(), stats.attackCooldownTicks(), stats.maxOwnerDistance());
    }
}
