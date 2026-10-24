package io.github.bg7427941.jojosmod.gametest;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.bg7427941.jojosmod.items.StandDiscItem;
import io.github.bg7427941.jojosmod.registry.ModAttachments;
import io.github.bg7427941.jojosmod.registry.ModDataComponents;
import io.github.bg7427941.jojosmod.registry.ModItems;
import io.github.bg7427941.jojosmod.registry.ModStands;
import io.github.bg7427941.jojosmod.stands.PlayerStands;
import io.github.bg7427941.jojosmod.stands.PlayerWillpower;
import io.github.bg7427941.jojosmod.stands.StandController;
import io.github.bg7427941.jojosmod.stands.StandProgressionConfig;
import io.github.bg7427941.jojosmod.stands.WillpowerData;
import io.netty.buffer.Unpooled;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Exercises production recipes, held-item use and actual attachment/item serialization. */
public final class StandDiscGameTests {
    private static final String MOD_ID = PhaseOneGameTests.MOD_ID;
    private static final List<String> CASES = List.of(
            "core_disc_crafting", "core_recipe_rejections", "disc_survival", "disc_preserves_assignment",
            "disc_invalid_inputs", "disc_creative_offhand", "disc_willpower_gate",
            "disc_identity_round_trip", "willpower_persistence", "willpower_network");
    private static final DeferredRegister<MapCodec<? extends GameTestInstance>> TYPES =
            DeferredRegister.create(Registries.TEST_INSTANCE_TYPE, MOD_ID);

    static {
        TYPES.register("stand_discs", () -> StandTestInstance.CODEC);
    }

    private StandDiscGameTests() {}

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
        modBus.addListener(StandDiscGameTests::registerTests);
    }

    private static void registerTests(RegisterGameTestsEvent event) {
        var environment = event.registerEnvironment(id("stand_discs"));
        for (String name : CASES) {
            var data = new TestData<>(environment, Level.OVERWORLD, id("empty"),
                    80, 0, true, Rotation.NONE, false, 1, 1, false, 0);
            event.registerTest(id("stand_discs/" + name), new StandTestInstance(data, name));
        }
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    private static TestPlayer player(GameTestHelper helper) {
        var player = new TestPlayer(helper.getLevel());
        player.snapTo(helper.absoluteVec(new net.minecraft.world.phys.Vec3(1.5, 1, 0.5)), 0, 0);
        return player;
    }

    private static InteractionResult use(ServerPlayer player, InteractionHand hand) {
        return ModItems.STAND_DISC.get().use(player.level(), player, hand);
    }

    private static void cleanup(ServerPlayer... players) {
        for (var player : players) {
            StandController.dismiss(player);
            player.discard();
        }
    }

    private static void coreDiscCrafting(GameTestHelper helper) {
        var cores = List.of(ModItems.ECHOES_ACT_1_CORE.get(), ModItems.LITTLE_FEET_CORE.get());
        var stands = List.of(ModStands.ECHOES_ACT_1, ModStands.LITTLE_FEET);
        for (int variant = 0; variant < cores.size(); variant++) {
            var core = cores.get(variant);
            var stand = stands.get(variant);
            check(helper, BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("jojosmod",
                    variant == 0 ? "echoes_act_1_core" : "little_feet_core")) == core, "Core registry ID changed");
            check(helper, core.getDefaultInstance().getMaxStackSize() == 1, "Cores must not stack");
            var owner = player(helper);
            helper.setBlock(1, 1, 1, Blocks.CRAFTING_TABLE);
            var menu = new CraftingMenu(1, owner.getInventory(), ContainerLevelAccess.create(helper.getLevel(),
                    helper.absolutePos(new BlockPos(1, 1, 1))));
            try {
                menu.getInputGridSlots().get(2).set(new ItemStack(core));
                menu.getInputGridSlots().get(6).set(new ItemStack(ModItems.EMPTY_STAND_DISC.get()));
                var result = menu.getResultSlot().getItem();
                check(helper, result.is(ModItems.STAND_DISC.get()) && result.getCount() == 1,
                        "Loaded core recipe did not create exactly one full disc");
                check(helper, stand.id().equals(result.get(ModDataComponents.STAND_ID.get())),
                        "Recipe lost or swapped the core's Stand identity");
                check(helper, result.getHoverName().getString().contains(
                        Component.translatable(stand.translationKey()).getString()), "Disc name hides its identity");
                menu.clicked(0, 0, ContainerInput.PICKUP, owner);
                check(helper, ItemStack.matches(menu.getCarried(), StandDiscItem.containing(stand)),
                        "Crafted identity did not reach the cursor");
                check(helper, menu.getInputGridSlots().stream().allMatch(slot -> slot.getItem().isEmpty()),
                        "Crafting did not consume both the core and the costly empty disc");
                menu.clicked(0, 0, ContainerInput.PICKUP, owner);
                check(helper, menu.getCarried().getCount() == 1, "Repeated pickup duplicated the disc");

                var input = CraftingInput.of(2, 2, List.of(new ItemStack(ModItems.EMPTY_STAND_DISC.get()),
                        ItemStack.EMPTY, ItemStack.EMPTY, new ItemStack(core)));
                var recipe = helper.getLevel().recipeAccess().getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel());
                check(helper, recipe.isPresent() && ItemStack.matches(recipe.orElseThrow().value().assemble(input),
                        StandDiscItem.containing(stand)), "Shapeless recipe failed in a 2x2 inventory grid");
            } finally {
                menu.removed(owner);
                cleanup(owner);
            }
        }
        helper.succeed();
    }

    private static void coreRecipeRejections(GameTestHelper helper) {
        var core = new ItemStack(ModItems.ECHOES_ACT_1_CORE.get());
        for (var substitute : List.of(ItemStack.EMPTY, new ItemStack(ModItems.STAND_DISC.get()),
                StandDiscItem.containing(ModStands.LITTLE_FEET), new ItemStack(ModItems.STAND_DISC_FRAGMENT.get()),
                new ItemStack(Items.MUSIC_DISC_13))) {
            rejects(helper, List.of(substitute, core, ItemStack.EMPTY, ItemStack.EMPTY));
        }
        for (var substitute : List.of(ItemStack.EMPTY, new ItemStack(ModItems.STAND_DISC_FRAGMENT.get()),
                new ItemStack(Items.ECHO_SHARD))) {
            rejects(helper, List.of(new ItemStack(ModItems.EMPTY_STAND_DISC.get()), substitute,
                    ItemStack.EMPTY, ItemStack.EMPTY));
        }
        rejects(helper, List.of(new ItemStack(ModItems.EMPTY_STAND_DISC.get()), core,
                new ItemStack(ModItems.LITTLE_FEET_CORE.get()), ItemStack.EMPTY));
        helper.succeed();
    }

    private static void rejects(GameTestHelper helper, List<ItemStack> items) {
        check(helper, helper.getLevel().recipeAccess().getRecipeFor(RecipeType.CRAFTING,
                CraftingInput.of(2, 2, items), helper.getLevel()).isEmpty(), "Invalid ingredients bypassed the full-disc cost");
    }

    private static void discSurvival(GameTestHelper helper) {
        for (var stand : List.of(ModStands.ECHOES_ACT_1, ModStands.LITTLE_FEET)) {
            var owner = player(helper);
            try {
                owner.setItemInHand(InteractionHand.MAIN_HAND, StandDiscItem.containing(stand));
                float health = owner.getHealth();
                check(helper, use(owner, InteractionHand.MAIN_HAND).consumesAction(), "Valid disc use failed");
                check(helper, owner.getMainHandItem().isEmpty(), "Survival disc was not consumed");
                check(helper, PlayerStands.get(owner).standId().equals(Optional.of(stand.id())), "Wrong Stand was equipped");
                check(helper, owner.getHealth() == health && PlayerWillpower.get(owner) == 1,
                        "Equipping spent health or permanent willpower");
                check(helper, StandController.toggle(owner), "Equipped Stand could not be summoned");
                check(helper, StandController.getActive(owner).orElseThrow().getStandDefinition().equals(stand),
                        "Equipped Stand manifested with the wrong definition");
                check(helper, use(owner, InteractionHand.MAIN_HAND) == InteractionResult.FAIL,
                        "Repeated use of an empty hand granted another Stand");
            } finally {
                cleanup(owner);
            }
        }
        helper.succeed();
    }

    private static void discPreservesAssignment(GameTestHelper helper) {
        var owner = player(helper);
        try {
            PlayerStands.assign(owner, ModStands.ECHOES_ACT_1.id());
            check(helper, StandController.toggle(owner), "Fixture could not summon its existing Stand");
            var active = StandController.getActive(owner).orElseThrow();
            for (var mode : List.of(GameType.SURVIVAL, GameType.CREATIVE)) {
                owner.setGameMode(mode);
                var disc = StandDiscItem.containing(ModStands.LITTLE_FEET);
                owner.setItemInHand(InteractionHand.OFF_HAND, disc.copy());
                check(helper, use(owner, InteractionHand.OFF_HAND) == InteractionResult.FAIL, "Disc replaced an existing Stand");
                check(helper, ItemStack.matches(disc, owner.getOffhandItem()), "Rejected disc lost its identity or count");
                check(helper, PlayerStands.get(owner).standId().equals(Optional.of(ModStands.ECHOES_ACT_1.id()))
                        && StandController.getActive(owner).orElseThrow() == active, "Rejection changed the existing manifestation");
            }
            StandController.dismiss(owner);
            PlayerStands.assign(owner, id("unavailable_assignment"));
            check(helper, use(owner, InteractionHand.OFF_HAND) == InteractionResult.FAIL
                    && PlayerStands.get(owner).standId().equals(Optional.of(id("unavailable_assignment"))),
                    "An unknown saved assignment allowed replacement");
        } finally {
            cleanup(owner);
        }
        helper.succeed();
    }

    private static void discInvalidInputs(GameTestHelper helper) {
        var owner = player(helper);
        try {
            var unknown = StandDiscItem.containing(ModStands.ECHOES_ACT_1);
            unknown.set(ModDataComponents.STAND_ID.get(), id("unavailable_disc"));
            for (var stack : List.of(new ItemStack(ModItems.STAND_DISC.get()), unknown,
                    new ItemStack(ModItems.EMPTY_STAND_DISC.get()), new ItemStack(ModItems.ECHOES_ACT_1_CORE.get()),
                    ItemStack.EMPTY)) {
                owner.setItemInHand(InteractionHand.MAIN_HAND, stack.copy());
                check(helper, use(owner, InteractionHand.MAIN_HAND) == InteractionResult.FAIL, "Invalid disc granted a Stand");
                check(helper, ItemStack.matches(stack, owner.getMainHandItem())
                        && PlayerStands.get(owner).standId().isEmpty(), "Invalid disc use changed inventory or assignment");
            }
            for (int state = 0; state < 3; state++) {
                var invalid = player(helper);
                try {
                    if (state == 0) invalid.setGameMode(GameType.SPECTATOR);
                    if (state == 1) invalid.setHealth(0);
                    if (state == 2) invalid.discard();
                    var disc = StandDiscItem.containing(ModStands.LITTLE_FEET);
                    invalid.setItemInHand(InteractionHand.OFF_HAND, disc.copy());
                    check(helper, use(invalid, InteractionHand.OFF_HAND) == InteractionResult.FAIL
                            && ItemStack.matches(disc, invalid.getOffhandItem())
                            && PlayerStands.get(invalid).standId().isEmpty(), "Ineligible player consumed or equipped a disc");
                } finally {
                    cleanup(invalid);
                }
            }
        } finally {
            cleanup(owner);
        }
        helper.succeed();
    }

    private static void discCreativeOffhand(GameTestHelper helper) {
        var owner = player(helper);
        try {
            owner.setGameMode(GameType.CREATIVE);
            var disc = StandDiscItem.containing(ModStands.LITTLE_FEET);
            owner.setItemInHand(InteractionHand.OFF_HAND, disc.copy());
            check(helper, use(owner, InteractionHand.OFF_HAND).consumesAction(), "Creative offhand disc use failed");
            check(helper, ItemStack.matches(disc, owner.getOffhandItem()), "Creative use consumed or modified the disc");
            check(helper, PlayerStands.get(owner).standId().equals(Optional.of(ModStands.LITTLE_FEET.id())),
                    "Creative disc granted the wrong Stand");
            check(helper, use(owner, InteractionHand.OFF_HAND) == InteractionResult.FAIL, "Creative reuse bypassed single assignment");
        } finally {
            cleanup(owner);
        }
        helper.succeed();
    }

    private static void discWillpowerGate(GameTestHelper helper) {
        for (var stand : List.of(ModStands.ECHOES_ACT_1, ModStands.LITTLE_FEET)) {
            var owner = player(helper);
            try {
                int required = StandProgressionConfig.requiredWillpower(stand);
                check(helper, required > 0, "This boundary test requires a positive configured requirement");
                PlayerWillpower.set(owner, required - 1);
                var disc = StandDiscItem.containing(stand);
                owner.setItemInHand(InteractionHand.OFF_HAND, disc.copy());
                check(helper, use(owner, InteractionHand.OFF_HAND) == InteractionResult.FAIL
                        && ItemStack.matches(disc, owner.getOffhandItem()) && PlayerStands.get(owner).standId().isEmpty(),
                        "Insufficient willpower consumed or equipped a disc");
                PlayerWillpower.set(owner, required);
                check(helper, use(owner, InteractionHand.OFF_HAND).consumesAction() && owner.getOffhandItem().isEmpty(),
                        "The exact willpower threshold did not allow equipping");
                check(helper, PlayerWillpower.get(owner) == required, "Equipping consumed permanent willpower");
            } finally {
                cleanup(owner);
            }
        }
        helper.succeed();
    }

    private static void discIdentityRoundTrip(GameTestHelper helper) {
        var unknown = StandDiscItem.containing(ModStands.ECHOES_ACT_1);
        unknown.set(ModDataComponents.STAND_ID.get(), id("unavailable_disc"));
        var ops = helper.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
        for (var stack : List.of(StandDiscItem.containing(ModStands.ECHOES_ACT_1),
                StandDiscItem.containing(ModStands.LITTLE_FEET), unknown, new ItemStack(ModItems.STAND_DISC.get()))) {
            var saved = ItemStack.CODEC.encodeStart(ops, stack).getOrThrow();
            var restored = ItemStack.CODEC.parse(ops, saved).getOrThrow();
            check(helper, ItemStack.matches(stack, restored), "Disc identity did not survive item save/load");
            var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
            try {
                ItemStack.STREAM_CODEC.encode(buffer, stack);
                check(helper, ItemStack.matches(stack, ItemStack.STREAM_CODEC.decode(buffer)) && !buffer.isReadable(),
                        "Disc identity did not survive inventory synchronization");
            } finally {
                buffer.release();
            }
        }
        helper.succeed();
    }

    private static void willpowerPersistence(GameTestHelper helper) {
        var owner = player(helper);
        var loaded = player(helper);
        var respawned = player(helper);
        try {
            check(helper, PlayerWillpower.get(owner) == 1 && !owner.hasData(ModAttachments.WILLPOWER),
                    "A legacy player did not get the initial value through a read without mutation");
            for (int value : new int[] {7, 0, WillpowerData.MAX_VALUE}) {
                PlayerWillpower.set(owner, value);
                PlayerStands.assign(owner, ModStands.ECHOES_ACT_1.id());
                PlayerStands.clear(owner);
                check(helper, PlayerWillpower.get(owner) == value, "Changing the Stand reset willpower");
                var problems = new ProblemReporter.Collector();
                var output = TagValueOutput.createWithContext(problems, helper.getLevel().registryAccess());
                owner.serializeAttachments(output);
                loaded.readAttachments(TagValueInput.create(problems, helper.getLevel().registryAccess(), output.buildResult()));
                respawned.copyAttachmentsFrom(owner, true);
                check(helper, problems.isEmpty() && PlayerWillpower.get(loaded) == value
                        && PlayerWillpower.get(respawned) == value, "Willpower changed during save/load or death copy");
                PlayerWillpower.set(respawned, 1);
                check(helper, PlayerWillpower.get(owner) == value, "One player's willpower mutation affected another");
            }
        } finally {
            cleanup(owner, loaded, respawned);
        }
        helper.succeed();
    }

    private static void willpowerNetwork(GameTestHelper helper) {
        for (int value : new int[] {0, 1, 7, WillpowerData.MAX_VALUE}) {
            var expected = new WillpowerData(value);
            var buffer = new FriendlyByteBuf(Unpooled.buffer());
            try {
                WillpowerData.STREAM_CODEC.encode(buffer, expected);
                check(helper, expected.equals(WillpowerData.STREAM_CODEC.decode(buffer)) && !buffer.isReadable(),
                        "Willpower changed in its owner synchronization packet");
            } finally {
                buffer.release();
            }
        }
        helper.succeed();
    }

    private static void check(GameTestHelper helper, boolean condition, String message) {
        helper.assertTrue(condition, Component.literal(message));
    }

    private static final class TestPlayer extends FakePlayer {
        private TestPlayer(ServerLevel level) {
            super(level, new GameProfile(UUID.randomUUID(), "DiscTest"));
            setNoGravity(true);
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
                case "core_disc_crafting" -> coreDiscCrafting(helper);
                case "core_recipe_rejections" -> coreRecipeRejections(helper);
                case "disc_survival" -> discSurvival(helper);
                case "disc_preserves_assignment" -> discPreservesAssignment(helper);
                case "disc_invalid_inputs" -> discInvalidInputs(helper);
                case "disc_creative_offhand" -> discCreativeOffhand(helper);
                case "disc_willpower_gate" -> discWillpowerGate(helper);
                case "disc_identity_round_trip" -> discIdentityRoundTrip(helper);
                case "willpower_persistence" -> willpowerPersistence(helper);
                case "willpower_network" -> willpowerNetwork(helper);
                default -> throw new IllegalArgumentException("Unknown Stand Disc test: " + name);
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
