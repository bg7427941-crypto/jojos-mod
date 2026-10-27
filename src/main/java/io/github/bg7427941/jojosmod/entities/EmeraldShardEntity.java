package io.github.bg7427941.jojosmod.entities;

import io.github.bg7427941.jojosmod.abilities.HierophantSplash;
import io.github.bg7427941.jojosmod.effects.HierophantEffects;
import io.github.bg7427941.jojosmod.registry.ModDamageTypes;
import io.github.bg7427941.jojosmod.registry.ModSounds;
import io.github.bg7427941.jojosmod.stands.HierophantProgress;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Rendered as an emerald, but never an inventory item, pickup or persistent world entity. */
public final class EmeraldShardEntity extends ThrowableItemProjectile {
    public static final double MAX_RANGE = 14;
    public static final float SPEED = 1.2F;
    public static final float DAMAGE = 3;
    public static final int MAX_AGE = 25;
    private UUID volley;
    private double travelled;

    public EmeraldShardEntity(EntityType<? extends EmeraldShardEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    public void initialize(ServerPlayer owner, UUID volley, Vec3 origin) {
        setOwner(owner);
        this.volley = volley;
        setPos(origin);
    }

    @Override protected Item getDefaultItem() { return Items.EMERALD; }

    private ServerPlayer validOwner() {
        return getOwner() instanceof ServerPlayer player && player.level() == level() && volley != null
                && HierophantSplash.projectileValid(player, volley) ? player : null;
    }

    @Override public void tick() {
        if (!level().isClientSide() && validOwner() == null || tickCount >= MAX_AGE || travelled >= MAX_RANGE) {
            discard(); return;
        }
        var delta = getDeltaMovement();
        double remaining = MAX_RANGE - travelled;
        if (delta.lengthSqr() > remaining * remaining) setDeltaMovement(delta.normalize().scale(remaining));
        var before = position();
        super.tick();
        travelled += before.distanceTo(position());
        if (travelled >= MAX_RANGE - .00001) discard();
        if (!isRemoved() && tickCount % 3 == 0 && level() instanceof ServerLevel server) {
            HierophantEffects.trail(server, position());
        }
    }

    @Override protected boolean canHitEntity(Entity entity) {
        if (!super.canHitEntity(entity) || !(entity instanceof LivingEntity target)) return false;
        if (!(level() instanceof ServerLevel)) return true;
        var owner = validOwner();
        return owner != null && canDamage(owner, target);
    }

    public static boolean canDamage(ServerPlayer owner, LivingEntity target) {
        return target != owner && target.isAlive() && !target.isSpectator() && !owner.isAlliedTo(target)
                && owner.canAttack(target) && (!(target instanceof ServerPlayer player)
                    || owner.level().isPvpAllowed() && owner.canHarmPlayer(player));
    }

    @Override protected void onHitEntity(EntityHitResult hit) {
        if (!(level() instanceof ServerLevel server) || !(hit.getEntity() instanceof LivingEntity target)) return;
        var owner = validOwner();
        if (owner == null || !canDamage(owner, target)) return;
        float before = target.getHealth();
        if (target.hurtServer(server, ModDamageTypes.emerald(owner, this), DAMAGE) && target.getHealth() < before) {
            HierophantProgress.onSplashHit(owner, target);
        }
    }

    @Override protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (level() instanceof ServerLevel server) {
            HierophantEffects.impact(server, hit.getLocation());
            server.playSound(null, blockPosition(), ModSounds.HIEROPHANT_IMPACT.get(), SoundSource.PLAYERS, .22F, 1);
            discard();
        }
    }
}
