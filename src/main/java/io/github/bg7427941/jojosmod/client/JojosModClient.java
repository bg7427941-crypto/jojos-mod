package io.github.bg7427941.jojosmod.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.bg7427941.jojosmod.JojosMod;
import io.github.bg7427941.jojosmod.network.BasicAttackPayload;
import io.github.bg7427941.jojosmod.network.ToggleStandPayload;
import io.github.bg7427941.jojosmod.registry.ModEntities;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.NeoForge;

/** Kept in a client-only entrypoint so dedicated servers never load renderer or input classes. */
@Mod(value = JojosMod.MOD_ID, dist = Dist.CLIENT)
public final class JojosModClient {
    private static final KeyMapping.Category STAND_CATEGORY =
            new KeyMapping.Category(Identifier.fromNamespaceAndPath(JojosMod.MOD_ID, "stands"));
    private static final KeyMapping TOGGLE_STAND =
            new KeyMapping("key.jojosmod.toggle_stand", InputConstants.KEY_R, STAND_CATEGORY);
    private static final KeyMapping BASIC_ATTACK =
            new KeyMapping("key.jojosmod.basic_attack", InputConstants.KEY_Z, STAND_CATEGORY);

    public JojosModClient(IEventBus modEventBus) {
        modEventBus.addListener(JojosModClient::registerKeys);
        modEventBus.addListener(JojosModClient::registerRenderers);
        NeoForge.EVENT_BUS.addListener(JojosModClient::onClientTick);
    }

    private static void registerKeys(RegisterKeyMappingsEvent event) {
        event.registerCategory(STAND_CATEGORY);
        event.register(TOGGLE_STAND);
        event.register(BASIC_ATTACK);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.TRAINING_STAND.get(), TrainingStandRenderer::new);
        event.registerEntityRenderer(ModEntities.DISC_KEEPER.get(), DiscKeeperRenderer::new);
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        var minecraft = Minecraft.getInstance();
        boolean canSend = minecraft.player != null
                && minecraft.level != null
                && minecraft.getConnection() != null
                && minecraft.gui.screen() == null
                && minecraft.gui.overlay() == null
                && !minecraft.isPaused();

        // Always drain queued clicks so keys pressed in menus cannot trigger on returning to the world.
        while (TOGGLE_STAND.consumeClick()) {
            if (canSend) {
                ClientPacketDistributor.sendToServer(ToggleStandPayload.INSTANCE);
            }
        }
        while (BASIC_ATTACK.consumeClick()) {
            if (canSend) {
                ClientPacketDistributor.sendToServer(BasicAttackPayload.INSTANCE);
            }
        }
    }
}
