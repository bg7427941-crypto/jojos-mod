package io.github.bg7427941.jojosmod.registry;

import io.github.bg7427941.jojosmod.JojosMod;
import io.github.bg7427941.jojosmod.entities.TrainingStandEntity;
import io.github.bg7427941.jojosmod.entities.DiscKeeperEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {
    private static final float STAND_WIDTH = 0.6F;
    private static final float STAND_HEIGHT = 1.8F;
    private static final int TRACKING_RANGE = 8;
    private static final int UPDATE_INTERVAL = 1;
    private static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, JojosMod.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<TrainingStandEntity>> TRAINING_STAND =
            ENTITIES.register("training_stand", () -> EntityType.Builder
                    .of(TrainingStandEntity::new, MobCategory.MISC)
                    .sized(STAND_WIDTH, STAND_HEIGHT)
                    .noSave()
                    .noSummon()
                    .clientTrackingRange(TRACKING_RANGE)
                    .updateInterval(UPDATE_INTERVAL)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath(JojosMod.MOD_ID, "training_stand"))));

    public static final DeferredHolder<EntityType<?>, EntityType<DiscKeeperEntity>> DISC_KEEPER =
            ENTITIES.register("disc_keeper", () -> EntityType.Builder
                    .of(DiscKeeperEntity::new, MobCategory.MISC)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(TRACKING_RANGE)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath(JojosMod.MOD_ID, "disc_keeper"))));

    public static final DeferredHolder<EntityType<?>, EntityType<TrainingStandEntity>> STAR_PLATINUM =
            ENTITIES.register("star_platinum", () -> EntityType.Builder
                    .of(TrainingStandEntity::new, MobCategory.MISC)
                    .sized(0.8F, 2.1F).noSave().noSummon()
                    .clientTrackingRange(TRACKING_RANGE).updateInterval(UPDATE_INTERVAL)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath(JojosMod.MOD_ID, "star_platinum"))));

    private ModEntities() {}

    public static void register(IEventBus modEventBus) {
        ENTITIES.register(modEventBus);
    }
}
