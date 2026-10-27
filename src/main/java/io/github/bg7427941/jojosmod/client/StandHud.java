package io.github.bg7427941.jojosmod.client;

import io.github.bg7427941.jojosmod.JojosMod;
import io.github.bg7427941.jojosmod.registry.ModStands;
import io.github.bg7427941.jojosmod.stands.PlayerStands;
import io.github.bg7427941.jojosmod.stands.PlayerWillpower;
import io.github.bg7427941.jojosmod.stands.StarPlatinumProgress;
import io.github.bg7427941.jojosmod.stands.StarPlatinumProgress.Milestone;
import io.github.bg7427941.jojosmod.stands.HierophantProgress;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/** Reads only the local owner's synchronized progression; never infers combat readiness. */
public final class StandHud {
    private static final int X = 8;
    private static final int Y = 8;
    private static final int PADDING = 6;

    private StandHud() {}

    public static void register(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.BOSS_OVERLAY,
                Identifier.fromNamespaceAndPath(JojosMod.MOD_ID, "stand_progress"), StandHud::render);
    }

    private static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        var minecraft = Minecraft.getInstance();
        var player = minecraft.player;
        if (player == null || minecraft.level == null || minecraft.gui.hud.isHidden()
                || minecraft.gui.screen() != null || minecraft.gui.overlay() != null
                || minecraft.getDebugOverlay().showDebugScreen() || !player.isAlive() || player.isSpectator()) {
            return;
        }
        var assignment = PlayerStands.get(player).standId();
        if (assignment.filter(ModStands.HIEROPHANT_GREEN.id()::equals).isPresent()) {
            var progress = HierophantProgress.get(player);
            draw(graphics, minecraft, List.of(
                    Component.translatable("hud.jojosmod.willpower", Component.translatable(ModStands.HIEROPHANT_GREEN.translationKey()), PlayerWillpower.get(player)),
                    Component.translatable("hud.jojosmod.hierophant.mastery", progress.splashTargets().size(), HierophantProgress.TARGETS_REQUIRED),
                    Component.translatable(progress.completed() ? "hud.jojosmod.hierophant.mastered" : "hud.jojosmod.hierophant.splash",
                            Component.keybind("key.jojosmod.basic_attack"))),
                    List.of(0xFFE9FFF0, 0xFFC1E6CC, 0xFFF2D278), 0xFF45C88D);
            return;
        }
        if (assignment.filter(ModStands.STAR_PLATINUM.id()::equals).isEmpty()) return;

        var progress = StarPlatinumProgress.get(player);
        int goal = StarPlatinumProgress.TARGETS_REQUIRED;
        boolean unlocked = StarPlatinumProgress.starFingerUnlocked(player);
        var title = Component.translatable("hud.jojosmod.willpower",
                Component.translatable(ModStands.STAR_PLATINUM.translationKey()), PlayerWillpower.get(player));
        var mastery = Component.translatable("hud.jojosmod.mastery",
                progress.count(Milestone.PRECISE), goal,
                progress.count(Milestone.BARRAGE), goal,
                progress.count(Milestone.GUARD), goal);
        var advanced = Component.translatable("hud.jojosmod.advanced_mastery",
                progress.count(Milestone.FINGER), goal, progress.count(Milestone.INTERCEPT), goal);
        var fingerKey = Component.keybind("key.jojosmod.star_finger");
        var ability = unlocked
                ? Component.translatable(progress.completed(Milestone.FINGER)
                        ? "hud.jojosmod.star_finger.mastered" : "hud.jojosmod.star_finger.unlocked", fingerKey)
                : Component.translatable("hud.jojosmod.star_finger.locked", fingerKey,
                        StarPlatinumProgress.STAR_FINGER_REQUIREMENT);

        boolean interceptUnlocked = StarPlatinumProgress.interceptUnlocked(player);
        var interceptKey = Component.keybind("key.jojosmod.star_intercept");
        var interception = interceptUnlocked
                ? Component.translatable(progress.completed(Milestone.INTERCEPT)
                        ? "hud.jojosmod.intercept.mastered" : "hud.jojosmod.intercept.unlocked", interceptKey)
                : Component.translatable("hud.jojosmod.intercept.locked", interceptKey,
                        StarPlatinumProgress.INTERCEPT_REQUIREMENT);
        boolean leapUnlocked = StarPlatinumProgress.leapUnlocked(player);
        var leapKey = Component.keybind("key.jojosmod.star_leap");
        var leap = leapUnlocked ? Component.translatable("hud.jojosmod.leap.unlocked", leapKey)
                : Component.translatable("hud.jojosmod.leap.locked", leapKey, StarPlatinumProgress.LEAP_REQUIREMENT);
        draw(graphics, minecraft, List.of(title, mastery, advanced, ability, interception, leap),
                List.of(0xFFF2E9FF, 0xFFD3C0EC, 0xFFD3C0EC, unlocked ? 0xFFF2D278 : 0xFFB8B4C4,
                        interceptUnlocked ? 0xFFF2D278 : 0xFFB8B4C4, leapUnlocked ? 0xFFF2D278 : 0xFFB8B4C4), 0xFF9470C9);
    }

    private static void draw(GuiGraphicsExtractor graphics, Minecraft minecraft, List<Component> lines,
            List<Integer> colors, int accent) {
        var font = minecraft.font;
        int textWidth = lines.stream().mapToInt(font::width).max().orElse(0);
        int width = textWidth + PADDING * 2;
        int height = font.lineHeight * lines.size() + PADDING * 2;
        float scale = Math.min(1.0F, Math.max(1, graphics.guiWidth() - X * 2) / (float) width);

        graphics.pose().pushMatrix();
        graphics.pose().translate(X, Y);
        graphics.pose().scale(scale, scale);
        graphics.fill(0, 0, width, height, 0xBE11101C);
        graphics.fill(0, 0, 2, height, accent);
        graphics.fill(2, 0, width, 1, 0xFFD8B85C);
        for (int row = 0; row < lines.size(); row++) {
            graphics.text(font, lines.get(row), PADDING, PADDING + font.lineHeight * row, colors.get(row), false);
        }
        graphics.pose().popMatrix();
    }
}
