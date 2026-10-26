package io.github.bg7427941.jojosmod.network;

import io.github.bg7427941.jojosmod.stands.StandController;
import io.github.bg7427941.jojosmod.abilities.StarPlatinumActions;
import io.github.bg7427941.jojosmod.abilities.StarPlatinumLeap;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;

public final class ModNetworking {
    private static final String PROTOCOL_VERSION = "7";

    private ModNetworking() {}

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(ModNetworking::registerPayloads);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(PROTOCOL_VERSION).executesOn(HandlerThread.MAIN);
        registrar.playToClient(StandVoicePayload.TYPE, StandVoicePayload.STREAM_CODEC);
        registrar.playToServer(ToggleStandPayload.TYPE, ToggleStandPayload.STREAM_CODEC, (payload, context) -> {
            if (context.player() instanceof ServerPlayer player) {
                StandController.toggle(player);
            }
        });
        registrar.playToServer(BasicAttackPayload.TYPE, BasicAttackPayload.STREAM_CODEC, (payload, context) -> {
            if (context.player() instanceof ServerPlayer player) {
                StandController.attack(player);
            }
        });
        registrar.playToServer(BarragePayload.TYPE, BarragePayload.STREAM_CODEC, (payload, context) -> {
            if (context.player() instanceof ServerPlayer player) StarPlatinumActions.barrage(player);
        });
        registrar.playToServer(GuardPayload.TYPE, GuardPayload.STREAM_CODEC, (payload, context) -> {
            if (context.player() instanceof ServerPlayer player) StarPlatinumActions.toggleGuard(player);
        });
        registrar.playToServer(StarFingerPayload.TYPE, StarFingerPayload.STREAM_CODEC, (payload, context) -> {
            if (context.player() instanceof ServerPlayer player) StarPlatinumActions.starFinger(player);
        });
        registrar.playToServer(InterceptPayload.TYPE, InterceptPayload.STREAM_CODEC, (payload, context) -> {
            if (context.player() instanceof ServerPlayer player) StarPlatinumActions.intercept(player);
        });
        registrar.playToServer(LeapPayload.TYPE, LeapPayload.STREAM_CODEC, (payload, context) -> {
            if (context.player() instanceof ServerPlayer player) StarPlatinumLeap.leap(player);
        });
    }
}
