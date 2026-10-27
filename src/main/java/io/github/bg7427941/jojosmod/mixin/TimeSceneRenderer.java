package io.github.bg7427941.jojosmod.mixin;

import io.github.bg7427941.jojosmod.client.TimeStopPresentation;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class TimeSceneRenderer {
    @Inject(method = "update", at = @At("TAIL"))
    private void jojosmod$timeScene(DeltaTracker delta, CallbackInfo ci) {
        TimeStopPresentation.collect(((GameRenderer)(Object)this).getRequestedPostEffects(), delta.getGameTimeDeltaPartialTick(false));
    }
}
