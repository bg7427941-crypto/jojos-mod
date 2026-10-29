package io.github.bg7427941.jojosmod.abilities;

import io.github.bg7427941.jojosmod.effects.HierophantEffects;
import io.github.bg7427941.jojosmod.entities.EmeraldShardEntity;
import io.github.bg7427941.jojosmod.entities.TrainingStandEntity;
import io.github.bg7427941.jojosmod.registry.ModAttachments;
import io.github.bg7427941.jojosmod.registry.ModEntities;
import io.github.bg7427941.jojosmod.registry.ModSounds;
import io.github.bg7427941.jojosmod.registry.ModStands;
import io.github.bg7427941.jojosmod.stands.HierophantProgress;
import io.github.bg7427941.jojosmod.stands.HierophantSession;
import io.github.bg7427941.jojosmod.stands.PlayerStands;
import io.github.bg7427941.jojosmod.stands.StandController;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** One timed, server-authoritative volley. Clients never supply targets, origins or projectile counts. */
public final class HierophantSplash {
    public static final int WINDUP = 6;
    public static final int PULSES = 3;
    public static final int PULSE_INTERVAL = 2;
    public static final int ACTION_TICKS = 18;
    public static final int COOLDOWN = 100;
    public static final int MASTERED_COOLDOWN = 80;

    private HierophantSplash() {}

    public static HierophantSession session(ServerPlayer owner) {
        return owner.getExistingData(ModAttachments.HIEROPHANT_SESSION).orElse(HierophantSession.EMPTY);
    }

    public static Optional<TrainingStandEntity> active(ServerPlayer owner) {
        if (!owner.isAlive() || owner.isRemoved() || owner.isSpectator() || TimeStop.isFrozen(owner)
                || PlayerStands.get(owner).standId().filter(ModStands.HIEROPHANT_GREEN.id()::equals).isEmpty()) return Optional.empty();
        return StandController.getActive(owner).filter(TrainingStandEntity::isHierophantGreen)
                .filter(stand -> StandController.isActive(owner, stand))
                .filter(stand -> stand.distanceToSqr(owner) <= 16);
    }

    public static int cooldown(ServerPlayer owner) {
        return HierophantProgress.get(owner).completed() ? MASTERED_COOLDOWN : COOLDOWN;
    }

    public static boolean start(ServerPlayer owner, TrainingStandEntity stand) {
        var state = session(owner);
        long now = owner.level().getGameTime();
        if (active(owner).filter(current -> current == stand).isEmpty() || state.casting() || HierophantTools.busy(owner) || now < state.readyAt()) return false;
        owner.setData(ModAttachments.HIEROPHANT_SESSION,
                new HierophantSession(Optional.of(UUID.randomUUID()), now, 0, true, now + cooldown(owner)));
        stand.playAttackAnimation();
        io.github.bg7427941.jojosmod.effects.StandVoices.play(owner, io.github.bg7427941.jojosmod.network.StandVoicePayload.Cue.EMERALD_SPLASH);
        owner.level().playSound(null, stand.blockPosition(), ModSounds.HIEROPHANT_CHARGE.get(), SoundSource.PLAYERS, .5F, 1);
        HierophantEffects.pulse(owner.level(), stand.position().add(0, 1.35, 0));
        owner.sendSystemMessage(Component.translatable("message.jojosmod.hierophant.splash"), true);
        return true;
    }

    public static boolean projectileValid(ServerPlayer owner, UUID volley) {
        return session(owner).volley().filter(volley::equals).isPresent() && active(owner).isPresent();
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer owner) tick(owner);
    }

    public static void tick(ServerPlayer owner) {
        var state = session(owner);
        if (state.volley().isEmpty()) return;
        var stand = active(owner);
        if (stand.isEmpty()) { cancel(owner); return; }
        if (!state.casting()) return;
        long elapsed = owner.level().getGameTime() - state.startedAt();
        // Each callback emits at most one scheduled pulse.
        if (state.pulses() < PULSES && elapsed >= WINDUP + state.pulses() * PULSE_INTERVAL) {
            emit(owner, stand.get(), state.volley().orElseThrow());
            // Other mods may cancel or replace the cast during entity-join hooks.
            if (!session(owner).volley().equals(state.volley()) || !session(owner).casting()) return;
            state = new HierophantSession(state.volley(), state.startedAt(), state.pulses() + 1, true, state.readyAt());
            owner.setData(ModAttachments.HIEROPHANT_SESSION, state);
        }
        if (elapsed >= ACTION_TICKS) {
            owner.setData(ModAttachments.HIEROPHANT_SESSION, state.finish());
            stand.get().finishActionAnimation();
        }
    }

    private static void emit(ServerPlayer owner, TrainingStandEntity stand, UUID volley) {
        var eye = owner.getEyePosition();
        var desiredOrigin = stand.position().add(0, 1.35, 0).add(owner.getLookAngle().scale(.3));
        var obstruction = owner.level().clip(new ClipContext(eye, desiredOrigin,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, owner));
        // A cosmetic hand offset must never create a projectile behind a wall beside the player.
        var origin = obstruction.getType() == HitResult.Type.MISS ? desiredOrigin : eye;
        // Aim from the cosmetic hand at the server's actual crosshair hit. A fixed far
        // plane made close targets miss the whole volley because of shoulder parallax.
        var aimedHit = net.minecraft.world.entity.projectile.ProjectileUtil.getHitResultOnViewVector(owner,
                entity -> entity instanceof net.minecraft.world.entity.LivingEntity target && EmeraldShardEntity.canDamage(owner, target),
                EmeraldShardEntity.MAX_RANGE);
        var aim = aimedHit.getType() == HitResult.Type.MISS ? eye.add(owner.getLookAngle().scale(EmeraldShardEntity.MAX_RANGE)) : aimedHit.getLocation();
        var centerDirection = aim.subtract(origin).normalize();
        for (int angle : new int[] {-5, 0, 5}) {
            if (!projectileValid(owner, volley)) return;
            var velocity = centerDirection.yRot((float)Math.toRadians(angle));
            var shard = new EmeraldShardEntity(ModEntities.EMERALD_SHARD.get(), owner.level());
            shard.initialize(owner, volley, origin);
            shard.shoot(velocity.x, velocity.y, velocity.z, EmeraldShardEntity.SPEED, 0);
            owner.level().addFreshEntity(shard);
        }
        owner.level().playSound(null, stand.blockPosition(), ModSounds.HIEROPHANT_SPLASH.get(), SoundSource.PLAYERS, .4F, 1);
        HierophantEffects.pulse(owner.level(), origin);
    }

    public static void cancel(ServerPlayer owner) {
        var state = session(owner);
        if (state.volley().isEmpty()) return;
        io.github.bg7427941.jojosmod.effects.StandVoices.stop(owner);
        owner.setData(ModAttachments.HIEROPHANT_SESSION, state.cancel());
        StandController.getActive(owner).filter(TrainingStandEntity::isHierophantGreen)
                .ifPresent(TrainingStandEntity::stopActionAnimation);
    }

    public static void onDamage(LivingDamageEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer owner && event.getHealthDamage() > 0) cancel(owner);
    }
}
