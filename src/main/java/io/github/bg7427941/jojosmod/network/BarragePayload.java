package io.github.bg7427941.jojosmod.network;

import io.github.bg7427941.jojosmod.JojosMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** An intention only: timing, victims and damage remain server-owned. */
public record BarragePayload() implements CustomPacketPayload {
    public static final BarragePayload INSTANCE = new BarragePayload();
    public static final Type<BarragePayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(JojosMod.MOD_ID, "star_barrage"));
    public static final StreamCodec<FriendlyByteBuf, BarragePayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<BarragePayload> type() { return TYPE; }
}
