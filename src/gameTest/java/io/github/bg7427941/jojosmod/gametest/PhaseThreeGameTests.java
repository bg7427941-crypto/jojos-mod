package io.github.bg7427941.jojosmod.gametest;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.bg7427941.jojosmod.registry.ModItems;
import io.github.bg7427941.jojosmod.registry.ModStands;
import io.github.bg7427941.jojosmod.stands.PlayerStands;
import io.github.bg7427941.jojosmod.stands.StandController;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Runs the real item-use, attachment, combat and loaded loot-table paths on the server. */
public final class PhaseThreeGameTests {
    private static final String MOD_ID = PhaseOneGameTests.MOD_ID;
    private static final List<String> CASES = List.of(
            "arrow_survival_and_pool", "arrow_preserves_assignment", "arrow_creative_offhand",
            "arrow_invalid_users", "arrow_persistence", "variant_stats_and_identity", "dungeon_loot");
    private static final DeferredRegister<MapCodec<? extends GameTestInstance>> TYPES =
            DeferredRegister.create(Registries.TEST_INSTANCE_TYPE, MOD_ID);

    static {
        TYPES.register("phase_three", () -> StandTestInstance.CODEC);
    }

    private PhaseThreeGameTests() {}

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
        modBus.addListener(PhaseThreeGameTests::registerTests);
    }

    private static void registerTests(RegisterGameTestsEvent event) {
        var environment = event.registerEnvironment(id("phase_three"));
        for (String name : CASES) {
            var data = new TestData<>(environment, Level.OVERWORLD, id("empty"),
                    80, 0, true, Rotation.NONE, false, 1, 1, false, 0);
            event.registerTest(id("phase_three/" + name), new StandTestInstance(data, name));
        }
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    private static TestPlayer player(GameTestHelper helper) {
        var player = new TestPlayer(helper.getLevel());
        player.setNoGravity(true);
        player.snapTo(helper.absoluteVec(new Vec3(1.5, 1, 0.5)), 0, 0);
        return player;
    }

    private static InteractionResult useArrow(ServerPlayer player, InteractionHand hand) {
        return ModItems.STAND_ARROW.get().use(player.level(), player, hand);
    }

    private static void giveArrow(ServerPlayer player, InteractionHand hand) {
        player.setItemInHand(hand, new ItemStack(ModItems.STAND_ARROW.get()));
    }

    private static void cleanup(ServerPlayer... players) {
        for (var player : players) {
            StandController.dismiss(player);
            player.discard();
        }
    }

    private static void arrowSurvivalAndPool(GameTestHelper helper) {
        var results = new HashSet<Identifier>();
        // Fixed seeds exercise both outcomes without a probabilistic frequency assertion.
        for (long seed : new long[] {0, 4096}) {
            var owner = player(helper);
            try {
                owner.getRandom().setSeed(seed);
                giveArrow(owner, InteractionHand.MAIN_HAND);
                float health = owner.getHealth();
                check(helper, useArrow(owner, InteractionHand.MAIN_HAND).consumesAction(), "Arrow use was rejected");
                check(helper, owner.getMainHandItem().isEmpty(), "Survival arrow was not consumed");
                check(helper, owner.getHealth() == health, "Arrow damaged its user");
                var assigned = PlayerStands.get(owner).standId().orElseThrow();
                check(helper, ModStands.ARROW_POOL.stream().anyMatch(s -> s.id().equals(assigned)),
                        "Arrow granted a Stand outside its approved basic pool");
                results.add(assigned);
                check(helper, StandController.toggle(owner), "Obtained Stand could not be summoned");
                check(helper, StandController.getActive(owner).orElseThrow().getStandDefinition().id().equals(assigned),
                        "Manifestation lost its granted identity");
            } finally {
                cleanup(owner);
            }
        }
        check(helper, results.equals(new HashSet<>(List.of(ModStands.ECHOES_ACT_1.id(), ModStands.LITTLE_FEET.id()))),
                "The two controlled rolls did not reach both approved Stands");
        helper.succeed();
    }

    private static void arrowPreservesAssignment(GameTestHelper helper) {
        var owner = player(helper);
        try {
            for (var stand : List.of(ModStands.TRAINING_STAND.id(), ModStands.ECHOES_ACT_1.id(),
                    ModStands.LITTLE_FEET.id(), id("missing_definition"))) {
                PlayerStands.assign(owner, stand);
                giveArrow(owner, InteractionHand.OFF_HAND);
                check(helper, useArrow(owner, InteractionHand.OFF_HAND) == InteractionResult.FAIL,
                        "An existing assignment allowed a reroll");
                check(helper, owner.getOffhandItem().getCount() == 1, "A rejected arrow was consumed");
                check(helper, PlayerStands.get(owner).standId().equals(Optional.of(stand)),
                        "A rejected arrow replaced the existing assignment");
            }
        } finally {
            cleanup(owner);
        }
        helper.succeed();
    }

    private static void arrowCreativeOffhand(GameTestHelper helper) {
        var owner = player(helper);
        try {
            owner.setGameMode(GameType.CREATIVE);
            giveArrow(owner, InteractionHand.OFF_HAND);
            check(helper, useArrow(owner, InteractionHand.OFF_HAND).consumesAction(), "Creative offhand use failed");
            var assigned = PlayerStands.get(owner);
            check(helper, assigned.standId().isPresent(), "Creative arrow did not grant a Stand");
            check(helper, owner.getOffhandItem().getCount() == 1, "Creative arrow was consumed");
            check(helper, useArrow(owner, InteractionHand.OFF_HAND) == InteractionResult.FAIL,
                    "Retained creative arrow enabled a reroll");
            check(helper, PlayerStands.get(owner).equals(assigned), "Creative rejection changed the Stand");
        } finally {
            cleanup(owner);
        }
        helper.succeed();
    }

    private static void arrowInvalidUsers(GameTestHelper helper) {
        var spectator = player(helper);
        var dead = player(helper);
        var emptyHand = player(helper);
        try {
            spectator.setGameMode(GameType.SPECTATOR);
            dead.setHealth(0);
            for (var owner : List.of(spectator, dead)) {
                giveArrow(owner, InteractionHand.MAIN_HAND);
                check(helper, useArrow(owner, InteractionHand.MAIN_HAND) == InteractionResult.FAIL,
                        "An ineligible player obtained a Stand");
                check(helper, PlayerStands.get(owner).standId().isEmpty(), "Rejected player has an assignment");
                check(helper, owner.getMainHandItem().getCount() == 1, "Rejected player lost an arrow");
            }
            check(helper, useArrow(emptyHand, InteractionHand.MAIN_HAND) == InteractionResult.FAIL,
                    "An empty hand obtained a Stand");
            check(helper, PlayerStands.get(emptyHand).standId().isEmpty(), "Empty-handed player has an assignment");
        } finally {
            cleanup(spectator, dead, emptyHand);
        }
        helper.succeed();
    }

    private static void arrowPersistence(GameTestHelper helper) {
        var owner = player(helper);
        var loaded = player(helper);
        var respawned = player(helper);
        try {
            giveArrow(owner, InteractionHand.MAIN_HAND);
            useArrow(owner, InteractionHand.MAIN_HAND);
            var expected = PlayerStands.get(owner);
            check(helper, expected.standId().isPresent(), "Persistence fixture was not assigned by the arrow");
            var problems = new ProblemReporter.Collector();
            var output = TagValueOutput.createWithContext(problems, helper.getLevel().registryAccess());
            owner.serializeAttachments(output);
            loaded.readAttachments(TagValueInput.create(problems, helper.getLevel().registryAccess(), output.buildResult()));
            respawned.copyAttachmentsFrom(owner, true);
            check(helper, problems.isEmpty(), "Saving the granted Stand reported a problem");
            for (var restored : List.of(loaded, respawned)) {
                check(helper, PlayerStands.get(restored).equals(expected), "Granted identity did not survive save/death copy");
                check(helper, StandController.toggle(restored), "Restored Stand could not be summoned");
            }
        } finally {
            cleanup(owner, loaded, respawned);
        }
        helper.succeed();
    }

    private static void variantStatsAndIdentity(GameTestHelper helper) {
        var echoes = player(helper);
        var feet = player(helper);
        try {
            PlayerStands.assign(echoes, ModStands.ECHOES_ACT_1.id());
            PlayerStands.assign(feet, ModStands.LITTLE_FEET.id());
            for (var owner : List.of(echoes, feet)) {
                check(helper, StandController.toggle(owner), "Basic Stand could not manifest");
                var stand = StandController.getActive(owner).orElseThrow();
                var definition = stand.getStandDefinition();
                stand.setPos(owner.position().add(definition.stats().maxOwnerDistance() + 1, 0, 0));
                stand.tick();
                check(helper, stand.distanceToSqr(owner) < Math.pow(definition.stats().maxOwnerDistance(), 2),
                        "Stand did not use its own follow range");
                var target = helper.spawnWithNoFreeWill(EntityTypes.VILLAGER, new Vec3(1.5, 1, 2.5));
                target.setNoGravity(true);
                try {
                    float before = target.getHealth();
                    check(helper, StandController.attack(owner), "Basic Stand attack was rejected");
                    check(helper, Math.abs(before - target.getHealth() - definition.stats().attackDamage()) < 0.001,
                            "Basic Stand attack used the training Stand's damage");
                } finally {
                    target.discard();
                }
                check(helper, !StandController.attack(owner), "Basic Stand bypassed its cooldown");
            }
        } catch (RuntimeException | Error error) {
            cleanup(echoes, feet);
            throw error;
        }
        helper.runAfterDelay(ModStands.LITTLE_FEET.stats().attackCooldownTicks(), () -> {
            check(helper, StandController.attack(feet), "Little Feet's shorter cooldown did not recover");
            check(helper, !StandController.attack(echoes), "Echoes used Little Feet's shorter cooldown");
        });
        helper.runAfterDelay(ModStands.ECHOES_ACT_1.stats().attackCooldownTicks(), () -> {
            try {
                check(helper, StandController.attack(echoes), "Echoes' longer cooldown did not recover");
                // Changing an assignment must invalidate the old manifestation, including attacks before its next tick.
                var stale = StandController.getActive(feet).orElseThrow();
                PlayerStands.assign(feet, ModStands.ECHOES_ACT_1.id());
                check(helper, !StandController.attack(feet), "A stale manifestation attacked under a different definition");
                stale.tick();
                check(helper, stale.isRemoved(), "Assignment change retained a stale manifestation");
                helper.succeed();
            } finally {
                cleanup(echoes, feet);
            }
        });
    }

    @SuppressWarnings("deprecation") // Compare the unmodified baseline with the public modifier-aware path.
    private static void dungeonLoot(GameTestHelper helper) {
        var level = helper.getLevel();
        var tables = level.getServer().reloadableRegistries();
        var dungeon = tables.getLootTable(BuiltInLootTables.SIMPLE_DUNGEON);
        var unrelated = tables.getLootTable(BuiltInLootTables.DESERT_PYRAMID);
        var params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, helper.absoluteVec(Vec3.ZERO))
                .create(LootContextParamSets.CHEST);
        int arrows = 0;
        // Reproducible seeds cover successful and failed rolls, and compare the actual loaded vanilla contents.
        for (int seed = 1; seed <= 256; seed++) {
            var baseline = new java.util.ArrayList<ItemStack>();
            dungeon.getRandomItemsRaw(new LootContext.Builder(params)
                    .withOptionalRandomSource(RandomSource.create(seed)).create(Optional.empty()), baseline::add);
            var modified = dungeon.getRandomItems(params, RandomSource.create(seed));
            var vanillaOnly = modified.stream().filter(stack -> !stack.is(ModItems.STAND_ARROW.get())).toList();
            check(helper, baseline.size() == vanillaOnly.size(), "Dungeon modifier removed or added vanilla stacks");
            for (int i = 0; i < baseline.size(); i++) {
                check(helper, ItemStack.matches(baseline.get(i), vanillaOnly.get(i)), "Dungeon modifier changed vanilla loot");
            }
            int count = modified.stream().filter(stack -> stack.is(ModItems.STAND_ARROW.get()))
                    .mapToInt(ItemStack::getCount).sum();
            check(helper, count <= 1, "A dungeon chest rolled more than one arrow");
            arrows += count;
            check(helper, unrelated.getRandomItems(params, RandomSource.create(seed)).stream()
                    .noneMatch(stack -> stack.is(ModItems.STAND_ARROW.get())), "Arrow leaked into an unrelated loot table");
        }
        check(helper, arrows > 0 && arrows < 256, "Loaded modifier did not produce both successful and failed rolls");
        helper.succeed();
    }

    private static void check(GameTestHelper helper, boolean condition, String message) {
        helper.assertTrue(condition, Component.literal(message));
    }

    private static final class TestPlayer extends FakePlayer {
        private TestPlayer(ServerLevel level) {
            super(level, new GameProfile(UUID.randomUUID(), "ArrowTest"));
        }

        private void readAttachments(ValueInput input) {
            deserializeAttachments(input);
        }
    }

    private static final class StandTestInstance extends GameTestInstance {
        private static final MapCodec<StandTestInstance> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                TestData.CODEC.fieldOf("data").forGetter(StandTestInstance::info),
                Codec.STRING.fieldOf("case").forGetter(test -> test.name)
        ).apply(instance, StandTestInstance::new));

        private final String name;

        private StandTestInstance(TestData<Holder<TestEnvironmentDefinition<?>>> data, String name) {
            super(data);
            this.name = name;
        }

        @Override
        public void run(GameTestHelper helper) {
            switch (name) {
                case "arrow_survival_and_pool" -> arrowSurvivalAndPool(helper);
                case "arrow_preserves_assignment" -> arrowPreservesAssignment(helper);
                case "arrow_creative_offhand" -> arrowCreativeOffhand(helper);
                case "arrow_invalid_users" -> arrowInvalidUsers(helper);
                case "arrow_persistence" -> arrowPersistence(helper);
                case "variant_stats_and_identity" -> variantStatsAndIdentity(helper);
                case "dungeon_loot" -> dungeonLoot(helper);
                default -> throw new IllegalArgumentException("Unknown Phase 3 test: " + name);
            }
        }

        @Override
        public MapCodec<? extends GameTestInstance> codec() {
            return CODEC;
        }

        @Override
        protected MutableComponent typeDescription() {
            return Component.literal(name);
        }
    }
}
