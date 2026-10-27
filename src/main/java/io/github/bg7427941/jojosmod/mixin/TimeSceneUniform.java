package io.github.bg7427941.jojosmod.mixin;

import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import io.github.bg7427941.jojosmod.client.TimeStopPresentation;
import java.util.Map;
import net.minecraft.client.renderer.PostPass;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Only our named uniform changes. Vanilla owns buffer disposal/reload with the post chain. */
@Mixin(PostPass.class)
public abstract class TimeSceneUniform {
    @Shadow @Final private Map<String, GpuBuffer> customUniforms;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void jojosmod$dynamicBuffer(CallbackInfo ci) {
        var previous = customUniforms.get("JojoTimeConfig");
        if (previous != null) {
            var dynamic = RenderSystem.getDevice().createBuffer(() -> "JoJo time scene",
                    GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, 16L);
            customUniforms.put("JojoTimeConfig", dynamic); previous.close();
        }
    }

    @Inject(method = "addToFrame", at = @At("HEAD"))
    private void jojosmod$updateTime(CallbackInfo ci) {
        var buffer = customUniforms.get("JojoTimeConfig");
        if (buffer == null) return;
        try (var stack = MemoryStack.stackPush()) {
            var data = Std140Builder.onStack(stack, 16);
            TimeStopPresentation.writeUniform(data);
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(buffer.slice(), data.get());
        }
    }
}
