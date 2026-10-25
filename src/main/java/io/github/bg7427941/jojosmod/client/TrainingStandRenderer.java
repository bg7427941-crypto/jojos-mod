package io.github.bg7427941.jojosmod.client;

import com.geckolib.model.DefaultedEntityGeoModel;
import com.geckolib.renderer.GeoEntityRenderer;
import io.github.bg7427941.jojosmod.JojosMod;
import io.github.bg7427941.jojosmod.entities.TrainingStandEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.Identifier;

/** GeckoLib rendering for temporary Stands, with a separate model for Star Platinum. */
public final class TrainingStandRenderer
        extends GeoEntityRenderer<TrainingStandEntity, EntityRenderState> {
    public TrainingStandRenderer(EntityRendererProvider.Context context) {
        this(context, "training_stand");
    }

    public TrainingStandRenderer(EntityRendererProvider.Context context, String model) {
        super(context, new DefaultedEntityGeoModel<>(Identifier.fromNamespaceAndPath(JojosMod.MOD_ID, model)));
        this.shadowRadius = 0.2F;
    }
}
