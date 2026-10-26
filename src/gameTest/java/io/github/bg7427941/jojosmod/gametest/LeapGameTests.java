package io.github.bg7427941.jojosmod.gametest;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.bg7427941.jojosmod.abilities.StarPlatinumActions;
import io.github.bg7427941.jojosmod.abilities.StarPlatinumLeap;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Server impulse and real fall-event coverage; native QA exercises client movement and landing. */
public final class LeapGameTests {
    private static final String MOD_ID = PhaseOneGameTests.MOD_ID;
    private static final List<String> CASES = List.of(
            "eligibility_and_survival", "direction_impulse_and_headroom", "combat_exclusivity_and_cooldowns",
            "bounded_first_landing", "airborne_and_grounded_edges", "expiry_and_owner_cleanup",
            "damage_and_dismissal", "movement_state_cleanup", "cooldown_and_independent_owners",
            "predicted_landing_preserves_grace");
    private static final DeferredRegister<MapCodec<? extends GameTestInstance>> TYPES =
            DeferredRegister.create(Registries.TEST_INSTANCE_TYPE, MOD_ID);

    static { TYPES.register("leap", () -> LeapTestInstance.CODEC); }

    private LeapGameTests() {}

    public static void register(IEventBus bus) {
        TYPES.register(bus);
        bus.addListener(LeapGameTests::registerTests);
    }

    private static void registerTests(RegisterGameTestsEvent event) {
        var environment = event.registerEnvironment(id("leap"),
                new TestEnvironmentDefinition.SetDifficulty(net.minecraft.world.Difficulty.NORMAL));
        for (var name : CASES) {
            var data = new TestData<>(environment, Level.OVERWORLD, id("star_arena"),
                    110, 0, true, Rotation.NONE, false, 1, 1, false, 0);
            event.registerTest(id("leap/" + name), new LeapTestInstance(data, name));
        }
    }

    private static Identifier id(String path) { return Identifier.fromNamespaceAndPath(MOD_ID, path); }

    private static Fixture fixture(GameTestHelper helper) {
        var owner = new TestPlayer(helper.getLevel());
        owner.snapTo(helper.absoluteVec(new Vec3(2.5, 1, 1.5)), 0, 0);
        owner.setOnGround(true);
        PlayerStands.assign(owner, ModStands.STAR_PLATINUM.id());
        PlayerWillpower.set(owner, 6);
        check(helper, StandController.toggle(owner), "Star Platinum could not summon");
        var fixture = new Fixture(owner, StandController.getActive(owner).orElseThrow());
        fixture.block(owner.blockPosition().below(), Blocks.STONE.defaultBlockState());
        return fixture;
    }

    private static void tick(Fixture fixture) {
        NeoForge.EVENT_BUS.post(new PlayerTickEvent.Pre(fixture.owner));
    }

    private static LivingFallEvent fall(Fixture fixture, double distance) {
        return NeoForge.EVENT_BUS.post(new LivingFallEvent(fixture.owner, distance, 1));
    }

    private static void airborne(Fixture fixture) {
        fixture.owner.setOnGround(false);
        fixture.owner.snapTo(fixture.owner.position().add(0, 0.5, 0), fixture.owner.getYRot(), fixture.owner.getXRot());
        tick(fixture);
    }

    private static void eligibilityAndSurvival(GameTestHelper helper) {
        var empty = new TestPlayer(helper.getLevel());
        empty.snapTo(helper.absoluteVec(new Vec3(2.5, 1, 1.5)), 0, 0);
        empty.setOnGround(true);
        try {
            PlayerWillpower.set(empty, 6);
            check(helper, !StarPlatinumLeap.leap(empty), "An unassigned player leapt");
            PlayerStands.assign(empty, ModStands.ECHOES_ACT_1.id());
            check(helper, StandController.toggle(empty) && !StarPlatinumLeap.leap(empty),
                    "Another active Stand could leap");
            StandController.getActive(empty).ifPresent(TrainingStandEntity::discard);
            StandController.dismiss(empty);
            PlayerStands.assign(empty, ModStands.STAR_PLATINUM.id());
            check(helper, !StarPlatinumLeap.leap(empty) && StarPlatinumLeap.session(empty).readyAt() == 0,
                    "An unsummoned Star Platinum leapt or consumed cooldown");
        } finally {
            StandController.getActive(empty).ifPresent(TrainingStandEntity::discard);
            empty.discard();
        }
        for (int scenario = 0; scenario < 6; scenario++) {
            try (var f = fixture(helper)) {
                if (scenario == 0) PlayerWillpower.set(f.owner, 3);
                if (scenario == 1) f.owner.setGameMode(GameType.SPECTATOR);
                if (scenario == 2) f.owner.setHealth(0);
                if (scenario == 3) f.owner.setOnGround(false);
                if (scenario == 4) f.owner.snapTo(f.owner.position().add(10, 0, 0), 0, 0);
                if (scenario == 5) f.owner.snapTo(f.owner.position().add(0, 2, 0), 0, 0);
                check(helper, !StarPlatinumLeap.leap(f.owner) && StarPlatinumLeap.session(f.owner).readyAt() == 0,
                        "An ineligible owner leapt or consumed cooldown: " + scenario);
            }
        }
        for (int state = 0; state < 6; state++) {
            try (var f = fixture(helper)) {
                movementState(helper, f, state);
                f.owner.setOnGround(true);
                check(helper, !StarPlatinumLeap.leap(f.owner) && StarPlatinumLeap.session(f.owner).readyAt() == 0,
                        "A prohibited movement state leapt or consumed cooldown: " + state);
            }
        }
        try (var f = fixture(helper)) {
            PlayerWillpower.set(f.owner, 4);
            check(helper, StarPlatinumProgress.LEAP_REQUIREMENT == 4 && StarPlatinumProgress.leapUnlocked(f.owner)
                    && StarPlatinumLeap.leap(f.owner), "Four willpower did not unlock a survival leap");
        }
        helper.succeed();
    }

    private static void directionImpulseAndHeadroom(GameTestHelper helper) {
        float[] yaws = {0, 90, 180, -90};
        Vec3[] expected = {new Vec3(0, .85, .85), new Vec3(-.85, .85, 0),
                new Vec3(0, .85, -.85), new Vec3(.85, .85, 0)};
        for (int direction = 0; direction < yaws.length; direction++) {
            try (var f = fixture(helper)) {
                f.owner.snapTo(f.owner.position(), yaws[direction], direction % 2 == 0 ? -70 : 70);
                f.owner.setDeltaMovement(new Vec3(3, -2, -4));
                f.owner.fallDistance = 12;
                f.owner.syncVelocity = false;
                f.owner.needsSync = false;
                var progress = StarPlatinumProgress.get(f.owner);
                long now = f.owner.level().getGameTime();
                check(helper, StarPlatinumLeap.leap(f.owner), "A grounded cardinal leap did not launch: " + direction);
                var leap = StarPlatinumLeap.session(f.owner);
                check(helper, f.owner.getDeltaMovement().distanceToSqr(expected[direction]) < 1.0E-10
                        && !f.owner.onGround() && f.owner.fallDistance == 0
                        && f.owner.syncVelocity && f.owner.needsSync,
                        "Leap did not replace momentum with a yaw-only synchronized impulse: " + direction);
                check(helper, leap.readyAt() == now + 80 && leap.protectionUntil() == now + 60
                        && leap.launchY() == f.owner.getY() && !leap.airborne()
                        && leap.dimension().equals(f.owner.level().dimension())
                        && StarPlatinumActions.session(f.owner).action() == Action.NONE
                        && PlayerWillpower.get(f.owner) == 6 && StarPlatinumProgress.get(f.owner).equals(progress),
                        "Leap changed combat, progression, or its bounded cooldown and grace state");
                check(helper, !StarPlatinumLeap.leap(f.owner), "A leap accepted an immediate airborne repeat");
            }
        }
        try (var f = fixture(helper)) {
            // A small gap keeps support within the server probe while separating body clearance from headroom.
            f.owner.snapTo(f.owner.position().add(0, .025, 0), 0, 0);
            f.owner.setOnGround(true);
            var ceiling = f.owner.blockPosition().above(2);
            f.block(ceiling, Blocks.STONE.defaultBlockState());
            check(helper, f.owner.level().noCollision(f.owner, f.owner.getBoundingBox())
                    && !f.owner.level().noCollision(f.owner, f.owner.getBoundingBox().move(0, .2, 0)),
                    "The ceiling fixture did not isolate the required headroom");
            check(helper, !StarPlatinumLeap.leap(f.owner) && StarPlatinumLeap.session(f.owner).readyAt() == 0,
                    "Leap ignored blocked headroom or charged its cooldown");
            f.block(ceiling, Blocks.AIR.defaultBlockState());
            check(helper, StarPlatinumLeap.leap(f.owner), "Removing the ceiling did not allow a leap");
        }
        helper.succeed();
    }

    private static void combatExclusivityAndCooldowns(GameTestHelper helper) {
        for (int action = 0; action < 5; action++) {
            try (var f = fixture(helper)) {
                boolean started = switch (action) {
                    case 0 -> StandController.attack(f.owner);
                    case 1 -> StarPlatinumActions.barrage(f.owner);
                    case 2 -> StarPlatinumActions.toggleGuard(f.owner);
                    case 3 -> StarPlatinumActions.starFinger(f.owner);
                    default -> StarPlatinumActions.intercept(f.owner);
                };
                var before = StarPlatinumActions.session(f.owner);
                check(helper, started && !StarPlatinumLeap.leap(f.owner)
                        && StarPlatinumLeap.session(f.owner).readyAt() == 0
                        && StarPlatinumActions.session(f.owner).equals(before),
                        "Leap stacked on or interrupted a combat action: " + action);
            }
        }
        try (var f = fixture(helper)) {
            long now = f.owner.level().getGameTime();
            var combat = new StarPlatinumSession(Action.NONE, 0, 0, now + 17, now + 23, now + 29, now + 31);
            f.owner.setData(ModAttachments.STAR_SESSION, combat);
            check(helper, StarPlatinumLeap.leap(f.owner) && StarPlatinumActions.session(f.owner).equals(combat),
                    "Other skills' cooldowns blocked leap or were overwritten by it");
            StarPlatinumActions.stop(f.owner);
            check(helper, StarPlatinumLeap.session(f.owner).readyAt() == now + 80,
                    "Stopping a combat action reset leap cooldown");
        }
        try (var f = fixture(helper)) {
            check(helper, StarPlatinumLeap.leap(f.owner) && StandController.attack(f.owner),
                    "The instant leap prevented a subsequent airborne precise strike");
        }
        helper.succeed();
    }

    private static void boundedFirstLanding(GameTestHelper helper) {
        try (var ordinary = fixture(helper); var large = fixture(helper); var observed = fixture(helper)) {
            check(helper, StarPlatinumLeap.leap(ordinary.owner) && StarPlatinumLeap.leap(large.owner)
                    && StarPlatinumLeap.leap(observed.owner), "Fall fixtures did not launch");
            long readyAt = StarPlatinumLeap.session(large.owner).readyAt();
            ordinary.owner.setOnGround(true);
            ordinary.owner.causeFallDamage(4, 1, ordinary.owner.damageSources().fall());
            check(helper, ordinary.owner.getHealth() == 20 && StarPlatinumLeap.session(ordinary.owner).protectionUntil() == 0,
                    "A four-block landing dealt damage or retained another landing's grace");
            large.owner.setOnGround(true);
            large.owner.causeFallDamage(14, 1, large.owner.damageSources().fall());
            check(helper, large.owner.getHealth() == 13 && StarPlatinumLeap.session(large.owner).protectionUntil() == 0
                    && StarPlatinumLeap.session(large.owner).readyAt() == readyAt,
                    "A large landing did not retain vanilla damage after four blocks of relief");
            large.owner.damageCooldownTime = 0;
            large.owner.causeFallDamage(5, 1, large.owner.damageSources().fall());
            check(helper, large.owner.getHealth() == 11, "A second fall was protected by the same leap");
            var event = fall(observed, 9);
            check(helper, !event.isCanceled() && event.getDistance() == 5 && event.getDamageMultiplier() == 1,
                    "Leap canceled a fall event or changed more than its four-block distance allowance");
            var second = fall(observed, 9);
            check(helper, !second.isCanceled() && second.getDistance() == 9,
                    "The first fall-event reduction leaked into a second fall");
        }
        helper.succeed();
    }

    private static void airborneAndGroundedEdges(GameTestHelper helper) {
        try (var eventLanding = fixture(helper); var tickLanding = fixture(helper); var falling = fixture(helper)) {
            check(helper, StarPlatinumLeap.leap(eventLanding.owner) && StarPlatinumLeap.leap(tickLanding.owner)
                    && StarPlatinumLeap.leap(falling.owner), "Airborne fixtures did not launch");
            eventLanding.owner.setOnGround(true);
            for (int check = 0; check < 3; check++) {
                var event = fall(eventLanding, 0);
                tick(eventLanding);
                check(helper, !event.isCanceled() && event.getDistance() == 0
                        && StarPlatinumLeap.session(eventLanding.owner).protectionUntil() > 0
                        && !StarPlatinumLeap.session(eventLanding.owner).airborne(),
                        "A launch-tick zero-distance ground check consumed grace");
            }
            airborne(eventLanding);
            check(helper, StarPlatinumLeap.session(eventLanding.owner).airborne(),
                    "An actual rise did not mark the leap airborne");
            eventLanding.owner.snapTo(eventLanding.owner.position().add(0, -0.5, 0), 0, 0);
            eventLanding.owner.setOnGround(true);
            var landed = fall(eventLanding, 0);
            check(helper, !landed.isCanceled() && StarPlatinumLeap.session(eventLanding.owner).protectionUntil() == 0
                    && fall(eventLanding, 8).getDistance() == 8,
                    "A first zero-distance landing did not consume grace after real airtime");
            airborne(tickLanding);
            tickLanding.owner.snapTo(tickLanding.owner.position().add(0, -0.5, 0), 0, 0);
            tickLanding.owner.setOnGround(true);
            tick(tickLanding);
            check(helper, StarPlatinumLeap.session(tickLanding.owner).protectionUntil() == 0,
                    "A grounded tick after airtime did not consume landing grace");
            falling.owner.fallDistance = .01;
            tick(falling);
            check(helper, StarPlatinumLeap.session(falling.owner).airborne(),
                    "Falling without an observed rise did not mark the leap airborne");
        }
        helper.succeed();
    }

    private static void predictedLandingPreservesGrace(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            Vec3 ground = f.owner.position();
            check(helper, StarPlatinumLeap.leap(f.owner), "Prediction fixture did not launch");
            airborne(f);
            var before = StarPlatinumLeap.session(f.owner);
            // Server travel predicts a supported landing in Post, then the connection rolls position back.
            f.owner.snapTo(ground, 0, 0);
            f.owner.setOnGround(true);
            NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(f.owner));
            check(helper, StarPlatinumLeap.session(f.owner).equals(before),
                    "A speculative post-travel landing consumed grace before the real fall packet");
            f.owner.snapTo(ground.add(0, 0.14, 0), 0, 0);
            tick(f);
            fall(f, 0);
            check(helper, StarPlatinumLeap.session(f.owner).equals(before),
                    "A stale onGround flag without physical support consumed landing grace");
            f.owner.snapTo(ground, 0, 0);
            f.owner.causeFallDamage(14, 1, f.owner.damageSources().fall());
            check(helper, f.owner.getHealth() == 13
                    && StarPlatinumLeap.session(f.owner).protectionUntil() == 0
                    && StarPlatinumLeap.session(f.owner).readyAt() == before.readyAt(),
                    "The authoritative landing lost its bounded fall relief after server prediction");
        }
        helper.succeed();
    }

    private static void expiryAndOwnerCleanup(GameTestHelper helper) {
        for (int invalid = 0; invalid < 6; invalid++) {
            try (var f = fixture(helper)) {
                check(helper, StarPlatinumLeap.leap(f.owner), "Owner cleanup fixture did not launch");
                long readyAt = StarPlatinumLeap.session(f.owner).readyAt();
                if (invalid == 0) PlayerStands.clear(f.owner);
                if (invalid == 1) PlayerStands.assign(f.owner, ModStands.ECHOES_ACT_1.id());
                if (invalid == 2) f.owner.setHealth(0);
                if (invalid == 3) f.owner.setGameMode(GameType.SPECTATOR);
                if (invalid == 4) {
                    var nether = helper.getLevel().getServer().getLevel(Level.NETHER);
                    check(helper, nether != null, "The dimension-change fixture has no Nether");
                    f.owner.setServerLevel(nether);
                }
                if (invalid == 5) f.owner.discard();
                tick(f);
                var event = fall(f, 8);
                check(helper, StarPlatinumLeap.session(f.owner).protectionUntil() == 0
                        && StarPlatinumLeap.session(f.owner).readyAt() == readyAt
                        && !event.isCanceled() && event.getDistance() == 8,
                        "An invalid owner retained grace or lost cooldown: " + invalid);
            }
        }
        var expires = fixture(helper);
        check(helper, StarPlatinumLeap.leap(expires.owner), "Expiry fixture did not launch");
        long started = expires.owner.level().getGameTime();
        long readyAt = StarPlatinumLeap.session(expires.owner).readyAt();
        var sawLastTick = new boolean[1];
        helper.onEachTick(() -> {
            tick(expires);
            long elapsed = expires.owner.level().getGameTime() - started;
            if (elapsed == 59) {
                check(helper, StarPlatinumLeap.session(expires.owner).protectionUntil() == started + 60,
                        "Landing grace expired before sixty ticks");
                sawLastTick[0] = true;
            } else if (elapsed == 60) {
                var event = fall(expires, 8);
                check(helper, sawLastTick[0] && StarPlatinumLeap.session(expires.owner).protectionUntil() == 0
                        && StarPlatinumLeap.session(expires.owner).readyAt() == readyAt
                        && !event.isCanceled() && event.getDistance() == 8,
                        "Expired grace protected a fall or erased the leap cooldown");
                expires.close(); helper.succeed();
            }
        });
    }

    private static void damageAndDismissal(GameTestHelper helper) {
        try (var damaged = fixture(helper); var dismissed = fixture(helper); var absorbed = fixture(helper)) {
            check(helper, StarPlatinumLeap.leap(damaged.owner) && StarPlatinumLeap.leap(dismissed.owner)
                    && StarPlatinumLeap.leap(absorbed.owner), "Damage fixtures did not launch");
            long readyAt = StarPlatinumLeap.session(damaged.owner).readyAt();
            airborne(damaged); airborne(dismissed); airborne(absorbed);
            check(helper, damaged.owner.hurtServer(helper.getLevel(), damaged.owner.damageSources().generic(), 1)
                    && damaged.owner.getHealth() == 19, "The owner did not receive real health damage");
            check(helper, StarPlatinumLeap.session(damaged.owner).protectionUntil() == 0
                    && StarPlatinumLeap.session(damaged.owner).readyAt() == readyAt
                    && fall(damaged, 8).getDistance() == 8,
                    "Health damage did not interrupt grace while retaining cooldown");
            var maximumAbsorption = absorbed.owner.getAttribute(Attributes.MAX_ABSORPTION);
            check(helper, maximumAbsorption != null, "The absorption fixture has no maximum-absorption attribute");
            maximumAbsorption.setBaseValue(2);
            absorbed.owner.setAbsorptionAmount(2);
            check(helper, absorbed.owner.hurtServer(helper.getLevel(), absorbed.owner.damageSources().generic(), 1)
                    && absorbed.owner.getHealth() == 20 && absorbed.owner.getAbsorptionAmount() == 1
                    && StarPlatinumLeap.session(absorbed.owner).protectionUntil() > 0,
                    "Absorbed damage incorrectly interrupted landing grace");
            StandController.dismiss(dismissed.owner);
            tick(dismissed);
            check(helper, StandController.getActive(dismissed.owner).isEmpty()
                    && StarPlatinumLeap.session(dismissed.owner).protectionUntil() > 0,
                    "Desummoning alone erased protection for the owner's landing");
            dismissed.owner.setOnGround(true);
            dismissed.owner.causeFallDamage(4, 1, dismissed.owner.damageSources().fall());
            check(helper, dismissed.owner.getHealth() == 20
                    && StarPlatinumLeap.session(dismissed.owner).protectionUntil() == 0
                    && StarPlatinumLeap.session(dismissed.owner).readyAt() == readyAt,
                    "A desummoned owner's first landing lost protection or refunded cooldown");
        }
        helper.succeed();
    }

    private static void movementStateCleanup(GameTestHelper helper) {
        for (int state = 0; state < 6; state++) {
            try (var f = fixture(helper)) {
                check(helper, StarPlatinumLeap.leap(f.owner), "Movement cleanup fixture did not launch");
                long readyAt = StarPlatinumLeap.session(f.owner).readyAt();
                movementState(helper, f, state);
                tick(f);
                var event = fall(f, 8);
                check(helper, StarPlatinumLeap.session(f.owner).protectionUntil() == 0
                        && StarPlatinumLeap.session(f.owner).readyAt() == readyAt
                        && !event.isCanceled() && event.getDistance() == 8,
                        "A prohibited movement state retained grace or refunded cooldown: " + state);
            }
        }
        helper.succeed();
    }

    private static void movementState(GameTestHelper helper, Fixture fixture, int state) {
        var owner = fixture.owner;
        switch (state) {
            case 0, 1 -> {
                fixture.block(owner.blockPosition(), (state == 0 ? Blocks.WATER : Blocks.LAVA).defaultBlockState());
                owner.refreshEnvironment();
                check(helper, state == 0 ? owner.isInWater() : owner.isInLava(),
                        "The fluid fixture did not enter its real fluid state: " + state);
            }
            case 2 -> {
                fixture.block(owner.blockPosition().south(), Blocks.STONE.defaultBlockState());
                fixture.block(owner.blockPosition(), Blocks.LADDER.defaultBlockState());
                owner.refreshEnvironment();
                check(helper, owner.onClimbable(), "The ladder fixture was not climbable");
            }
            case 3 -> {
                fixture.mount = EntityTypes.PIG.create(owner.level(), EntitySpawnReason.COMMAND);
                check(helper, fixture.mount != null, "The mounted fixture has no vehicle");
                fixture.mount.snapTo(owner.position(), 0, 0);
                check(helper, owner.startRiding(fixture.mount, true, false) && owner.isPassenger(),
                        "The passenger fixture did not mount");
            }
            case 4 -> {
                owner.startFallFlying();
                check(helper, owner.isFallFlying(), "The elytra fixture was not fall-flying");
            }
            case 5 -> {
                owner.getAbilities().flying = true;
                check(helper, owner.getAbilities().flying, "The flying fixture did not enable flight");
            }
            default -> throw new IllegalArgumentException("Unknown movement state " + state);
        }
    }

    private static void cooldownAndIndependentOwners(GameTestHelper helper) {
        var first = fixture(helper);
        var second = fixture(helper);
        check(helper, StarPlatinumLeap.leap(first.owner), "The first owner could not leap");
        long started = first.owner.level().getGameTime();
        long firstReady = StarPlatinumLeap.session(first.owner).readyAt();
        check(helper, StarPlatinumLeap.session(second.owner).readyAt() == 0
                && StarPlatinumLeap.session(second.owner).protectionUntil() == 0,
                "One owner's leap changed another owner's session");
        helper.onEachTick(() -> {
            tick(first); tick(second);
            long elapsed = first.owner.level().getGameTime() - started;
            if (elapsed == 3) {
                check(helper, StarPlatinumLeap.leap(second.owner)
                        && StarPlatinumLeap.session(second.owner).readyAt() == firstReady + 3,
                        "Another owner's cooldown prevented an independent leap");
                second.owner.setOnGround(true);
                var event = fall(second, 4);
                check(helper, !event.isCanceled() && event.getDistance() == 0
                        && StarPlatinumLeap.session(first.owner).protectionUntil() == started + 60,
                        "One owner's landing consumed another owner's grace");
            } else if (elapsed == 79) {
                first.owner.setOnGround(true);
                check(helper, !StarPlatinumLeap.leap(first.owner), "Leap recovered before eighty ticks");
            } else if (elapsed == 80) {
                first.owner.setOnGround(false);
                check(helper, !StarPlatinumLeap.leap(first.owner), "An airborne owner leapt after cooldown expired");
                first.owner.setOnGround(true);
                check(helper, StarPlatinumLeap.leap(first.owner)
                        && StarPlatinumLeap.session(first.owner).readyAt() == started + 160
                        && !StarPlatinumLeap.leap(second.owner)
                        && StarPlatinumLeap.session(second.owner).readyAt() == firstReady + 3,
                        "Leap did not recover exactly at eighty ticks or shared another owner's cooldown");
                first.close(); second.close(); helper.succeed();
            }
        });
    }

    private static void check(GameTestHelper helper, boolean condition, String message) {
        helper.assertTrue(condition, Component.literal(message));
    }

    private record BlockChange(ServerLevel level, BlockPos position, BlockState original) {}

    private static final class Fixture implements AutoCloseable {
        private final TestPlayer owner;
        private final TrainingStandEntity stand;
        private final List<BlockChange> blocks = new ArrayList<>();
        private Entity mount;

        private Fixture(TestPlayer owner, TrainingStandEntity stand) {
            this.owner = owner;
            this.stand = stand;
        }

        private void block(BlockPos position, BlockState state) {
            blocks.add(new BlockChange(owner.level(), position.immutable(), owner.level().getBlockState(position)));
            owner.level().setBlockAndUpdate(position, state);
        }

        @Override public void close() {
            owner.stopRiding();
            if (mount != null) mount.discard();
            StandController.getActive(owner).ifPresent(TrainingStandEntity::discard);
            StarPlatinumActions.stop(owner);
            stand.discard(); owner.discard();
            for (int i = blocks.size() - 1; i >= 0; i--) {
                var block = blocks.get(i);
                block.level().setBlockAndUpdate(block.position(), block.original());
            }
        }
    }

    /** A real loaded server connection allows LivingDamageEvent.Post to observe health damage. */
    private static final class TestPlayer extends ServerPlayer {
        private TestPlayer(ServerLevel level) {
            super(level.getServer(), level, new GameProfile(UUID.randomUUID(),
                    "Leap" + UUID.randomUUID().toString().substring(0, 8)), ClientInformation.createDefault());
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

        private void refreshEnvironment() {
            firstTick = false;
            baseTick();
        }
    }

    private static final class LeapTestInstance extends GameTestInstance {
        private static final MapCodec<LeapTestInstance> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                TestData.CODEC.fieldOf("data").forGetter(LeapTestInstance::info),
                Codec.STRING.fieldOf("case").forGetter(test -> test.name)).apply(instance, LeapTestInstance::new));
        private final String name;

        private LeapTestInstance(TestData<Holder<TestEnvironmentDefinition<?>>> data, String name) {
            super(data);
            this.name = name;
        }

        @Override public void run(GameTestHelper helper) {
            switch (name) {
                case "eligibility_and_survival" -> eligibilityAndSurvival(helper);
                case "direction_impulse_and_headroom" -> directionImpulseAndHeadroom(helper);
                case "combat_exclusivity_and_cooldowns" -> combatExclusivityAndCooldowns(helper);
                case "bounded_first_landing" -> boundedFirstLanding(helper);
                case "airborne_and_grounded_edges" -> airborneAndGroundedEdges(helper);
                case "expiry_and_owner_cleanup" -> expiryAndOwnerCleanup(helper);
                case "damage_and_dismissal" -> damageAndDismissal(helper);
                case "movement_state_cleanup" -> movementStateCleanup(helper);
                case "cooldown_and_independent_owners" -> cooldownAndIndependentOwners(helper);
                case "predicted_landing_preserves_grace" -> predictedLandingPreservesGrace(helper);
                default -> throw new IllegalArgumentException(name);
            }
        }

        @Override public MapCodec<? extends GameTestInstance> codec() { return CODEC; }
        @Override protected MutableComponent typeDescription() { return Component.literal(name); }
    }
}
