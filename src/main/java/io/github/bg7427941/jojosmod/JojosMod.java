package io.github.bg7427941.jojosmod;

import io.github.bg7427941.jojosmod.registry.ModAttachments;
import io.github.bg7427941.jojosmod.registry.ModItems;
import io.github.bg7427941.jojosmod.registry.ModEntities;
import io.github.bg7427941.jojosmod.commands.StandTestCommand;
import io.github.bg7427941.jojosmod.network.ModNetworking;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(JojosMod.MOD_ID)
public final class JojosMod {
    public static final String MOD_ID = "jojosmod";

    public JojosMod(IEventBus modEventBus) {
        ModItems.register(modEventBus);
        ModAttachments.register(modEventBus);
        ModEntities.register(modEventBus);
        ModNetworking.register(modEventBus);
        NeoForge.EVENT_BUS.addListener(StandTestCommand::register);
    }
}
