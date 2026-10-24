package io.github.bg7427941.jojosmod.items;

import io.github.bg7427941.jojosmod.registry.ModDataComponents;
import io.github.bg7427941.jojosmod.registry.ModItems;
import io.github.bg7427941.jojosmod.registry.ModStands;
import io.github.bg7427941.jojosmod.stands.PlayerStands;
import io.github.bg7427941.jojosmod.stands.PlayerWillpower;
import io.github.bg7427941.jojosmod.stands.StandDefinition;
import io.github.bg7427941.jojosmod.stands.StandProgressionConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** The server reads the held disc; no client-supplied Stand or willpower is accepted. */
public final class StandDiscItem extends Item {
    public StandDiscItem(Properties properties) {
        super(properties);
    }

    public static ItemStack containing(StandDefinition stand) {
        var stack = new ItemStack(ModItems.STAND_DISC.get());
        stack.set(ModDataComponents.STAND_ID.get(), stand.id());
        return stack;
    }

    @Override
    public Component getName(ItemStack stack) {
        var id = stack.get(ModDataComponents.STAND_ID.get());
        if (id == null) {
            return super.getName(stack);
        }
        var name = ModStands.find(id).map(stand -> Component.translatable(stand.translationKey()))
                .orElseGet(() -> Component.literal(id.toString()));
        return Component.translatable("item.jojosmod.stand_disc.filled", name);
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
        if (PlayerStands.get(serverPlayer).standId().isPresent()) {
            serverPlayer.sendSystemMessage(Component.translatable("message.jojosmod.disc.already_has_stand"), true);
            return InteractionResult.FAIL;
        }
        var id = stack.get(ModDataComponents.STAND_ID.get());
        var stand = id == null ? null : ModStands.find(id).orElse(null);
        // Old placeholder discs and unknown IDs remain intact, including their saved identity.
        if (stand == null) {
            serverPlayer.sendSystemMessage(Component.translatable("message.jojosmod.disc.invalid"), true);
            return InteractionResult.FAIL;
        }
        int required = StandProgressionConfig.requiredWillpower(stand);
        if (PlayerWillpower.get(serverPlayer) < required) {
            serverPlayer.sendSystemMessage(Component.translatable("message.jojosmod.disc.insufficient_willpower",
                    required, PlayerWillpower.get(serverPlayer)), true);
            return InteractionResult.FAIL;
        }

        PlayerStands.assign(serverPlayer, stand.id());
        stack.consume(1, serverPlayer);
        serverPlayer.awardStat(Stats.ITEM_USED.get(this));
        serverPlayer.sendSystemMessage(Component.translatable("message.jojosmod.stand_obtained",
                Component.translatable(stand.translationKey()), Component.keybind("key.jojosmod.toggle_stand")), false);
        return InteractionResult.SUCCESS_SERVER;
    }
}
