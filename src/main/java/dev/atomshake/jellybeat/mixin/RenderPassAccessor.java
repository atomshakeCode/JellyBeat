package dev.atomshake.jellybeat.mixin;
import com.mojang.blaze3d.systems.RenderPass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(RenderPass.class)
public interface RenderPassAccessor {
 @Accessor("renderArea") RenderPass.RenderArea jellybeat$renderArea();
}
