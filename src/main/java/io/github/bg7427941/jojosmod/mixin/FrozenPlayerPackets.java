package io.github.bg7427941.jojosmod.mixin;

import io.github.bg7427941.jojosmod.abilities.TimeStop;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Gate gameplay packets on the server thread while allowing keep-alives and teleport acknowledgements. */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class FrozenPlayerPackets {
    @Shadow public ServerPlayer player;
    @Inject(method = {"handleMovePlayer", "handleMoveVehicle", "handlePlayerAction", "handleInteract",
            "handleUseItem", "handleUseItemOn", "handleContainerClick"}, at = @At("HEAD"), cancellable = true)
    private void jojosmod$freezeGameplay(CallbackInfo ci) {
        // The vanilla handler first reschedules off-thread calls; never inspect game state on the IO thread.
        if (player.level().getServer().isSameThread() && TimeStop.isFrozen(player)) ci.cancel();
    }
}
