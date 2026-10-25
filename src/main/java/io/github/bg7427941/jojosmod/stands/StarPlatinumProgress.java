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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;

/** Persistent, bounded mastery milestones; repeating the same opponent cannot farm willpower. */
public record StarPlatinumProgress(boolean awakened, List<UUID> preciseTargets,
        List<UUID> barrageTargets, List<UUID> guardTargets) {
    public static final int TARGETS_REQUIRED = 3;
    public static final int STAR_FINGER_REQUIREMENT = 5;
    public static final StarPlatinumProgress EMPTY = new StarPlatinumProgress(false);
    private static final Codec<List<UUID>> TARGETS_CODEC = UUIDUtil.CODEC.listOf(0, TARGETS_REQUIRED).validate(targets ->
            validTargets(targets) ? DataResult.success(targets)
                    : DataResult.error(() -> "A mastery milestone holds at most three distinct target UUIDs"));
    private static final StreamCodec<ByteBuf, List<UUID>> TARGETS_STREAM_CODEC =
            UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs.list(TARGETS_REQUIRED));

    // All new fields are optional so the original {awakened: true} data retains its reward state.
    public static final MapCodec<StarPlatinumProgress> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("awakened", false).forGetter(StarPlatinumProgress::awakened),
            TARGETS_CODEC.optionalFieldOf("precise_targets", List.of()).forGetter(StarPlatinumProgress::preciseTargets),
            TARGETS_CODEC.optionalFieldOf("barrage_targets", List.of()).forGetter(StarPlatinumProgress::barrageTargets),
            TARGETS_CODEC.optionalFieldOf("guard_targets", List.of()).forGetter(StarPlatinumProgress::guardTargets)
    ).apply(instance, StarPlatinumProgress::new));
    public static final StreamCodec<ByteBuf, StarPlatinumProgress> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, StarPlatinumProgress::awakened,
            TARGETS_STREAM_CODEC, StarPlatinumProgress::preciseTargets,
            TARGETS_STREAM_CODEC, StarPlatinumProgress::barrageTargets,
            TARGETS_STREAM_CODEC, StarPlatinumProgress::guardTargets,
            StarPlatinumProgress::new);

    public enum Milestone {
        PRECISE("precise"), BARRAGE("barrage"), GUARD("guard");

        private final String key;
        Milestone(String key) { this.key = key; }
        public String translationKey() { return "mastery.jojosmod.star." + key; }
    }

    /** Source-compatible constructor for the original awakening-only attachment. */
    public StarPlatinumProgress(boolean awakened) {
        this(awakened, List.of(), List.of(), List.of());
    }

    public StarPlatinumProgress {
        if (!validTargets(preciseTargets) || !validTargets(barrageTargets) || !validTargets(guardTargets)) {
            throw new IllegalArgumentException("Mastery targets must contain at most three distinct UUIDs");
        }
        preciseTargets = List.copyOf(preciseTargets);
        barrageTargets = List.copyOf(barrageTargets);
        guardTargets = List.copyOf(guardTargets);
    }

    private static boolean validTargets(List<UUID> targets) {
        if (targets == null || targets.size() > TARGETS_REQUIRED) return false;
        for (var target : targets) if (target == null) return false;
        return new HashSet<>(targets).size() == targets.size();
    }

    public static StarPlatinumProgress get(Player player) {
        return player.getExistingData(ModAttachments.STAR_PROGRESS).orElse(EMPTY);
    }

    public List<UUID> targets(Milestone milestone) {
        return switch (milestone) {
            case PRECISE -> preciseTargets;
            case BARRAGE -> barrageTargets;
            case GUARD -> guardTargets;
        };
    }

    public int count(Milestone milestone) { return targets(milestone).size(); }

    public boolean completed(Milestone milestone) { return count(milestone) == TARGETS_REQUIRED; }

    public static boolean starFingerUnlocked(Player player) {
        return PlayerWillpower.get(player) >= STAR_FINGER_REQUIREMENT;
    }

    public static void onAssigned(ServerPlayer player) {
        var current = get(player);
        if (!hasStarPlatinum(player) || current.awakened()) return;
        var awakened = new StarPlatinumProgress(true, current.preciseTargets(), current.barrageTargets(), current.guardTargets());
        int awarded = grant(player, awakened, 2);
        player.sendSystemMessage(Component.translatable("message.jojosmod.star.awakened", awarded,
                Component.keybind("key.jojosmod.toggle_stand"), Component.keybind("key.jojosmod.basic_attack"),
                Component.keybind("key.jojosmod.star_barrage"), Component.keybind("key.jojosmod.star_guard")), false);
    }

    /** Call only after the precise strike has dealt real health damage. A fatal hit also counts. */
    public static void onPreciseHit(ServerPlayer player, LivingEntity target) {
        record(player, target, Milestone.PRECISE);
    }

    /** Each opponent counts once, regardless of how many barrage pulses hit it. */
    public static void onBarrageHit(ServerPlayer player, LivingEntity target) {
        record(player, target, Milestone.BARRAGE);
    }

    /** Call only when a frontal guard actually reduces positive incoming damage. */
    public static void onGuardBlock(ServerPlayer player, DamageSource source) {
        if (source.getEntity() instanceof LivingEntity attacker) record(player, attacker, Milestone.GUARD);
    }

    private static boolean hasStarPlatinum(Player player) {
        return PlayerStands.get(player).standId().filter(ModStands.STAR_PLATINUM.id()::equals).isPresent();
    }

    private static void record(ServerPlayer player, LivingEntity target, Milestone milestone) {
        if (!player.isAlive() || player.isRemoved() || player.isSpectator() || player.getAbilities().instabuild
                || !hasStarPlatinum(player) || !(target instanceof Enemy) || target.level() != player.level()
                || player.isAlliedTo(target)) return;
        var current = get(player);
        var targets = current.targets(milestone);
        if (current.completed(milestone) || targets.contains(target.getUUID())) return;

        var updatedTargets = new ArrayList<>(targets);
        updatedTargets.add(target.getUUID());
        var updated = new StarPlatinumProgress(current.awakened(),
                milestone == Milestone.PRECISE ? updatedTargets : current.preciseTargets(),
                milestone == Milestone.BARRAGE ? updatedTargets : current.barrageTargets(),
                milestone == Milestone.GUARD ? updatedTargets : current.guardTargets());
        var name = Component.translatable(milestone.translationKey());
        if (updated.completed(milestone)) {
            int awarded = grant(player, updated, 1);
            player.sendSystemMessage(Component.translatable("message.jojosmod.mastery.completed",
                    name, awarded, PlayerWillpower.get(player)), false);
        } else {
            player.setData(ModAttachments.STAR_PROGRESS, updated);
            player.sendSystemMessage(Component.translatable("message.jojosmod.mastery.progress",
                    name, updated.count(milestone), TARGETS_REQUIRED), true);
        }
    }

    /** Store completion before awarding; all callers run once on the logical server thread. */
    private static int grant(ServerPlayer player, StarPlatinumProgress progress, int amount) {
        int before = PlayerWillpower.get(player);
        int after = Math.min(WillpowerData.MAX_VALUE, before + amount);
        player.setData(ModAttachments.STAR_PROGRESS, progress);
        PlayerWillpower.set(player, after);
        if (before < STAR_FINGER_REQUIREMENT && after >= STAR_FINGER_REQUIREMENT) {
            player.sendSystemMessage(Component.translatable("message.jojosmod.mastery.star_finger_unlocked",
                    Component.keybind("key.jojosmod.star_finger")), false);
        }
        return after - before;
    }
}
