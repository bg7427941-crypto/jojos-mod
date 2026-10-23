package io.github.bg7427941.jojosmod.gametest;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.bg7427941.jojosmod.registry.ModItems;
import java.util.ArrayList;
import java.util.HashSet;
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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Loads the production recipes and loot resources, including real crafting-menu consumption. */
public final class PhaseFourGameTests {
    private static final String MOD_ID = PhaseOneGameTests.MOD_ID;
    private static final List<String> CASES = List.of(
            "blank_disc_crafting", "recipe_rejects_substitutions", "ancient_city_loot", "fragment_chance_boundary");
    private static final DeferredRegister<MapCodec<? extends GameTestInstance>> TYPES =
            DeferredRegister.create(Registries.TEST_INSTANCE_TYPE, MOD_ID);

    static {
        TYPES.register("phase_four", () -> StandTestInstance.CODEC);
    }

    private PhaseFourGameTests() {}

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
        modBus.addListener(PhaseFourGameTests::registerTests);
    }

    private static void registerTests(RegisterGameTestsEvent event) {
        var environment = event.registerEnvironment(id("phase_four"));
        for (String name : CASES) {
            var data = new TestData<>(environment, Level.OVERWORLD, id("empty"),
                    80, 0, true, Rotation.NONE, false, 1, 1, false, 0);
            event.registerTest(id("phase_four/" + name), new StandTestInstance(data, name));
        }
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    private static List<ItemStack> ingredients() {
        var items = new ArrayList<ItemStack>();
        for (int i = 0; i < 9; i++) {
            items.add(new ItemStack(i == 4 ? Items.ECHO_SHARD
                    : i == 7 ? Items.NETHERITE_INGOT : ModItems.STAND_DISC_FRAGMENT.get()));
        }
        return items;
    }

    private static void blankDiscCrafting(GameTestHelper helper) {
        check(helper, BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("jojosmod", "empty_stand_disc"))
                == ModItems.EMPTY_STAND_DISC.get(), "Empty Stand Disc has the wrong registry ID");
        check(helper, ModItems.EMPTY_STAND_DISC.get().getDefaultInstance().getMaxStackSize() == 1,
                "Empty Stand Discs must stack to one");
        check(helper, ModItems.EMPTY_STAND_DISC.get() != ModItems.STAND_DISC.get(),
                "The old placeholder disc must not substitute for the costly empty disc");

        var owner = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "DiscCrafter"));
        helper.setBlock(1, 1, 1, Blocks.CRAFTING_TABLE);
        var menu = new CraftingMenu(1, owner.getInventory(),
                ContainerLevelAccess.create(helper.getLevel(), helper.absolutePos(new BlockPos(1, 1, 1))));
        try {
            var inputs = ingredients();
            for (int i = 0; i < inputs.size(); i++) {
                menu.getInputGridSlots().get(i).set(inputs.get(i).copyWithCount(2));
            }
            var output = menu.getResultSlot().getItem();
            check(helper, output.is(ModItems.EMPTY_STAND_DISC.get()) && output.getCount() == 1,
                    "Loaded recipe did not produce exactly one empty disc");

            menu.clicked(0, 0, ContainerInput.PICKUP, owner);
            check(helper, menu.getCarried().is(ModItems.EMPTY_STAND_DISC.get()) && menu.getCarried().getCount() == 1,
                    "Crafted disc did not reach the cursor");
            for (var slot : menu.getInputGridSlots()) {
                check(helper, slot.getItem().getCount() == 1, "Crafting did not consume exactly one item from every slot");
            }
            // A full cursor cannot take another unstackable result or consume another set of ingredients.
            menu.clicked(0, 0, ContainerInput.PICKUP, owner);
            check(helper, menu.getCarried().getCount() == 1, "Crafted discs stacked past their maximum");
            for (var slot : menu.getInputGridSlots()) {
                check(helper, slot.getItem().getCount() == 1, "A blocked second pickup consumed ingredients");
            }
        } finally {
            menu.removed(owner);
            owner.discard();
        }
        helper.succeed();
    }

    private static void rejects(GameTestHelper helper, List<ItemStack> items, String message) {
        var level = helper.getLevel();
        var recipe = level.recipeAccess().getRecipeFor(RecipeType.CRAFTING, CraftingInput.of(3, 3, items), level);
        check(helper, recipe.isEmpty(), message);
    }

    private static void recipeRejectsSubstitutions(GameTestHelper helper) {
        for (int missing = 0; missing < 9; missing++) {
            var input = ingredients();
            input.set(missing, ItemStack.EMPTY);
            rejects(helper, input, "Incomplete recipe matched with slot " + missing + " missing");
        }
        var input = ingredients();
        input.set(4, new ItemStack(Items.AMETHYST_SHARD));
        rejects(helper, input, "Amethyst replaced the required echo shard");
        input = ingredients();
        input.set(7, new ItemStack(Items.IRON_INGOT));
        rejects(helper, input, "Iron replaced the required netherite ingot");
        input = ingredients();
        input.set(0, new ItemStack(ModItems.STAND_DISC.get()));
        rejects(helper, input, "The old placeholder disc bypassed the fragment cost");
        input = ingredients();
        input.set(4, new ItemStack(ModItems.STAND_DISC_FRAGMENT.get()));
        input.set(7, new ItemStack(ModItems.STAND_DISC_FRAGMENT.get()));
        rejects(helper, input, "Nine fragments bypassed both rare ingredients");
        helper.succeed();
    }

    private static LootParams chestParams(GameTestHelper helper) {
        return new LootParams.Builder(helper.getLevel())
                .withParameter(LootContextParams.ORIGIN, helper.absoluteVec(Vec3.ZERO))
                .create(LootContextParamSets.CHEST);
    }

    @SuppressWarnings("deprecation") // Compare actual vanilla output before and after the registered modifiers.
    private static void ancientCityLoot(GameTestHelper helper) {
        var tables = helper.getLevel().getServer().reloadableRegistries();
        var city = tables.getLootTable(BuiltInLootTables.ANCIENT_CITY);
        var params = chestParams(helper);
        var quantities = new HashSet<Integer>();
        for (int seed = 1; seed <= 256; seed++) {
            var baseline = new ArrayList<ItemStack>();
            // Ancient City potion entries can roll 2–3 unstackable items; use vanilla's splitter on both paths.
            city.getRandomItemsRaw(new LootContext.Builder(params)
                    .withOptionalRandomSource(RandomSource.create(seed)).create(Optional.empty()),
                    LootTable.createStackSplitter(helper.getLevel(), baseline::add));
            var modified = city.getRandomItems(params, RandomSource.create(seed));
            var vanillaOnly = modified.stream().filter(stack -> !stack.is(ModItems.STAND_DISC_FRAGMENT.get())).toList();
            check(helper, baseline.size() == vanillaOnly.size(), "Fragment modifier changed the number of vanilla stacks");
            for (int i = 0; i < baseline.size(); i++) {
                check(helper, ItemStack.matches(baseline.get(i), vanillaOnly.get(i)), "Fragment modifier changed vanilla loot");
            }
            int count = modified.stream().filter(stack -> stack.is(ModItems.STAND_DISC_FRAGMENT.get()))
                    .mapToInt(ItemStack::getCount).sum();
            check(helper, count >= 0 && count <= 2, "An Ancient City chest rolled more than two fragments");
            quantities.add(count);
            check(helper, modified.stream().noneMatch(stack -> stack.is(ModItems.EMPTY_STAND_DISC.get())),
                    "An empty disc bypassed its crafting cost via chest loot");

            for (var excluded : List.of(BuiltInLootTables.ANCIENT_CITY_ICE_BOX, BuiltInLootTables.SIMPLE_DUNGEON)) {
                check(helper, tables.getLootTable(excluded).getRandomItems(params, RandomSource.create(seed)).stream()
                        .noneMatch(stack -> stack.is(ModItems.STAND_DISC_FRAGMENT.get())),
                        "Fragments leaked into an excluded chest table");
            }
        }
        check(helper, quantities.equals(new HashSet<>(List.of(0, 1, 2))),
                "Controlled seeds did not exercise absent, single and double fragment drops");
        helper.succeed();
    }

    private static void fragmentChanceBoundary(GameTestHelper helper) {
        var key = ResourceKey.create(Registries.LOOT_TABLE,
                Identifier.fromNamespaceAndPath("jojosmod", "chests/stand_disc_fragments"));
        var table = helper.getLevel().getServer().reloadableRegistries().getLootTable(key);
        for (float roll : new float[] {0.0F, 0.24999F, 0.25F, 0.99999F}) {
            var random = new LegacyRandomSource(1) {
                @Override
                public float nextFloat() {
                    return roll;
                }
            };
            var drops = table.getRandomItems(chestParams(helper), random);
            if (roll < 0.25F) {
                check(helper, drops.size() == 1 && drops.getFirst().is(ModItems.STAND_DISC_FRAGMENT.get())
                        && drops.getFirst().getCount() >= 1 && drops.getFirst().getCount() <= 2,
                        "A roll below 25% did not produce one or two fragments");
            } else {
                check(helper, drops.isEmpty(), "A roll at or above 25% produced fragments");
            }
        }
        helper.succeed();
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
                case "blank_disc_crafting" -> blankDiscCrafting(helper);
                case "recipe_rejects_substitutions" -> recipeRejectsSubstitutions(helper);
                case "ancient_city_loot" -> ancientCityLoot(helper);
                case "fragment_chance_boundary" -> fragmentChanceBoundary(helper);
                default -> throw new IllegalArgumentException("Unknown Phase 4 test: " + name);
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
