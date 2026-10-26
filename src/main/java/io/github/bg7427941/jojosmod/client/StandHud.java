package io.github.bg7427941.jojosmod.client;

import io.github.bg7427941.jojosmod.JojosMod;
import io.github.bg7427941.jojosmod.registry.ModStands;
import io.github.bg7427941.jojosmod.stands.PlayerStands;
import io.github.bg7427941.jojosmod.stands.PlayerWillpower;
import io.github.bg7427941.jojosmod.stands.StarPlatinumProgress;
import io.github.bg7427941.jojosmod.stands.StarPlatinumProgress.Milestone;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
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
                || minecraft.getDebugOverlay().showDebugScreen() || !player.isAlive() || player.isSpectator()
                || PlayerStands.get(player).standId().filter(ModStands.STAR_PLATINUM.id()::equals).isEmpty()) {
            return;
        }

        var progress = StarPlatinumProgress.get(player);
        int goal = StarPlatinumProgress.TARGETS_REQUIRED;
        boolean unlocked = StarPlatinumProgress.starFingerUnlocked(player);
        var title = Component.translatable("hud.jojosmod.willpower",
                Component.translatable(ModStands.STAR_PLATINUM.translationKey()), PlayerWillpower.get(player));
        var mastery = Component.translatable("hud.jojosmod.mastery",
                progress.count(Milestone.PRECISE), goal,
                progress.count(Milestone.BARRAGE), goal,
                progress.count(Milestone.GUARD), goal);
        var fingerKey = Component.keybind("key.jojosmod.star_finger");
        var ability = unlocked
                ? Component.translatable("hud.jojosmod.star_finger.unlocked", fingerKey)
                : Component.translatable("hud.jojosmod.star_finger.locked", fingerKey,
                        StarPlatinumProgress.STAR_FINGER_REQUIREMENT);

        var font = minecraft.font;
        boolean interceptUnlocked = StarPlatinumProgress.interceptUnlocked(player);
        var interceptKey = Component.keybind("key.jojosmod.star_intercept");
        var interception = interceptUnlocked
                ? Component.translatable("hud.jojosmod.intercept.unlocked", interceptKey)
                : Component.translatable("hud.jojosmod.intercept.locked", interceptKey,
                        StarPlatinumProgress.INTERCEPT_REQUIREMENT);
        boolean leapUnlocked = StarPlatinumProgress.leapUnlocked(player);
        var leapKey = Component.keybind("key.jojosmod.star_leap");
        var leap = leapUnlocked ? Component.translatable("hud.jojosmod.leap.unlocked", leapKey)
                : Component.translatable("hud.jojosmod.leap.locked", leapKey, StarPlatinumProgress.LEAP_REQUIREMENT);
        int textWidth = Math.max(Math.max(font.width(title), font.width(mastery)),
                Math.max(font.width(ability), font.width(interception)));
        int width = Math.max(textWidth, font.width(leap)) + PADDING * 2;
        int height = font.lineHeight * 5 + PADDING * 2;
        float scale = Math.min(1.0F, Math.max(1, graphics.guiWidth() - X * 2) / (float) width);

        graphics.pose().pushMatrix();
        graphics.pose().translate(X, Y);
        graphics.pose().scale(scale, scale);
        graphics.fill(0, 0, width, height, 0xBE11101C);
        graphics.fill(0, 0, 2, height, 0xFF9470C9);
        graphics.fill(2, 0, width, 1, 0xFFD8B85C);
        graphics.text(font, title, PADDING, PADDING, 0xFFF2E9FF, false);
        graphics.text(font, mastery, PADDING, PADDING + font.lineHeight, 0xFFD3C0EC, false);
        graphics.text(font, ability, PADDING, PADDING + font.lineHeight * 2,
                unlocked ? 0xFFF2D278 : 0xFFB8B4C4, false);
        graphics.text(font, interception, PADDING, PADDING + font.lineHeight * 3,
                interceptUnlocked ? 0xFFF2D278 : 0xFFB8B4C4, false);
        graphics.text(font, leap, PADDING, PADDING + font.lineHeight * 4,
                leapUnlocked ? 0xFFF2D278 : 0xFFB8B4C4, false);
        graphics.pose().popMatrix();
    }
}
