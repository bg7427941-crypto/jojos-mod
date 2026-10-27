package io.github.bg7427941.jojosmod.stands;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.bg7427941.jojosmod.registry.ModAttachments;
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

/** Bounded per-Stand learning, preserved through death/storage and synchronized only to its owner. */
public record CrusaderProgress(Training chariot, Training red, Training purple, Training fool, Training world) {
    public static final int GOAL = 3;
    public record Training(boolean awakened, List<UUID> targets) {
        public static final Training EMPTY = new Training(false, List.of());
        private static final Codec<List<UUID>> TARGETS = UUIDUtil.CODEC.listOf(0, GOAL).validate(list ->
                new HashSet<>(list).size() == list.size() ? com.mojang.serialization.DataResult.success(list)
                        : com.mojang.serialization.DataResult.error(() -> "Duplicate mastery target"));
        public static final Codec<Training> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.BOOL.optionalFieldOf("awakened", false).forGetter(Training::awakened),
                TARGETS.optionalFieldOf("targets", List.of()).forGetter(Training::targets)
        ).apply(i, Training::new));
        public static final StreamCodec<ByteBuf, Training> STREAM = StreamCodec.composite(
                ByteBufCodecs.BOOL, Training::awakened,
                UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs.list(GOAL)), Training::targets, Training::new);
        public Training {
            if (targets.size() > GOAL || targets.stream().anyMatch(java.util.Objects::isNull) || new HashSet<>(targets).size() != targets.size())
                throw new IllegalArgumentException("Expected at most three distinct mastery targets");
            targets = List.copyOf(targets);
        }
        public boolean completed() { return targets.size() == GOAL; }
    }
    public static final CrusaderProgress EMPTY = new CrusaderProgress(Training.EMPTY, Training.EMPTY, Training.EMPTY, Training.EMPTY, Training.EMPTY);
    public static final MapCodec<CrusaderProgress> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Training.CODEC.optionalFieldOf("silver_chariot", Training.EMPTY).forGetter(CrusaderProgress::chariot),
            Training.CODEC.optionalFieldOf("magicians_red", Training.EMPTY).forGetter(CrusaderProgress::red),
            Training.CODEC.optionalFieldOf("hermit_purple", Training.EMPTY).forGetter(CrusaderProgress::purple),
            Training.CODEC.optionalFieldOf("the_fool", Training.EMPTY).forGetter(CrusaderProgress::fool),
            Training.CODEC.optionalFieldOf("the_world", Training.EMPTY).forGetter(CrusaderProgress::world)
    ).apply(i, CrusaderProgress::new));
    public static final StreamCodec<ByteBuf, CrusaderProgress> STREAM = StreamCodec.composite(
            Training.STREAM, CrusaderProgress::chariot, Training.STREAM, CrusaderProgress::red,
            Training.STREAM, CrusaderProgress::purple, Training.STREAM, CrusaderProgress::fool,
            Training.STREAM, CrusaderProgress::world, CrusaderProgress::new);
    public Training training(PartThreeStand kind) { return switch (kind) {
        case SILVER_CHARIOT -> chariot; case MAGICIANS_RED -> red; case HERMIT_PURPLE -> purple;
        case THE_FOOL -> fool; case THE_WORLD -> world;
    }; }
    public CrusaderProgress with(PartThreeStand kind, Training t) { return new CrusaderProgress(
            kind == PartThreeStand.SILVER_CHARIOT ? t : chariot, kind == PartThreeStand.MAGICIANS_RED ? t : red,
            kind == PartThreeStand.HERMIT_PURPLE ? t : purple, kind == PartThreeStand.THE_FOOL ? t : fool,
            kind == PartThreeStand.THE_WORLD ? t : world); }
    public static CrusaderProgress get(Player player) { return player.getExistingData(ModAttachments.CRUSADER_PROGRESS).orElse(EMPTY); }
    public static void onAssigned(ServerPlayer player) {
        PartThreeStand.of(player).ifPresent(kind -> {
            var p = get(player); var t = p.training(kind);
            if (t.awakened()) return;
            player.setData(ModAttachments.CRUSADER_PROGRESS, p.with(kind, new Training(true, t.targets())));
            PlayerWillpower.set(player, Math.min(WillpowerData.MAX_VALUE, PlayerWillpower.get(player) + 2));
            player.sendSystemMessage(Component.translatable("message.jojosmod.crusader.awakened",
                    Component.translatable(kind.translationKey())), false);
        });
    }
    public static void hit(ServerPlayer player, LivingEntity target) {
        if (!(target instanceof Enemy) || player.getAbilities().instabuild || player.isSpectator()
                || !player.isAlive() || player.isRemoved() || player.level() != target.level() || player.isAlliedTo(target)) return;
        PartThreeStand.of(player).ifPresent(kind -> {
            var p = get(player); var t = p.training(kind);
            if (t.completed() || t.targets().contains(target.getUUID())) return;
            var targets = new ArrayList<>(t.targets()); targets.add(target.getUUID());
            var next = new Training(t.awakened(), targets);
            player.setData(ModAttachments.CRUSADER_PROGRESS, p.with(kind, next));
            int before = PlayerWillpower.get(player);
            if (next.completed()) PlayerWillpower.set(player, Math.min(WillpowerData.MAX_VALUE, before + 1));
            int reward = PlayerWillpower.get(player) - before;
            player.sendSystemMessage(Component.translatable(next.completed() ? "message.jojosmod.crusader.mastered"
                    : "message.jojosmod.crusader.progress", targets.size(), GOAL, reward), true);
        });
    }
}
