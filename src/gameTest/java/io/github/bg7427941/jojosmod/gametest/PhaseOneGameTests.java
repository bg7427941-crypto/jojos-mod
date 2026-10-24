package io.github.bg7427941.jojosmod.gametest;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.bg7427941.jojosmod.registry.ModAttachments;
import io.github.bg7427941.jojosmod.registry.ModItems;
import io.github.bg7427941.jojosmod.stands.PlayerStands;
import io.github.bg7427941.jojosmod.stands.StandData;
import io.netty.buffer.Unpooled;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Server integration tests, loaded only by the gameTest source set. */
@Mod(PhaseOneGameTests.MOD_ID)
public final class PhaseOneGameTests {
    public static final String MOD_ID = "jojosmod_tests";
    private static final Identifier FIRST_STAND = Identifier.fromNamespaceAndPath(MOD_ID, "first");
    private static final Identifier SECOND_STAND = Identifier.fromNamespaceAndPath(MOD_ID, "second");
    private static final List<String> CASES = List.of(
            "items_registered", "single_assignment", "attachment_save_load", "death_copy", "network_round_trip");
    private static final DeferredRegister<MapCodec<? extends GameTestInstance>> TYPES =
            DeferredRegister.create(Registries.TEST_INSTANCE_TYPE, MOD_ID);

    static {
        TYPES.register("phase_one", () -> StandTestInstance.CODEC);
    }

    public PhaseOneGameTests(IEventBus modBus) {
        TYPES.register(modBus);
        modBus.addListener(PhaseOneGameTests::registerTests);
        PhaseTwoGameTests.register(modBus);
        PhaseThreeGameTests.register(modBus);
        PhaseFourGameTests.register(modBus);
        StandDiscGameTests.register(modBus);
        StandStorageGameTests.register(modBus);
    }

    private static void registerTests(RegisterGameTestsEvent event) {
        var environment = event.registerEnvironment(id("phase_one"));
        for (String name : CASES) {
            var data = new TestData<>(environment, Level.OVERWORLD, id("empty"),
                    40, 0, true, Rotation.NONE, false, 1, 1, false, 0);
            event.registerTest(id(name), new StandTestInstance(data, name));
        }
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    private static void itemsRegistered(GameTestHelper helper) {
        assertItem(helper, "stand_arrow", ModItems.STAND_ARROW.get(), 1);
        assertItem(helper, "stand_disc_fragment", ModItems.STAND_DISC_FRAGMENT.get(), 64);
        assertItem(helper, "stand_disc", ModItems.STAND_DISC.get(), 1);
    }

    private static void assertItem(GameTestHelper helper, String name, Item expected, int stackSize) {
        var key = Identifier.fromNamespaceAndPath("jojosmod", name);
        helper.assertTrue(BuiltInRegistries.ITEM.getValue(key) == expected,
                Component.literal("Item is not registered under " + key));
        helper.assertTrue(expected.getDefaultInstance().getMaxStackSize() == stackSize,
                Component.literal("Unexpected stack size for " + key));
    }

    private static void singleAssignment(GameTestHelper helper) {
        var player = new TestPlayer(helper.getLevel());
        assertData(helper, player, StandData.EMPTY);
        helper.assertTrue(!player.hasData(ModAttachments.STAND_DATA),
                Component.literal("Reading an unassigned Stand should not create an attachment"));

        PlayerStands.assign(player, FIRST_STAND);
        assertData(helper, player, new StandData(Optional.of(FIRST_STAND)));
        PlayerStands.assign(player, SECOND_STAND);
        assertData(helper, player, new StandData(Optional.of(SECOND_STAND)));
        PlayerStands.clear(player);
        assertData(helper, player, StandData.EMPTY);
    }

    private static void attachmentSaveLoad(GameTestHelper helper) {
        var source = new TestPlayer(helper.getLevel());
        PlayerStands.assign(source, FIRST_STAND);
        assertSavedAttachments(helper, source, new StandData(Optional.of(FIRST_STAND)));
        PlayerStands.clear(source);
        assertSavedAttachments(helper, source, StandData.EMPTY);
    }

    private static void assertSavedAttachments(GameTestHelper helper, TestPlayer source, StandData expected) {
        var problems = new ProblemReporter.Collector();
        var output = TagValueOutput.createWithContext(problems, helper.getLevel().registryAccess());
        source.serializeAttachments(output);
        var saved = output.buildResult();
        var restored = new TestPlayer(helper.getLevel());
        restored.readAttachments(TagValueInput.create(problems, helper.getLevel().registryAccess(), saved));

        helper.assertTrue(problems.isEmpty(), Component.literal("Attachment serialization reported a problem"));
        helper.assertTrue(restored.hasData(ModAttachments.STAND_DATA),
                Component.literal("Stand attachment was not restored"));
        assertData(helper, restored, expected);
        // This checks the real attachment save/load path, not a client reconnect or a full player-file reload.
    }

    private static void deathCopy(GameTestHelper helper) {
        var original = new TestPlayer(helper.getLevel());
        var replacement = new TestPlayer(helper.getLevel());
        PlayerStands.assign(original, FIRST_STAND);

        // NeoForge's player-death handler invokes this same copy mechanism with isDeath = true.
        replacement.copyAttachmentsFrom(original, true);
        assertData(helper, replacement, new StandData(Optional.of(FIRST_STAND)));
        PlayerStands.clear(replacement);
        assertData(helper, original, new StandData(Optional.of(FIRST_STAND)));
    }

    private static void networkRoundTrip(GameTestHelper helper) {
        for (StandData expected : List.of(StandData.EMPTY, new StandData(Optional.of(FIRST_STAND)))) {
            var buffer = new FriendlyByteBuf(Unpooled.buffer());
            try {
                StandData.STREAM_CODEC.encode(buffer, expected);
                var decoded = StandData.STREAM_CODEC.decode(buffer);
                helper.assertTrue(expected.equals(decoded), Component.literal("Stand network data changed in transit"));
                helper.assertTrue(!buffer.isReadable(), Component.literal("Stand network codec left unread bytes"));
            } finally {
                buffer.release();
            }
        }
    }

    private static void assertData(GameTestHelper helper, TestPlayer player, StandData expected) {
        helper.assertTrue(expected.equals(PlayerStands.get(player)),
                Component.literal("Expected Stand assignment " + expected + " but got " + PlayerStands.get(player)));
    }

    private static final class TestPlayer extends FakePlayer {
        private TestPlayer(ServerLevel level) {
            super(level, new GameProfile(UUID.randomUUID(), "StandTest"));
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
                case "items_registered" -> itemsRegistered(helper);
                case "single_assignment" -> singleAssignment(helper);
                case "attachment_save_load" -> attachmentSaveLoad(helper);
                case "death_copy" -> deathCopy(helper);
                case "network_round_trip" -> networkRoundTrip(helper);
                default -> throw new IllegalArgumentException("Unknown Phase 1 test: " + name);
            }
            helper.succeed();
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
