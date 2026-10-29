package io.github.bg7427941.jojosmod.journey;

import io.github.bg7427941.jojosmod.entities.DioEntity;
import io.github.bg7427941.jojosmod.entities.DiscKeeperEntity;
import io.github.bg7427941.jojosmod.registry.*;
import io.github.bg7427941.jojosmod.stands.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Survival route: a real desert temple, voluntary keeper challenge and per-participant victory. */
public final class StardustJourney {
    private static final Map<UUID,Long> MISSING_SINCE=new HashMap<>();
    private StardustJourney(){}
    public static JourneyProgress progress(net.minecraft.world.entity.player.Player p){return p.getExistingData(ModAttachments.JOURNEY_PROGRESS).orElse(JourneyProgress.EMPTY);}
    public static boolean mastered(ServerPlayer p){
        var id=PlayerStands.get(p).standId();
        if(id.filter(ModStands.STAR_PLATINUM.id()::equals).isPresent()) {
            var data=StarPlatinumProgress.get(p);
            return java.util.Arrays.stream(StarPlatinumProgress.Milestone.values()).anyMatch(data::completed);
        }
        if(id.filter(ModStands.HIEROPHANT_GREEN.id()::equals).isPresent())return HierophantProgress.get(p).completed();
        return PartThreeStand.of(p).map(k->CrusaderProgress.get(p).training(k).completed()).orElse(false);
    }
    public static void onTick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer p)||p.tickCount%80!=0||!p.isAlive()||p.isSpectator()||!p.level().dimension().equals(Level.OVERWORLD))return;
        applyPending(p);
        if(mastered(p))advance(p,"mastery");
        var level=p.level();
        var key=ResourceKey.create(Registries.STRUCTURE,Identifier.withDefaultNamespace("desert_pyramid"));
        var pyramid=level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getOrThrow(key).value();
        var start=level.structureManager().getStructureAt(p.blockPosition(),pyramid);
        if(!start.isValid())return;
        var bounds=start.getBoundingBox();var anchor=new BlockPos((bounds.minX()+bounds.maxX())/2,bounds.minY(),(bounds.minZ()+bounds.maxZ())/2);
        var keeper=ensureKeeper(level,anchor);
        if(keeper!=null)notifyKeeper(p,keeper);
    }
    public static void notifyKeeper(ServerPlayer p,DiscKeeperEntity keeper) {
        var current=progress(p);var position=keeper.blockPosition();
        if(current.keeper().filter(position::equals).isPresent())return;
        p.setData(ModAttachments.JOURNEY_PROGRESS,current.locate(position));
        p.sendSystemMessage(Component.translatable("message.jojosmod.journey.keeper_near",position.getX(),position.getY(),position.getZ()),false);
    }
    public static DiscKeeperEntity ensureKeeper(ServerLevel level,BlockPos temple){
        var data=JourneyWorldData.get(level);var shrine=data.shrine(temple);
        if(shrine!=null) {
            if(!level.hasChunkAt(shrine.keeperPosition()))return null;
            if(level.getEntity(shrine.keeper()) instanceof DiscKeeperEntity keeper){MISSING_SINCE.remove(shrine.keeper());return keeper;}
            long now=level.getGameTime();long missing=MISSING_SINCE.computeIfAbsent(shrine.keeper(),u->now);
            if(now-missing<100)return null;
        } else {
            var safe=safeSurface(level,temple.offset(0,0,-12));if(safe.isEmpty())return null;
            shrine=new JourneyWorldData.Shrine(temple.immutable(),safe.get(),UUID.randomUUID(),0);data.replace(shrine);
        }
        var keeper=new DiscKeeperEntity(ModEntities.DISC_KEEPER.get(),level);keeper.setUUID(shrine.keeper());keeper.setTemple(temple);
        keeper.snapTo(shrine.keeperPosition().getX()+.5,shrine.keeperPosition().getY(),shrine.keeperPosition().getZ()+.5,0,0);
        if(!level.addFreshEntity(keeper))return null;
        MISSING_SINCE.remove(shrine.keeper());return keeper;
    }
    public static Optional<BlockPos> safeSurface(ServerLevel level,BlockPos near){
        for(int radius=0;radius<=8;radius+=2)for(int direction=0;direction<8;direction++) {
            double a=direction*Math.PI/4;int x=near.getX()+(int)(Math.cos(a)*radius),z=near.getZ()+(int)(Math.sin(a)*radius);
            if(!level.hasChunkAt(new BlockPos(x,near.getY(),z)))continue;
            var pos=new BlockPos(x,level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,x,z),z);
            if(!level.hasChunkAt(pos)||!level.isEmptyBlock(pos)||!level.isEmptyBlock(pos.above())||!level.getFluidState(pos.below()).isEmpty()
                    ||!level.getBlockState(pos.below()).isSolidRender())continue;
            return Optional.of(pos);
        }
        return Optional.empty();
    }
    public static void meet(ServerPlayer p,DiscKeeperEntity keeper){
        if(p.getAbilities().instabuild||p.isSpectator()){guide(p);return;}
        var before=progress(p);p.setData(ModAttachments.JOURNEY_PROGRESS,before.meet(keeper.blockPosition()));
        if(!before.metKeeper()) {rewardWill(p,1);advance(p,"meet_keeper");}
        claim(p);
        guide(p);
    }
    public static boolean challenge(ServerPlayer p,DiscKeeperEntity keeper){
        if(p.getAbilities().instabuild||p.isSpectator()||!p.isAlive()||p.level()!=keeper.level()||p.distanceToSqr(keeper)>9||!p.hasLineOfSight(keeper))return false;
        meet(p,keeper);
        if(keeper.temple().isEmpty()||PlayerStands.get(p).standId().isEmpty()||PlayerWillpower.get(p)<4||!mastered(p)) {
            p.sendSystemMessage(Component.translatable("message.jojosmod.journey.challenge_locked"),false);return false;
        }
        if(p.level().getDifficulty()==net.minecraft.world.Difficulty.PEACEFUL) {p.sendSystemMessage(Component.translatable("message.jojosmod.journey.peaceful"),false);return false;}
        long day=p.level().getOverworldClockTime()%24000;
        if(day<13000||day>23000){p.sendSystemMessage(Component.translatable("message.jojosmod.journey.night"),true);return false;}
        var data=JourneyWorldData.get(p.level());var shrine=data.shrine(keeper.temple().get());
        if(shrine==null||p.level().getGameTime()<shrine.readyAt()) {p.sendSystemMessage(Component.translatable("message.jojosmod.journey.retry"),true);return false;}
        if(!p.level().getEntitiesOfClass(DioEntity.class,keeper.getBoundingBox().inflate(96),DioEntity::isAlive).isEmpty())return false;
        var safe=safeSurface(p.level(),keeper.blockPosition().offset(12,0,0));if(safe.isEmpty())return false;
        int players=(int)p.level().players().stream().filter(t->t.isAlive()&&!t.isSpectator()&&!t.getAbilities().instabuild&&t.distanceToSqr(keeper)<48*48).count();
        var dio=new DioEntity(ModEntities.DIO.get(),p.level());dio.prepare(safe.get(),Math.min(4,Math.max(1,players)));
        dio.snapTo(safe.get().getX()+.5,safe.get().getY(),safe.get().getZ()+.5,p.getYRot()+180,0);
        if(!p.level().addFreshEntity(dio))return false;
        data.replace(shrine.cooldown(p.level().getGameTime()+1200));
        p.sendSystemMessage(Component.translatable("message.jojosmod.journey.challenge"),false);return true;
    }
    public static void defeated(DioEntity dio,java.util.Collection<UUID> participants){
        var world=JourneyWorldData.get(dio.level().getServer().overworld());
        for(var uuid:participants){world.queue(uuid);var p=dio.level().getServer().getPlayerList().getPlayer(uuid);if(p!=null)applyPending(p);}
    }
    public static void applyPending(ServerPlayer p){
        var world=JourneyWorldData.get(p.level().getServer().overworld());if(!world.pending.contains(p.getUUID()))return;
        var before=progress(p);
        if(!before.defeatedDio()) {
            p.setData(ModAttachments.JOURNEY_PROGRESS,before.victory());rewardWill(p,2);advance(p,"defeat_dio");
            p.sendSystemMessage(Component.translatable("message.jojosmod.journey.victory"),false);
        }
        world.delivered(p.getUUID());claim(p);
    }
    public static boolean claim(ServerPlayer p){
        boolean world=claimWorldCore(p);
        return claimMasteryCore(p)||world;
    }
    private static boolean claimMasteryCore(ServerPlayer p){
        if(!p.isAlive()||p.isSpectator()||p.getAbilities().instabuild||io.github.bg7427941.jojosmod.abilities.TimeStop.isFrozen(p)||!mastered(p))return false;
        var id=PlayerStands.get(p).standId().orElseThrow();var progress=progress(p);
        if(!progress.metKeeper()||progress.masteryClaims().contains(id.getPath()))return false;
        var item=switch(id.getPath()){
            case "star_platinum"->ModItems.STAR_PLATINUM_CORE;case "hierophant_green"->ModItems.HIEROPHANT_GREEN_CORE;
            case "silver_chariot"->ModItems.SILVER_CHARIOT_CORE;case "magicians_red"->ModItems.MAGICIANS_RED_CORE;
            case "hermit_purple"->ModItems.HERMIT_PURPLE_CORE;case "the_fool"->ModItems.THE_FOOL_CORE;default->null;};
        if(item==null)return false;
        var stack=new ItemStack(item.get());if(!p.getInventory().add(stack)){p.sendSystemMessage(Component.translatable("message.jojosmod.journey.mastery_full"),false);return false;}
        p.setData(ModAttachments.JOURNEY_PROGRESS,progress.claimMastery(id.getPath()));
        p.sendSystemMessage(Component.translatable("message.jojosmod.journey.mastery_core",Component.translatable(ModStands.find(id).orElseThrow().translationKey())),false);return true;
    }
    private static boolean claimWorldCore(ServerPlayer p){
        if(!p.isAlive()||p.isSpectator()||p.getAbilities().instabuild||io.github.bg7427941.jojosmod.abilities.TimeStop.isFrozen(p))return false;
        var s=progress(p);if(!s.defeatedDio()||s.claimedCore())return false;
        var core=new ItemStack(ModItems.THE_WORLD_CORE.get());
        if(!p.getInventory().add(core)){p.sendSystemMessage(Component.translatable("message.jojosmod.journey.claim_full"),false);return false;}
        p.setData(ModAttachments.JOURNEY_PROGRESS,s.claimed());p.sendSystemMessage(Component.translatable("message.jojosmod.journey.claimed"),false);return true;
    }
    private static void rewardWill(ServerPlayer p,int value){PlayerWillpower.set(p,Math.min(WillpowerData.MAX_VALUE,PlayerWillpower.get(p)+value));}
    private static void advance(ServerPlayer p,String path){
        var a=p.level().getServer().getAdvancements().get(Identifier.fromNamespaceAndPath("jojosmod","journey/"+path));
        if(a!=null)for(var criterion:a.value().criteria().keySet())p.getAdvancements().award(a,criterion);
    }
    public static void guide(ServerPlayer p){
        var progress=progress(p);
        var id=PlayerStands.get(p).standId();
        boolean stardust=id.filter(s->s.equals(ModStands.STAR_PLATINUM.id())||s.equals(ModStands.HIEROPHANT_GREEN.id())||PartThreeStand.of(s).isPresent()).isPresent();
        String next=id.isEmpty()?"arrow":!stardust?"legacy":!mastered(p)?"mastery":!progress.metKeeper()?"keeper":!progress.defeatedDio()?"dio":!progress.claimedCore()?"claim":"complete";
        p.sendSystemMessage(Component.translatable("message.jojosmod.journey.next_"+next),false);
        progress.keeper().ifPresent(pos->p.sendSystemMessage(Component.translatable("message.jojosmod.journey.keeper_at",pos.getX(),pos.getY(),pos.getZ()),false));
    }
    public static void onAssigned(ServerPlayer p) {
        var id=PlayerStands.get(p).standId();
        if(id.filter(s->s.equals(ModStands.STAR_PLATINUM.id())||s.equals(ModStands.HIEROPHANT_GREEN.id())||PartThreeStand.of(s).isPresent()).isPresent()) {
            advance(p,"root");advance(p,"awaken");
        }
    }
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event){if(event.getEntity() instanceof ServerPlayer p){
        if(!p.hasData(ModAttachments.JOURNEY_PROGRESS)) {p.setData(ModAttachments.JOURNEY_PROGRESS,JourneyProgress.EMPTY);p.sendSystemMessage(Component.translatable("message.jojosmod.journey.welcome"),false);}
        applyPending(p);onAssigned(p);
    }}
}
