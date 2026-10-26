package io.github.bg7427941.jojosmod.network;

import io.github.bg7427941.jojosmod.JojosMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** A request only; the server owns eligibility, window, projectile and cooldown. */
public record InterceptPayload() implements CustomPacketPayload {
    public static final InterceptPayload INSTANCE = new InterceptPayload();
    public static final Type<InterceptPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(JojosMod.MOD_ID, "star_intercept"));
    public static final StreamCodec<FriendlyByteBuf, InterceptPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override public Type<InterceptPayload> type() { return TYPE; }
}
