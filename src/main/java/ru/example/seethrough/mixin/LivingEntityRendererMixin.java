package ru.example.seethrough.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> {

    // Насколько расширять хитбокс игрока (в блоках), чтобы эффект включался чуть заранее
    private static final double MARGIN = 0.35;

    @Inject(
        method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V",
        at = @At("RETURN")
    )
    private void seethrough$makeGhost(T entity, S state, float tickDelta, CallbackInfo ci) {
        if (!(entity instanceof PlayerEntity)) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || entity == mc.player) return;

        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        Box box = entity.getBoundingBox().expand(MARGIN);

        if (box.contains(cam)) {
            // Включаем ванильный "призрачный" режим (как у невидимого союзника): ~15% непрозрачности
            state.invisible = true;
            state.invisibleToPlayer = false;
        }
    }
}
