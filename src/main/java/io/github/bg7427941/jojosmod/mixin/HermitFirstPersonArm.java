package io.github.bg7427941.jojosmod.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.bg7427941.jojosmod.client.HermitPurpleLayer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public abstract class HermitFirstPersonArm {
    @Inject(method = "renderHand", at = @At("TAIL"))
    private void jojosmod$armVines(PoseStack pose, SubmitNodeCollector nodes, int light, Identifier texture,
                                  ModelPart arm, boolean sleeve, CallbackInfo ci) {
        HermitPurpleLayer.firstPerson(pose, nodes, light, arm);
    }
}
