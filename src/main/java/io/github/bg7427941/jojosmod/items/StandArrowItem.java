package io.github.bg7427941.jojosmod.items;

import io.github.bg7427941.jojosmod.registry.ModStands;
import io.github.bg7427941.jojosmod.stands.PlayerStands;
import io.github.bg7427941.jojosmod.stands.StandDefinition;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/** The normal use-item packet supplies only the hand; the server owns the roll and consumption. */
public final class StandArrowItem extends Item {
    private final List<StandDefinition> pool;

    public StandArrowItem(Properties properties) {
        this(properties, ModStands.ARROW_POOL);
    }

    public StandArrowItem(Properties properties, List<StandDefinition> pool) {
        super(properties);
        if (pool.isEmpty()) throw new IllegalArgumentException("Arrow pool cannot be empty");
        this.pool = List.copyOf(pool);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS_SERVER;
        }
        var stack = serverPlayer.getItemInHand(hand);
        if (!serverPlayer.isAlive() || serverPlayer.isRemoved() || serverPlayer.isSpectator()
                || stack.isEmpty() || !stack.is(this)) {
            return InteractionResult.FAIL;
        }
        // Preserve even an unknown saved ID: a missing definition must never enable a free reroll.
        if (PlayerStands.get(serverPlayer).standId().isPresent()) {
            serverPlayer.sendSystemMessage(Component.translatable("message.jojosmod.already_has_stand"), true);
            return InteractionResult.FAIL;
        }

        var stand = pool.get(serverPlayer.getRandom().nextInt(pool.size()));
        PlayerStands.assign(serverPlayer, stand.id());
        stack.consume(1, serverPlayer);
        serverPlayer.awardStat(Stats.ITEM_USED.get(this));
        serverPlayer.sendSystemMessage(Component.translatable("message.jojosmod.stand_obtained",
                Component.translatable(stand.translationKey()), Component.keybind("key.jojosmod.toggle_stand")), false);
        return InteractionResult.SUCCESS_SERVER;
    }
}
