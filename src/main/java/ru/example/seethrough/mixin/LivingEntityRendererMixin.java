package ru.example.seethrough.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.example.seethrough.GhostState;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> {

    // Насколько расширять хитбокс игрока (в блоках), чтобы эффект включался чуть заранее
    private static final double MARGIN = 0.35;

    @Inject(
        method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V",
        at = @At("RETURN")
    )
    private void seethrough$makeGhost(T entity, S state, float tickDelta, CallbackInfo ci) {
        boolean ghost = false;

        if (entity instanceof PlayerEntity) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player != null && entity != mc.player) {
                Vec3d cam = mc.gameRenderer.getCamera().getPos();
                Box box = entity.getBoundingBox().expand(MARGIN);
                ghost = box.contains(cam);
            }
        }

        ((GhostState) (Object) state).seethrough$setGhost(ghost);

        if (ghost) {
            // Призрачный режим для тела (~15% непрозрачности)
            state.invisible = true;
            state.invisibleToPlayer = false;
        }
    }

    // Пока игрок "призрак": не рисуем броню, элитры, плащ, предметы в руках и т.п.
    @Redirect(
        method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/entity/feature/FeatureRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/state/EntityRenderState;FF)V"
        )
    )
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void seethrough$skipFeatures(FeatureRenderer instance, MatrixStack matrices, VertexConsumerProvider providers,
                                         int light, EntityRenderState state, float yaw, float pitch) {
        if (state instanceof GhostState g && g.seethrough$isGhost()) return;
        instance.render(matrices, providers, light, state, yaw, pitch);
    }
}
