package io.github.bg7427941.jojosmod.stands;

import io.github.bg7427941.jojosmod.registry.ModAttachments;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/** Server-owned storage; gameplay controllers validate IDs against registered Stand definitions. */
public final class PlayerStands {
    private PlayerStands() {}

    /** Reads the server value, or the owner's synchronized client copy, without creating data. */
    public static StandData get(Player player) {
        Objects.requireNonNull(player, "player");
        return player.getExistingData(ModAttachments.STAND_DATA).orElse(StandData.EMPTY);
    }

    /** Replaces the single assignment on the server thread and synchronizes it to its owner. */
    public static void assign(ServerPlayer player, Identifier standId) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(standId, "standId");
        if (get(player).standId().filter(standId::equals).isEmpty()) StandController.dismiss(player);
        player.setData(ModAttachments.STAND_DATA, new StandData(Optional.of(standId)));
        StarPlatinumProgress.onAssigned(player);
        HierophantProgress.onAssigned(player);
        CrusaderProgress.onAssigned(player);
    }

    /** Clears the assignment on the server thread and synchronizes the empty value. */
    public static void clear(ServerPlayer player) {
        Objects.requireNonNull(player, "player");
        StandController.dismiss(player);
        player.setData(ModAttachments.STAND_DATA, StandData.EMPTY);
    }
}
