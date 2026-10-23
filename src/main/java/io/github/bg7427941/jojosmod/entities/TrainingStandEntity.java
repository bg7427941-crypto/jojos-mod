package io.github.bg7427941.jojosmod.entities;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;
import io.github.bg7427941.jojosmod.registry.ModStands;
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

/** Shared placeholder manifestation; the player's attachment owns the persistent assignment. */
public final class TrainingStandEntity extends Entity implements GeoEntity {
    private static final EntityDataAccessor<Integer> OWNER_ID =
            SynchedEntityData.defineId(TrainingStandEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> STAND_ID =
            SynchedEntityData.defineId(TrainingStandEntity.class, EntityDataSerializers.STRING);
    private static final double FOLLOW_BEHIND = 1.25;
    private static final double FOLLOW_SIDE = 0.9;
    private static final double FOLLOW_HEIGHT = 0.15;
    private static final double FOLLOW_SMOOTHING = 0.35;
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.training_stand.idle");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.training_stand.attack");

    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private @Nullable ServerPlayer owner;

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

        var player = getOwner();
        if (player == null || player.isRemoved() || !player.isAlive() || player.isSpectator()
                || player.level() != level() || !StandController.isActive(player, this)) {
            discard();
            return;
        }

        double yaw = Math.toRadians(player.getYRot());
        var forward = new Vec3(-Math.sin(yaw), 0, Math.cos(yaw));
        var side = new Vec3(Math.cos(yaw), 0, Math.sin(yaw));
        var target = player.position()
                .add(forward.scale(-FOLLOW_BEHIND))
                .add(side.scale(FOLLOW_SIDE))
                .add(0, FOLLOW_HEIGHT, 0);
        double maxDistance = getStandDefinition().stats().maxOwnerDistance();

        // Snap after teleports so this temporary entity cannot remain separated from its owner.
        setPos(distanceToSqr(player) > maxDistance * maxDistance
                ? target : position().lerp(target, FOLLOW_SMOOTHING));
        setYRot(player.getYRot());
        setXRot(0);
        setDeltaMovement(Vec3.ZERO);
    }

    /** Called by the server after it accepts the basic-attack request. */
    public void playAttackAnimation() {
        triggerAnim("main", "attack");
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<TrainingStandEntity>(
                "main", animation -> animation.setAndContinue(IDLE))
                .triggerableAnim("attack", ATTACK));
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
