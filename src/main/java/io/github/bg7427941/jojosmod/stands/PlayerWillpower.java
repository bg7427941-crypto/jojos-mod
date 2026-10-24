package io.github.bg7427941.jojosmod.stands;

import io.github.bg7427941.jojosmod.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class PlayerWillpower {
    private PlayerWillpower() {}

    /** Existing players start at one without requiring a save migration. */
    public static int get(Player player) {
        return player.getExistingData(ModAttachments.WILLPOWER).orElse(WillpowerData.INITIAL).value();
    }

    public static void set(ServerPlayer player, int value) {
        player.setData(ModAttachments.WILLPOWER, new WillpowerData(value));
    }
}
