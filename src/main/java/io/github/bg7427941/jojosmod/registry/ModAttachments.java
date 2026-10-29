package io.github.bg7427941.jojosmod.registry;

import io.github.bg7427941.jojosmod.JojosMod;
import io.github.bg7427941.jojosmod.stands.StandData;
import io.github.bg7427941.jojosmod.stands.StandSession;
import io.github.bg7427941.jojosmod.stands.WillpowerData;
import io.github.bg7427941.jojosmod.stands.StarPlatinumSession;
import io.github.bg7427941.jojosmod.stands.StarPlatinumProgress;
import io.github.bg7427941.jojosmod.stands.StarLeapSession;
import io.github.bg7427941.jojosmod.stands.HierophantProgress;
import io.github.bg7427941.jojosmod.stands.HierophantSession;
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
                    // Manifested entities sync their own identity; the player's assignment stays private.
                    .sync((holder, recipient) -> holder == recipient, StandData.STREAM_CODEC)
                    .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<StandSession>> STAND_SESSION =
            ATTACHMENTS.register("stand_session", () -> AttachmentType.builder(() -> StandSession.EMPTY).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<WillpowerData>> WILLPOWER =
            ATTACHMENTS.register("willpower", () -> AttachmentType.builder(() -> WillpowerData.INITIAL)
                    .serialize(WillpowerData.CODEC)
                    .copyOnDeath()
                    .sync((holder, recipient) -> holder == recipient, WillpowerData.STREAM_CODEC)
                    .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<StarPlatinumSession>> STAR_SESSION =
            ATTACHMENTS.register("star_session", () -> AttachmentType.builder(() -> StarPlatinumSession.EMPTY).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<StarPlatinumProgress>> STAR_PROGRESS =
            ATTACHMENTS.register("star_progress", () -> AttachmentType.builder(() -> StarPlatinumProgress.EMPTY)
                    .serialize(StarPlatinumProgress.CODEC).copyOnDeath()
                    .sync((holder, recipient) -> holder == recipient, StarPlatinumProgress.STREAM_CODEC)
                    .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<HierophantSession>> HIEROPHANT_SESSION =
            ATTACHMENTS.register("hierophant_session", () -> AttachmentType.builder(() -> HierophantSession.EMPTY).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<HierophantProgress>> HIEROPHANT_PROGRESS =
            ATTACHMENTS.register("hierophant_progress", () -> AttachmentType.builder(() -> HierophantProgress.EMPTY)
                    .serialize(HierophantProgress.CODEC).copyOnDeath()
                    .sync((holder, recipient) -> holder == recipient, HierophantProgress.STREAM_CODEC).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<io.github.bg7427941.jojosmod.stands.CrusaderSession>> CRUSADER_SESSION =
            ATTACHMENTS.register("crusader_session", () -> AttachmentType.builder(io.github.bg7427941.jojosmod.stands.CrusaderSession::new).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<io.github.bg7427941.jojosmod.stands.CrusaderProgress>> CRUSADER_PROGRESS =
            ATTACHMENTS.register("crusader_progress", () -> AttachmentType.builder(() -> io.github.bg7427941.jojosmod.stands.CrusaderProgress.EMPTY)
                    .serialize(io.github.bg7427941.jojosmod.stands.CrusaderProgress.CODEC).copyOnDeath()
                    .sync((holder, recipient) -> holder == recipient, io.github.bg7427941.jojosmod.stands.CrusaderProgress.STREAM).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> TIME_FROZEN =
            ATTACHMENTS.register("time_frozen", () -> AttachmentType.builder(() -> false)
                    .sync((holder, recipient) -> true, net.minecraft.network.codec.ByteBufCodecs.BOOL).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<io.github.bg7427941.jojosmod.abilities.HierophantTools.State>> HIEROPHANT_TOOLS =
            ATTACHMENTS.register("hierophant_tools", () -> AttachmentType.builder(io.github.bg7427941.jojosmod.abilities.HierophantTools.State::new).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<io.github.bg7427941.jojosmod.journey.JourneyProgress>> JOURNEY_PROGRESS =
            ATTACHMENTS.register("journey_progress", () -> AttachmentType.builder(() -> io.github.bg7427941.jojosmod.journey.JourneyProgress.EMPTY)
                    .serialize(io.github.bg7427941.jojosmod.journey.JourneyProgress.CODEC).copyOnDeath()
                    .sync((holder,recipient)->holder==recipient,io.github.bg7427941.jojosmod.journey.JourneyProgress.STREAM_CODEC).build());

    private ModAttachments() {}

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<StarLeapSession>> STAR_LEAP =
            ATTACHMENTS.register("star_leap", () -> AttachmentType.builder(() -> StarLeapSession.EMPTY).build());

    public static void register(IEventBus modEventBus) {
        ATTACHMENTS.register(modEventBus);
    }
}
