package io.github.bg7427941.jojosmod;

import io.github.bg7427941.jojosmod.registry.ModAttachments;
import io.github.bg7427941.jojosmod.registry.ModDataComponents;
import io.github.bg7427941.jojosmod.registry.ModItems;
import io.github.bg7427941.jojosmod.registry.ModEntities;
import io.github.bg7427941.jojosmod.registry.ModSounds;
import io.github.bg7427941.jojosmod.commands.StandTestCommand;
import io.github.bg7427941.jojosmod.commands.StandProgressCommand;
import io.github.bg7427941.jojosmod.network.ModNetworking;
import io.github.bg7427941.jojosmod.stands.StandProgressionConfig;
import io.github.bg7427941.jojosmod.abilities.StarPlatinumActions;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;

@Mod(JojosMod.MOD_ID)
public final class JojosMod {
    public static final String MOD_ID = "jojosmod";

    public JojosMod(IEventBus modEventBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.SYNCED, StandProgressionConfig.SPEC);
        ModDataComponents.register(modEventBus);
        ModItems.register(modEventBus);
        ModAttachments.register(modEventBus);
        ModEntities.register(modEventBus);
        ModSounds.register(modEventBus);
        ModNetworking.register(modEventBus);
        NeoForge.EVENT_BUS.addListener(StandTestCommand::register);
        NeoForge.EVENT_BUS.addListener(StandProgressCommand::register);
        NeoForge.EVENT_BUS.addListener(StarPlatinumActions::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(StarPlatinumActions::onIncomingDamage);
        NeoForge.EVENT_BUS.addListener(StarPlatinumActions::onDamage);
        NeoForge.EVENT_BUS.addListener(StarPlatinumActions::onProjectileImpact);
    }
}
