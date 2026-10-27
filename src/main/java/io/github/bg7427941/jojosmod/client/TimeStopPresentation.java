package io.github.bg7427941.jojosmod.client;

import com.mojang.blaze3d.buffers.Std140Builder;
import io.github.bg7427941.jojosmod.JojosMod;
import io.github.bg7427941.jojosmod.abilities.TimeStop;
import io.github.bg7427941.jojosmod.network.TimeScenePayload;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import org.jspecify.annotations.Nullable;

/** One scene per dimension, matching the server's bubble lifetime. No saved graphics state. */
public final class TimeStopPresentation {
    private static final Identifier EFFECT = Identifier.fromNamespaceAndPath(JojosMod.MOD_ID, "time_stop");
    private static @Nullable ClientLevel sceneLevel;
    private static @Nullable TimeScenePayload scene;
    private static long received;
    private static long ends;
    private static float partialTick;

    private TimeStopPresentation() {}

    public static void register(RegisterClientPayloadHandlersEvent event) {
        event.register(TimeScenePayload.TYPE, (payload, context) -> {
            var level = Minecraft.getInstance().level;
            if (level == null || payload.age() < 0 || payload.remaining() < 0 || payload.remaining() > 60) return;
            // Ignore a late end for a different caster.
            if (payload.remaining() == 0 && (scene == null || !scene.owner().equals(payload.owner()))) return;
            if (payload.remaining() == 0) Minecraft.getInstance().getSoundManager().stop(
                    Identifier.fromNamespaceAndPath(JojosMod.MOD_ID, payload.world() ? "the_world.time_stop" : "star_platinum.time_stop"),
                    net.minecraft.sounds.SoundSource.PLAYERS);
            sceneLevel = level; scene = payload; received = level.getGameTime();
            ends = received + payload.remaining();
        });
    }

    /** Called after vanilla collects its effects, preserving resource packs and other mods. */
    public static void collect(List<Identifier> effects, float partial) {
        partialTick = partial;
        var mc = Minecraft.getInstance();
        if (scene == null) return;
        if (mc.level != sceneLevel || mc.player == null || !mc.player.isAlive()) { clear(); return; }
        // A lost end snapshot cannot leave the screen filtered indefinitely.
        if (sceneLevel.getGameTime() > ends + 6) { clear(); return; }
        if (mc.player.position().distanceToSqr(scene.center()) <= TimeStop.RADIUS * TimeStop.RADIUS
                && mc.options.screenEffectScale().get() > 0) effects.add(EFFECT);
    }

    /** The temporal recording includes speech; a new cue from its caster replaces that call. */
    public static void cutCall(java.util.UUID owner) {
        var mc = Minecraft.getInstance();
        if (scene != null && mc.level == sceneLevel && scene.owner().equals(owner))
            mc.getSoundManager().stop(Identifier.fromNamespaceAndPath(JojosMod.MOD_ID,
                    scene.world() ? "the_world.time_stop" : "star_platinum.time_stop"), net.minecraft.sounds.SoundSource.PLAYERS);
    }

    public static void writeUniform(Std140Builder builder) {
        var mc = Minecraft.getInstance();
        if (scene == null || mc.level != sceneLevel) { builder.putVec4(0, 0, 0, 0); return; }
        float age = (scene.age() + sceneLevel.getGameTime() - received + partialTick) / 20F;
        float fade = Mth.clamp(age / .12F, 0, 1) * Mth.clamp((ends + 6 - sceneLevel.getGameTime() - partialTick) / 6F, 0, 1);
        builder.putVec4(age, fade * mc.options.screenEffectScale().get().floatValue(), scene.world() ? 1 : 0,
                mc.options.hideLightningFlash().get() ? 0 : 1);
    }

    private static void clear() { scene = null; sceneLevel = null; }
}
