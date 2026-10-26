package io.github.bg7427941.jojosmod.abilities;

import io.github.bg7427941.jojosmod.JojosMod;
import io.github.bg7427941.jojosmod.entities.TrainingStandEntity;
import io.github.bg7427941.jojosmod.registry.ModAttachments;
import io.github.bg7427941.jojosmod.registry.ModStands;
import io.github.bg7427941.jojosmod.registry.ModSounds;
import io.github.bg7427941.jojosmod.effects.StarPlatinumEffects;
import io.github.bg7427941.jojosmod.effects.StandVoices;
import io.github.bg7427941.jojosmod.network.StandVoicePayload.Cue;
import io.github.bg7427941.jojosmod.stands.PlayerStands;
import io.github.bg7427941.jojosmod.stands.StandController;
import io.github.bg7427941.jojosmod.stands.StarPlatinumSession;
import io.github.bg7427941.jojosmod.stands.StarPlatinumProgress;
import io.github.bg7427941.jojosmod.stands.StarPlatinumSession.Action;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Timed actions and defensive mitigation are resolved on the server, once per player tick. */
public final class StarPlatinumActions {
    public static final int WINDUP_TICKS = 4;
    public static final int STRIKE_TICKS = 12;
    public static final int BARRAGE_HITS = 8;
    public static final int BARRAGE_INTERVAL = 3;
    public static final int BARRAGE_TICKS = 30;
    public static final int BARRAGE_COOLDOWN = 100;
    public static final int GUARD_TICKS = 40;
    public static final int GUARD_COOLDOWN = 80;
    public static final float GUARD_DAMAGE_MULTIPLIER = 0.4F;
    public static final int STAR_FINGER_WINDUP = 8;
    public static final int STAR_FINGER_TICKS = 18;
    public static final int STAR_FINGER_COOLDOWN = 120;
    public static final float STAR_FINGER_DAMAGE = 4;
    public static final double STAR_FINGER_REACH = 6;
    private static final Identifier GUARD_SLOW = Identifier.fromNamespaceAndPath(JojosMod.MOD_ID, "star_guard");

    private StarPlatinumActions() {}

    public static StarPlatinumSession session(ServerPlayer owner) {
        return owner.getExistingData(ModAttachments.STAR_SESSION).orElse(StarPlatinumSession.EMPTY);
    }

    public static boolean isBusy(ServerPlayer owner) {
        return session(owner).action() != Action.NONE;
    }

    private static Optional<TrainingStandEntity> active(ServerPlayer owner) {
        if (!owner.isAlive() || owner.isRemoved() || owner.isSpectator()
                || PlayerStands.get(owner).standId().filter(ModStands.STAR_PLATINUM.id()::equals).isEmpty()) {
            return Optional.empty();
        }
        return StandController.getActive(owner).filter(TrainingStandEntity::isStarPlatinum)
                .filter(stand -> StandController.isActive(owner, stand))
                .filter(stand -> stand.distanceToSqr(owner) <= Math.pow(ModStands.STAR_PLATINUM.combat().tetherDistance(), 2));
    }

    private static Optional<TrainingStandEntity> requireActive(ServerPlayer owner) {
        var stand = active(owner);
        if (stand.isEmpty()) owner.sendSystemMessage(Component.translatable("message.jojosmod.star.required"), true);
        return stand;
    }

    public static boolean preciseStrike(ServerPlayer owner, TrainingStandEntity stand) {
        var state = session(owner);
        if (active(owner).filter(s -> s == stand).isEmpty() || busy(owner, state)) return false;
        owner.setData(ModAttachments.STAR_SESSION,
                new StarPlatinumSession(Action.STRIKE, owner.level().getGameTime(), 0,
                        state.barrageReadyAt(), state.guardReadyAt(), state.fingerReadyAt()));
        stand.playAttackAnimation();
        return true;
    }

    public static boolean barrage(ServerPlayer owner) {
        var stand = requireActive(owner);
        var state = session(owner);
        long now = owner.level().getGameTime();
        if (stand.isEmpty() || busy(owner, state) || coolingDown(owner, state.barrageReadyAt() - now)) return false;
        owner.setData(ModAttachments.STAR_SESSION,
                new StarPlatinumSession(Action.BARRAGE, now, 0, now + BARRAGE_COOLDOWN, state.guardReadyAt(), state.fingerReadyAt()));
        stand.get().playBarrageAnimation();
        StandVoices.play(owner, Cue.BARRAGE);
        owner.sendSystemMessage(Component.translatable("message.jojosmod.star.barrage"), true);
        return true;
    }

    public static boolean starFinger(ServerPlayer owner) {
        var stand = requireActive(owner);
        if (stand.isEmpty()) return false;
        if (!StarPlatinumProgress.starFingerUnlocked(owner)) {
            owner.sendSystemMessage(Component.translatable("message.jojosmod.star.finger_locked",
                    StarPlatinumProgress.STAR_FINGER_REQUIREMENT), true);
            return false;
        }
        var state = session(owner);
        long now = owner.level().getGameTime();
        if (busy(owner, state) || coolingDown(owner, state.fingerReadyAt() - now)) return false;
        owner.setData(ModAttachments.STAR_SESSION, new StarPlatinumSession(Action.STAR_FINGER, now, 0,
                state.barrageReadyAt(), state.guardReadyAt(), now + STAR_FINGER_COOLDOWN));
        stand.get().playStarFingerAnimation();
        StandVoices.play(owner, Cue.STAR_FINGER);
        owner.sendSystemMessage(Component.translatable("message.jojosmod.star.finger"), true);
        return true;
    }

    public static boolean toggleGuard(ServerPlayer owner) {
        var state = session(owner);
        if (state.action() == Action.GUARD) {
            stop(owner);
            return true;
        }
        var stand = requireActive(owner);
        long now = owner.level().getGameTime();
        if (stand.isEmpty() || busy(owner, state) || coolingDown(owner, state.guardReadyAt() - now)) return false;
        owner.setData(ModAttachments.STAR_SESSION,
                new StarPlatinumSession(Action.GUARD, now, 0, state.barrageReadyAt(), now + GUARD_COOLDOWN, state.fingerReadyAt()));
        var speed = owner.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null && !speed.hasModifier(GUARD_SLOW)) {
            speed.addTransientModifier(new AttributeModifier(GUARD_SLOW, -0.45, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
        stand.get().setGuarding(true);
        owner.level().playSound(null, owner.blockPosition(), ModSounds.STAR_GUARD.get(), SoundSource.PLAYERS, 0.5F, 1.0F);
        StarPlatinumEffects.guardStart(owner, stand.get());
        owner.sendSystemMessage(Component.translatable("message.jojosmod.star.guard"), true);
        return true;
    }

    private static boolean busy(ServerPlayer owner, StarPlatinumSession state) {
        if (state.action() == Action.NONE) return false;
        owner.sendSystemMessage(Component.translatable("message.jojosmod.star.busy"), true);
        return true;
    }

    private static boolean coolingDown(ServerPlayer owner, long remaining) {
        if (remaining <= 0) return false;
        owner.sendSystemMessage(Component.translatable("message.jojosmod.star.cooldown", (remaining + 19) / 20), true);
        return true;
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer owner) tick(owner);
    }

    public static void tick(ServerPlayer owner) {
        var state = session(owner);
        if (state.action() == Action.NONE) return;
        var stand = active(owner);
        if (stand.isEmpty()) {
            stop(owner);
            return;
        }
        long elapsed = owner.level().getGameTime() - state.startedAt();
        if (state.action() == Action.GUARD) {
            if (elapsed >= GUARD_TICKS) stop(owner);
            return;
        }
        boolean strike = state.action() == Action.STRIKE;
        boolean finger = state.action() == Action.STAR_FINGER;
        boolean barrage = state.action() == Action.BARRAGE;
        int total = barrage ? BARRAGE_HITS : 1;
        int next = (finger ? STAR_FINGER_WINDUP : WINDUP_TICKS) + (barrage ? state.pulses() * BARRAGE_INTERVAL : 0);
        if (state.pulses() < total && elapsed >= next) {
            var profile = ModStands.STAR_PLATINUM.combat();
            owner.level().playSound(null, stand.get().blockPosition(),
                    ModSounds.STAR_SWING.get(),
                    SoundSource.PLAYERS, barrage ? 0.27F : 0.55F, barrage ? 1.14F : 0.94F);
            if (finger) StarPlatinumEffects.starFinger(owner, stand.get(), STAR_FINGER_REACH);
            else StarPlatinumEffects.swing(owner, stand.get(), barrage);
            BasicAttack.hit(owner, stand.get(), finger ? STAR_FINGER_DAMAGE : strike ? profile.damage() : 1.0F,
                    finger ? STAR_FINGER_REACH : profile.reach(), barrage);
            // Damage hooks from other mods may have interrupted the owner during a strike.
            if (session(owner).action() != state.action() || session(owner).startedAt() != state.startedAt()) return;
            owner.setData(ModAttachments.STAR_SESSION, new StarPlatinumSession(state.action(), state.startedAt(),
                    state.pulses() + 1, state.barrageReadyAt(), state.guardReadyAt(), state.fingerReadyAt()));
        }
        if (elapsed >= (finger ? STAR_FINGER_TICKS : strike ? STRIKE_TICKS : BARRAGE_TICKS)) stop(owner, false);
    }

    public static void stop(ServerPlayer owner) {
        stop(owner, true);
    }

    private static void stop(ServerPlayer owner, boolean cancelVoice) {
        if (cancelVoice && StandController.getActive(owner).filter(TrainingStandEntity::isStarPlatinum).isPresent()) {
            StandVoices.stop(owner);
        }
        var state = session(owner);
        var speed = owner.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) speed.removeModifier(GUARD_SLOW);
        if (state.action() != Action.NONE) {
            StandController.getActive(owner).filter(TrainingStandEntity::isStarPlatinum)
                    .ifPresent(TrainingStandEntity::stopActionAnimation);
            owner.setData(ModAttachments.STAR_SESSION, state.finish());
        }
    }

    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer owner) || session(owner).action() != Action.GUARD
                || active(owner).isEmpty()) return;
        var source = event.getSource();
        boolean physical = source.is(DamageTypeTags.IS_PROJECTILE) || source.is(DamageTypeTags.IS_PLAYER_ATTACK)
                || source.is(DamageTypes.MOB_ATTACK) || source.is(DamageTypes.MOB_ATTACK_NO_AGGRO);
        var sourcePosition = source.getSourcePosition();
        if (!physical || sourcePosition == null || event.getAmount() <= 0) return;
        var direction = sourcePosition.subtract(owner.position()).multiply(1, 0, 1);
        var look = owner.getLookAngle().multiply(1, 0, 1);
        if (direction.lengthSqr() < 0.0001 || look.lengthSqr() < 0.0001
                || direction.normalize().dot(look.normalize()) < 0.5) return;
        event.setAmount(event.getAmount() * GUARD_DAMAGE_MULTIPLIER);
        StarPlatinumProgress.onGuardBlock(owner, source);
        owner.level().playSound(null, owner.blockPosition(), ModSounds.STAR_GUARD_BLOCK.get(), SoundSource.PLAYERS, 0.6F, 1.05F);
        StarPlatinumEffects.guardBlock(owner);
    }

    public static void onDamage(LivingDamageEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer owner && event.getHealthDamage() > 0
                && (session(owner).action() == Action.BARRAGE || session(owner).action() == Action.STRIKE
                    || session(owner).action() == Action.STAR_FINGER)) {
            stop(owner);
        }
    }
}
