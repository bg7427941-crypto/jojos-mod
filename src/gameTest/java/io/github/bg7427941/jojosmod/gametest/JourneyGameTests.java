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


import net.minecraft.world.Difficulty;
import io.github.bg7427941.jojosmod.journey.*;
import io.github.bg7427941.jojosmod.entities.*;
import io.github.bg7427941.jojosmod.abilities.*;
import io.github.bg7427941.jojosmod.registry.ModAttachments;
import net.minecraft.world.phys.AABB;
import com.mojang.serialization.JsonOps;

/** New campaign, advanced powers and time budgets exercised on the native server. */
public final class JourneyGameTests {
 private static final String MOD_ID=PhaseOneGameTests.MOD_ID;
 private static final List<String> CASES=List.of(
 "tentacle_expiry","tentacle_interrupt","tentacle_walls_pvp","tentacle_pvp","barrier_once","barrier_walls_pvp","barrier_splash_coexist","barrier_dismiss","barrier_callback_cancel","barrier_expiry",
 "rapier_hit","knives_hit","weapon_wall_cancel_range","life_detector","hamon_bonus","sand_dome","advanced_gate","rapier_interrupt",
 "keeper_identity_save","keeper_discovery_once","keeper_reward_once","challenge_gate","challenge_retry","cooperative_reward","pending_full_inventory","journey_codec","mastery_core_once","mastery_core_full",
 "boss_projection_cleanup","boss_attack_telegraph","time_budget","time_reentry","boss_time_restore","boss_time_attack","time_star_budget","time_boss_budget");
 private static final DeferredRegister<MapCodec<? extends GameTestInstance>> TYPES=DeferredRegister.create(Registries.TEST_INSTANCE_TYPE,MOD_ID);
 static {TYPES.register("journey",()->Test.CODEC);}
 public static void register(IEventBus bus){TYPES.register(bus);bus.addListener(JourneyGameTests::registerTests);}
 private static Identifier id(String path){return Identifier.fromNamespaceAndPath(MOD_ID,path);}
 private static void registerTests(RegisterGameTestsEvent event){
  var env=event.registerEnvironment(id("journey"),new TestEnvironmentDefinition.SetDifficulty(Difficulty.NORMAL));
  for(var name:CASES){
   var chosen=name.contains("time")?event.registerEnvironment(id("journey_"+name),new TestEnvironmentDefinition.SetDifficulty(Difficulty.NORMAL)):env;
   event.registerTest(id("journey/"+name),new Test(new TestData<>(chosen,Level.OVERWORLD,id("emerald_arena"),name.equals("boss_time_attack")?300:230,0,true,Rotation.NONE,false,1,1,false,0),name));
  }
 }
 private static void check(GameTestHelper h,boolean value,String why){h.assertTrue(value,Component.literal(why));}
 private record Fixture(TestPlayer owner,TrainingStandEntity stand) implements AutoCloseable {
  public void close(){StandController.dismiss(owner);stand.discard();owner.discard();}
 }
 private static Fixture fixture(GameTestHelper h,Identifier stand){
  var p=new TestPlayer(h.getLevel());p.snapTo(h.absoluteVec(new Vec3(8.5,1,2.5)),0,0);PlayerStands.assign(p,stand);PlayerWillpower.set(p,6);
  check(h,StandController.toggle(p),"No fixture manifestation");return new Fixture(p,StandController.getActive(p).orElseThrow());
 }
 private static Husk enemy(GameTestHelper h,Vec3 pos){var t=h.spawn(EntityTypes.HUSK,new BlockPos(1,1,1));t.snapTo(pos,0,0);t.setNoAi(true);t.setNoGravity(true);t.getAttribute(Attributes.ARMOR).setBaseValue(0);t.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);t.setHealth(100);t.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);return t;}
 private static void tick(Fixture f){
  f.stand.tick();HierophantTools.tick(f.owner);HierophantSplash.tick(f.owner);CrusaderActions.tick(f.owner);
  for(var weapon:f.owner.level().getEntitiesOfClass(StandWeaponEntity.class,f.owner.getBoundingBox().inflate(24),e->e.getOwner()==f.owner))if(!weapon.isRemoved()){weapon.commonTick();weapon.tick();}
 }
 private static void tentacle(GameTestHelper h,String name){
  var f=fixture(h,ModStands.HIEROPHANT_GREEN.id());var target=enemy(h,f.owner.position().add(0,0,5));
  TestPlayer protectedPlayer=new TestPlayer(h.getLevel());protectedPlayer.snapTo(f.owner.position().add(0,0,5),0,0);f.owner.protectedPlayers.add(protectedPlayer.getUUID());
  if(name.equals("tentacle_pvp")){target.discard();h.getLevel().addNewPlayer(protectedPlayer);}
  double speed=target.getAttributeValue(Attributes.MOVEMENT_SPEED);
  if(name.equals("tentacle_walls_pvp"))for(int y=1;y<5;y++)for(int x=6;x<12;x++)h.setBlock(x,y,5,Blocks.STONE);
  check(h,HierophantTools.tentacle(f.owner)&&!HierophantTools.tentacle(f.owner),"Tentacle accepted twice");
  check(h,!StandController.attack(f.owner),"Splash overlapped tentacle");h.onEachTick(()->tick(f));
  h.runAfterDelay(9,()->{
   if(name.equals("tentacle_pvp")){check(h,protectedPlayer.getHealth()==20&&HierophantTools.state(f.owner).bound==null,"Tentacle restrained a PvP-protected player");protectedPlayer.discard();f.close();h.succeed();return;}
   if(name.equals("tentacle_walls_pvp")){check(h,target.getHealth()==100,"Tentacle crossed wall");f.close();target.discard();h.succeed();return;}
   check(h,target.getHealth()==96&&target.getAttributeValue(Attributes.MOVEMENT_SPEED)<speed,"Tentacle contact/slow failed");
   if(name.equals("tentacle_interrupt")){long ready=HierophantTools.state(f.owner).tentacleReady;StandController.dismiss(f.owner);check(h,target.getAttributeValue(Attributes.MOVEMENT_SPEED)==speed,"Tentacle slow leaked on dismiss");check(h,HierophantTools.state(f.owner).tentacleReady==ready,"Interrupt reset cooldown");f.close();target.discard();h.succeed();}
  });
  if(name.equals("tentacle_expiry"))h.runAfterDelay(44,()->{check(h,target.getAttributeValue(Attributes.MOVEMENT_SPEED)==speed,"Natural expiry leaked restraint");check(h,HierophantTools.state(f.owner).action==HierophantTools.Action.NONE,"Tentacle remained busy");f.close();target.discard();h.succeed();});
 }
 private static void barrier(GameTestHelper h,String name){
  var f=fixture(h,ModStands.HIEROPHANT_GREEN.id());check(h,HierophantTools.barrier(f.owner),"Barrier failed");var center=HierophantTools.state(f.owner).center;
  var target=enemy(h,center.add(0,-.9,0));
  if(name.equals("barrier_walls_pvp")){for(int x=5;x<13;x++)for(int y=1;y<5;y++)h.setBlock(x,y,6,Blocks.STONE);target.snapTo(center.add(0,-.9,1),0,0);}
  java.util.function.Consumer<net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Post> callback=e->{if(e.getEntity()==target&&name.equals("barrier_callback_cancel"))StandController.dismiss(f.owner);};
  if(name.equals("barrier_callback_cancel"))NeoForge.EVENT_BUS.addListener(net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Post.class,callback);
  h.onEachTick(()->tick(f));
  h.runAfterDelay(10,()->{check(h,target.getHealth()==(name.equals("barrier_walls_pvp")?100:94),"Barrier damage/wall filtering wrong");});
  h.runAfterDelay(24,()->{
   check(h,target.getHealth()==(name.equals("barrier_walls_pvp")?100:94),"Barrier hit one victim repeatedly");
   if(name.equals("barrier_callback_cancel")){NeoForge.EVENT_BUS.unregister(callback);check(h,HierophantTools.state(f.owner).center==null,"Damage callback failed to cancel barrier");}
   if(name.equals("barrier_expiry"))return;
   if(name.equals("barrier_splash_coexist")){check(h,StandController.attack(f.owner)&&HierophantTools.state(f.owner).center!=null,"Splash removed prepared barrier");}
   if(name.equals("barrier_dismiss")){long ready=HierophantTools.state(f.owner).barrierReady;StandController.dismiss(f.owner);check(h,HierophantTools.state(f.owner).center==null&&HierophantTools.state(f.owner).barrierReady==ready,"Dismiss failed barrier cleanup/cooldown");}
   f.close();target.discard();h.succeed();
  });
  if(name.equals("barrier_expiry"))h.runAfterDelay(203,()->{check(h,HierophantTools.state(f.owner).center==null&&!HierophantTools.barrier(f.owner),"Barrier lifetime/cooldown not bounded");f.close();target.discard();h.succeed();});
 }
 private static void advanced(GameTestHelper h,String name){
  var kind=switch(name){case "rapier_hit","weapon_wall_cancel_range","rapier_interrupt"->PartThreeStand.SILVER_CHARIOT;case "knives_hit","advanced_gate"->PartThreeStand.THE_WORLD;case "life_detector"->PartThreeStand.MAGICIANS_RED;case "hamon_bonus"->PartThreeStand.HERMIT_PURPLE;default->PartThreeStand.THE_FOOL;};
  var f=fixture(h,kind.id());var target=enemy(h,f.owner.position().add(0,0,name.equals("knives_hit")?2.1:5));
  if(name.equals("advanced_gate")){PlayerWillpower.set(f.owner,3);check(h,!CrusaderAdvanced.start(f.owner),"Advanced ignored will requirement");PlayerWillpower.set(f.owner,4);}
  if(name.equals("weapon_wall_cancel_range"))for(int x=5;x<13;x++)for(int y=1;y<5;y++)h.setBlock(x,y,5,Blocks.STONE);
  if(name.equals("sand_dome"))h.getLevel().addNewPlayer(f.owner);
  check(h,CrusaderAdvanced.start(f.owner)&&!CrusaderAdvanced.start(f.owner)&&!CrusaderActions.primary(f.owner),"Advanced overlap accepted");
  h.onEachTick(()->tick(f));
  h.runAfterDelay(20,()->{
   switch(name){
    case "rapier_interrupt"->{long ready=CrusaderActions.session(f.owner).advancedReady;CrusaderActions.cancel(f.owner);check(h,CrusaderActions.session(f.owner).rapierUntil==0&&CrusaderActions.session(f.owner).advancedReady==ready,"Rapier interrupt retained transient lock or reset cooldown");PlayerStands.assign(f.owner,PartThreeStand.MAGICIANS_RED.id());StandController.toggle(f.owner);check(h,CrusaderActions.primary(f.owner),"Rapier lock leaked to another Stand");}
    case "rapier_hit"->check(h,target.getHealth()==91,"Rapier did not hit for 9");
    case "knives_hit"->check(h,target.getHealth()<=94&&target.getHealth()>=85,"Knife volley damage/physical fan wrong: "+target.getHealth());
    case "weapon_wall_cancel_range"->{
     check(h,target.getHealth()==100,"Rapier crossed a wall");
     var weapon=new StandWeaponEntity(ModEntities.STAND_WEAPON.get(),h.getLevel());weapon.initialize(f.owner,CrusaderActions.session(f.owner).volley,true,f.owner.getEyePosition(),new Vec3(1,0,0));h.getLevel().addFreshEntity(weapon);var start=weapon.position();
     for(int i=0;i<30&&!weapon.isRemoved();i++){weapon.commonTick();weapon.tick();}
     check(h,weapon.isRemoved()&&weapon.position().distanceTo(start)<=12.0001,"Rapier exceeded bounds");
     var cancelled=new StandWeaponEntity(ModEntities.STAND_WEAPON.get(),h.getLevel());cancelled.initialize(f.owner,CrusaderActions.session(f.owner).volley,true,f.owner.getEyePosition(),new Vec3(1,0,0));h.getLevel().addFreshEntity(cancelled);StandController.dismiss(f.owner);cancelled.tick();check(h,cancelled.isRemoved(),"Dismissed weapon survived");
    }
    case "life_detector"->check(h,target.hasEffect(net.minecraft.world.effect.MobEffects.GLOWING)&&target.getHealth()==100,"Life detection damaged or failed to reveal");
    case "hamon_bonus"->check(h,target.getHealth()==91,"Hamon did not use undead bonus");
    case "sand_dome"->{f.owner.setHealth(20);f.owner.hurtServer(h.getLevel(),h.getLevel().damageSources().mobAttack(target),10);check(h,Math.abs(f.owner.getHealth()-14)<.001,"Dome reduction failed");}
    default->{}
   }
   f.close();target.discard();h.succeed();
  });
 }
 private static DiscKeeperEntity keeper(GameTestHelper h){var k=new DiscKeeperEntity(ModEntities.DISC_KEEPER.get(),h.getLevel());k.snapTo(h.absoluteVec(new Vec3(10.5,1,2.5)),0,0);k.setTemple(h.absolutePos(new BlockPos(10,0,10)));h.getLevel().addFreshEntity(k);return k;}
 private static void mastery(TestPlayer p){for(int i=0;i<3;i++){var t=EntityTypes.HUSK.create(p.level(),EntitySpawnReason.COMMAND);t.setNoAi(true);t.snapTo(p.position().add(0,0,4),0,0);p.level().addFreshEntity(t);CrusaderProgress.hit(p,t);t.discard();}}
 private static void campaign(GameTestHelper h,String name){
  var f=fixture(h,name.startsWith("mastery_core")?PartThreeStand.SILVER_CHARIOT.id():PartThreeStand.THE_WORLD.id());var keeper=keeper(h);var before=PlayerWillpower.get(f.owner);
  switch(name){
   case "mastery_core_once","mastery_core_full"->{
    mastery(f.owner);if(name.endsWith("full"))for(int slot=0;slot<36;slot++)f.owner.getInventory().setItem(slot,new ItemStack(Items.STONE,64));
    StardustJourney.meet(f.owner,keeper);
    if(name.endsWith("full")){check(h,StardustJourney.progress(f.owner).masteryClaims().isEmpty(),"Full inventory consumed mastery claim");f.owner.getInventory().setItem(0,ItemStack.EMPTY);check(h,StardustJourney.claim(f.owner),"Mastery reward retry failed");}
    check(h,StardustJourney.progress(f.owner).masteryClaims().equals(List.of("silver_chariot"))&&!StardustJourney.claim(f.owner),"Mastery core missing or duplicated");
   }
   case "keeper_discovery_once"->{int messages=f.owner.messages.size();StardustJourney.notifyKeeper(f.owner,keeper);StardustJourney.notifyKeeper(f.owner,keeper);check(h,f.owner.messages.size()==messages+1&&!StardustJourney.progress(f.owner).metKeeper()&&StardustJourney.progress(f.owner).keeper().isPresent()&&PlayerWillpower.get(f.owner)==before,"Discovery repeated its notice or granted meeting reward early");}
   case "keeper_reward_once"->{StardustJourney.meet(f.owner,keeper);StardustJourney.meet(f.owner,keeper);check(h,PlayerWillpower.get(f.owner)==before+1&&StardustJourney.progress(f.owner).metKeeper(),"Keeper reward duplicated/missing");}
   case "keeper_identity_save"->{
    var data=JourneyWorldData.get(h.getLevel());var shrine=new JourneyWorldData.Shrine(keeper.temple().orElseThrow(),keeper.blockPosition(),keeper.getUUID(),42);data.replace(shrine);
    check(h,StardustJourney.ensureKeeper(h.getLevel(),shrine.temple())==keeper,"Existing keeper was duplicated");
    var encoded=JourneyWorldData.CODEC.encodeStart(JsonOps.INSTANCE,data).getOrThrow();var copy=JourneyWorldData.CODEC.parse(JsonOps.INSTANCE,encoded).getOrThrow();check(h,copy.shrine(shrine.temple()).keeper().equals(keeper.getUUID()),"Shrine identity failed save/load");
   }
   case "challenge_gate"->{check(h,!StardustJourney.challenge(f.owner,keeper),"Untrained player challenged DIO");check(h,h.getLevel().getEntitiesOfClass(DioEntity.class,keeper.getBoundingBox().inflate(48),DioEntity::isAlive).isEmpty(),"Failed challenge spawned DIO");}
   case "challenge_retry"->{
    mastery(f.owner);h.getLevel().getServer().getCommands().performPrefixedCommand(h.getLevel().getServer().createCommandSourceStack().withSuppressedOutput(),"time set midnight");
    var safe=keeper.blockPosition().offset(12,0,0);h.getLevel().setBlockAndUpdate(safe.below(),Blocks.STONE.defaultBlockState());
    var data=JourneyWorldData.get(h.getLevel());data.replace(new JourneyWorldData.Shrine(keeper.temple().orElseThrow(),keeper.blockPosition(),keeper.getUUID(),0));
    check(h,StardustJourney.challenge(f.owner,keeper),"Eligible night challenge could not start");check(h,!StardustJourney.challenge(f.owner,keeper),"Repeated challenge duplicated boss");
    var bosses=h.getLevel().getEntitiesOfClass(DioEntity.class,keeper.getBoundingBox().inflate(48),DioEntity::isAlive);check(h,bosses.size()==1,"Wrong boss count");bosses.forEach(Entity::discard);
   }
   case "cooperative_reward","pending_full_inventory"->{
    var friend=new TestPlayer(h.getLevel());friend.snapTo(f.owner.position().add(1,0,0),0,0);
    if(name.equals("pending_full_inventory"))for(int i=0;i<36;i++)f.owner.getInventory().setItem(i,new ItemStack(Items.STONE,64));
    var dio=new DioEntity(ModEntities.DIO.get(),h.getLevel());dio.snapTo(f.owner.position().add(0,0,4),0,0);dio.prepare(dio.blockPosition(),2);h.getLevel().addFreshEntity(dio);
    dio.hurtServer(h.getLevel(),h.getLevel().damageSources().playerAttack(f.owner),10);dio.setInvulnerableTime(0);dio.hurtServer(h.getLevel(),h.getLevel().damageSources().playerAttack(friend),1000);
    StardustJourney.applyPending(f.owner);StardustJourney.applyPending(friend);
    check(h,StardustJourney.progress(f.owner).defeatedDio()&&StardustJourney.progress(friend).defeatedDio(),"Only last hit received victory");
    int will=PlayerWillpower.get(f.owner);StardustJourney.applyPending(f.owner);check(h,PlayerWillpower.get(f.owner)==will,"Victory paid twice");
    if(name.equals("pending_full_inventory")){check(h,!StardustJourney.progress(f.owner).claimedCore(),"Full inventory silently lost core");f.owner.getInventory().setItem(0,ItemStack.EMPTY);check(h,StardustJourney.claim(f.owner),"Could not claim pending reward");}
    check(h,StardustJourney.progress(f.owner).claimedCore()&&!StardustJourney.claim(f.owner),"Core duplicated or missing");friend.discard();dio.discard();
   }
   case "journey_codec"->{
    var legacy=JourneyProgress.CODEC.codec().parse(JsonOps.INSTANCE,new com.google.gson.JsonObject()).getOrThrow();check(h,legacy.equals(JourneyProgress.EMPTY),"Legacy journey data not empty");
    var value=new JourneyProgress(true,true,false,java.util.Optional.of(keeper.blockPosition()),List.of("star_platinum","hermit_purple"));var codec=JourneyProgress.CODEC.codec();check(h,codec.parse(JsonOps.INSTANCE,codec.encodeStart(JsonOps.INSTANCE,value).getOrThrow()).getOrThrow().equals(value),"Journey codec lost reward/location");
    var buffer=new net.minecraft.network.RegistryFriendlyByteBuf(Unpooled.buffer(),h.getLevel().registryAccess());try{JourneyProgress.STREAM_CODEC.encode(buffer,value);check(h,JourneyProgress.STREAM_CODEC.decode(buffer).equals(value),"Private journey sync lost state");}finally{buffer.release();}
   }
  }
  f.close();keeper.discard();h.succeed();
 }
 private static void boss(GameTestHelper h,String name){
  var f=fixture(h,name.equals("time_star_budget")?ModStands.STAR_PLATINUM.id():PartThreeStand.THE_WORLD.id());h.getLevel().addNewPlayer(f.owner);var dio=new DioEntity(ModEntities.DIO.get(),h.getLevel());dio.snapTo(f.owner.position().add(0,0,2.5),180,0);dio.prepare(dio.blockPosition(),1);h.getLevel().addFreshEntity(dio);
  check(h,dio.getType().builtInRegistryHolder().is(net.minecraft.tags.EntityTypeTags.UNDEAD)&&dio.getType().builtInRegistryHolder().is(net.minecraft.tags.EntityTypeTags.BURN_IN_DAYLIGHT),"DIO lacks vampire sunlight/Hamon traits");
  dio.tick();
  if(name.equals("boss_time_attack")) {
   dio.setHealth(60);PlayerWillpower.set(f.owner,3);f.owner.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);f.owner.setHealth(100);
   long[] stopped={-1};
   h.onEachTick(()->{
    dio.tick();long now=h.getLevel().getGameTime();
    if(stopped[0]<0){f.owner.setHealth(100);if(TimeStop.isFrozen(f.owner))stopped[0]=now;}
    else if(now-stopped[0]>=26){check(h,TimeStop.isFrozen(f.owner)&&f.owner.getHealth()<100,"DIO never followed up its time stop with a real attack");dio.discard();f.close();h.succeed();}
   });return;
  }
  if(name.equals("boss_projection_cleanup")){check(h,h.getLevel().getEntitiesOfClass(TrainingStandEntity.class,dio.getBoundingBox().inflate(4),s->s.getOwnerEntityId()==dio.getId()).size()==1,"DIO lacks The World projection");dio.discard();check(h,h.getLevel().getEntitiesOfClass(TrainingStandEntity.class,f.owner.getBoundingBox().inflate(8),s->s.getOwnerEntityId()==dio.getId()).isEmpty(),"Projection leaked after boss removal");f.close();h.succeed();return;}
  if(name.equals("boss_attack_telegraph")){h.onEachTick(dio::tick);h.runAfterDelay(41,()->check(h,f.owner.messages.stream().anyMatch(s->s.contains("telegraph")),"Boss attack had no warning"));h.runAfterDelay(57,()->{check(h,Math.abs(f.owner.getHealth()-13)<.01,"Telegraphed DIO strike did not deal 7 damage");dio.discard();f.close();h.succeed();});return;}
  PlayerWillpower.set(f.owner,name.equals("boss_time_restore")?3:6);
  if(name.equals("time_boss_budget")){
   check(h,TimeStop.start(f.owner)&&!TimeStop.isFrozen(dio),"DIO could not initially move in foreign stopped time");
   h.onEachTick(()->TimeStop.tick(h.getLevel()));h.runAfterDelay(33,()->{check(h,TimeStop.isFrozen(dio),"DIO ignored its temporal movement budget");TimeStop.end(h.getLevel());dio.discard();f.close();h.succeed();});return;
  }
  check(h,TimeStop.startBoss(dio,60),"Boss time stop failed");
  check(h,TimeStop.isFrozen(f.owner)==name.equals("boss_time_restore"),"Boss stop compatibility wrong");
  if(name.equals("boss_time_restore")){dio.discard();check(h,!TimeStop.isFrozen(f.owner)&&!f.owner.getData(ModAttachments.TIME_FROZEN),"Boss removal stranded input freeze");f.close();h.succeed();return;}
  h.onEachTick(()->TimeStop.tick(h.getLevel()));
  if(name.equals("time_reentry")){
   h.runAfterDelay(10,()->{f.owner.snapTo(dio.position().add(30,0,0),0,0);TimeStop.tick(h.getLevel());});
   h.runAfterDelay(25,()->{f.owner.snapTo(dio.position().add(0,0,2),0,0);TimeStop.tick(h.getLevel());});
  }
  h.runAfterDelay(name.equals("time_star_budget")?23:33,()->{check(h,TimeStop.isFrozen(f.owner),"Compatible movement budget renewed or never exhausted");dio.discard();check(h,!TimeStop.isFrozen(f.owner),"Budget freeze not released");f.close();h.succeed();});
 }
    private static final class TestPlayer extends ServerPlayer {
        final Set<UUID> protectedPlayers = new HashSet<>(); final List<String> messages = new ArrayList<>();
        final List<io.github.bg7427941.jojosmod.network.StandVoicePayload> voices = new ArrayList<>();
        TestPlayer(ServerLevel level) {
            super(level.getServer(), level, new GameProfile(UUID.randomUUID(), "Crusader" + UUID.randomUUID().toString().substring(0, 6)), ClientInformation.createDefault());
            var transport = new net.minecraft.network.Connection(PacketFlow.SERVERBOUND) { @Override public void setListenerForServerboundHandshake(PacketListener listener) {} };
            new EmbeddedChannel(transport);
            connection = new ServerGamePacketListenerImpl(level.getServer(), transport, this, CommonListenerCookie.createInitial(getGameProfile(), false)) {
                @Override public void send(Packet<?> p) {
                    if (p instanceof net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket custom
                            && custom.payload() instanceof io.github.bg7427941.jojosmod.network.StandVoicePayload voice
                            && voice.owner().equals(TestPlayer.this.getUUID())) voices.add(voice);
                } @Override public void tick() {}
            };
            connection.markClientLoaded(); setGameMode(GameType.SURVIVAL); setNoGravity(true);
        }
        private void readAttachments(ValueInput input) { deserializeAttachments(input); }
        @Override public boolean canHarmPlayer(Player p) { return !protectedPlayers.contains(p.getUUID()) && super.canHarmPlayer(p); }
        @Override public void sendSystemMessage(Component text, boolean overlay) { if (messages != null) messages.add(text.toString()); }
    }
 private static final class Test extends GameTestInstance {
  static final MapCodec<Test> CODEC=RecordCodecBuilder.mapCodec(i->i.group(TestData.CODEC.fieldOf("data").forGetter(Test::info),com.mojang.serialization.Codec.STRING.fieldOf("case").forGetter(t->t.name)).apply(i,Test::new));
  final String name;Test(TestData<Holder<TestEnvironmentDefinition<?>>> data,String name){super(data);this.name=name;}
  @Override public void run(GameTestHelper h){if(name.startsWith("tentacle"))tentacle(h,name);else if(name.startsWith("barrier"))barrier(h,name);else if(name.startsWith("boss")||name.startsWith("time"))boss(h,name);else if(List.of("rapier_hit","knives_hit","weapon_wall_cancel_range","life_detector","hamon_bonus","sand_dome","advanced_gate","rapier_interrupt").contains(name))advanced(h,name);else campaign(h,name);}
  @Override public MapCodec<? extends GameTestInstance> codec(){return CODEC;}
  @Override protected net.minecraft.network.chat.MutableComponent typeDescription(){return Component.literal(name);}
 }
}
