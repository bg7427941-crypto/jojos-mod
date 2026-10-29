package io.github.bg7427941.jojosmod.entities;

import io.github.bg7427941.jojosmod.abilities.CrusaderActions;
import io.github.bg7427941.jojosmod.abilities.CrusaderAdvanced;
import io.github.bg7427941.jojosmod.registry.ModDamageTypes;
import java.util.UUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Physical rapier/knife presentation. No pickup, inventory ammunition or persistent entity. */
public final class StandWeaponEntity extends ThrowableItemProjectile {
    private UUID token;
    private boolean rapier;
    private double travelled;
    public StandWeaponEntity(EntityType<? extends StandWeaponEntity> type,Level level){super(type,level);setNoGravity(true);}
    @Override protected Item getDefaultItem(){return Items.IRON_NUGGET;}
    public void initialize(LivingEntity owner,UUID token,boolean rapier,Vec3 origin,Vec3 direction){
        setOwner(owner);this.token=token;this.rapier=rapier;setPos(origin);
        var item=new ItemStack(Items.IRON_NUGGET);item.set(DataComponents.ITEM_MODEL,Identifier.fromNamespaceAndPath("jojosmod",rapier?"flying_rapier":"flying_knife"));setItem(item);
        shoot(direction.x,direction.y,direction.z,rapier?1.4F:1.1F,0);
    }
    private boolean valid(){
        return getOwner() instanceof ServerPlayer p&&p.level()==level()&&token!=null&&CrusaderAdvanced.weaponValid(p,token,rapier)
                ||getOwner() instanceof DioEntity dio&&dio.isAlive()&&dio.level()==level()&&!rapier;
    }
    public boolean isRapier(){return rapier;}
    public double maxRange(){return rapier?12:16;}
    @Override public void tick(){
        if(!level().isClientSide()&&!valid()||tickCount>=30||travelled>=maxRange()){discard();return;}
        var delta=getDeltaMovement();if(delta.length()>maxRange()-travelled)setDeltaMovement(delta.normalize().scale(maxRange()-travelled));
        var before=position();super.tick();travelled+=before.distanceTo(position());if(travelled>=maxRange()-.00001)discard();
    }
    @Override protected boolean canHitEntity(Entity e){
        if(!super.canHitEntity(e)||!(e instanceof LivingEntity t))return false;
        if(level().isClientSide())return true;
        if(getOwner() instanceof ServerPlayer p)return valid()&&CrusaderActions.canDamage(p,t);
        return valid()&&getOwner() instanceof DioEntity dio&&dio.canThreaten(t);
    }
    @Override protected void onHitEntity(EntityHitResult hit){
        if(!(level() instanceof net.minecraft.server.level.ServerLevel level)||!valid()||!(hit.getEntity() instanceof LivingEntity target))return;
        if(getOwner() instanceof ServerPlayer p)CrusaderActions.damage(p,target,rapier?9:3,false);
        else if(getOwner() instanceof DioEntity dio)target.hurtServer(level,level.damageSources().mobProjectile(this,dio),3);
    }
    @Override protected void onHit(HitResult hit){super.onHit(hit);if(!level().isClientSide())discard();}
}
