package io.github.bg7427941.jojosmod.abilities;

import io.github.bg7427941.jojosmod.entities.TrainingStandEntity;
import io.github.bg7427941.jojosmod.stands.StandDefinition;
import io.github.bg7427941.jojosmod.registry.ModDamageTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.EntityHitResult;

public final class BasicAttack {
    private BasicAttack() {}

    /** The server chooses the target; block collisions cap the ray before entity selection. */
    public static void perform(ServerPlayer owner, TrainingStandEntity stand, StandDefinition definition) {
        stand.playAttackAnimation();
        hit(owner, stand, definition.combat().damage(), definition.combat().reach(), false);
    }

    /** Shared server-side ray and PvP validation for each real strike, including every barrage pulse. */
    public static boolean hit(ServerPlayer owner, TrainingStandEntity stand, float damage, double reach, boolean barrage) {
        var hit = ProjectileUtil.getHitResultOnViewVector(owner,
                entity -> entity instanceof LivingEntity target && target.isAlive()
                        && target != owner && target.isPickable() && !target.isSpectator(),
                reach);

        if (!(hit instanceof EntityHitResult entityHit)
                || !(entityHit.getEntity() instanceof LivingEntity target)
                || !owner.canAttack(target) || !owner.hasLineOfSight(target)) {
            return false;
        }
        if (target instanceof ServerPlayer otherPlayer
                && (!owner.level().isPvpAllowed() || !owner.canHarmPlayer(otherPlayer))) {
            return false;
        }
        boolean hurt = target.hurtServer(owner.level(), barrage ? ModDamageTypes.barrage(owner, stand)
                : owner.damageSources().playerAttack(owner), damage);
        if (hurt && stand.isStarPlatinum()) {
            owner.level().playSound(null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_STRONG,
                    SoundSource.PLAYERS, barrage ? 0.45F : 0.8F, barrage ? 1.35F : 0.9F);
            owner.level().sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + target.getBbHeight() * 0.6,
                    target.getZ(), barrage ? 3 : 8, 0.15, 0.2, 0.15, 0.05);
        }
        return hurt;
    }
}
