package io.github.bg7427941.jojosmod.abilities;

import io.github.bg7427941.jojosmod.entities.TrainingStandEntity;
import io.github.bg7427941.jojosmod.stands.StandDefinition;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.EntityHitResult;

public final class BasicAttack {
    private BasicAttack() {}

    /** The server chooses the target; block collisions cap the ray before entity selection. */
    public static void perform(ServerPlayer owner, TrainingStandEntity stand, StandDefinition definition) {
        stand.playAttackAnimation();
        var hit = ProjectileUtil.getHitResultOnViewVector(owner,
                entity -> entity instanceof LivingEntity target && target.isAlive()
                        && target != owner && target.isPickable() && !target.isSpectator(),
                definition.stats().attackReach());

        if (!(hit instanceof EntityHitResult entityHit)
                || !(entityHit.getEntity() instanceof LivingEntity target)
                || !owner.canAttack(target) || !owner.hasLineOfSight(target)) {
            return;
        }
        if (target instanceof ServerPlayer otherPlayer
                && (!owner.level().isPvpAllowed() || !owner.canHarmPlayer(otherPlayer))) {
            return;
        }
        target.hurtServer(owner.level(), owner.damageSources().playerAttack(owner),
                definition.stats().attackDamage());
    }
}
