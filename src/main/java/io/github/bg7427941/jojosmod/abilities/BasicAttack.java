package io.github.bg7427941.jojosmod.abilities;

import io.github.bg7427941.jojosmod.entities.TrainingStandEntity;
import io.github.bg7427941.jojosmod.stands.StandDefinition;
import io.github.bg7427941.jojosmod.registry.ModDamageTypes;
import io.github.bg7427941.jojosmod.registry.ModSounds;
import io.github.bg7427941.jojosmod.effects.StarPlatinumEffects;
import io.github.bg7427941.jojosmod.stands.StarPlatinumProgress;
import io.github.bg7427941.jojosmod.stands.StarPlatinumSession.Action;
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
        float healthBefore = target.getHealth();
        var action = StarPlatinumActions.session(owner).action();
        boolean hurt = target.hurtServer(owner.level(), barrage ? ModDamageTypes.barrage(owner, stand)
                : owner.damageSources().playerAttack(owner), damage);
        if (hurt && stand.isStarPlatinum()) {
            if (target.getHealth() < healthBefore) {
                if (action == Action.STRIKE) StarPlatinumProgress.onPreciseHit(owner, target);
                if (action == Action.BARRAGE) StarPlatinumProgress.onBarrageHit(owner, target);
                if (action == Action.STAR_FINGER) StarPlatinumProgress.onStarFingerHit(owner, target,
                        owner.getEyePosition().distanceToSqr(entityHit.getLocation()));
            }
            owner.level().playSound(null, target.blockPosition(), ModSounds.STAR_IMPACT.get(),
                    SoundSource.PLAYERS, barrage ? 0.38F : 0.7F, barrage ? 1.12F : 0.94F);
            StarPlatinumEffects.impact(owner, target, barrage);
        }
        return hurt;
    }
}
