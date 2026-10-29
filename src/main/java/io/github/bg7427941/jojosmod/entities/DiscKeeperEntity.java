package io.github.bg7427941.jojosmod.entities;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;
import io.github.bg7427941.jojosmod.registry.ModItems;
import io.github.bg7427941.jojosmod.stands.StandStorage;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.LinearInterpolationHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/** Persistent neutral service NPC at desert temples; its separate DIO challenge is voluntary. */
public final class DiscKeeperEntity extends Entity implements GeoEntity {
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.disc_keeper.idle");
    private java.util.Optional<net.minecraft.core.BlockPos> temple = java.util.Optional.empty();
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);

    public DiscKeeperEntity(EntityType<? extends DiscKeeperEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
        setCustomName(Component.translatable("entity.jojosmod.disc_keeper"));
        setCustomNameVisible(true);
    }

    public void setTemple(net.minecraft.core.BlockPos position) { temple=java.util.Optional.of(position.immutable()); }
    public java.util.Optional<net.minecraft.core.BlockPos> temple() { return temple; }

    @Override
    public boolean isPickable() {
        return isAlive();
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 location) {
        // Select the player's offhand blank before a usable main-hand item can consume the interaction.
        if (hand == InteractionHand.MAIN_HAND && !player.getMainHandItem().is(ModItems.EMPTY_STAND_DISC.get())
                && player.getOffhandItem().is(ModItems.EMPTY_STAND_DISC.get())) {
            hand = InteractionHand.OFF_HAND;
        }
        if (temple.isPresent() && player instanceof ServerPlayer serverPlayer && !player.getItemInHand(hand).is(ModItems.EMPTY_STAND_DISC.get())) {
            if (serverPlayer.distanceToSqr(this)>9 || !serverPlayer.hasLineOfSight(this)) return InteractionResult.FAIL;
            if (serverPlayer.isShiftKeyDown()) io.github.bg7427941.jojosmod.journey.StardustJourney.challenge(serverPlayer,this);
            else io.github.bg7427941.jojosmod.journey.StardustJourney.meet(serverPlayer,this);
            return InteractionResult.SUCCESS_SERVER;
        }
        return player instanceof ServerPlayer serverPlayer
                ? StandStorage.storeAtKeeper(serverPlayer, this, hand) : InteractionResult.SUCCESS_SERVER;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {}

    @Override
    protected InterpolationHandler createInterpolationHandler() {
        return LinearInterpolationHandler.create(this);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        // This service prototype cannot be defeated for rewards; /kill still removes it.
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        temple = input.read("Temple", net.minecraft.core.BlockPos.CODEC);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) { temple.ifPresent(pos -> output.store("Temple",net.minecraft.core.BlockPos.CODEC,pos)); }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<DiscKeeperEntity>("main", state -> state.setAndContinue(IDLE)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animationCache;
    }
}
