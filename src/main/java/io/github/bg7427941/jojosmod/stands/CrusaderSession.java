package io.github.bg7427941.jojosmod.stands;

import java.util.UUID;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/** Mutable, per-player, server-only transient state; never shared through a static default. */
public final class CrusaderSession {
    public enum Action { NONE, PRIMARY, SECONDARY, DEFENSE, GRAPPLE, GLIDE, ADVANCED }
    public Action action = Action.NONE;
    public PartThreeStand kind;
    public long startedAt, primaryReady, secondaryReady, utilityReady, timeReady, armorUntil, advancedReady, rapierUntil;
    public int pulses;
    public UUID volley;
    public LivingEntity bound;
    public Vec3 anchor;
}
