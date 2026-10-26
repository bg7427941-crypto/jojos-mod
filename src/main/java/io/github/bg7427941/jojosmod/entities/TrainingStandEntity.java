package io.github.bg7427941.jojosmod.entities;

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
    private static final double FOLLOW_BEHIND = 1.25;
    private static final double FOLLOW_SIDE = 0.9;
    private static final double FOLLOW_HEIGHT = 0.15;
    private static final double FOLLOW_SMOOTHING = 0.35;
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.training_stand.idle");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.training_stand.attack");
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
        boolean acting = isStarPlatinum() && StarPlatinumActions.isBusy(player);
        var target = player.position()
                .add(forward.scale(acting ? 0.45 : -FOLLOW_BEHIND))
                .add(side.scale(FOLLOW_SIDE))
                .add(0, FOLLOW_HEIGHT, 0);
        double maxDistance = getStandDefinition().combat().tetherDistance();

        // Snap after teleports so this temporary entity cannot remain separated from its owner.
        setPos(distanceToSqr(player) > maxDistance * maxDistance
                ? target : position().lerp(target, FOLLOW_SMOOTHING));
        setYRot(player.getYRot());
        setXRot(0);
        setDeltaMovement(Vec3.ZERO);
        if (isStarPlatinum()) {
            entityData.set(MOVING, player.getDeltaMovement().horizontalDistanceSqr() > 0.001);
            StarPlatinumEffects.aura(this, level().getGameTime());
        }
    }

    /** Called by the server after it accepts the basic-attack request. */
    public void playAttackAnimation() {
        triggerAnim("main", "attack");
    }

    public boolean isStarPlatinum() {
        return getType() == ModEntities.STAR_PLATINUM.get();
    }

    public boolean isDismissing() {
        return entityData.get(DISMISSING);
    }

    public void playSummonAnimation() {
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

    public void setGuarding(boolean guarding) {
        if (guarding) {
            stopTriggeredAnim("main", "summon");
            stopTriggeredAnim("main", "recover");
        }
        entityData.set(GUARDING, guarding);
    }

    public void stopActionAnimation() {
        setGuarding(false);
        stopTriggeredAnim("main", "attack");
        stopTriggeredAnim("main", "barrage");
        stopTriggeredAnim("main", "star_finger");
        stopTriggeredAnim("main", "intercept");
        stopTriggeredAnim("main", "intercept_catch");
        if (isStarPlatinum()) triggerAnim("main", "recover");
    }

    /** Clears gameplay immediately while giving Star Platinum four ticks to disappear visually. */
    public void dismiss() {
        if (!isStarPlatinum()) {
            discard();
            return;
        }
        entityData.set(DISMISSING, true);
        dismissAt = level().getGameTime() + 4;
        triggerAnim("main", "dismiss");
        if (level() instanceof ServerLevel server) {
            server.playSound(null, blockPosition(), ModSounds.STAR_DISMISS.get(), SoundSource.PLAYERS, 0.45F, 1.1F);
            StarPlatinumEffects.dismiss(this);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        if (!isStarPlatinum()) {
            controllers.add(new AnimationController<TrainingStandEntity>("main", state -> state.setAndContinue(IDLE))
                    .triggerableAnim("attack", ATTACK));
            return;
        }
        controllers.add(new AnimationController<TrainingStandEntity>("main", state -> state.setAndContinue(
                starAnimation(entityData.get(GUARDING) ? "guard" : entityData.get(MOVING) ? "move" : "idle", true)))
                .triggerableAnim("attack", starAnimation("strike", false))
                .triggerableAnim("barrage", starAnimation("barrage", false))
                .triggerableAnim("star_finger", starAnimation("star_finger", false))
                .triggerableAnim("intercept", starAnimation("intercept", false))
                .triggerableAnim("intercept_catch", starAnimation("intercept_catch", false))
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
