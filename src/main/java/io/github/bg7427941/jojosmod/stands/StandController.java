package io.github.bg7427941.jojosmod.stands;

import io.github.bg7427941.jojosmod.abilities.BasicAttack;
import io.github.bg7427941.jojosmod.abilities.StarPlatinumActions;
import io.github.bg7427941.jojosmod.entities.TrainingStandEntity;
import io.github.bg7427941.jojosmod.registry.ModAttachments;
import io.github.bg7427941.jojosmod.registry.ModEntities;
import io.github.bg7427941.jojosmod.registry.ModStands;
import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;

/** All entry points run on the logical server thread. Clients send intentions only. */
public final class StandController {
    public static final int TOGGLE_COOLDOWN_TICKS = 5;

    private StandController() {}

    private static StandSession session(ServerPlayer owner) {
        return owner.getExistingData(ModAttachments.STAND_SESSION).orElse(StandSession.EMPTY);
    }

    private static Optional<StandDefinition> definition(ServerPlayer owner) {
        return PlayerStands.get(owner).standId().flatMap(ModStands::find);
    }

    public static Optional<TrainingStandEntity> getActive(ServerPlayer owner) {
        return session(owner).activeEntity()
                .map(id -> owner.level().getEntities().get(id))
                .filter(TrainingStandEntity.class::isInstance)
                .map(TrainingStandEntity.class::cast)
                .filter(stand -> !stand.isRemoved() && !stand.isDismissing() && stand.getOwner() == owner);
    }

    public static boolean isActive(ServerPlayer owner, TrainingStandEntity stand) {
        return session(owner).activeEntity().filter(stand.getUUID()::equals).isPresent()
                && stand.getOwner() == owner
                && definition(owner).filter(stand.getStandDefinition()::equals).isPresent();
    }

    public static boolean toggle(ServerPlayer owner) {
        if (!owner.isAlive() || owner.isRemoved() || owner.isSpectator()) {
            return false;
        }
        var state = session(owner);
        long now = owner.level().getGameTime();
        if (now < state.nextToggleTick()) {
            return false;
        }
        var active = getActive(owner);
        if (active.isPresent()) {
            StarPlatinumActions.stop(owner);
            active.get().dismiss();
            owner.setData(ModAttachments.STAND_SESSION,
                    new StandSession(Optional.empty(), now + TOGGLE_COOLDOWN_TICKS, state.nextAttackTick()));
            return true;
        }
        var definition = definition(owner);
        if (definition.isEmpty()) {
            return false;
        }

        var type = definition.get().id().equals(ModStands.STAR_PLATINUM.id())
                ? ModEntities.STAR_PLATINUM.get() : ModEntities.TRAINING_STAND.get();
        var stand = new TrainingStandEntity(type, owner.level());
        stand.setOwner(owner);
        stand.setStandDefinition(definition.get());
        stand.snapTo(owner.position(), owner.getYRot(), 0);
        if (!owner.level().addFreshEntity(stand)) {
            return false;
        }
        owner.setData(ModAttachments.STAND_SESSION, new StandSession(
                Optional.of(stand.getUUID()), now + TOGGLE_COOLDOWN_TICKS, state.nextAttackTick()));
        stand.playSummonAnimation();
        return true;
    }

    /** Returns whether an attempt was accepted; misses consume cooldown as well. */
    public static boolean attack(ServerPlayer owner) {
        if (!owner.isAlive() || owner.isRemoved() || owner.isSpectator()) {
            return false;
        }
        var active = getActive(owner);
        var definition = definition(owner);
        var state = session(owner);
        long now = owner.level().getGameTime();
        if (active.isEmpty() || definition.isEmpty() || !isActive(owner, active.get())
                || now < state.nextAttackTick()
                || active.get().distanceToSqr(owner) > Math.pow(definition.get().combat().tetherDistance(), 2)) {
            return false;
        }
        if (active.get().isStarPlatinum() && !StarPlatinumActions.preciseStrike(owner, active.get())) return false;
        owner.setData(ModAttachments.STAND_SESSION, new StandSession(state.activeEntity(),
                state.nextToggleTick(), now + definition.get().combat().cooldownTicks()));
        if (!active.get().isStarPlatinum()) BasicAttack.perform(owner, active.get(), definition.get());
        return true;
    }

    public static void dismiss(ServerPlayer owner) {
        StarPlatinumActions.stop(owner);
        getActive(owner).ifPresent(TrainingStandEntity::dismiss);
        var state = session(owner);
        owner.setData(ModAttachments.STAND_SESSION,
                new StandSession(Optional.empty(), state.nextToggleTick(), state.nextAttackTick()));
    }
}
