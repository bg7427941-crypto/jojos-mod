package io.github.bg7427941.jojosmod.registry;

import io.github.bg7427941.jojosmod.JojosMod;
import io.github.bg7427941.jojosmod.entities.TrainingStandEntity;
import io.github.bg7427941.jojosmod.entities.DiscKeeperEntity;
import io.github.bg7427941.jojosmod.entities.EmeraldShardEntity;
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

    public static final DeferredHolder<EntityType<?>, EntityType<TrainingStandEntity>> HIEROPHANT_GREEN =
            ENTITIES.register("hierophant_green", () -> EntityType.Builder
                    .of(TrainingStandEntity::new, MobCategory.MISC)
                    .sized(.65F, 2.0F).noSave().noSummon()
                    .clientTrackingRange(TRACKING_RANGE).updateInterval(UPDATE_INTERVAL)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath(JojosMod.MOD_ID, "hierophant_green"))));

    public static final DeferredHolder<EntityType<?>, EntityType<EmeraldShardEntity>> EMERALD_SHARD =
            ENTITIES.register("emerald_shard", () -> EntityType.Builder
                    .of(EmeraldShardEntity::new, MobCategory.MISC)
                    .sized(.18F, .18F).noSave().noSummon()
                    .clientTrackingRange(TRACKING_RANGE).updateInterval(UPDATE_INTERVAL)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath(JojosMod.MOD_ID, "emerald_shard"))));

    public static final DeferredHolder<EntityType<?>, EntityType<TrainingStandEntity>> SILVER_CHARIOT = crusaderType("silver_chariot", 0.65F, 2.0F);
    public static final DeferredHolder<EntityType<?>, EntityType<TrainingStandEntity>> MAGICIANS_RED = crusaderType("magicians_red", 0.65F, 2.0F);
    public static final DeferredHolder<EntityType<?>, EntityType<TrainingStandEntity>> HERMIT_PURPLE = crusaderType("hermit_purple", 0.65F, 1.8F);
    public static final DeferredHolder<EntityType<?>, EntityType<TrainingStandEntity>> THE_FOOL = crusaderType("the_fool", 1.0F, 1.15F);
    public static final DeferredHolder<EntityType<?>, EntityType<TrainingStandEntity>> THE_WORLD = crusaderType("the_world", 0.65F, 2.0F);

    public static EntityType<TrainingStandEntity> crusader(io.github.bg7427941.jojosmod.stands.PartThreeStand kind) {
        return switch (kind) {
            case SILVER_CHARIOT -> SILVER_CHARIOT.get();
            case MAGICIANS_RED -> MAGICIANS_RED.get();
            case HERMIT_PURPLE -> HERMIT_PURPLE.get();
            case THE_FOOL -> THE_FOOL.get();
            case THE_WORLD -> THE_WORLD.get();
        };
    }
    private static DeferredHolder<EntityType<?>, EntityType<TrainingStandEntity>> crusaderType(String path, float width, float height) {
        return ENTITIES.register(path, () -> EntityType.Builder.of(TrainingStandEntity::new, MobCategory.MISC)
                .sized(width, height).noSave().noSummon().clientTrackingRange(TRACKING_RANGE).updateInterval(UPDATE_INTERVAL)
                .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(JojosMod.MOD_ID, path))));
    }
    public static final DeferredHolder<EntityType<?>, EntityType<io.github.bg7427941.jojosmod.entities.CrusaderFlameEntity>> CRUSADER_FLAME =
            ENTITIES.register("crusader_flame", () -> EntityType.Builder
                    .of(io.github.bg7427941.jojosmod.entities.CrusaderFlameEntity::new, MobCategory.MISC)
                    .sized(.3F, .3F).noSave().noSummon().clientTrackingRange(TRACKING_RANGE).updateInterval(UPDATE_INTERVAL)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(JojosMod.MOD_ID, "crusader_flame"))));

    public static final DeferredHolder<EntityType<?>, EntityType<io.github.bg7427941.jojosmod.entities.StandWeaponEntity>> STAND_WEAPON =
            ENTITIES.register("stand_weapon", () -> EntityType.Builder.of(io.github.bg7427941.jojosmod.entities.StandWeaponEntity::new, MobCategory.MISC)
                    .sized(.18F,.18F).noSave().noSummon().clientTrackingRange(TRACKING_RANGE).updateInterval(1)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE,Identifier.fromNamespaceAndPath(JojosMod.MOD_ID,"stand_weapon"))));

    public static final DeferredHolder<EntityType<?>, EntityType<io.github.bg7427941.jojosmod.entities.DioEntity>> DIO =
            ENTITIES.register("dio", () -> EntityType.Builder.of(io.github.bg7427941.jojosmod.entities.DioEntity::new, MobCategory.MONSTER)
                    .sized(.6F,1.95F).noSave().clientTrackingRange(10).updateInterval(2)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE,Identifier.fromNamespaceAndPath(JojosMod.MOD_ID,"dio"))));

    private ModEntities() {}

    public static void register(IEventBus modEventBus) {
        ENTITIES.register(modEventBus);
    }
}
