package io.github.bg7427941.jojosmod.entities;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;
import io.github.bg7427941.jojosmod.abilities.TimeStop;
import io.github.bg7427941.jojosmod.journey.StardustJourney;
import io.github.bg7427941.jojosmod.registry.ModEntities;
import io.github.bg7427941.jojosmod.registry.ModStands;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Voluntary PvE encounter. No block griefing, ambient spawn or last-hit-only reward. */
public final class DioEntity extends Monster implements GeoEntity {
    public enum Pattern { NONE, STRIKE, KNIVES, TIME }
    private final AnimatableInstanceCache cache=GeckoLibUtil.createInstanceCache(this);
    private final ServerBossEvent boss=new ServerBossEvent(UUID.randomUUID(),Component.translatable("entity.jojosmod.dio"),BossEvent.BossBarColor.YELLOW,BossEvent.BossBarOverlay.PROGRESS);
    private final Map<UUID,Long> participants=new HashMap<>();
    private BlockPos arena;
    private TrainingStandEntity projection;
    private Pattern pattern=Pattern.NONE;
    private long resolveAt,readyAt,timeReady,emptySince=-1;
    private int cycle;
    private boolean rewarded;
    public DioEntity(EntityType<? extends DioEntity> type,Level level){super(type,level);setPersistenceRequired();xpReward=50;}
    public static net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder attributes(){
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,140).add(Attributes.MOVEMENT_SPEED,.29)
                .add(Attributes.ATTACK_DAMAGE,7).add(Attributes.ARMOR,4).add(Attributes.FOLLOW_RANGE,40).add(Attributes.KNOCKBACK_RESISTANCE,.65);
    }
    @Override protected void registerGoals(){goalSelector.addGoal(0,new FloatGoal(this));}
    public void prepare(BlockPos arena,int players){
        this.arena=arena.immutable();getAttribute(Attributes.MAX_HEALTH).setBaseValue(140+40*(Math.max(1,Math.min(4,players))-1));setHealth(getMaxHealth());
        readyAt=level().getGameTime()+40;timeReady=level().getGameTime()+160;
    }
    public boolean ownsProjection(TrainingStandEntity entity){return projection==entity&&isAlive()&&!isRemoved();}
    public boolean canThreaten(LivingEntity t){return t instanceof ServerPlayer p&&p.isAlive()&&!p.isSpectator()&&!p.getAbilities().instabuild&&!isAlliedTo(p);}
    public Pattern pattern(){return pattern;}
    public long resolveAt(){return resolveAt;}
    public java.util.Set<UUID> participants(){return java.util.Set.copyOf(participants.keySet());}
    private void contribute(ServerPlayer p){if(!p.getAbilities().instabuild&&!p.isSpectator()&&p.level()==level()&&p.distanceToSqr(this)<=48*48)participants.put(p.getUUID(),level().getGameTime());}
    @Override public boolean hurtServer(ServerLevel level,DamageSource source,float amount){
        float before=getHealth();boolean hit=super.hurtServer(level,source,amount);
        if(getHealth()<before&&source.getEntity() instanceof ServerPlayer p)contribute(p);
        return hit;
    }
    @Override public void die(DamageSource source){
        if(!rewarded&&level() instanceof ServerLevel level){
            rewarded=true;if(source.getEntity() instanceof ServerPlayer p)contribute(p);
            long now=level.getGameTime();var eligible=participants.entrySet().stream().filter(e->now-e.getValue()<=1200).map(Map.Entry::getKey).toList();
            StardustJourney.defeated(this,eligible);TimeStop.cancel(this);io.github.bg7427941.jojosmod.effects.StandVoices.stop(this);if(projection!=null)projection.discard();boss.removeAllPlayers();
        }
        super.die(source);
    }
    @Override public void tick(){
        super.tick();if(!(level() instanceof ServerLevel level)||!isAlive())return;
        long now=level.getGameTime();if(arena==null)arena=blockPosition();
        boss.setProgress(Math.max(0,getHealth()/getMaxHealth()));
        var nearby=level.players().stream().filter(p->canThreaten(p)&&p.distanceToSqr(this)<=48*48).toList();
        for(var old:java.util.List.copyOf(boss.getPlayers()))if(!nearby.contains(old))boss.removePlayer(old);
        nearby.forEach(boss::addPlayer);
        if(nearby.isEmpty()) {
            if(emptySince<0)emptySince=now;
            if(now-emptySince>=200)discard();return;
        }
        emptySince=-1;
        if(distanceToSqr(Vec3.atBottomCenterOf(arena))>48*48){discard();return;}
        var target=nearby.stream().min(java.util.Comparator.comparingDouble(this::distanceToSqr)).orElseThrow();
        setTarget(target);getLookControl().setLookAt(target,90,90);
        if(projection==null||projection.isRemoved()) {
            projection=new TrainingStandEntity(ModEntities.THE_WORLD.get(),level);projection.setEncounterOwner(this);projection.setStandDefinition(ModStands.THE_WORLD);
            projection.snapTo(position(),getYRot(),0);level.addFreshEntity(projection);projection.playCrusaderAnimation("summon");
        }
        if(pattern!=Pattern.NONE) {
            getNavigation().stop();
            if(now>=resolveAt)resolve(target);
            return;
        }
        if(distanceToSqr(target)>7)getNavigation().moveTo(target,1.05);else getNavigation().stop();
        if(now<readyAt||distanceToSqr(target)>18*18||!hasLineOfSight(target))return;
        pattern=getHealth()<=getMaxHealth()*.5F&&now>=timeReady?Pattern.TIME:distanceToSqr(target)>20||++cycle%3==0?Pattern.KNIVES:Pattern.STRIKE;
        resolveAt=now+(pattern==Pattern.TIME?28:pattern==Pattern.KNIVES?20:12);
        readyAt=resolveAt+(pattern==Pattern.TIME?12:pattern==Pattern.KNIVES?55:24);
        triggerAnim("main",pattern==Pattern.TIME?"time_stop":pattern==Pattern.KNIVES?"knives":"attack");
        if(pattern!=Pattern.TIME)io.github.bg7427941.jojosmod.effects.StandVoices.play(this,io.github.bg7427941.jojosmod.network.StandVoicePayload.Cue.WORLD_STRIKE);
        projection.playCrusaderAnimation(pattern==Pattern.TIME?"time_stop":pattern==Pattern.KNIVES?"advanced":"attack");
        for(var p:nearby)p.sendSystemMessage(Component.translatable("message.jojosmod.dio.telegraph."+pattern.name().toLowerCase(java.util.Locale.ROOT)),true);
    }
    private void resolve(ServerPlayer target){
        var old=pattern;pattern=Pattern.NONE;
        if(!canThreaten(target))return;
        switch(old){
            case STRIKE->{if(distanceToSqr(target)<=3.5*3.5&&hasLineOfSight(target)){contribute(target);target.hurtServer((ServerLevel)level(),level().damageSources().mobAttack(this),7);}}
            case KNIVES->{
                var direction=target.getEyePosition().subtract(getEyePosition()).normalize();var token=UUID.randomUUID();
                for(int angle:new int[]{-8,-4,0,4,8}){var knife=new StandWeaponEntity(ModEntities.STAND_WEAPON.get(),level());knife.initialize(this,token,false,getEyePosition(),direction.yRot((float)Math.toRadians(angle)));level().addFreshEntity(knife);}
            }
            case TIME->{timeReady=level().getGameTime()+260;TimeStop.startBoss(this,getHealth()<getMaxHealth()*.25?60:40);}
            default->{}
        }
    }
    @Override public void remove(net.minecraft.world.entity.Entity.RemovalReason reason){
        if(!level().isClientSide()){TimeStop.cancel(this);io.github.bg7427941.jojosmod.effects.StandVoices.stop(this);if(projection!=null)projection.discard();boss.removeAllPlayers();}
        super.remove(reason);
    }
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers){
        controllers.add(new AnimationController<DioEntity>("locomotion",3,state->state.setAndContinue(RawAnimation.begin().thenLoop("animation.dio."+(getDeltaMovement().horizontalDistanceSqr()>.001?"move":"idle")))));
        var main=new AnimationController<DioEntity>("main",0,state->com.geckolib.animation.object.PlayState.STOP);
        for(var name:java.util.List.of("attack","knives","time_stop"))main.triggerableAnim(name,RawAnimation.begin().thenPlay("animation.dio."+name));controllers.add(main);
    }
    @Override public AnimatableInstanceCache getAnimatableInstanceCache(){return cache;}
}
