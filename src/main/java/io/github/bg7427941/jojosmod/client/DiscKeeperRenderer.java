package io.github.bg7427941.jojosmod.client;

import com.geckolib.model.DefaultedEntityGeoModel;
import com.geckolib.renderer.GeoEntityRenderer;
import io.github.bg7427941.jojosmod.JojosMod;
import io.github.bg7427941.jojosmod.entities.DiscKeeperEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.Identifier;

/** Reuses the project's original mannequin until the keeper has dedicated artwork. */
public final class DiscKeeperRenderer extends GeoEntityRenderer<DiscKeeperEntity, EntityRenderState> {
    public DiscKeeperRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedEntityGeoModel<>(
                Identifier.fromNamespaceAndPath(JojosMod.MOD_ID, "training_stand")));
        this.shadowRadius = 0.3F;
    }
}
