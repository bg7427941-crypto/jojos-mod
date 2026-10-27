package io.github.bg7427941.jojosmod.effects;

import io.github.bg7427941.jojosmod.entities.TrainingStandEntity;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

/** Small server-broadcast cues; no damage or target selection occurs in presentation code. */
public final class HierophantEffects {
    private static final DustParticleOptions JADE = new DustParticleOptions(0x32CE88, 0.7F);
    private static final DustParticleOptions IVORY = new DustParticleOptions(0xE5F1C8, 0.6F);

    private HierophantEffects() {}

    public static void aura(TrainingStandEntity stand, long tick) {
        if (tick % 5 == 0 && stand.level() instanceof ServerLevel level) {
            dust(level, stand.position().add(Math.sin(tick * .1) * .4, 1.1, Math.cos(tick * .1) * .3), 1, .06);
        }
    }

    public static void manifestation(TrainingStandEntity stand) {
        if (stand.level() instanceof ServerLevel level) dust(level, stand.position().add(0, 1, 0), 10, .4);
    }

    public static void pulse(ServerLevel level, Vec3 origin) { dust(level, origin, 5, .13); }
    public static void impact(ServerLevel level, Vec3 point) { dust(level, point, 5, .1); }
    public static void trail(ServerLevel level, Vec3 point) { dust(level, point, 1, .02); }

    private static void dust(ServerLevel level, Vec3 point, int count, double spread) {
        level.sendParticles(JADE, point.x, point.y, point.z, count, spread, spread, spread, 0);
        if (count > 1) level.sendParticles(IVORY, point.x, point.y, point.z, 2, spread, spread, spread, 0);
    }
}
