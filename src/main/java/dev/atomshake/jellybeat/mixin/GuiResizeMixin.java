package dev.atomshake.jellybeat.mixin;
import dev.atomshake.jellybeat.GuiClip;
import com.mojang.blaze3d.systems.RenderPass;
import net.minecraft.client.gui.render.GuiRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(GuiRenderer.class)
public abstract class GuiResizeMixin {
 @Unique private boolean jellybeat$emptyClip;
 @Inject(method="executeDraw",at=@At("HEAD"))
 private void jellybeat$resetClip(CallbackInfo ci){jellybeat$emptyClip=false;}
 @Redirect(method="enableScissor",at=@At(value="INVOKE",target="Lcom/mojang/blaze3d/systems/RenderPass;enableScissor(IIII)V"))
 private void jellybeat$clip(RenderPass pass,int x,int y,int width,int height){
  var a=((RenderPassAccessor)pass).jellybeat$renderArea();
  var clip=GuiClip.intersect(x,y,width,height,a.x(),a.y(),a.width(),a.height());
  jellybeat$emptyClip=clip.empty();
  if(!jellybeat$emptyClip)pass.enableScissor(clip.x(),clip.y(),clip.width(),clip.height());
 }
 @Redirect(method="executeDraw",at=@At(value="INVOKE",target="Lcom/mojang/blaze3d/systems/RenderPass;drawIndexed(IIIII)V"))
 private void jellybeat$draw(RenderPass pass,int count,int instances,int first,int vertex,int instance){
  if(!jellybeat$emptyClip)pass.drawIndexed(count,instances,first,vertex,instance);
 }
}
