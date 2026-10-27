package io.github.bg7427941.jojosmod.gametest;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.bg7427941.jojosmod.abilities.HierophantSplash;
import io.github.bg7427941.jojosmod.abilities.StarPlatinumActions;
import io.github.bg7427941.jojosmod.entities.DiscKeeperEntity;
import io.github.bg7427941.jojosmod.entities.EmeraldShardEntity;
import io.github.bg7427941.jojosmod.entities.TrainingStandEntity;
import io.github.bg7427941.jojosmod.items.StandDiscItem;
import io.github.bg7427941.jojosmod.registry.ModAttachments;
import io.github.bg7427941.jojosmod.registry.ModEntities;
import io.github.bg7427941.jojosmod.registry.ModItems;
import io.github.bg7427941.jojosmod.registry.ModStands;
import io.github.bg7427941.jojosmod.stands.HierophantProgress;
import io.github.bg7427941.jojosmod.stands.PlayerStands;
import io.github.bg7427941.jojosmod.stands.PlayerWillpower;
import io.github.bg7427941.jojosmod.stands.StandController;
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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.zombie.Husk;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

/** Native projectile flight and collision, real crafting/storage, timed casts and persistent owner rewards. */
public final class HierophantGameTests {
    private static final String MOD_ID = PhaseOneGameTests.MOD_ID;
    private static final List<String> CASES = List.of("arrow_pool_and_awakening", "timed_volley_and_cooldown",
            "native_flight_and_walls", "projectile_range_and_lifetime", "allies_and_pvp",
            "interruption_matrix", "recovery_and_cancelled_flight", "mastery_health_and_bonus",
            "mastery_eligibility_and_coop", "persistence_network_and_legacy", "recipe_storage_no_duplication",
            "projectile_absorption_no_reward", "guard_reduces_emerald_damage", "near_crosshair_hits");
    private static final DeferredRegister<MapCodec<? extends GameTestInstance>> TYPES =
            DeferredRegister.create(Registries.TEST_INSTANCE_TYPE, MOD_ID);
    static { TYPES.register("hierophant", () -> HierophantTestInstance.CODEC); }

    private HierophantGameTests() {}

    public static void register(IEventBus bus) {
        TYPES.register(bus);
        bus.addListener(HierophantGameTests::registerTests);
    }

    private static void registerTests(RegisterGameTestsEvent event) {
        var environment = event.registerEnvironment(id("hierophant"),
                new TestEnvironmentDefinition.SetDifficulty(net.minecraft.world.Difficulty.NORMAL));
        for (var name : CASES) event.registerTest(id("hierophant/" + name), new HierophantTestInstance(
                new TestData<>(environment, Level.OVERWORLD, id("emerald_arena"), 230, 0,
                        true, Rotation.NONE, false, 1, 1, false, 0), name));
    }

    private static Identifier id(String path) { return Identifier.fromNamespaceAndPath(MOD_ID, path); }

    private static Fixture fixture(GameTestHelper helper) { return fixture(helper, 10.5); }

    private static Fixture fixture(GameTestHelper helper, double x) {
        var owner = new TestPlayer(helper.getLevel());
        owner.snapTo(helper.absoluteVec(new Vec3(x, 1, 2.5)), 0, 0);
        PlayerStands.assign(owner, ModStands.HIEROPHANT_GREEN.id());
        check(helper, StandController.toggle(owner), "Hierophant Green could not summon");
        var stand = StandController.getActive(owner).orElseThrow();
        check(helper, stand.getType() == ModEntities.HIEROPHANT_GREEN.get(), "Hierophant Green uses the placeholder model");
        return new Fixture(owner, stand, new ArrayList<>());
    }

    private static Husk enemy(GameTestHelper helper, Vec3 position) {
        var target = helper.spawn(EntityTypes.HUSK, new BlockPos(1, 1, 1));
        target.setNoAi(true); target.setNoGravity(true);
        target.getAttribute(Attributes.ARMOR).setBaseValue(0);
        target.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
        target.snapTo(position, 0, 0);
        return target;
    }

    private static void tick(Fixture f) {
        if (!f.stand.isRemoved()) f.stand.tick();
        NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(f.owner));
        // Fake owners do not guarantee entity-ticking chunks. Drive the actual vanilla flight here as well.
        var level = (ServerLevel)f.stand.level();
        for (var shard : level.getEntitiesOfClass(EmeraldShardEntity.class, f.stand.getBoundingBox().inflate(30),
                entity -> entity.getOwner() == f.owner)) {
            if (!f.shards.contains(shard)) f.shards.add(shard);
            if (!shard.isRemoved()) step(shard);
        }
    }

    private static EmeraldShardEntity shard(Fixture f, Vec3 velocity) {
        var shard = new EmeraldShardEntity(ModEntities.EMERALD_SHARD.get(), f.owner.level());
        shard.initialize(f.owner, HierophantSplash.session(f.owner).volley().orElseThrow(), f.owner.getEyePosition());
        shard.setDeltaMovement(velocity);
        f.owner.level().addFreshEntity(shard);
        f.shards.add(shard);
        return shard;
    }

    private static void fly(EmeraldShardEntity shard) {
        for (int i = 0; i < 30 && !shard.isRemoved(); i++) step(shard);
    }

    /** ServerLevel.tickNonPassenger advances common state before invoking an entity's own tick. */
    private static void step(EmeraldShardEntity shard) { shard.commonTick(); shard.tick(); }

    private static void arrowPoolAndAwakening(GameTestHelper helper) {
        for (int index = 0; index < ModStands.STARDUST_POOL.size(); index++) {
            var owner = new TestPlayer(helper.getLevel());
            try {
                owner.getRandom().setSeed(new long[] {0, 6, 3, 1, 2, 5}[index]);
                owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.STARDUST_ARROW.get()));
                check(helper, ModItems.STARDUST_ARROW.get().use(owner.level(), owner, InteractionHand.MAIN_HAND).consumesAction(),
                        "The Part 3 arrow did not accept its controlled roll");
                check(helper, PlayerStands.get(owner).standId().filter(ModStands.STARDUST_POOL.get(index).id()::equals).isPresent()
                        && PlayerWillpower.get(owner) == 3 && owner.getMainHandItem().isEmpty(),
                        "The arrow did not roll each Part 3 Stand or award its first awakening exactly once");
                owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.STARDUST_ARROW.get()));
                check(helper, !ModItems.STARDUST_ARROW.get().use(owner.level(), owner, InteractionHand.MAIN_HAND).consumesAction()
                        && owner.getMainHandItem().getCount() == 1, "An assigned player rerolled or lost an arrow");
                var id = ModStands.STARDUST_POOL.get(index).id();
                PlayerStands.clear(owner); PlayerStands.assign(owner, id);
                check(helper, PlayerWillpower.get(owner) == 3, "Repeated assignment farmed awakening willpower");
            } finally { owner.discard(); }
        }
        check(helper, ModStands.ARROW_POOL.equals(List.of(ModStands.ECHOES_ACT_1, ModStands.LITTLE_FEET)),
                "The original arrow pool changed");
        helper.succeed();
    }

    private static void nearCrosshairHits(GameTestHelper helper) {
        var f = fixture(helper);
        var target = enemy(helper, f.owner.position().add(0, 0, 2.8));
        target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100); target.setHealth(100);
        helper.onEachTick(() -> tick(f));
        check(helper, StandController.attack(f.owner), "Close-range Emerald Splash did not start");
        helper.runAfterDelay(35, () -> {
            check(helper, target.getHealth() == 73, "Shoulder parallax missed a centered close target or changed the nine-projectile damage cap: " + target.getHealth());
            target.discard(); f.close(); helper.succeed();
        });
    }
    private static void timedVolleyAndCooldown(GameTestHelper helper) {
        var f = fixture(helper);
        helper.onEachTick(() -> tick(f));
        check(helper, StandController.attack(f.owner) && !StandController.attack(f.owner), "A volley did not start or stacked");
        long ready = HierophantSplash.session(f.owner).readyAt();
        helper.runAfterDelay(5, () -> check(helper, f.shards.isEmpty(), "Emeralds launched before the six-tick preparation"));
        helper.runAfterDelay(7, () -> check(helper, f.shards.size() == 3, "The first pulse did not launch three emeralds"));
        helper.runAfterDelay(9, () -> check(helper, f.shards.size() == 6, "The second pulse did not launch three emeralds"));
        helper.runAfterDelay(12, () -> check(helper, f.shards.size() == 9, "The volley did not stop at nine emeralds"));
        helper.runAfterDelay(20, () -> check(helper, !HierophantSplash.session(f.owner).casting()
                && !StandController.attack(f.owner) && HierophantSplash.session(f.owner).readyAt() == ready,
                "Recovery bypassed the cooldown or reset its ready time"));
        helper.runAfterDelay(99, () -> check(helper, !StandController.attack(f.owner), "The volley recovered before five seconds"));
        helper.runAfterDelay(101, () -> check(helper, StandController.attack(f.owner), "The volley did not recover after five seconds"));
        helper.runAfterDelay(114, () -> {
            check(helper, f.shards.size() == 18 && f.owner.getInventory().countItem(Items.EMERALD) == 0,
                    "The second volley produced the wrong count or created inventory emeralds");
            f.close(); helper.succeed();
        });
    }

    private static void nativeFlightAndWalls(GameTestHelper helper) {
        var clear = fixture(helper, 5.5);
        var blocked = fixture(helper, 17.5);
        var target = enemy(helper, clear.owner.position().add(0, 0, 8));
        var hidden = enemy(helper, blocked.owner.position().add(0, 0, 8));
        for (int x = 13; x < 24; x++) for (int y = 1; y <= 4; y++) helper.setBlock(x, y, 6, Blocks.STONE);
        helper.onEachTick(() -> { tick(clear); tick(blocked); });
        check(helper, StandController.attack(clear.owner) && StandController.attack(blocked.owner), "Flight fixtures did not start");
        helper.runAfterDelay(35, () -> {
            check(helper, target.getHealth() < 20 && target.getHealth() >= 0,
                    "Native flight did not hit or exceeded the bounded volley damage");
            check(helper, hidden.getHealth() == 20, "Emeralds crossed a solid wall");
            check(helper, HierophantProgress.get(clear.owner).splashTargets().size() == 1
                    && HierophantProgress.get(blocked.owner).splashTargets().isEmpty(),
                    "Native health damage did not update only the successful owner's progress");
            check(helper, helper.getLevel().getEntitiesOfClass(ItemEntity.class, clear.stand.getBoundingBox().inflate(20)).isEmpty(),
                    "An emerald projectile became a dropped inventory item");
            target.discard(); hidden.discard(); clear.close(); blocked.close(); helper.succeed();
        });
    }

    private static void projectileRangeAndLifetime(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            check(helper, StandController.attack(f.owner), "Range fixture did not start");
            var moving = shard(f, new Vec3(0, 0, EmeraldShardEntity.SPEED));
            var start = moving.position();
            fly(moving);
            check(helper, moving.isRemoved() && moving.position().distanceTo(start) >= 13.99
                    && moving.position().distanceTo(start) <= 14.00001, "The shard stopped early or travelled beyond fourteen blocks");
            var stationary = shard(f, Vec3.ZERO);
            var output = TagValueOutput.createWithContext(new ProblemReporter.Collector(), helper.getLevel().registryAccess());
            check(helper, !stationary.save(output), "A live temporary emerald shard became persistent world data");
            fly(stationary);
            check(helper, stationary.isRemoved() && stationary.tickCount <= 25, "A stationary shard survived its lifetime bound");
        }
        helper.succeed();
    }

    private static void alliesAndPvp(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            var friend = new TestPlayer(helper.getLevel());
            friend.snapTo(f.owner.position().add(0, 0, 4), 180, 0);
            helper.getLevel().addNewPlayer(friend);
            var enemy = enemy(helper, f.owner.position().add(0, 0, 8));
            try {
                f.owner.allowPlayerHarm = false;
                check(helper, StandController.attack(f.owner), "Protected player fixture did not start");
                fly(shard(f, new Vec3(0, 0, 1.2)));
                check(helper, friend.getHealth() == 20 && enemy.getHealth() == 17,
                        "Emeralds hurt a PvP-protected player or failed to pass through to a valid enemy");
            } finally { friend.discard(); enemy.discard(); }
            var ally = enemy(helper, f.owner.position().add(0, 0, 4));
            var hostile = enemy(helper, f.owner.position().add(0, 0, 8));
            var board = helper.getLevel().getScoreboard();
            var team = board.addPlayerTeam("jade_" + UUID.randomUUID().toString().substring(0, 8));
            try {
                board.addPlayerToTeam(f.owner.getScoreboardName(), team);
                board.addPlayerToTeam(ally.getScoreboardName(), team);
                fly(shard(f, new Vec3(0, 0, 1.2)));
                check(helper, ally.getHealth() == 20 && hostile.getHealth() == 17,
                        "An allied creature took damage or blocked cooperative fire");
            } finally { board.removePlayerTeam(team); ally.discard(); hostile.discard(); }
        }
        helper.succeed();
    }

    private static void interruptionMatrix(GameTestHelper helper) {
        for (int scenario = 0; scenario < 8; scenario++) {
            try (var f = fixture(helper)) {
                check(helper, StandController.attack(f.owner), "Interruption fixture did not start");
                long ready = HierophantSplash.session(f.owner).readyAt();
                var flying = shard(f, new Vec3(0, 0, 1));
                if (scenario == 0) StandController.dismiss(f.owner);
                if (scenario == 1) PlayerStands.clear(f.owner);
                if (scenario == 2) PlayerStands.assign(f.owner, ModStands.ECHOES_ACT_1.id());
                if (scenario == 3) f.owner.setHealth(0);
                if (scenario == 4) f.owner.setGameMode(GameType.SPECTATOR);
                if (scenario == 5) f.owner.setServerLevel(helper.getLevel().getServer().getLevel(Level.NETHER));
                if (scenario == 6) f.owner.discard();
                if (scenario == 7) check(helper, f.owner.hurtServer(helper.getLevel(), f.owner.damageSources().generic(), 1),
                        "The interruption fixture did not take health damage");
                tick(f); step(flying);
                check(helper, flying.isRemoved() && !HierophantSplash.session(f.owner).casting()
                        && HierophantSplash.session(f.owner).volley().isEmpty()
                        && HierophantSplash.session(f.owner).readyAt() == ready,
                        "An invalid or interrupted owner retained a cast, projectile or reset cooldown: " + scenario);
            }
        }
        helper.succeed();
    }

    private static void recoveryAndCancelledFlight(GameTestHelper helper) {
        var f = fixture(helper);
        helper.onEachTick(() -> tick(f));
        check(helper, StandController.attack(f.owner), "Recovery fixture did not start");
        helper.runAfterDelay(20, () -> {
            check(helper, !HierophantSplash.session(f.owner).casting(), "The volley did not recover its animation");
            var late = shard(f, Vec3.ZERO);
            step(late);
            check(helper, !late.isRemoved(), "Normal recovery canceled an in-flight emerald");
            check(helper, f.owner.hurtServer(helper.getLevel(), f.owner.damageSources().generic(), 1), "Post-recovery damage failed");
            step(late);
            check(helper, late.isRemoved() && !StandController.attack(f.owner), "Health damage left flight active or reset cooldown");
            f.close(); helper.succeed();
        });
    }

    private static void masteryHealthAndBonus(GameTestHelper helper) {
        var f = fixture(helper);
        var target = enemy(helper, f.owner.position().add(0, 0, 8));
        f.owner.setData(ModAttachments.HIEROPHANT_PROGRESS,
                new HierophantProgress(true, List.of(UUID.randomUUID(), UUID.randomUUID())));
        helper.onEachTick(() -> tick(f));
        check(helper, StandController.attack(f.owner), "Mastery health fixture did not start");
        long ready = HierophantSplash.session(f.owner).readyAt();
        helper.runAfterDelay(35, () -> {
            check(helper, target.getHealth() < 20 && HierophantProgress.get(f.owner).completed()
                    && PlayerWillpower.get(f.owner) == 4, "Real projectile health damage did not award mastery exactly once");
            check(helper, HierophantSplash.session(f.owner).readyAt() == ready, "Completing mastery shortened the current cooldown");
        });
        helper.runAfterDelay(101, () -> {
            check(helper, StandController.attack(f.owner), "The mastered volley did not start");
            var state = HierophantSplash.session(f.owner);
            check(helper, state.readyAt() - state.startedAt() == 80, "The next volley did not use a four-second cooldown");
        });
        helper.runAfterDelay(180, () -> check(helper, !StandController.attack(f.owner), "Mastered cooldown recovered early"));
        helper.runAfterDelay(183, () -> {
            check(helper, StandController.attack(f.owner) && PlayerWillpower.get(f.owner) == 4,
                    "The controller kept the old five-second cooldown or duplicated mastery rewards");
            target.discard(); f.close(); helper.succeed();
        });
    }

    private static void masteryEligibilityAndCoop(GameTestHelper helper) {
        try (var first = fixture(helper, 5.5); var second = fixture(helper, 17.5)) {
            var passive = helper.spawn(EntityTypes.VILLAGER, new BlockPos(12, 1, 12));
            var enemies = List.of(enemy(helper, first.owner.position().add(0, 0, 4)),
                    enemy(helper, first.owner.position().add(0, 0, 6)), enemy(helper, first.owner.position().add(0, 0, 8)));
            try {
                HierophantProgress.onSplashHit(first.owner, passive);
                HierophantProgress.onSplashHit(first.owner, second.owner);
                first.owner.setGameMode(GameType.CREATIVE);
                HierophantProgress.onSplashHit(first.owner, enemies.getFirst());
                first.owner.setGameMode(GameType.SURVIVAL);
                check(helper, HierophantProgress.get(first.owner).splashTargets().isEmpty(), "An ineligible action farmed mastery");
                for (var enemy : enemies) {
                    HierophantProgress.onSplashHit(first.owner, enemy);
                    HierophantProgress.onSplashHit(first.owner, enemy);
                }
                check(helper, PlayerWillpower.get(first.owner) == 4 && PlayerWillpower.get(second.owner) == 3,
                        "One owner's mastery changed a friend or duplicated its reward");
                for (var enemy : enemies) HierophantProgress.onSplashHit(second.owner, enemy);
                check(helper, PlayerWillpower.get(second.owner) == 4, "Shared enemies denied the second player's own progress");
                PlayerStands.clear(first.owner); PlayerStands.assign(first.owner, ModStands.HIEROPHANT_GREEN.id());
                for (var enemy : enemies) HierophantProgress.onSplashHit(first.owner, enemy);
                check(helper, PlayerWillpower.get(first.owner) == 4, "Recovering the Stand repeated rewards");
            } finally { passive.discard(); enemies.forEach(Husk::discard); }
        }
        helper.succeed();
    }

    private static void persistenceNetworkAndLegacy(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            var expected = new HierophantProgress(true, List.of(UUID.randomUUID(), UUID.randomUUID()));
            f.owner.setData(ModAttachments.HIEROPHANT_PROGRESS, expected);
            var problems = new ProblemReporter.Collector();
            var output = TagValueOutput.createWithContext(problems, helper.getLevel().registryAccess());
            f.owner.serializeAttachments(output);
            for (boolean death : List.of(false, true)) {
                var restored = new TestPlayer(helper.getLevel());
                try {
                    if (death) restored.copyAttachmentsFrom(f.owner, true);
                    else restored.readAttachments(TagValueInput.create(problems, helper.getLevel().registryAccess(), output.buildResult()));
                    check(helper, HierophantProgress.get(restored).equals(expected) && PlayerWillpower.get(restored) == 3,
                            "Persistence changed Hierophant mastery or its awakening reward");
                    PlayerStands.clear(restored); PlayerStands.assign(restored, ModStands.HIEROPHANT_GREEN.id());
                    check(helper, PlayerWillpower.get(restored) == 3, "Restoring data repeated the awakening reward");
                } finally { restored.discard(); }
            }
            var buffer = Unpooled.buffer();
            try {
                HierophantProgress.STREAM_CODEC.encode(buffer, expected);
                check(helper, HierophantProgress.STREAM_CODEC.decode(buffer).equals(expected) && !buffer.isReadable(),
                        "The owner network codec lost or appended progress data");
            } finally { buffer.release(); }
            var legacy = new com.google.gson.JsonObject(); legacy.addProperty("awakened", true);
            check(helper, HierophantProgress.CODEC.codec().parse(JsonOps.INSTANCE, legacy).getOrThrow().equals(
                    new HierophantProgress(true, List.of())), "Missing optional mastery data no longer loads");
            for (boolean duplicate : List.of(false, true)) {
                var json = legacy.deepCopy(); var id = UUID.randomUUID();
                var targets = duplicate ? List.of(id, id) : List.of(id, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
                json.add("splash_targets", net.minecraft.core.UUIDUtil.CODEC.listOf().encodeStart(JsonOps.INSTANCE, targets).getOrThrow());
                check(helper, HierophantProgress.CODEC.codec().parse(JsonOps.INSTANCE, json).error().isPresent(), "Unbounded or duplicate targets loaded");
            }
            check(helper, problems.isEmpty(), "Attachment persistence reported problems");
            PlayerWillpower.set(f.owner, WillpowerData.MAX_VALUE);
            var enemy = enemy(helper, f.owner.position().add(0, 0, 6));
            HierophantProgress.onSplashHit(f.owner, enemy);
            check(helper, HierophantProgress.get(f.owner).completed() && PlayerWillpower.get(f.owner) == WillpowerData.MAX_VALUE
                    && HierophantSplash.cooldown(f.owner) == 80, "The willpower cap blocked mastery or overflowed");
            enemy.discard();
        }
        helper.succeed();
    }

    private static void recipeStorageNoDuplication(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            helper.setBlock(10, 1, 4, Blocks.CRAFTING_TABLE);
            var menu = new CraftingMenu(1, f.owner.getInventory(), ContainerLevelAccess.create(helper.getLevel(),
                    helper.absolutePos(new BlockPos(10, 1, 4))));
            try {
                menu.getInputGridSlots().get(0).set(new ItemStack(ModItems.EMPTY_STAND_DISC.get()));
                menu.getInputGridSlots().get(8).set(new ItemStack(ModItems.HIEROPHANT_GREEN_CORE.get()));
                check(helper, ItemStack.matches(menu.getResultSlot().getItem(), StandDiscItem.containing(ModStands.HIEROPHANT_GREEN)),
                        "The loaded core recipe lost the Hierophant identity");
                menu.clicked(0, 0, ContainerInput.PICKUP, f.owner);
                check(helper, menu.getInputGridSlots().stream().allMatch(slot -> slot.getItem().isEmpty()), "Crafting did not consume its ingredients");
            } finally { menu.removed(f.owner); }
            var keeper = new DiscKeeperEntity(ModEntities.DISC_KEEPER.get(), helper.getLevel());
            keeper.snapTo(f.owner.position().add(2, 0, 0), 0, 0);
            helper.getLevel().addFreshEntity(keeper);
            try {
                f.owner.setShiftKeyDown(true);
                f.owner.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(ModItems.EMPTY_STAND_DISC.get()));
                check(helper, StandController.attack(f.owner), "Storage fixture did not start a volley");
                check(helper, f.owner.interactOn(keeper, InteractionHand.OFF_HAND, Vec3.ZERO).consumesAction()
                        && StandController.getActive(f.owner).isEmpty() && HierophantSplash.session(f.owner).volley().isEmpty(),
                        "Storage left a casting or active Stand");
                check(helper, ItemStack.matches(f.owner.getOffhandItem(), StandDiscItem.containing(ModStands.HIEROPHANT_GREEN)),
                        "Storage lost the Stand identity");
                check(helper, ModItems.STAND_DISC.get().use(f.owner.level(), f.owner, InteractionHand.OFF_HAND).consumesAction()
                        && f.owner.getOffhandItem().isEmpty() && PlayerWillpower.get(f.owner) == 3,
                        "Recovery lost identity, duplicated the awakening reward or retained the disc");
            } finally { keeper.discard(); }
        }
        helper.succeed();
    }

    private static void projectileAbsorptionNoReward(GameTestHelper helper) {
        var f = fixture(helper);
        var target = enemy(helper, f.owner.position().add(0, 0, 8));
        target.getAttribute(Attributes.MAX_ABSORPTION).setBaseValue(50);
        target.setAbsorptionAmount(50);
        helper.onEachTick(() -> tick(f));
        check(helper, StandController.attack(f.owner), "Absorption fixture did not start");
        helper.runAfterDelay(35, () -> {
            check(helper, target.getHealth() == 20 && target.getAbsorptionAmount() < 50
                    && HierophantProgress.get(f.owner).splashTargets().isEmpty(), "Absorbed damage counted as health mastery");
            target.discard(); f.close(); helper.succeed();
        });
    }

    private static void guardReducesEmeraldDamage(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            var defender = new TestPlayer(helper.getLevel());
            defender.snapTo(f.owner.position().add(0, 0, 6), 180, 0);
            PlayerStands.assign(defender, ModStands.STAR_PLATINUM.id());
            helper.getLevel().addNewPlayer(defender);
            try {
                check(helper, StandController.toggle(defender) && StarPlatinumActions.toggleGuard(defender), "The Star Platinum defender did not guard");
                check(helper, StandController.attack(f.owner), "Guard attack fixture did not start");
                fly(shard(f, new Vec3(0, 0, 1.2)));
                check(helper, Math.abs(defender.getHealth() - 18.8F) < .001,
                        "A frontal guard did not reduce the real emerald projectile's damage");
            } finally { StandController.dismiss(defender); defender.discard(); }
        }
        helper.succeed();
    }

    private static void check(GameTestHelper helper, boolean condition, String message) { helper.assertTrue(condition, Component.literal(message)); }

    private record Fixture(TestPlayer owner, TrainingStandEntity stand, ArrayList<EmeraldShardEntity> shards) implements AutoCloseable {
        @Override public void close() {
            HierophantSplash.cancel(owner); shards.forEach(EmeraldShardEntity::discard); stand.discard(); owner.discard();
        }
    }

    private static final class TestPlayer extends ServerPlayer {
        private boolean allowPlayerHarm = true;
        private TestPlayer(ServerLevel level) {
            super(level.getServer(), level, new GameProfile(UUID.randomUUID(), "Jade" + UUID.randomUUID().toString().substring(0, 8)), ClientInformation.createDefault());
            var transport = new net.minecraft.network.Connection(PacketFlow.SERVERBOUND) {
                @Override public void setListenerForServerboundHandshake(PacketListener listener) {}
            };
            new EmbeddedChannel(transport);
            connection = new ServerGamePacketListenerImpl(level.getServer(), transport, this, CommonListenerCookie.createInitial(getGameProfile(), false)) {
                @Override public void send(Packet<?> packet) {}
                @Override public void tick() {}
            };
            connection.markClientLoaded(); setGameMode(GameType.SURVIVAL); setNoGravity(true);
        }
        @Override public boolean canHarmPlayer(Player target) { return allowPlayerHarm && super.canHarmPlayer(target); }
        private void readAttachments(ValueInput input) { deserializeAttachments(input); }
    }

    private static final class HierophantTestInstance extends GameTestInstance {
        private static final MapCodec<HierophantTestInstance> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                TestData.CODEC.fieldOf("data").forGetter(HierophantTestInstance::info),
                Codec.STRING.fieldOf("case").forGetter(test -> test.name)).apply(instance, HierophantTestInstance::new));
        private final String name;
        private HierophantTestInstance(TestData<Holder<TestEnvironmentDefinition<?>>> data, String name) { super(data); this.name = name; }
        @Override public void run(GameTestHelper helper) {
            switch (name) {
                case "arrow_pool_and_awakening" -> arrowPoolAndAwakening(helper);
                case "timed_volley_and_cooldown" -> timedVolleyAndCooldown(helper);
                case "native_flight_and_walls" -> nativeFlightAndWalls(helper);
                case "near_crosshair_hits" -> nearCrosshairHits(helper);
                case "projectile_range_and_lifetime" -> projectileRangeAndLifetime(helper);
                case "allies_and_pvp" -> alliesAndPvp(helper);
                case "interruption_matrix" -> interruptionMatrix(helper);
                case "recovery_and_cancelled_flight" -> recoveryAndCancelledFlight(helper);
                case "mastery_health_and_bonus" -> masteryHealthAndBonus(helper);
                case "mastery_eligibility_and_coop" -> masteryEligibilityAndCoop(helper);
                case "persistence_network_and_legacy" -> persistenceNetworkAndLegacy(helper);
                case "recipe_storage_no_duplication" -> recipeStorageNoDuplication(helper);
                case "projectile_absorption_no_reward" -> projectileAbsorptionNoReward(helper);
                case "guard_reduces_emerald_damage" -> guardReducesEmeraldDamage(helper);
                default -> throw new IllegalArgumentException(name);
            }
        }
        @Override public MapCodec<? extends GameTestInstance> codec() { return CODEC; }
        @Override protected MutableComponent typeDescription() { return Component.literal(name); }
    }
}
