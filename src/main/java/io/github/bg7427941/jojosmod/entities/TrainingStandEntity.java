package io.github.bg7427941.jojosmod.entities;

import io.github.bg7427941.jojosmod.abilities.CrusaderActions;
import io.github.bg7427941.jojosmod.abilities.TimeStop;
import io.github.bg7427941.jojosmod.stands.PartThreeStand;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;
import io.github.bg7427941.jojosmod.registry.ModStands;
import io.github.bg7427941.jojosmod.registry.ModEntities;
import io.github.bg7427941.jojosmod.registry.ModSounds;
import io.github.bg7427941.jojosmod.effects.StarPlatinumEffects;
import io.github.bg7427941.jojosmod.effects.StandVoices;
import io.github.bg7427941.jojosmod.network.StandVoicePayload.Cue;
import io.github.bg7427941.jojosmod.abilities.StarPlatinumActions;
import io.github.bg7427941.jojosmod.abilities.HierophantSplash;
import io.github.bg7427941.jojosmod.effects.HierophantEffects;
import io.github.bg7427941.jojosmod.stands.StandController;
import io.github.bg7427941.jojosmod.stands.StandDefinition;
import java.util.Objects;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.LinearInterpolationHandler;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** Temporary Stand manifestation; the player's attachment owns the persistent assignment. */
public final class TrainingStandEntity extends Entity implements GeoEntity {
    private static final EntityDataAccessor<Integer> OWNER_ID =
            SynchedEntityData.defineId(TrainingStandEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> STAND_ID =
            SynchedEntityData.defineId(TrainingStandEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> GUARDING =
            SynchedEntityData.defineId(TrainingStandEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> MOVING =
            SynchedEntityData.defineId(TrainingStandEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DISMISSING =
            SynchedEntityData.defineId(TrainingStandEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> ARMOR_RELEASED = SynchedEntityData.defineId(TrainingStandEntity.class, EntityDataSerializers.BOOLEAN);
    private static final double FOLLOW_BEHIND = 1.25;
    private static final double FOLLOW_SIDE = 0.9;
    private static final double FOLLOW_HEIGHT = 0.15;
    private static final double FOLLOW_SMOOTHING = 0.35;
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.training_stand.idle");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.training_stand.attack");
    private static final RawAnimation HIEROPHANT_IDLE = RawAnimation.begin().thenLoop("animation.hierophant_green.idle");
    private static final RawAnimation HIEROPHANT_MOVE = RawAnimation.begin().thenLoop("animation.hierophant_green.move");
    private static final RawAnimation STAR_IDLE = RawAnimation.begin().thenLoop("animation.star_platinum.idle");
    private static final RawAnimation STAR_MOVE = RawAnimation.begin().thenLoop("animation.star_platinum.move");
    private static final RawAnimation STAR_GUARD = RawAnimation.begin().thenLoop("animation.star_platinum.guard");

    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private @Nullable ServerPlayer owner;
    private long dismissAt = -1;

    public TrainingStandEntity(EntityType<? extends TrainingStandEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        setNoGravity(true);
    }

    public void setOwner(ServerPlayer owner) {
        this.owner = Objects.requireNonNull(owner, "owner");
        entityData.set(OWNER_ID, owner.getId());
    }

    /** Synced owner identity for observer render layers. */
    public int getOwnerEntityId() { return entityData.get(OWNER_ID); }

    /** The authoritative owner reference is available only on the server. */
    public @Nullable ServerPlayer getOwner() {
        return owner;
    }

    public void setStandDefinition(StandDefinition definition) {
        entityData.set(STAND_ID, definition.id().toString());
    }

    /** Entity data also reaches observers, without making the owner's private attachment public. */
    public StandDefinition getStandDefinition() {
        var id = Identifier.tryParse(entityData.get(STAND_ID));
        return id == null ? ModStands.TRAINING_STAND : ModStands.find(id).orElse(ModStands.TRAINING_STAND);
    }

    @Override
    protected Component getTypeName() {
        return Component.translatable(getStandDefinition().translationKey());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(OWNER_ID, -1);
        builder.define(STAND_ID, ModStands.TRAINING_STAND.id().toString());
        builder.define(GUARDING, false);
        builder.define(MOVING, false);
        builder.define(DISMISSING, false);
        builder.define(ARMOR_RELEASED, false);
    }

    @Override
    protected InterpolationHandler createInterpolationHandler() {
        return LinearInterpolationHandler.create(this);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            return;
        }
        if (dismissAt >= 0) {
            if (level().getGameTime() >= dismissAt) discard();
            return;
        }

        var player = getOwner();
        if (player == null || player.isRemoved() || !player.isAlive() || player.isSpectator()
                || player.level() != level() || !StandController.isActive(player, this)) {
            discard();
            return;
        }

        double yaw = Math.toRadians(player.getYRot());
        var forward = new Vec3(-Math.sin(yaw), 0, Math.cos(yaw));
        var side = new Vec3(Math.cos(yaw), 0, Math.sin(yaw));
        boolean acting = isStarPlatinum() && StarPlatinumActions.isBusy(player)
                || isHierophantGreen() && HierophantSplash.session(player).casting()
                || isCrusader() && CrusaderActions.session(player).action != io.github.bg7427941.jojosmod.stands.CrusaderSession.Action.NONE;
        var target = player.position()
                .add(forward.scale(acting ? 0.45 : -FOLLOW_BEHIND))
                .add(side.scale(FOLLOW_SIDE))
                .add(0, FOLLOW_HEIGHT, 0);
        if (getStandDefinition().id().equals(PartThreeStand.HERMIT_PURPLE.id())) target = player.position();
        double maxDistance = getStandDefinition().combat().tetherDistance();

        // Snap after teleports so this temporary entity cannot remain separated from its owner.
        setPos(getStandDefinition().id().equals(PartThreeStand.HERMIT_PURPLE.id()) || distanceToSqr(player) > maxDistance * maxDistance
                ? target : position().lerp(target, FOLLOW_SMOOTHING));
        setYRot(player.getYRot());
        setXRot(0);
        setDeltaMovement(Vec3.ZERO);
        if (isStarPlatinum() || isHierophantGreen() || isCrusader()) {
            entityData.set(MOVING, player.getDeltaMovement().horizontalDistanceSqr() > 0.001);
            if (isStarPlatinum()) StarPlatinumEffects.aura(this, level().getGameTime());
            else if (isHierophantGreen()) HierophantEffects.aura(this, level().getGameTime());
            else if (level().getGameTime() % 8 == 0 && level() instanceof ServerLevel server) io.github.bg7427941.jojosmod.effects.CrusaderEffects.burst(server, PartThreeStand.of(getStandDefinition().id()).orElseThrow(), position().add(0, 1, 0), 1);
        }
    }

    /** Called by the server after it accepts the basic-attack request. */
    public void playAttackAnimation() {
        triggerAnim("main", "attack");
    }

    public boolean isStarPlatinum() {
        return getType() == ModEntities.STAR_PLATINUM.get();
    }

    public boolean isCrusader() { return PartThreeStand.of(getStandDefinition().id()).isPresent(); }
    public void setArmorReleased(boolean released) { entityData.set(ARMOR_RELEASED, released); }
    public void playCrusaderAnimation(String action) { triggerAnim("main", action); }
    public boolean isHierophantGreen() { return getType() == ModEntities.HIEROPHANT_GREEN.get(); }

    public boolean isDismissing() {
        return entityData.get(DISMISSING);
    }

    public void playSummonAnimation() {
        if (isCrusader()) {
            playCrusaderAnimation("summon");
            if (level() instanceof ServerLevel server) {
                var kind = PartThreeStand.of(getStandDefinition().id()).orElseThrow();
                io.github.bg7427941.jojosmod.effects.CrusaderEffects.sound(server, kind, "summon", position());
                io.github.bg7427941.jojosmod.effects.CrusaderEffects.burst(server, kind, position().add(0, 1, 0), 10);
            }
            return;
        }
        if (isHierophantGreen()) {
            triggerAnim("main", "summon");
            if (level() instanceof ServerLevel server) {
                server.playSound(null, blockPosition(), ModSounds.HIEROPHANT_SUMMON.get(), SoundSource.PLAYERS, .6F, 1);
                HierophantEffects.manifestation(this);
            }
            return;
        }
        if (isStarPlatinum()) {
            triggerAnim("main", "summon");
            if (level() instanceof ServerLevel server) {
                if (owner != null) StandVoices.play(owner, Cue.SUMMON);
                StarPlatinumEffects.summon(this);
            }
        }
    }

    public void playBarrageAnimation() {
        triggerAnim("main", "barrage");
    }

    public void playStarFingerAnimation() {
        triggerAnim("main", "star_finger");
    }

    public void playInterceptAnimation() { triggerAnim("main", "intercept"); }

    public void playInterceptCatchAnimation() { triggerAnim("main", "intercept_catch"); }
    public void playLeapAnimation() { triggerAnim("main", "leap"); }
    public void playLeapLandAnimation() { triggerAnim("main", "leap_land"); }

    public void setGuarding(boolean guarding) {
        if (guarding) {
            stopTriggeredAnim("main", "summon");
            stopTriggeredAnim("main", "recover");
        }
        entityData.set(GUARDING, guarding);
    }

    /** Natural recovery lets the authored follow-through finish; a fresh action can replace it. */
    public void finishActionAnimation() { setGuarding(false); }

    public void stopActionAnimation() {
        setGuarding(false);
        stopTriggeredAnim("main", "attack");
        stopTriggeredAnim("main", "barrage");
        stopTriggeredAnim("main", "star_finger");
        stopTriggeredAnim("main", "intercept");
        stopTriggeredAnim("main", "intercept_catch");
        stopTriggeredAnim("main", "secondary"); stopTriggeredAnim("main", "utility");
        stopTriggeredAnim("main", "time_stop");
        if (isStarPlatinum() || isHierophantGreen() || isCrusader()) triggerAnim("main", "recover");
    }

    /** Clears gameplay immediately while giving animated Stands four ticks to disappear visually. */
    public void dismiss() {
        if (!isStarPlatinum() && !isHierophantGreen() && !isCrusader()) {
            discard();
            return;
        }
        entityData.set(DISMISSING, true);
        dismissAt = level().getGameTime() + 4;
        triggerAnim("main", "dismiss");
        if (level() instanceof ServerLevel server) {
            if (isCrusader()) {
                io.github.bg7427941.jojosmod.effects.CrusaderEffects.sound(server, PartThreeStand.of(getStandDefinition().id()).orElseThrow(), "dismiss", position());
                return;
            }
            server.playSound(null, blockPosition(), isHierophantGreen() ? ModSounds.HIEROPHANT_DISMISS.get()
                    : ModSounds.STAR_DISMISS.get(), SoundSource.PLAYERS, 0.45F, 1.1F);
            if (isHierophantGreen()) HierophantEffects.manifestation(this);
            else StarPlatinumEffects.dismiss(this);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        var kind = PartThreeStand.of(getStandDefinition().id());
        // Entity data can arrive after controller registration: the registered type is authoritative for choosing assets.
        if (kind.isEmpty()) for (var candidate : PartThreeStand.values()) if (getType() == ModEntities.crusader(candidate)) kind = java.util.Optional.of(candidate);
        if (kind.isPresent()) {
            String prefix = "animation." + kind.get().path() + ".";
            controllers.add(new AnimationController<TrainingStandEntity>("locomotion", 3, state -> state.setAndContinue(
                    RawAnimation.begin().thenLoop(prefix + (entityData.get(MOVING) ? "move" : "idle")))));
            var main = new AnimationController<TrainingStandEntity>("main", 0, state -> com.geckolib.animation.object.PlayState.STOP);
            for (var clip : java.util.List.of("attack", "secondary", "utility", "summon", "dismiss", "recover", "time_stop"))
                main.triggerableAnim(clip, RawAnimation.begin().thenPlay(prefix + clip));
            controllers.add(main);
            if (kind.get() == PartThreeStand.SILVER_CHARIOT)
                controllers.add(new AnimationController<TrainingStandEntity>("armor", state -> state.setAndContinue(
                        RawAnimation.begin().thenLoop(prefix + (entityData.get(ARMOR_RELEASED) ? "armor_off" : "armor_on")))));
            return;
        }
        if (isHierophantGreen()) {
            controllers.add(new AnimationController<TrainingStandEntity>("locomotion", 3, state -> state.setAndContinue(
                    entityData.get(MOVING) ? HIEROPHANT_MOVE : HIEROPHANT_IDLE)));
            controllers.add(new AnimationController<TrainingStandEntity>("main", 0, state -> com.geckolib.animation.object.PlayState.STOP)
                    .triggerableAnim("attack", RawAnimation.begin().thenPlay("animation.hierophant_green.splash"))
                    .triggerableAnim("summon", RawAnimation.begin().thenPlay("animation.hierophant_green.summon"))
                    .triggerableAnim("dismiss", RawAnimation.begin().thenPlay("animation.hierophant_green.dismiss"))
                    .triggerableAnim("recover", RawAnimation.begin().thenPlay("animation.hierophant_green.recover")));
            return;
        }
        if (!isStarPlatinum()) {
            controllers.add(new AnimationController<TrainingStandEntity>("main", 2, state -> state.setAndContinue(IDLE))
                    .triggerableAnim("attack", ATTACK));
            return;
        }
        controllers.add(new AnimationController<TrainingStandEntity>("locomotion", 3, state -> state.setAndContinue(
                starAnimation(entityData.get(GUARDING) ? "guard" : entityData.get(MOVING) ? "move" : "idle", true))));
        controllers.add(new AnimationController<TrainingStandEntity>("main", 0, state -> com.geckolib.animation.object.PlayState.STOP)
                .triggerableAnim("attack", starAnimation("strike", false))
                .triggerableAnim("barrage", starAnimation("barrage", false))
                .triggerableAnim("star_finger", starAnimation("star_finger", false))
                .triggerableAnim("intercept", starAnimation("intercept", false))
                .triggerableAnim("intercept_catch", starAnimation("intercept_catch", false))
                .triggerableAnim("leap", starAnimation("leap", false))
                .triggerableAnim("leap_land", starAnimation("leap_land", false))
                .triggerableAnim("time_stop", starAnimation("time_stop", false))
                .triggerableAnim("summon", starAnimation("summon", false))
                .triggerableAnim("dismiss", starAnimation("dismiss", false))
                .triggerableAnim("recover", starAnimation("recover", false)));
    }

    private static RawAnimation starAnimation(String name, boolean loop) {
        if (loop) return switch (name) {
            case "idle" -> STAR_IDLE;
            case "move" -> STAR_MOVE;
            case "guard" -> STAR_GUARD;
            default -> throw new IllegalArgumentException("Unknown looping Star Platinum animation: " + name);
        };
        String key = "animation.star_platinum." + name;
        return RawAnimation.begin().thenPlay(key);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animationCache;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        // This prototype has no health pool; configurable damage reflection belongs to a later phase.
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        // The entity type uses noSave(); only the player's assignment survives world saves.
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        // Never retain a manifested entity or a stale owner reference across a restart.
    }
}
