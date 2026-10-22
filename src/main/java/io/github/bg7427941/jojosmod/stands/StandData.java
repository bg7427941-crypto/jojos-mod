package io.github.bg7427941.jojosmod.stands;

import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

/** The single Stand assigned to a player, independent of any summoned entity. */
public record StandData(Optional<Identifier> standId) {
    public static final StandData EMPTY = new StandData(Optional.empty());

    public static final MapCodec<StandData> CODEC = Identifier.CODEC
            .optionalFieldOf("stand_id")
            .xmap(StandData::new, StandData::standId);

    public static final StreamCodec<ByteBuf, StandData> STREAM_CODEC = ByteBufCodecs
            .optional(Identifier.STREAM_CODEC)
            .map(StandData::new, StandData::standId);

    public StandData {
        Objects.requireNonNull(standId, "standId");
    }
}
