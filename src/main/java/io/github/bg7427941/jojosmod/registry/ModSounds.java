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
    public static final DeferredHolder<SoundEvent, SoundEvent> STAR_SINGLE_ORA = sound("star_platinum.single_ora");
    public static final DeferredHolder<SoundEvent, SoundEvent> STAR_BARRAGE_START = sound("star_platinum.barrage_start");
    public static final DeferredHolder<SoundEvent, SoundEvent> STAR_FINGER = sound("star_platinum.star_finger");
    public static final DeferredHolder<SoundEvent, SoundEvent> STAR_IMPACT = sound("star_platinum.impact");
    public static final DeferredHolder<SoundEvent, SoundEvent> STAR_GUARD = sound("star_platinum.guard");
    public static final DeferredHolder<SoundEvent, SoundEvent> STAR_GUARD_BLOCK = sound("star_platinum.guard_block");
    public static final DeferredHolder<SoundEvent, SoundEvent> STAR_INTERCEPT = sound("star_platinum.intercept");
    public static final DeferredHolder<SoundEvent, SoundEvent> STAR_INTERCEPT_CATCH = sound("star_platinum.intercept_catch");
    public static final DeferredHolder<SoundEvent, SoundEvent> STAR_LEAP = sound("star_platinum.leap");
    public static final DeferredHolder<SoundEvent, SoundEvent> STAR_TIME_STOP = sound("star_platinum.time_stop");
    public static final DeferredHolder<SoundEvent, SoundEvent> STAR_TIME_RESUME = sound("star_platinum.resume");
    public static final DeferredHolder<SoundEvent, SoundEvent> STAR_LEAP_LAND = sound("star_platinum.leap_land");

    public static final DeferredHolder<SoundEvent, SoundEvent> HIEROPHANT_SUMMON = sound("hierophant_green.summon");
    public static final DeferredHolder<SoundEvent, SoundEvent> HIEROPHANT_DISMISS = sound("hierophant_green.dismiss");
    public static final DeferredHolder<SoundEvent, SoundEvent> HIEROPHANT_CHARGE = sound("hierophant_green.charge");
    public static final DeferredHolder<SoundEvent, SoundEvent> HIEROPHANT_SPLASH = sound("hierophant_green.splash");
    public static final DeferredHolder<SoundEvent, SoundEvent> HIEROPHANT_IMPACT = sound("hierophant_green.impact");

    private static final java.util.Map<String, DeferredHolder<SoundEvent, SoundEvent>> CRUSADER = new java.util.HashMap<>();
    static {
        for (var kind : io.github.bg7427941.jojosmod.stands.PartThreeStand.values())
            for (var cue : java.util.List.of("summon", "dismiss", "cast", "impact"))
                CRUSADER.put(kind.path() + "." + cue, sound(kind.path() + "." + cue));
        for (var cue : java.util.List.of("time_stop", "resume")) CRUSADER.put("the_world." + cue, sound("the_world." + cue));
    }
    public static SoundEvent crusader(io.github.bg7427941.jojosmod.stands.PartThreeStand kind, String cue) {
        return CRUSADER.get(kind.path() + "." + cue).get();
    }
    private ModSounds() {}

    private static DeferredHolder<SoundEvent, SoundEvent> sound(String path) {
        return SOUNDS.register(path, () -> SoundEvent.createVariableRangeEvent(
                Identifier.fromNamespaceAndPath(JojosMod.MOD_ID, path)));
    }

    public static void register(IEventBus bus) { SOUNDS.register(bus); }
}
