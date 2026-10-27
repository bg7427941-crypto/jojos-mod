package io.github.bg7427941.jojosmod.mixin;

import net.minecraft.client.player.ClientInput;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientInput.class)
public interface FrozenClientInput {
    @Accessor("moveVector") void jojosmod$setMoveVector(Vec2 vector);
}
