package io.github.bg7427941.jojosmod.abilities;

import io.github.bg7427941.jojosmod.entities.TrainingStandEntity;
import io.github.bg7427941.jojosmod.registry.ModAttachments;
import io.github.bg7427941.jojosmod.registry.ModDamageTypes;
import io.github.bg7427941.jojosmod.registry.ModSounds;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Bounded tentacle restraint and a stationary emerald tripwire; all state is server-owned. */
public final class HierophantTools {
    public enum Action { NONE, TENTACLE, BARRIER }
    public static final class State {
        public Action action = Action.NONE;
        public long started, tentacleReady, barrierReady, barrierUntil;
        public LivingEntity bound;
        public boolean struck;
        public Vec3 center, side, normal;
        public final Set<UUID> triggered = new HashSet<>();
    }
    private HierophantTools() {}
    public static State state(ServerPlayer owner) { return owner.getData(ModAttachments.HIEROPHANT_TOOLS); }
    public static boolean busy(ServerPlayer owner) {
        return owner.hasData(ModAttachments.HIEROPHANT_TOOLS) && state(owner).action != Action.NONE;
    }
    public static boolean tentacle(ServerPlayer owner) {
        var stand = HierophantSplash.active(owner);
        if (stand.isEmpty() || HierophantSplash.session(owner).casting()) return false;
        var s = state(owner); long now = owner.level().getGameTime();
        if (s.action != Action.NONE || now < s.tentacleReady) return false;
        s.action = Action.TENTACLE; s.started = now; s.tentacleReady = now + 120; s.struck = false;
        stand.get().playCrusaderAnimation("secondary"); sound(owner, false);
        return true;
    }
    public static boolean barrier(ServerPlayer owner) {
        var stand = HierophantSplash.active(owner);
        if (stand.isEmpty() || HierophantSplash.session(owner).casting()) return false;
        var s = state(owner); long now = owner.level().getGameTime();
        if (s.barrierUntil > now) { clearBarrier(s); return true; }
        if (s.action != Action.NONE || now < s.barrierReady) return false;
        var eye = owner.getEyePosition(); var forward = new Vec3(owner.getLookAngle().x, 0, owner.getLookAngle().z).normalize();
        if (forward.lengthSqr() < .5) return false;
        var desired = eye.add(forward.scale(4));
        var hit = owner.level().clip(new ClipContext(eye, desired, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, owner));
        if (hit.getType() != HitResult.Type.MISS) return false;
        s.action = Action.BARRIER; s.started = now; s.barrierReady = now + 260; s.barrierUntil = now + 200;
        s.center = desired.add(0, -.6, 0); s.normal = forward; s.side = new Vec3(forward.z, 0, -forward.x);
        s.triggered.clear(); stand.get().playCrusaderAnimation("utility"); sound(owner, false);
        owner.sendSystemMessage(Component.translatable("message.jojosmod.hierophant.barrier"), true);
        return true;
    }
    public static void onTick(PlayerTickEvent.Post event) { if (event.getEntity() instanceof ServerPlayer p) tick(p); }
    public static void tick(ServerPlayer owner) {
        if (!owner.hasData(ModAttachments.HIEROPHANT_TOOLS)) return;
        var s = state(owner); if (s.action == Action.NONE && s.center == null) return;
        var stand = HierophantSplash.active(owner);
        if (stand.isEmpty()) { cancel(owner); return; }
        long now = owner.level().getGameTime(), elapsed = now - s.started;
        if (s.action == Action.TENTACLE) {
            if (elapsed >= 6 && !s.struck) {
                s.struck = true;
                CrusaderActions.target(owner, 10).ifPresent(target -> {
                    if (!damage(owner, stand.get(), target, 4)) return;
                    // Damage hooks may dismiss or replace the owner during the hit.
                    if (s.action != Action.TENTACLE || HierophantSplash.active(owner).isEmpty() || !target.isAlive()) return;
                    s.bound = target;
                    var speed = target.getAttribute(Attributes.MOVEMENT_SPEED);
                    if (speed != null) speed.addOrUpdateTransientModifier(new AttributeModifier(bindId(owner), -.65, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
                });
            }
            if (s.bound != null) {
                if (!CrusaderActions.canDamage(owner, s.bound) || s.bound.level() != owner.level()
                        || s.bound.distanceToSqr(owner) > 144 || !owner.hasLineOfSight(s.bound)) finish(owner, true);
                else if (now % 3 == 0) thread(owner, stand.get().position().add(0, 1.3, 0), s.bound.getEyePosition());
            }
            if (s.action == Action.TENTACLE && (elapsed >= 40 || elapsed >= 12 && s.bound == null)) finish(owner, false);
        } else if (s.action == Action.BARRIER && elapsed >= 20) finish(owner, false);
        if (s.center == null) return;
        if (now >= s.barrierUntil || owner.distanceToSqr(s.center) > 256) { clearBarrier(s); return; }
        if (now % 4 == 0) for (double y : new double[]{-.7, 0, .7})
            thread(owner, s.center.add(s.side.scale(-3)).add(0,y,0), s.center.add(s.side.scale(3)).add(0,y,0));
        if (now < s.barrierUntil - 194) return;
        for (var target : owner.level().getEntitiesOfClass(LivingEntity.class, new AABB(s.center, s.center).inflate(3.5, 1.8, 3.5), t -> CrusaderActions.canDamage(owner, t))) {
            var delta = target.position().add(0, target.getBbHeight() * .5, 0).subtract(s.center);
            // Check swept movement as well as current overlap, so fast entities cannot step through.
            double at = delta.dot(s.normal), before = delta.subtract(target.getDeltaMovement()).dot(s.normal);
            if ((Math.abs(at) > .45 + target.getBbWidth() * .5 && at * before > 0)
                    || Math.abs(delta.dot(s.side)) > 3 || Math.abs(delta.y) > 1.25
                    || s.triggered.contains(target.getUUID())) continue;
            var sight = owner.level().clip(new ClipContext(s.center, target.getEyePosition(), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, owner));
            if (sight.getType() != HitResult.Type.MISS) continue;
            s.triggered.add(target.getUUID());
            var castCenter = s.center;
            boolean hit = damage(owner, stand.get(), target, 6);
            if (s.center != castCenter || HierophantSplash.active(owner).isEmpty()) return;
            if (hit) { thread(owner, castCenter, target.getEyePosition()); sound(owner, true); }
            if (s.triggered.size() >= 8) { clearBarrier(s); return; }
        }
    }
    private static boolean damage(ServerPlayer owner, TrainingStandEntity stand, LivingEntity target, float amount) {
        if (!CrusaderActions.canDamage(owner, target)) return false;
        return target.hurtServer(owner.level(), ModDamageTypes.emerald(owner, stand), amount);
    }
    private static Identifier bindId(ServerPlayer p) { return Identifier.fromNamespaceAndPath("jojosmod", "hierophant_tentacle/" + p.getUUID()); }
    private static void finish(ServerPlayer p, boolean interrupted) {
        var s = state(p);
        if (s.bound != null) { var speed = s.bound.getAttribute(Attributes.MOVEMENT_SPEED); if (speed != null) speed.removeModifier(bindId(p)); s.bound = null; }
        boolean running = s.action != Action.NONE; s.action = Action.NONE;
        if (running) StandControllerHelper.finish(p, interrupted);
    }
    private static void clearBarrier(State s) { s.center = null; s.barrierUntil = 0; s.triggered.clear(); }
    public static void cancel(ServerPlayer p) {
        if (!p.hasData(ModAttachments.HIEROPHANT_TOOLS)) return;
        finish(p, true); clearBarrier(state(p));
    }
    public static void onDamage(LivingDamageEvent.Post event) { if (event.getEntity() instanceof ServerPlayer p && event.getHealthDamage() > 0) cancel(p); }
    private static void sound(ServerPlayer p, boolean hit) { p.level().playSound(null, p.blockPosition(), (hit ? ModSounds.HIEROPHANT_IMPACT : ModSounds.HIEROPHANT_CHARGE).get(), SoundSource.PLAYERS, .5F, 1); }
    private static void thread(ServerPlayer p, Vec3 a, Vec3 b) {
        int count = Math.min(40, Math.max(2, (int)(a.distanceTo(b) * 4)));
        for (int i=0;i<=count;i++) { var at=a.lerp(b,(double)i/count); p.level().sendParticles(new DustParticleOptions(0x47DD93,.65F),at.x,at.y,at.z,1,0,0,0,0); }
    }
    private static final class StandControllerHelper {
        static void finish(ServerPlayer p, boolean interrupted) {
            io.github.bg7427941.jojosmod.stands.StandController.getActive(p).filter(TrainingStandEntity::isHierophantGreen).ifPresent(stand -> { if(interrupted)stand.stopActionAnimation();else stand.finishActionAnimation(); });
        }
    }
}
