package io.github.bg7427941.jojosmod.client;

import com.mojang.logging.LogUtils;
import io.github.bg7427941.jojosmod.network.StandVoicePayload;
import io.github.bg7427941.jojosmod.network.StandVoicePayload.Cue;
import io.github.bg7427941.jojosmod.registry.ModSounds;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

/** Independent voices for friends; replacing one owner's phrase cannot stop another's. */
public final class StandVoicePlayer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<UUID, Playing> VOICES = new HashMap<>();
    private static @Nullable ClientLevel previousLevel;
    private static int ticks;

    private StandVoicePlayer() {}

    public static void register(RegisterClientPayloadHandlersEvent event) {
        event.register(StandVoicePayload.TYPE, (payload, context) -> receive(payload));
    }

    private static void receive(StandVoicePayload payload) {
        var minecraft = Minecraft.getInstance();
        checkLevel(minecraft);
        var previous = VOICES.remove(payload.owner());
        if (previous != null) minecraft.getSoundManager().stop(previous.sound());
        if (payload.cue() == Cue.STOP) {
            LOGGER.debug("Stopped Stand voice for {}", payload.owner());
            return;
        }
        var level = minecraft.level;
        if (level == null || level.getPlayerByUUID(payload.owner()) == null) return;
        var event = VoiceSound.soundFor(payload.cue());
        var definition = minecraft.getSoundManager().getSoundEvent(event.location());
        // Optional voices stay silent without a resource-pack clip, without warnings.
        if (definition == null || definition.getWeight() == 0) return;
        var sound = new VoiceSound(level, payload, event);
        VOICES.put(payload.owner(), new Playing(sound, ticks));
        minecraft.getSoundManager().play(sound);
        LOGGER.debug("Playing Stand voice {} for {}", payload.cue(), payload.owner());
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        var minecraft = Minecraft.getInstance();
        checkLevel(minecraft);
        ticks++;
        VOICES.entrySet().removeIf(entry -> {
            var playing = entry.getValue();
            int elapsed = ticks - playing.startedAt();
            // Permit asynchronous buffer loading, and remove completed or failed playback.
            boolean finished = playing.sound().isStopped()
                    || elapsed > 2 && !minecraft.getSoundManager().isActive(playing.sound());
            if (finished || elapsed > 120) {
                minecraft.getSoundManager().stop(playing.sound());
                return true;
            }
            return false;
        });
    }

    private static void checkLevel(Minecraft minecraft) {
        if (previousLevel != minecraft.level) {
            VOICES.values().forEach(playing -> minecraft.getSoundManager().stop(playing.sound()));
            VOICES.clear();
            previousLevel = minecraft.level;
        }
    }

    private record Playing(VoiceSound sound, int startedAt) {}

    private static final class VoiceSound extends AbstractTickableSoundInstance {
        private final ClientLevel level;
        private final UUID owner;

        private VoiceSound(ClientLevel level, StandVoicePayload payload, SoundEvent event) {
            super(event, SoundSource.PLAYERS, RandomSource.create());
            this.level = level;
            owner = payload.owner();
            volume = 0.65F;
            pitch = 1.0F;
            looping = false;
            relative = false;
            attenuation = SoundInstance.Attenuation.LINEAR;
            x = payload.position().x;
            y = payload.position().y;
            z = payload.position().z;
        }

        private static SoundEvent soundFor(Cue cue) {
            return switch (cue) {
                case SUMMON -> ModSounds.STAR_SUMMON.get();
                case BARRAGE -> ModSounds.STAR_BARRAGE_START.get();
                case STAR_FINGER -> ModSounds.STAR_FINGER.get();
                case STRIKE -> ModSounds.STAR_SINGLE_ORA.get();
                case STOP -> throw new IllegalArgumentException("A stop cue cannot create a voice");
            };
        }

        @Override
        public void tick() {
            var player = level.getPlayerByUUID(owner);
            if (Minecraft.getInstance().level != level || player == null
                    || !player.isAlive() || player.isSpectator()) {
                stop();
                return;
            }
            x = player.getX();
            y = player.getY() + 1;
            z = player.getZ();
        }
    }
}
