package io.github.bg7427941.jojosmod.effects;

import io.github.bg7427941.jojosmod.entities.TrainingStandEntity;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** Small, shared visual cues. These methods never select targets or alter combat state. */
public final class StarPlatinumEffects {
    private static final DustParticleOptions VIOLET = new DustParticleOptions(0xA76BE8, 0.85F);
    private static final DustParticleOptions GOLD = new DustParticleOptions(0xF5D879, 0.75F);

    private StarPlatinumEffects() {}

    public static void summon(TrainingStandEntity stand) {
        var level = server(stand);
        if (level == null) return;
        // A short outline at the feet and shoulders leaves the centre of the owner's view clear.
        for (int i = 0; i < 6; i++) {
            double angle = i * Math.PI / 3;
            var edge = stand.position().add(Math.cos(angle) * 0.65, 0.25, Math.sin(angle) * 0.65);
            dust(level, VIOLET, edge, 1, 0.06, 0.16, 0.06);
            dust(level, GOLD, edge.add(0, 1.2, 0), 1, 0.04, 0.12, 0.04);
        }
    }

    public static void dismiss(TrainingStandEntity stand) {
        var level = server(stand);
        if (level == null) return;
        dust(level, VIOLET, stand.position().add(0, 1.0, 0), 8, 0.3, 0.5, 0.3);
        dust(level, GOLD, stand.position().add(0, 1.4, 0), 3, 0.24, 0.3, 0.24);
    }

    /** Call on the actual strike tick, including misses, rather than during the windup. */
    public static void swing(ServerPlayer owner, TrainingStandEntity stand, boolean barrage) {
        var level = server(stand);
        if (level == null || owner.level() != level) return;
        var start = stand.position().add(0, 1.25, 0);
        var side = horizontalSide(owner);
        var end = owner.getEyePosition().add(owner.getLookAngle().scale(2.2))
                .add(side.scale(barrage ? 0.18 : 0.3)).add(0, -0.16, 0);
        for (int i = 1; i <= 3; i++) {
            var point = start.lerp(end, i / 4.0);
            dust(level, i == 2 ? GOLD : VIOLET, point, 1, 0.025, 0.025, 0.025);
        }
    }

    public static void impact(ServerPlayer owner, LivingEntity target, boolean barrage) {
        if (!(target.level() instanceof ServerLevel level) || owner.level() != level) return;
        var point = target.position().add(0, target.getBbHeight() * 0.6, 0);
        dust(level, GOLD, point, barrage ? 3 : 7, 0.13, 0.18, 0.13);
        dust(level, VIOLET, point, barrage ? 2 : 4, 0.11, 0.16, 0.11);
        level.sendParticles(ParticleTypes.CRIT, point.x, point.y, point.z,
                barrage ? 1 : 3, 0.1, 0.15, 0.1, 0.04);
    }

    public static void guardStart(ServerPlayer owner, TrainingStandEntity stand) {
        if (server(stand) != owner.level()) return;
        guardArc(owner);
    }

    public static void guardBlock(ServerPlayer owner) {
        guardArc(owner);
        var point = owner.position().add(horizontalForward(owner).scale(0.85)).add(0, 1.1, 0);
        owner.level().sendParticles(ParticleTypes.CRIT, point.x, point.y, point.z,
                3, 0.14, 0.18, 0.14, 0.015);
    }

    /** Four small particles every ten ticks: eight particles per second at 20 TPS. */
    public static void aura(TrainingStandEntity stand, long gameTime) {
        var level = server(stand);
        if (level == null || stand.isDismissing() || Math.floorMod(gameTime + stand.getId(), 10) != 0) return;
        dust(level, VIOLET, stand.position().add(0, 0.75, 0), 2, 0.3, 0.35, 0.3);
        dust(level, GOLD, stand.position().add(0, 1.65, 0), 2, 0.28, 0.18, 0.28);
    }

    private static void guardArc(ServerPlayer owner) {
        var forward = horizontalForward(owner);
        var side = horizontalSide(owner);
        for (int i = -2; i <= 2; i++) {
            double angle = i * Math.PI / 6;
            var point = owner.position().add(forward.scale(Math.cos(angle) * 0.9))
                    .add(side.scale(Math.sin(angle) * 0.9)).add(0, 1.05, 0);
            dust(owner.level(), i % 2 == 0 ? GOLD : VIOLET, point, 1, 0.03, 0.12, 0.03);
        }
    }

    private static Vec3 horizontalForward(ServerPlayer owner) {
        double yaw = Math.toRadians(owner.getYRot());
        return new Vec3(-Math.sin(yaw), 0, Math.cos(yaw));
    }

    private static Vec3 horizontalSide(ServerPlayer owner) {
        double yaw = Math.toRadians(owner.getYRot());
        return new Vec3(Math.cos(yaw), 0, Math.sin(yaw));
    }

    private static @Nullable ServerLevel server(TrainingStandEntity stand) {
        return stand.isStarPlatinum() && stand.level() instanceof ServerLevel level ? level : null;
    }

    private static void dust(ServerLevel level, DustParticleOptions particle, Vec3 point,
            int count, double spreadX, double spreadY, double spreadZ) {
        // The native default respects client particle settings and the normal visibility distance.
        level.sendParticles(particle, point.x, point.y, point.z, count, spreadX, spreadY, spreadZ, 0);
    }
}
