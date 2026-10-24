package io.github.bg7427941.jojosmod.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import io.github.bg7427941.jojosmod.registry.ModStands;
import io.github.bg7427941.jojosmod.stands.PlayerStands;
import io.github.bg7427941.jojosmod.stands.StandController;
import io.github.bg7427941.jojosmod.stands.PlayerWillpower;
import io.github.bg7427941.jojosmod.stands.WillpowerData;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** Operator-only testing hooks for assignment and provisional progression. */
public final class StandTestCommand {
    private StandTestCommand() {}

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("standtest")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("assign").executes(context -> {
                    var player = context.getSource().getPlayerOrException();
                    StandController.dismiss(player);
                    PlayerStands.assign(player, ModStands.TRAINING_STAND.id());
                    context.getSource().sendSuccess(() -> Component.translatable(
                            "command.jojosmod.training_assigned", Component.keybind("key.jojosmod.toggle_stand")), false);
                    return 1;
                }))
                .then(Commands.literal("willpower")
                        .executes(context -> {
                            int value = PlayerWillpower.get(context.getSource().getPlayerOrException());
                            context.getSource().sendSuccess(() -> Component.translatable(
                                    "command.jojosmod.willpower", value), false);
                            return value;
                        })
                        .then(Commands.argument("value", IntegerArgumentType.integer(0, WillpowerData.MAX_VALUE))
                                .executes(context -> {
                                    int value = IntegerArgumentType.getInteger(context, "value");
                                    PlayerWillpower.set(context.getSource().getPlayerOrException(), value);
                                    context.getSource().sendSuccess(() -> Component.translatable(
                                            "command.jojosmod.willpower", value), false);
                                    return 1;
                                })))
                .then(Commands.literal("clear").executes(context -> {
                    var player = context.getSource().getPlayerOrException();
                    StandController.dismiss(player);
                    PlayerStands.clear(player);
                    context.getSource().sendSuccess(() -> Component.translatable("command.jojosmod.stand_cleared"), false);
                    return 1;
                })));
    }
}
