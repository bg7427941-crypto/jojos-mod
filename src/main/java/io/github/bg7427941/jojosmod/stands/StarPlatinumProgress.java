package io.github.bg7427941.jojosmod.stands;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.bg7427941.jojosmod.registry.ModAttachments;
import io.github.bg7427941.jojosmod.registry.ModStands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** One-time awakening milestone saved alongside willpower, rather than farmable on each acquisition. */
public record StarPlatinumProgress(boolean awakened) {
    public static final StarPlatinumProgress EMPTY = new StarPlatinumProgress(false);
    public static final MapCodec<StarPlatinumProgress> CODEC = Codec.BOOL.optionalFieldOf("awakened", false)
            .xmap(StarPlatinumProgress::new, StarPlatinumProgress::awakened);

    public static void onAssigned(ServerPlayer player) {
        if (!PlayerStands.get(player).standId().filter(ModStands.STAR_PLATINUM.id()::equals).isPresent()
                || player.getExistingData(ModAttachments.STAR_PROGRESS).orElse(EMPTY).awakened()) {
            return;
        }
        int before = PlayerWillpower.get(player);
        int after = Math.min(WillpowerData.MAX_VALUE, before + 2);
        player.setData(ModAttachments.STAR_PROGRESS, new StarPlatinumProgress(true));
        PlayerWillpower.set(player, after);
        player.sendSystemMessage(Component.translatable("message.jojosmod.star.awakened", after - before,
                Component.keybind("key.jojosmod.toggle_stand"), Component.keybind("key.jojosmod.basic_attack"),
                Component.keybind("key.jojosmod.star_barrage"), Component.keybind("key.jojosmod.star_guard")), false);
    }
}
