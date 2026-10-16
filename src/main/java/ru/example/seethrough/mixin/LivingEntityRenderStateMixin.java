package ru.example.seethrough.mixin;

import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import ru.example.seethrough.GhostState;

@Mixin(LivingEntityRenderState.class)
public abstract class LivingEntityRenderStateMixin implements GhostState {
    private boolean seethrough$ghost;

    @Override
    public boolean seethrough$isGhost() { return seethrough$ghost; }

    @Override
    public void seethrough$setGhost(boolean ghost) { this.seethrough$ghost = ghost; }
}
