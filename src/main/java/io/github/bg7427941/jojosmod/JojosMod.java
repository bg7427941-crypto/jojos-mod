package io.github.bg7427941.jojosmod;

import io.github.bg7427941.jojosmod.registry.ModAttachments;
import io.github.bg7427941.jojosmod.registry.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(JojosMod.MOD_ID)
public final class JojosMod {
    public static final String MOD_ID = "jojosmod";

    public JojosMod(IEventBus modEventBus) {
        ModItems.register(modEventBus);
        ModAttachments.register(modEventBus);
    }
}
