package io.github.bg7427941.jojosmod.gametest;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.bg7427941.jojosmod.entities.DiscKeeperEntity;
import io.github.bg7427941.jojosmod.items.StandDiscItem;
import io.github.bg7427941.jojosmod.registry.ModDataComponents;
import io.github.bg7427941.jojosmod.registry.ModEntities;
import io.github.bg7427941.jojosmod.registry.ModItems;
import io.github.bg7427941.jojosmod.registry.ModStands;
import io.github.bg7427941.jojosmod.stands.PlayerStands;
import io.github.bg7427941.jojosmod.stands.PlayerWillpower;
import io.github.bg7427941.jojosmod.stands.StandController;
import java.util.ArrayList;
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
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Runs native player/entity interactions, in-place transfers and native player/entity save-load paths. */
public final class StandStorageGameTests {
    private static final String MOD_ID = PhaseOneGameTests.MOD_ID;
    private static final List<String> CASES = List.of(
            "voluntary_storage", "storage_requires_disc", "storage_preserves_unavailable_assignment",
            "storage_invalid_users", "storage_range_and_walls", "storage_full_inventory",
            "storage_player_save_load", "keeper_save_load", "keeper_shared_service");
    private static final DeferredRegister<MapCodec<? extends GameTestInstance>> TYPES =
            DeferredRegister.create(Registries.TEST_INSTANCE_TYPE, MOD_ID);

    static {
        TYPES.register("stand_storage", () -> StandTestInstance.CODEC);
    }

    private StandStorageGameTests() {}

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
        modBus.addListener(StandStorageGameTests::registerTests);
    }

    private static void registerTests(RegisterGameTestsEvent event) {
        var environment = event.registerEnvironment(id("stand_storage"));
        for (String name : CASES) {
            var data = new TestData<>(environment, Level.OVERWORLD, id("empty"),
                    80, 0, true, Rotation.NONE, false, 1, 1, false, 0);
            event.registerTest(id("stand_storage/" + name), new StandTestInstance(data, name));
        }
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    private static Fixture fixture(GameTestHelper helper) {
        var player = new TestPlayer(helper.getLevel());
        player.snapTo(helper.absoluteVec(new Vec3(1.5, 1, 0.5)), 0, 0);
        PlayerStands.assign(player, ModStands.ECHOES_ACT_1.id());
        PlayerWillpower.set(player, 7);
        player.setShiftKeyDown(true);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.EMPTY_STAND_DISC.get()));
        var keeper = new DiscKeeperEntity(ModEntities.DISC_KEEPER.get(), helper.getLevel());
        keeper.snapTo(helper.absoluteVec(new Vec3(1.5, 1, 2.5)), 180, 0);
        check(helper, helper.getLevel().addFreshEntity(keeper), "Keeper fixture was not added to the level");
        return new Fixture(player, keeper);
    }

    private static InteractionResult use(Fixture fixture, InteractionHand hand) {
        return fixture.player.interactOn(fixture.keeper, hand, Vec3.ZERO);
    }

    private static void voluntaryStorage(GameTestHelper helper) {
        for (var stand : List.of(ModStands.ECHOES_ACT_1, ModStands.LITTLE_FEET)) {
            for (var mode : List.of(GameType.SURVIVAL, GameType.CREATIVE)) {
                try (var f = fixture(helper)) {
                    f.player.setGameMode(mode);
                    PlayerStands.assign(f.player, stand.id());
                    check(helper, StandController.toggle(f.player), "Fixture could not summon its Stand");
                    var active = StandController.getActive(f.player).orElseThrow();
                    f.player.setShiftKeyDown(false);
                    var blank = f.player.getMainHandItem().copy();
                    check(helper, !use(f, InteractionHand.MAIN_HAND).consumesAction(), "Storage did not require consent");
                    check(helper, PlayerStands.get(f.player).standId().equals(Optional.of(stand.id()))
                            && ItemStack.matches(blank, f.player.getMainHandItem()) && !active.isRemoved(),
                            "The consent prompt changed the Stand or disc");
                    f.player.setShiftKeyDown(true);
                    float health = f.player.getHealth();
                    check(helper, use(f, InteractionHand.MAIN_HAND).consumesAction(), "Confirmed storage failed");
                    check(helper, ItemStack.matches(f.player.getMainHandItem(), StandDiscItem.containing(stand)),
                            "Storage did not transform exactly one blank into the correct full disc");
                    check(helper, PlayerStands.get(f.player).standId().isEmpty() && active.isRemoved()
                            && StandController.getActive(f.player).isEmpty(), "Storage left an assignment or active Stand");
                    check(helper, PlayerWillpower.get(f.player) == 7 && f.player.getHealth() == health,
                            "Storage spent permanent willpower or health");
                    var stored = f.player.getMainHandItem().copy();
                    check(helper, !use(f, InteractionHand.MAIN_HAND).consumesAction()
                            && ItemStack.matches(stored, f.player.getMainHandItem()), "Repeated use duplicated or changed the full disc");
                    f.player.setItemInHand(InteractionHand.MAIN_HAND, blank.copy());
                    check(helper, !use(f, InteractionHand.MAIN_HAND).consumesAction()
                            && ItemStack.matches(blank, f.player.getMainHandItem()), "A second blank stored an already-removed Stand");
                }
            }
        }
        helper.succeed();
    }

    private static void storageRequiresDisc(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            for (var stack : List.of(ItemStack.EMPTY, new ItemStack(ModItems.STAND_DISC.get()),
                    StandDiscItem.containing(ModStands.LITTLE_FEET), new ItemStack(Items.MUSIC_DISC_13),
                    new ItemStack(ModItems.STAND_DISC_FRAGMENT.get()),
                    new ItemStack(ModItems.EMPTY_STAND_DISC.get(), 2))) {
                f.player.setItemInHand(InteractionHand.MAIN_HAND, stack.copy());
                check(helper, !use(f, InteractionHand.MAIN_HAND).consumesAction()
                        && ItemStack.matches(stack, f.player.getMainHandItem())
                        && PlayerStands.get(f.player).standId().equals(Optional.of(ModStands.ECHOES_ACT_1.id())),
                        "Wrong or malformed ingredients changed the player's Stand or inventory");
            }
            f.player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.EMPTY_STAND_DISC.get()));
            PlayerStands.clear(f.player);
            check(helper, !use(f, InteractionHand.MAIN_HAND).consumesAction()
                    && f.player.getMainHandItem().is(ModItems.EMPTY_STAND_DISC.get()), "An unassigned player lost a blank disc");
        }
        helper.succeed();
    }

    private static void storagePreservesUnavailableAssignment(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            var unavailable = id("unavailable_stand");
            PlayerStands.assign(f.player, unavailable);
            check(helper, !use(f, InteractionHand.MAIN_HAND).consumesAction()
                    && f.player.getMainHandItem().is(ModItems.EMPTY_STAND_DISC.get())
                    && PlayerStands.get(f.player).standId().equals(Optional.of(unavailable))
                    && PlayerWillpower.get(f.player) == 7, "Storage destroyed an unavailable saved identity");
        }
        helper.succeed();
    }

    private static void storageInvalidUsers(GameTestHelper helper) {
        for (int state = 0; state < 5; state++) {
            try (var f = fixture(helper)) {
                if (state == 0) f.player.setGameMode(GameType.SPECTATOR);
                if (state == 1) f.player.setHealth(0);
                if (state == 2) f.player.discard();
                if (state == 3) f.keeper.discard();
                if (state == 4) {
                    var otherLevel = helper.getLevel().getServer().getLevel(Level.NETHER);
                    var remoteKeeper = new DiscKeeperEntity(ModEntities.DISC_KEEPER.get(), otherLevel);
                    try {
                        check(helper, !f.player.interactOn(remoteKeeper, InteractionHand.MAIN_HAND, Vec3.ZERO).consumesAction(),
                                "Storage worked across dimensions");
                    } finally {
                        remoteKeeper.discard();
                    }
                } else {
                    check(helper, !use(f, InteractionHand.MAIN_HAND).consumesAction(), "Ineligible actor completed storage");
                }
                check(helper, f.player.getMainHandItem().is(ModItems.EMPTY_STAND_DISC.get())
                        && PlayerStands.get(f.player).standId().equals(Optional.of(ModStands.ECHOES_ACT_1.id())),
                        "Rejected storage changed inventory or assignment");
            }
        }
        helper.succeed();
    }

    private static void storageRangeAndWalls(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            f.player.snapTo(helper.absoluteVec(new Vec3(0.5, 0, 0.5)), 0, 0);
            f.keeper.snapTo(f.player.position().add(0, 0, 3.01), 0, 0);
            check(helper, !use(f, InteractionHand.MAIN_HAND).consumesAction()
                    && f.player.getMainHandItem().is(ModItems.EMPTY_STAND_DISC.get()), "Storage exceeded the three-block range");
            // A 2/1/2 offset is exactly three blocks and keeps both eyes inside the 3x3x3 test structure.
            f.keeper.snapTo(f.player.position().add(2, 1, 2), 0, 0);
            check(helper, f.player.distanceToSqr(f.keeper) == 9.0, "Range fixture did not reach exactly three blocks");
            helper.setBlock(1, 2, 1, Blocks.STONE);
            check(helper, !f.player.hasLineOfSight(f.keeper), "Wall fixture did not block sight");
            check(helper, !use(f, InteractionHand.MAIN_HAND).consumesAction()
                    && PlayerStands.get(f.player).standId().isPresent(), "Storage passed through a wall");
            helper.setBlock(1, 2, 1, Blocks.AIR);
            check(helper, f.player.hasLineOfSight(f.keeper), "Wall removal did not restore the keeper's line of sight");
            check(helper, use(f, InteractionHand.MAIN_HAND).consumesAction(), "Storage rejected the exact three-block boundary");
        }
        helper.succeed();
    }

    private static void storageFullInventory(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            for (int i = 0; i < 36; i++) f.player.getInventory().setItem(i, new ItemStack(Items.STONE, 64));
            var before = new ArrayList<ItemStack>();
            for (int i = 0; i < 36; i++) before.add(f.player.getInventory().getItem(i).copy());
            f.player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(ModItems.EMPTY_STAND_DISC.get()));
            // The native client starts with MAIN_HAND, even when the supplied blank is in the offhand.
            check(helper, use(f, InteractionHand.MAIN_HAND).consumesAction(), "A full inventory blocked offhand storage");
            check(helper, ItemStack.matches(f.player.getOffhandItem(), StandDiscItem.containing(ModStands.ECHOES_ACT_1)),
                    "The stored disc did not replace its offhand blank");
            for (int i = 0; i < 36; i++) check(helper, ItemStack.matches(before.get(i), f.player.getInventory().getItem(i)),
                    "Storage modified an occupied inventory slot");
            check(helper, helper.getLevel().getEntities(EntityTypes.ITEM, f.keeper.getBoundingBox().inflate(4), e -> true).isEmpty(),
                    "Storage dropped an output instead of replacing the held blank");
        }
        try (var f = fixture(helper)) {
            f.player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BOW));
            f.player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(ModItems.EMPTY_STAND_DISC.get()));
            check(helper, use(f, InteractionHand.MAIN_HAND).consumesAction()
                    && f.player.getMainHandItem().is(Items.BOW) && f.player.getOffhandItem().is(ModItems.STAND_DISC.get()),
                    "A usable main-hand item prevented offhand service");
        }
        helper.succeed();
    }

    private static void storagePlayerSaveLoad(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            f.player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            f.player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(ModItems.EMPTY_STAND_DISC.get()));
            check(helper, use(f, InteractionHand.OFF_HAND).consumesAction(), "Persistence fixture did not store its Stand");
            var problems = new ProblemReporter.Collector();
            var output = TagValueOutput.createWithContext(problems, helper.getLevel().registryAccess());
            f.player.saveWithoutId(output);
            var loaded = new TestPlayer(helper.getLevel());
            try {
                loaded.load(TagValueInput.create(problems, helper.getLevel().registryAccess(), output.buildResult()));
                check(helper, problems.isEmpty(), "Stored player serialization reported a problem");
                check(helper, PlayerStands.get(loaded).standId().isEmpty() && PlayerWillpower.get(loaded) == 7,
                        "Player save restored an assignment or lost willpower after storage");
                check(helper, ItemStack.matches(loaded.getOffhandItem(), StandDiscItem.containing(ModStands.ECHOES_ACT_1)),
                        "Player save lost the stored disc or its identity");
                check(helper, ModItems.STAND_DISC.get().use(loaded.level(), loaded, InteractionHand.OFF_HAND).consumesAction()
                        && loaded.getOffhandItem().isEmpty()
                        && PlayerStands.get(loaded).standId().equals(Optional.of(ModStands.ECHOES_ACT_1.id())),
                        "The restored stored disc could not equip its original Stand");
            } finally {
                StandController.dismiss(loaded);
                loaded.discard();
            }
        }
        helper.succeed();
    }

    private static void keeperSaveLoad(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            var problems = new ProblemReporter.Collector();
            var output = TagValueOutput.createWithContext(problems, helper.getLevel().registryAccess());
            check(helper, f.keeper.save(output), "Keeper's entity type cannot be saved");
            var loaded = EntityType.loadEntityRecursive(
                    TagValueInput.create(problems, helper.getLevel().registryAccess(), output.buildResult()),
                    helper.getLevel(), EntitySpawnReason.LOAD, entity -> entity);
            check(helper, loaded instanceof DiscKeeperEntity && problems.isEmpty(), "Native entity loader did not restore the keeper");
            try {
                check(helper, loaded.getUUID().equals(f.keeper.getUUID()) && loaded.position().equals(f.keeper.position())
                        && loaded.isPickable() && loaded.isCustomNameVisible(), "Loaded keeper lost its identity or interactable presentation");
                check(helper, f.player.interactOn(loaded, InteractionHand.MAIN_HAND, Vec3.ZERO).consumesAction(),
                        "Loaded keeper did not offer the storage service");
            } finally {
                loaded.discard();
            }
        }
        helper.succeed();
    }

    private static void keeperSharedService(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            var other = new TestPlayer(helper.getLevel());
            other.snapTo(f.player.position(), 0, 0);
            other.setShiftKeyDown(true);
            PlayerStands.assign(other, ModStands.LITTLE_FEET.id());
            PlayerWillpower.set(other, 3);
            other.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.EMPTY_STAND_DISC.get()));
            try {
                check(helper, StandController.toggle(other), "Other player's Stand fixture did not summon");
                var otherActive = StandController.getActive(other).orElseThrow();
                check(helper, use(f, InteractionHand.MAIN_HAND).consumesAction(), "First player could not use the keeper");
                check(helper, PlayerStands.get(other).standId().equals(Optional.of(ModStands.LITTLE_FEET.id()))
                        && !otherActive.isRemoved() && other.getMainHandItem().is(ModItems.EMPTY_STAND_DISC.get()),
                        "One player's storage changed another player's Stand or inventory");
                check(helper, other.interactOn(f.keeper, InteractionHand.MAIN_HAND, Vec3.ZERO).consumesAction(),
                        "The keeper stopped serving other players after one use");
                check(helper, ItemStack.matches(other.getMainHandItem(), StandDiscItem.containing(ModStands.LITTLE_FEET))
                        && ItemStack.matches(f.player.getMainHandItem(), StandDiscItem.containing(ModStands.ECHOES_ACT_1))
                        && PlayerWillpower.get(other) == 3 && PlayerWillpower.get(f.player) == 7 && otherActive.isRemoved(),
                        "The shared service mixed identities, duplicated a Stand or changed willpower");
            } finally {
                StandController.dismiss(other);
                other.discard();
            }
        }
        helper.succeed();
    }

    private static void check(GameTestHelper helper, boolean condition, String message) {
        helper.assertTrue(condition, Component.literal(message));
    }

    private record Fixture(TestPlayer player, DiscKeeperEntity keeper) implements AutoCloseable {
        @Override
        public void close() {
            StandController.dismiss(player);
            player.discard();
            keeper.discard();
        }
    }

    private static final class TestPlayer extends FakePlayer {
        private TestPlayer(ServerLevel level) {
            super(level, new GameProfile(UUID.randomUUID(), "StorageTest"));
            setNoGravity(true);
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
                case "voluntary_storage" -> voluntaryStorage(helper);
                case "storage_requires_disc" -> storageRequiresDisc(helper);
                case "storage_preserves_unavailable_assignment" -> storagePreservesUnavailableAssignment(helper);
                case "storage_invalid_users" -> storageInvalidUsers(helper);
                case "storage_range_and_walls" -> storageRangeAndWalls(helper);
                case "storage_full_inventory" -> storageFullInventory(helper);
                case "storage_player_save_load" -> storagePlayerSaveLoad(helper);
                case "keeper_save_load" -> keeperSaveLoad(helper);
                case "keeper_shared_service" -> keeperSharedService(helper);
                default -> throw new IllegalArgumentException("Unknown storage test: " + name);
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
