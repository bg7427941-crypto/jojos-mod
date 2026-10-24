package io.github.bg7427941.jojosmod.stands;

import io.github.bg7427941.jojosmod.entities.DiscKeeperEntity;
import io.github.bg7427941.jojosmod.items.StandDiscItem;
import io.github.bg7427941.jojosmod.registry.ModItems;
import io.github.bg7427941.jojosmod.registry.ModStands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;

/** One synchronous server-thread transfer of the caller's own Stand into the held blank disc. */
public final class StandStorage {
    public static final double SERVICE_RANGE = 3.0;

    private StandStorage() {}

    public static InteractionResult storeAtKeeper(ServerPlayer player, DiscKeeperEntity keeper, InteractionHand hand) {
        if (!player.isAlive() || player.isRemoved() || player.isSpectator() || !keeper.isAlive()) {
            return InteractionResult.FAIL;
        }
        if (player.level() != keeper.level() || player.distanceToSqr(keeper) > SERVICE_RANGE * SERVICE_RANGE
                || !player.hasLineOfSight(keeper)) {
            player.sendSystemMessage(Component.translatable("message.jojosmod.storage.out_of_reach"), true);
            return InteractionResult.FAIL;
        }
        var blank = player.getItemInHand(hand);
        // Replacing a malformed multi-item stack would otherwise silently destroy extra blanks.
        if (!blank.is(ModItems.EMPTY_STAND_DISC.get()) || blank.getCount() != 1) {
            player.sendSystemMessage(Component.translatable("message.jojosmod.storage.instructions"), false);
            return InteractionResult.FAIL;
        }
        if (!player.isShiftKeyDown()) {
            player.sendSystemMessage(Component.translatable("message.jojosmod.storage.confirm"), false);
            return InteractionResult.FAIL;
        }
        var id = PlayerStands.get(player).standId();
        if (id.isEmpty()) {
            player.sendSystemMessage(Component.translatable("message.jojosmod.storage.no_stand"), true);
            return InteractionResult.FAIL;
        }
        var stand = ModStands.find(id.get()).orElse(null);
        if (stand == null) {
            player.sendSystemMessage(Component.translatable("message.jojosmod.storage.unknown_stand"), true);
            return InteractionResult.FAIL;
        }

        var filled = StandDiscItem.containing(stand);
        // Validate everything before mutation. The same player save owns both the slot and assignment.
        // Replace the blank in place, including in creative: no inventory insertion or dropped output.
        StandController.dismiss(player);
        PlayerStands.clear(player);
        player.setItemInHand(hand, filled);
        player.containerMenu.broadcastChanges();
        player.sendSystemMessage(Component.translatable("message.jojosmod.storage.stored",
                Component.translatable(stand.translationKey())), false);
        return InteractionResult.SUCCESS_SERVER;
    }
}
