package dev.atomshake.jellybeat.mixin;
import dev.atomshake.jellybeat.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.*;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.EntityType;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityDanceMixin {
 @Inject(method="extractEntity",at=@At("RETURN"))
 private void jellybeat$extract(net.minecraft.world.entity.Entity entity,float delta,org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<EntityRenderState> ci){
  ((VoiceRenderState)ci.getReturnValue()).jellybeat$voice(JellyBeat.CONFIG!=null&&JellyBeat.CONFIG.voicechat&&entity instanceof net.minecraft.world.entity.player.Player?VoiceBeats.sample(entity.getUUID()):VoiceBeats.Sample.NONE);
 }

 @Inject(method="submit",at=@At(value="INVOKE",target="Lnet/minecraft/client/renderer/entity/EntityRenderer;submit(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V"))
 private void jellybeat$before(EntityRenderState s,CameraRenderState camera,double x,double y,double z,PoseStack pose,SubmitNodeCollector collector,CallbackInfo ci){
  pose.pushPose();
  Config c=JellyBeat.CONFIG;if(c==null)return;
  DanceTarget target=EntityDanceTarget.of(s);
  float raw=JellyBeat.AUDIO.pulse();double time=JellyBeat.AUDIO.seconds();
  if(target==DanceTarget.PLAYERS&&c.voicechat){var voice=((VoiceRenderState)s).jellybeat$voice();raw=voice.override(raw);if(voice.weight()>0)time=voice.seconds();}
  float power=c.power(target,raw);if(power<.001f||s.distanceToCameraSq>128*128)return;
  double phase=s.entityType==null?0:s.entityType.hashCode()%31*.2;
  var d=DanceMath.shape(time,phase,power);
  pose.translate(0,d.lift(),0);pose.mulPose(new Quaternionf().rotationZ(d.tilt()).rotateY(d.yaw()));pose.scale(d.x(),d.y(),d.z());
 }
 @Inject(method="submit",at=@At(value="INVOKE",target="Lnet/minecraft/client/renderer/entity/EntityRenderer;submit(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",shift=At.Shift.AFTER))
 private void jellybeat$after(EntityRenderState s,CameraRenderState camera,double x,double y,double z,PoseStack pose,SubmitNodeCollector collector,CallbackInfo ci){pose.popPose();}
}

