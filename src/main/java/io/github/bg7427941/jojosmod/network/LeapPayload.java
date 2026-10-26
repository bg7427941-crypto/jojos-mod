package io.github.bg7427941.jojosmod.network;

import io.github.bg7427941.jojosmod.JojosMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Only requests a leap; the server decides movement, footing and cooldown. */
public record LeapPayload() implements CustomPacketPayload {
    public static final LeapPayload INSTANCE = new LeapPayload();
    public static final Type<LeapPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(JojosMod.MOD_ID, "star_leap"));
    public static final StreamCodec<FriendlyByteBuf, LeapPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);
    @Override public Type<LeapPayload> type() { return TYPE; }
}
