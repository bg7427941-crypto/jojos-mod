package io.github.bg7427941.jojosmod.gametest;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.bg7427941.jojosmod.abilities.StarPlatinumActions;
import io.github.bg7427941.jojosmod.entities.TrainingStandEntity;
import io.github.bg7427941.jojosmod.registry.ModAttachments;
import io.github.bg7427941.jojosmod.registry.ModStands;
import io.github.bg7427941.jojosmod.stands.PlayerStands;
import io.github.bg7427941.jojosmod.stands.PlayerWillpower;
import io.github.bg7427941.jojosmod.stands.StandController;
import io.github.bg7427941.jojosmod.stands.StarPlatinumProgress;
import io.github.bg7427941.jojosmod.stands.StarPlatinumSession;
import io.github.bg7427941.jojosmod.stands.StarPlatinumSession.Action;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.PacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Deterministic impact-event coverage; projectile flight is exercised separately in native QA. */
public final class InterceptionGameTests {
    private static final String MOD_ID = PhaseOneGameTests.MOD_ID;
    private static final Vec3 FRONT = new Vec3(0, 0, -1);
    private static final List<String> CASES = List.of(
            "gate_and_exclusivity", "window_edges", "direction_and_pitch", "types_and_hit_targets",
            "ownership_and_pvp", "single_catch_and_cooldown", "damage_and_dismiss_cleanup",
            "independent_owners", "other_cooldowns_and_cancellation");
    private static final DeferredRegister<MapCodec<? extends GameTestInstance>> TYPES =
            DeferredRegister.create(Registries.TEST_INSTANCE_TYPE, MOD_ID);

    static { TYPES.register("interception", () -> InterceptionTestInstance.CODEC); }

    private InterceptionGameTests() {}

    public static void register(IEventBus bus) {
        TYPES.register(bus);
        bus.addListener(InterceptionGameTests::registerTests);
    }

    private static void registerTests(RegisterGameTestsEvent event) {
        var environment = event.registerEnvironment(id("interception"),
                new TestEnvironmentDefinition.SetDifficulty(net.minecraft.world.Difficulty.NORMAL));
        for (var name : CASES) {
            var data = new TestData<>(environment, Level.OVERWORLD, id("star_arena"),
                    90, 0, true, Rotation.NONE, false, 1, 1, false, 0);
            event.registerTest(id("interception/" + name), new InterceptionTestInstance(data, name));
        }
    }

    private static Identifier id(String path) { return Identifier.fromNamespaceAndPath(MOD_ID, path); }

    private static Fixture fixture(GameTestHelper helper) {
        var owner = new TestPlayer(helper.getLevel());
        owner.snapTo(helper.absoluteVec(new Vec3(2.5, 1, 1.5)), 0, 0);
        PlayerStands.assign(owner, ModStands.STAR_PLATINUM.id());
        PlayerWillpower.set(owner, 6);
        check(helper, StandController.toggle(owner), "Star Platinum could not summon");
        return new Fixture(owner, StandController.getActive(owner).orElseThrow());
    }

    private static void tick(Fixture fixture) {
        NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(fixture.owner));
    }

    private static <P extends Projectile> P projectile(EntityType<P> type, ServerLevel level, Vec3 position, Vec3 velocity) {
        var projectile = type.create(level, EntitySpawnReason.COMMAND);
        if (projectile == null) throw new IllegalStateException("Cannot create test projectile " + type);
        projectile.snapTo(position, 0, 0);
        projectile.setNoGravity(true);
        projectile.setDeltaMovement(velocity);
        return projectile;
    }

    private static Arrow arrow(Fixture fixture, Vec3 velocity) {
        return projectile(EntityTypes.ARROW, fixture.owner.level(), fixture.owner.getEyePosition(), velocity);
    }

    private static ProjectileImpactEvent impact(Projectile projectile, HitResult result) {
        return NeoForge.EVENT_BUS.post(new ProjectileImpactEvent(projectile, result));
    }

    private static ProjectileImpactEvent impact(Projectile projectile, Entity target) {
        return impact(projectile, new EntityHitResult(target));
    }

    private static void reject(GameTestHelper helper, Projectile projectile, HitResult target, String message) {
        try {
            var event = impact(projectile, target);
            check(helper, !event.isCanceled() && !projectile.isRemoved(), message);
        } finally { projectile.discard(); }
    }

    private static void reject(GameTestHelper helper, Projectile projectile, Entity target, String message) {
        reject(helper, projectile, new EntityHitResult(target), message);
    }

    private static void catchArrow(GameTestHelper helper, Fixture fixture, Projectile projectile, String message) {
        try {
            var event = impact(projectile, fixture.owner);
            check(helper, event.isCanceled() && projectile.isRemoved()
                    && StarPlatinumActions.session(fixture.owner).action() == Action.NONE, message);
        } finally { projectile.discard(); }
    }

    private static void gateAndExclusivity(GameTestHelper helper) {
        var empty = new TestPlayer(helper.getLevel());
        try {
            PlayerWillpower.set(empty, 6);
            check(helper, !StarPlatinumActions.intercept(empty), "An unassigned player intercepted");
            PlayerStands.assign(empty, ModStands.ECHOES_ACT_1.id());
            check(helper, StandController.toggle(empty) && !StarPlatinumActions.intercept(empty),
                    "Another active Stand could intercept");
            StandController.getActive(empty).ifPresent(TrainingStandEntity::discard);
            StandController.dismiss(empty);
            PlayerStands.assign(empty, ModStands.STAR_PLATINUM.id());
            check(helper, !StarPlatinumActions.intercept(empty), "An unsummoned Star Platinum intercepted");
        } finally { empty.discard(); }
        for (int scenario = 0; scenario < 4; scenario++) {
            try (var f = fixture(helper)) {
                if (scenario == 0) PlayerWillpower.set(f.owner, 5);
                if (scenario == 1) f.owner.setGameMode(GameType.SPECTATOR);
                if (scenario == 2) f.owner.setHealth(0);
                if (scenario == 3) f.owner.snapTo(f.owner.position().add(10, 0, 0), 0, 0);
                check(helper, !StarPlatinumActions.intercept(f.owner)
                        && StarPlatinumActions.session(f.owner).interceptReadyAt() == 0,
                        "An ineligible owner started interception or consumed cooldown: " + scenario);
            }
        }
        try (var f = fixture(helper)) {
            long started = f.owner.level().getGameTime();
            check(helper, StarPlatinumActions.intercept(f.owner)
                    && StarPlatinumActions.session(f.owner).action() == Action.INTERCEPT
                    && StarPlatinumActions.session(f.owner).interceptReadyAt() == started + 60,
                    "Six willpower did not start interception with a three-second cooldown");
            check(helper, !StarPlatinumActions.intercept(f.owner) && !StarPlatinumActions.barrage(f.owner)
                    && !StarPlatinumActions.toggleGuard(f.owner) && !StarPlatinumActions.starFinger(f.owner)
                    && !StandController.attack(f.owner), "Another action stacked during interception");
        }
        for (int action = 0; action < 4; action++) {
            try (var f = fixture(helper)) {
                boolean started = switch (action) {
                    case 0 -> StandController.attack(f.owner);
                    case 1 -> StarPlatinumActions.barrage(f.owner);
                    case 2 -> StarPlatinumActions.toggleGuard(f.owner);
                    default -> StarPlatinumActions.starFinger(f.owner);
                };
                check(helper, started && !StarPlatinumActions.intercept(f.owner)
                        && StarPlatinumActions.session(f.owner).interceptReadyAt() == 0,
                        "Interception stacked on an existing action or consumed cooldown: " + action);
            }
        }
        helper.succeed();
    }

    private static void windowEdges(GameTestHelper helper) {
        var early = fixture(helper);
        var last = fixture(helper);
        var expired = fixture(helper);
        check(helper, StarPlatinumActions.intercept(early.owner) && StarPlatinumActions.intercept(last.owner)
                && StarPlatinumActions.intercept(expired.owner), "Window fixtures did not start");
        long started = StarPlatinumActions.session(early.owner).startedAt();
        long readyAt = StarPlatinumActions.session(expired.owner).interceptReadyAt();
        reject(helper, arrow(early, FRONT), early.owner, "An arrow was caught on the activation tick");
        var seen = new boolean[5];
        helper.onEachTick(() -> {
            tick(early); tick(last); tick(expired);
            long elapsed = early.owner.level().getGameTime() - started;
            if (elapsed == 1) {
                reject(helper, arrow(early, FRONT), early.owner, "Interception caught during its windup");
                seen[0] = true;
            } else if (elapsed == 2) {
                catchArrow(helper, early, arrow(early, FRONT), "The first active tick did not catch an arrow");
                seen[1] = true;
            } else if (elapsed == 9) {
                catchArrow(helper, last, arrow(last, FRONT), "The last active tick did not catch an arrow");
                seen[2] = true;
            } else if (elapsed == 10) {
                reject(helper, arrow(expired, FRONT), expired.owner, "The exclusive window end caught an arrow");
                check(helper, StarPlatinumActions.session(expired.owner).action() == Action.INTERCEPT,
                        "Interception ended before its recovery ticks");
                seen[3] = true;
            } else if (elapsed == 11) {
                reject(helper, arrow(expired, FRONT), expired.owner, "Interception caught during recovery");
                check(helper, StarPlatinumActions.session(expired.owner).action() == Action.INTERCEPT,
                        "Interception recovered before twelve ticks");
                seen[4] = true;
            } else if (elapsed == 12) {
                for (boolean observed : seen) check(helper, observed, "A timing boundary was not exercised");
                check(helper, StarPlatinumActions.session(expired.owner).action() == Action.NONE
                        && StarPlatinumActions.session(expired.owner).interceptReadyAt() == readyAt
                        && !StarPlatinumActions.intercept(expired.owner),
                        "A missed interception did not recover its action while retaining cooldown");
                early.close(); last.close(); expired.close(); helper.succeed();
            }
        });
    }

    private static void directionAndPitch(GameTestHelper helper) {
        var horizontal = fixture(helper);
        var pitched = fixture(helper);
        pitched.owner.snapTo(pitched.owner.position(), 0, -60);
        check(helper, StarPlatinumActions.intercept(horizontal.owner) && StarPlatinumActions.intercept(pitched.owner),
                "Direction fixtures did not start");
        helper.runAfterDelay(3, () -> {
            for (var velocity : List.of(new Vec3(0, 0, 1), new Vec3(1, 0, 0), new Vec3(0, 2, -1),
                    new Vec3(Math.sqrt(1 - .4999 * .4999), 0, -.4999), Vec3.ZERO, new Vec3(0, 0, -1.0E-8))) {
                reject(helper, arrow(horizontal, velocity), horizontal.owner,
                        "Interception accepted a rear, side, steep, outside-cone, or stationary arrow: " + velocity);
            }
            check(helper, StarPlatinumActions.session(horizontal.owner).action() == Action.INTERCEPT,
                    "Rejected directions consumed interception");
            catchArrow(helper, horizontal, arrow(horizontal, new Vec3(Math.sqrt(3) / 2, 0, -.5)),
                    "An arrow on the sixty-degree cone boundary was rejected");
            catchArrow(helper, pitched, arrow(pitched, pitched.owner.getLookAngle().scale(-1)),
                    "Interception ignored the owner's vertical aim");
            horizontal.close(); pitched.close(); helper.succeed();
        });
    }

    private static void typesAndHitTargets(GameTestHelper helper) {
        var f = fixture(helper);
        var other = new TestPlayer(helper.getLevel());
        check(helper, StarPlatinumActions.intercept(f.owner), "Projectile type fixture did not start");
        long started = StarPlatinumActions.session(f.owner).startedAt();
        long readyAt = StarPlatinumActions.session(f.owner).interceptReadyAt();
        var piercingTicks = new int[1];
        helper.onEachTick(() -> {
            tick(f);
            long elapsed = f.owner.level().getGameTime() - started;
            if (elapsed < 2 || elapsed >= 10) return;
            for (var type : List.of(EntityTypes.ARROW, EntityTypes.SPECTRAL_ARROW)) {
                var piercing = projectile(type, helper.getLevel(), f.owner.getEyePosition(), FRONT);
                var problems = new ProblemReporter.Collector();
                var output = TagValueOutput.createWithContext(problems, helper.getLevel().registryAccess());
                piercing.saveWithoutId(output);
                output.putByte("PierceLevel", (byte) 1);
                piercing.load(TagValueInput.create(problems, helper.getLevel().registryAccess(), output.buildResult()));
                check(helper, problems.isEmpty() && piercing.getPierceLevel() == 1,
                        "The Piercing arrow fixture did not load its vanilla pierce level");
                reject(helper, piercing, f.owner, "Interception changed a Piercing arrow on active tick " + elapsed + ": " + type);
                check(helper, StarPlatinumActions.session(f.owner).action() == Action.INTERCEPT
                        && StarPlatinumActions.session(f.owner).interceptReadyAt() == readyAt,
                        "A Piercing arrow consumed the action or changed its cooldown on tick " + elapsed);
            }
            piercingTicks[0]++;
            if (elapsed == 9) {
                check(helper, piercingTicks[0] == 8, "Piercing arrows were not checked throughout the active window");
                catchArrow(helper, f, projectile(EntityTypes.SPECTRAL_ARROW, helper.getLevel(), f.owner.getEyePosition(), FRONT),
                        "A vanilla spectral arrow was not intercepted after the Piercing arrows passed");
                other.discard(); f.close(); helper.succeed();
            }
        });
        helper.runAfterDelay(3, () -> {
            for (var type : List.of(EntityTypes.TRIDENT, EntityTypes.SPLASH_POTION, EntityTypes.LINGERING_POTION,
                    EntityTypes.FIREBALL, EntityTypes.SNOWBALL)) {
                reject(helper, projectile(type, helper.getLevel(), f.owner.getEyePosition(), FRONT), f.owner,
                        "Interception changed an unsupported projectile: " + type);
            }
            reject(helper, arrow(f, FRONT), new BlockHitResult(f.owner.position(), Direction.NORTH,
                    f.owner.blockPosition(), false), "Interception changed an arrow's block impact");
            reject(helper, arrow(f, FRONT), other, "Interception caught an arrow hitting another player");
            reject(helper, arrow(f, FRONT), f.stand, "Interception caught an arrow hitting the Stand");
            var removed = arrow(f, FRONT);
            removed.discard();
            check(helper, !impact(removed, f.owner).isCanceled(), "Interception processed an already removed arrow");
            var nether = helper.getLevel().getServer().getLevel(Level.NETHER);
            check(helper, nether != null, "The cross-dimension fixture has no Nether");
            reject(helper, projectile(EntityTypes.ARROW, nether, f.owner.getEyePosition(), FRONT), f.owner,
                    "Interception processed an arrow from another dimension");
            check(helper, StarPlatinumActions.session(f.owner).action() == Action.INTERCEPT,
                    "An unrelated impact consumed interception");
        });
    }

    private static void ownershipAndPvp(GameTestHelper helper) {
        var f = fixture(helper);
        var playerTarget = fixture(helper);
        var shooter = new TestPlayer(helper.getLevel());
        var ally = helper.spawn(EntityTypes.HUSK, new BlockPos(5, 1, 5));
        ally.setNoAi(true);
        ally.setNoGravity(true);
        var board = helper.getLevel().getScoreboard();
        var team = board.addPlayerTeam("catch_" + UUID.randomUUID().toString().substring(0, 8));
        team.setAllowFriendlyFire(false);
        board.addPlayerToTeam(f.owner.getScoreboardName(), team);
        board.addPlayerToTeam(ally.getScoreboardName(), team);
        check(helper, StarPlatinumActions.intercept(f.owner) && StarPlatinumActions.intercept(playerTarget.owner),
                "Ownership fixtures did not start");
        helper.runAfterDelay(3, () -> {
            var self = arrow(f, FRONT);
            self.setOwner(f.owner);
            reject(helper, self, f.owner, "Interception consumed a self-owned arrow");
            var allied = arrow(f, FRONT);
            allied.setOwner(ally);
            reject(helper, allied, f.owner, "Interception consumed an allied creature's arrow");
            board.addPlayerToTeam(shooter.getScoreboardName(), team);
            var friendly = arrow(f, FRONT);
            friendly.setOwner(shooter);
            reject(helper, friendly, f.owner, "Interception consumed a teammate's arrow");
            board.removePlayerFromTeam(shooter.getScoreboardName(), team);
            shooter.allowPlayerHarm = false;
            var protectedArrow = arrow(playerTarget, FRONT);
            protectedArrow.setOwner(shooter);
            reject(helper, protectedArrow, playerTarget.owner, "Interception ignored canHarmPlayer protection");
            shooter.allowPlayerHarm = true;
            var hostilePlayerArrow = arrow(playerTarget, FRONT);
            hostilePlayerArrow.setOwner(shooter);
            boolean permitted = helper.getLevel().isPvpAllowed() && shooter.canHarmPlayer(playerTarget.owner);
            if (permitted) {
                catchArrow(helper, playerTarget, hostilePlayerArrow, "A permitted hostile player's arrow was rejected");
            } else {
                reject(helper, hostilePlayerArrow, playerTarget.owner, "Interception consumed an arrow while PvP was disabled");
            }
            board.removePlayerFromTeam(ally.getScoreboardName(), team);
            var hostile = arrow(f, FRONT);
            hostile.setOwner(ally);
            catchArrow(helper, f, hostile, "A hostile creature's arrow was rejected after excluded impacts");
            board.removePlayerTeam(team);
            ally.discard(); shooter.discard(); f.close(); playerTarget.close(); helper.succeed();
        });
    }

    private static void singleCatchAndCooldown(GameTestHelper helper) {
        var f = fixture(helper);
        var progress = StarPlatinumProgress.get(f.owner);
        int arrows = f.owner.getInventory().countItem(Items.ARROW);
        int spectral = f.owner.getInventory().countItem(Items.SPECTRAL_ARROW);
        check(helper, StarPlatinumActions.intercept(f.owner), "Single-catch fixture did not start");
        long started = StarPlatinumActions.session(f.owner).startedAt();
        long readyAt = StarPlatinumActions.session(f.owner).interceptReadyAt();
        helper.onEachTick(() -> {
            tick(f);
            long elapsed = f.owner.level().getGameTime() - started;
            if (elapsed == 2) {
                var arrow = arrow(f, FRONT);
                arrow.pickup = AbstractArrow.Pickup.ALLOWED;
                check(helper, helper.getLevel().addFreshEntity(arrow), "The catchable arrow could not enter the world");
                int drops = helper.getLevel().getEntities(EntityTypes.ITEM, f.owner.getBoundingBox().inflate(3), item -> true).size();
                catchArrow(helper, f, arrow, "The first frontal arrow was not consumed");
                reject(helper, arrow(f, FRONT), f.owner, "One activation caught a second arrow");
                check(helper, f.owner.getHealth() == 20 && f.owner.getInventory().countItem(Items.ARROW) == arrows
                        && f.owner.getInventory().countItem(Items.SPECTRAL_ARROW) == spectral
                        && helper.getLevel().getEntities(EntityTypes.ITEM, f.owner.getBoundingBox().inflate(3), item -> true).size() == drops
                        && PlayerWillpower.get(f.owner) == 6 && StarPlatinumProgress.get(f.owner).equals(progress),
                        "Catching an arrow dealt damage, dropped an item, or awarded inventory, willpower, or mastery");
                check(helper, StarPlatinumActions.session(f.owner).interceptReadyAt() == readyAt
                        && !StarPlatinumActions.intercept(f.owner), "A catch reset or skipped interception cooldown");
            } else if (elapsed == 59) {
                check(helper, !StarPlatinumActions.intercept(f.owner), "Interception recovered before sixty ticks");
            } else if (elapsed == 60) {
                check(helper, StarPlatinumActions.intercept(f.owner), "Interception did not recover exactly sixty ticks after activation");
                f.close(); helper.succeed();
            }
        });
    }

    private static void damageAndDismissCleanup(GameTestHelper helper) {
        var damaged = fixture(helper);
        var dismissed = fixture(helper);
        check(helper, StarPlatinumActions.intercept(damaged.owner) && StarPlatinumActions.intercept(dismissed.owner),
                "Cleanup fixtures did not start");
        long damagedReady = StarPlatinumActions.session(damaged.owner).interceptReadyAt();
        long dismissedReady = StarPlatinumActions.session(dismissed.owner).interceptReadyAt();
        helper.onEachTick(() -> { tick(damaged); tick(dismissed); });
        helper.runAfterDelay(3, () -> {
            check(helper, damaged.owner.hurtServer(helper.getLevel(), damaged.owner.damageSources().generic(), 1)
                    && damaged.owner.getHealth() < 20, "The server player did not receive real health damage");
            StandController.dismiss(dismissed.owner);
            check(helper, StarPlatinumActions.session(damaged.owner).action() == Action.NONE
                    && StarPlatinumActions.session(dismissed.owner).action() == Action.NONE
                    && StandController.getActive(dismissed.owner).isEmpty(),
                    "Health damage or dismissal left interception running");
            reject(helper, arrow(damaged, FRONT), damaged.owner, "A damaged owner still intercepted");
            reject(helper, arrow(dismissed, FRONT), dismissed.owner, "A dismissed Stand still intercepted");
        });
        helper.runAfterDelay(7, () -> {
            check(helper, StandController.toggle(dismissed.owner), "The dismissed Stand could not resummon");
            check(helper, StarPlatinumActions.session(damaged.owner).interceptReadyAt() == damagedReady
                    && StarPlatinumActions.session(dismissed.owner).interceptReadyAt() == dismissedReady
                    && !StarPlatinumActions.intercept(damaged.owner) && !StarPlatinumActions.intercept(dismissed.owner),
                    "Damage, dismissal, or resummoning erased the interception cooldown");
            damaged.close(); dismissed.close(); helper.succeed();
        });
    }

    private static void independentOwners(GameTestHelper helper) {
        var first = fixture(helper);
        var second = fixture(helper);
        check(helper, StarPlatinumActions.intercept(first.owner), "The first owner could not start interception");
        long firstReady = StarPlatinumActions.session(first.owner).interceptReadyAt();
        helper.onEachTick(() -> { tick(first); tick(second); });
        helper.runAfterDelay(2, () -> {
            catchArrow(helper, first, arrow(first, FRONT), "The first owner did not catch its arrow");
            check(helper, StarPlatinumActions.session(second.owner).action() == Action.NONE
                    && StarPlatinumActions.session(second.owner).interceptReadyAt() == 0,
                    "One owner's catch or cooldown changed another owner");
        });
        helper.runAfterDelay(3, () -> check(helper, StarPlatinumActions.intercept(second.owner),
                "Another owner's cooldown prevented independent activation"));
        helper.runAfterDelay(5, () -> {
            reject(helper, arrow(first, FRONT), first.owner, "The second owner's action protected the first owner");
            check(helper, StarPlatinumActions.session(second.owner).action() == Action.INTERCEPT,
                    "An impact on another owner consumed the second owner's action");
            catchArrow(helper, second, arrow(second, FRONT), "The second owner did not catch independently");
            check(helper, StarPlatinumActions.session(first.owner).interceptReadyAt() == firstReady
                    && StarPlatinumActions.session(second.owner).interceptReadyAt() == firstReady + 3,
                    "Independent owners shared or overwrote cooldown timestamps");
            first.close(); second.close(); helper.succeed();
        });
    }

    private static void otherCooldownsAndCancellation(GameTestHelper helper) {
        var f = fixture(helper);
        long now = f.owner.level().getGameTime();
        f.owner.setData(ModAttachments.STAR_SESSION, new StarPlatinumSession(Action.NONE, 0, 0,
                now + 17, now + 23, now + 29, 0));
        check(helper, StarPlatinumActions.intercept(f.owner), "Other skills' cooldowns prevented interception");
        long readyAt = StarPlatinumActions.session(f.owner).interceptReadyAt();
        StarPlatinumActions.stop(f.owner);
        var finished = StarPlatinumActions.session(f.owner);
        check(helper, finished.action() == Action.NONE && finished.barrageReadyAt() == now + 17
                && finished.guardReadyAt() == now + 23 && finished.fingerReadyAt() == now + 29
                && finished.interceptReadyAt() == readyAt && !StarPlatinumActions.intercept(f.owner),
                "Cancellation changed another cooldown or refunded interception");
        helper.onEachTick(() -> tick(f));
        helper.runAfterDelay(30, () -> {
            for (int action = 0; action < 4; action++) {
                boolean accepted = switch (action) {
                    case 0 -> StarPlatinumActions.barrage(f.owner);
                    case 1 -> StarPlatinumActions.toggleGuard(f.owner);
                    case 2 -> StarPlatinumActions.starFinger(f.owner);
                    default -> StandController.attack(f.owner);
                };
                check(helper, accepted && StarPlatinumActions.session(f.owner).interceptReadyAt() == readyAt,
                        "A different skill was blocked by or erased interception cooldown: " + action);
                StarPlatinumActions.stop(f.owner);
                check(helper, StarPlatinumActions.session(f.owner).interceptReadyAt() == readyAt,
                        "Finishing a different skill erased interception cooldown");
            }
        });
        helper.runAfterDelay(59, () -> check(helper, !StarPlatinumActions.intercept(f.owner),
                "A canceled interception recovered early"));
        helper.runAfterDelay(60, () -> {
            check(helper, StarPlatinumActions.intercept(f.owner), "A canceled interception did not recover after sixty ticks");
            f.close(); helper.succeed();
        });
    }

    private static void check(GameTestHelper helper, boolean condition, String message) {
        helper.assertTrue(condition, Component.literal(message));
    }

    private record Fixture(TestPlayer owner, TrainingStandEntity stand) implements AutoCloseable {
        @Override public void close() {
            StandController.getActive(owner).ifPresent(TrainingStandEntity::discard);
            StarPlatinumActions.stop(owner);
            stand.discard(); owner.discard();
        }
    }

    /** Uses the same loaded server connection as MasteryGameTests so real health damage fires. */
    private static final class TestPlayer extends ServerPlayer {
        private boolean allowPlayerHarm = true;

        private TestPlayer(ServerLevel level) {
            super(level.getServer(), level, new GameProfile(UUID.randomUUID(),
                    "Catch" + UUID.randomUUID().toString().substring(0, 8)), ClientInformation.createDefault());
            var transport = new net.minecraft.network.Connection(PacketFlow.SERVERBOUND) {
                @Override public void setListenerForServerboundHandshake(PacketListener listener) {}
            };
            new EmbeddedChannel(transport);
            connection = new ServerGamePacketListenerImpl(level.getServer(), transport,
                    this, CommonListenerCookie.createInitial(getGameProfile(), false)) {
                @Override public void send(Packet<?> packet) {}
                @Override public void tick() {}
            };
            connection.markClientLoaded();
            setGameMode(GameType.SURVIVAL);
            setNoGravity(true);
        }

        @Override public boolean canHarmPlayer(Player target) {
            return allowPlayerHarm && super.canHarmPlayer(target);
        }
    }

    private static final class InterceptionTestInstance extends GameTestInstance {
        private static final MapCodec<InterceptionTestInstance> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                TestData.CODEC.fieldOf("data").forGetter(InterceptionTestInstance::info),
                Codec.STRING.fieldOf("case").forGetter(test -> test.name)).apply(instance, InterceptionTestInstance::new));
        private final String name;

        private InterceptionTestInstance(TestData<Holder<TestEnvironmentDefinition<?>>> data, String name) {
            super(data);
            this.name = name;
        }

        @Override public void run(GameTestHelper helper) {
            switch (name) {
                case "gate_and_exclusivity" -> gateAndExclusivity(helper);
                case "window_edges" -> windowEdges(helper);
                case "direction_and_pitch" -> directionAndPitch(helper);
                case "types_and_hit_targets" -> typesAndHitTargets(helper);
                case "ownership_and_pvp" -> ownershipAndPvp(helper);
                case "single_catch_and_cooldown" -> singleCatchAndCooldown(helper);
                case "damage_and_dismiss_cleanup" -> damageAndDismissCleanup(helper);
                case "independent_owners" -> independentOwners(helper);
                case "other_cooldowns_and_cancellation" -> otherCooldownsAndCancellation(helper);
                default -> throw new IllegalArgumentException(name);
            }
        }

        @Override public MapCodec<? extends GameTestInstance> codec() { return CODEC; }
        @Override protected MutableComponent typeDescription() { return Component.literal(name); }
    }
}
