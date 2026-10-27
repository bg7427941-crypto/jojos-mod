package io.github.bg7427941.jojosmod.stands;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.bg7427941.jojosmod.registry.ModAttachments;
import io.github.bg7427941.jojosmod.registry.ModStands;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;

/** Each user's successful health-damaging hits count independently, including shared enemies. */
public record HierophantProgress(boolean awakened, List<UUID> splashTargets) {
    public static final int TARGETS_REQUIRED = 3;
    public static final HierophantProgress EMPTY = new HierophantProgress(false, List.of());
    private static final Codec<List<UUID>> TARGETS_CODEC = UUIDUtil.CODEC.listOf(0, TARGETS_REQUIRED)
            .validate(list -> valid(list) ? DataResult.success(list)
                    : DataResult.error(() -> "Emerald Splash requires at most three distinct target UUIDs"));
    public static final MapCodec<HierophantProgress> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("awakened", false).forGetter(HierophantProgress::awakened),
            TARGETS_CODEC.optionalFieldOf("splash_targets", List.of()).forGetter(HierophantProgress::splashTargets)
    ).apply(instance, HierophantProgress::new));
    public static final StreamCodec<ByteBuf, HierophantProgress> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, HierophantProgress::awakened,
            UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs.list(TARGETS_REQUIRED)), HierophantProgress::splashTargets,
            HierophantProgress::new);

    public HierophantProgress {
        if (!valid(splashTargets)) throw new IllegalArgumentException("Invalid Emerald Splash mastery targets");
        splashTargets = List.copyOf(splashTargets);
    }

    private static boolean valid(List<UUID> list) {
        if (list == null || list.size() > TARGETS_REQUIRED) return false;
        for (var target : list) if (target == null) return false;
        return new HashSet<>(list).size() == list.size();
    }

    public static HierophantProgress get(Player player) {
        return player.getExistingData(ModAttachments.HIEROPHANT_PROGRESS).orElse(EMPTY);
    }

    public boolean completed() { return splashTargets.size() == TARGETS_REQUIRED; }

    private static boolean hasHierophant(Player player) {
        return PlayerStands.get(player).standId().filter(ModStands.HIEROPHANT_GREEN.id()::equals).isPresent();
    }

    public static void onAssigned(ServerPlayer player) {
        var current = get(player);
        if (!hasHierophant(player) || current.awakened()) return;
        int reward = grant(player, new HierophantProgress(true, current.splashTargets()), 2);
        player.sendSystemMessage(Component.translatable("message.jojosmod.hierophant.awakened", reward,
                Component.keybind("key.jojosmod.toggle_stand"), Component.keybind("key.jojosmod.basic_attack")), false);
    }

    /** Call only after an owned Emerald Splash shard has reduced the target's health. Fatal hits count. */
    public static void onSplashHit(ServerPlayer player, LivingEntity target) {
        if (!player.isAlive() || player.isRemoved() || player.isSpectator() || player.getAbilities().instabuild
                || !hasHierophant(player) || !(target instanceof Enemy) || target.level() != player.level()
                || player.isAlliedTo(target)) return;
        var current = get(player);
        if (current.completed() || current.splashTargets().contains(target.getUUID())) return;
        var targets = new ArrayList<>(current.splashTargets());
        targets.add(target.getUUID());
        var updated = new HierophantProgress(current.awakened(), targets);
        if (updated.completed()) {
            int reward = grant(player, updated, 1);
            player.sendSystemMessage(Component.translatable("message.jojosmod.hierophant.mastered", reward,
                    PlayerWillpower.get(player)), false);
        } else {
            player.setData(ModAttachments.HIEROPHANT_PROGRESS, updated);
            player.sendSystemMessage(Component.translatable("message.jojosmod.hierophant.progress",
                    targets.size(), TARGETS_REQUIRED), true);
        }
    }

    private static int grant(ServerPlayer player, HierophantProgress updated, int amount) {
        int before = PlayerWillpower.get(player);
        player.setData(ModAttachments.HIEROPHANT_PROGRESS, updated);
        PlayerWillpower.set(player, Math.min(WillpowerData.MAX_VALUE, before + amount));
        return PlayerWillpower.get(player) - before;
    }
}
