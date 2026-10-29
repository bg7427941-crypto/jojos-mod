package io.github.bg7427941.jojosmod.abilities;

import io.github.bg7427941.jojosmod.effects.CrusaderEffects;
import io.github.bg7427941.jojosmod.entities.StandWeaponEntity;
import io.github.bg7427941.jojosmod.registry.ModEntities;
import io.github.bg7427941.jojosmod.stands.*;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/** Each Crusader's V technique complements its existing kit without client-selected targets. */
public final class CrusaderAdvanced {
    private CrusaderAdvanced() {}
    public static boolean start(ServerPlayer owner) {
        var stand = CrusaderActions.active(owner); if (stand.isEmpty()) return false;
        var s = CrusaderActions.session(owner); long now = owner.level().getGameTime();
        if (PlayerWillpower.get(owner) < 4) { owner.sendSystemMessage(Component.translatable("message.jojosmod.advanced.locked", 4),true); return false; }
        if (s.action != CrusaderSession.Action.NONE || now < s.advancedReady) return false;
        s.kind = PartThreeStand.of(owner).orElseThrow(); s.action = CrusaderSession.Action.ADVANCED;
        s.startedAt = now; s.pulses = 0; s.volley = UUID.randomUUID();
        s.advancedReady = now + switch(s.kind) {case SILVER_CHARIOT->160;case MAGICIANS_RED->180;case HERMIT_PURPLE->120;case THE_FOOL->240;case THE_WORLD->140;};
        if (s.kind == PartThreeStand.SILVER_CHARIOT) s.rapierUntil = now + 35;
        stand.get().playCrusaderAnimation("advanced");
        if(s.kind==PartThreeStand.THE_WORLD || s.kind==PartThreeStand.SILVER_CHARIOT || s.kind==PartThreeStand.HERMIT_PURPLE)
            io.github.bg7427941.jojosmod.effects.StandVoices.play(owner,s.kind==PartThreeStand.THE_WORLD?io.github.bg7427941.jojosmod.network.StandVoicePayload.Cue.WORLD_STRIKE:s.kind==PartThreeStand.SILVER_CHARIOT?io.github.bg7427941.jojosmod.network.StandVoicePayload.Cue.CHARIOT_STRIKE:io.github.bg7427941.jojosmod.network.StandVoicePayload.Cue.PURPLE_CALL);
        CrusaderEffects.sound(owner.level(),s.kind,"cast",stand.get().position());
        owner.sendSystemMessage(Component.translatable("message.jojosmod.advanced."+s.kind.path()),true);
        return true;
    }
    public static void tick(ServerPlayer owner) {
        var s=CrusaderActions.session(owner);long age=owner.level().getGameTime()-s.startedAt;
        if(s.action!=CrusaderSession.Action.ADVANCED)return;
        if(age>=8&&s.pulses==0) {
            s.pulses=1;
            switch(s.kind) {
                case SILVER_CHARIOT -> weapon(owner,true,0);
                case THE_WORLD -> {for(int angle:new int[]{-8,-4,0,4,8}) {if(s.action!=CrusaderSession.Action.ADVANCED)return;weapon(owner,false,angle);}}
                case MAGICIANS_RED -> {
                    int count=0;
                    for(var t:owner.level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,owner.getBoundingBox().inflate(20),t->t instanceof Enemy&&CrusaderActions.canDamage(owner,t))) {
                        t.addEffect(new MobEffectInstance(MobEffects.GLOWING,100,0,false,false));
                        CrusaderEffects.line(owner.level(),s.kind,owner.getEyePosition(),t.getEyePosition());if(++count==12)break;
                    }
                    owner.sendSystemMessage(Component.translatable("message.jojosmod.advanced.life_count",count),true);
                }
                case HERMIT_PURPLE -> CrusaderActions.target(owner,7).ifPresent(t->{
                    CrusaderEffects.line(owner.level(),s.kind,owner.getEyePosition(),t.getEyePosition());
                    CrusaderActions.damage(owner,t,t.getType().builtInRegistryHolder().is(EntityTypeTags.UNDEAD)?9:4,false);
                });
                case THE_FOOL -> {}
            }
        }
        if(s.kind==PartThreeStand.THE_FOOL&&age%4==0) dome(owner);
        if(age>=(s.kind==PartThreeStand.THE_FOOL?60:s.kind==PartThreeStand.SILVER_CHARIOT?35:24)) CrusaderActions.finish(owner,false);
    }
    private static void weapon(ServerPlayer owner,boolean rapier,int angle) {
        var projectile=new StandWeaponEntity(ModEntities.STAND_WEAPON.get(),owner.level());
        var direction=owner.getLookAngle().yRot((float)Math.toRadians(angle));
        projectile.initialize(owner,CrusaderActions.session(owner).volley,rapier,owner.getEyePosition(),direction);
        owner.level().addFreshEntity(projectile);
    }
    public static boolean weaponValid(ServerPlayer p,UUID token,boolean rapier) {
        return CrusaderActions.active(p).filter(s->s.getStandDefinition().id().equals((rapier?PartThreeStand.SILVER_CHARIOT:PartThreeStand.THE_WORLD).id())).isPresent()
                && token.equals(CrusaderActions.session(p).volley);
    }
    public static boolean domeActive(ServerPlayer p) {
        if(CrusaderActions.active(p).isEmpty())return false;
        var s=CrusaderActions.session(p);
        return s.kind==PartThreeStand.THE_FOOL&&s.action==CrusaderSession.Action.ADVANCED&&p.level().getGameTime()-s.startedAt<60;
    }
    public static void onIncoming(LivingIncomingDamageEvent event) {
        if(!(event.getEntity().level() instanceof net.minecraft.server.level.ServerLevel level)
                ||event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)||event.getSource().is(DamageTypeTags.BYPASSES_ARMOR))return;
        for(var p:level.players()) {
            if(p.distanceToSqr(event.getEntity())>9||!domeActive(p))continue;
            boolean friend=event.getEntity()==p||p.isAlliedTo(event.getEntity())
                    ||!level.isPvpAllowed()&&event.getEntity() instanceof ServerPlayer;
            if(friend) {event.setAmount(event.getAmount()*.6F);return;}
        }
    }
    private static void dome(ServerPlayer p) {
        for(int row=0;row<3;row++)for(int i=0;i<16;i++) {
            double a=Math.PI*2*i/16,r=3*Math.cos(row*Math.PI/6);
            Vec3 at=p.position().add(Math.cos(a)*r,.3+3*Math.sin(row*Math.PI/6),Math.sin(a)*r);
            CrusaderEffects.burst(p.level(),PartThreeStand.THE_FOOL,at,1);
        }
    }
}
