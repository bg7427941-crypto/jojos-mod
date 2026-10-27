package io.github.bg7427941.jojosmod.gametest;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.bg7427941.jojosmod.abilities.StarPlatinumActions;
import io.github.bg7427941.jojosmod.entities.DiscKeeperEntity;
import io.github.bg7427941.jojosmod.entities.TrainingStandEntity;
import io.github.bg7427941.jojosmod.items.StandDiscItem;
import io.github.bg7427941.jojosmod.network.StandVoicePayload;
import io.github.bg7427941.jojosmod.network.StandVoicePayload.Cue;
import io.github.bg7427941.jojosmod.registry.ModAttachments;
import io.github.bg7427941.jojosmod.registry.ModEntities;
import io.github.bg7427941.jojosmod.registry.ModItems;
import io.github.bg7427941.jojosmod.registry.ModStands;
import io.github.bg7427941.jojosmod.stands.PlayerStands;
import io.github.bg7427941.jojosmod.stands.PlayerWillpower;
import io.github.bg7427941.jojosmod.stands.StandController;
import io.github.bg7427941.jojosmod.stands.StarPlatinumSession.Action;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.PacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Real timed server actions, incoming-damage hooks, loot, crafting and persistence. */
public final class StarPlatinumGameTests {
    private static final String MOD_ID = PhaseOneGameTests.MOD_ID;
    private static final List<String> CASES = List.of(
            "awakening_milestone", "precise_strike_timing", "barrage_and_cooldown", "damage_interrupts_barrage",
            "guard_direction_and_expiry", "pvp_and_teams", "walls_stop_barrage", "invalid_owners_and_range",
            "desert_pyramid_loot", "disc_crafting_and_storage", "voice_cues_and_cancellation");
    private static final DeferredRegister<MapCodec<? extends GameTestInstance>> TYPES =
            DeferredRegister.create(Registries.TEST_INSTANCE_TYPE, MOD_ID);

    static { TYPES.register("star_platinum", () -> StandTestInstance.CODEC); }

    private StarPlatinumGameTests() {}

    public static void register(IEventBus bus) {
        TYPES.register(bus);
        bus.addListener(StarPlatinumGameTests::registerTests);
    }

    private static void registerTests(RegisterGameTestsEvent event) {
        var environment = event.registerEnvironment(id("star_platinum"));
        for (var name : CASES) {
            var data = new TestData<>(environment, Level.OVERWORLD, id("star_arena"),
                    160, 0, true, Rotation.NONE, false, 1, 1, false, 0);
            event.registerTest(id("star_platinum/" + name), new StandTestInstance(data, name));
        }
    }

    private static Identifier id(String path) { return Identifier.fromNamespaceAndPath(MOD_ID, path); }

    private static Fixture fixture(GameTestHelper helper) { return fixture(helper, 1.5); }

    private static Fixture fixture(GameTestHelper helper, double x) {
        var owner = new TestPlayer(helper.getLevel());
        owner.snapTo(helper.absoluteVec(new Vec3(x, 1, 0.5)), 0, 0);
        PlayerStands.assign(owner, ModStands.STAR_PLATINUM.id());
        check(helper, StandController.toggle(owner), "Star Platinum could not summon");
        var stand = StandController.getActive(owner).orElseThrow();
        check(helper, stand.getType() == ModEntities.STAR_PLATINUM.get(), "Star Platinum uses the placeholder entity type");
        var target = helper.spawn(EntityTypes.VILLAGER, new BlockPos((int)x, 1, 2));
        target.setNoAi(true);
        target.setNoGravity(true);
        target.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
        return new Fixture(owner, stand, target);
    }

    private static void tick(Fixture f) { NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(f.owner)); }

    private static void awakeningMilestone(GameTestHelper helper) {
        var owner = new TestPlayer(helper.getLevel());
        var loaded = new TestPlayer(helper.getLevel());
        var respawned = new TestPlayer(helper.getLevel());
        try {
            owner.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(ModItems.STARDUST_ARROW.get()));
            check(helper, ModItems.STARDUST_ARROW.get().use(owner.level(), owner, InteractionHand.OFF_HAND).consumesAction(),
                    "Stardust arrow did not awaken a Stand");
            check(helper, owner.getOffhandItem().isEmpty() && PlayerStands.get(owner).standId().equals(Optional.of(ModStands.STAR_PLATINUM.id()))
                    && PlayerWillpower.get(owner) == 3, "Awakening changed identity, consumption or the one-time reward");
            PlayerStands.clear(owner);
            PlayerStands.assign(owner, ModStands.STAR_PLATINUM.id());
            check(helper, PlayerWillpower.get(owner) == 3, "Repeated acquisition farmed willpower");
            var problems = new ProblemReporter.Collector();
            var output = TagValueOutput.createWithContext(problems, helper.getLevel().registryAccess());
            owner.serializeAttachments(output);
            loaded.readAttachments(TagValueInput.create(problems, helper.getLevel().registryAccess(), output.buildResult()));
            respawned.copyAttachmentsFrom(owner, true);
            for (var p : List.of(loaded, respawned)) {
                PlayerStands.clear(p);
                PlayerStands.assign(p, ModStands.STAR_PLATINUM.id());
                check(helper, PlayerWillpower.get(p) == 3 && p.getData(ModAttachments.STAR_PROGRESS).awakened(),
                        "Saving or death copy reset the milestone reward");
            }
            check(helper, problems.isEmpty(), "Milestone serialization reported a problem");
            owner.setGameMode(GameType.CREATIVE);
            PlayerStands.clear(owner);
            owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.STARDUST_ARROW.get()));
            check(helper, ModItems.STARDUST_ARROW.get().use(owner.level(), owner, InteractionHand.MAIN_HAND).consumesAction()
                    && owner.getMainHandItem().getCount() == 1 && PlayerWillpower.get(owner) == 3,
                    "Creative awakening consumed its arrow or repeated the milestone");
        } finally {
            owner.discard(); loaded.discard(); respawned.discard();
        }
        helper.succeed();
    }

    private static void preciseStrikeTiming(GameTestHelper helper) {
        var f = fixture(helper);
        helper.onEachTick(() -> tick(f));
        check(helper, StandController.attack(f.owner), "Precise strike did not start");
        check(helper, !StandController.attack(f.owner) && f.target.getHealth() == 20, "Strike ignored preparation or cooldown");
        helper.runAfterDelay(3, () -> check(helper, f.target.getHealth() == 20, "Strike landed before its windup"));
        helper.runAfterDelay(6, () -> check(helper, f.target.getHealth() == 14, "Precise strike did not deal its explicit six damage"));
        helper.runAfterDelay(17, () -> check(helper, StandController.attack(f.owner), "Strike cooldown did not recover"));
        helper.runAfterDelay(24, () -> {
            check(helper, f.target.getHealth() == 8, "Second precise strike did not resolve once");
            f.close(); helper.succeed();
        });
    }

    private static void barrageAndCooldown(GameTestHelper helper) {
        var f = fixture(helper);
        helper.onEachTick(() -> tick(f));
        check(helper, StarPlatinumActions.barrage(f.owner), "Barrage did not start");
        long readyAt = StarPlatinumActions.session(f.owner).barrageReadyAt();
        check(helper, !StarPlatinumActions.barrage(f.owner) && !StarPlatinumActions.toggleGuard(f.owner)
                && !StandController.attack(f.owner), "Actions stacked during a barrage");
        helper.runAfterDelay(3, () -> check(helper, f.target.getHealth() == 20, "Barrage skipped preparation"));
        helper.runAfterDelay(28, () -> check(helper, f.target.getHealth() == 12,
                "The eight barrage hits did not bypass only their own normal hit cooldown"));
        helper.runAfterDelay(32, () -> check(helper, !StarPlatinumActions.barrage(f.owner)
                && StarPlatinumActions.session(f.owner).barrageReadyAt() == readyAt, "Rejected input reset or bypassed barrage cooldown"));
        helper.runAfterDelay(102, () -> check(helper, StarPlatinumActions.barrage(f.owner), "Barrage did not recover after five seconds"));
        helper.runAfterDelay(107, () -> {
            check(helper, f.target.getHealth() < 12, "Recovered barrage did not hit");
            f.close(); helper.succeed();
        });
    }

    private static void damageInterruptsBarrage(GameTestHelper helper) {
        var f = fixture(helper);
        helper.onEachTick(() -> tick(f));
        check(helper, StarPlatinumActions.barrage(f.owner), "Barrage fixture did not start");
        helper.runAfterDelay(6, () -> {
            check(helper, f.target.getHealth() == 19, "Fixture did not reach exactly its first pulse");
            f.owner.hurtServer(helper.getLevel(), f.owner.damageSources().generic(), 1);
            check(helper, StarPlatinumActions.session(f.owner).action() == Action.NONE, "Incoming damage did not interrupt barrage");
        });
        helper.runAfterDelay(32, () -> {
            check(helper, f.target.getHealth() == 19 && !StarPlatinumActions.barrage(f.owner), "Interrupted barrage resumed or reset cooldown");
            f.close(); helper.succeed();
        });
    }

    private static void guardDirectionAndExpiry(GameTestHelper helper) {
        for (int scenario = 0; scenario < 3; scenario++) {
            try (var f = fixture(helper)) {
                check(helper, StarPlatinumActions.toggleGuard(f.owner), "Guard did not start");
                check(helper, f.owner.getAttributeValue(Attributes.MOVEMENT_SPEED) < 0.1, "Guard did not slow movement");
                var attacker = new TestPlayer(helper.getLevel());
                attacker.snapTo(f.owner.position().add(0, 0, scenario == 1 ? -2 : 2), 0, 0);
                try {
                    var damage = scenario == 2 ? f.owner.damageSources().onFire() : f.owner.damageSources().playerAttack(attacker);
                    f.owner.hurtServer(helper.getLevel(), damage, 10);
                } finally { attacker.discard(); }
                check(helper, Math.abs(f.owner.getHealth() - (scenario == 0 ? 16 : 10)) < 0.001,
                        "Guard did not distinguish frontal physical damage from rear or fire damage");
            }
        }
        var f = fixture(helper);
        helper.onEachTick(() -> {
            tick(f);
            // Fake owners do not keep every test chunk entity-ticking. Exercise the real entity timer explicitly.
            if (!f.stand.isRemoved()) f.stand.tick();
        });
        check(helper, StarPlatinumActions.toggleGuard(f.owner), "Expiry fixture did not guard");
        helper.runAfterDelay(42, () -> check(helper, StarPlatinumActions.session(f.owner).action() == Action.NONE
                && Math.abs(f.owner.getAttributeValue(Attributes.MOVEMENT_SPEED) - 0.1) < 0.0001
                && !StarPlatinumActions.toggleGuard(f.owner), "Guard expiry left slow movement or bypassed cooldown"));
        helper.runAfterDelay(82, () -> {
            check(helper, StarPlatinumActions.toggleGuard(f.owner), "Guard did not recover");
            StandController.dismiss(f.owner);
            check(helper, !StarPlatinumActions.isBusy(f.owner)
                    && Math.abs(f.owner.getAttributeValue(Attributes.MOVEMENT_SPEED) - 0.1) < 0.0001
                    && StandController.getActive(f.owner).isEmpty(), "Dismissal left a guard, slow modifier or active Stand");
        });
        helper.runAfterDelay(85, () -> check(helper, !f.stand.isRemoved() && f.stand.isDismissing(),
                "Desummon removed the entity before its four-tick animation"));
        helper.runAfterDelay(88, () -> {
            check(helper, f.stand.isRemoved(), "Desummon animation never removed the entity");
            f.close(); helper.succeed();
        });
    }

    private static void pvpAndTeams(GameTestHelper helper) {
        var blocked = fixture(helper, 1.5);
        var allied = fixture(helper, 5.5);
        blocked.target.discard(); allied.target.discard();
        var victim = new TestPlayer(helper.getLevel());
        var friend = new TestPlayer(helper.getLevel());
        victim.snapTo(blocked.owner.position().add(0, 0, 2), 0, 0);
        friend.snapTo(allied.owner.position().add(0, 0, 2), 0, 0);
        helper.getLevel().addNewPlayer(victim);
        helper.getLevel().addNewPlayer(friend);
        var board = helper.getLevel().getScoreboard();
        var team = board.addPlayerTeam("star_" + UUID.randomUUID().toString().substring(0, 8));
        team.setAllowFriendlyFire(false);
        board.addPlayerToTeam(allied.owner.getScoreboardName(), team);
        board.addPlayerToTeam(friend.getScoreboardName(), team);
        check(helper, StarPlatinumActions.barrage(blocked.owner) && StarPlatinumActions.barrage(allied.owner), "PvP fixtures could not barrage");
        helper.onEachTick(() -> {
            var rules = helper.getLevel().getGameRules();
            boolean original = rules.get(GameRules.PVP);
            try {
                rules.set(GameRules.PVP, false, null);
                tick(blocked);
                rules.set(GameRules.PVP, true, null);
                tick(allied);
            } finally {
                rules.set(GameRules.PVP, original, null);
            }
        });
        helper.runAfterDelay(33, () -> {
            check(helper, victim.getHealth() == 20 && friend.getHealth() == 20,
                    "Barrage bypassed disabled PvP or friendly-fire team rules");
            var rules = helper.getLevel().getGameRules();
            boolean original = rules.get(GameRules.PVP);
            try {
                rules.set(GameRules.PVP, true, null);
                team.setAllowFriendlyFire(true);
                check(helper, io.github.bg7427941.jojosmod.abilities.BasicAttack.hit(blocked.owner, blocked.stand, 2, 3, false)
                        && io.github.bg7427941.jojosmod.abilities.BasicAttack.hit(allied.owner, allied.stand, 2, 3, false)
                        && victim.getHealth() == 18 && friend.getHealth() == 18,
                        "PvP controls could not damage real server-player fixtures when permitted");
            } finally { rules.set(GameRules.PVP, original, null); }
            blocked.close(); allied.close(); victim.discard(); friend.discard(); board.removePlayerTeam(team);
            helper.succeed();
        });
    }

    private static void wallsStopBarrage(GameTestHelper helper) {
        var f = fixture(helper);
        helper.setBlock(1, 2, 1, Blocks.STONE);
        helper.onEachTick(() -> tick(f));
        check(helper, StarPlatinumActions.barrage(f.owner), "Wall fixture did not start barrage");
        helper.runAfterDelay(33, () -> {
            check(helper, f.target.getHealth() == 20, "A barrage pulse passed through a wall");
            f.close(); helper.succeed();
        });
    }

    private static void invalidOwnersAndRange(GameTestHelper helper) {
        var empty = new TestPlayer(helper.getLevel());
        try {
            check(helper, !StarPlatinumActions.barrage(empty) && !StarPlatinumActions.toggleGuard(empty)
                    && !empty.hasData(ModAttachments.STAR_SESSION), "Unassigned requests created an action");
            PlayerStands.assign(empty, ModStands.ECHOES_ACT_1.id());
            check(helper, !StarPlatinumActions.barrage(empty), "A different Stand used Star Platinum's barrage");
        } finally { empty.discard(); }
        for (int scenario = 0; scenario < 3; scenario++) {
            try (var f = fixture(helper)) {
                if (scenario == 0) f.owner.setGameMode(GameType.SPECTATOR);
                if (scenario == 1) f.owner.setHealth(0);
                if (scenario == 2) f.owner.snapTo(f.owner.position().add(10, 0, 0), 0, 0);
                check(helper, !StarPlatinumActions.barrage(f.owner) && !StarPlatinumActions.toggleGuard(f.owner),
                        "An ineligible or separated owner started an action");
            }
        }
        var f = fixture(helper);
        f.target.snapTo(f.owner.position().add(0, 0, 4.5), 0, 0);
        helper.onEachTick(() -> tick(f));
        check(helper, StarPlatinumActions.barrage(f.owner), "Range fixture did not start barrage");
        helper.runAfterDelay(33, () -> {
            check(helper, f.target.getHealth() == 20, "Barrage exceeded its three-block attack reach");
            f.close(); helper.succeed();
        });
    }

    @SuppressWarnings("deprecation")
    private static void desertPyramidLoot(GameTestHelper helper) {
        var params = new LootParams.Builder(helper.getLevel()).withParameter(LootContextParams.ORIGIN,
                helper.absoluteVec(Vec3.ZERO)).create(LootContextParamSets.CHEST);
        var tables = helper.getLevel().getServer().reloadableRegistries();
        var pyramid = tables.getLootTable(BuiltInLootTables.DESERT_PYRAMID);
        var quantities = new HashSet<Integer>();
        for (int seed = 1; seed <= 256; seed++) {
            var vanilla = new ArrayList<ItemStack>();
            pyramid.getRandomItemsRaw(new LootContext.Builder(params).withOptionalRandomSource(RandomSource.create(seed))
                    .create(Optional.empty()), LootTable.createStackSplitter(helper.getLevel(), vanilla::add));
            var modified = pyramid.getRandomItems(params, RandomSource.create(seed));
            var vanillaOnly = modified.stream().filter(s -> !s.is(ModItems.STARDUST_ARROW.get())).toList();
            check(helper, vanilla.size() == vanillaOnly.size(), "Stardust modifier changed vanilla loot count");
            for (int i = 0; i < vanilla.size(); i++) check(helper, ItemStack.matches(vanilla.get(i), vanillaOnly.get(i)),
                    "Stardust modifier changed vanilla loot components or quantities");
            int count = modified.stream().filter(s -> s.is(ModItems.STARDUST_ARROW.get())).mapToInt(ItemStack::getCount).sum();
            check(helper, count <= 1, "A chest produced more than one Stardust arrow");
            quantities.add(count);
            for (var excluded : List.of(BuiltInLootTables.SIMPLE_DUNGEON, BuiltInLootTables.ANCIENT_CITY)) {
                check(helper, tables.getLootTable(excluded).getRandomItems(params, RandomSource.create(seed)).stream()
                        .noneMatch(s -> s.is(ModItems.STARDUST_ARROW.get())), "Stardust arrows leaked into an excluded chest");
            }
        }
        check(helper, quantities.equals(new HashSet<>(List.of(0, 1))), "Controlled seeds did not exercise both arrow outcomes");
        var table = tables.getLootTable(ResourceKey.create(Registries.LOOT_TABLE,
                Identifier.fromNamespaceAndPath("jojosmod", "chests/stardust_arrow")));
        for (float roll : new float[] {0, .19999F, .2F, .9999F}) {
            var random = new LegacyRandomSource(1) { @Override public float nextFloat() { return roll; } };
            check(helper, !table.getRandomItems(params, random).isEmpty() == (roll < .2F), "Stardust loot did not respect the exact 20% threshold");
        }
        helper.succeed();
    }

    private static void discCraftingAndStorage(GameTestHelper helper) {
        var f = fixture(helper);
        helper.setBlock(1, 1, 5, Blocks.CRAFTING_TABLE);
        var menu = new CraftingMenu(1, f.owner.getInventory(), ContainerLevelAccess.create(helper.getLevel(),
                helper.absolutePos(new BlockPos(1, 1, 5))));
        try {
            menu.getInputGridSlots().get(0).set(new ItemStack(ModItems.EMPTY_STAND_DISC.get()));
            menu.getInputGridSlots().get(8).set(new ItemStack(ModItems.STAR_PLATINUM_CORE.get()));
            check(helper, ItemStack.matches(menu.getResultSlot().getItem(), StandDiscItem.containing(ModStands.STAR_PLATINUM)),
                    "Star Platinum's loaded core recipe lost its identity");
            menu.clicked(0, 0, ContainerInput.PICKUP, f.owner);
            check(helper, menu.getCarried().is(ModItems.STAND_DISC.get())
                    && menu.getInputGridSlots().stream().allMatch(s -> s.getItem().isEmpty()), "Core crafting did not consume both ingredients");
        } finally { menu.removed(f.owner); }
        var keeper = new DiscKeeperEntity(ModEntities.DISC_KEEPER.get(), helper.getLevel());
        keeper.snapTo(f.owner.position().add(2, 0, 0), 0, 0);
        helper.getLevel().addFreshEntity(keeper);
        f.owner.setShiftKeyDown(true);
        f.owner.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(ModItems.EMPTY_STAND_DISC.get()));
        check(helper, StarPlatinumActions.toggleGuard(f.owner), "Storage fixture did not guard");
        check(helper, f.owner.interactOn(keeper, InteractionHand.OFF_HAND, Vec3.ZERO).consumesAction()
                && StandController.getActive(f.owner).isEmpty() && !StarPlatinumActions.isBusy(f.owner)
                && Math.abs(f.owner.getAttributeValue(Attributes.MOVEMENT_SPEED) - .1) < .0001,
                "Storage left an active action or movement penalty");
        check(helper, ModItems.STAND_DISC.get().use(f.owner.level(), f.owner, InteractionHand.OFF_HAND).consumesAction()
                && f.owner.getOffhandItem().isEmpty() && PlayerWillpower.get(f.owner) == 3,
                "Stored Star Platinum could not be recovered without repeating the milestone reward");
        helper.runAfterDelay(6, () -> {
            check(helper, f.stand.isRemoved() && StandController.toggle(f.owner), "Storage left a stale manifestation");
            var fresh = StandController.getActive(f.owner).orElseThrow();
            check(helper, fresh != f.stand && fresh.getType() == ModEntities.STAR_PLATINUM.get(), "Recovery spawned the wrong Stand model");
            keeper.discard(); f.close(); helper.succeed();
        });
    }

    private static void check(GameTestHelper helper, boolean condition, String message) {
        helper.assertTrue(condition, Component.literal(message));
    }

    private static void voiceCuesAndCancellation(GameTestHelper helper) {
        var first = fixture(helper, 1.5);
        var second = fixture(helper, 5.5);
        // Tracking/self payloads require owners registered in the real chunk map.
        helper.getLevel().addNewPlayer(first.owner);
        helper.getLevel().addNewPlayer(second.owner);
        for (var cue : Cue.values()) {
            var sent = new StandVoicePayload(first.owner.getUUID(), cue, new Vec3(1.25, 3.5, -2));
            var buffer = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
            try {
                StandVoicePayload.STREAM_CODEC.encode(buffer, sent);
                check(helper, sent.equals(StandVoicePayload.STREAM_CODEC.decode(buffer))
                        && buffer.readableBytes() == 0, "Voice payload lost owner, cue or position");
            } finally { buffer.release(); }
        }
        first.owner.voices.clear();
        second.owner.voices.clear();
        check(helper, StandController.attack(first.owner), "Precise strike did not start");
        check(helper, first.owner.ownVoices().size() == 1
                && first.owner.ownVoices().getFirst().cue() == Cue.STRIKE,
                "Z did not emit exactly one owner-specific ORA cue");
        check(helper, !StandController.attack(first.owner) && !StarPlatinumActions.barrage(first.owner)
                && first.owner.ownVoices().size() == 1, "Rejected actions emitted an extra voice");
        check(helper, StarPlatinumActions.barrage(second.owner)
                && second.owner.ownVoices().size() == 1 && second.owner.ownVoices().getFirst().cue() == Cue.BARRAGE,
                "X reused the single ORA cue");
        StarPlatinumActions.stop(first.owner);
        check(helper, first.owner.ownVoices().getLast().cue() == Cue.STOP
                && second.owner.ownVoices().size() == 1, "Canceling Z affected another owner's voice");
        StarPlatinumActions.stop(second.owner);
        PlayerWillpower.set(second.owner, 5);
        check(helper, StarPlatinumActions.starFinger(second.owner)
                && second.owner.ownVoices().getLast().cue() == Cue.STAR_FINGER
                && second.owner.ownVoices().stream().noneMatch(p -> p.cue() == Cue.STRIKE),
                "V reused the single ORA cue");
        StarPlatinumActions.stop(second.owner);
        helper.runAfterDelay(6, () -> {
            tick(first); tick(second);
            check(helper, first.target.getHealth() == 20 && second.target.getHealth() == 20,
                    "An interrupted voice action still dealt damage");
            check(helper, first.owner.ownVoices().stream().filter(p -> p.cue() == Cue.STRIKE).count() == 1,
                    "A delayed action tick replayed the single ORA");
            first.close(); second.close(); helper.succeed();
        });
    }

    private record Fixture(TestPlayer owner, TrainingStandEntity stand, Villager target) implements AutoCloseable {
        @Override public void close() {
            StandController.getActive(owner).ifPresent(TrainingStandEntity::discard);
            StarPlatinumActions.stop(owner);
            stand.discard(); owner.discard(); target.discard();
        }
    }

    private static final class TestPlayer extends ServerPlayer {
        private final List<StandVoicePayload> voices = new ArrayList<>();
        private List<StandVoicePayload> ownVoices() {
            return voices.stream().filter(p -> p.owner().equals(getUUID())).toList();
        }
        private TestPlayer(ServerLevel level) {
            super(level.getServer(), level, new GameProfile(UUID.randomUUID(),
                    "Star" + UUID.randomUUID().toString().substring(0, 8)), ClientInformation.createDefault());
            // Use real ServerPlayer combat/PvP behavior; only the absent network transport is stubbed.
            var transport = new net.minecraft.network.Connection(PacketFlow.SERVERBOUND) {
                @Override public void setListenerForServerboundHandshake(PacketListener listener) {}
            };
            new EmbeddedChannel(transport);
            connection = new ServerGamePacketListenerImpl(level.getServer(), transport,
                    this, CommonListenerCookie.createInitial(getGameProfile(), false)) {
                @Override public void send(Packet<?> packet) {
                    if (packet instanceof ClientboundCustomPayloadPacket custom
                            && custom.payload() instanceof StandVoicePayload voice) voices.add(voice);
                }
                @Override public void tick() {}
            };
            connection.markClientLoaded();
            setGameMode(GameType.SURVIVAL);
            setNoGravity(true);
        }
        private void readAttachments(ValueInput input) { deserializeAttachments(input); }
    }

    private static final class StandTestInstance extends GameTestInstance {
        private static final MapCodec<StandTestInstance> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                TestData.CODEC.fieldOf("data").forGetter(StandTestInstance::info),
                Codec.STRING.fieldOf("case").forGetter(t -> t.name)).apply(instance, StandTestInstance::new));
        private final String name;
        private StandTestInstance(TestData<Holder<TestEnvironmentDefinition<?>>> data, String name) { super(data); this.name = name; }
        @Override public void run(GameTestHelper helper) {
            switch (name) {
                case "awakening_milestone" -> awakeningMilestone(helper);
                case "precise_strike_timing" -> preciseStrikeTiming(helper);
                case "barrage_and_cooldown" -> barrageAndCooldown(helper);
                case "damage_interrupts_barrage" -> damageInterruptsBarrage(helper);
                case "guard_direction_and_expiry" -> guardDirectionAndExpiry(helper);
                case "pvp_and_teams" -> pvpAndTeams(helper);
                case "walls_stop_barrage" -> wallsStopBarrage(helper);
                case "invalid_owners_and_range" -> invalidOwnersAndRange(helper);
                case "desert_pyramid_loot" -> desertPyramidLoot(helper);
                case "disc_crafting_and_storage" -> discCraftingAndStorage(helper);
                case "voice_cues_and_cancellation" -> voiceCuesAndCancellation(helper);
                default -> throw new IllegalArgumentException(name);
            }
        }
        @Override public MapCodec<? extends GameTestInstance> codec() { return CODEC; }
        @Override protected MutableComponent typeDescription() { return Component.literal(name); }
    }
}
