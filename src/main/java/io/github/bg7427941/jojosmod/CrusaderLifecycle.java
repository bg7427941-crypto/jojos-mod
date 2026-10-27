package io.github.bg7427941.jojosmod;

import io.github.bg7427941.jojosmod.stands.StandController;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/** Release binds and time bubbles before transient player objects or levels are replaced. */
public final class CrusaderLifecycle {
    private CrusaderLifecycle() {}
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) { dismiss(event); }
    public static void onDimension(PlayerEvent.PlayerChangedDimensionEvent event) { dismiss(event); }
    private static void dismiss(PlayerEvent event) {
        if (event.getEntity() instanceof ServerPlayer p) StandController.dismiss(p);
    }
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer p) StandController.dismiss(p);
    }
}
