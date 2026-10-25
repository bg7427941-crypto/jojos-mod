package io.github.bg7427941.jojosmod.registry;

import io.github.bg7427941.jojosmod.JojosMod;
import io.github.bg7427941.jojosmod.entities.TrainingStandEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;

public final class ModDamageTypes {
    public static final ResourceKey<DamageType> STAND_BARRAGE = ResourceKey.create(Registries.DAMAGE_TYPE,
            Identifier.fromNamespaceAndPath(JojosMod.MOD_ID, "stand_barrage"));

    private ModDamageTypes() {}

    public static DamageSource barrage(ServerPlayer owner, TrainingStandEntity stand) {
        return new DamageSource(owner.level().registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE)
                .getOrThrow(STAND_BARRAGE), stand, owner);
    }
}
