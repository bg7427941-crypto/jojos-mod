package io.github.bg7427941.jojosmod.network;

import io.github.bg7427941.jojosmod.JojosMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** The server chooses the unlock, timing, reach and victim. */
public record StarFingerPayload() implements CustomPacketPayload {
    public static final StarFingerPayload INSTANCE = new StarFingerPayload();
    public static final Type<StarFingerPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(JojosMod.MOD_ID, "star_finger"));
    public static final StreamCodec<FriendlyByteBuf, StarFingerPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<StarFingerPayload> type() { return TYPE; }
}
