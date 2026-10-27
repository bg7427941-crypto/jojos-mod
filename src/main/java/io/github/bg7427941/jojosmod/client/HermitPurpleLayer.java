package io.github.bg7427941.jojosmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.bg7427941.jojosmod.JojosMod;
import io.github.bg7427941.jojosmod.entities.TrainingStandEntity;
import io.github.bg7427941.jojosmod.stands.PartThreeStand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Avatar;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import net.neoforged.neoforge.client.renderstate.AvatarRenderStateModifier;

/** Shares the actual player arm transforms: walking, head/body turn, crouch, swim and items. */
public final class HermitPurpleLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(JojosMod.MOD_ID, "textures/entity/hermit_purple_arms.png");
    private static final ContextKey<Boolean> ACTIVE = new ContextKey<>(Identifier.fromNamespaceAndPath(JojosMod.MOD_ID, "hermit_arms"));
    private static final ModelPart[] REGULAR = {arm(false, false), arm(true, false)};
    private static final ModelPart[] SLIM = {arm(false, true), arm(true, true)};
    private final boolean slim;

    private HermitPurpleLayer(RenderLayerParent<AvatarRenderState, PlayerModel> renderer, boolean slim) {
        super(renderer); this.slim = slim;
    }

    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (var skin : event.getSkins()) {
            var renderer = event.getPlayerRenderer(skin);
            if (renderer != null) renderer.addLayer(new HermitPurpleLayer(renderer, skin == net.minecraft.world.entity.player.PlayerModelType.SLIM));
        }
    }

    public static void registerState(RegisterRenderStateModifiersEvent event) {
        event.registerAvatarEntityModifier(new AvatarRenderStateModifier() {
            public <T extends Avatar & ClientAvatarEntity> void accept(T player, AvatarRenderState state) {
                state.setRenderData(ACTIVE, active(player));
            }
        });
    }

    private static boolean active(Avatar player) {
        return player.isAlive() && !player.isInvisible() && !player.isSpectator()
                && !player.level().getEntitiesOfClass(TrainingStandEntity.class, player.getBoundingBox().inflate(16),
                stand -> stand.getOwnerEntityId() == player.getId() && !stand.isDismissing()
                        && stand.getStandDefinition().id().equals(PartThreeStand.HERMIT_PURPLE.id())).isEmpty();
    }

    @Override
    public void submit(PoseStack pose, SubmitNodeCollector nodes, int light, AvatarRenderState state, float yaw, float pitch) {
        if (!Boolean.TRUE.equals(state.getRenderData(ACTIVE)) || state.isInvisible) return;
        submitArm(pose, nodes, light, getParentModel().rightArm, false, slim);
        submitArm(pose, nodes, light, getParentModel().leftArm, true, slim);
    }

    /** Runs at the same point as vanilla's first-person hand, after equip/swing/bob transforms. */
    public static void firstPerson(PoseStack pose, SubmitNodeCollector nodes, int light, ModelPart arm) {
        var player = Minecraft.getInstance().player;
        if (player == null || !active(player)) return;
        boolean left = arm.x > 0;
        boolean slim = player.getSkin().model() == net.minecraft.world.entity.player.PlayerModelType.SLIM;
        submitArm(pose, nodes, light, arm, left, slim);
    }

    private static void submitArm(PoseStack pose, SubmitNodeCollector nodes, int light, ModelPart source, boolean left, boolean slim) {
        pose.pushPose();
        source.translateAndRotate(pose);
        // The local mesh never mutates: the collector snapshots each player's pose independently.
        nodes.order(2).submitModelPart((slim ? SLIM : REGULAR)[left ? 1 : 0], pose,
                RenderTypes.entityCutout(TEXTURE), light, OverlayTexture.NO_OVERLAY, null);
        pose.popPose();
    }

    private static ModelPart arm(boolean left, boolean slim) {
        var mesh = new MeshDefinition();
        var root = mesh.getRoot();
        float center = (left ? 1 : -1) * (slim ? .5F : 1F);
        float radiusX = slim ? 2.15F : 2.65F;
        for (int i = 0; i < 36; i++) {
            double angle = i * .4 + (left ? Math.PI : 0);
            float x = center + (float)Math.cos(angle) * radiusX;
            float y = -1.65F + i * .33F;
            float z = (float)Math.sin(angle) * 2.65F;
            root.addOrReplaceChild("coil_" + i, CubeListBuilder.create().texOffs((i % 4) * 8, 0)
                    .addBox(-.6F, -.6F, -.6F, 1.2F, 1.35F, 1.2F), PartPose.offset(x, y, z));
            if (i % 6 == 2) root.addOrReplaceChild("thorn_" + i, CubeListBuilder.create().texOffs(32, 0)
                    .addBox(-.24F, -.7F, -.24F, .48F, 1.4F, .48F),
                    PartPose.offsetAndRotation(x * 1.08F, y, z * 1.12F, 0, 0, left ? .7F : -.7F));
        }
        return LayerDefinition.create(mesh, 64, 32).bakeRoot();
    }
}
