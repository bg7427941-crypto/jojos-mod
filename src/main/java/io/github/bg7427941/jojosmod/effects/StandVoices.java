package io.github.bg7427941.jojosmod.effects;

import io.github.bg7427941.jojosmod.network.StandVoicePayload;
import io.github.bg7427941.jojosmod.network.StandVoicePayload.Cue;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

/** Only the server can start or cancel a voice; observers receive the same owner identity. */
public final class StandVoices {
    private StandVoices() {}

    public static void play(net.minecraft.world.entity.Entity owner, Cue cue) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(owner,
                new StandVoicePayload(owner.getUUID(), cue, owner.position().add(0, 1, 0)));
    }

    public static void stop(net.minecraft.world.entity.Entity owner) { play(owner, Cue.STOP); }
}
