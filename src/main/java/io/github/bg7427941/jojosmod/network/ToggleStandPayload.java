package io.github.bg7427941.jojosmod.network;

import io.github.bg7427941.jojosmod.JojosMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Requests a toggle; ownership and summon state are resolved by the server. */
public record ToggleStandPayload() implements CustomPacketPayload {
    public static final ToggleStandPayload INSTANCE = new ToggleStandPayload();
    public static final Type<ToggleStandPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(JojosMod.MOD_ID, "toggle_stand"));
    public static final StreamCodec<FriendlyByteBuf, ToggleStandPayload> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    @Override
    public Type<ToggleStandPayload> type() {
        return TYPE;
    }
}
