package io.github.bg7427941.jojosmod.gametest;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.bg7427941.jojosmod.abilities.*;
import io.github.bg7427941.jojosmod.entities.*;
import io.github.bg7427941.jojosmod.items.StandDiscItem;
import io.github.bg7427941.jojosmod.registry.*;
import io.github.bg7427941.jojosmod.stands.*;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.*;
import net.minecraft.network.protocol.game.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.*;
import net.minecraft.server.network.*;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.zombie.Husk;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/** Native damage, flight, crafting, packet gating and persistence; fake owners send no invented targets. */
public final class CrusaderGameTests {
    private static final String MOD_ID = PhaseOneGameTests.MOD_ID;
    private static final List<String> CASES = List.of("silver_chariot", "magicians_red", "hermit_purple", "the_fool", "the_world",
            "rapier_flurry", "red_bind_cleanup", "vine_anchor_and_collision", "sand_shell_and_glide", "muda_and_guard",
            "armor_tradeoff", "divination_cooldown", "flame_lifetime_and_cancel", "interruption_and_switch",
            "mastery_coop_and_absorption", "persistence_network_legacy", "five_recipes_and_storage", "allies_and_pvp",
            "time_stop_lifecycle_and_packets", "muda_barrage", "flame_guard", "red_bind_expiry", "time_scene_codec", "hermit_arm_follow");
    private static final DeferredRegister<MapCodec<? extends GameTestInstance>> TYPES = DeferredRegister.create(Registries.TEST_INSTANCE_TYPE, MOD_ID);
    static { TYPES.register("crusader", () -> Test.CODEC); }
    public static void register(IEventBus bus) { TYPES.register(bus); bus.addListener(CrusaderGameTests::registerTests); }
    private static Identifier id(String path) { return Identifier.fromNamespaceAndPath(MOD_ID, path); }
    private static void registerTests(RegisterGameTestsEvent event) {
        var env = event.registerEnvironment(id("crusader"), new TestEnvironmentDefinition.SetDifficulty(net.minecraft.world.Difficulty.NORMAL));
        var timeEnv = event.registerEnvironment(id("crusader_time"), new TestEnvironmentDefinition.SetDifficulty(net.minecraft.world.Difficulty.NORMAL));
        for (var name : CASES) event.registerTest(id("crusader/" + name), new Test(new TestData<>(name.startsWith("time_stop") ? timeEnv : env, Level.OVERWORLD,
                id("emerald_arena"), 200, 0, true, Rotation.NONE, false, 1, 1, false, 0), name));
    }
    private static void check(GameTestHelper h, boolean value, String why) { h.assertTrue(value, Component.literal(why)); }
    private static Fixture fixture(GameTestHelper h, PartThreeStand kind) { return fixture(h, kind, new Vec3(6.5, 1, 2.5)); }
    private static Fixture fixture(GameTestHelper h, PartThreeStand kind, Vec3 pos) {
        var p = new TestPlayer(h.getLevel()); p.snapTo(h.absoluteVec(pos), 0, 0);
        PlayerStands.assign(p, kind.id());
        check(h, StandController.toggle(p), "Cannot summon " + kind);
        var stand = StandController.getActive(p).orElseThrow();
        check(h, stand.getType() == ModEntities.crusader(kind) && stand.isCrusader(), "Placeholder manifestation for " + kind);
        return new Fixture(p, stand);
    }
    private record Fixture(TestPlayer owner, TrainingStandEntity stand) implements AutoCloseable {
        @Override public void close() { StandController.dismiss(owner); stand.discard(); owner.discard(); }
    }
    private static Husk enemy(GameTestHelper h, Vec3 pos) {
        var t = h.spawn(EntityTypes.HUSK, new BlockPos(1, 1, 1));
        t.setNoAi(true); t.setNoGravity(true); t.snapTo(pos, 0, 0);
        t.getAttribute(Attributes.ARMOR).setBaseValue(0); t.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
        return t;
    }
    private static void tick(Fixture f) {
        f.stand.tick(); CrusaderActions.tick(f.owner);
        for (var flame : f.owner.level().getEntitiesOfClass(CrusaderFlameEntity.class, f.owner.getBoundingBox().inflate(24), e -> e.getOwner() == f.owner)) {
            if (!flame.isRemoved()) { flame.commonTick(); flame.tick(); }
        }
    }
    private static void primary(GameTestHelper h, PartThreeStand kind) {
        var clear = fixture(h, kind); var blocked = fixture(h, kind, new Vec3(17.5, 1, 2.5));
        var t = enemy(h, clear.owner.position().add(0, 0, 2.8)); var wall = enemy(h, blocked.owner.position().add(0, 0, 2.8));
        for (int x = 13; x < 24; x++) for (int y = 1; y <= 4; y++) h.setBlock(x, y, 4, Blocks.STONE);
        h.onEachTick(() -> { tick(clear); tick(blocked); });
        check(h, StandController.attack(clear.owner) && StandController.attack(blocked.owner) && !StandController.attack(clear.owner), "No attack, or duplicate primary accepted");
        h.runAfterDelay(35, () -> {
            float expected = switch (kind) { case SILVER_CHARIOT -> 4; case MAGICIANS_RED, THE_WORLD -> 6; default -> 3; };
            check(h, Math.abs(t.getHealth() - (20 - expected)) < .01 && wall.getHealth() == 20,
                    "Primary damage/count or solid-wall collision failed: " + kind + " health=" + t.getHealth() + "/" + wall.getHealth());
            check(h, CrusaderProgress.get(clear.owner).training(kind).targets().size() == 1
                    && CrusaderProgress.get(blocked.owner).training(kind).targets().isEmpty(), "Wrong health mastery owner");
            clear.close(); blocked.close(); t.discard(); wall.discard(); h.succeed();
        });
    }
    private static void barrage(GameTestHelper h, PartThreeStand kind) {
        var f = fixture(h, kind); var t = enemy(h, f.owner.position().add(0, 0, 2.8));
        h.onEachTick(() -> tick(f));
        check(h, CrusaderActions.secondary(f.owner) && !CrusaderActions.secondary(f.owner) && !StandController.attack(f.owner), "Overlapping actions accepted");
        h.runAfterDelay(38, () -> {
            float damage = kind == PartThreeStand.SILVER_CHARIOT ? 6 : 10;
            check(h, Math.abs(t.getHealth() - (20 - damage)) < .01 && !CrusaderActions.secondary(f.owner), "Wrong barrage count or cooldown");
            check(h, CrusaderActions.session(f.owner).action == CrusaderSession.Action.NONE, "Barrage never recovered");
            f.close(); t.discard(); h.succeed();
        });
    }
    private static void bind(GameTestHelper h) {
        var f = fixture(h, PartThreeStand.MAGICIANS_RED); var t = enemy(h, f.owner.position().add(0, 0, 5));
        double speed = t.getAttributeValue(Attributes.MOVEMENT_SPEED);
        h.onEachTick(() -> tick(f)); check(h, CrusaderActions.secondary(f.owner), "Red Bind failed");
        h.runAfterDelay(10, () -> {
            check(h, t.getHealth() == 17 && t.getAttributeValue(Attributes.MOVEMENT_SPEED) < speed * .11, "Bind did not damage and restrain");
            f.owner.hurtServer(h.getLevel(), f.owner.damageSources().generic(), 1);
            check(h, Math.abs(t.getAttributeValue(Attributes.MOVEMENT_SPEED) - speed) < .00001
                    && CrusaderActions.session(f.owner).bound == null, "Interruption leaked a movement modifier");
            check(h, !CrusaderActions.secondary(f.owner), "Cancel reset bind cooldown");
            f.close(); t.discard(); h.succeed();
        });
    }
    private static void bindExpiry(GameTestHelper h) {
        var f = fixture(h, PartThreeStand.MAGICIANS_RED); var t = enemy(h, f.owner.position().add(0, 0, 5));
        double speed = t.getAttributeValue(Attributes.MOVEMENT_SPEED);
        h.onEachTick(() -> tick(f)); check(h, CrusaderActions.secondary(f.owner), "Bind did not start");
        h.runAfterDelay(48, () -> {
            check(h, Math.abs(t.getAttributeValue(Attributes.MOVEMENT_SPEED) - speed) < .00001 && CrusaderActions.session(f.owner).bound == null, "Natural expiry left restraint behind");
            f.close(); t.discard(); h.succeed();
        });
    }
    private static void flameGuard(GameTestHelper h) {
        var f = fixture(h, PartThreeStand.MAGICIANS_RED); var t = enemy(h, f.owner.position().add(0, 0, 4));
        check(h, CrusaderActions.utility(f.owner), "Flame guard did not start");
        f.owner.hurtServer(h.getLevel(), physical(h, t), 4);
        check(h, f.owner.getHealth() == 18, "Flame guard failed");
        h.onEachTick(() -> tick(f));
        h.runAfterDelay(43, () -> {
            f.owner.hurtServer(h.getLevel(), physical(h, t), 4);
            check(h, f.owner.getHealth() == 14, "Flame guard did not expire");
            f.close(); t.discard(); h.succeed();
        });
    }
    private static void grapple(GameTestHelper h) {
        var f = fixture(h, PartThreeStand.HERMIT_PURPLE);
        check(h, !CrusaderActions.secondary(f.owner), "Grapple accepted empty sky");
        h.setBlock(6, 2, 10, Blocks.STONE);
        check(h, CrusaderActions.secondary(f.owner), "Solid anchor was rejected");
        tick(f);
        check(h, f.owner.getDeltaMovement().z > .5 && f.owner.getDeltaMovement().length() <= .851, "Grapple did not apply bounded physical motion");
        var before = f.owner.position(); h.setBlock(6, 2, 6, Blocks.STONE); tick(f);
        check(h, CrusaderActions.session(f.owner).action == CrusaderSession.Action.NONE && f.owner.position().equals(before), "Grapple teleported through a wall");
        f.close(); h.succeed();
    }
    private static void sand(GameTestHelper h) {
        var f = fixture(h, PartThreeStand.THE_FOOL);
        check(h, CrusaderActions.secondary(f.owner), "Sand shell did not start");
        f.owner.hurtServer(h.getLevel(), f.owner.damageSources().mobAttack(enemy(h, f.owner.position().add(0, 0, 4))), 4);
        check(h, Math.abs(f.owner.getHealth() - 18.6) < .01, "Sand shell did not reduce incoming damage");
        CrusaderActions.cancel(f.owner);
        check(h, CrusaderActions.utility(f.owner), "Sand glide did not start");
        f.owner.setDeltaMovement(0, -1.5, .3); tick(f);
        check(h, f.owner.getDeltaMovement().y >= -.12 && f.owner.fallDistance == 0, "Glide did not bound descent");
        var fall = new net.neoforged.neoforge.event.entity.living.LivingFallEvent(f.owner, 12, 1); NeoForge.EVENT_BUS.post(fall);
        check(h, fall.isCanceled(), "Active glide allowed fall damage");
        h.onEachTick(() -> tick(f));
        h.runAfterDelay(63, () -> {
            var later = new net.neoforged.neoforge.event.entity.living.LivingFallEvent(f.owner, 12, 1); NeoForge.EVENT_BUS.post(later);
            check(h, !later.isCanceled() && CrusaderActions.session(f.owner).action == CrusaderSession.Action.NONE, "Glide protection never expired");
            f.close(); h.succeed();
        });
    }
    private static void armor(GameTestHelper h) {
        var f = fixture(h, PartThreeStand.SILVER_CHARIOT);
        check(h, CrusaderActions.utility(f.owner), "Armor release did not start");
        h.onEachTick(() -> tick(f));
        h.runAfterDelay(14, () -> {
            long now = h.getLevel().getGameTime(); check(h, StandController.attack(f.owner), "Unarmored thrust rejected");
            check(h, CrusaderActions.session(f.owner).primaryReady == now + 8, "Armor release did not accelerate thrust");
            f.owner.hurtServer(h.getLevel(), f.owner.damageSources().generic(), 4);
            check(h, f.owner.getHealth() == 15 && !CrusaderActions.utility(f.owner), "Armor drawback or retained cooldown failed");
            f.close(); h.succeed();
        });
    }
    private static net.minecraft.world.damagesource.DamageSource physical(GameTestHelper h, LivingEntity t) {
        return new net.minecraft.world.damagesource.DamageSource(h.getLevel().registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(ModDamageTypes.CRUSADER_STRIKE), t, t);
    }
    private static void guard(GameTestHelper h) {
        var f = fixture(h, PartThreeStand.THE_WORLD); var t = enemy(h, f.owner.position().add(0, 0, 4));
        check(h, CrusaderActions.utility(f.owner), "The World did not guard");
        f.owner.hurtServer(h.getLevel(), physical(h, t), 4);
        check(h, f.owner.getHealth() == 18, "Frontal guard failed");
        t.snapTo(f.owner.position().add(0, 0, -4), 0, 0); f.owner.setInvulnerableTime(0);
        f.owner.hurtServer(h.getLevel(), physical(h, t), 4);
        check(h, f.owner.getHealth() == 14, "Rear damage was incorrectly guarded");
        f.close(); t.discard(); h.succeed();
    }
    private static void divination(GameTestHelper h) {
        var f = fixture(h, PartThreeStand.HERMIT_PURPLE); var t = enemy(h, f.owner.position().add(0, 0, 8));
        check(h, CrusaderActions.utility(f.owner) && !CrusaderActions.utility(f.owner), "Divination could be spammed");
        check(h, f.owner.messages.stream().anyMatch(s -> s.contains("divination")), "Divination gave no presence/position feedback");
        tick(f); check(h, t.getHealth() == 20, "Divination caused primary damage");
        f.close(); t.discard(); h.succeed();
    }
    private static void flameBounds(GameTestHelper h) {
        try (var f = fixture(h, PartThreeStand.MAGICIANS_RED)) {
            check(h, StandController.attack(f.owner), "No flame cast");
            var flame = new CrusaderFlameEntity(ModEntities.CRUSADER_FLAME.get(), h.getLevel());
            flame.initialize(f.owner, CrusaderActions.session(f.owner).volley); flame.setDeltaMovement(1, 0, 0); h.getLevel().addFreshEntity(flame);
            var start = flame.position();
            for (int i = 0; i < 32 && !flame.isRemoved(); i++) { flame.commonTick(); flame.tick(); }
            check(h, flame.isRemoved() && flame.position().distanceTo(start) <= 16.0001, "Flame exceeded travel/lifetime");
            var cancelled = new CrusaderFlameEntity(ModEntities.CRUSADER_FLAME.get(), h.getLevel());
            cancelled.initialize(f.owner, CrusaderActions.session(f.owner).volley); h.getLevel().addFreshEntity(cancelled);
            StandController.dismiss(f.owner); cancelled.commonTick(); cancelled.tick();
            check(h, cancelled.isRemoved(), "Dismissed flame remained damaging");
        }
        h.succeed();
    }
    private static void interruptions(GameTestHelper h) {
        for (var kind : PartThreeStand.values()) try (var f = fixture(h, kind)) {
            check(h, StandController.attack(f.owner), "No interrupt fixture");
            long ready = CrusaderActions.session(f.owner).primaryReady;
            PlayerStands.assign(f.owner, ModStands.HIEROPHANT_GREEN.id());
            check(h, StandController.getActive(f.owner).isEmpty() && CrusaderActions.session(f.owner).volley == null
                    && CrusaderActions.session(f.owner).primaryReady == ready, "Stand replacement retained action or reset cooldown");
        }
        h.succeed();
    }
    private static void mastery(GameTestHelper h) {
        for (var kind : PartThreeStand.values()) try (var a = fixture(h, kind); var b = fixture(h, kind)) {
            var shield = enemy(h, a.owner.position().add(0, 0, 4));
            shield.getAttribute(Attributes.MAX_ABSORPTION).setBaseValue(30); shield.setAbsorptionAmount(30);
            CrusaderActions.damage(a.owner, shield, 1, false);
            check(h, CrusaderProgress.get(a.owner).training(kind).targets().isEmpty(), "Absorption granted mastery"); shield.discard();
            for (int i = 0; i < 3; i++) {
                var t = enemy(h, a.owner.position().add(0, 0, 4));
                CrusaderActions.damage(a.owner, t, 1, false); CrusaderActions.damage(a.owner, t, 1, false);
                CrusaderActions.damage(b.owner, t, 1, false); t.discard();
            }
            check(h, CrusaderProgress.get(a.owner).training(kind).completed() && CrusaderProgress.get(b.owner).training(kind).completed()
                    && PlayerWillpower.get(a.owner) == 4 && PlayerWillpower.get(b.owner) == 4, "Co-op hit ownership or unique rewards failed for " + kind);
            check(h, StandController.attack(a.owner) && CrusaderActions.session(a.owner).primaryReady == h.getLevel().getGameTime() + kind.cooldown() * 4 / 5, "Mastery did not improve the next cooldown");
        }
        h.succeed();
    }
    private static void persistence(GameTestHelper h) {
        var owner = new TestPlayer(h.getLevel());
        try {
            for (var kind : PartThreeStand.values()) {
                PlayerStands.assign(owner, kind.id());
                for (int i = 0; i < 3; i++) { var t = enemy(h, owner.position().add(0, 0, 3)); CrusaderProgress.hit(owner, t); t.discard(); }
            }
            var p = CrusaderProgress.get(owner);
            var json = CrusaderProgress.CODEC.codec().encodeStart(JsonOps.INSTANCE, p).getOrThrow();
            check(h, CrusaderProgress.CODEC.codec().parse(JsonOps.INSTANCE, json).getOrThrow().equals(p), "Saved progress did not roundtrip");
            check(h, CrusaderProgress.CODEC.codec().parse(JsonOps.INSTANCE, new com.google.gson.JsonObject()).getOrThrow().equals(CrusaderProgress.EMPTY), "Legacy defaults did not decode");
            var buf = Unpooled.buffer();
            try { CrusaderProgress.STREAM.encode(buf, p); check(h, CrusaderProgress.STREAM.decode(buf).equals(p) && !buf.isReadable(), "Network progress mismatch"); } finally { buf.release(); }
            var out = TagValueOutput.createWithContext(new ProblemReporter.Collector(), h.getLevel().registryAccess()); owner.serializeAttachments(out);
            var loaded = new TestPlayer(h.getLevel());
            try { loaded.readAttachments(TagValueInput.create(new ProblemReporter.Collector(), h.getLevel().registryAccess(), out.buildResult())); check(h, CrusaderProgress.get(loaded).equals(p), "Native attachments lost progress"); } finally { loaded.discard(); }
            var revived = new TestPlayer(h.getLevel());
            try { revived.copyAttachmentsFrom(owner, true); check(h, CrusaderProgress.get(revived).equals(p), "Death dropped progression"); } finally { revived.discard(); }
            int will = PlayerWillpower.get(owner); PlayerStands.clear(owner);
            for (var kind : PartThreeStand.values()) PlayerStands.assign(owner, kind.id());
            check(h, PlayerWillpower.get(owner) == will, "Switching farmed repeat awakening rewards");
        } finally { owner.discard(); }
        h.succeed();
    }
    private static void recipes(GameTestHelper h) {
        for (var kind : PartThreeStand.values()) try (var f = fixture(h, kind)) {
            h.setBlock(10, 1, 4, Blocks.CRAFTING_TABLE);
            var menu = new CraftingMenu(1, f.owner.getInventory(), ContainerLevelAccess.create(h.getLevel(), h.absolutePos(new BlockPos(10, 1, 4))));
            try {
                var core = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("jojosmod", kind.path() + "_core"));
                menu.getInputGridSlots().get(0).set(new ItemStack(ModItems.EMPTY_STAND_DISC.get())); menu.getInputGridSlots().get(8).set(new ItemStack(core));
                check(h, ItemStack.matches(menu.getResultSlot().getItem(), StandDiscItem.containing(kind.definition())), "Recipe lost " + kind);
                menu.clicked(0, 0, ContainerInput.PICKUP, f.owner);
                check(h, menu.getInputGridSlots().stream().allMatch(s -> s.getItem().isEmpty()), "Ingredients duplicated");
            } finally { menu.removed(f.owner); }
            var keeper = new DiscKeeperEntity(ModEntities.DISC_KEEPER.get(), h.getLevel()); keeper.snapTo(f.owner.position().add(2, 0, 0), 0, 0); h.getLevel().addFreshEntity(keeper);
            try {
                f.owner.setShiftKeyDown(true); f.owner.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(ModItems.EMPTY_STAND_DISC.get()));
                check(h, f.owner.interactOn(keeper, InteractionHand.OFF_HAND, Vec3.ZERO).consumesAction() && StandController.getActive(f.owner).isEmpty(), "Keeper failed to store " + kind);
                check(h, ItemStack.matches(f.owner.getOffhandItem(), StandDiscItem.containing(kind.definition())), "Keeper lost disc identity");
                check(h, ModItems.STAND_DISC.get().use(h.getLevel(), f.owner, InteractionHand.OFF_HAND).consumesAction()
                        && f.owner.getOffhandItem().isEmpty() && PlayerWillpower.get(f.owner) == 3, "Recovery duplicated an awakening");
            } finally { keeper.discard(); }
        }
        h.succeed();
    }
    private static void allies(GameTestHelper h) {
        for (var kind : PartThreeStand.values()) try (var f = fixture(h, kind)) {
            var friend = new TestPlayer(h.getLevel());
            try {
                friend.snapTo(f.owner.position().add(0, 0, 3), 180, 0); f.owner.protectedPlayers.add(friend.getUUID());
                check(h, !CrusaderActions.damage(f.owner, friend, 4, false) && friend.getHealth() == 20, "PvP protection failed");
                var ally = enemy(h, f.owner.position().add(0, 0, 3));
                var board = h.getLevel().getScoreboard(); var team = board.addPlayerTeam("roster_" + UUID.randomUUID().toString().substring(0, 8));
                try {
                    board.addPlayerToTeam(f.owner.getScoreboardName(), team); board.addPlayerToTeam(ally.getScoreboardName(), team);
                    check(h, !CrusaderActions.damage(f.owner, ally, 4, true) && ally.getHealth() == 20, "Team damage accepted");
                } finally { board.removePlayerTeam(team); ally.discard(); }
            } finally { friend.discard(); }
        }
        h.succeed();
    }
    private static void sceneCodec(GameTestHelper h) {
        var center = new Vec3(17.5, 80.25, -31.75);
        var owner = UUID.randomUUID();
        for (boolean world : new boolean[]{false, true}) for (int remaining : new int[]{0, 1, 40, 60}) {
            var payload = new io.github.bg7427941.jojosmod.network.TimeScenePayload(owner, center, world, 13, remaining);
            var buffer = new FriendlyByteBuf(Unpooled.buffer());
            try {
                io.github.bg7427941.jojosmod.network.TimeScenePayload.STREAM_CODEC.encode(buffer, payload);
                var decoded = io.github.bg7427941.jojosmod.network.TimeScenePayload.STREAM_CODEC.decode(buffer);
                check(h, decoded.equals(payload) && !buffer.isReadable(), "Time scene lost caster, palette, center or lifetime");
            } finally { buffer.release(); }
        }
        h.succeed();
    }
    private static void hermitFollow(GameTestHelper h) {
        try (var f = fixture(h, PartThreeStand.HERMIT_PURPLE)) {
            for (int i = 0; i < 6; i++) {
                f.owner.setPos(f.owner.position().add(.8, 0, .5)); f.owner.setYRot(i * 67);
                f.stand.tick();
                check(h, f.stand.distanceToSqr(f.owner) < 1E-8, "Arm vines trailed their owner's server anchor");
                check(h, f.stand.getOwnerEntityId() == f.owner.getId(), "Observers lost the vine owner identity");
            }
        }
        h.succeed();
    }
    private static void time(GameTestHelper h) {
        // A separate test environment runs this local bubble after the other combat cases.
        var sounds = new ArrayList<Identifier>();
        java.util.function.Consumer<net.neoforged.neoforge.event.PlayLevelSoundEvent.AtPosition> soundListener = event -> {
            if (event.getLevel() == h.getLevel() && event.getSound() != null) sounds.add(event.getSound().value().location());
        };
        NeoForge.EVENT_BUS.addListener(net.neoforged.neoforge.event.PlayLevelSoundEvent.AtPosition.class, soundListener);
        h.testInfo.addListener(new GameTestListener() {
            public void testStructureLoaded(GameTestInfo info) {}
            public void testPassed(GameTestInfo info, GameTestRunner runner) { NeoForge.EVENT_BUS.unregister(soundListener); }
            public void testFailed(GameTestInfo info, GameTestRunner runner) { NeoForge.EVENT_BUS.unregister(soundListener); }
            public void testAddedForRerun(GameTestInfo original, GameTestInfo copy, GameTestRunner runner) {}
        });
        var f = fixture(h, PartThreeStand.THE_WORLD);
        h.getLevel().getChunkAt(f.owner.blockPosition());
        var target = enemy(h, f.owner.position().add(0, 0, 4)); target.setDeltaMovement(.2, .1, .3);
        var friend = new TestPlayer(h.getLevel()); var victim = new TestPlayer(h.getLevel()); var star = new TestPlayer(h.getLevel());
        for (var p : List.of(friend, victim, star)) { p.snapTo(f.owner.position().add(2, 0, 2), 0, 0); h.getLevel().addNewPlayer(p); }
        f.owner.protectedPlayers.add(friend.getUUID()); PlayerStands.assign(star, ModStands.STAR_PLATINUM.id()); PlayerWillpower.set(star, 6);
        check(h, !TimeStop.start(f.owner), "Time stop ignored learning requirement"); PlayerWillpower.set(f.owner, 4);
        check(h, TimeStop.start(f.owner), "Time stop did not start");
        check(h, sounds.contains(Identifier.fromNamespaceAndPath("jojosmod", "the_world.time_stop")), "The World did not emit its time sound");
        check(h, TimeStop.isFrozen(target) && TimeStop.isFrozen(victim) && !TimeStop.isFrozen(friend) && !TimeStop.isFrozen(star), "Time bubble target/PvP/compatible rules failed");
        var pre = new EntityTickEvent.Pre(target); NeoForge.EVENT_BUS.post(pre); check(h, pre.isCanceled(), "A frozen mob still ticked");
        victim.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND));
        victim.connection.handlePlayerAction(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.DROP_ITEM, BlockPos.ZERO, Direction.DOWN, 0));
        check(h, victim.getMainHandItem().is(Items.DIAMOND), "Frozen player's gameplay packet executed");
        check(h, !TimeStop.start(f.owner) && !StandController.attack(victim), "Time stop stacking/frozen action accepted");
        var arrow = new net.minecraft.world.entity.projectile.arrow.Arrow(EntityTypes.ARROW, h.getLevel()); arrow.setPos(f.owner.position().add(0, 1, 2)); arrow.setDeltaMovement(0, 0, 1); h.getLevel().addFreshEntity(arrow);
        TimeStop.tick(h.getLevel()); check(h, TimeStop.isFrozen(arrow), "A projectile entering the bubble was not captured");
        h.runAfterDelay(63, () -> {
            TimeStop.tick(h.getLevel());
            check(h, !TimeStop.isFrozen(victim) && !victim.getData(ModAttachments.TIME_FROZEN) && !TimeStop.isFrozen(arrow), "Time did not resume");
            check(h, sounds.contains(Identifier.fromNamespaceAndPath("jojosmod", "the_world.resume")), "The World did not emit its resume sound");
            check(h, !TimeStop.start(f.owner), "Resuming reset the 30-second cooldown");
            // A fresh owner can start a bubble; dismissal must restore all movement immediately.
            var other = fixture(h, PartThreeStand.THE_WORLD, new Vec3(6.5, 1, 2.5)); PlayerWillpower.set(other.owner, 4);
            check(h, TimeStop.start(other.owner), "A fresh caster could not stop time"); StandController.dismiss(other.owner);
            check(h, !TimeStop.isFrozen(target) && !victim.getData(ModAttachments.TIME_FROZEN), "Dismissal stranded a frozen target");
            other.close();
            for (int reason = 0; reason < 5; reason++) {
                var interrupted = fixture(h, PartThreeStand.THE_WORLD); PlayerWillpower.set(interrupted.owner, 4);
                check(h, TimeStop.start(interrupted.owner), "Cancellation fixture did not start");
                switch (reason) {
                    case 0 -> interrupted.owner.setHealth(0);
                    case 1 -> PlayerStands.clear(interrupted.owner);
                    case 2 -> interrupted.owner.discard();
                    case 3 -> interrupted.owner.setPos(interrupted.owner.position().add(20, 0, 0));
                    case 4 -> interrupted.owner.setServerLevel(h.getLevel().getServer().getLevel(Level.NETHER));
                }
                TimeStop.tick(h.getLevel());
                check(h, !TimeStop.isFrozen(victim) && !victim.getData(ModAttachments.TIME_FROZEN), "Interruption stranded frozen player: " + reason);
                interrupted.close();
            }
            var advanced = new TestPlayer(h.getLevel()); advanced.snapTo(f.owner.position(), 0, 0);
            PlayerStands.assign(advanced, ModStands.STAR_PLATINUM.id()); PlayerWillpower.set(advanced, 6);
            check(h, StandController.toggle(advanced) && TimeStop.start(advanced), "Advanced Star Platinum could not stop time");
            check(h, sounds.getLast().equals(ModSounds.STAR_TIME_STOP.get().location()), "Star Platinum reused The World audio");
            h.runAfterDelay(43, () -> {
                TimeStop.tick(h.getLevel());
                check(h, !TimeStop.isFrozen(victim) && !victim.getData(ModAttachments.TIME_FROZEN), "Star Platinum time did not resume after two seconds");
                check(h, sounds.getLast().equals(ModSounds.STAR_TIME_RESUME.get().location()), "Star Platinum resume used the wrong sound");
                victim.connection.handlePlayerAction(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.DROP_ITEM, BlockPos.ZERO, Direction.DOWN, 0));
                check(h, victim.getMainHandItem().isEmpty(), "Player actions remained blocked after time resumed");
                StandController.dismiss(advanced); advanced.discard(); f.close(); target.discard(); arrow.discard(); friend.discard(); victim.discard(); star.discard(); h.succeed();
            });
        });
    }
    private static final class TestPlayer extends ServerPlayer {
        final Set<UUID> protectedPlayers = new HashSet<>(); final List<String> messages = new ArrayList<>();
        TestPlayer(ServerLevel level) {
            super(level.getServer(), level, new GameProfile(UUID.randomUUID(), "Crusader" + UUID.randomUUID().toString().substring(0, 6)), ClientInformation.createDefault());
            var transport = new net.minecraft.network.Connection(PacketFlow.SERVERBOUND) { @Override public void setListenerForServerboundHandshake(PacketListener listener) {} };
            new EmbeddedChannel(transport);
            connection = new ServerGamePacketListenerImpl(level.getServer(), transport, this, CommonListenerCookie.createInitial(getGameProfile(), false)) {
                @Override public void send(Packet<?> p) {} @Override public void tick() {}
            };
            connection.markClientLoaded(); setGameMode(GameType.SURVIVAL); setNoGravity(true);
        }
        private void readAttachments(ValueInput input) { deserializeAttachments(input); }
        @Override public boolean canHarmPlayer(Player p) { return !protectedPlayers.contains(p.getUUID()) && super.canHarmPlayer(p); }
        @Override public void sendSystemMessage(Component text, boolean overlay) { if (messages != null) messages.add(text.toString()); }
    }
    private static final class Test extends GameTestInstance {
        static final MapCodec<Test> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(TestData.CODEC.fieldOf("data").forGetter(Test::info),
                com.mojang.serialization.Codec.STRING.fieldOf("case").forGetter(t -> t.name)).apply(i, Test::new));
        final String name;
        Test(TestData<Holder<TestEnvironmentDefinition<?>>> data, String name) { super(data); this.name = name; }
        @Override public void run(GameTestHelper h) {
            switch (name) {
                case "time_scene_codec" -> sceneCodec(h); case "hermit_arm_follow" -> hermitFollow(h);
                case "rapier_flurry" -> barrage(h, PartThreeStand.SILVER_CHARIOT); case "muda_and_guard" -> { guard(h); }
                case "muda_barrage" -> barrage(h, PartThreeStand.THE_WORLD);
                case "flame_guard" -> flameGuard(h); case "red_bind_expiry" -> bindExpiry(h);
                case "red_bind_cleanup" -> bind(h); case "vine_anchor_and_collision" -> grapple(h);
                case "sand_shell_and_glide" -> sand(h); case "armor_tradeoff" -> armor(h); case "divination_cooldown" -> divination(h);
                case "flame_lifetime_and_cancel" -> flameBounds(h); case "interruption_and_switch" -> interruptions(h);
                case "mastery_coop_and_absorption" -> mastery(h); case "persistence_network_legacy" -> persistence(h);
                case "five_recipes_and_storage" -> recipes(h); case "allies_and_pvp" -> allies(h); case "time_stop_lifecycle_and_packets" -> time(h);
                default -> primary(h, PartThreeStand.valueOf(name.toUpperCase(java.util.Locale.ROOT)));
            }
        }
        @Override public MapCodec<? extends GameTestInstance> codec() { return CODEC; }
        @Override protected net.minecraft.network.chat.MutableComponent typeDescription() { return Component.literal(name); }
    }
}
