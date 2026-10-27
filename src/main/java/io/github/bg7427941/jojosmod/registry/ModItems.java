package io.github.bg7427941.jojosmod.registry;

import io.github.bg7427941.jojosmod.JojosMod;
import io.github.bg7427941.jojosmod.items.StandArrowItem;
import io.github.bg7427941.jojosmod.items.StandDiscItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Registers progression items and their creative tab. */
public final class ModItems {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(JojosMod.MOD_ID);
    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, JojosMod.MOD_ID);

    public static final DeferredItem<StandArrowItem> STAND_ARROW =
            ITEMS.registerItem("stand_arrow", StandArrowItem::new, properties -> properties.stacksTo(1));
    public static final DeferredItem<StandArrowItem> STARDUST_ARROW =
            ITEMS.registerItem("stardust_arrow", properties -> new StandArrowItem(properties, ModStands.STARDUST_POOL),
                    properties -> properties.stacksTo(1));
    public static final DeferredItem<Item> STAND_DISC_FRAGMENT =
            ITEMS.registerSimpleItem("stand_disc_fragment");
    public static final DeferredItem<Item> EMPTY_STAND_DISC =
            ITEMS.registerSimpleItem("empty_stand_disc", properties -> properties.stacksTo(1));
    public static final DeferredItem<StandDiscItem> STAND_DISC =
            ITEMS.registerItem("stand_disc", StandDiscItem::new, properties -> properties.stacksTo(1));
    public static final DeferredItem<Item> ECHOES_ACT_1_CORE =
            ITEMS.registerSimpleItem("echoes_act_1_core", properties -> properties.stacksTo(1));
    public static final DeferredItem<Item> LITTLE_FEET_CORE =
            ITEMS.registerSimpleItem("little_feet_core", properties -> properties.stacksTo(1));
    public static final DeferredItem<Item> STAR_PLATINUM_CORE =
            ITEMS.registerSimpleItem("star_platinum_core", properties -> properties.stacksTo(1));
    public static final DeferredItem<Item> HIEROPHANT_GREEN_CORE =
            ITEMS.registerSimpleItem("hierophant_green_core", properties -> properties.stacksTo(1));

    public static final DeferredItem<Item> SILVER_CHARIOT_CORE = ITEMS.registerSimpleItem("silver_chariot_core", properties -> properties.stacksTo(1));
    public static final DeferredItem<Item> MAGICIANS_RED_CORE = ITEMS.registerSimpleItem("magicians_red_core", properties -> properties.stacksTo(1));
    public static final DeferredItem<Item> HERMIT_PURPLE_CORE = ITEMS.registerSimpleItem("hermit_purple_core", properties -> properties.stacksTo(1));
    public static final DeferredItem<Item> THE_FOOL_CORE = ITEMS.registerSimpleItem("the_fool_core", properties -> properties.stacksTo(1));
    public static final DeferredItem<Item> THE_WORLD_CORE = ITEMS.registerSimpleItem("the_world_core", properties -> properties.stacksTo(1));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> JOJOS_TAB =
            TABS.register("jojos_mod", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.jojosmod"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> STAND_ARROW.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(STAND_ARROW.get());
                        output.accept(STARDUST_ARROW.get());
                        output.accept(STAND_DISC_FRAGMENT.get());
                        output.accept(EMPTY_STAND_DISC.get());
                        output.accept(STAND_DISC.get());
                        output.accept(ECHOES_ACT_1_CORE.get());
                        output.accept(LITTLE_FEET_CORE.get());
                        output.accept(STAR_PLATINUM_CORE.get());
                        output.accept(HIEROPHANT_GREEN_CORE.get());
                        output.accept(SILVER_CHARIOT_CORE.get());
                        output.accept(StandDiscItem.containing(ModStands.SILVER_CHARIOT));
                        output.accept(MAGICIANS_RED_CORE.get());
                        output.accept(StandDiscItem.containing(ModStands.MAGICIANS_RED));
                        output.accept(HERMIT_PURPLE_CORE.get());
                        output.accept(StandDiscItem.containing(ModStands.HERMIT_PURPLE));
                        output.accept(THE_FOOL_CORE.get());
                        output.accept(StandDiscItem.containing(ModStands.THE_FOOL));
                        output.accept(THE_WORLD_CORE.get());
                        output.accept(StandDiscItem.containing(ModStands.THE_WORLD));
                        output.accept(StandDiscItem.containing(ModStands.ECHOES_ACT_1));
                        output.accept(StandDiscItem.containing(ModStands.LITTLE_FEET));
                        output.accept(StandDiscItem.containing(ModStands.STAR_PLATINUM));
                        output.accept(StandDiscItem.containing(ModStands.HIEROPHANT_GREEN));
                    })
                    .build());

    private ModItems() {}

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        TABS.register(modEventBus);
    }
}
