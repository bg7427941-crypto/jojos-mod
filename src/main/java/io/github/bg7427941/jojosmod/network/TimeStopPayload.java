package io.github.bg7427941.jojosmod.network;

import io.github.bg7427941.jojosmod.JojosMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** An intention only: timing, victims and damage remain server-owned. */
public record TimeStopPayload() implements CustomPacketPayload {
    public static final TimeStopPayload INSTANCE = new TimeStopPayload();
    public static final Type<TimeStopPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(JojosMod.MOD_ID, "time_stop"));
    public static final StreamCodec<FriendlyByteBuf, TimeStopPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<TimeStopPayload> type() { return TYPE; }
}
