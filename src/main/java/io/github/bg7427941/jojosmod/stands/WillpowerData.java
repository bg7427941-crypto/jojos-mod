package io.github.bg7427941.jojosmod.stands;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** Persistent progression, independent of the assigned Stand and vanilla experience. */
public record WillpowerData(int value) {
    public static final int MAX_VALUE = 1_000_000;
    public static final WillpowerData INITIAL = new WillpowerData(1);
    public static final MapCodec<WillpowerData> CODEC = Codec.intRange(0, MAX_VALUE)
            .fieldOf("value").xmap(WillpowerData::new, WillpowerData::value);
    public static final StreamCodec<ByteBuf, WillpowerData> STREAM_CODEC = ByteBufCodecs.VAR_INT
            .map(WillpowerData::new, WillpowerData::value);

    public WillpowerData {
        if (value < 0 || value > MAX_VALUE) {
            throw new IllegalArgumentException("Willpower must be between 0 and " + MAX_VALUE);
        }
    }
}
