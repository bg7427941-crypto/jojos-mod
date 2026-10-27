package io.github.bg7427941.jojosmod.abilities;

import io.github.bg7427941.jojosmod.effects.CrusaderEffects;
import io.github.bg7427941.jojosmod.entities.TrainingStandEntity;
import io.github.bg7427941.jojosmod.registry.ModAttachments;
import io.github.bg7427941.jojosmod.registry.ModStands;
import io.github.bg7427941.jojosmod.stands.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** A bounded local pause, never a global server pause. Network/teleport acknowledgements keep running. */
public final class TimeStop {
    public static final double RADIUS = 12;
    public static final int MAX_TARGETS = 256;
    private static final Map<ServerLevel, Bubble> BUBBLES = new HashMap<>();
    private record Snapshot(Vec3 position, Vec3 velocity, float yaw, float pitch) {}
    private static final class Bubble {
        final ServerPlayer owner;
        final TrainingStandEntity stand;
        final Vec3 center;
        final long until;
        final Map<Entity, Snapshot> targets = new LinkedHashMap<>();
        Bubble(ServerPlayer p, TrainingStandEntity s, int duration) {
            owner = p; stand = s; center = p.position(); until = p.level().getGameTime() + duration;
        }
    }
    private TimeStop() {}
    public static boolean compatible(ServerPlayer p) {
        var id = PlayerStands.get(p).standId();
        return id.filter(ModStands.THE_WORLD.id()::equals).isPresent() && PlayerWillpower.get(p) >= 4
                || id.filter(ModStands.STAR_PLATINUM.id()::equals).isPresent() && PlayerWillpower.get(p) >= 6;
    }
    public static boolean start(ServerPlayer p) {
        var stand = StandController.getActive(p);
        if (!p.isAlive() || p.isRemoved() || p.isSpectator() || isFrozen(p) || !compatible(p)
                || stand.isEmpty() || !StandController.isActive(p, stand.get())) {
            p.sendSystemMessage(Component.translatable("message.jojosmod.time.locked"), true); return false;
        }
        var s = CrusaderActions.session(p); long now = p.level().getGameTime();
        if (now < s.timeReady || BUBBLES.containsKey(p.level())) return false;
        s.timeReady = now + 600;
        boolean world = PlayerStands.get(p).standId().filter(ModStands.THE_WORLD.id()::equals).isPresent();
        var bubble = new Bubble(p, stand.get(), world ? 60 : 40);
        BUBBLES.put(p.level(), bubble); tick(p.level());
        if (world) stand.get().playCrusaderAnimation("time_stop");
        CrusaderEffects.sound(p.level(), PartThreeStand.THE_WORLD, "time_stop", p.position());
        for (var player : p.level().players()) if (player.distanceToSqr(p) <= 1024)
            player.sendSystemMessage(Component.translatable("message.jojosmod.time.start", p.getName()), true);
        return true;
    }
    public static boolean isFrozen(Entity entity) {
        return entity.level() instanceof ServerLevel l && BUBBLES.containsKey(l) && BUBBLES.get(l).targets.containsKey(entity);
    }
    private static boolean valid(ServerLevel level, Bubble b) {
        return level.getGameTime() < b.until && b.owner.isAlive() && !b.owner.isRemoved() && !b.owner.isSpectator()
                && b.owner.level() == level && b.owner.distanceToSqr(b.center) <= RADIUS * RADIUS
                && StandController.isActive(b.owner, b.stand) && !b.stand.isRemoved() && !b.stand.isDismissing();
    }
    private static boolean eligible(Bubble b, Entity e) {
        if (e == b.owner || e.isRemoved() || e.isSpectator() || !(e instanceof LivingEntity || e instanceof Projectile)) return false;
        if (e instanceof ServerPlayer p && (p.getAbilities().instabuild || compatible(p)
                || !b.owner.level().isPvpAllowed() || !b.owner.canHarmPlayer(p))) return false;
        return !b.owner.isAlliedTo(e);
    }
    public static void onServerTick(ServerTickEvent.Pre event) {
        for (var level : event.getServer().getAllLevels()) tick(level);
    }
    public static void tick(ServerLevel level) {
        var b = BUBBLES.get(level); if (b == null) return;
        if (!valid(level, b)) { end(level); return; }
        var it = b.targets.entrySet().iterator();
        while (it.hasNext()) {
            var entry = it.next(); var e = entry.getKey();
            if (e.level() != level || !eligible(b, e) || e.distanceToSqr(b.center) > RADIUS * RADIUS) {
                release(e, entry.getValue()); it.remove();
            }
        }
        for (var e : level.getEntities((Entity)null, new AABB(b.center, b.center).inflate(RADIUS), e -> eligible(b, e))) {
            if (b.targets.size() >= MAX_TARGETS) break;
            if (e.distanceToSqr(b.center) > RADIUS * RADIUS || b.targets.containsKey(e)) continue;
            b.targets.put(e, new Snapshot(e.position(), e.getDeltaMovement(), e.getYRot(), e.getXRot()));
            e.setData(ModAttachments.TIME_FROZEN, true);
            e.syncVelocity = true;
            if (e instanceof ServerPlayer p) {
                p.stopUsingItem(); p.connection.teleport(p.getX(), p.getY(), p.getZ(), p.getYRot(), p.getXRot());
            }
            if (e instanceof LivingEntity living) living.stopUsingItem();
        }
        for (var entry : b.targets.entrySet()) {
            var e = entry.getKey(); var snapshot = entry.getValue();
            e.setPos(snapshot.position); e.setDeltaMovement(Vec3.ZERO);
            e.setYRot(snapshot.yaw); e.setXRot(snapshot.pitch);
            if (e instanceof ServerPlayer p && level.getGameTime() % 10 == 0)
                p.connection.teleport(p.getX(), p.getY(), p.getZ(), snapshot.yaw, snapshot.pitch);
        }
        if (level.getGameTime() % 5 == 0) {
            for (int i = 0; i < 24; i++) {
                double a = Math.PI * 2 * i / 24;
                CrusaderEffects.burst(level, PartThreeStand.THE_WORLD, b.center.add(Math.cos(a) * RADIUS, 1, Math.sin(a) * RADIUS), 1);
            }
        }
    }
    public static void onEntityTick(EntityTickEvent.Pre event) {
        var e = event.getEntity();
        boolean clientFrozen = e.level().isClientSide() && !(e instanceof net.minecraft.world.entity.player.Player)
                && e.getExistingData(ModAttachments.TIME_FROZEN).orElse(false);
        if (isFrozen(e) || clientFrozen) event.setCanceled(true);
    }
    public static void onIncoming(LivingIncomingDamageEvent event) {
        if (event.getSource().getEntity() != null && isFrozen(event.getSource().getEntity())) event.setCanceled(true);
        if (event.getSource().getDirectEntity() != null && isFrozen(event.getSource().getDirectEntity())) event.setCanceled(true);
    }
    private static void release(Entity e, Snapshot snapshot) {
        e.setData(ModAttachments.TIME_FROZEN, false);
        if (!e.isRemoved()) { e.setDeltaMovement(snapshot.velocity); e.syncVelocity = true; }
    }
    public static void end(ServerLevel level) {
        var b = BUBBLES.remove(level); if (b == null) return;
        b.targets.forEach(TimeStop::release);
        CrusaderEffects.sound(level, PartThreeStand.THE_WORLD, "resume", b.center);
    }
    public static void cancel(ServerPlayer owner) {
        for (var level : java.util.List.copyOf(BUBBLES.keySet())) if (BUBBLES.get(level).owner == owner) end(level);
    }
    public static void onStopping(ServerStoppingEvent event) {
        for (var level : java.util.List.copyOf(BUBBLES.keySet())) if (level.getServer() == event.getServer()) end(level);
    }
}
