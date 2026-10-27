package io.github.bg7427941.jojosmod.network;

import io.github.bg7427941.jojosmod.JojosMod;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

/** Server-selected voice cues share one replaceable channel per owner on each listener. */
public record StandVoicePayload(UUID owner, Cue cue, Vec3 position) implements CustomPacketPayload {
    public enum Cue {
        STOP, SUMMON, BARRAGE, STAR_FINGER, STRIKE,
        EMERALD_SPLASH("hierophant_green.splash_call"), CHARIOT_STRIKE("silver_chariot.strike_call"),
        CHARIOT_BARRAGE("silver_chariot.barrage_call"), CROSSFIRE("magicians_red.crossfire_call"),
        RED_BIND("magicians_red.bind_call"), PURPLE_CALL("hermit_purple.call"),
        WORLD_STRIKE("the_world.strike_call"), WORLD_BARRAGE("the_world.barrage_call");
        private final String soundPath;
        Cue() { this.soundPath = null; }
        Cue(String soundPath) { this.soundPath = soundPath; }
        public String soundPath() { return soundPath; }
    }

    public static final Type<StandVoicePayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(JojosMod.MOD_ID, "stand_voice"));
    public static final StreamCodec<FriendlyByteBuf, StandVoicePayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public StandVoicePayload decode(FriendlyByteBuf buffer) {
            return new StandVoicePayload(buffer.readUUID(), buffer.readEnum(Cue.class), Vec3.STREAM_CODEC.decode(buffer));
        }

        @Override
        public void encode(FriendlyByteBuf buffer, StandVoicePayload payload) {
            buffer.writeUUID(payload.owner());
            buffer.writeEnum(payload.cue());
            Vec3.STREAM_CODEC.encode(buffer, payload.position());
        }
    };

    @Override
    public Type<StandVoicePayload> type() { return TYPE; }
}
