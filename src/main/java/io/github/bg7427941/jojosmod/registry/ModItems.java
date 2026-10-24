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

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> JOJOS_TAB =
            TABS.register("jojos_mod", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.jojosmod"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> STAND_ARROW.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(STAND_ARROW.get());
                        output.accept(STAND_DISC_FRAGMENT.get());
                        output.accept(EMPTY_STAND_DISC.get());
                        output.accept(STAND_DISC.get());
                        output.accept(ECHOES_ACT_1_CORE.get());
                        output.accept(LITTLE_FEET_CORE.get());
                        output.accept(StandDiscItem.containing(ModStands.ECHOES_ACT_1));
                        output.accept(StandDiscItem.containing(ModStands.LITTLE_FEET));
                    })
                    .build());

    private ModItems() {}

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        TABS.register(modEventBus);
    }
}
