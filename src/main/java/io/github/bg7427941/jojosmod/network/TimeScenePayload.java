package io.github.bg7427941.jojosmod.network;

import io.github.bg7427941.jojosmod.JojosMod;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

/** A nearby listener's presentation snapshot; never authorizes a freeze or an ability. */
public record TimeScenePayload(UUID owner, Vec3 center, boolean world, int age, int remaining)
        implements CustomPacketPayload {
    public static final Type<TimeScenePayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(JojosMod.MOD_ID, "time_scene"));
    public static final StreamCodec<FriendlyByteBuf, TimeScenePayload> STREAM_CODEC = new StreamCodec<>() {
        public TimeScenePayload decode(FriendlyByteBuf b) {
            return new TimeScenePayload(b.readUUID(), Vec3.STREAM_CODEC.decode(b), b.readBoolean(), b.readVarInt(), b.readVarInt());
        }
        public void encode(FriendlyByteBuf b, TimeScenePayload p) {
            b.writeUUID(p.owner()); Vec3.STREAM_CODEC.encode(b, p.center());
            b.writeBoolean(p.world()); b.writeVarInt(p.age()); b.writeVarInt(p.remaining());
        }
    };
    public Type<TimeScenePayload> type() { return TYPE; }
}
