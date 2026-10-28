package io.github.bg7427941.jojosmod.entities;

import io.github.bg7427941.jojosmod.abilities.CrusaderActions;
import io.github.bg7427941.jojosmod.effects.CrusaderEffects;
import io.github.bg7427941.jojosmod.stands.PartThreeStand;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/** Crossfire energy, not ammunition. It never creates fire blocks, pickups or a lingering burn. */
public final class CrusaderFlameEntity extends ThrowableItemProjectile {
    private UUID volley;
    private double travelled;
    public CrusaderFlameEntity(EntityType<? extends CrusaderFlameEntity> type, Level level) { super(type, level); setNoGravity(true); }
    @Override protected Item getDefaultItem() { return Items.FIRE_CHARGE; }
    public void initialize(ServerPlayer owner, UUID volley) {
        setOwner(owner); this.volley = volley; setPos(owner.getEyePosition());
        var look = owner.getLookAngle(); shoot(look.x, look.y, look.z, .9F, 0);
    }
    private ServerPlayer validOwner() {
        return getOwner() instanceof ServerPlayer p && p.level() == level() && volley != null
                && CrusaderActions.flameValid(p, volley) ? p : null;
    }
    @Override public void tick() {
        if ((!level().isClientSide() && validOwner() == null) || travelled >= 16 || tickCount >= 30) { discard(); return; }
        var delta = getDeltaMovement();
        if (delta.length() > 16 - travelled) setDeltaMovement(delta.normalize().scale(16 - travelled));
        var before = position(); super.tick(); travelled += position().distanceTo(before);
        if (travelled >= 15.99999) discard();
        if (!isRemoved() && tickCount % 2 == 0 && level() instanceof net.minecraft.server.level.ServerLevel l)
            CrusaderEffects.crossfire(l, position());
    }
    @Override protected boolean canHitEntity(Entity entity) {
        if (!super.canHitEntity(entity) || !(entity instanceof LivingEntity t)) return false;
        if (level().isClientSide()) return true;
        var p = validOwner(); return p != null && CrusaderActions.canDamage(p, t);
    }
    @Override protected void onHitEntity(EntityHitResult hit) {
        var p = validOwner();
        if (p != null && hit.getEntity() instanceof LivingEntity target) CrusaderActions.damage(p, target, io.github.bg7427941.jojosmod.registry.ModStands.MAGICIANS_RED.combat().damage(), true);
    }
    @Override protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (level() instanceof net.minecraft.server.level.ServerLevel l) {
            CrusaderEffects.burst(l, PartThreeStand.MAGICIANS_RED, hit.getLocation(), 10); discard();
        }
    }
}
