package io.github.bg7427941.jojosmod.commands;

import io.github.bg7427941.jojosmod.stands.PartThreeStand;
import io.github.bg7427941.jojosmod.stands.CrusaderProgress;
import io.github.bg7427941.jojosmod.registry.ModStands;
import io.github.bg7427941.jojosmod.stands.PartThreeStand;
import io.github.bg7427941.jojosmod.stands.CrusaderProgress;
import io.github.bg7427941.jojosmod.stands.PlayerStands;
import io.github.bg7427941.jojosmod.stands.PartThreeStand;
import io.github.bg7427941.jojosmod.stands.CrusaderProgress;
import io.github.bg7427941.jojosmod.stands.PlayerWillpower;
import io.github.bg7427941.jojosmod.stands.PartThreeStand;
import io.github.bg7427941.jojosmod.stands.CrusaderProgress;
import io.github.bg7427941.jojosmod.stands.StarPlatinumProgress;
import io.github.bg7427941.jojosmod.stands.PartThreeStand;
import io.github.bg7427941.jojosmod.stands.CrusaderProgress;
import io.github.bg7427941.jojosmod.stands.HierophantProgress;
import io.github.bg7427941.jojosmod.stands.PartThreeStand;
import io.github.bg7427941.jojosmod.stands.CrusaderProgress;
import net.minecraft.commands.CommandSourceStack;
import io.github.bg7427941.jojosmod.stands.PartThreeStand;
import io.github.bg7427941.jojosmod.stands.CrusaderProgress;
import net.minecraft.commands.Commands;
import io.github.bg7427941.jojosmod.stands.PartThreeStand;
import io.github.bg7427941.jojosmod.stands.CrusaderProgress;
import net.minecraft.network.chat.Component;
import io.github.bg7427941.jojosmod.stands.PartThreeStand;
import io.github.bg7427941.jojosmod.stands.CrusaderProgress;
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
        var kind = PartThreeStand.of(player);
        if (kind.isPresent()) {
            var k = kind.get();
            source.sendSuccess(() -> Component.translatable("hud.jojosmod.crusader.mastery", CrusaderProgress.get(player).training(k).targets().size(), CrusaderProgress.GOAL), false);
            source.sendSuccess(() -> Component.translatable("hud.jojosmod." + k.path() + ".primary", Component.keybind("key.jojosmod.basic_attack")), false);
            source.sendSuccess(() -> Component.translatable("hud.jojosmod." + k.path() + ".secondary", Component.keybind("key.jojosmod.star_barrage")), false);
            source.sendSuccess(() -> Component.translatable("hud.jojosmod." + k.path() + ".utility", Component.keybind("key.jojosmod.star_guard")), false);
            if (k == PartThreeStand.THE_WORLD) source.sendSuccess(() -> Component.translatable("hud.jojosmod.time", Component.keybind("key.jojosmod.time_stop"), 4), false);
            return 1;
        }
        var hierophant = HierophantProgress.get(player);
        boolean hasHierophant = assignment.filter(ModStands.HIEROPHANT_GREEN.id()::equals).isPresent();
        if (hasHierophant || hierophant.awakened()) {
            source.sendSuccess(() -> Component.translatable("command.jojosmod.hierophant.training",
                    hierophant.splashTargets().size(), HierophantProgress.TARGETS_REQUIRED), false);
            source.sendSuccess(() -> Component.translatable(hierophant.completed()
                    ? "command.jojosmod.hierophant.mastered" : "command.jojosmod.hierophant.splash",
                    Component.keybind("key.jojosmod.basic_attack")), false);
            if (hasHierophant) return 1;
        }
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
        source.sendSuccess(() -> Component.translatable("command.jojosmod.stand.finger_training"), false);
        source.sendSuccess(() -> Component.translatable("command.jojosmod.stand.intercept_training"), false);
        source.sendSuccess(() -> StarPlatinumProgress.starFingerUnlocked(player)
                ? Component.translatable(progress.completed(StarPlatinumProgress.Milestone.FINGER)
                        ? "command.jojosmod.stand.star_finger_mastered" : "command.jojosmod.stand.star_finger_ready",
                        Component.keybind("key.jojosmod.star_finger"))
                : Component.translatable("command.jojosmod.stand.star_finger_locked",
                        StarPlatinumProgress.STAR_FINGER_REQUIREMENT), false);
        source.sendSuccess(() -> StarPlatinumProgress.interceptUnlocked(player)
                ? Component.translatable(progress.completed(StarPlatinumProgress.Milestone.INTERCEPT)
                        ? "command.jojosmod.stand.intercept_mastered" : "command.jojosmod.stand.intercept_ready",
                        Component.keybind("key.jojosmod.star_intercept"))
                : Component.translatable("command.jojosmod.stand.intercept_locked",
                        StarPlatinumProgress.INTERCEPT_REQUIREMENT), false);
        source.sendSuccess(() -> StarPlatinumProgress.leapUnlocked(player)
                ? Component.translatable("command.jojosmod.stand.leap_ready", Component.keybind("key.jojosmod.star_leap"))
                : Component.translatable("command.jojosmod.stand.leap_locked", StarPlatinumProgress.LEAP_REQUIREMENT), false);
        return 1;
    }
}
