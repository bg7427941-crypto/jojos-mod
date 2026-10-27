package io.github.bg7427941.jojosmod.gametest;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
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
import io.github.bg7427941.jojosmod.stands.StarPlatinumProgress.Milestone;
import io.github.bg7427941.jojosmod.stands.StarPlatinumSession.Action;
import io.github.bg7427941.jojosmod.stands.WillpowerData;
import io.netty.buffer.Unpooled;
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
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.zombie.Husk;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Exercises the timed skill and persistent rewards with real server combat and player fixtures. */
public final class MasteryGameTests {
    private static final String MOD_ID = PhaseOneGameTests.MOD_ID;
    private static final List<String> CASES = List.of(
            "finger_gate_and_windup", "finger_walls", "finger_miss_and_cooldown", "finger_interruption",
            "finger_dismiss_and_invalid", "precise_progress_integration", "barrage_progress_integration",
            "guard_progress_integration", "mastery_eligibility", "mastery_coop_and_unique_targets",
            "mastery_save_death_and_network", "legacy_data_and_reward_cap", "finger_training_integration",
            "finger_training_absorption", "advanced_training_eligibility", "advanced_training_coop",
            "advanced_training_save_and_network", "advanced_training_legacy_and_bounds");
    private static final DeferredRegister<MapCodec<? extends GameTestInstance>> TYPES =
            DeferredRegister.create(Registries.TEST_INSTANCE_TYPE, MOD_ID);

    static { TYPES.register("mastery", () -> MasteryTestInstance.CODEC); }

    private MasteryGameTests() {}

    public static void register(IEventBus bus) {
        TYPES.register(bus);
        bus.addListener(MasteryGameTests::registerTests);
    }

    private static void registerTests(RegisterGameTestsEvent event) {
        var environment = event.registerEnvironment(id("mastery"),
                new TestEnvironmentDefinition.SetDifficulty(net.minecraft.world.Difficulty.NORMAL));
        for (var name : CASES) {
            var data = new TestData<>(environment, Level.OVERWORLD, id("star_arena"),
                    name.equals("finger_training_integration") ? 370 : 170,
                    0, true, Rotation.NONE, false, 1, 1, false, 0);
            event.registerTest(id("mastery/" + name), new MasteryTestInstance(data, name));
        }
    }

    private static Identifier id(String path) { return Identifier.fromNamespaceAndPath(MOD_ID, path); }

    private static Fixture fixture(GameTestHelper helper) {
        var owner = player(helper);
        check(helper, StandController.toggle(owner), "Star Platinum could not summon");
        var target = helper.spawn(EntityTypes.VILLAGER, new BlockPos(1, 1, 5));
        target.setNoAi(true);
        target.setNoGravity(true);
        target.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
        target.snapTo(owner.position().add(0, 0, 5), 0, 0);
        return new Fixture(owner, StandController.getActive(owner).orElseThrow(), target);
    }

    private static TestPlayer player(GameTestHelper helper) {
        var owner = new TestPlayer(helper.getLevel());
        owner.snapTo(helper.absoluteVec(new Vec3(1.5, 1, .5)), 0, 0);
        PlayerStands.assign(owner, ModStands.STAR_PLATINUM.id());
        return owner;
    }

    private static Husk enemy(GameTestHelper helper, Vec3 position) {
        var enemy = helper.spawn(EntityTypes.HUSK, new BlockPos(4, 1, 5));
        enemy.setNoAi(true);
        enemy.setNoGravity(true);
        enemy.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
        enemy.snapTo(position, 0, 0);
        return enemy;
    }

    private static List<Husk> enemies(GameTestHelper helper, TestPlayer owner) {
        var enemies = new ArrayList<Husk>();
        for (int i = 0; i < 3; i++) enemies.add(enemy(helper, owner.position().add(3 + i * .5, 0, 4)));
        return enemies;
    }

    private static void tick(Fixture fixture) { NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(fixture.owner)); }

    private static void fingerGateAndWindup(GameTestHelper helper) {
        var f = fixture(helper);
        PlayerWillpower.set(f.owner, 4);
        check(helper, !StarPlatinumActions.starFinger(f.owner), "Four willpower bypassed Star Finger's unlock requirement");
        PlayerWillpower.set(f.owner, 5);
        helper.onEachTick(() -> tick(f));
        check(helper, StarPlatinumActions.starFinger(f.owner), "Five willpower did not unlock Star Finger");
        check(helper, StarPlatinumActions.session(f.owner).action() == Action.STAR_FINGER
                && !StarPlatinumActions.starFinger(f.owner) && !StarPlatinumActions.barrage(f.owner)
                && !StarPlatinumActions.toggleGuard(f.owner) && !StandController.attack(f.owner),
                "An action stacked during Star Finger");
        helper.runAfterDelay(7, () -> check(helper, f.target.getHealth() == 20, "Star Finger landed before its eight-tick windup"));
        helper.runAfterDelay(10, () -> check(helper, f.target.getHealth() == 16,
                "Star Finger did not deal four damage beyond the basic attack's reach"));
        helper.runAfterDelay(20, () -> {
            check(helper, StarPlatinumActions.session(f.owner).action() == Action.NONE && !StarPlatinumActions.starFinger(f.owner),
                    "Star Finger did not recover its action or ignored its longer cooldown");
            f.close(); helper.succeed();
        });
    }

    private static void fingerWalls(GameTestHelper helper) {
        var f = fixture(helper);
        PlayerWillpower.set(f.owner, 5);
        helper.setBlock(1, 2, 2, Blocks.STONE);
        helper.onEachTick(() -> tick(f));
        check(helper, StarPlatinumActions.starFinger(f.owner), "The wall fixture did not start Star Finger");
        helper.runAfterDelay(20, () -> {
            check(helper, f.target.getHealth() == 20 && !StarPlatinumActions.starFinger(f.owner),
                    "Star Finger crossed a wall or a blocked attempt skipped cooldown");
            f.close(); helper.succeed();
        });
    }

    private static void fingerMissAndCooldown(GameTestHelper helper) {
        var f = fixture(helper);
        PlayerWillpower.set(f.owner, 5);
        // Keep the miss target inside the arena's barrier walls to avoid suffocation damage.
        f.target.snapTo(f.owner.position().add(4, 0, 5.5), 0, 0);
        f.owner.snapTo(f.owner.position(), (float) Math.toDegrees(Math.atan2(-4, 5.5)), 0);
        helper.onEachTick(() -> tick(f));
        check(helper, StarPlatinumActions.starFinger(f.owner), "An empty Star Finger attempt was not accepted");
        long readyAt = StarPlatinumActions.session(f.owner).fingerReadyAt();
        helper.runAfterDelay(20, () -> {
            check(helper, f.target.getHealth() == 20,
                    "Star Finger damaged the out-of-range target: " + f.target.getHealth() + " at " + f.target.distanceTo(f.owner));
            check(helper, !StarPlatinumActions.starFinger(f.owner), "A miss skipped Star Finger cooldown");
            check(helper, StarPlatinumActions.session(f.owner).fingerReadyAt() == readyAt,
                    "A miss changed Star Finger's ready time");
            f.target.snapTo(f.owner.position().add(0, 0, 5), 0, 0);
            f.owner.snapTo(f.owner.position(), 0, 0);
        });
        helper.runAfterDelay(119, () -> check(helper, !StarPlatinumActions.starFinger(f.owner), "Star Finger recovered before six seconds"));
        helper.runAfterDelay(122, () -> check(helper, StarPlatinumActions.starFinger(f.owner), "Star Finger did not recover after six seconds"));
        helper.runAfterDelay(133, () -> {
            check(helper, f.target.getHealth() == 16, "Recovered Star Finger did not resolve once");
            f.close(); helper.succeed();
        });
    }

    private static void fingerInterruption(GameTestHelper helper) {
        var f = fixture(helper);
        PlayerWillpower.set(f.owner, 5);
        helper.onEachTick(() -> tick(f));
        check(helper, StarPlatinumActions.starFinger(f.owner), "The interruption fixture did not start");
        long readyAt = StarPlatinumActions.session(f.owner).fingerReadyAt();
        helper.runAfterDelay(4, () -> {
            check(helper, f.owner.hurtServer(helper.getLevel(), f.owner.damageSources().generic(), 1), "Real player fixture did not receive damage");
            check(helper, StarPlatinumActions.session(f.owner).action() == Action.NONE, "Health damage did not interrupt Star Finger");
        });
        helper.runAfterDelay(20, () -> {
            check(helper, f.target.getHealth() == 20 && StarPlatinumActions.session(f.owner).fingerReadyAt() == readyAt
                    && !StarPlatinumActions.starFinger(f.owner), "An interrupted skill landed or reset its cooldown");
            f.close(); helper.succeed();
        });
    }

    private static void fingerDismissAndInvalid(GameTestHelper helper) {
        var empty = new TestPlayer(helper.getLevel());
        PlayerWillpower.set(empty, 5);
        check(helper, !StarPlatinumActions.starFinger(empty), "Unassigned player used Star Finger");
        PlayerStands.assign(empty, ModStands.ECHOES_ACT_1.id());
        check(helper, !StarPlatinumActions.starFinger(empty), "Another Stand used Star Finger");
        empty.discard();
        for (int scenario = 0; scenario < 3; scenario++) {
            try (var invalid = fixture(helper)) {
                PlayerWillpower.set(invalid.owner, 5);
                if (scenario == 0) invalid.owner.setGameMode(GameType.SPECTATOR);
                if (scenario == 1) invalid.owner.setHealth(0);
                if (scenario == 2) invalid.owner.snapTo(invalid.owner.position().add(10, 0, 0), 0, 0);
                check(helper, !StarPlatinumActions.starFinger(invalid.owner), "An ineligible or separated owner used Star Finger");
            }
        }
        var f = fixture(helper);
        PlayerWillpower.set(f.owner, 5);
        helper.onEachTick(() -> tick(f));
        check(helper, StarPlatinumActions.starFinger(f.owner), "The dismissal fixture did not start");
        long readyAt = StarPlatinumActions.session(f.owner).fingerReadyAt();
        StandController.dismiss(f.owner);
        helper.runAfterDelay(20, () -> {
            check(helper, f.target.getHealth() == 20 && StarPlatinumActions.session(f.owner).action() == Action.NONE
                    && StarPlatinumActions.session(f.owner).fingerReadyAt() == readyAt,
                    "Dismissal left an attack running or erased the skill cooldown");
            check(helper, StandController.toggle(f.owner) && !StarPlatinumActions.starFinger(f.owner),
                    "Resummoning reset the Star Finger cooldown");
            f.close(); helper.succeed();
        });
    }

    private static void preciseProgressIntegration(GameTestHelper helper) {
        var f = fixture(helper);
        f.target.discard();
        var enemies = enemies(helper, f.owner);
        helper.onEachTick(() -> tick(f));
        enemies.get(0).snapTo(f.owner.position().add(0, 0, 2), 0, 0);
        check(helper, StandController.attack(f.owner), "The precise mastery fixture did not attack");
        helper.runAfterDelay(7, () -> check(helper, StarPlatinumProgress.get(f.owner).count(Milestone.PRECISE) == 1
                && PlayerWillpower.get(f.owner) == 3, "A real hostile strike did not record exactly one opponent"));
        helper.runAfterDelay(18, () -> check(helper, StandController.attack(f.owner), "Repeated opponent fixture could not attack"));
        helper.runAfterDelay(25, () -> {
            check(helper, StarPlatinumProgress.get(f.owner).count(Milestone.PRECISE) == 1, "Repeated strikes on the same enemy farmed mastery");
            enemies.get(0).snapTo(f.owner.position().add(3, 0, 4), 0, 0);
            enemies.get(1).snapTo(f.owner.position().add(0, 0, 2), 0, 0);
        });
        helper.runAfterDelay(36, () -> check(helper, StandController.attack(f.owner), "Second opponent attack did not start"));
        helper.runAfterDelay(43, () -> {
            check(helper, StarPlatinumProgress.get(f.owner).count(Milestone.PRECISE) == 2 && PlayerWillpower.get(f.owner) == 3,
                    "Willpower was awarded before the third distinct opponent");
            enemies.get(1).snapTo(f.owner.position().add(3.5, 0, 4), 0, 0);
            enemies.get(2).snapTo(f.owner.position().add(0, 0, 2), 0, 0);
        });
        helper.runAfterDelay(54, () -> check(helper, StandController.attack(f.owner), "Third opponent attack did not start"));
        helper.runAfterDelay(63, () -> {
            check(helper, StarPlatinumProgress.get(f.owner).completed(Milestone.PRECISE) && PlayerWillpower.get(f.owner) == 4,
                    "Three real hostile strikes did not award exactly one willpower");
            enemies.forEach(Husk::discard); f.close(); helper.succeed();
        });
    }

    private static void barrageProgressIntegration(GameTestHelper helper) {
        var f = fixture(helper);
        f.target.discard();
        var enemies = enemies(helper, f.owner);
        helper.onEachTick(() -> tick(f));
        enemies.get(0).snapTo(f.owner.position().add(0, 0, 2), 0, 0);
        check(helper, StarPlatinumActions.barrage(f.owner), "The barrage mastery fixture did not start");
        helper.runAfterDelay(6, () -> {
            check(helper, StarPlatinumProgress.get(f.owner).count(Milestone.BARRAGE) == 1, "A barrage pulse did not record its hostile opponent");
            enemies.get(0).snapTo(f.owner.position().add(3, 0, 4), 0, 0);
            enemies.get(1).snapTo(f.owner.position().add(0, 0, 2), 0, 0);
        });
        helper.runAfterDelay(9, () -> {
            check(helper, StarPlatinumProgress.get(f.owner).count(Milestone.BARRAGE) == 2, "Barrage did not track a newly aimed opponent");
            enemies.get(1).snapTo(f.owner.position().add(3.5, 0, 4), 0, 0);
            enemies.get(2).snapTo(f.owner.position().add(0, 0, 2), 0, 0);
        });
        helper.runAfterDelay(28, () -> {
            check(helper, StarPlatinumProgress.get(f.owner).completed(Milestone.BARRAGE) && PlayerWillpower.get(f.owner) == 4,
                    "Barrage pulses duplicated the reward or failed to award for three unique opponents");
            enemies.forEach(Husk::discard); f.close(); helper.succeed();
        });
    }

    private static void guardProgressIntegration(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            var enemies = enemies(helper, f.owner);
            check(helper, StarPlatinumActions.toggleGuard(f.owner), "The guard mastery fixture did not start");
            try {
                for (int i = 0; i < 3; i++) {
                    var enemy = enemies.get(i);
                    enemy.snapTo(f.owner.position().add(0, 0, 2), 0, 0);
                    f.owner.damageCooldownTime = 0;
                    f.owner.setHealth(20);
                    check(helper, f.owner.hurtServer(helper.getLevel(), f.owner.damageSources().mobAttack(enemy), 10),
                            "The guarded player fixture did not receive physical attack " + i
                                    + " (difficulty=" + helper.getLevel().getDifficulty() + ")");
                    check(helper, f.owner.getHealth() > 10 && f.owner.getHealth() < 20,
                            "The guard fixture did not actually reduce incoming positive damage");
                    check(helper, StarPlatinumProgress.get(f.owner).count(Milestone.GUARD) == i + 1,
                            "A real frontal block did not track a new hostile attacker");
                    f.owner.damageCooldownTime = 0;
                    f.owner.hurtServer(helper.getLevel(), f.owner.damageSources().mobAttack(enemy), 1);
                    check(helper, StarPlatinumProgress.get(f.owner).count(Milestone.GUARD) == i + 1,
                            "The same hostile attacker farmed guard progress");
                }
                check(helper, PlayerWillpower.get(f.owner) == 4, "Guard mastery did not award exactly one willpower");
            } finally { enemies.forEach(Husk::discard); }
        }
        helper.succeed();
    }

    private static void masteryEligibility(GameTestHelper helper) {
        var owner = player(helper);
        var hostile = enemy(helper, owner.position().add(0, 0, 2));
        var passive = helper.spawn(EntityTypes.VILLAGER, new BlockPos(4, 1, 4));
        var other = new TestPlayer(helper.getLevel());
        try {
            for (var target : List.of(passive, other)) {
                StarPlatinumProgress.onPreciseHit(owner, target);
                StarPlatinumProgress.onBarrageHit(owner, target);
                StarPlatinumProgress.onGuardBlock(owner, owner.damageSources().mobAttack(target));
            }
            check(helper, noMastery(owner), "Passive creatures or players counted as hostile mastery targets");
            for (int scenario = 0; scenario < 5; scenario++) {
                owner.setGameMode(GameType.SURVIVAL);
                owner.setHealth(20);
                PlayerStands.assign(owner, ModStands.STAR_PLATINUM.id());
                if (scenario == 0) owner.setGameMode(GameType.CREATIVE);
                if (scenario == 1) owner.setGameMode(GameType.SPECTATOR);
                if (scenario == 2) owner.setHealth(0);
                if (scenario == 3) PlayerStands.assign(owner, ModStands.ECHOES_ACT_1.id());
                if (scenario == 4) PlayerStands.clear(owner);
                StarPlatinumProgress.onPreciseHit(owner, hostile);
                StarPlatinumProgress.onBarrageHit(owner, hostile);
                StarPlatinumProgress.onGuardBlock(owner, owner.damageSources().mobAttack(hostile));
                check(helper, noMastery(owner) && PlayerWillpower.get(owner) == 3, "An ineligible owner gained mastery or repeated the awakening reward");
            }
        } finally { owner.discard(); hostile.discard(); passive.discard(); other.discard(); }
        helper.succeed();
    }

    private static boolean noMastery(TestPlayer player) {
        var progress = StarPlatinumProgress.get(player);
        return progress.preciseTargets().isEmpty() && progress.barrageTargets().isEmpty() && progress.guardTargets().isEmpty()
                && progress.fingerTargets().isEmpty() && progress.interceptTargets().isEmpty();
    }

    private static void masteryCoopAndUniqueTargets(GameTestHelper helper) {
        var first = player(helper);
        var second = player(helper);
        var enemies = enemies(helper, first);
        try {
            for (var enemy : enemies) {
                for (var owner : List.of(first, second)) {
                    StarPlatinumProgress.onPreciseHit(owner, enemy);
                    StarPlatinumProgress.onPreciseHit(owner, enemy);
                }
            }
            for (var owner : List.of(first, second)) {
                check(helper, StarPlatinumProgress.get(owner).completed(Milestone.PRECISE) && PlayerWillpower.get(owner) == 4,
                        "Shared opponents gave mastery to only one participant or duplicated rewards");
                for (var enemy : enemies) StarPlatinumProgress.onPreciseHit(owner, enemy);
                PlayerStands.clear(owner);
                PlayerStands.assign(owner, ModStands.STAR_PLATINUM.id());
                check(helper, PlayerWillpower.get(owner) == 4 && StarPlatinumProgress.get(owner).preciseTargets().size() == 3,
                        "Changing or reacquiring the Stand reset the mastery reward");
            }
        } finally { first.discard(); second.discard(); enemies.forEach(Husk::discard); }
        helper.succeed();
    }

    private static void masterySaveDeathAndNetwork(GameTestHelper helper) {
        var source = player(helper);
        var loaded = new TestPlayer(helper.getLevel());
        var respawned = new TestPlayer(helper.getLevel());
        var enemies = enemies(helper, source);
        try {
            for (var enemy : enemies) {
                StarPlatinumProgress.onPreciseHit(source, enemy);
                StarPlatinumProgress.onBarrageHit(source, enemy);
            }
            StarPlatinumProgress.onGuardBlock(source, source.damageSources().mobAttack(enemies.get(0)));
            check(helper, PlayerWillpower.get(source) == 5 && StarPlatinumProgress.starFingerUnlocked(source),
                    "Two natural mastery rewards did not unlock Star Finger");
            var expected = StarPlatinumProgress.get(source);
            var problems = new ProblemReporter.Collector();
            var output = TagValueOutput.createWithContext(problems, helper.getLevel().registryAccess());
            source.serializeAttachments(output);
            loaded.readAttachments(TagValueInput.create(problems, helper.getLevel().registryAccess(), output.buildResult()));
            respawned.copyAttachmentsFrom(source, true);
            for (var restored : List.of(loaded, respawned)) {
                check(helper, StarPlatinumProgress.get(restored).equals(expected) && PlayerWillpower.get(restored) == 5,
                        "Save/load or death copy changed mastery targets and willpower");
                for (var enemy : enemies) {
                    StarPlatinumProgress.onPreciseHit(restored, enemy);
                    StarPlatinumProgress.onBarrageHit(restored, enemy);
                    StarPlatinumProgress.onGuardBlock(restored, restored.damageSources().mobAttack(enemy));
                }
                check(helper, PlayerWillpower.get(restored) == 6 && StarPlatinumProgress.get(restored).completed(Milestone.GUARD),
                        "Restoration repeated completed rewards or lost partial progress");
            }
            var buffer = Unpooled.buffer();
            try {
                StarPlatinumProgress.STREAM_CODEC.encode(buffer, expected);
                check(helper, StarPlatinumProgress.STREAM_CODEC.decode(buffer).equals(expected) && !buffer.isReadable(),
                        "Owner synchronization lost mastery state or left unread bytes");
            } finally { buffer.release(); }
            check(helper, problems.isEmpty(), "Mastery attachment serialization reported a problem");
        } finally { source.discard(); loaded.discard(); respawned.discard(); enemies.forEach(Husk::discard); }
        helper.succeed();
    }

    private static void legacyDataAndRewardCap(GameTestHelper helper) {
        var legacy = new JsonObject();
        legacy.addProperty("awakened", true);
        var decoded = StarPlatinumProgress.CODEC.codec().parse(JsonOps.INSTANCE, legacy).getOrThrow();
        check(helper, decoded.awakened() && decoded.preciseTargets().isEmpty() && decoded.barrageTargets().isEmpty()
                && decoded.guardTargets().isEmpty(), "The old awakening-only save format no longer loads");
        var owner = new TestPlayer(helper.getLevel());
        var enemies = enemies(helper, owner);
        try {
            owner.setData(ModAttachments.STAR_PROGRESS, decoded);
            PlayerWillpower.set(owner, 7);
            PlayerStands.assign(owner, ModStands.STAR_PLATINUM.id());
            check(helper, PlayerWillpower.get(owner) == 7, "Migrating old awakening data repeated the +2 reward");
            PlayerWillpower.set(owner, WillpowerData.MAX_VALUE);
            for (var enemy : enemies) StarPlatinumProgress.onPreciseHit(owner, enemy);
            check(helper, PlayerWillpower.get(owner) == WillpowerData.MAX_VALUE && StarPlatinumProgress.get(owner).completed(Milestone.PRECISE),
                    "A reward at maximum willpower overflowed or failed to persist completion");
            for (var enemy : enemies) StarPlatinumProgress.onPreciseHit(owner, enemy);
            check(helper, PlayerWillpower.get(owner) == WillpowerData.MAX_VALUE, "Repeated capped rewards changed willpower");
        } finally { owner.discard(); enemies.forEach(Husk::discard); }
        helper.succeed();
    }

    private static void fingerTrainingIntegration(GameTestHelper helper) {
        var f = fixture(helper);
        f.target.discard();
        PlayerWillpower.set(f.owner, 5);
        var target = enemy(helper, f.owner.position().add(0, 0, 2));
        target.getAttribute(Attributes.ARMOR).setBaseValue(0);
        f.owner.setData(ModAttachments.STAR_PROGRESS, new StarPlatinumProgress(true,
                List.of(), List.of(), List.of(), List.of(UUID.randomUUID(), UUID.randomUUID()), List.of()));
        helper.onEachTick(() -> tick(f));
        check(helper, StarPlatinumActions.starFinger(f.owner), "Close Star Finger did not start");
        helper.runAfterDelay(20, () -> {
            check(helper, target.getHealth() == 16 && StarPlatinumProgress.get(f.owner).count(Milestone.FINGER) == 2,
                    "A successful close-range Star Finger completed the ranged challenge");
            target.snapTo(f.owner.position().add(0, 0, 5), 0, 0);
        });
        helper.runAfterDelay(121, () -> check(helper, StarPlatinumActions.starFinger(f.owner), "Ranged Star Finger did not start"));
        helper.runAfterDelay(132, () -> {
            check(helper, target.getHealth() == 12 && StarPlatinumProgress.get(f.owner).completed(Milestone.FINGER)
                    && PlayerWillpower.get(f.owner) == 6, "Real ranged health damage did not grant the mastery reward");
            check(helper, StarPlatinumActions.session(f.owner).fingerReadyAt() - StarPlatinumActions.session(f.owner).startedAt() == 120,
                    "Mastery shortened an already running cooldown");
            check(helper, StarPlatinumActions.interceptCooldown(f.owner) == 60,
                    "Star Finger mastery also reduced interception cooldown");
        });
        helper.runAfterDelay(242, () -> {
            check(helper, StarPlatinumActions.starFinger(f.owner), "Mastered Star Finger did not start");
            var state = StarPlatinumActions.session(f.owner);
            check(helper, state.fingerReadyAt() - state.startedAt() == 100, "The next mastered activation did not use five seconds");
        });
        helper.runAfterDelay(341, () -> check(helper, !StarPlatinumActions.starFinger(f.owner),
                "Mastered Star Finger recovered before tick 100"));
        helper.runAfterDelay(343, () -> check(helper, StarPlatinumActions.starFinger(f.owner),
                "Mastered Star Finger did not recover after tick 100"));
        helper.runAfterDelay(355, () -> {
            check(helper, PlayerWillpower.get(f.owner) == 6, "Repeated mastered hits duplicated willpower");
            target.discard(); f.close(); helper.succeed();
        });
    }

    private static void fingerTrainingAbsorption(GameTestHelper helper) {
        var f = fixture(helper);
        f.target.discard();
        PlayerWillpower.set(f.owner, 5);
        var target = enemy(helper, f.owner.position().add(0, 0, 5));
        target.getAttribute(Attributes.ARMOR).setBaseValue(0);
        target.getAttribute(Attributes.MAX_ABSORPTION).setBaseValue(4);
        target.setAbsorptionAmount(4);
        check(helper, target.getAbsorptionAmount() == 4, "The absorption fixture did not receive its shield");
        helper.onEachTick(() -> tick(f));
        check(helper, StarPlatinumActions.starFinger(f.owner), "Absorption fixture could not attack");
        helper.runAfterDelay(20, () -> {
            check(helper, target.getHealth() == 20 && target.getAbsorptionAmount() == 0
                    && StarPlatinumProgress.get(f.owner).fingerTargets().isEmpty(),
                    "Absorbed damage counted as ranged health damage");
            target.discard(); f.close(); helper.succeed();
        });
    }

    private static void advancedTrainingEligibility(GameTestHelper helper) {
        var owner = player(helper);
        var hostile = enemy(helper, owner.position().add(0, 0, 5));
        var passive = helper.spawn(EntityTypes.VILLAGER, new BlockPos(4, 1, 4));
        var other = new TestPlayer(helper.getLevel());
        try {
            StarPlatinumProgress.onStarFingerHit(owner, hostile, 25);
            StarPlatinumProgress.onArrowIntercepted(owner, hostile);
            check(helper, noMastery(owner), "Locked skills granted advanced training");
            PlayerWillpower.set(owner, 6);
            for (var target : List.of(passive, other)) {
                StarPlatinumProgress.onStarFingerHit(owner, target, 25);
                StarPlatinumProgress.onArrowIntercepted(owner, target);
            }
            StarPlatinumProgress.onArrowIntercepted(owner, null);
            for (double distance : new double[] {0, 8.99, 9, Double.NaN, Double.POSITIVE_INFINITY}) {
                StarPlatinumProgress.onStarFingerHit(owner, hostile, distance);
            }
            check(helper, noMastery(owner), "Passive targets, player arrows or a contact at/below three blocks gave progress");
            var board = helper.getLevel().getScoreboard();
            var team = board.addPlayerTeam("train_" + UUID.randomUUID().toString().substring(0, 8));
            try {
                board.addPlayerToTeam(owner.getScoreboardName(), team);
                board.addPlayerToTeam(hostile.getScoreboardName(), team);
                check(helper, owner.isAlliedTo(hostile), "The allied hostile fixture did not join the owner's team");
                StarPlatinumProgress.onStarFingerHit(owner, hostile, 25);
                StarPlatinumProgress.onArrowIntercepted(owner, hostile);
                check(helper, noMastery(owner), "An allied hostile farmed advanced training");
            } finally { board.removePlayerTeam(team); }
            for (int scenario = 0; scenario < 5; scenario++) {
                owner.setGameMode(GameType.SURVIVAL);
                owner.setHealth(20);
                PlayerStands.assign(owner, ModStands.STAR_PLATINUM.id());
                if (scenario == 0) owner.setGameMode(GameType.CREATIVE);
                if (scenario == 1) owner.setGameMode(GameType.SPECTATOR);
                if (scenario == 2) owner.setHealth(0);
                if (scenario == 3) PlayerStands.assign(owner, ModStands.ECHOES_ACT_1.id());
                if (scenario == 4) PlayerStands.clear(owner);
                StarPlatinumProgress.onStarFingerHit(owner, hostile, 25);
                StarPlatinumProgress.onArrowIntercepted(owner, hostile);
                check(helper, noMastery(owner), "An ineligible owner gained advanced progress: " + scenario);
            }
            owner.setHealth(20);
            PlayerStands.assign(owner, ModStands.STAR_PLATINUM.id());
            StarPlatinumProgress.onStarFingerHit(owner, hostile, 9.01);
            check(helper, StarPlatinumProgress.get(owner).count(Milestone.FINGER) == 1,
                    "Contact just beyond three blocks did not qualify");
        } finally { owner.discard(); hostile.discard(); passive.discard(); other.discard(); }
        helper.succeed();
    }

    private static void advancedTrainingCoop(GameTestHelper helper) {
        var first = player(helper);
        var second = player(helper);
        var enemies = enemies(helper, first);
        try {
            for (var owner : List.of(first, second)) {
                // Follow the natural sequence from three willpower, without an operator grant.
                for (var enemy : enemies) {
                    StarPlatinumProgress.onPreciseHit(owner, enemy);
                    StarPlatinumProgress.onBarrageHit(owner, enemy);
                    StarPlatinumProgress.onGuardBlock(owner, owner.damageSources().mobAttack(enemy));
                }
                check(helper, PlayerWillpower.get(owner) == 6, "Base training did not unlock the advanced skills");
                for (var enemy : enemies) {
                    for (int repeat = 0; repeat < 2; repeat++) {
                        StarPlatinumProgress.onStarFingerHit(owner, enemy, 25);
                        StarPlatinumProgress.onArrowIntercepted(owner, enemy);
                    }
                }
                check(helper, PlayerWillpower.get(owner) == 8 && StarPlatinumProgress.get(owner).completed(Milestone.FINGER)
                        && StarPlatinumProgress.get(owner).completed(Milestone.INTERCEPT),
                        "Advanced training did not reward each owner once against shared enemies");
            }
            PlayerStands.clear(first);
            PlayerStands.assign(first, ModStands.STAR_PLATINUM.id());
            for (var enemy : enemies) {
                StarPlatinumProgress.onStarFingerHit(first, enemy, 25);
                StarPlatinumProgress.onArrowIntercepted(first, enemy);
            }
            check(helper, PlayerWillpower.get(first) == 8 && StarPlatinumActions.starFingerCooldown(first) == 100
                    && StarPlatinumActions.interceptCooldown(first) == 50,
                    "Reacquiring Star Platinum reset rewards or lost cooldown bonuses");
        } finally { first.discard(); second.discard(); enemies.forEach(Husk::discard); }
        helper.succeed();
    }

    private static void advancedTrainingSaveAndNetwork(GameTestHelper helper) {
        var source = player(helper);
        var loaded = new TestPlayer(helper.getLevel());
        var respawned = new TestPlayer(helper.getLevel());
        var enemies = enemies(helper, source);
        try {
            PlayerWillpower.set(source, 6);
            for (var enemy : enemies) StarPlatinumProgress.onStarFingerHit(source, enemy, 25);
            StarPlatinumProgress.onArrowIntercepted(source, enemies.getFirst());
            var expected = StarPlatinumProgress.get(source);
            var problems = new ProblemReporter.Collector();
            var output = TagValueOutput.createWithContext(problems, helper.getLevel().registryAccess());
            source.serializeAttachments(output);
            loaded.readAttachments(TagValueInput.create(problems, helper.getLevel().registryAccess(), output.buildResult()));
            respawned.copyAttachmentsFrom(source, true);
            for (var restored : List.of(loaded, respawned)) {
                check(helper, StarPlatinumProgress.get(restored).equals(expected) && PlayerWillpower.get(restored) == 7
                        && StarPlatinumActions.starFingerCooldown(restored) == 100
                        && StarPlatinumActions.interceptCooldown(restored) == 60, "Persistence changed advanced mastery or its bonuses");
                for (var enemy : enemies) {
                    StarPlatinumProgress.onStarFingerHit(restored, enemy, 25);
                    StarPlatinumProgress.onArrowIntercepted(restored, enemy);
                }
                check(helper, PlayerWillpower.get(restored) == 8 && StarPlatinumActions.interceptCooldown(restored) == 50,
                        "Restoring partial progress repeated rewards or lost the remaining shooters");
            }
            var buffer = Unpooled.buffer();
            try {
                StarPlatinumProgress.STREAM_CODEC.encode(buffer, expected);
                check(helper, StarPlatinumProgress.STREAM_CODEC.decode(buffer).equals(expected) && !buffer.isReadable(),
                        "Advanced owner synchronization lost targets or left unread bytes");
            } finally { buffer.release(); }
            check(helper, problems.isEmpty(), "Advanced attachment persistence reported a problem");
        } finally { source.discard(); loaded.discard(); respawned.discard(); enemies.forEach(Husk::discard); }
        helper.succeed();
    }

    private static void advancedTrainingLegacyAndBounds(GameTestHelper helper) {
        var legacy = new StarPlatinumProgress(true, List.of(UUID.randomUUID()),
                List.of(UUID.randomUUID()), List.of(UUID.randomUUID()));
        var json = StarPlatinumProgress.CODEC.codec().encodeStart(JsonOps.INSTANCE, legacy).getOrThrow().getAsJsonObject();
        json.remove("finger_targets"); json.remove("intercept_targets");
        check(helper, StarPlatinumProgress.CODEC.codec().parse(JsonOps.INSTANCE, json).getOrThrow().equals(legacy),
                "Pre-advanced saves lost their existing targets");
        for (var field : List.of("finger_targets", "intercept_targets")) {
            for (boolean duplicate : List.of(false, true)) {
                var invalid = json.deepCopy();
                var id = UUID.randomUUID();
                var ids = duplicate ? List.of(id, id) : List.of(id, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
                invalid.add(field, net.minecraft.core.UUIDUtil.CODEC.listOf().encodeStart(JsonOps.INSTANCE, ids).getOrThrow());
                check(helper, StarPlatinumProgress.CODEC.codec().parse(JsonOps.INSTANCE, invalid).error().isPresent(),
                        "Invalid advanced training targets loaded: " + field + " duplicate=" + duplicate);
            }
        }
        var owner = player(helper);
        var enemies = enemies(helper, owner);
        try {
            PlayerWillpower.set(owner, WillpowerData.MAX_VALUE);
            for (var enemy : enemies) {
                StarPlatinumProgress.onStarFingerHit(owner, enemy, 25);
                StarPlatinumProgress.onArrowIntercepted(owner, enemy);
            }
            check(helper, PlayerWillpower.get(owner) == WillpowerData.MAX_VALUE
                    && StarPlatinumActions.starFingerCooldown(owner) == 100 && StarPlatinumActions.interceptCooldown(owner) == 50,
                    "The willpower cap blocked mastery bonuses or overflowed");
        } finally { owner.discard(); enemies.forEach(Husk::discard); }
        helper.succeed();
    }

    private static void check(GameTestHelper helper, boolean condition, String message) {
        helper.assertTrue(condition, Component.literal(message));
    }

    private record Fixture(TestPlayer owner, TrainingStandEntity stand, Villager target) implements AutoCloseable {
        @Override public void close() {
            StandController.getActive(owner).ifPresent(TrainingStandEntity::discard);
            StarPlatinumActions.stop(owner);
            stand.discard(); owner.discard(); target.discard();
        }
    }

    private static final class TestPlayer extends ServerPlayer {
        private TestPlayer(ServerLevel level) {
            super(level.getServer(), level, new GameProfile(UUID.randomUUID(),
                    "Master" + UUID.randomUUID().toString().substring(0, 8)), ClientInformation.createDefault());
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

        private void readAttachments(ValueInput input) { deserializeAttachments(input); }
    }

    private static final class MasteryTestInstance extends GameTestInstance {
        private static final MapCodec<MasteryTestInstance> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                TestData.CODEC.fieldOf("data").forGetter(MasteryTestInstance::info),
                Codec.STRING.fieldOf("case").forGetter(t -> t.name)).apply(instance, MasteryTestInstance::new));
        private final String name;
        private MasteryTestInstance(TestData<Holder<TestEnvironmentDefinition<?>>> data, String name) { super(data); this.name = name; }
        @Override public void run(GameTestHelper helper) {
            switch (name) {
                case "finger_gate_and_windup" -> fingerGateAndWindup(helper);
                case "finger_walls" -> fingerWalls(helper);
                case "finger_miss_and_cooldown" -> fingerMissAndCooldown(helper);
                case "finger_interruption" -> fingerInterruption(helper);
                case "finger_dismiss_and_invalid" -> fingerDismissAndInvalid(helper);
                case "precise_progress_integration" -> preciseProgressIntegration(helper);
                case "barrage_progress_integration" -> barrageProgressIntegration(helper);
                case "guard_progress_integration" -> guardProgressIntegration(helper);
                case "mastery_eligibility" -> masteryEligibility(helper);
                case "mastery_coop_and_unique_targets" -> masteryCoopAndUniqueTargets(helper);
                case "mastery_save_death_and_network" -> masterySaveDeathAndNetwork(helper);
                case "legacy_data_and_reward_cap" -> legacyDataAndRewardCap(helper);
                case "finger_training_integration" -> fingerTrainingIntegration(helper);
                case "finger_training_absorption" -> fingerTrainingAbsorption(helper);
                case "advanced_training_eligibility" -> advancedTrainingEligibility(helper);
                case "advanced_training_coop" -> advancedTrainingCoop(helper);
                case "advanced_training_save_and_network" -> advancedTrainingSaveAndNetwork(helper);
                case "advanced_training_legacy_and_bounds" -> advancedTrainingLegacyAndBounds(helper);
                default -> throw new IllegalArgumentException(name);
            }
        }
        @Override public MapCodec<? extends GameTestInstance> codec() { return CODEC; }
        @Override protected MutableComponent typeDescription() { return Component.literal(name); }
    }
}
