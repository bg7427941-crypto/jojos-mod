package io.github.bg7427941.jojosmod.network;

import io.github.bg7427941.jojosmod.JojosMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record GuardPayload() implements CustomPacketPayload {
    public static final GuardPayload INSTANCE = new GuardPayload();
    public static final Type<GuardPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(JojosMod.MOD_ID, "star_guard"));
    public static final StreamCodec<FriendlyByteBuf, GuardPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<GuardPayload> type() { return TYPE; }
}
