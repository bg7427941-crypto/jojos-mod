package io.github.bg7427941.jojosmod.registry;

import io.github.bg7427941.jojosmod.JojosMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Short positional cues; no continuous audio survives an interrupted action. */
public final class ModSounds {
    private static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, JojosMod.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> STAR_SUMMON = sound("star_platinum.summon");
    public static final DeferredHolder<SoundEvent, SoundEvent> STAR_DISMISS = sound("star_platinum.dismiss");
    public static final DeferredHolder<SoundEvent, SoundEvent> STAR_SWING = sound("star_platinum.swing");
    public static final DeferredHolder<SoundEvent, SoundEvent> STAR_IMPACT = sound("star_platinum.impact");
    public static final DeferredHolder<SoundEvent, SoundEvent> STAR_GUARD = sound("star_platinum.guard");
    public static final DeferredHolder<SoundEvent, SoundEvent> STAR_GUARD_BLOCK = sound("star_platinum.guard_block");

    private ModSounds() {}

    private static DeferredHolder<SoundEvent, SoundEvent> sound(String path) {
        return SOUNDS.register(path, () -> SoundEvent.createVariableRangeEvent(
                Identifier.fromNamespaceAndPath(JojosMod.MOD_ID, path)));
    }

    public static void register(IEventBus bus) { SOUNDS.register(bus); }
}
