package io.github.bg7427941.jojosmod.gametest;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.bg7427941.jojosmod.entities.TrainingStandEntity;
import io.github.bg7427941.jojosmod.registry.ModStands;
import io.github.bg7427941.jojosmod.stands.PlayerStands;
import io.github.bg7427941.jojosmod.stands.StandController;
import java.util.List;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Exercises the authoritative Stand lifecycle and attack behavior on a real server level. */
public final class PhaseTwoGameTests {
    private static final String MOD_ID = PhaseOneGameTests.MOD_ID;
    private static final List<String> CASES = List.of(
            "unassigned_rejected", "toggle_and_cooldown", "owners_isolated", "follow_range",
            "basic_attack_and_cooldown", "wall_blocks_attack", "lifecycle_cleanup");
    private static final DeferredRegister<MapCodec<? extends GameTestInstance>> TYPES =
            DeferredRegister.create(Registries.TEST_INSTANCE_TYPE, MOD_ID);

    static {
        TYPES.register("phase_two", () -> StandTestInstance.CODEC);
    }

    private PhaseTwoGameTests() {}

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
        modBus.addListener(PhaseTwoGameTests::registerTests);
    }

    private static void registerTests(RegisterGameTestsEvent event) {
        var environment = event.registerEnvironment(id("phase_two"));
        for (String name : CASES) {
            var data = new TestData<>(environment, Level.OVERWORLD, id("empty"),
                    80, 0, true, Rotation.NONE, false, 1, 1, false, 0);
            event.registerTest(id("phase_two/" + name), new StandTestInstance(data, name));
        }
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    private static ServerPlayer player(GameTestHelper helper, boolean assigned) {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "StandTest"));
        player.setNoGravity(true);
        player.snapTo(helper.absoluteVec(new Vec3(1.5, 1, 0.5)), 0, 0);
        if (assigned) {
            PlayerStands.assign(player, ModStands.TRAINING_STAND.id());
        }
        return player;
    }

    private static TrainingStandEntity summon(GameTestHelper helper, ServerPlayer owner) {
        check(helper, StandController.toggle(owner), "Assigned player could not summon a Stand");
        var stand = StandController.getActive(owner);
        check(helper, stand.isPresent(), "Summoning did not create an active entity");
        return stand.orElseThrow();
    }

    private static void cleanup(ServerPlayer... owners) {
        for (var owner : owners) {
            StandController.dismiss(owner);
            owner.discard();
        }
    }

    private static void unassignedRejected(GameTestHelper helper) {
        var owner = player(helper, false);
        check(helper, !StandController.toggle(owner), "An unassigned player summoned a Stand");
        check(helper, !StandController.attack(owner), "An unassigned player attacked with a Stand");
        check(helper, StandController.getActive(owner).isEmpty(), "Unassigned player has an active entity");
        PlayerStands.assign(owner, id("unknown_stand"));
        check(helper, !StandController.toggle(owner), "An unregistered Stand definition was summoned");
        cleanup(owner);
        helper.succeed();
    }

    private static void toggleAndCooldown(GameTestHelper helper) {
        var owner = player(helper, true);
        var stand = summon(helper, owner);
        check(helper, !stand.getType().canSerialize(), "A manifested Stand must not be persisted as an entity");
        check(helper, !StandController.toggle(owner), "Toggle spam bypassed the cooldown");
        check(helper, StandController.getActive(owner).orElseThrow() == stand,
                "A rejected toggle replaced the active entity");
        long count = helper.getLevel().getEntitiesOfClass(TrainingStandEntity.class,
                owner.getBoundingBox().inflate(ModStands.TRAINING_STAND.stats().maxOwnerDistance()),
                entity -> entity.getOwner() == owner && !entity.isRemoved()).size();
        check(helper, count == 1, "Owner has more than one manifested Stand");

        helper.runAfterDelay(StandController.TOGGLE_COOLDOWN_TICKS, () -> {
            try {
                check(helper, StandController.toggle(owner), "Toggle remained blocked after its cooldown");
                check(helper, stand.isRemoved(), "Dismissing the Stand left its entity alive");
                check(helper, StandController.getActive(owner).isEmpty(), "Dismissed Stand is still active");
                helper.succeed();
            } finally {
                cleanup(owner);
            }
        });
    }

    private static void ownersIsolated(GameTestHelper helper) {
        var first = player(helper, true);
        var second = player(helper, true);
        var firstStand = summon(helper, first);
        var secondStand = summon(helper, second);
        check(helper, firstStand != secondStand, "Two owners share one Stand entity");
        check(helper, firstStand.getOwner() == first && secondStand.getOwner() == second,
                "Stand ownership crossed between players");
        PlayerStands.clear(first);
        firstStand.tick();
        check(helper, firstStand.isRemoved(), "A Stand remained after clearing its owner's assignment");
        check(helper, StandController.getActive(second).orElseThrow() == secondStand,
                "Clearing one owner affected another owner's Stand");
        check(helper, !secondStand.isRemoved(), "Another owner's Stand was discarded");
        cleanup(first, second);
        helper.succeed();
    }

    private static void followRange(GameTestHelper helper) {
        var owner = player(helper, true);
        var stand = summon(helper, owner);
        stand.tick();
        owner.setPos(owner.position().add(3, 0, 0));
        double before = stand.distanceToSqr(owner);
        stand.tick();
        check(helper, stand.distanceToSqr(owner) < before, "Stand did not follow its moving owner");

        double maximum = ModStands.TRAINING_STAND.stats().maxOwnerDistance();
        // Simulate separation without loading or teleporting a player into a different chunk.
        stand.setPos(owner.position().add(maximum + 1, 0, 0));
        stand.tick();
        check(helper, stand.distanceToSqr(owner) <= maximum * maximum,
                "Stand remained outside its owner's maximum range");
        check(helper, !stand.isRemoved(), "Following a valid owner unexpectedly removed the Stand");
        cleanup(owner);
        helper.succeed();
    }

    private static Villager target(GameTestHelper helper) {
        var target = helper.spawnWithNoFreeWill(EntityTypes.VILLAGER, new Vec3(1.5, 1, 2.5));
        target.setNoGravity(true);
        return target;
    }

    private static void basicAttackAndCooldown(GameTestHelper helper) {
        var owner = player(helper, true);
        summon(helper, owner);
        var target = target(helper);
        check(helper, owner.hasLineOfSight(target), "Attack fixture has an obstructed line of sight");
        check(helper, owner.distanceToSqr(target) < Math.pow(ModStands.TRAINING_STAND.stats().attackReach(), 2),
                "Attack fixture is outside the configured reach");
        float before = target.getHealth();
        check(helper, StandController.attack(owner), "Valid basic attack was rejected");
        check(helper, target.getHealth() < before, "Accepted basic attack did not damage the target");
        check(helper, Math.abs((before - target.getHealth()) - ModStands.TRAINING_STAND.stats().attackDamage()) < 0.001,
                "Basic attack did not use the configured damage");
        float after = target.getHealth();
        check(helper, !StandController.attack(owner), "Attack spam bypassed the cooldown");
        check(helper, target.getHealth() == after, "Rejected attack still damaged the target");
        target.discard();
        helper.runAfterDelay(ModStands.TRAINING_STAND.stats().attackCooldownTicks(), () -> {
            try {
                check(helper, StandController.attack(owner), "Attack remained blocked after its cooldown");
                check(helper, !StandController.attack(owner), "A new accepted attempt failed to reset its cooldown");
                helper.succeed();
            } finally {
                cleanup(owner);
            }
        });
    }

    private static void wallBlocksAttack(GameTestHelper helper) {
        var owner = player(helper, true);
        summon(helper, owner);
        var target = target(helper);
        helper.setBlock(1, 1, 1, Blocks.STONE);
        helper.setBlock(1, 2, 1, Blocks.STONE);
        check(helper, !owner.hasLineOfSight(target), "Wall fixture did not obstruct the target");
        float before = target.getHealth();
        check(helper, StandController.attack(owner), "A miss should still be an accepted attack attempt");
        check(helper, target.getHealth() == before, "Basic attack damaged a target behind a wall");
        check(helper, !StandController.attack(owner), "A blocked attack failed to consume its cooldown");
        target.discard();
        cleanup(owner);
        helper.succeed();
    }

    private static void lifecycleCleanup(GameTestHelper helper) {
        var deadOwner = player(helper, true);
        var deadOwnerStand = summon(helper, deadOwner);
        deadOwner.setHealth(0);
        deadOwnerStand.tick();
        check(helper, deadOwnerStand.isRemoved(), "Stand survived its owner's death");
        check(helper, PlayerStands.get(deadOwner).standId().filter(ModStands.TRAINING_STAND.id()::equals).isPresent(),
                "Manifestation cleanup erased the persistent assignment");

        var owner = player(helper, true);
        var discarded = summon(helper, owner);
        discarded.discard();
        check(helper, StandController.getActive(owner).isEmpty(), "Discarded entity remained active");
        helper.runAfterDelay(StandController.TOGGLE_COOLDOWN_TICKS, () -> {
            try {
                var replacement = summon(helper, owner);
                check(helper, replacement != discarded && !replacement.isRemoved(),
                        "Stale entity reference prevented a new manifestation");
                helper.succeed();
            } finally {
                cleanup(deadOwner, owner);
            }
        });
    }

    private static void check(GameTestHelper helper, boolean condition, String message) {
        helper.assertTrue(condition, Component.literal(message));
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
                case "unassigned_rejected" -> unassignedRejected(helper);
                case "toggle_and_cooldown" -> toggleAndCooldown(helper);
                case "owners_isolated" -> ownersIsolated(helper);
                case "follow_range" -> followRange(helper);
                case "basic_attack_and_cooldown" -> basicAttackAndCooldown(helper);
                case "wall_blocks_attack" -> wallBlocksAttack(helper);
                case "lifecycle_cleanup" -> lifecycleCleanup(helper);
                default -> throw new IllegalArgumentException("Unknown Phase 2 test: " + name);
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
