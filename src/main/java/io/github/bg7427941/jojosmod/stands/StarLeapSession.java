package io.github.bg7427941.jojosmod.stands;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/** Server-only mobility cooldown and bounded protection for one landing; never saved or copied on death. */
public record StarLeapSession(long readyAt, long protectionUntil, double launchY, boolean airborne,
        ResourceKey<Level> dimension) {
    public static final StarLeapSession EMPTY = new StarLeapSession(0, 0, 0, false, Level.OVERWORLD);

    public StarLeapSession clearGrace() {
        return new StarLeapSession(readyAt, 0, 0, false, dimension);
    }

    public StarLeapSession markAirborne() {
        return new StarLeapSession(readyAt, protectionUntil, launchY, true, dimension);
    }
}
