package io.github.bg7427941.jojosmod.commands;

import io.github.bg7427941.jojosmod.registry.ModStands;
import io.github.bg7427941.jojosmod.stands.PlayerStands;
import io.github.bg7427941.jojosmod.stands.PlayerWillpower;
import io.github.bg7427941.jojosmod.stands.StarPlatinumProgress;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** Player-facing, read-only progression lookup; available without operator privileges. */
public final class StandProgressCommand {
    private StandProgressCommand() {}

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("stand")
                .executes(context -> status(context.getSource()))
                .then(Commands.literal("progress").executes(context -> status(context.getSource()))));
    }

    private static int status(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var player = source.getPlayerOrException();
        var assignment = PlayerStands.get(player).standId();
        Component standName = assignment.<Component>map(id -> ModStands.find(id)
                .<Component>map(definition -> Component.translatable(definition.translationKey()))
                .orElseGet(() -> Component.literal(id.toString())))
                .orElseGet(() -> Component.translatable("command.jojosmod.stand.none"));
        source.sendSuccess(() -> Component.translatable("command.jojosmod.stand.status",
                standName, PlayerWillpower.get(player)), false);
        var progress = StarPlatinumProgress.get(player);
        if (!progress.awakened() && assignment.filter(ModStands.STAR_PLATINUM.id()::equals).isEmpty()) {
            source.sendSuccess(() -> Component.translatable("command.jojosmod.stand.star_mastery_hint"), false);
            return 1;
        }
        for (var milestone : StarPlatinumProgress.Milestone.values()) {
            source.sendSuccess(() -> Component.translatable("command.jojosmod.stand.mastery",
                    Component.translatable(milestone.translationKey()), progress.count(milestone),
                    StarPlatinumProgress.TARGETS_REQUIRED), false);
        }
        source.sendSuccess(() -> StarPlatinumProgress.starFingerUnlocked(player)
                ? Component.translatable("command.jojosmod.stand.star_finger_ready",
                        Component.keybind("key.jojosmod.star_finger"))
                : Component.translatable("command.jojosmod.stand.star_finger_locked",
                        StarPlatinumProgress.STAR_FINGER_REQUIREMENT), false);
        return 1;
    }
}
