package io.github.bg7427941.jojosmod.client;
import com.geckolib.model.DefaultedEntityGeoModel;
import com.geckolib.renderer.GeoEntityRenderer;
import io.github.bg7427941.jojosmod.entities.DioEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.Identifier;
public final class DioRenderer extends GeoEntityRenderer<DioEntity,EntityRenderState> {
    public DioRenderer(EntityRendererProvider.Context context){super(context,new DefaultedEntityGeoModel<>(Identifier.fromNamespaceAndPath("jojosmod","dio")));shadowRadius=.35F;}
}
