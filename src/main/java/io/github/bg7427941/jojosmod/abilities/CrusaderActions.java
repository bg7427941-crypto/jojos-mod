package io.github.bg7427941.jojosmod.abilities;

import io.github.bg7427941.jojosmod.effects.CrusaderEffects;
import io.github.bg7427941.jojosmod.effects.StandVoices;
import io.github.bg7427941.jojosmod.network.StandVoicePayload.Cue;
import io.github.bg7427941.jojosmod.entities.CrusaderFlameEntity;
import io.github.bg7427941.jojosmod.entities.TrainingStandEntity;
import io.github.bg7427941.jojosmod.registry.ModAttachments;
import io.github.bg7427941.jojosmod.registry.ModDamageTypes;
import io.github.bg7427941.jojosmod.registry.ModEntities;
import io.github.bg7427941.jojosmod.stands.*;
import io.github.bg7427941.jojosmod.stands.CrusaderSession.Action;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Five distinct server-owned kits. All target selection and scheduled effects run on the server. */
public final class CrusaderActions {
    private CrusaderActions() {}
    public static CrusaderSession session(ServerPlayer owner) { return owner.getData(ModAttachments.CRUSADER_SESSION); }
    public static Optional<TrainingStandEntity> active(ServerPlayer owner) {
        if (!owner.isAlive() || owner.isRemoved() || owner.isSpectator() || TimeStop.isFrozen(owner)) return Optional.empty();
        return StandController.getActive(owner).filter(s -> PartThreeStand.of(s.getStandDefinition().id()).isPresent())
                .filter(s -> StandController.isActive(owner, s) && s.distanceToSqr(owner) <= 16);
    }
    public static boolean canDamage(ServerPlayer owner, LivingEntity target) {
        return target != owner && target.isAlive() && !target.isSpectator() && !owner.isAlliedTo(target)
                && owner.canAttack(target) && (!(target instanceof ServerPlayer p)
                    || owner.level().isPvpAllowed() && owner.canHarmPlayer(p));
    }
    public static Optional<LivingEntity> target(ServerPlayer owner, double reach) {
        var hit = ProjectileUtil.getHitResultOnViewVector(owner, e -> e instanceof LivingEntity living
                && living.isPickable() && canDamage(owner, living), reach);
        return hit instanceof EntityHitResult e && e.getEntity() instanceof LivingEntity t && owner.hasLineOfSight(t)
                ? Optional.of(t) : Optional.empty();
    }
    public static boolean primary(ServerPlayer owner) {
        var stand = active(owner); if (stand.isEmpty()) return false;
        var s = session(owner); long now = owner.level().getGameTime();
        if (s.action != Action.NONE || now < s.primaryReady) return false;
        var kind = PartThreeStand.of(owner).orElseThrow();
        int cd = kind.cooldown();
        if (CrusaderProgress.get(owner).training(kind).completed()) cd = cd * 4 / 5;
        if (kind == PartThreeStand.SILVER_CHARIOT && now < s.armorUntil) cd = 8;
        s.primaryReady = now + cd;
        begin(owner, stand.get(), kind, Action.PRIMARY, "attack");
        return true;
    }
    public static boolean secondary(ServerPlayer owner) {
        var stand = active(owner); if (stand.isEmpty()) return false;
        var s = session(owner); long now = owner.level().getGameTime();
        if (s.action != Action.NONE || now < s.secondaryReady) return false;
        var kind = PartThreeStand.of(owner).orElseThrow();
        if (kind == PartThreeStand.HERMIT_PURPLE) {
            var eye = owner.getEyePosition();
            var hit = owner.level().clip(new ClipContext(eye, eye.add(owner.getLookAngle().scale(12)),
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, owner));
            if (hit.getType() != HitResult.Type.BLOCK || eye.distanceTo(hit.getLocation()) < 2) {
                owner.sendSystemMessage(Component.translatable("message.jojosmod.crusader.anchor"), true); return false;
            }
            s.anchor = hit.getLocation();
        }
        s.secondaryReady = now + switch (kind) {
            case SILVER_CHARIOT -> 100; case MAGICIANS_RED -> 120; case HERMIT_PURPLE -> 60;
            case THE_FOOL -> 120; case THE_WORLD -> 100;
        };
        begin(owner, stand.get(), kind, kind == PartThreeStand.HERMIT_PURPLE ? Action.GRAPPLE
                : kind == PartThreeStand.THE_FOOL ? Action.DEFENSE : Action.SECONDARY, "secondary");
        return true;
    }
    public static boolean utility(ServerPlayer owner) {
        var stand = active(owner); if (stand.isEmpty()) return false;
        var s = session(owner); long now = owner.level().getGameTime();
        if (s.action != Action.NONE || now < s.utilityReady) return false;
        var kind = PartThreeStand.of(owner).orElseThrow();
        s.utilityReady = now + (kind == PartThreeStand.SILVER_CHARIOT ? 200 : 180);
        if (kind == PartThreeStand.SILVER_CHARIOT) {
            s.armorUntil = now + 100;
            stand.get().setArmorReleased(true);
        }
        if (kind == PartThreeStand.HERMIT_PURPLE) {
            var enemies = owner.level().getEntitiesOfClass(LivingEntity.class, owner.getBoundingBox().inflate(16),
                    t -> t instanceof Enemy && canDamage(owner, t) && t.distanceToSqr(owner) <= 256);
            var nearest = enemies.stream().min(java.util.Comparator.comparingDouble(owner::distanceToSqr));
            owner.sendSystemMessage(nearest.<Component>map(t -> Component.translatable("message.jojosmod.crusader.divination",
                    t.getName(), t.blockPosition().getX(), t.blockPosition().getY(), t.blockPosition().getZ()))
                    .orElseGet(() -> Component.translatable("message.jojosmod.crusader.no_presence")), false);
            nearest.ifPresent(t -> CrusaderEffects.line(owner.level(), kind, owner.getEyePosition(), t.getEyePosition()));
        }
        begin(owner, stand.get(), kind, kind == PartThreeStand.THE_FOOL ? Action.GLIDE
                : kind == PartThreeStand.MAGICIANS_RED || kind == PartThreeStand.THE_WORLD ? Action.DEFENSE : Action.PRIMARY, "utility");
        // Utility poses for Chariot/divination must not execute their primary damage.
        if (kind == PartThreeStand.SILVER_CHARIOT || kind == PartThreeStand.HERMIT_PURPLE) s.pulses = 100;
        return true;
    }
    private static void begin(ServerPlayer owner, TrainingStandEntity stand, PartThreeStand kind, Action action, String animation) {
        var s = session(owner);
        s.kind = kind; s.action = action; s.startedAt = owner.level().getGameTime(); s.pulses = 0;
        s.volley = UUID.randomUUID();
        stand.playCrusaderAnimation(animation);
        var voice = switch (kind) {
            case SILVER_CHARIOT -> animation.equals("attack") ? Cue.CHARIOT_STRIKE : animation.equals("secondary") ? Cue.CHARIOT_BARRAGE : null;
            case THE_WORLD -> animation.equals("attack") ? Cue.WORLD_STRIKE : animation.equals("secondary") ? Cue.WORLD_BARRAGE : null;
            case MAGICIANS_RED -> animation.equals("attack") ? Cue.CROSSFIRE : animation.equals("secondary") ? Cue.RED_BIND : null;
            case HERMIT_PURPLE -> animation.equals("utility") ? null : Cue.PURPLE_CALL;
            case THE_FOOL -> null;
        };
        if (voice != null) StandVoices.play(owner, voice);
        if (kind == PartThreeStand.SILVER_CHARIOT && animation.equals("utility"))
            owner.level().playSound(null, stand.blockPosition(), io.github.bg7427941.jojosmod.registry.ModSounds.CHARIOT_ARMOR.get(), net.minecraft.sounds.SoundSource.PLAYERS, .65F, 1);
        CrusaderEffects.sound(owner.level(), kind, "cast", stand.position());
        CrusaderEffects.burst(owner.level(), kind, stand.position().add(0, 1, 0), 6);
    }
    public static void onPlayerTick(PlayerTickEvent.Post event) { if (event.getEntity() instanceof ServerPlayer p) tick(p); }
    public static void tick(ServerPlayer owner) {
        if (!owner.hasData(ModAttachments.CRUSADER_SESSION)) return;
        var s = session(owner); var stand = active(owner);
        if (stand.isEmpty() || PartThreeStand.of(owner).filter(k -> k == s.kind).isEmpty()) { cancel(owner); return; }
        long now = owner.level().getGameTime();
        if (s.kind == PartThreeStand.SILVER_CHARIOT) stand.get().setArmorReleased(now < s.armorUntil);
        if (s.action == Action.NONE) return;
        long t = now - s.startedAt;
        if (s.action == Action.PRIMARY) {
            int windup = s.kind == PartThreeStand.SILVER_CHARIOT ? 3 : s.kind == PartThreeStand.THE_WORLD ? 4 : 6;
            if (t >= windup && s.pulses == 0) {
                s.pulses++;
                switch (s.kind) {
                    case SILVER_CHARIOT -> strike(owner, s.kind.definition().combat().damage(), 3.5);
                    case MAGICIANS_RED -> {
                        var flame = new CrusaderFlameEntity(ModEntities.CRUSADER_FLAME.get(), owner.level());
                        flame.initialize(owner, s.volley); owner.level().addFreshEntity(flame);
                        owner.level().playSound(null, stand.get().blockPosition(), io.github.bg7427941.jojosmod.registry.ModSounds.RED_SHOOT.get(), net.minecraft.sounds.SoundSource.PLAYERS, .65F, 1);
                    }
                    case HERMIT_PURPLE -> strike(owner, s.kind.definition().combat().damage(), 5);
                    case THE_WORLD -> strike(owner, s.kind.definition().combat().damage(), 3);
                    case THE_FOOL -> sand(owner);
                }
            }
            if (s.kind == PartThreeStand.THE_FOOL && s.pulses < 3 && t >= 6 + s.pulses * 2) { s.pulses++; sand(owner); }
            if (t >= (s.kind == PartThreeStand.THE_FOOL ? 14 : 12)) finish(owner, false);
        } else if (s.action == Action.SECONDARY) {
            if (s.kind == PartThreeStand.MAGICIANS_RED) {
                if (t >= 5 && s.pulses == 0) {
                    s.pulses++;
                    target(owner, 10).ifPresent(target -> {
                        var token = s.volley;
                        s.bound = target; damage(owner, target, 5, true);
                        if (!java.util.Objects.equals(token, session(owner).volley)) return;
                        var speed = target.getAttribute(Attributes.MOVEMENT_SPEED);
                        if (speed != null) speed.addOrUpdateTransientModifier(new AttributeModifier(bindId(owner), -.9, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
                    });
                }
                if (s.bound != null && (!s.bound.isAlive() || s.bound.level() != owner.level()
                        || !canDamage(owner, s.bound) || !owner.hasLineOfSight(s.bound) || s.bound.distanceToSqr(owner) > 144)) finish(owner);
                else if (s.bound != null && t % 4 == 0) CrusaderEffects.line(owner.level(), s.kind, stand.get().position().add(0, 1, 0), s.bound.getEyePosition());
                if (t >= 45) finish(owner, false);
            } else {
                int max = s.kind == PartThreeStand.SILVER_CHARIOT ? 10 : 12;
                if (s.pulses < max && t >= 3 + s.pulses * 3) { s.pulses++; strike(owner, s.kind == PartThreeStand.SILVER_CHARIOT ? 1.2F : 1.5F, 3.5); }
                if (t >= 3 + max * 3 + 4) finish(owner, false);
            }
        } else if (s.action == Action.GRAPPLE) {
            if (s.anchor == null || t >= 12 || owner.getEyePosition().distanceTo(s.anchor) < 1.7) { finish(owner); return; }
            var direction = s.anchor.subtract(owner.getEyePosition()).normalize();
            var hit = owner.level().clip(new ClipContext(owner.getEyePosition(), s.anchor.add(direction.scale(.05)),
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, owner));
            if (hit.getType() != HitResult.Type.BLOCK || hit.getLocation().distanceTo(s.anchor) > .3) { finish(owner); return; }
            owner.setDeltaMovement(direction.scale(.85)); owner.syncVelocity = true;
            if (t % 2 == 0) CrusaderEffects.line(owner.level(), s.kind, owner.getEyePosition(), s.anchor);
        } else if (s.action == Action.GLIDE) {
            var velocity = owner.getDeltaMovement();
            if (velocity.y < -.12) owner.setDeltaMovement(velocity.x, -.12, velocity.z);
            owner.resetFallDistance(); owner.syncVelocity = true;
            if (t % 5 == 0) CrusaderEffects.burst(owner.level(), s.kind, owner.position(), 4);
            if (t >= 60) finish(owner, false);
        } else if (s.action == Action.DEFENSE) {
            if (t % 5 == 0) CrusaderEffects.burst(owner.level(), s.kind, owner.position().add(0, 1, 0), 8);
            if (t >= 40) finish(owner, false);
        }
    }
    private static void strike(ServerPlayer owner, float amount, double reach) {
        target(owner, reach).ifPresent(t -> {
            CrusaderEffects.line(owner.level(), PartThreeStand.of(owner).orElseThrow(), owner.getEyePosition(), t.getEyePosition());
            damage(owner, t, amount, false);
        });
    }
    private static void sand(ServerPlayer owner) {
        var kind = PartThreeStand.THE_FOOL;
        var eye = owner.getEyePosition();
        var end = owner.level().clip(new ClipContext(eye, eye.add(owner.getLookAngle().scale(6)),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, owner)).getLocation();
        CrusaderEffects.line(owner.level(), kind, eye, end);
        int count = 0;
        for (var t : owner.level().getEntitiesOfClass(LivingEntity.class, owner.getBoundingBox().inflate(6), t -> canDamage(owner, t))) {
            var delta = t.getEyePosition().subtract(owner.getEyePosition());
            if (delta.lengthSqr() <= 36 && delta.normalize().dot(owner.getLookAngle()) >= .8 && owner.hasLineOfSight(t)) {
                damage(owner, t, 2, false);
                if (++count == 8) break;
            }
        }
    }
    public static boolean damage(ServerPlayer owner, LivingEntity target, float amount, boolean fire) {
        if (!canDamage(owner, target) || active(owner).isEmpty()) return false;
        float before = target.getHealth();
        boolean hit = target.hurtServer(owner.level(), ModDamageTypes.crusader(owner, fire), amount);
        if (hit) {
            if (target.getHealth() < before) CrusaderProgress.hit(owner, target);
            PartThreeStand.of(owner).ifPresent(k -> {
                CrusaderEffects.burst(owner.level(), k, target.getEyePosition(), 4);
                CrusaderEffects.sound(owner.level(), k, "impact", target.position());
            });
        }
        return hit;
    }
    private static Identifier bindId(ServerPlayer owner) { return Identifier.fromNamespaceAndPath("jojosmod", "red_bind/" + owner.getUUID()); }
    private static void finish(ServerPlayer owner) { finish(owner, true); }
    private static void finish(ServerPlayer owner, boolean interrupted) {
        var s = session(owner);
        boolean running = s.action != Action.NONE;
        if (interrupted && running) StandVoices.stop(owner);
        if (s.bound != null) { var speed = s.bound.getAttribute(Attributes.MOVEMENT_SPEED); if (speed != null) speed.removeModifier(bindId(owner)); s.bound = null; }
        s.action = Action.NONE; s.anchor = null;
        if (running) StandController.getActive(owner).filter(TrainingStandEntity::isCrusader).ifPresent(stand -> {
            if (interrupted) stand.stopActionAnimation(); else stand.finishActionAnimation();
        });
    }
    public static void cancel(ServerPlayer owner) {
        if (PartThreeStand.of(owner).isPresent()) StandVoices.stop(owner);
        if (!owner.hasData(ModAttachments.CRUSADER_SESSION)) return;
        finish(owner); var s = session(owner); s.volley = null; s.armorUntil = 0;
        StandController.getActive(owner).filter(TrainingStandEntity::isCrusader).ifPresent(e -> e.setArmorReleased(false));
    }
    public static boolean flameValid(ServerPlayer owner, UUID volley) {
        return active(owner).filter(s -> s.getStandDefinition().id().equals(PartThreeStand.MAGICIANS_RED.id())).isPresent()
                && volley.equals(session(owner).volley);
    }
    public static void onDamage(LivingDamageEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer p && event.getHealthDamage() > 0
                && session(p).action != Action.DEFENSE && session(p).action != Action.GLIDE) cancel(p);
    }
    public static void onIncoming(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer owner) || active(owner).isEmpty()) return;
        var s = session(owner);
        if (s.kind == PartThreeStand.SILVER_CHARIOT && owner.level().getGameTime() < s.armorUntil) event.setAmount(event.getAmount() * 1.25F);
        if (s.action != Action.DEFENSE || event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        var pos = event.getSource().getSourcePosition();
        if (s.kind == PartThreeStand.THE_WORLD && (pos == null || pos.subtract(owner.position()).normalize().dot(owner.getLookAngle()) < .5)) return;
        event.setAmount(event.getAmount() * (s.kind == PartThreeStand.THE_FOOL ? .35F : .5F));
    }
    public static void onFall(LivingFallEvent event) {
        if (event.getEntity() instanceof ServerPlayer p && active(p).isPresent() && session(p).action == Action.GLIDE) event.setCanceled(true);
    }
}
