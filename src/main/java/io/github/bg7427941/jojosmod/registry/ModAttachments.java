package io.github.bg7427941.jojosmod.registry;

import io.github.bg7427941.jojosmod.JojosMod;
import io.github.bg7427941.jojosmod.stands.StandData;
import io.github.bg7427941.jojosmod.stands.StandSession;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModAttachments {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, JojosMod.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<StandData>> STAND_DATA =
            ATTACHMENTS.register("stand_data", () -> AttachmentType.builder(() -> StandData.EMPTY)
                    .serialize(StandData.CODEC)
                    .copyOnDeath()
                    // Assignment is private player data until other clients need it for rendered Stands.
                    .sync((holder, recipient) -> holder == recipient, StandData.STREAM_CODEC)
                    .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<StandSession>> STAND_SESSION =
            ATTACHMENTS.register("stand_session", () -> AttachmentType.builder(() -> StandSession.EMPTY).build());

    private ModAttachments() {}

    public static void register(IEventBus modEventBus) {
        ATTACHMENTS.register(modEventBus);
    }
}
