package io.github.bg7427941.jojosmod.abilities;

import io.github.bg7427941.jojosmod.effects.StarPlatinumEffects;
import io.github.bg7427941.jojosmod.registry.ModAttachments;
import io.github.bg7427941.jojosmod.registry.ModSounds;
import io.github.bg7427941.jojosmod.registry.ModStands;
import io.github.bg7427941.jojosmod.stands.PlayerStands;
import io.github.bg7427941.jojosmod.stands.StarLeapSession;
import io.github.bg7427941.jojosmod.stands.StarPlatinumProgress;
import io.github.bg7427941.jojosmod.stands.StandController;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** One server-authorized ground impulse; a leap never grants flight or general damage immunity. */
public final class StarPlatinumLeap {
    public static final double HORIZONTAL_SPEED = 0.85;
    public static final double VERTICAL_SPEED = 0.85;
    public static final int COOLDOWN_TICKS = 80;
    public static final int GRACE_TICKS = 60;
    public static final float FALL_RELIEF = 4;

    private StarPlatinumLeap() {}

    public static StarLeapSession session(ServerPlayer owner) {
        return owner.getExistingData(ModAttachments.STAR_LEAP).orElse(StarLeapSession.EMPTY);
    }

    private static boolean groundedMovement(ServerPlayer owner) {
        return owner.onGround() && !owner.getAbilities().flying && !owner.isFallFlying()
                && !owner.isPassenger() && !owner.isInWater() && !owner.isInLava() && !owner.onClimbable()
                // Check actual support as well as the player's movement packet flag.
                && supported(owner)
                && owner.level().noCollision(owner, owner.getBoundingBox().move(0, 0.2, 0));
    }

    private static boolean supported(ServerPlayer owner) {
        return !owner.level().noCollision(owner, owner.getBoundingBox().move(0, -0.05, 0));
    }

    public static boolean leap(ServerPlayer owner) {
        var stand = StarPlatinumActions.active(owner);
        if (stand.isEmpty()) {
            owner.sendSystemMessage(Component.translatable("message.jojosmod.star.required"), true);
            return false;
        }
        if (!StarPlatinumProgress.leapUnlocked(owner)) {
            owner.sendSystemMessage(Component.translatable("message.jojosmod.star.leap_locked",
                    StarPlatinumProgress.LEAP_REQUIREMENT), true);
            return false;
        }
        if (StarPlatinumActions.isBusy(owner)) {
            owner.sendSystemMessage(Component.translatable("message.jojosmod.star.busy"), true);
            return false;
        }
        var state = session(owner);
        long now = owner.level().getGameTime();
        if (now < state.readyAt()) {
            owner.sendSystemMessage(Component.translatable("message.jojosmod.star.cooldown",
                    (state.readyAt() - now + 19) / 20), true);
            return false;
        }
        if (!groundedMovement(owner)) {
            owner.sendSystemMessage(Component.translatable("message.jojosmod.star.leap_ground"), true);
            return false;
        }
        double yaw = Math.toRadians(owner.getYRot());
        var impulse = new Vec3(-Math.sin(yaw) * HORIZONTAL_SPEED, VERTICAL_SPEED,
                Math.cos(yaw) * HORIZONTAL_SPEED);
        owner.setData(ModAttachments.STAR_LEAP, new StarLeapSession(now + COOLDOWN_TICKS,
                now + GRACE_TICKS, owner.getY(), false, owner.level().dimension()));
        owner.fallDistance = 0;
        owner.setDeltaMovement(impulse);
        owner.setOnGround(false);
        owner.syncVelocity = true;
        owner.needsSync = true;
        stand.get().playLeapAnimation();
        owner.level().playSound(null, owner.blockPosition(), ModSounds.STAR_LEAP.get(), SoundSource.PLAYERS, 0.6F, 1);
        StarPlatinumEffects.leap(owner, false);
        owner.sendSystemMessage(Component.translatable("message.jojosmod.star.leap"), true);
        return true;
    }

    private static boolean validGrace(ServerPlayer owner, StarLeapSession state) {
        return state.protectionUntil() > owner.level().getGameTime() && owner.isAlive() && !owner.isRemoved()
                && !owner.isSpectator() && state.dimension().equals(owner.level().dimension())
                && !owner.getAbilities().flying && !owner.isFallFlying() && !owner.isPassenger()
                && !owner.isInWater() && !owner.isInLava() && !owner.onClimbable()
                && PlayerStands.get(owner).standId().filter(ModStands.STAR_PLATINUM.id()::equals).isPresent();
    }

    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        if (event.getEntity() instanceof ServerPlayer owner) tick(owner);
    }

    public static void tick(ServerPlayer owner) {
        var state = session(owner);
        if (state.protectionUntil() == 0) return;
        if (!validGrace(owner, state)) {
            owner.setData(ModAttachments.STAR_LEAP, state.clearGrace());
            return;
        }
        // Inspect committed movement before server travel predicts a landing and rolls position back.
        // That prediction can leave onGround set while the real fall packet is still in flight.
        if (state.airborne() && owner.onGround() && supported(owner)) {
            land(owner, state);
        } else if (!state.airborne() && (owner.getY() > state.launchY() + 0.1 || owner.fallDistance > 0)) {
            owner.setData(ModAttachments.STAR_LEAP, state.markAirborne());
        }
    }

    public static void onFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer owner)) return;
        var state = session(owner);
        if (state.protectionUntil() == 0) return;
        if (!validGrace(owner, state)) {
            owner.setData(ModAttachments.STAR_LEAP, state.clearGrace());
            return;
        }
        // Ignore the zero-distance ground check until the player has actually left the ground.
        if (event.getDistance() <= 0 && (!state.airborne() || !owner.onGround() || !supported(owner))) return;
        event.setDistance(Math.max(0, event.getDistance() - FALL_RELIEF));
        land(owner, state);
    }

    private static void land(ServerPlayer owner, StarLeapSession state) {
        owner.setData(ModAttachments.STAR_LEAP, state.clearGrace());
        if (!StarPlatinumActions.isBusy(owner)) {
            StandController.getActive(owner).filter(stand -> stand.isStarPlatinum())
                    .ifPresent(stand -> stand.playLeapLandAnimation());
        }
        owner.level().playSound(null, owner.blockPosition(), ModSounds.STAR_LEAP_LAND.get(), SoundSource.PLAYERS, 0.55F, 1);
        StarPlatinumEffects.leap(owner, true);
    }

    public static void onDamage(LivingDamageEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer owner && event.getHealthDamage() > 0) {
            var state = session(owner);
            if (state.protectionUntil() != 0) owner.setData(ModAttachments.STAR_LEAP, state.clearGrace());
        }
    }
}
