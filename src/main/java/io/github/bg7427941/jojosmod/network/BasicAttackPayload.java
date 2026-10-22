package io.github.bg7427941.jojosmod.network;

import io.github.bg7427941.jojosmod.JojosMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Requests an attack without trusting the client with a target, damage or cooldown. */
public record BasicAttackPayload() implements CustomPacketPayload {
    public static final BasicAttackPayload INSTANCE = new BasicAttackPayload();
    public static final Type<BasicAttackPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(JojosMod.MOD_ID, "basic_attack"));
    public static final StreamCodec<FriendlyByteBuf, BasicAttackPayload> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    @Override
    public Type<BasicAttackPayload> type() {
        return TYPE;
    }
}
