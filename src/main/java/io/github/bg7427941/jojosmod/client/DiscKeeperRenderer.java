package io.github.bg7427941.jojosmod.client;

import com.geckolib.model.DefaultedEntityGeoModel;
import com.geckolib.renderer.GeoEntityRenderer;
import io.github.bg7427941.jojosmod.JojosMod;
import io.github.bg7427941.jojosmod.entities.DiscKeeperEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.Identifier;

/** Original robed keeper, separate from the training mannequin and DIO. */
public final class DiscKeeperRenderer extends GeoEntityRenderer<DiscKeeperEntity, EntityRenderState> {
    public DiscKeeperRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedEntityGeoModel<>(
                Identifier.fromNamespaceAndPath(JojosMod.MOD_ID, "disc_keeper")));
        this.shadowRadius = 0.3F;
    }
}
