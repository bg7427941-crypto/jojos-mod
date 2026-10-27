package io.github.bg7427941.jojosmod.effects;

import io.github.bg7427941.jojosmod.registry.ModSounds;
import io.github.bg7427941.jojosmod.stands.PartThreeStand;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

public final class CrusaderEffects {
    private CrusaderEffects() {}
    public static void burst(ServerLevel level, PartThreeStand kind, Vec3 at, int count) {
        level.sendParticles(new DustParticleOptions(kind.color() & 0xFFFFFF, .7F), at.x, at.y, at.z, count, .25, .25, .25, 0);
    }
    public static void line(ServerLevel level, PartThreeStand kind, Vec3 from, Vec3 to) {
        int points = Math.min(28, Math.max(2, (int)(from.distanceTo(to) * 2)));
        for (int i = 0; i <= points; i++) {
            var p = from.lerp(to, i / (double)points);
            level.sendParticles(new DustParticleOptions(kind.color() & 0xFFFFFF, .55F), p.x, p.y, p.z, 1, .01, .01, .01, 0);
        }
    }
    public static void sound(ServerLevel level, PartThreeStand kind, String cue, Vec3 at) {
        level.playSound(null, at.x, at.y, at.z, ModSounds.crusader(kind, cue), SoundSource.PLAYERS, .55F, 1);
    }
    public static void crossfire(ServerLevel level, Vec3 at) {
        // Ankh-shaped fire outline around the energy core, rather than persistent block fire.
        for (int i = 0; i < 8; i++) {
            double angle = Math.PI * 2 * i / 8;
            var p = at.add(Math.cos(angle) * .25, .25 + Math.sin(angle) * .25, 0);
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.FLAME, p.x, p.y, p.z, 1, 0, 0, 0, 0);
        }
        line(level, PartThreeStand.MAGICIANS_RED, at.add(0, .05, 0), at.add(0, -.5, 0));
        line(level, PartThreeStand.MAGICIANS_RED, at.add(-.25, -.12, 0), at.add(.25, -.12, 0));
    }
}
